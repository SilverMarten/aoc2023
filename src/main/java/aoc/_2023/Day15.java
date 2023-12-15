package aoc._2023;

import java.util.List;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/15
 * 
 * @author Paul Cormier
 *
 */
public class Day15 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day15.class);

    private static final String INPUT_TXT = "input/Day15.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day15.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The sum of the results of the HASH algorithm is: {} (should be 1320)", part1(testLines.get(0)));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the results of the HASH algorithm is: {}", part1(lines.get(0)));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * Run the HASH algorithm on each step in the initialization sequence. What
     * is the sum of the results?
     * 
     * @param line
     *            The line to run the HASH algorithm on
     * @return The sum of the results of running the HASH algorithm on each step
     *         in the line.
     */
    private static int part1(final String line) {

        return Stream.of(line.split(","))
                     .mapToInt(step -> step.chars().reduce(0, (sum, character) -> (sum + character) * 17 % 256))
                     .sum();
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}