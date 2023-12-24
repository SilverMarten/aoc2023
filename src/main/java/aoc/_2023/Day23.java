package aoc._2023;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.LoggerFactory;

import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;

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

        private final Set<Node> neighbours;

        public Node(Coordinate position, char type) {
            this.position = position;
            this.type = type;
            this.neighbours = new HashSet<>();
        }

        public Coordinate getPosition() {
            return position;
        }

        public char getType() {
            return type;
        }

        public Set<Node> getNeighbours() {
            return neighbours;
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

        expectedTestResult = 154;
        int part2TestResult = part2(testLines);
        log.info("The longest path is: {} (should be {})", part2TestResult, expectedTestResult);

        if (part2TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);

        log.info("The longest path is: {}", part2(lines));
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

        // Create a new graph
        Graph<Node, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        // First add all vertexes
        nodeMap.values().forEach(graph::addVertex);

        // Add edges
        nodeMap.values().stream().forEach(node -> {
            // Flat terrain checks all
            Coordinate nodePosition = node.getPosition();
            if (node.getType() == '.') {
                nodePosition.findOrthogonalAdjacent()
                            .stream()
                            .filter(nodeMap::containsKey)
                            .map(nodeMap::get)
                            .forEach(neighbour -> {
                                if (neighbour.getType() == '.') {
                                    node.getNeighbours().add(neighbour);
                                    // Bi-directional
                                    graph.addEdge(node, neighbour);
                                    graph.addEdge(neighbour, node);
                                } else {
                                    // If it's a slope it can't be reached from its direction
                                    Coordinate neighbourDirection = Direction.withSymbol(neighbour.getType())
                                                                             .getTranslation();
                                    Coordinate neighbourPosition = neighbour.getPosition();
                                    if (!nodePosition.equals(Coordinate.of(neighbourPosition.getRow()
                                                                           + neighbourDirection.getRow(),
                                                                           neighbourPosition.getColumn() + neighbourDirection.getColumn()))) {
                                        node.getNeighbours().add(neighbour);
                                        graph.addEdge(node, neighbour);
                                    }

                                    // If it's sloped away, there's no return path
                                    if (!neighbourPosition.equals(Coordinate.of(nodePosition.getRow()
                                                                                + neighbourDirection.getRow(),
                                                                                nodePosition.getColumn() + neighbourDirection.getColumn())))
                                        graph.addEdge(neighbour, node);
                                }
                            });
            } else {
                // Sloped terrain always (only) connects to its neighbour in its direction
                Coordinate neighbourDirection = Direction.withSymbol(node.getType()).getTranslation();
                Coordinate neighbourPosition = Coordinate.of(nodePosition.getRow() + neighbourDirection.getRow(),
                                                             nodePosition.getColumn() + neighbourDirection.getColumn());
                if (nodeMap.containsKey(neighbourPosition)) {
                    Node neighbour = nodeMap.get(neighbourPosition);
                    // Make sure it's not two slopes facing each other
                    char neighbourType = neighbour.getType();
                    if (!(neighbourType != '.'
                          && node.getType() == Direction.withSymbol(neighbourType).opposite().getSymbol())) {
                        node.getNeighbours().add(neighbour);
                        graph.addEdge(node, neighbour);
                    }
                }
            }
        });

        if (log.isTraceEnabled()) {
            outputGraph(graph);
        }

        Node start = nodeMap.get(Coordinate.of(1, 2));
        Node end = nodeMap.get(Coordinate.of(rows, columns - 1));

        AllDirectedPaths<Node, DefaultEdge> algorithm = new AllDirectedPaths<Node, DefaultEdge>(graph);
        List<GraphPath<Node, DefaultEdge>> paths = algorithm.getAllPaths(start, end, true, nodeMap.size());

        List<Node> path = paths.stream()
                               .sorted(Comparator.comparingInt((GraphPath<Node, DefaultEdge> g) -> g.getLength())
                                                 .reversed())
                               .findFirst()
                               .map(GraphPath::getVertexList)
                               .orElseGet(Collections::emptyList);

        log.debug("Path from {}, to {}:\n{}", start.getPosition(), path.get(path.size() - 1).getPosition(), path);

        return path.size() - 1;
    }

    private static void outputGraph(Graph<Node, DefaultEdge> graph) {
        File imageFile = new File("out/Day23-graph.png");
        try {
            imageFile.createNewFile();
            JGraphXAdapter<Node, DefaultEdge> graphAdapter = new JGraphXAdapter<Node, DefaultEdge>(graph);
            mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
            layout.execute(graphAdapter.getDefaultParent());

            BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
            ImageIO.write(image, "PNG", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Map<Coordinate, Node> parseNodes(List<String> lines) {

        return Coordinate.mapCoordinates(lines, '#')
                         .entrySet()
                         .stream()
                         .map(e -> new Node(e.getKey(), e.getValue()))
                         .collect(Collectors.toMap(Node::getPosition, Function.identity()));

    }

    /**
     * Find the longest hike you can take through the surprisingly dry hiking trails listed on your map. How many steps
     * long is the longest hike?
     * 
     * @param lines The lines describing the map.
     * @return The longest path.
     */
    private static int part2(final List<String> lines) {

        int rows = lines.size();
        int columns = lines.get(0).length();

        // Parse the map
        Map<Coordinate, Node> nodeMap = parseNodes(lines);

        log.atDebug()
           .setMessage("Nodes:\n{}")
           .addArgument(() -> Coordinate.printMap(1, 1, rows, columns, nodeMap, n -> n.type, '#'))
           .log();

        // Create a new graph
        Graph<Node, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        // First add all vertexes
        nodeMap.values().forEach(graph::addVertex);

        // Add edges
        nodeMap.values().stream().forEach(node -> {
            // Flat terrain checks all
            Coordinate nodePosition = node.getPosition();
            /*if (node.getType() == '.') {*/
            nodePosition.findOrthogonalAdjacent()
                        .stream()
                        .filter(nodeMap::containsKey)
                        .map(nodeMap::get)
                        .forEach(neighbour -> {
                            /*if (neighbour.getType() == '.') {*/
                            node.getNeighbours().add(neighbour);
                            // Bi-directional
                            graph.addEdge(node, neighbour);
                            //                            graph.addEdge(neighbour, node);
                            /* } else {
                                 // If it's a slope it can't be reached from its direction
                                 Coordinate neighbourDirection = Direction.withSymbol(neighbour.getType())
                                                                          .getTranslation();
                                 Coordinate neighbourPosition = neighbour.getPosition();
                                 if (!nodePosition.equals(Coordinate.of(neighbourPosition.getRow()
                                                                        + neighbourDirection.getRow(),
                                                                        neighbourPosition.getColumn() + neighbourDirection.getColumn()))) {
                                     node.getNeighbours().add(neighbour);
                                     graph.addEdge(node, neighbour);
                                 }
                            
                                 // If it's sloped away, there's no return path
                                 if (!neighbourPosition.equals(Coordinate.of(nodePosition.getRow()
                                                                             + neighbourDirection.getRow(),
                                                                             nodePosition.getColumn() + neighbourDirection.getColumn())))
                                     graph.addEdge(neighbour, node);
                             }*/
                        });
            /*} else {
                // Sloped terrain always (only) connects to its neighbour in its direction
                Coordinate neighbourDirection = Direction.withSymbol(node.getType()).getTranslation();
                Coordinate neighbourPosition = Coordinate.of(nodePosition.getRow() + neighbourDirection.getRow(),
                                                             nodePosition.getColumn() + neighbourDirection.getColumn());
                if (nodeMap.containsKey(neighbourPosition)) {
                    Node neighbour = nodeMap.get(neighbourPosition);
                    // Make sure it's not two slopes facing each other
                    char neighbourType = neighbour.getType();
                    if (!(neighbourType != '.'
                          && node.getType() == Direction.withSymbol(neighbourType).opposite().getSymbol())) {
                        node.getNeighbours().add(neighbour);
                        graph.addEdge(node, neighbour);
                    }
                }
            }*/
        });

        if (log.isTraceEnabled()) {
            outputGraph(graph);
        }

        Node start = nodeMap.get(Coordinate.of(1, 2));
        Node end = nodeMap.get(Coordinate.of(rows, columns - 1));

        AllDirectedPaths<Node, DefaultEdge> algorithm = new AllDirectedPaths<>(graph);
        List<GraphPath<Node, DefaultEdge>> paths = algorithm.getAllPaths(start, end, true, nodeMap.size());

        List<Node> path = paths.stream()
                               .sorted(Comparator.comparingInt((GraphPath<Node, DefaultEdge> g) -> g.getLength())
                                                 .reversed())
                               .findFirst()
                               .map(GraphPath::getVertexList)
                               .orElseGet(Collections::emptyList);

        log.debug("Path from {}, to {}:\n{}", start.getPosition(), path.get(path.size() - 1).getPosition(), path);

        return path.size() - 1;
    }

}