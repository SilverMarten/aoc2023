package aoc._2023;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.Range;
import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/14
 * 
 * @author Paul Cormier
 *
 */
public class Day14 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day14.class);

    private static final String INPUT_TXT = "input/Day14.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day14.txt";

    private static final char ROUND_ROCK_CHAR = 'O';

    private static final char SQUARE_ROCK_CHAR = '#';

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The total load on the north support beam is: {} (should be 136)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The total load on the north support beam is: {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * Tilt the platform so that the rounded rocks all roll north. Afterward,
     * what is the total load on the north support beams?
     * 
     * @param lines
     *            The lines representing the positions of the rocks on the
     *            platform.
     * @return The total load on the north support beam.
     */
    private static int part1(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        // Parse the map for the squareRocks
        Set<Coordinate> squareRocks = Coordinate.mapCoordinates(lines, SQUARE_ROCK_CHAR);

        // Add some implicit square rocks in last row
        IntStream.rangeClosed(1, columns).mapToObj(c -> new Coordinate(rows + 1, c)).forEach(squareRocks::add);

        // Parse the map for the roundRocks
        Set<Coordinate> roundRocks = Coordinate.mapCoordinates(lines, ROUND_ROCK_CHAR);

        // For each column, count the round rocks between the square rocks
        return IntStream.rangeClosed(1, columns).map(column -> {
            AtomicInteger lastRow = new AtomicInteger(0);
            Set<Coordinate> roundRocksInColumn = roundRocks.stream().filter(r -> r.getColumn() == column).collect(Collectors.toSet());

            int columnSum = squareRocks.stream()
                                       .filter(r -> r.getColumn() == column)
                                       .mapToInt(Coordinate::getRow)
                                       .sorted()
                                       .map(row -> {
                                           int rocks = (int) IterableUtils.countMatches(roundRocksInColumn,
                                                                                        r -> Range.of(lastRow.get() + 1, row - 1)
                                                                                                  .contains(r.getRow()));
                                           int sum = 0;
                                           if (rocks != 0) {
                                               int maxValue = rows - lastRow.get();
                                               sum = (int) (rocks * maxValue - rocks * (rocks - 1) / 2.);
                                               log.trace("The sum from {} to {} is: {}", maxValue, maxValue - rocks + 1, sum);
                                           }
                                           lastRow.set(row);
                                           return sum;
                                       })
                                       .sum();
            log.debug("Column {} sum: {}", column, columnSum);
            return columnSum;
        }).sum();

    }

    /**
     * Run the spin cycle for 1000000000 cycles. Afterward, what is the total
     * load on the north support beams?
     * 
     * @param lines
     *            The lines representing the positions of the rocks on the
     *            platform.
     * @return The total load on the north support beam after 1,000,000,000 spin
     *         cycles.
     */
    private static int part2(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        // Parse the map for the squareRocks
        Set<Coordinate> squareRocks = Coordinate.mapCoordinates(lines, SQUARE_ROCK_CHAR);

        // Add some implicit square rocks around the frame
        IntStream.rangeClosed(1, columns).mapToObj(c -> new Coordinate(0, c)).forEach(squareRocks::add);
        IntStream.rangeClosed(1, columns).mapToObj(c -> new Coordinate(rows + 1, c)).forEach(squareRocks::add);
        IntStream.rangeClosed(1, rows).mapToObj(r -> new Coordinate(r, 0)).forEach(squareRocks::add);
        IntStream.rangeClosed(1, rows).mapToObj(r -> new Coordinate(r, columns + 1)).forEach(squareRocks::add);

        // Parse the map for the roundRocks
        Set<Coordinate> roundRocks = Coordinate.mapCoordinates(lines, ROUND_ROCK_CHAR);

        log.atDebug()
           .setMessage("Start:\n{}")
           .addArgument(() -> Coordinate.printMap(rows, columns, squareRocks, SQUARE_ROCK_CHAR, roundRocks, ROUND_ROCK_CHAR))
           .log();

        IntStream.rangeClosed(1, 3).forEach(i -> {
            Set<Coordinate> nextRoundRocks = spinCycle(rows, columns, roundRocks, squareRocks);

            log.atDebug()
               .setMessage("After {} cycle{}:\n{}")
               .addArgument(i)
               .addArgument(() -> i > 1 ? "s" : "")
               .addArgument(() -> Coordinate.printMap(rows, columns, squareRocks, SQUARE_ROCK_CHAR, nextRoundRocks, ROUND_ROCK_CHAR))
               .log();
        });

        return -1;
    }

    /**
     * Translate the round rocks as far as they can go north, west, south, then
     * east.
     * 
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @param roundRocks
     *            The positions of the round rocks.
     * @param squareRocks
     *            The positions of the square rocks.
     * @return The positions of the round rocks after translation.
     */
    private static Set<Coordinate> spinCycle(int rows, int columns, Set<Coordinate> roundRocks, Set<Coordinate> squareRocks) {
        Set<Coordinate> rocksLeftToTranslate = new HashSet<>(roundRocks);
        Set<Coordinate> translatedRocks = new HashSet<>();

        // Translate all rocks north
        // Fill each row with the next round rock in the column 
        for (int row = 1; row <= rows; row++) {
            for (int column = 1; column <= columns; column++) {
                Coordinate coordinateToCheck = new Coordinate(row, column);
                // If there's already a square stone here, continue
                if (squareRocks.contains(coordinateToCheck))
                    continue;

                // Look down the column for the next round rock, stop at square rocks
                int columnToCheck = column;
                int rowToCheck = row;
                int nextSquareRockRow = squareRocks.stream().filter(r -> r.getColumn() == columnToCheck &&
                                                                         r.getRow() >= rowToCheck)
                                                   .mapToInt(Coordinate::getRow)
                                                   .sorted()
                                                   .findFirst()
                                                   .getAsInt();
                Range<Integer> rowsToCheck = Range.of(row, nextSquareRockRow - 1);
                Optional<Coordinate> rock = rocksLeftToTranslate.stream()
                                                                .filter(r -> r.getColumn() == columnToCheck &&
                                                                             rowsToCheck.contains(r.getRow()))
                                                                .sorted(Comparator.comparing(Coordinate::getRow))
                                                                .findFirst();
                rock.ifPresent(r -> {
                    rocksLeftToTranslate.remove(r);
                    translatedRocks.add(coordinateToCheck);
                });
            }
        }

        // Translate all rocks west
        rocksLeftToTranslate.addAll(translatedRocks);
        translatedRocks.clear();
        // Fill each column with the next round rock in the row 
        for (int column = 1; column <= columns; column++) {
            for (int row = 1; row <= rows; row++) {
                Coordinate coordinateToCheck = new Coordinate(row, column);
                // If there's already a square stone here, continue
                if (squareRocks.contains(coordinateToCheck))
                    continue;

                // Look down the row for the next round rock, stop at square rocks
                int columnToCheck = column;
                int rowToCheck = row;
                int nextSquareRockColumn = squareRocks.stream().filter(r -> r.getColumn() >= columnToCheck &&
                                                                            r.getRow() == rowToCheck)
                                                      .mapToInt(Coordinate::getColumn)
                                                      .sorted()
                                                      .findFirst()
                                                      .getAsInt();
                Range<Integer> columnsToCheck = Range.of(column, nextSquareRockColumn - 1);
                Optional<Coordinate> rock = rocksLeftToTranslate.stream()
                                                                .filter(r -> r.getRow() == rowToCheck &&
                                                                             columnsToCheck.contains(r.getColumn()))
                                                                .sorted(Comparator.comparing(Coordinate::getColumn))
                                                                .findFirst();
                rock.ifPresent(r -> {
                    rocksLeftToTranslate.remove(r);
                    translatedRocks.add(coordinateToCheck);
                });
            }
        }

        return translatedRocks;
    }

}