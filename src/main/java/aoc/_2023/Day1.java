package aoc._2023;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

    private static final Map<String, Integer> NUMBERS = Map.of("one", 1,
                                                               "two", 2,
                                                               "three", 3,
                                                               "four", 4,
                                                               "five", 5,
                                                               "six", 6,
                                                               "seven", 7,
                                                               "eight", 8,
                                                               "nine", 9);

    private static final String NUMBER_REGEX = NUMBERS.keySet().stream().collect(Collectors.joining("|", "(", ")"));
    private static final String NUMBER_REVERSE_REGEX = StringUtils.reverse(NUMBERS.keySet().stream()
                                                                                  .collect(Collectors.joining("|", ")", "(")));

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = Arrays.asList("1abc2",
                                               "pqr3stu8vwx",
                                               "a1b2c3d4e5f",
                                               "treb7uchet");
        log.trace(testLines.toString());

        log.info("The sum of the values in the test input is: {}. It should be 142.", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the values in the real input is: {}", part1(lines));

        // PART 2
        log.info("Part 2:");

        testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.setLevel(Level.DEBUG);

        log.info("The sum of the values in the test input is: {}. It should be 281.", part2(testLines));

        log.info("The value of the line {} is: {}", "mbkfgktwolbvsptgsixseven1oneightzvm",
                 findNumberValue("mbkfgktwolbvsptgsixseven1oneightzvm"));

        log.setLevel(Level.INFO);

        log.info("The sum of the values in the real input is: {}. It should be higher than 53293.", part2(lines));
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

        return lines.stream().mapToInt(Day1::findDigitValue).sum();
    }

    /**
     * Given a line of alphanumeric characters, find the first and last digits
     * (could be the same), treat those as a two digit number.
     * 
     * @param line
     *            The line of alphanumeric characters.
     * @return The value of the first and last digits as a two digit number.
     */
    private static int findDigitValue(String line) {
        String digits = StringUtils.getDigits(line);
        if (digits.length() == 0)
            return 0;

        char firstDigit = digits.charAt(0);
        char secondDigit = digits.charAt(digits.length() - 1);

        return (firstDigit - '0') * 10 + (secondDigit - '0');
    }

    /**
     * Given a list of lines of alphanumeric characters, find the first and last
     * number (either the digit or the word, which could be the same), treat
     * those as a two digit number, and sum the value for all lines.
     * 
     * @param lines
     *            The lines of alphanumeric characters.
     * @return The sum of interpreting the first and last numbers of each line
     *         as a number.
     */
    private static int part2(final List<String> lines) {

        return lines.stream().mapToInt(Day1::findNumberValue).sum();
    }

    /**
     * Given a line of alphanumeric characters, find the first and last numbers
     * (either the digit or the word, which could be the same), treat those as a
     * two digit number.
     * 
     * @param line
     *            The line of alphanumeric characters.
     * @return The value of the first and last numbers as a two digit number.
     */
    private static int findNumberValue(String line) {
        log.debug("Received: {}", line);
        // First, substitute the words for digits

        String line1 = line.replaceAll(NUMBER_REGEX, "{$1}");
        // This seemed the easiest way to find the instances of number words from the right
        String line2 = StringUtils.reverse(line).replaceAll(NUMBER_REVERSE_REGEX, "}$1{");
        line2 = StringUtils.reverse(line2);

        for (Entry<String, Integer> e : NUMBERS.entrySet()) {
            String word = e.getKey();
            String digit = e.getValue().toString();
            line1 = line1.replace(word, digit);
            line2 = line2.replace(word, digit);
        }

        log.debug("Translated to: {} and {}", line1, line2);

        String digits1 = StringUtils.getDigits(line1);

        // Just in case there are no digits (there wouldn't be any from the other direction either)
        if (digits1.length() == 0)
            return 0;

        char firstDigit = digits1.charAt(0);

        String digits2 = StringUtils.getDigits(line2);
        char secondDigit = digits2.charAt(digits2.length() - 1);

        return (firstDigit - '0') * 10 + (secondDigit - '0');

    }

}