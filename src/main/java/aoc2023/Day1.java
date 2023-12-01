package aoc2023;

import java.util.List;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/1
 * 
 * @author Paul Cormier
 *
 */
public class Day1{

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day1.class);

    private static final String INPUT_TXT = "input/Day1.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day1.txt";

    public static void main(String[] args) {

        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace(testLines.toString());

        log.info("{}", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("{}", part1(lines));

        // PART 2

        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

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