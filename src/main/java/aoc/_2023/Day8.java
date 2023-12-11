package aoc._2023;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/8
 * 
 * @author Paul Cormier
 *
 */
public class Day8 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day8.class);

    private static final String INPUT_TXT = "input/Day8.txt";

    private static final String TEST_INPUT_TXT_1 = "testInput/Day8.1.txt";
    private static final String TEST_INPUT_TXT_2 = "testInput/Day8.2.txt";
    private static final String TEST_INPUT_TXT_3 = "testInput/Day8.3.txt";

    private static final String STARTING_NODE = "AAA";
    private static final String ENDING_NODE = "ZZZ";

    /**
     * Basic left-right node.
     */
    private static final class Node {
        String label;
        Node left;
        Node right;

        public Node(String label) {
            this.label = label;
        }

        @Override
        public int hashCode() {
            return Objects.hash(label);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Node other = (Node) obj;
            return Objects.equals(label, other.label);
        }

        @Override
        public String toString() {
            return String.format("%s = (%s, %s)", this.label, left.label, right.label);
        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test files
        List<String> testLines1 = FileUtils.readFile(TEST_INPUT_TXT_1);
        log.trace("{}", testLines1);
        List<String> testLines2 = FileUtils.readFile(TEST_INPUT_TXT_2);
        log.trace("{}", testLines2);
        List<String> testLines3 = FileUtils.readFile(TEST_INPUT_TXT_3);
        log.trace("{}", testLines3);

        log.info("It takes {} steps to reach the end. (should be 2)", part1(testLines1));
        log.info("It takes {} steps to reach the end. (should be 6)", part1(testLines2));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("It takes {} steps to reach the end.", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.TRACE);

        log.info("It takes {} steps for all starting nodes to reach the end. (should be 6)", part2(testLines3));

        log.setLevel(Level.DEBUG);
        //        log.setLevel(Level.INFO);

        log.info("It takes {} steps for all starting nodes to reach the end.", part2(lines));
    }

    /**
     * How many steps are required to reach ZZZ?
     * 
     * @param lines
     *            The lines representing the steps to take followed by the node
     *            definitions.
     * @return The number of steps between AAA and ZZZ
     */
    private static int part1(final List<String> lines) {

        String[] steps = lines.get(0).split("");

        // Parse into nodes
        Map<String, Node> nodeMap = new HashMap<>();

        lines.subList(2, lines.size()).forEach(l -> {
            String label = l.substring(0, 3);
            String leftLabel = l.substring(7, 10);
            String rightLabel = l.substring(12, 15);

            Node newNode = nodeMap.computeIfAbsent(label, Node::new);
            newNode.left = nodeMap.computeIfAbsent(leftLabel, Node::new);
            newNode.right = nodeMap.computeIfAbsent(rightLabel, Node::new);
        });
        log.debug("Nodes:\n{}", nodeMap.values());

        // Follow the steps
        final Node endNode = nodeMap.get(ENDING_NODE);
        Node currentNode = nodeMap.get(STARTING_NODE);
        int stepsTaken = 0;
        while (!currentNode.equals(endNode)) {
            log.debug(currentNode.toString());

            if ("L".equals(steps[stepsTaken % steps.length]))
                currentNode = currentNode.left;
            else
                currentNode = currentNode.right;
            stepsTaken++;
        }
        log.debug(currentNode.toString());

        return stepsTaken;
    }

    /**
     * Simultaneously start on every node that ends with A. How many steps does
     * it take before you're only on nodes that end with Z?
     * 
     * @param lines
     *            The lines representing the steps to take followed by the node
     *            definitions.
     * @return The number of steps of a equal path from all nodes ending in "A"
     *         to any node ending in "Z".
     */
    private static long part2(final List<String> lines) {

        String[] steps = lines.get(0).split("");
        log.debug("Steps: {}", lines.get(0));

        // Parse into nodes
        Map<String, Node> nodeMap = new HashMap<>();

        lines.subList(2, lines.size()).forEach(l -> {
            String label = l.substring(0, 3);
            String leftLabel = l.substring(7, 10);
            String rightLabel = l.substring(12, 15);

            Node newNode = nodeMap.computeIfAbsent(label, Node::new);
            newNode.left = nodeMap.computeIfAbsent(leftLabel, Node::new);
            newNode.right = nodeMap.computeIfAbsent(rightLabel, Node::new);
        });
        log.debug("Nodes:\n{}", nodeMap.values());

        // Follow the steps
        Set<Node> currentNodes = nodeMap.entrySet().stream()
                                        .filter(e -> e.getKey().endsWith("A"))
                                        .map(Entry::getValue)
                                        .collect(Collectors.toSet());
        int stepsTaken = 0;

        // Try checking the length of the path to an end for each starting node

        Set<Integer> individualStepsTaken = new HashSet<>();
        for (Node startingNode : currentNodes) {
            stepsTaken = 0;
            Node currentNode = startingNode;
            while (!currentNode.label.endsWith("Z")) {
                log.trace(currentNode.toString());

                if ("L".equals(steps[stepsTaken % steps.length]))
                    currentNode = currentNode.left;
                else
                    currentNode = currentNode.right;
                stepsTaken++;
            }
            log.trace(currentNode.toString());
            log.debug("It took {} (steps.length * {}) steps to get from {} to {}.", stepsTaken, stepsTaken / (double) steps.length,
                      startingNode, currentNode);
            individualStepsTaken.add(stepsTaken);
        }

        // Since they're all a multiple of steps.length compute LCM
        BigInteger lcm = individualStepsTaken.stream()
                                             .map(BigInteger::valueOf)
                                             .reduce(Day8::lcm)
                                             .get();

        log.debug("The LCM of all of the steps taken is: {}", lcm);

        return lcm.longValue();
    }

    /**
     * Find the Lowest Common Multiple (LCM) of two numbers.
     * 
     * <a href=
     * "https://www.baeldung.com/java-least-common-multiple#lcm-biginteger">Finding
     * the Least Common Multiple in Java by Baeldung</a>
     * 
     * @param number1
     *            The first of two numbers to be evaluated for LCM
     * @param number2
     *            The second of two numbers to be evaluated for LCM
     * @return The LCM of the two numbers.
     */
    public static BigInteger lcm(BigInteger number1, BigInteger number2) {
        BigInteger gcd = number1.gcd(number2);
        BigInteger absProduct = number1.multiply(number2).abs();
        return absProduct.divide(gcd);
    }
}