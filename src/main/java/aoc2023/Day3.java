package aoc2023;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final class Coordinate implements Comparable<Coordinate> {
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

        @Override
        public int compareTo(Coordinate o) {
            int result = Integer.compare(this.row, o.row);

            return result == 0 ? Integer.compare(this.column, o.column) : result;
        }

    }

    /**
     * A container to hold instances of numbers found in the input, which may be a
     * part number.
     */
    private static final class FoundNumber {
        final int value;
        final List<Coordinate> locations;
        boolean part = false;

        public FoundNumber(int value) {
            this.value = value;
            this.locations = new ArrayList<>();
        }

        int getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return String.format("%s%s %s", this.value, part ? "*" : "", this.locations);
        }

    }

    /**
     * A container to hold instances of numbers symbol in the input.
     */
    private static final class FoundSymbol {
        final char value;
        final Coordinate location;
        final Set<FoundNumber> partNumbers;

        public FoundSymbol(char value, Coordinate location) {
            super();
            this.value = value;
            this.location = location;
            this.partNumbers = new HashSet<>();
        }

        @Override
        public String toString() {
            String parts = this.partNumbers.stream()
                                           .map(n -> Integer.toString(n.value))
                                           .collect(Collectors.joining(", "));
            return String.format("%s %s parts: %s", this.value, this.location, parts);
        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        // log.setLevel(Level.TRACE);
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace(testLines.toString());

        log.info("The sum of the part numbers in the schematic is: {} (should be 4361)", part1(testLines));

        log.setLevel(Level.DEBUG);
        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the part numbers in the schematic: {} (should be between 508745 and 810041, and not 511038)",
                 part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{} (should be 467835)", part2(testLines));

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
        List<FoundSymbol> symbols = listSymbols(lines);

        // For each symbol, find adjacent numbers, and sum them
        /* int sum = symbols.stream()
                         .flatMap(s -> s.location.findAdjacent().stream())
                         .map(numberLocations::get)
                         .filter(Objects::nonNull)
                         .distinct()
                         .mapToInt(FoundNumber::getValue)
                         .sum();*/

        /* int sum = 0;
        for (FoundSymbol symbol : symbols) {
            sum += symbol.location.findAdjacent().stream()
                                  .map(numberLocations::get)
                                  .filter(Objects::nonNull)
                                  .distinct()
                                  .peek(n -> {
                                      symbol.partNumbers.add(n);
                                      n.part = true;
                                  })
                                  .mapToInt(FoundNumber::getValue)
                                  .sum();
        }*/

        for (FoundSymbol symbol : symbols) {
            symbol.location.findAdjacent().stream()
                           .map(numberLocations::get)
                           .filter(Objects::nonNull)
                           .distinct()
                           .forEach(n -> {
                               symbol.partNumbers.add(n);
                               n.part = true;
                           });
        }
        int sum = numberLocations.values().stream().distinct().filter(n -> n.part).mapToInt(FoundNumber::getValue)
                                 .sum();

        log.debug("Found symbols:\n{}", symbols.stream().map(FoundSymbol::toString).collect(Collectors.joining("\n")));

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
            AtomicInteger lastColumn = new AtomicInteger(0);
            Stream.of(line.split("\\D"))
                  .filter(StringUtils::isNotBlank)
                  .forEach(s -> {
                      FoundNumber number = new FoundNumber(Integer.parseInt(s));
                      int column = line.indexOf(s, lastColumn.get());
                      /*if (column < 0)
                          throw new IndexOutOfBoundsException("The number " + s
                                                              + " wasn't found in the line, starting from index "
                                                              + lastColumn.get() + "\n" + line);*/
                      IntStream.range(0, s.length())
                               .mapToObj(i -> new Coordinate(row.get(), column + i))
                               .forEach(c -> {
                                   number.locations.add(c);
                                   numbers.put(c, number);
                               });
                      lastColumn.set(column + s.length());
                  });

            row.incrementAndGet();
        }

        log.trace("Found the following numbers:\n{}", formatFoundNumberMap(numbers));

        return numbers;
    }

    /**
     * Given a list of lines, create a list of the locations containing symbols.
     * Periods (.) do not count as a symbol.
     * 
     * @param lines The lines of text in which to find symbols.
     * @return A list of {@link FoundSymbol}s.
     */
    private static List<FoundSymbol> listSymbols(List<String> lines) {

        List<FoundSymbol> symbols = new ArrayList<>();
        int row = 0;
        for (String line : lines) {
            for (int column = 0; column < line.length(); column++) {
                char symbol = line.charAt(column);
                if (!('.' == symbol || Character.isDigit(symbol)))
                    symbols.add(new FoundSymbol(symbol, new Coordinate(row, column)));
            }
            row++;
        }

        // log.trace("Found symbols:\n{}",
        // symbols.stream().map(FoundSymbol::toString).collect(Collectors.joining("\n")));
        return symbols;
    }

    private static String formatFoundNumberMap(Map<Coordinate, FoundNumber> map) {
        return map.values().stream()
                  .distinct()
                  .sorted(Comparator.comparing(n -> n.locations.get(0)))
                  .map(FoundNumber::toString)
                  .collect(Collectors.joining(",\n"));
    }

    /**
     * What is the sum of all of the gear ratios in your engine schematic?
     * 
     * @param lines The lines of text to be interpreted as an engine schematic.
     * @return The sum of the products of the part numbers of gears in the
     *     schematic.
     */
    private static int part2(final List<String> lines) {

        // Map out the locations of the numbers
        Map<Coordinate, FoundNumber> numberLocations = mapNumbers(lines);

        // Map out the location of the symbols
        List<FoundSymbol> symbols = listSymbols(lines);

        // For each symbol, find adjacent parts
        for (FoundSymbol symbol : symbols) {
            symbol.location.findAdjacent().stream()
                           .map(numberLocations::get)
                           .filter(Objects::nonNull)
                           .distinct()
                           .forEach(n -> {
                               symbol.partNumbers.add(n);
                               n.part = true;
                           });
        }

        return symbols.stream()
                      .filter(g -> g.partNumbers.size() == 2)
                      .mapToInt(g -> {
                          AtomicInteger product = new AtomicInteger(1);
                          g.partNumbers.stream().mapToInt(FoundNumber::getValue)
                                       .forEach(v -> product.set(product.get() * v));
                          return product.get();
                      })
                      .sum();
    }

}