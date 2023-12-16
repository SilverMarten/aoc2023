package aoc._2023;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
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
        // log.setLevel(Level.TRACE);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The sum of the note summaries is: {} (should be 405)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the note summaries is: {} (should be less than 35668)", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("The sum of the note summaries is: {} (should be 400)", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("The sum of the note summaries is: {}", part2(lines));
    }

    /**
     * Find the line of reflection in each of the patterns in your notes. What
     * number do you get after summarizing all of your notes?
     * 
     * @param lines
     *     The lines containing blocks of notes to summarize.
     * @return The sum of the columns and 100 * the rows before the axis of
     *     reflection.
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

    /**
     * Find the line of reflection in each of the patterns in your notes, given that
     * there is one error in each group. What
     * number do you get after summarizing all of your notes?
     * 
     * @param lines
     *     The lines containing blocks of notes to summarize.
     * @return The sum of the columns and 100 * the rows before the axis of
     *     reflection.
     */
    private static int part2(final List<String> lines) {

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
        return blocks.stream().mapToInt(Day13::summarizeBlock2).sum();
    }

    /**
     * Within a single block, find the axis of symmetry. Return either the
     * number of columns to its left, or 100 * the number of rows above it.
     * 
     * @param lines
     *     The lines representing a single block.
     * @return The summary of the columns or 100 * the rows before the axis of
     *     reflection.
     */
    private static int summarizeBlock(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        Set<Coordinate> coordinates = Coordinate.findCoordinates(lines);

        log.atDebug().setMessage("Block:\n{}\n{} rows, {} columns")
           .addArgument(() -> Coordinate.printMap(rows, columns, coordinates))
           .addArgument(rows)
           .addArgument(columns)
           .log();

        // Check the rows
        for (int row = 2; row <= rows; row++) {
            // It is symmetrical if the translated set of points on the shorter side of the
            // line have a corresponding point on the larger side of the line.
            int i = row;

            Range<Integer> shortSideRange = i <= rows / 2 + 1 ? Range.of(1, i - 1) : Range.of(i, rows);
            Range<Integer> correspondingRange = i <= rows / 2 + 1 ? Range.of(i, (i - 1) * 2)
                    : Range.of(i - (rows - i) - 1, i - 1);

            log.trace("Comparing {} to {}", shortSideRange, correspondingRange);

            Set<Coordinate> shortSide = coordinates.stream()
                                                   .filter(c -> shortSideRange.contains(c.getRow()))
                                                   // Translate the coordinates and check if there is a match
                                                   .map(c -> new Coordinate(c.getRow() + 2 * (i - c.getRow()) - 1,
                                                                            c.getColumn()))
                                                   .collect(Collectors.toSet());

            log.atTrace().setMessage("Translated short side:\n{}\n{}")
               .addArgument(shortSide)
               .addArgument(() -> Coordinate.printMap(rows, columns, shortSide))
               .log();

            // Find the corresponding rows in the rest of the map
            Set<Coordinate> correspondingPoints = coordinates.stream()
                                                             .filter(c -> correspondingRange.contains(c.getRow()))
                                                             .collect(Collectors.toSet());

            if (correspondingPoints.equals(shortSide)) {
                log.debug("It's a match at row {}!", row);
                return (row - 1) * 100;
            }
        }

        // Check the columns
        for (int column = 2; column <= columns; column++) {
            // It is symmetrical if the translated set of points on the shorter side of the
            // line have a corresponding point on the larger side of the line.
            int i = column;

            Range<Integer> shortSideRange = i <= columns / 2 + 1 ? Range.of(1, i - 1) : Range.of(i, columns);
            Range<Integer> correspondingRange = i <= columns / 2 + 1 ? Range.of(i, (i - 1) * 2)
                    : Range.of(i - (columns - i) - 1, i - 1);

            log.trace("Comparing {} to {}", shortSideRange, correspondingRange);

            Set<Coordinate> shortSide = coordinates.stream()
                                                   .filter(c -> shortSideRange.contains(c.getColumn()))
                                                   // Translate the coordinates and check if there is a match
                                                   .map(c -> new Coordinate(c.getRow(),
                                                                            c.getColumn() + 2 * (i - c.getColumn())
                                                                                        - 1))
                                                   .collect(Collectors.toSet());

            log.atTrace().setMessage("Translated short side:\n{}\n{}")
               .addArgument(shortSide)
               .addArgument(() -> Coordinate.printMap(rows, columns, shortSide))
               .log();

            // Find the corresponding rows in the rest of the map
            Set<Coordinate> correspondingPoints = coordinates.stream()
                                                             .filter(c -> correspondingRange.contains(c.getColumn()))
                                                             .collect(Collectors.toSet());

            if (correspondingPoints.equals(shortSide)) {
                log.debug("It's a match at column {}!", column);
                return column - 1;
            }
        }

        return 0;
    }

    /**
     * Within a single block, find the axis of symmetry, given that one spot is
     * incorrect. Return either the
     * number of columns to its left, or 100 * the number of rows above it.
     * 
     * @param lines
     *     The lines representing a single block.
     * @return The summary of the columns or 100 * the rows before the axis of
     *     reflection.
     */
    private static int summarizeBlock2(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        Set<Coordinate> coordinates = Coordinate.findCoordinates(lines);

        log.atDebug().setMessage("Block:\n{}\n{} rows, {} columns")
           .addArgument(() -> Coordinate.printMap(rows, columns, coordinates))
           .addArgument(rows)
           .addArgument(columns)
           .log();

        // Check the rows
        for (int row = 2; row <= rows; row++) {
            // It is symmetrical if the translated set of points on the shorter side of the
            // line have a corresponding point on the larger side of the line.
            // Except for one?
            int i = row;

            Range<Integer> shortSideRange = i <= rows / 2 + 1 ? Range.of(1, i - 1) : Range.of(i, rows);
            Range<Integer> correspondingRange = i <= rows / 2 + 1 ? Range.of(i, (i - 1) * 2)
                    : Range.of(i - (rows - i) - 1, i - 1);

            log.trace("Comparing {} to {}", shortSideRange, correspondingRange);

            Set<Coordinate> shortSide = coordinates.stream()
                                                   .filter(c -> shortSideRange.contains(c.getRow()))
                                                   // Translate the coordinates and check if there is a match
                                                   .map(c -> new Coordinate(c.getRow() + 2 * (i - c.getRow()) - 1,
                                                                            c.getColumn()))
                                                   .collect(Collectors.toSet());

            log.atTrace().setMessage("Translated short side:\n{}\n{}")
               .addArgument(shortSide)
               .addArgument(() -> Coordinate.printMap(rows, columns, shortSide))
               .log();

            // Find the corresponding rows in the rest of the map
            Set<Coordinate> correspondingPoints = coordinates.stream()
                                                             .filter(c -> correspondingRange.contains(c.getRow()))
                                                             .collect(Collectors.toSet());

            if (CollectionUtils.disjunction(correspondingPoints, shortSide).size() == 1) {
                log.debug("It's a match at row {}!", row);
                return (row - 1) * 100;
            }
        }

        // Check the columns
        for (int column = 2; column <= columns; column++) {
            // It is symmetrical if the translated set of points on the shorter side of the
            // line have a corresponding point on the larger side of the line.
            int i = column;

            Range<Integer> shortSideRange = i <= columns / 2 + 1 ? Range.of(1, i - 1) : Range.of(i, columns);
            Range<Integer> correspondingRange = i <= columns / 2 + 1 ? Range.of(i, (i - 1) * 2)
                    : Range.of(i - (columns - i) - 1, i - 1);

            log.trace("Comparing {} to {}", shortSideRange, correspondingRange);

            Set<Coordinate> shortSide = coordinates.stream()
                                                   .filter(c -> shortSideRange.contains(c.getColumn()))
                                                   // Translate the coordinates and check if there is a match
                                                   .map(c -> new Coordinate(c.getRow(),
                                                                            c.getColumn() + 2 * (i - c.getColumn())
                                                                                        - 1))
                                                   .collect(Collectors.toSet());

            log.atTrace().setMessage("Translated short side:\n{}\n{}")
               .addArgument(shortSide)
               .addArgument(() -> Coordinate.printMap(rows, columns, shortSide))
               .log();

            // Find the corresponding rows in the rest of the map
            Set<Coordinate> correspondingPoints = coordinates.stream()
                                                             .filter(c -> correspondingRange.contains(c.getColumn()))
                                                             .collect(Collectors.toSet());

            if (CollectionUtils.disjunction(correspondingPoints, shortSide).size() == 1) {
                log.debug("It's a match at column {}!", column);
                return column - 1;
            }
        }

        return 0;
    }

}