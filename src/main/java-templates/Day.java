package aoc._2023;

import java.util.List;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/${day}
 * 
 * @author Paul Cormier
 *
 */
public class Day${day} {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day${day}.class);

    private static final String INPUT_TXT = "input/Day${day}.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day${day}.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);

        int expectedTestResult = 1_234_567_890;
        int part1TestResult = part1(testLines);
        log.info("{} (should be {})", part1TestResult, expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("{}", part1(lines));

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

    private static int part1(final List<String> lines) {

        return -1;
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}