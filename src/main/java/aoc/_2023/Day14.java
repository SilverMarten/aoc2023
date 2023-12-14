package aoc._2023;

import java.util.List;
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
        Set<Coordinate> squareRocks = Coordinate.mapCoordinates(lines, '#');

        // Add some implicit square rocks in last row
        IntStream.rangeClosed(1, columns).mapToObj(c -> new Coordinate(rows + 1, c)).forEach(squareRocks::add);

        // Parse the map for the roundRocks
        Set<Coordinate> roundRocks = Coordinate.mapCoordinates(lines, 'O');

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
                                               log.debug("The sum from {} to {} is: {}", maxValue, maxValue - rocks + 1, sum);
                                           }
                                           lastRow.set(row);
                                           return sum;
                                       })
                                       .sum();
            log.debug("Column {} sum: {}", column, columnSum);
            return columnSum;
        }).sum();

    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}