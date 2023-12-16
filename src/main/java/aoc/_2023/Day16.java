package aoc._2023;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.SetUtils;
import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/16
 * 
 * @author Paul Cormier
 *
 */
public class Day16 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day16.class);

    private static final String INPUT_TXT = "input/Day16.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day16.txt";

    private static enum Direction {
        RIGHT('>'), LEFT('<'), UP('^'), DOWN('v');

        char symbol;

        Direction(char symbol) {
            this.symbol = symbol;
        }
    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("There are {} energized tiles. (should be 46)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("There are {} energized tiles. (should be higher than 7447)", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("There are {} energized tiles. (should be 51)", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("There are {} energized tiles. (should be higher than 7496)", part2(lines));
    }

    /**
     * With the beam starting in the top-left heading right, how many tiles end up
     * being energized?
     * 
     * @param lines The lines of the grid containing mirrors and splitters.
     * @return The number of energized tiles.
     */
    private static int part1(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        // Map out the mirrors
        Map<Coordinate, Character> mirrorMap = Coordinate.mapCoordinates(lines);

        log.atDebug()
           .setMessage("Mirrors:\n{}")
           .addArgument(() -> Coordinate.printMap(rows, columns, mirrorMap))
           .log();

        // Add a frame to stop the light
        IntStream.rangeClosed(1, columns).forEach(c -> {
            mirrorMap.put(new Coordinate(0, c), '#');
            mirrorMap.put(new Coordinate(rows + 1, c), '#');
        });
        IntStream.rangeClosed(1, rows).forEach(r -> {
            mirrorMap.put(new Coordinate(r, 0), '#');
            mirrorMap.put(new Coordinate(r, columns + 1), '#');
        });

        Direction currentDirection = Direction.RIGHT;
        Coordinate currentPosition = new Coordinate(1, 1);

        Set<Entry<Coordinate, Direction>> energizedTiles = new HashSet<>();

        // Start following the path of the light
        followTheLight(currentPosition, currentDirection, mirrorMap, energizedTiles);

        energizedTiles.stream().forEach(e -> {
            char mappedChar = mirrorMap.getOrDefault(e.getKey(), '.');
            if (mappedChar == '.')
                mirrorMap.put(e.getKey(), e.getValue().symbol);
            else if ("<>^v".indexOf(mappedChar) >= 0)
                mirrorMap.put(e.getKey(), '2');
            else if ("   234".indexOf(mappedChar) >= 0)
                mirrorMap.put(e.getKey(), (char) ("   234".indexOf(mappedChar) + '0'));

        });

        log.atDebug()
           .setMessage("Path:\n{}")
           .addArgument(() -> Coordinate.printMap(rows, columns, mirrorMap))
           .log();

        // Count the energized tiles
        Set<Coordinate> energizedTileSet = energizedTiles.stream().map(Entry::getKey).collect(Collectors.toSet());

        log.atDebug()
           .setMessage("Energized tiles:\n{}")
           .addArgument(() -> Coordinate.printMap(rows, columns, energizedTileSet))
           .log();

        return energizedTileSet.size();
    }

    /**
     * Find the initial beam configuration that energizes the largest number of
     * tiles; how many tiles are energized in that configuration?
     * 
     * @param lines The lines of the grid containing mirrors and splitters.
     * @return The maximum number of energized tiles.
     */
    private static int part2(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        // Map out the mirrors
        Map<Coordinate, Character> mirrorMap = Coordinate.mapCoordinates(lines);

        log.atDebug()
           .setMessage("Mirrors:\n{}")
           .addArgument(() -> Coordinate.printMap(rows, columns, mirrorMap))
           .log();

        // Add a frame to stop the light
        IntStream.rangeClosed(1, columns).forEach(c -> {
            mirrorMap.put(new Coordinate(0, c), '#');
            mirrorMap.put(new Coordinate(rows + 1, c), '#');
        });
        IntStream.rangeClosed(1, rows).forEach(r -> {
            mirrorMap.put(new Coordinate(r, 0), '#');
            mirrorMap.put(new Coordinate(r, columns + 1), '#');
        });

        // Try all the start positions and directions

        Set<Entry<Coordinate, Direction>> energizedTiles = SetUtils.emptySet();
        Set<Coordinate> energizedTileSet = SetUtils.emptySet();
        int energizedTileCount = 0;

        Set<Entry<Coordinate, Direction>> maxEnergizedTiles = new HashSet<>();

        // Left side, going right
        for (int row = 1; row <= rows; row++) {
            Direction currentDirection = Direction.RIGHT;
            Coordinate startPosition = new Coordinate(row, 1);
            energizedTiles = new HashSet<>();

            // Start following the path of the light
            followTheLight(startPosition, currentDirection, mirrorMap, energizedTiles);

            // Count the energized tiles
            energizedTileSet = energizedTiles.stream().map(Entry::getKey).collect(Collectors.toSet());

            if (energizedTileCount < energizedTileSet.size()) {
                energizedTileCount = energizedTileSet.size();
                maxEnergizedTiles = energizedTiles;
            }
        }
        // Right side, going left
        for (int row = 1; row <= rows; row++) {
            Direction currentDirection = Direction.LEFT;
            Coordinate startPosition = new Coordinate(row, columns);
            energizedTiles = new HashSet<>();

            // Start following the path of the light
            followTheLight(startPosition, currentDirection, mirrorMap, energizedTiles);

            // Count the energized tiles
            energizedTileSet = energizedTiles.stream().map(Entry::getKey).collect(Collectors.toSet());

            if (energizedTileCount < energizedTileSet.size()) {
                energizedTileCount = energizedTileSet.size();
                maxEnergizedTiles = energizedTiles;
            }
        }
        // Top side, going down
        for (int column = 1; column <= columns; column++) {
            Direction currentDirection = Direction.DOWN;
            Coordinate startPosition = new Coordinate(1, column);
            energizedTiles = new HashSet<>();

            // Start following the path of the light
            followTheLight(startPosition, currentDirection, mirrorMap, energizedTiles);

            // Count the energized tiles
            energizedTileSet = energizedTiles.stream().map(Entry::getKey).collect(Collectors.toSet());

            if (energizedTileCount < energizedTileSet.size()) {
                energizedTileCount = energizedTileSet.size();
                maxEnergizedTiles = energizedTiles;
            }
        }
        // Bottom side, going up
        for (int column = 1; column <= columns; column++) {
            Direction currentDirection = Direction.UP;
            Coordinate startPosition = new Coordinate(rows, column);
            energizedTiles = new HashSet<>();

            // Start following the path of the light
            followTheLight(startPosition, currentDirection, mirrorMap, energizedTiles);

            // Count the energized tiles
            energizedTileSet = energizedTiles.stream().map(Entry::getKey).collect(Collectors.toSet());

            if (energizedTileCount < energizedTileSet.size()) {
                energizedTileCount = energizedTileSet.size();
                maxEnergizedTiles = energizedTiles;
            }
        }

        // Display the path
        maxEnergizedTiles.stream().forEach(e -> {
            char mappedChar = mirrorMap.getOrDefault(e.getKey(), '.');
            if (mappedChar == '.')
                mirrorMap.put(e.getKey(), e.getValue().symbol);
            else if ("<>^v".indexOf(mappedChar) >= 0)
                mirrorMap.put(e.getKey(), '2');
            else if ("   234".indexOf(mappedChar) >= 0)
                mirrorMap.put(e.getKey(), (char) ("   234".indexOf(mappedChar) + '0'));

        });

        log.atDebug()
           .setMessage("Path:\n{}")
           .addArgument(() -> Coordinate.printMap(rows, columns, mirrorMap))
           .log();

        Set<Coordinate> maxEnergizedTileSet = maxEnergizedTiles.stream().map(Entry::getKey).collect(Collectors.toSet());
        log.atDebug()
           .setMessage("Energized tiles:\n{}")
           .addArgument(() -> Coordinate.printMap(rows, columns, maxEnergizedTileSet))
           .log();

        return energizedTileCount;
    }

    /**
     * Starting from the given position, follow the path of the light, energizing
     * tiles along the way.
     * 
     * @param currentPosition
     * @param currentDirection
     * @param mirrorMap
     * @param energizedTiles
     */
    private static void followTheLight(Coordinate currentPosition, Direction currentDirection,
                                       Map<Coordinate, Character> mirrorMap,
                                       Set<Entry<Coordinate, Direction>> energizedTiles) {

        boolean done = false;
        char chatAtPosition;
        // Energize the tile if we're not done, on the frame, or have been here already
        // from this direction
        while (!done) {

            chatAtPosition = mirrorMap.getOrDefault(currentPosition, '.');
            if (chatAtPosition == '#') {
                done = true;
                continue;
            }

            if (!energizedTiles.add(new SimpleEntry<Coordinate, Direction>(currentPosition, currentDirection))) {
                done = true;
                continue;
            }

            // Encounter?
            switch (chatAtPosition) {
                case '\\':
                    switch (currentDirection) {
                        case RIGHT:
                            currentDirection = Direction.DOWN;
                            break;
                        case LEFT:
                            currentDirection = Direction.UP;
                            break;
                        case UP:
                            currentDirection = Direction.LEFT;
                            break;
                        case DOWN:
                            currentDirection = Direction.RIGHT;
                            break;
                    }
                    break;
                case '/':
                    switch (currentDirection) {
                        case RIGHT:
                            currentDirection = Direction.UP;
                            break;
                        case LEFT:
                            currentDirection = Direction.DOWN;
                            break;
                        case UP:
                            currentDirection = Direction.RIGHT;
                            break;
                        case DOWN:
                            currentDirection = Direction.LEFT;
                            break;
                    }
                    break;
                case '|':
                    switch (currentDirection) {
                        case RIGHT:
                        case LEFT:
                            followTheLight(new Coordinate(currentPosition.getRow() - 1, currentPosition.getColumn()),
                                           Direction.UP, mirrorMap, energizedTiles);
                            followTheLight(new Coordinate(currentPosition.getRow() + 1, currentPosition.getColumn()),
                                           Direction.DOWN, mirrorMap, energizedTiles);
                            done = true;
                            continue;
                        case UP:
                        case DOWN:
                            // No change
                            break;
                    }
                    break;
                case '-':
                    switch (currentDirection) {
                        case UP:
                        case DOWN:
                            followTheLight(new Coordinate(currentPosition.getRow(), currentPosition.getColumn() - 1),
                                           Direction.LEFT, mirrorMap, energizedTiles);
                            followTheLight(new Coordinate(currentPosition.getRow(), currentPosition.getColumn() + 1),
                                           Direction.RIGHT, mirrorMap, energizedTiles);
                            done = true;
                            continue;
                        case RIGHT:
                        case LEFT:
                            // No change
                            break;
                    }
                    break;
                case '.':
                    // No change
                    break;
            }

            // Determine the next position
            switch (currentDirection) {
                case RIGHT:
                    currentPosition = new Coordinate(currentPosition.getRow(), currentPosition.getColumn() + 1);
                    break;
                case LEFT:
                    currentPosition = new Coordinate(currentPosition.getRow(), currentPosition.getColumn() - 1);
                    break;
                case UP:
                    currentPosition = new Coordinate(currentPosition.getRow() - 1, currentPosition.getColumn());
                    break;
                case DOWN:
                    currentPosition = new Coordinate(currentPosition.getRow() + 1, currentPosition.getColumn());
                    break;
            }
        }
    }

}