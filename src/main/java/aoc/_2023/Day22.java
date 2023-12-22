package aoc._2023;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.IterableUtils;
import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.Coordinate3D;
import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/22
 * 
 * @author Paul Cormier
 *
 */
public class Day22 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day22.class);

    private static final String INPUT_TXT = "input/Day22.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day22.txt";

    private static final class Brick {
        Set<Coordinate3D> blocks = new TreeSet<>();

        Set<Brick> onTop = new HashSet<>();
        Set<Brick> underneath = new HashSet<>();

        Coordinate3D orientation = Coordinate3D.of(0, 0, 0);
        int length = 0;

        int bottomLevel = 0;

        static Brick from(String brickString) {
            Brick newBrick = new Brick();

            String[] coordinateStrings = brickString.split("~");

            String[] startString = coordinateStrings[0].split(",");
            Coordinate3D start = Coordinate3D.of(Integer.parseInt(startString[0]),
                                                 Integer.parseInt(startString[1]),
                                                 Integer.parseInt(startString[2]));
            newBrick.blocks.add(start);
            String[] endString = coordinateStrings[1].split(",");
            Coordinate3D end = Coordinate3D.of(Integer.parseInt(endString[0]),
                                               Integer.parseInt(endString[1]),
                                               Integer.parseInt(endString[2]));
            newBrick.blocks.add(end);

            // Determine orientation and length
            int length = end.getRow() - start.getRow() + end.getColumn() - start.getColumn() + end.getHeight() - start.getHeight();
            newBrick.length = length + 1;
            if (length > 1) {
                Coordinate3D orientation = Coordinate3D.of((end.getRow() - start.getRow()) / length,
                                                           (end.getColumn() - start.getColumn()) / length,
                                                           (end.getHeight() - start.getHeight()) / length);

                newBrick.blocks.addAll(IntStream.range(1, length)
                                                .mapToObj(i -> Coordinate3D.of(start.getRow() + orientation.getRow() * i,
                                                                               start.getColumn() + orientation.getColumn() * i,
                                                                               start.getHeight() + orientation.getHeight() * i))
                                                .collect(Collectors.toSet()));
                newBrick.orientation = orientation;
            }

            newBrick.bottomLevel = newBrick.blocks.stream().mapToInt(Coordinate3D::getHeight).min().getAsInt();

            return newBrick;
        }

        @Override
        public String toString() {
            return blocks.toString();
        }
    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);

        int expectedTestResult = 5;
        int part1TestResult = part1(testLines);
        log.info("The number of bricks that can be safely, individually, disintegrated is: {} (should be {})", part1TestResult,
                 expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The number of bricks that can be safely, individually, disintegrated is: {} (should be lower than 1003)", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        expectedTestResult = 1_234_567_890;
        int part2TestResult = part2(testLines);
        log.info("{} (should be {})", part2TestResult, expectedTestResult);

        if (part2TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * Figure how the blocks will settle based on the snapshot. Once they've
     * settled, consider disintegrating a single brick; how many bricks could be
     * safely chosen as the one to get disintegrated?
     * 
     * @param lines
     *            The lines giving the 3d coordinates of the ends of the bricks
     * @return The number of bricks that can be safely, individually,
     *         disintegrated.
     */
    private static int part1(final List<String> lines) {

        // Parse the bricks
        Set<Brick> bricks = lines.stream().map(Brick::from).collect(Collectors.toSet());

        log.debug("Bricks:\n{}", bricks);

        int rows = bricks.stream().flatMap(b -> b.blocks.stream()).mapToInt(Coordinate3D::getRow).max().getAsInt() + 1;
        int columns = bricks.stream().flatMap(b -> b.blocks.stream()).mapToInt(Coordinate3D::getColumn).max().getAsInt() + 1;
        int height = bricks.stream().flatMap(b -> b.blocks.stream()).mapToInt(Coordinate3D::getHeight).max().getAsInt();

        log.debug("Rows: {}  Columns: {}  Height: {}", rows, columns, height);

        // Lower the bricks...
        // Create a floor
        Brick floor = new Brick();
        floor.blocks.addAll(IntStream.range(0, rows)
                                     .mapToObj(r -> IntStream.range(0, columns)
                                                             .mapToObj(c -> Coordinate3D.of(r, c, 0)))
                                     .flatMap(s -> s)
                                     .collect(Collectors.toSet()));
        log.debug("Floor: {} ", floor);

        //        bricks.add(floor);

        Function<? super Coordinate3D, ? extends Long> //
        countMatches = c -> IterableUtils.countMatches(bricks,
                                                       brick -> brick.blocks.stream()
                                                                            .anyMatch(b -> b.getRow() == c.getRow() &&
                                                                                           b.getColumn() == c.getColumn()));
        log.atDebug()
           .setMessage("Top view:\n{}")
           .addArgument(() -> Coordinate.printMap(0, 0, rows - 1, columns - 1,
                                                  floor.blocks.stream()
                                                              .collect(Collectors.toMap(c -> Coordinate.of(c.getRow(), c.getColumn()),
                                                                                        countMatches)),
                                                  l -> l < 16 ? Character.forDigit(l.intValue(), 16) : 'X'))
           .log();

        // Start from 1, lower any brick that can be lowered?

        // Find out which bricks are resting on which other bricks.
        bricks.stream()
              .forEach(brick -> {
                  brick.blocks.stream().forEach(block -> {
                      IntStream.range(1, block.getHeight())
                               .mapToObj(d -> IterableUtils.find(bricks, otherBrick -> brick != otherBrick &&
                                                                                       otherBrick.blocks.stream()
                                                                                                        .anyMatch(b -> b.getRow() == block.getRow() &&
                                                                                                                       b.getColumn() == block.getColumn() &&
                                                                                                                       b.getHeight() == block.getHeight() -
                                                                                                                                        d)))
                               .filter(Objects::nonNull)
                               .findFirst()
                               .ifPresent(otherBrick -> {
                                   otherBrick.onTop.add(brick);
                                   brick.underneath.add(otherBrick);
                               });
                  });
              });

        // Count how many bricks are resting on more than one
        return (int) bricks.stream().filter(b -> b.onTop.stream().allMatch(ob -> ob.underneath.size() > 1)).count();

    }

    private static int countRestingPoints(Brick brick, Set<Brick> allBricks) {

        return 1;
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}