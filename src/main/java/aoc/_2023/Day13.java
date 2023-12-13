package aoc._2023;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/13
 * 
 * @author Paul Cormier
 *
 */
public class Day13 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day13.class);

    private static final String INPUT_TXT = "input/Day13.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day13.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The sum of the note surraies is: {} (should be 405)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the note surraies is: {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * Find the line of reflection in each of the patterns in your notes. What
     * number do you get after summarizing all of your notes?
     * 
     * @param lines
     *            The lines containing blocks of notes to summarize.
     * @return The sum of the columns and 100 * the rows before the axis of
     *         reflection.
     */
    private static int part1(final List<String> lines) {

        // Parse out each block
        List<List<String>> blocks = new ArrayList<>();
        List<String> block = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank()) {
                blocks.add(block);
                block = new ArrayList<>();
            } else {
                block.add(line);
            }
        }
        blocks.add(block);

        // Summarize each block
        return blocks.stream().mapToInt(Day13::summarizeBlock).sum();

    }

    private static int part2(final List<String> lines) {

        return -1;
    }

    /**
     * Within a single block, find the axis of symmetry. Return either the
     * number of columns to its left, or 100 * the number of rows above it.
     * 
     * @param lines
     *            The lines representing a single block.
     * @return The summary of the columns and 100 * the rows before the axis of
     *         reflection.
     */
    private static int summarizeBlock(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        Set<Coordinate> coordinates = mapCoordinates(lines);

        log.atDebug().setMessage("Block:\n{}").addArgument(() -> Coordinate.printMap(coordinates, rows, columns)).log();

        return 0;
    }

    /**
     * Map a list of strings into a set of coordinates of the locations of # in
     * the strings.
     * 
     * @param lines
     *            The lines to find and map the locations of #s.
     * @return The set of coordinates of the locations of #s.
     */
    private static Set<Coordinate> mapCoordinates(List<String> lines) {
        AtomicInteger row = new AtomicInteger(1);

        Set<Coordinate> coordinates = new HashSet<>();
        for (String line : lines) {
            coordinates.addAll(ArrayUtils.indexesOf(line.toCharArray(), '#')
                                         .stream()
                                         .mapToObj(c -> new Coordinate(row.get(), c + 1))
                                         .collect(Collectors.toSet()));
            row.getAndIncrement();
        }

        return coordinates;
    }

}