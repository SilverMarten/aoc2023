package aoc2023;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
public class Day1 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day1.class);

    private static final String INPUT_TXT = "input/Day1.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day1.txt";

    public static void main(String[] args) {

        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace(testLines.toString());

        log.info("The sum of the values in the test input is: {}. It should be 142.", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the values in the real input is: {}", part1(lines));

        // PART 2

        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * Given a list of lines of alphanumeric characters, find the first and last
     * digits (could be the same), treat those as a two digit number, and sum
     * the value for all lines.
     * 
     * @param lines
     *            The lines of alphanumeric characters.
     * @return The sum of interpreting the first and last digits of each line as
     *         a number.
     */
    private static int part1(final List<String> lines) {

        return lines.stream().mapToInt(Day1::findValue).sum();
    }

    private static int findValue(String line) {
        String digits = StringUtils.getDigits(line);
        char firstDigit = digits.charAt(0);
        char secondDigit = digits.charAt(digits.length()-1);

        return (firstDigit - '0') * 10 + (secondDigit - '0');
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}