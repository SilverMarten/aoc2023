package aoc._2023;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import aoc.Coordinate;
import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/17
 * 
 * @author Paul Cormier
 *
 */
public class Day17 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day17.class);

    private static final String INPUT_TXT = "input/Day17.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day17.txt";

    private static enum Direction {
        UP('^'), RIGHT('>'), DOWN('v'), LEFT('<');

        char symbol;

        Direction(char symbol) {
            this.symbol = symbol;
        }

        public static Direction oppositeOf(Direction direction) {
            switch (direction) {
                case UP:
                    return DOWN;
                case RIGHT:
                    return LEFT;
                case DOWN:
                    return UP;
                case LEFT:
                    return RIGHT;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private static class Node {
        Coordinate position;
        // Cost to enter this node
        int cost;

        // Costs to leave
        Map<Direction, Integer> costMap = new HashMap<>();

        public Node(Coordinate position, int cost) {
            this.position = position;
            this.cost = cost;
        }

        /**
         * Depending on the direction the path is coming from, and the current length on
         * the path, find the least cost to leave.
         * 
         * @param fromDirection The direction of the path entering this node.
         * @param pathLength The current length of the path.
         * @return The least cost direction to traverse this node and the cost of that
         *     path.
         */
        public Entry<Direction, Integer> findLeastCostToLeave(Direction fromDirection, int pathLength) {

            // Find the least cost to leave given the conditions:
            // - Can't go back the way you came
            // - Can't go a third step in the same direction
            return costMap.entrySet().stream()
                          .filter(e -> Direction.oppositeOf(fromDirection) != e.getKey())
                          .filter(e -> e.getKey() == fromDirection && pathLength <= 3)
                          .sorted(Comparator.comparing(Entry::getValue))
                          .findFirst()
                          .get();

        }

        @Override
        public String toString() {
            return String.format("%s %s", position, cost);
        }
    }

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);

        log.info("The least heat loss it can incur is: {}. (should be 102)", part1(testLines));

        log.setLevel(Level.INFO);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("The least heat loss it can incur is: {}.", part1(lines));

        // PART 2
        log.info("Part 2:");
        log.setLevel(Level.DEBUG);

        log.info("{}", part2(testLines));

        log.setLevel(Level.INFO);

        log.info("{}", part2(lines));
    }

    /**
     * Directing the crucible from the lava pool to the machine parts factory, but
     * not moving more than three consecutive blocks in the same direction, what is
     * the least heat loss it can incur?
     * 
     * @param lines The lines describing the heat map.
     * @return The minimum heat loss when traversing the city.
     */
    private static int part1(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        // Map the heat loss
        Map<Coordinate, Node> heatMap = Coordinate.mapDigits(lines)
                                                  .entrySet()
                                                  .stream()
                                                  .collect(Collectors.toMap(Entry::getKey,
                                                                            e -> new Node(e.getKey(), e.getValue())));

        log.atDebug()
           .setMessage("Heat map:\n{}")
           .addArgument(() -> Coordinate.printMap(rows, columns, heatMap, n -> (char) (n.cost + '0')))
           .log();

        // Find the least-cost path

        // Start from the end, compute the least costs
        Queue<Node> nodesToCheck = new ArrayDeque<>();
        Set<Node> checkedNodes = new HashSet<>();
        Node endNode = heatMap.get(Coordinate.of(rows, columns));
        endNode.costMap.putAll(Map.of(Direction.DOWN, 0, Direction.RIGHT, 0));

        nodesToCheck.add(endNode);
        while (!nodesToCheck.isEmpty()) {
            Node nodeToCheck = nodesToCheck.poll();
            log.debug("Checking node: {}", nodeToCheck);
            checkedNodes.add(nodeToCheck);

            // Do something
            // For the neighbours, add the cost to traverse this node in each direction,
            // for each path length
            Set<Node> neighbours = nodeToCheck.position.findAdjacent()
                                                       .stream()
                                                       .filter(c -> c.getColumn() == nodeToCheck.position.getColumn()
                                                                    || c.getRow() == nodeToCheck.position.getRow())
                                                       .map(heatMap::get)
                                                       .filter(Objects::nonNull)
                                                       .filter(n -> n.costMap.size() != n.position.findAdjacent()
                                                                                                  .size())
                                                       .collect(Collectors.toSet());
            neighbours.forEach(n -> {
                Direction traversalDirection = null;
                if (n.position.getRow() == nodeToCheck.position.getRow())
                    traversalDirection = n.position.getColumn() > nodeToCheck.position.getColumn()
                            ? Direction.LEFT
                            : Direction.RIGHT;
                if (n.position.getColumn() == nodeToCheck.position.getColumn())
                    traversalDirection = n.position.getRow() < nodeToCheck.position.getRow()
                            ? Direction.DOWN
                            : Direction.UP;

                // What does it cost for that node to get to this node?
                log.debug("{} from {} to {}", traversalDirection, n, nodeToCheck);
                int cost = nodeToCheck.cost + nodeToCheck.findLeastCostToLeave(traversalDirection, 0).getValue();
                log.debug("Costs: {}", cost);
                n.costMap.put(traversalDirection, cost);
            });

            // Add the neighbours to the queue
            nodesToCheck.addAll(neighbours);

        }

        log.debug("{} nodes have been checked.", checkedNodes.size());

        // Start from the start and traverse the least-cost path.
        List<Node> path = new ArrayList<>();
        Node currentNode = heatMap.get(Coordinate.of(1, 1));
        while (currentNode != endNode) {
            path.add(currentNode);

            // Temp short-circuit
            currentNode = endNode;
        }

        // Add up the path costs?
        // Shouldn't the least cost path out of the start node be the answer?
        // currentNode.findLeastCostToLeave(Direction.DOWN, 0).getValue();

        return -1;
    }

    private static int part2(final List<String> lines) {

        return -1;
    }

}