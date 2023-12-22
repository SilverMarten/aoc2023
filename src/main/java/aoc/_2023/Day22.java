package aoc._2023;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
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
        String name;
        Set<Coordinate3D> blocks = new TreeSet<>();

        Set<Brick> supports = new HashSet<>();
        Set<Brick> supportedBy = new HashSet<>();

        public void moveDown() {
            this.blocks = this.blocks.stream()
                                     .map(b -> Coordinate3D.of(b.getRow(), b.getColumn(), b.getHeight() - 1))
                                     .collect(Collectors.toSet());
        }

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
            if (length > 1) {
                Coordinate3D orientation = Coordinate3D.of((end.getRow() - start.getRow()) / length,
                                                           (end.getColumn() - start.getColumn()) / length,
                                                           (end.getHeight() - start.getHeight()) / length);

                newBrick.blocks.addAll(IntStream.range(1, length)
                                                .mapToObj(i -> Coordinate3D.of(start.getRow() + orientation.getRow() * i,
                                                                               start.getColumn() + orientation.getColumn() * i,
                                                                               start.getHeight() + orientation.getHeight() * i))
                                                .collect(Collectors.toSet()));
            }

            return newBrick;
        }

        @Override
        public String toString() {
            return String.format("%s %s", name, blocks);
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
        Iterator<String> nameQueue = IterableUtils.loopingIterable(IntStream.range(0, 26)
                                                                            .mapToObj(i -> Character.toString('A' + i))
                                                                            .collect(Collectors.toList()))
                                                  .iterator();
        Set<Brick> bricks = lines.stream()
                                 .map(Brick::from)
                                 .peek(b -> b.name = nameQueue.next())
                                 .collect(Collectors.toSet());

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

        AtomicBoolean bricksMoved = new AtomicBoolean();
        do {
            bricksMoved.set(false);
            // Start from 2, lower any brick that can be lowered?
            IntStream.rangeClosed(2, height).forEach(level -> {
                IterableUtils.filteredIterable(bricks, brick -> brick.blocks.stream().anyMatch(b -> b.getHeight() == level))
                             .forEach(brick -> {
                                 // Can it be lowered?
                                 int bottomLevel = brick.blocks.stream().mapToInt(Coordinate3D::getHeight).min().getAsInt();
                                 if (bottomLevel > 1) {
                                     // Is every bottom level block free to move down?
                                     Set<Brick> beneath = brick.blocks.stream()
                                                                      .filter(b -> b.getHeight() == bottomLevel)
                                                                      .flatMap(b -> bricks.stream()
                                                                                          .filter(otherBrick//
                                     -> otherBrick.blocks.contains(Coordinate3D.of(b.getRow(),
                                                                                   b.getColumn(),
                                                                                   bottomLevel - 1))))
                                                                      .collect(Collectors.toSet());
                                     if (beneath.isEmpty()) {
                                         bricksMoved.set(true);
                                         brick.moveDown();
                                     } else {
                                         // If not, add it the the brick it's resting on
                                         beneath.forEach(b -> {
                                             b.supports.add(brick);
                                             brick.supportedBy.add(b);
                                         });
                                     }
                                 }

                             });
            });

        } while (bricksMoved.get());

        log.atDebug()
           .setMessage("{}")
           .addArgument(() -> bricks.stream().sorted(Comparator.comparing(b -> b.name))
                                    .map(b -> String.format("Brick %s is supporting: %s", b.name,
                                                            b.supports.stream().map(s -> s.name).collect(Collectors.joining(", "))))
                                    .collect(Collectors.joining("\n")))
           .log();

        // Count how many bricks are underneath bricks which are resting on more than one brick
        return (int) bricks.stream().filter(b -> b.supports.stream().allMatch(ob -> ob.supportedBy.size() > 1)).count();

    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}