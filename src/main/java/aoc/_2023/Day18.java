package aoc._2023;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/18
 * 
 * @author Paul Cormier
 *
 */
public class Day18 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day18.class);

    private static final String INPUT_TXT = "input/Day18.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day18.txt";

    private enum Direction {
        UP('U', Coordinate.of(-1, 0)),
        RIGHT('R', Coordinate.of(0, 1)),
        DOWN('D', Coordinate.of(1, 0)),
        LEFT('L', Coordinate.of(0, -1));

        char symbol;
        Coordinate translation;

        private static Map<Character, Direction> directionMap = Collections.unmodifiableMap(Stream.of(Direction.values())
                                                                                                  .collect(Collectors.toMap(d -> d.symbol,
                                                                                                                            d -> d)));

        Direction(char symbol, Coordinate translation) {
            this.symbol = symbol;
            this.translation = translation;
        }

        static Direction of(char symbol) {
            return directionMap.get(symbol);
        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);

        log.info("The volume of the excavated lagoon is: {} (should be 62)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The volume of the excavated lagoon is: {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * The Elves are concerned the lagoon won't be large enough; if they follow
     * their dig plan, how many cubic meters of lava could it hold?
     * 
     * @param lines
     *            The lines describing the dig plan
     * @return The volume of the excavated lagoon.
     */
    private static int part1(final List<String> lines) {

        // Follow the dig plan
        Map<Coordinate, Integer> excavation = excavate(lines);

        // Determine boundaries (with padding)
        int maxRow = excavation.keySet().stream().mapToInt(Coordinate::getRow).max().getAsInt() + 1;
        int maxColumn = excavation.keySet().stream().mapToInt(Coordinate::getColumn).max().getAsInt() + 1;
        int minRow = excavation.keySet().stream().mapToInt(Coordinate::getRow).min().getAsInt() - 1;
        int minColumn = excavation.keySet().stream().mapToInt(Coordinate::getColumn).min().getAsInt() - 1;

        log.atDebug()
           .setMessage("Trench:\n{}")
           .addArgument(() -> Coordinate.printMap(minRow, minColumn, maxRow, maxColumn, excavation.keySet()))
           .log();

        // Hollow out the middle
        // Find all the "outside" spaces
        Set<Coordinate> outsideSpaces = new HashSet<>();
        Queue<Coordinate> spacesToCheck = new ArrayDeque<>();
        Range<Integer> rowRange = Range.of(minRow, maxRow);
        Range<Integer> columnRange = Range.of(minColumn, maxColumn);
        spacesToCheck.add(Coordinate.of(minRow, minColumn));
        while (!spacesToCheck.isEmpty()) {
            Coordinate spaceToCheck = spacesToCheck.poll();
            if (!(excavation.containsKey(spaceToCheck) || outsideSpaces.contains(spaceToCheck))) {
                outsideSpaces.add(spaceToCheck);
                spacesToCheck.addAll(spaceToCheck.findAdjacent()
                                                 .stream()
                                                 .filter(c -> !(excavation.containsKey(c) || outsideSpaces.contains(c)) &&
                                                              rowRange.contains(c.getRow()) && columnRange.contains(c.getColumn()))
                                                 .collect(Collectors.toSet()));
            }
        }

        // Well... All I need is the size of the lagoon right?
        return (maxRow - minRow + 1) * (maxColumn - minColumn + 1) - outsideSpaces.size();

        /*log.atDebug()
           .setMessage("Lagoon:\n{}")
           .addArgument(() -> Coordinate.printMap(minRow, minColumn, maxRow, maxColumn, excavation.keySet()))
           .log();
        
        return excavation.size();*/
    }

    private static Map<Coordinate, Integer> excavate(final List<String> lines) {
        Map<Coordinate, Integer> excavation = new HashMap<>();

        // Yes, "The digger starts in a 1 meter cube hole in the ground.",
        // but the colour isn't given until the last step.
        Coordinate currentPosition = Coordinate.of(1, 1);

        for (String line : lines) {
            String[] arguments = line.split(" ");
            Direction dir = Direction.of(arguments[0].charAt(0));
            int distance = Integer.parseInt(arguments[1]);
            int colour = Integer.parseInt(arguments[2].substring(2, 8), 16);

            // Excavate that many cubes in that direction
            int currentRow = currentPosition.getRow();
            int currentColumn = currentPosition.getColumn();
            IntStream.rangeClosed(1, distance)
                     .forEach(i -> excavation.put(Coordinate.of(currentRow + dir.translation.getRow() * i,
                                                                currentColumn + dir.translation.getColumn() * i),
                                                  colour));
            currentPosition = Coordinate.of(currentRow + dir.translation.getRow() * distance,
                                            currentColumn + dir.translation.getColumn() * distance);
        }

        return excavation;
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}