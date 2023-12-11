package aoc2023;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.IterableUtils;
import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/11
 * 
 * @author Paul Cormier
 *
 */
public class Day11 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day11.class);

    private static final String INPUT_TXT = "input/Day11.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day11.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The sum of the shortest paths between the stars is: {} (should be 374)", solve(testLines, 2));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the shortest paths between the stars is: {}", solve(lines, 2));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("The sum of the shortest paths between the stars is: {} (should be 1030)", solve(testLines, 10));
        log.info("The sum of the shortest paths between the stars is: {} (should be 8410)", solve(testLines, 100));

        log.setLevel(Level.INFO);

        log.info("The sum of the shortest paths between the stars is: {} (should be higher than 2070665131)", solve(lines, 1_000_000));
    }

    /**
     * Expand the universe, by a factor of n, then find the length of the
     * shortest path between every pair of galaxies. What is the sum of these
     * lengths?
     * 
     * @param lines
     *            The lines representing the map of the stars.
     * @param factor
     *            The factor by which to expand empty rows and columns.
     * @return The sum of the shortest paths between each permutation of star
     *         pairs.
     */
    private static long solve(final List<String> lines, int factor) {

        // Find stars
        int rows = lines.size();
        int columns = lines.get(0).length();

        Set<Coordinate> starMap = mapStars(lines);

        // Expand universe
        Set<Coordinate> expandedStarMap = expandStarMap(rows, columns, starMap, factor);

        // Compute paths
        Queue<Coordinate> starsToCheck = new ArrayDeque<>(expandedStarMap);

        long sumOfDistances = 0;
        while (!starsToCheck.isEmpty()) {
            Coordinate star = starsToCheck.poll();
            //            sumOfDistances += starsToCheck.stream().mapToLong(s -> manhattanDistanceBetween(s, star)).sum();
            for (Coordinate star2 : starsToCheck) {
                log.atTrace()
                   .setMessage("Distance between {} and {}: {}")
                   .addArgument(star)
                   .addArgument(star2)
                   .addArgument(() -> manhattanDistanceBetween(star, star2))
                   .log();
                sumOfDistances += manhattanDistanceBetween(star, star2);
            }
        }

        return sumOfDistances;
    }

    private static Set<Coordinate> expandStarMap(int rows, int columns, Set<Coordinate> starMap, int factor) {

        // Find empty rows and columns
        List<Integer> emptyRows = IntStream.rangeClosed(1, rows).boxed().collect(Collectors.toList());
        List<Integer> emptyColumns = IntStream.rangeClosed(1, columns).boxed().collect(Collectors.toList());
        starMap.forEach(s -> {
            emptyRows.remove(Integer.valueOf(s.getRow()));
            emptyColumns.remove(Integer.valueOf(s.getColumn()));
        });

        log.debug("Empty rows: {}", emptyRows);
        log.debug("Empty columns: {}", emptyColumns);

        // Move stars
        Set<Coordinate> expandedStarMap = starMap.stream()
                                                 .map(s -> {

                                                     int rowsToAdd = (int) IterableUtils.countMatches(emptyRows,
                                                                                                      r -> r < s.getRow()) *
                                                                     (factor - 1);
                                                     int columnsToAdd = (int) IterableUtils.countMatches(emptyColumns,
                                                                                                         c -> c < s.getColumn()) *
                                                                        (factor - 1);
                                                     return new Coordinate(s.getRow() + rowsToAdd,
                                                                           s.getColumn() + columnsToAdd);
                                                 })
                                                 .collect(Collectors.toSet());

        if (factor <= 10)
            log.atDebug().setMessage("Expanded star map:\n{}")
               .addArgument(() -> printMap(expandedStarMap, rows + emptyRows.size() * (factor - 1),
                                           columns + emptyColumns.size() * (factor - 1)))
               .log();

        return expandedStarMap;
    }

    private static Set<Coordinate> mapStars(List<String> lines) {
        AtomicInteger row = new AtomicInteger(0);
        AtomicInteger column = new AtomicInteger(0);

        Set<Coordinate> starMap = lines.stream()
                                       .peek(l -> row.incrementAndGet())
                                       .peek(l -> column.set(0))
                                       .flatMap(l -> l.chars()
                                                      .peek(c -> column.incrementAndGet())
                                                      .filter(c -> c != '.')
                                                      .mapToObj(c -> new Coordinate(row.get(), column.get())))
                                       .collect(Collectors.toSet());

        log.atDebug().setMessage("Star map:\n{}")
           .addArgument(() -> printMap(starMap, row.get(), column.get()))
           .log();

        return starMap;
    }

    /**
     * Find the Manhattan distance between two points.
     * 
     * @param point1
     *            A point to compare with the other point.
     * @param point2
     *            A point to compare with the other point.
     * @return The sum of the difference between the rows and columns of the two
     *         points.
     */
    private static long manhattanDistanceBetween(Coordinate point1, Coordinate point2) {
        return Math.abs(point1.getRow() - point2.getRow()) + Math.abs(point1.getColumn() - point2.getColumn());
    }

    /**
     * Create a printout of the map.
     * 
     * @param coordinates
     *            The set of coordinates to display.
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
     * @return A string representation of the map.
     */
    private static String printMap(Set<Coordinate> coordinates, int rows, int columns) {

        int location = columns;

        StringBuilder printout = new StringBuilder(rows * columns + rows);

        while (location < (rows + 1) * columns) {
            printout.append(coordinates.contains(new Coordinate(location / columns, location % columns + 1)) ? "*"
                                                                                                             : ".");

            if (location % columns == columns - 1)
                printout.append('\n');

            location++;
        }

        return printout.toString();
    }

}