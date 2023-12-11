package aoc2023;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.FileUtils;
import aoc2023.Day10.Pipe.PipeType;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/10
 * 
 * @author Paul Cormier
 *
 */
public class Day10 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day10.class);

    private static final String INPUT_TXT = "input/Day10.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day10.txt";
    private static final String[] TEST_INPUT_TXT_PART_2 = { "testInput/Day10.1.txt", "testInput/Day10.2.txt",
                                                            "testInput/Day10.3.txt" };
    private static final int[] TEST_SOLUTIONS_PART_2 = { 4, 8, 10 };

    static final class Pipe {
        enum PipeType {
            VERTICAL('|', new int[][] { { -1, 0 }, { 1, 0 } }),
            HORIZONTAL('-', new int[][] { { 0, -1 }, { 0, 1 } }),
            NORTH_EAST('L', new int[][] { { -1, 0 }, { 0, 1 } }),
            NORTH_WEST('J', new int[][] { { -1, 0 }, { 0, -1 } }),
            SOUTH_WEST('7', new int[][] { { 0, -1 }, { 1, 0 } }),
            SOUTH_EAST('F', new int[][] { { 0, 1 }, { 1, 0 } }),
            START('S', new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } });

            static final Map<Character, PipeType> enumMap = Arrays.asList(PipeType.values())
                                                                  .stream()
                                                                  .collect(Collectors.toMap(p -> p.symbol, p -> p));
            final char symbol;
            final Set<int[]> connectionDirections;

            private PipeType(char symbol, int[][] connections) {
                this.symbol = symbol;
                this.connectionDirections = Set.of(connections);
            }

            static PipeType fromSymbol(char symbol) {
                return enumMap.get(symbol);
            }

        }

        final Coordinate location;
        final PipeType type;
        // final Set<Pipe> connections;

        public Pipe(Coordinate location, PipeType type) {
            this.location = location;
            this.type = type;
            // this.connections = new HashSet<>(2);
        }

        public Set<Coordinate> getConnections() {
            return this.type.connectionDirections.stream()
                                                 .map(d -> new Coordinate(this.location.getRow() + d[0],
                                                                          this.location.getColumn() + d[1]))
                                                 .collect(Collectors.toSet());
        }

        public String toString() {
            return String.format("%s %s", this.type.symbol, this.location);
        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The farthest point from the start is {} steps away. (should be 8)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The farthest point from the start is {} steps away.", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);
        for (int i = 0; i < TEST_INPUT_TXT_PART_2.length; i++) {

            log.info("There are {} tiles enclosed by the loop. (should be {})",
                     part2(FileUtils.readFile(TEST_INPUT_TXT_PART_2[i])),
                     TEST_SOLUTIONS_PART_2[i]);
        }
        log.setLevel(Level.INFO);

        log.info("There are {} tiles enclosed by the loop.", part2(lines));
    }

    /**
     * How many steps along the loop does it take to get from the starting
     * position to the point farthest from the starting position?
     * 
     * @param lines
     *     The lines which describe the map of pipes.
     * @return The number of steps from the starting position to the farthest
     *     point in the loop.
     */
    private static int part1(final List<String> lines) {

        // First, parse the map of pipes
        AtomicInteger row = new AtomicInteger(0);
        AtomicInteger column = new AtomicInteger(0);

        Map<Coordinate, Pipe> pipeMap = lines.stream()
                                             .peek(l -> row.incrementAndGet())
                                             .peek(l -> column.set(0))
                                             .flatMap(l -> l.chars()
                                                            .peek(c -> column.incrementAndGet())
                                                            .filter(c -> c != '.')
                                                            .mapToObj(c -> new Pipe(new Coordinate(row.get(),
                                                                                                   column.get()),
                                                                                    PipeType.fromSymbol((char) c))))
                                             .collect(Collectors.toMap(p -> p.location, p -> p));

        log.debug("\n{}", printPipeMap(pipeMap, row.get(), column.get()));

        return findLoop(pipeMap).size() / 2;
    }

    private static Set<Pipe> findLoop(Map<Coordinate, Pipe> pipeMap) {
        // Map out connections
        Pipe startPipe = pipeMap.values().stream().filter(p -> p.type == PipeType.START).findAny().get();
        log.debug("Starting from: {}", startPipe);

        Set<Coordinate> visitedLocations = new HashSet<>();
        visitedLocations.add(startPipe.location);

        Set<Coordinate> nextLocations = startPipe.getConnections()
                                                 .stream()
                                                 .map(pipeMap::get)
                                                 .filter(Objects::nonNull)
                                                 .filter(p -> p.getConnections().contains(startPipe.location))
                                                 .map(p -> p.location)
                                                 .collect(Collectors.toSet());

        do {

            log.trace("Next: {}", nextLocations);
            visitedLocations.addAll(nextLocations);

            nextLocations = nextLocations.stream()
                                         .map(pipeMap::get)
                                         .flatMap(p -> p.getConnections()
                                                        .stream()
                                                        .filter(c -> !visitedLocations.contains(c)))
                                         .collect(Collectors.toSet());
        } while (nextLocations.size() > 1);

        visitedLocations.addAll(nextLocations);

        Set<Pipe> loopSegments = visitedLocations.stream().map(pipeMap::get).collect(Collectors.toSet());

        // Swap the starting pipe
        Coordinate startLocation = startPipe.location;
        PipeType startType = Stream.of(PipeType.values())
                                   .filter(t -> t != PipeType.START)
                                   .filter(t -> t.connectionDirections.stream()
                                                                      .allMatch(d -> Optional.ofNullable(pipeMap.get(new Coordinate(startLocation.getRow()
                                                                                                                                    + d[0],
                                                                                                                                    startLocation.getColumn()
                                                                                                                                            + d[1])))
                                                                                             .filter(p -> p.getConnections()
                                                                                                           .contains(startLocation))
                                                                                             .isPresent()))
                                   .findAny()
                                   .get();
        log.debug("Starting type: {}", startType);

        loopSegments.remove(startPipe);
        loopSegments.add(new Pipe(startLocation, startType));

        return loopSegments;
    }

    /**
     * Create a printout of the map.
     * 
     * @param pipeMap
     *     The map of coordinates to {@link Pipe} segments.
     * @param rows
     *     The number of rows in the map.
     * @param columns
     *     The number of columns in the map.
     * @return A string representation of the map.
     */
    private static String printPipeMap(Map<Coordinate, Pipe> pipeMap, int rows, int columns) {

        int location = columns;

        StringBuilder printout = new StringBuilder(rows * columns + rows);

        while (location < (rows + 1) * columns) {
            printout.append(Optional.ofNullable(pipeMap.get(new Coordinate(location / columns, location % columns + 1)))
                                    .map(p -> p.type.symbol)
                                    .orElse('.'));

            if (location % columns == columns - 1)
                printout.append('\n');

            location++;
        }

        return printout.toString();
    }

    /**
     * Create a printout of the map.
     * 
     * @param coordinates
     *     The set of coordinates to display.
     * @param rows
     *     The number of rows in the map.
     * @param columns
     *     The number of columns in the map.
     * @return A string representation of the map.
     */
    private static String printMap(Set<Coordinate> coordinates, int rows, int columns) {

        int location = columns;

        StringBuilder printout = new StringBuilder(rows * columns + rows);

        while (location < (rows + 1) * columns) {
            printout.append(coordinates.contains(new Coordinate(location / columns, location % columns + 1)) ? "O"
                    : ".");

            if (location % columns == columns - 1)
                printout.append('\n');

            location++;
        }

        return printout.toString();
    }

    /**
     * Create a printout of the map.
     * 
     * @param coordinates1
     *     The set of coordinates to display as Os.
     * @param coordinates2
     *     The set of coordinates to display as .s.
     * @param rows
     *     The number of rows in the map.
     * @param columns
     *     The number of columns in the map.
     * @return A string representation of the map.
     */
    private static String printMap(Set<Coordinate> coordinates1, Set<Coordinate> coordinates2, int rows, int columns) {

        int location = columns;

        StringBuilder printout = new StringBuilder(rows * columns + rows);

        while (location < (rows + 1) * columns) {
            if (coordinates1.contains(new Coordinate(location / columns, location % columns + 1)))
                printout.append("O");
            else if (coordinates2.contains(new Coordinate(location / columns, location % columns + 1)))
                printout.append(".");
            else
                printout.append(" ");

            if (location % columns == columns - 1)
                printout.append('\n');

            location++;
        }

        return printout.toString();
    }

    /**
     * How many tiles are enclosed by the loop?
     * 
     * @param lines
     *     The lines which describe the map of pipes.
     * @return The number of tiles enclosed by the loop.
     */
    private static int part2(final List<String> lines) {

        // First, parse the map of pipes
        AtomicInteger row = new AtomicInteger(0);
        AtomicInteger column = new AtomicInteger(0);

        Map<Coordinate, Pipe> pipeMap = lines.stream()
                                             .peek(l -> row.incrementAndGet())
                                             .peek(l -> column.set(0))
                                             .flatMap(l -> l.chars()
                                                            .peek(c -> column.incrementAndGet())
                                                            .filter(c -> c != '.')
                                                            .mapToObj(c -> new Pipe(new Coordinate(row.get(),
                                                                                                   column.get()),
                                                                                    PipeType.fromSymbol((char) c))))
                                             .collect(Collectors.toMap(p -> p.location, p -> p));

        log.debug("\n{}", printPipeMap(pipeMap, row.get(), column.get()));

        // Map out connections
        Set<Pipe> loopSegments = findLoop(pipeMap);

        // Add space between pipes
        int expandedColumns = column.get() * 3;
        int expandedRows = row.get() * 3;
        Set<Coordinate> pipeLocations = loopSegments.stream()
                                                    .flatMap(p -> expandPipeCoordinates(p))
                                                    .collect(Collectors.toSet());

        log.debug("Expanded pipe coordinates from (0,0) to ({},{}): {}", expandedRows, expandedColumns, pipeLocations);
        log.debug("Expanded map:\n{}", printMap(pipeLocations, expandedRows, expandedColumns));

        // Find all outside spaces.
        Set<Coordinate> outsideSpaces = new HashSet<>();
        Queue<Coordinate> spacesToCheck = new ArrayDeque<>();
        spacesToCheck.add(new Coordinate(0, 0));

        while (!spacesToCheck.isEmpty()) {
            Coordinate space = spacesToCheck.poll();
            if (outsideSpaces.add(space)) {
                if (space.getColumn() < expandedColumns) {
                    Coordinate right = new Coordinate(space.getRow(), space.getColumn() + 1);
                    if (!(pipeLocations.contains(right) || spacesToCheck.contains(right))) {
                        spacesToCheck.add(right);
                    }
                }
                if (space.getRow() < expandedRows) {
                    Coordinate down = new Coordinate(space.getRow() + 1, space.getColumn());
                    if (!(pipeLocations.contains(down) || spacesToCheck.contains(down))) {
                        spacesToCheck.add(down);
                    }
                }
                if (space.getColumn() > 0) {
                    Coordinate left = new Coordinate(space.getRow(), space.getColumn() - 1);
                    if (!(pipeLocations.contains(left) || spacesToCheck.contains(left))) {
                        spacesToCheck.add(left);
                    }
                }
                if (space.getRow() > 0) {
                    Coordinate up = new Coordinate(space.getRow() - 1, space.getColumn());
                    if (!(pipeLocations.contains(up) || spacesToCheck.contains(up))) {
                        spacesToCheck.add(up);
                    }
                }
            }
            log.trace("Spaces to check: {}", spacesToCheck);
        }

        log.debug("Spaces:\n{}", printMap(outsideSpaces, pipeLocations, expandedRows, expandedColumns));

        // Enclosed space = total space - pipes - outside space
        int totalSpace = row.get() * column.get();
        int pipes = loopSegments.size();
        // Now count the outside spaces which fall on a multiple of 3
        int outsideSpace = //
                (int) IntStream.rangeClosed(1, row.get())
                               .flatMap(r -> IntStream.rangeClosed(1, column.get())
                                                      .filter(c -> outsideSpaces.contains(new Coordinate(r * 3 - 1,
                                                                                                         c * 3 - 1))))
                               .count();
        int enclosedSpace = totalSpace - pipes - outsideSpace;
        log.debug("Enclosed space = total space - pipes - outside space: {} = {} - {} - {}",
                  enclosedSpace,
                  totalSpace, pipes, outsideSpace);

        return enclosedSpace;
    }

    private static Stream<Coordinate> expandPipeCoordinates(Pipe pipe) {

        log.trace("{} expands to:", pipe);

        return Stream.concat(Stream.of(new int[] { 0, 0 }), pipe.type.connectionDirections.stream())
                     .map(i -> new Coordinate(pipe.location.getRow() * 3 - 1 + i[0],
                                              pipe.location.getColumn() * 3 - 1 + i[1]))
                     .peek(c -> log.trace(c.toString()));
    }

}