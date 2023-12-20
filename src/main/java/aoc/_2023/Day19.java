package aoc._2023;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
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

    private static final class RangedPart {

        private static final int RANGE_MIN = 1;
        private static final int RANGE_MAX = 4000;
        private static final Range<Integer> DEFAULT_RANGE = Range.of(RANGE_MIN, RANGE_MAX);

        Set<Range<Integer>> xRanges;
        Set<Range<Integer>> mRanges;
        Set<Range<Integer>> aRanges;
        Set<Range<Integer>> sRanges;

        public RangedPart() {
            this.xRanges = Set.of(DEFAULT_RANGE);
            this.mRanges = Set.of(DEFAULT_RANGE);
            this.aRanges = Set.of(DEFAULT_RANGE);
            this.sRanges = Set.of(DEFAULT_RANGE);
        }

        private RangedPart(RangedPart otherPart) {
            this.xRanges = Set.copyOf(otherPart.xRanges);
            this.mRanges = Set.copyOf(otherPart.mRanges);
            this.aRanges = Set.copyOf(otherPart.aRanges);
            this.sRanges = Set.copyOf(otherPart.sRanges);
        }

        public long getCombinations() {
            ToLongFunction<Range<Integer>> mapper = r -> r.getMaximum() - r.getMinimum() + 1;
            return xRanges.stream().mapToLong(mapper).sum() *
                   aRanges.stream().mapToLong(mapper).sum() *
                   mRanges.stream().mapToLong(mapper).sum() *
                   sRanges.stream().mapToLong(mapper).sum();
        }

        public RangedPart evaluateTrue(Workflow.Rule rule) {
            RangedPart newPart = new RangedPart(this);
            // Evaluate the rule, replacing the appropriate range with a (potentially)
            // altered range
            switch (rule.propertyName) {
                case "x":
                    newPart.xRanges = newPart.xRanges.stream()
                                                     .map(range -> findMatchingRange(rule, range))
                                                     .collect(Collectors.toSet());
                    break;
                case "m":
                    newPart.mRanges = newPart.mRanges.stream()
                                                     .map(range -> findMatchingRange(rule, range))
                                                     .collect(Collectors.toSet());
                    break;
                case "a":
                    newPart.aRanges = newPart.aRanges.stream()
                                                     .map(range -> findMatchingRange(rule, range))
                                                     .collect(Collectors.toSet());
                    break;
                case "s":
                    newPart.sRanges = newPart.sRanges.stream()
                                                     .map(range -> findMatchingRange(rule, range))
                                                     .collect(Collectors.toSet());
                    break;
            }
            return newPart;
        }

        private static Range<Integer> findMatchingRange(Workflow.Rule rule, Range<Integer> range) {
            return range.intersectionWith(">".equals(rule.comparisonString) ? Range.of(rule.value + 1,
                                                                                       RANGE_MAX)
                                                                            : Range.of(RANGE_MIN,
                                                                                       rule.value - 1));
        }

        public RangedPart evaluateFalse(Workflow.Rule rule) {
            RangedPart newPart = new RangedPart(this);
            // Evaluate the rule, replacing the appropriate range with a (potentially)
            // altered range
            switch (rule.propertyName) {
                case "x":
                    newPart.xRanges = newPart.xRanges.stream()
                                                     .map(range -> findNonMatchingRange(rule, range))
                                                     .collect(Collectors.toSet());
                    break;
                case "m":
                    newPart.mRanges = newPart.mRanges.stream()
                                                     .map(range -> findNonMatchingRange(rule, range))
                                                     .collect(Collectors.toSet());
                    break;
                case "a":
                    newPart.aRanges = newPart.aRanges.stream()
                                                     .map(range -> findNonMatchingRange(rule, range))
                                                     .collect(Collectors.toSet());
                    break;
                case "s":
                    newPart.sRanges = newPart.sRanges.stream()
                                                     .map(range -> findNonMatchingRange(rule, range))
                                                     .collect(Collectors.toSet());
                    break;
            }
            return newPart;
        }

        private static Range<Integer> findNonMatchingRange(Workflow.Rule rule, Range<Integer> range) {
            return range.intersectionWith(!">".equals(rule.comparisonString) ? Range.of(rule.value,
                                                                                        RANGE_MAX)
                                                                             : Range.of(RANGE_MIN,
                                                                                        rule.value));
        }

        @Override
        public int hashCode() {
            return Objects.hash(aRanges, mRanges, sRanges, xRanges);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RangedPart other = (RangedPart) obj;
            return Objects.equals(aRanges, other.aRanges) && Objects.equals(mRanges, other.mRanges) &&
                   Objects.equals(sRanges, other.sRanges) && Objects.equals(xRanges, other.xRanges);
        }

    }

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
             *     The part to test.
             * @return If the part passes the rule, the outcome, otherwise an
             *     empty {@link Optional}.
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

        @Override
        public int hashCode() {
            return Objects.hash(name, rules);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Workflow other = (Workflow) obj;
            return Objects.equals(name, other.name) && Objects.equals(rules, other.rules);
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

        long part2Result = part2(testLines);
        log.info("The number of distinct combinations that will be accepted is: {} (should be 167409079868000)",
                 part2Result);
        if (part2Result != 167409079868000L)
            log.error("The sample data is not adding up.");

        log.setLevel(Level.INFO);

        log.info("The number of distinct combinations that will be accepted is: {}", part2(lines));
    }

    /**
     * Sort through all of the parts you've been given; what do you get if you
     * add together all of the rating numbers for all of the parts that
     * ultimately get accepted?
     * 
     * @param lines
     *     The lines representing workflows and parts.
     * @return The sum of the total values of the accepted parts.
     */
    private static int part1(final List<String> lines) {

        // Parse the workflows
        Map<String, Workflow> workflowMap = parseWorkflows(lines);

        int partsStart = workflowMap.size() - 1;
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

        // Run each part through the workflows
        for (Part part : parts) {
            Workflow currentWorkflow = start;
            while (currentWorkflow != accept && currentWorkflow != reject) {
                currentWorkflow = currentWorkflow.rules.stream()
                                                       .map(r -> r.evaluate(part))
                                                       .filter(Optional::isPresent)
                                                       .findFirst()
                                                       .get()
                                                       .get();
            }
            if (currentWorkflow == accept)
                accepted.add(part);
        }

        return accepted.stream().mapToInt(Part::getSum).sum();
    }

    /**
     * Each of the four ratings (x, m, a, s) can have an integer value ranging
     * from a minimum of 1 to a maximum of 4000. Of all possible distinct
     * combinations of ratings, your job is to figure out which ones will be
     * accepted.
     * 
     * How many distinct combinations of ratings will be accepted by the Elves'
     * workflows?
     * 
     * @param lines
     *     The lines representing workflows and parts (though the parts
     *     are to be excluded).
     * @return The number of distinct combinations that will be accepted.
     */
    private static long part2(final List<String> lines) {

        // Parse the workflows
        Map<String, Workflow> workflowMap = parseWorkflows(lines);

        // The workflows form a tree, with accept and reject at the leaves
        // Traverse the tree and narrow down the ranges of values at each node
        // The total possible combinations is the sum of the products of the ranges

        // Start from "in"
        Workflow start = workflowMap.get("in");
        Workflow accept = workflowMap.get("A");
        Workflow reject = workflowMap.get("R");

        Set<RangedPart> acceptedParts = new HashSet<>();

        Queue<Entry<Workflow, RangedPart>> stateQueue = new ArrayDeque<>();
        stateQueue.add(new SimpleEntry<>(start, new RangedPart()));
        while (!stateQueue.isEmpty()) {
            Entry<Workflow, RangedPart> currentState = stateQueue.poll();
            Workflow currentWorkflow = currentState.getKey();
            RangedPart currentPart = currentState.getValue();

            // If this is the accepted state, collect the ranged part that made it here
            if (accept.equals(currentWorkflow)) {
                acceptedParts.add(currentPart);
                continue;
            }
            // If it's not the rejected state, process it accordingly
            if (!reject.equals(currentWorkflow)) {
                // Evaluate each rule and add those states to the queue
                for (Workflow.Rule rule : currentWorkflow.rules) {
                    stateQueue.add(new SimpleEntry<>(rule.outcome, currentPart.evaluateTrue(rule)));
                    // Set the remaining state which would make it to the next rule
                    currentPart = currentPart.evaluateFalse(rule);
                }

            }
        }

        log.debug("{} accepted ranged parts.", acceptedParts.size());

        return acceptedParts.stream().mapToLong(RangedPart::getCombinations).sum();
    }

    /**
     * Read the lines representing {@link Workflow}s and construct the map of
     * them all.
     * 
     * @param lines
     *     The lines representing {@link Workflow}s.
     * @return A map of {@link Workflow} names to their instances.
     */
    private static Map<String, Workflow> parseWorkflows(final List<String> lines) {
        Map<String, Workflow> workflowMap = new HashMap<>();
        for (String line : lines) {
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
                                                                         workflowMap.computeIfAbsent(outcomeName,
                                                                                                     Workflow::new));
                                           })
                                           .collect(Collectors.toList()));

        }

        log.debug("Workflows:\n{}", workflowMap.values());
        return workflowMap;
    }

}