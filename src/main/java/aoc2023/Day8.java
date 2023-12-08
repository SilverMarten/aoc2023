package aoc2023;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private static final String STARTING_NODE = "AAA";
    private static final String ENDING_NODE = "ZZZ";

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

        log.info("It takes {} steps to reach the end. (should be 2)", part1(testLines1));
        log.info("It takes {} steps to reach the end. (should be 6)", part1(testLines2));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("It takes {} steps to reach the end.", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines1));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
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

        return stepsTaken;
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}