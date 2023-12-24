package aoc._2023;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.Direction;
import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/23
 * 
 * @author Paul Cormier
 *
 */
public class Day23 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day23.class);

    private static final String INPUT_TXT = "input/Day23.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day23.txt";

    private static final class Node {
        private final Coordinate position;
        private final char type;

        private final Map<Node, Integer> costMap;

        public Node(Coordinate position, char type) {
            this.position = position;
            this.type = type;
            this.costMap = new HashMap<>();
        }

        public Coordinate getPosition() {
            return position;
        }

        public char getType() {
            return type;
        }

        public Map<Node, Integer> getCostMap() {
            return costMap;
        }

        @Override
        public String toString() {
            return String.format("%s %s", this.type, this.position);
        }

    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);

        int expectedTestResult = 94;
        int part1TestResult = part1(testLines);
        log.info("The longest path is: {} (should be {})", part1TestResult, expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The longest path is: {}", part1(lines));

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

    /**
     * Find the longest hike you can take through the hiking trails listed on your
     * map. How many steps long is the longest hike?
     * 
     * @param lines The lines describing the map.
     * @return The longest path.
     */
    private static int part1(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        // Parse the map
        Map<Coordinate, Node> nodeMap = parseNodes(lines);

        log.atDebug()
           .setMessage("Nodes:\n{}")
           .addArgument(() -> Coordinate.printMap(1, 1, rows, columns, nodeMap, n -> n.type, '#'))
           .log();

        // Connect the nodes
        nodeMap.values().stream().forEach(node -> {
            // Flat terrain checks all
            Coordinate nodePosition = node.getPosition();
            if (node.getType() == '.')
                nodePosition.findOrthogonalAdjacent()
                            .stream()
                            .filter(nodeMap::containsKey)
                            .map(nodeMap::get)
                            .forEach(neighbour -> {
                                if (neighbour.getType() == '.') {
                                    node.getCostMap().put(neighbour, 0);
                                } else {
                                    // If it's a slope it can't be reached from its direction
                                    Coordinate neighbourDirection = Direction.withSymbol(neighbour.getType())
                                                                             .getTranslation();
                                    Coordinate neighbourPosition = neighbour.getPosition();
                                    if (!nodePosition.equals(Coordinate.of(neighbourPosition.getRow()
                                                                           + neighbourDirection.getRow(),
                                                                           neighbourPosition.getColumn() + neighbourDirection.getColumn())))
                                        node.getCostMap().put(neighbour, 0);
                                }
                            });
            else {
                // Sloped terrain always connects to its neighbour in its direction
                Coordinate neighbourDirection = Direction.withSymbol(node.getType()).getTranslation();
                Coordinate neighbourPosition = Coordinate.of(nodePosition.getRow() + neighbourDirection.getRow(),
                                                             nodePosition.getColumn() + neighbourDirection.getColumn());
                if (nodeMap.containsKey(neighbourPosition)) {
                    Node neighbour = nodeMap.get(neighbourPosition);
                    // Make sure it's not two slopes facing each other
                    char neighbourType = neighbour.getType();
                    if (!(neighbourType != '.'
                          && node.getType() == Direction.withSymbol(neighbourType).opposite().getSymbol()))
                        node.getCostMap().put(neighbour, 0);
                }
            }
        });

        // Determine costs
        Map<Node, Integer> maxCostMap = new HashMap<>();
        Node end = nodeMap.get(Coordinate.of(rows, columns-1));
        maxCostMap.put(end, 0);
        Node start = nodeMap.get(Coordinate.of(1, 2));
        Stack<Node> nodesToCheck = new Stack<>();
        Queue<Node> nextNodes = new ArrayDeque<>();
        // Build the paths
        nextNodes.add(start);
        while (!nextNodes.isEmpty()) {
            Node currentNode = nextNodes.poll();
            
            if (!nodesToCheck.contains(currentNode) && !maxCostMap.containsKey(currentNode)) {
                nodesToCheck.add(currentNode);
            }
            nextNodes.addAll(currentNode.costMap.keySet()
                                                .stream()
                                                .filter(n -> !nodesToCheck.contains(n))
                                                .collect(Collectors.toList()));
        }

        log.debug("Path to check:\n{}", nodesToCheck);

        // Start from the end
        while (!nodesToCheck.isEmpty()) {
            Node currentNode = nodesToCheck.pop();
            int maxCost = currentNode.getCostMap()
                                     .entrySet()
                                     .stream()
                                     .filter(e -> !nodesToCheck.contains(e.getKey()))
                                     .mapToInt(Entry::getValue)
                                     .max()
                                     .orElse(0);
            maxCostMap.put(currentNode, Math.max(maxCostMap.getOrDefault(currentNode, 0), maxCost + 1));
        }

        log.debug("Max cost map:\n{}", maxCostMap);

        // Find the longest path
        List<Node> path = new ArrayList<>();
        Node next = start;
        while (next != null) {
            path.add(next);
            next = next.getCostMap()
                       .keySet()
                       .stream()
                       .filter(n -> !path.contains(n))
                       .sorted(Comparator.comparing(maxCostMap::get))
                       .sorted((a, b) -> -1)
                       .findFirst()
                       .orElse(null);
        }

        log.debug("Path from {}, to {}:\n{}", start.getPosition(), path.get(path.size() - 1).getPosition(), path);

        return path.size();
    }

    private static Map<Coordinate, Node> parseNodes(List<String> lines) {

        return Coordinate.mapCoordinates(lines, '#')
                         .entrySet()
                         .stream()
                         .map(e -> new Node(e.getKey(), e.getValue()))
                         .collect(Collectors.toMap(Node::getPosition, Function.identity()));

    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}