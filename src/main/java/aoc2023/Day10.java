package aoc2023;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
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

        log.debug("\n{}", printMap(pipeMap, row.get(), column.get()));

        // Map out connections
        Pipe startPipe = pipeMap.values().stream().filter(p -> p.type == PipeType.START).findAny().get();
        log.debug("Starting from: {}", startPipe);

        int distance = 1;
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

            log.debug("Next: {}", nextLocations);
            distance++;
            visitedLocations.addAll(nextLocations);

            nextLocations = nextLocations.stream()
                                         .map(pipeMap::get)
                                         .flatMap(p -> p.getConnections()
                                                        .stream()
                                                        .filter(c -> !visitedLocations.contains(c)))
                                         .collect(Collectors.toSet());
        } while (nextLocations.size() > 1);

        return distance;
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
    private static String printMap(Map<Coordinate, Pipe> pipeMap, int rows, int columns) {

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

    private static int part2(final List<String> lines) {

        return -1;
    }

}