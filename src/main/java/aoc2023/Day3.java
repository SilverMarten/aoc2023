package aoc2023;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/3
 * 
 * @author Paul Cormier
 *
 */
public class Day3 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day3.class);

    private static final String INPUT_TXT = "input/Day3.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day3.txt";

    private static final class Coordinate {
        int row;
        int column;

        Coordinate(int row, int column) {
            this.row = row;
            this.column = column;
        }

        /**
         * @return The set of adjacent coordinates to this coordinate.
         */
        Set<Coordinate> findAdjacent() {
            return IntStream.rangeClosed(-1, 1)
                            .mapToObj(x -> IntStream.rangeClosed(-1, 1)
                                                    .filter(y -> !(x == 0 && y == 0))
                                                    .mapToObj(y -> new Coordinate(this.row + y, this.column + x)))
                            .flatMap(Function.identity())
                            // .filter(c -> c.column >= 0 && c.row >= 0)
                            .collect(Collectors.toSet());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + column;
            result = prime * result + row;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Coordinate other = (Coordinate) obj;
            if (column != other.column)
                return false;
            if (row != other.row)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return String.format("(row=%s, column=%s)", row, column);
        }

    }

    /**
     * A container to hold instances of numbers found in the input, which may be a
     * part number.
     */
    private static final class FoundNumber {
        final int value;

        public FoundNumber(int value) {
            this.value = value;
        }

        int getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace(testLines.toString());

        log.info("The sum of the part numbers in the schematic is: {} (should be 4361)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the part numbers in the schematic: {} (should be higher than 508745)", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * What is the sum of all of the part numbers in the engine schematic?
     * 
     * @param lines The lines of text to be interpreted as an engine schematic.
     * @return The sum of the part numbers in the schematic.
     */
    private static int part1(final List<String> lines) {

        // Map out the locations of the numbers
        Map<Coordinate, FoundNumber> numberLocations = mapNumbers(lines);

        // Map out the location of the symbols
        List<Coordinate> symbolLocations = listSymbols(lines);

        // For each symbol, find adjacent part numbers, and sum them
        int sum = symbolLocations.stream()
                                 .flatMap(c -> c.findAdjacent().stream())
                                 .map(numberLocations::get)
                                 .filter(Objects::nonNull)
                                 .distinct()
                                 .mapToInt(FoundNumber::getValue)
                                 .sum();

        return sum;
    }

    /**
     * Given a list of lines, create a map of the locations containing numbers.
     * 
     * @param lines The lines of text in which to find numbers.
     * @return A map of {@link Coordinate}s to {@link FoundNumber}s which may be
     *     part numbers.
     */
    private static Map<Coordinate, FoundNumber> mapNumbers(List<String> lines) {
        Map<Coordinate, FoundNumber> numbers = new HashMap<>();

        AtomicInteger row = new AtomicInteger(0);
        for (String line : lines) {
            Stream.of(line.split("\\D"))
                  .filter(StringUtils::isNotBlank)
                  .forEach(s -> {
                      FoundNumber number = new FoundNumber(Integer.parseInt(s));
                      IntStream.range(0, s.length())
                               .mapToObj(i -> new Coordinate(row.get(), line.indexOf(s) + i))
                               .forEach(c -> numbers.put(c, number));
                  });

            // for (int column = 0; column < line.length(); column++) {
            // char symbol = line.charAt(column);
            // if (!('.' == symbol || Character.isDigit(symbol)))
            // symbols.add(new Coordinate(row, column));
            // }
            row.incrementAndGet();
        }

        log.debug("Found the following numbers: {}", numbers);

        return numbers;
    }

    /**
     * Given a list of lines, create a list of the locations containing symbols.
     * Periods (.) do not count as a symbol.
     * 
     * @param lines The lines of text in which to find symbols.
     * @return A list of {@link Coordinate}s containing symbols.
     */
    private static List<Coordinate> listSymbols(List<String> lines) {

        List<Coordinate> symbols = new ArrayList<>();
        int row = 0;
        for (String line : lines) {
            for (int column = 0; column < line.length(); column++) {
                char symbol = line.charAt(column);
                if (!('.' == symbol || Character.isDigit(symbol)))
                    symbols.add(new Coordinate(row, column));
            }
            row++;
        }

        log.debug("Found symbols: {}", symbols);
        return symbols;
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}