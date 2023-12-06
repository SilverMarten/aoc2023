package aoc2023;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/6
 * 
 * @author Paul Cormier
 *
 */
public class Day6 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day6.class);

    private static final String INPUT_TXT = "input/Day6.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day6.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The product of the number of ways each race can be won is {} (should be 288)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The product of the number of ways each race can be won is {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("The the number of ways the race can be won is {} (should be 71503)", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("The the number of ways the race can be won is {}", part2(lines));
    }

    /**
     * Determine the number of ways you could beat the record in each race. What
     * do you get if you multiply these numbers together?
     * 
     * @param lines
     *            The two lines showing the times and distances for the race.
     * @return The product of the number of ways each race could be won.
     */
    private static int part1(final List<String> lines) {
        int[] times = Stream.of(lines.get(0).split(" +"))
                            .filter(StringUtils::isNumeric)
                            .mapToInt(Integer::parseInt)
                            .toArray();
        int[] distances = Stream.of(lines.get(1).split(" +"))
                                .filter(StringUtils::isNumeric)
                                .mapToInt(Integer::parseInt)
                                .toArray();

        int product = 1;
        for (int race = 0; race < times.length; race++) {
            int time = times[race];
            int distance = distances[race];
            log.debug("Race {}: time = {}, distance = {}", race + 1, time, distance);

            product *= IntStream.range(1, time)
                                .filter(t -> t * (time - t) > distance)
                                .count();
        }

        return product;
    }

    /**
     * How many ways can the race be won?
     * 
     * @param lines
     *            The time and distance lines for the race. All spaces between
     *            digits are to be ignored.
     * @return The number of ways that the race can be won.
     */
    private static long part2(final List<String> lines) {

        long time = Long.parseLong(StringUtils.getDigits(lines.get(0)));
        long distance = Long.parseLong(StringUtils.getDigits(lines.get(1)));
        log.debug("Race: time = {}, distance = {}", time, distance);

        return LongStream.range(1, time)
                         .filter(t -> t * (time - t) > distance)
                         .count();

    }

}