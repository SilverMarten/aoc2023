package aoc._2023;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/19
 * 
 * @author Paul Cormier
 *
 */
public class Day19 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day19.class);

    private static final String INPUT_TXT = "input/Day19.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day19.txt";

    private static final class Part {
        /** Extremely cool looking */
        final int x;
        /** Musical (it makes a noise when you hit it) */
        final int m;
        /** Aerodynamic */
        final int a;
        /** Shiny */
        final int s;

        public Part(int x, int m, int a, int s) {
            this.x = x;
            this.m = m;
            this.a = a;
            this.s = s;
        }

        public int getX() {
            return x;
        }

        public int getM() {
            return m;
        }

        public int getA() {
            return a;
        }

        public int getS() {
            return s;
        }

        public int getSum() {
            return x + m + a + s;
        }

        @Override
        public String toString() {
            return String.format("{x=%d,m=%d,a=%d,s=%d}", x, m, a, s);
        }

    }

    private static final class Workflow {

        static class Rule {
            static Map<String, Function<Part, Integer>> propertyMap = Map.of("x", Part::getX,
                                                                             "m", Part::getM,
                                                                             "a", Part::getA,
                                                                             "s", Part::getS,
                                                                             "1", p -> 1);

            static Map<String, BiPredicate<Integer, Integer>> comparisonMap = Map.of(">", (a, b) -> a > b,
                                                                                     "<", (a, b) -> a < b);

            final Function<Part, Integer> property;
            final String propertyName;
            final BiPredicate<Integer, Integer> comparison;
            final String comparisonString;
            final int value;
            final Workflow outcome;

            private Rule(String propertyName, String comparisonString, int value, Workflow outcome) {
                this.property = propertyMap.get(propertyName);
                this.propertyName = propertyName;
                this.comparison = comparisonMap.get(comparisonString);
                this.comparisonString = comparisonString;
                this.value = value;
                this.outcome = outcome;
            }

            public static Rule from(String conditionString, Workflow outcome) {
                if (StringUtils.isNotBlank(conditionString))
                    // Parse the property, comparison, and value which when true lead to the outcome
                    return new Rule(conditionString.substring(0, 1),
                                    conditionString.substring(1, 2),
                                    Integer.parseInt(StringUtils.getDigits(conditionString)),
                                    outcome);
                else
                    // Default, always true rule
                    return new Rule("1", ">", 0, outcome);
            }

            /**
             * @param part
             *            The part to test.
             * @return If the part passes the rule, the outcome, otherwise an
             *         empty {@link Optional}.
             */
            public Optional<Workflow> evaluate(Part part) {
                return Optional.ofNullable(comparison.test(property.apply(part), value) ? outcome : null);
            }

            @Override
            public String toString() {
                return String.format("%s%s%d:%s", propertyName, comparisonString, value, outcome.name);
            }

        }

        final String name;
        final List<Rule> rules;

        public Workflow(String name) {
            this.name = name;
            this.rules = new ArrayList<>();
        }

        @Override
        public String toString() {
            return String.format("%s%s", name, rules);
        }
    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);

        log.info("The sum of the total values of the accepted parts is: {} (should be 19114)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The sum of the total values of the accepted parts is: {}", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * Sort through all of the parts you've been given; what do you get if you
     * add together all of the rating numbers for all of the parts that
     * ultimately get accepted?
     * 
     * @param lines
     *            The lines representing workflows and parts.
     * @return The sum of the total values of the accepted parts.
     */
    private static int part1(final List<String> lines) {

        // Parse the workflows
        Map<String, Workflow> workflowMap = new HashMap<>();
        int partsStart = 0;
        for (String line : lines) {
            partsStart++;
            if (line.isBlank())
                break;

            // Find the workflow to add rules to
            Workflow newWorkflow = workflowMap.computeIfAbsent(line.substring(0, line.indexOf('{')), Workflow::new);
            // Parse the rules
            newWorkflow.rules.addAll(Stream.of(line.substring(line.indexOf('{') + 1, line.length() - 1).split(","))
                                           .map(r -> {
                                               String conditionString = r.indexOf(':') >= 0 ? r.split(":")[0] : "";
                                               String outcomeName = r.indexOf(':') >= 0 ? r.split(":")[1] : r;
                                               return Workflow.Rule.from(conditionString,
                                                                         workflowMap.computeIfAbsent(outcomeName, Workflow::new));
                                           })
                                           .collect(Collectors.toList()));

        }

        log.debug("Workflows:\n{}", workflowMap.values());

        // Parse the parts 
        Set<Part> parts = lines.stream()
                               .skip(partsStart)
                               .map(line -> {
                                   String[] values = StringUtils.strip(line, "{}").split(",");
                                   return new Part(Integer.parseInt(values[0].substring(2)),
                                                   Integer.parseInt(values[1].substring(2)),
                                                   Integer.parseInt(values[2].substring(2)),
                                                   Integer.parseInt(values[3].substring(2)));
                               })
                               .collect(Collectors.toSet());

        log.debug("Parts:\n{}", parts);

        // Start from "in"
        Workflow start = workflowMap.get("in");
        Workflow accept = workflowMap.get("A");
        Workflow reject = workflowMap.get("R");
        Set<Part> accepted = new HashSet<>();

        return accepted.stream().mapToInt(Part::getSum).sum();
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}