package aoc._2023;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/12
 * 
 * @author Paul Cormier
 *
 */
public class Day12 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day12.class);

    private static final String INPUT_TXT = "input/Day12.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day12.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);
        log.setLevel(Level.TRACE);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);
        log.trace("{}", testLines);

        log.info("The sum of the different arrangements of the springs is: {} (should be 21)", part1(testLines));

        log.setLevel(Level.INFO);
        // log.setLevel(Level.DEBUG);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the different arrangements of the springs is: {} (should be less than 6,142,112, it is 7025)",
                 part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("The sum of the different arrangements of the springs is: {} (should be 525,152)", part2(testLines));

        log.setLevel(Level.DEBUG);

        log.info("The sum of the different arrangements of the springs is: {} (should be less than 2,059,101,273,163, but greater than 19,302,506,771)",
                 part2(lines));
    }

    /**
     * For each row, count all of the different arrangements of operational and
     * broken springs that meet the given criteria. What is the sum of those
     * counts?
     * 
     * @param lines
     *     The lines containing information about the springs
     * @return The sum of the different arrangements of the springs
     */
    private static long part1(final List<String> lines) {

        Map<String, Long> cache = new HashMap<>();

        return lines.stream()
                    .peek(log::debug)
                    .mapToLong(l -> countCombinations(l.split(" ")[0],
                                                      Stream.of(l.split(" ")[1].split(",")).map(Integer::parseInt)
                                                            .collect(Collectors.toList()),
                                                      cache))
                    .peek(i -> log.debug("{} combinations", i))
                    .sum();
    }

    /**
     * For each row, count all of the different arrangements of operational and
     * broken springs that meet the given criteria. What is the sum of those
     * counts?
     * 
     * @param lines
     *     The lines containing information about the springs, to be duplicated 5
     *     times.
     * @return The sum of the different arrangements of the springs
     */
    private static long part2(final List<String> lines) {

        Map<String, Long> cache = new HashMap<>();
        return lines.stream()
                    .peek(log::debug)
                    .mapToLong(l -> countCombinations(Collections.nCopies(5, l.split(" ")[0])
                                                                 .stream()
                                                                 .collect(Collectors.joining("?")),
                                                      Stream.of(Collections.nCopies(5, l.split(" ")[1])
                                                                           .stream()
                                                                           .collect(Collectors.joining(","))
                                                                           .split(","))
                                                            .map(Integer::parseInt)
                                                            .collect(Collectors.toList()),
                                                      cache))
                    .peek(i -> log.debug("{} combinations (cache size: {})", i, cache.size()))
                    .sum();
    }

    /**
     * Given a description of a line of springs, and the known groups of damaged
     * springs, find how many configurations there could be.
     * 
     * @param springString
     *     The String describing the line of springs
     * @param groups
     *     The sizes of each contiguous group of damaged springs
     * @return The number of combinations which would satisfy the known
     *     information in the line of springs as well as the list of groups
     *     of damaged springs.
     */
    private static long countCombinations(String springString, List<Integer> groups, Map<String, Long> cache) {

        String cacheKey = springString + groups.toString();
        long combinations = cache.getOrDefault(cacheKey, 0L);
        // Return early on cache hit
        if (cache.containsKey(cacheKey)) {
            log.trace("{} {} - {} combinations", springString, groups, combinations);
            return combinations;
        }
        // Try recursion?
        // See:
        // https://www.reddit.com/r/adventofcode/comments/18ghux0/comment/kd0npmi/?utm_source=share&utm_medium=web2x&context=3
        // Catch the end of the recursion
        if (springString.isEmpty() || groups.isEmpty()) {
            combinations = springString.replaceAll("\\.|\\?", "").isEmpty() && groups.isEmpty() ? 1 : 0;
        } else {
            switch (springString.charAt(0)) {
                // Periods at the start don't affect the outcome
                case '.':
                    combinations = countCombinations(springString.substring(1), groups, cache);
                    break;

                // Could this match the first group?
                case '#':
                    int firstGroupSize = groups.get(0);
                    // Not enough springs, operational springs within n, too many #s
                    if (springString.length() < firstGroupSize
                        || springString.substring(0, firstGroupSize).contains(".")) {
                        // This can't be big enough for any match of the groups
                        combinations = 0;
                    } else if (springString.length() > firstGroupSize &&
                               springString.charAt(firstGroupSize) == '#') {
                        combinations = 0;
                    } else {
                        // Remove those characters and the group and recurse
                        List<Integer> newGroups = new ArrayList<>(groups);
                        newGroups.remove(0);
                        String newSpringString = springString.substring(firstGroupSize);
                        // A group of broken springs has to be followed by an operational string
                        if (newSpringString.startsWith("?"))
                            newSpringString = "." + newSpringString.substring(1);
                        combinations = countCombinations(newSpringString,
                                                         newGroups,
                                                         cache);
                    }
                    break;

                // Try both with a question mark
                case '?':
                    combinations = countCombinations("." + springString.substring(1), groups, cache) +
                                   countCombinations("#" + springString.substring(1), groups, cache);
                    break;

                default:
                    combinations = 0;

            }
        }
        log.trace("{} {} - {} combinations", springString, groups, combinations);
        cache.put(cacheKey, combinations);
        return combinations;

    }

}