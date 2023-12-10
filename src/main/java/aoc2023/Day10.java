package aoc2023;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            VERTICAL('|'), HORIZONTAL('-'), NORTH_EAST('L'), NORTH_WEST('J'), SOUTH_WEST('7'), SOUTH_EAST('F'), START('S');

            static final Map<Character, PipeType> enumMap = Arrays.asList(PipeType.values())
                                                                  .stream()
                                                                  .collect(Collectors.toMap(p -> p.symbol, p -> p));
            final char symbol;

            private PipeType(char symbol) {
                this.symbol = symbol;
            }

            static PipeType fromSymbol(char symbol) {
                return enumMap.get(symbol);
            }

        }

        final Coordinate location;
        final PipeType type;

        public Pipe(Coordinate location, PipeType type) {
            this.location = location;
            this.type = type;
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
     *            The lines which describe the map of pipes.
     * @return The number of steps from the starting position to the farthest
     *         point in the loop.
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
                                                            .mapToObj(c -> new Pipe(new Coordinate(row.get(), column.get()),
                                                                                    PipeType.fromSymbol((char) c))))
                                             .collect(Collectors.toMap(p -> p.location, p -> p));

        log.debug("\n{}", printMap(pipeMap, row.get(), column.get()));

        return -1;
    }

    /**
     * Create a printout of the map.
     * 
     * @param pipeMap
     *            The map of coordinates to {@link Pipe} segments.
     * @param rows
     *            The number of rows in the map.
     * @param columns
     *            The number of columns in the map.
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