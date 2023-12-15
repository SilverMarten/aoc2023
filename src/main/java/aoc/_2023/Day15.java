package aoc._2023;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.collections4.IterableUtils;
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

    private static final class Lens {
        String label;
        int focalLength;

        public Lens(String label, int focalLength) {
            super();
            this.label = label;
            this.focalLength = focalLength;
        }

        @Override
        public String toString() {
            return String.format("[%s %s]", label, focalLength);
        }

    }

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

        log.info("The focusing power of the resulting lens configuration is: {} (should be 145)",
                 part2(Arrays.asList(testLines.get(0).split(","))));

        log.setLevel(Level.INFO);

        log.info("The focusing power of the resulting lens configuration is: {}",
                 part2(Arrays.asList(lines.get(0).split(","))));
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
                     .mapToInt(Day15::hash)
                     .sum();
    }

    /**
     * Follow the initialization sequence. What is the focusing power of the
     * resulting lens configuration?
     * 
     * @param steps
     *            The steps in the initialization sequence.
     * @return The focusing power of the resulting lens configuration.
     */
    private static int part2(final List<String> steps) {

        Map<Integer, List<Lens>> boxMap = new HashMap<>();

        // Perform the steps
        steps.stream()
             .forEach(step -> {
                 String label = step.split("[-=]")[0];
                 char operation = step.indexOf('=') > 0 ? '=' : '-';
                 int value = step.charAt(step.length() - 1) - '0';
                 int box = hash(label);
                 List<Lens> boxContents = boxMap.computeIfAbsent(box, ArrayList::new);
                 if (operation == '-') {
                     boxContents.removeIf(l -> label.equals(l.label));
                 } else {
                     Optional.ofNullable(IterableUtils.find(boxContents, l -> label.equals(l.label)))
                             .ifPresentOrElse(l -> l.focalLength = value,
                                              () -> boxContents.add(new Lens(label, value)));
                 }

                 log.debug("After \"{}\":\n{}", step, boxMap);
             });

        // Add up focusing power

        return boxMap.entrySet().stream()
                     .mapToInt(b -> {
                         AtomicInteger i = new AtomicInteger(1);
                         return (b.getKey() + 1) * b.getValue().stream().mapToInt(l -> i.getAndIncrement() * l.focalLength).sum();
                     })
                     .sum();

    }

    /**
     * The HASH algorithm is a way to turn any string of characters into a
     * single number in the range 0 to 255. To run the HASH algorithm on a
     * string, start with a current value of 0. Then, for each character in the
     * string starting from the beginning:
     * <ul>
     * <li>Determine the ASCII code for the current character of the
     * string.</li>
     * <li>Increase the current value by the ASCII code you just
     * determined.</li>
     * <li>Set the current value to itself multiplied by 17.</li>
     * <li>Set the current value to the remainder of dividing itself by
     * 256.</li>
     * </ul>
     * 
     * After following these steps for each character in the string in order,
     * the current value is the output of the HASH algorithm.
     * 
     * @param label
     *            The value being hashed
     * @return The result of the HASH algorithm.
     */
    private static int hash(String label) {
        return label.chars().reduce(0, (sum, character) -> (sum + character) * 17 % 256);
    }

}