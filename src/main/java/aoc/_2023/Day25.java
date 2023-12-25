package aoc._2023;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.LabelPropagationClustering;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.slf4j.LoggerFactory;

import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;

import aoc.FileUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * https://adventofcode.com/2023/day/25
 * 
 * @author Paul Cormier
 *
 */
public class Day25 {

    private static final Logger log = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Day25.class);

    private static final String INPUT_TXT = "input/Day25.txt";

    private static final String TEST_INPUT_TXT = "testInput/Day25.txt";

    public static void main(String[] args) {

        log.info("Part 1:");
        log.setLevel(Level.DEBUG);
        log.setLevel(Level.TRACE);

        // Read the test file
        List<String> testLines = FileUtils.readFile(TEST_INPUT_TXT);

        int expectedTestResult = 54;
        int part1TestResult = part1(testLines);
        log.info("{} (should be {})", part1TestResult, expectedTestResult);

        if (part1TestResult != expectedTestResult)
            log.error("The test result doesn't match the expected value.");

        log.setLevel(Level.INFO);
        log.setLevel(Level.DEBUG);

        // Read the real file
        List<String> lines = FileUtils.readFile(INPUT_TXT);

        log.info("{}", part1(lines));

    }

    /**
     * Find the three wires you need to disconnect in order to divide the components into two separate groups. What do
     * you get if you multiply the sizes of these two groups together?
     * 
     * @param lines The lines describing the connected components.
     * @return The product of the size of the two sub-divided graphs.
     */
    private static int part1(final List<String> lines) {
        // Parse lines
        Graph<String, DefaultEdge> graph = new DefaultUndirectedGraph<>(DefaultEdge.class);

        lines.stream().forEach(l -> {
            String from = l.split(":")[0];
            graph.addVertex(from);
            Stream.of(l.split(": ")[1].split(" ")).forEach(to -> {
                graph.addVertex(to);
                graph.addEdge(from, to);
            });
        });

        if (log.isTraceEnabled()) {
            outputGraph(graph);
        }
        log.debug("Graph ({} vertexes, {} edges):\n{}", graph.vertexSet().size(), graph.edgeSet().size(), graph);

        // Brute force remove three?
        /*Set<Set<DefaultEdge>> combinations = graph.edgeSet().stream()
                                                  .flatMap(e1 -> graph.edgeSet()
                                                                      .stream()
                                                                      .filter(e2 -> e2 != e1)
                                                                      .flatMap(e2 -> graph.edgeSet()
                                                                                          .stream()
                                                                                          .filter(e3 -> e3 != e1
                                                                                                        && e3 != e2)
                                                                                          .map(e3 -> Set.of(e1, e2,
                                                                                                            e3))))
                                                  .collect(Collectors.toSet());
        
        log.debug("There are {} combinations to try.", combinations.size());*/
        // Nope.

        // Change edges into vertices?
        /*Graph<String, DefaultEdge> edgeGraph = new DefaultUndirectedGraph<>(DefaultEdge.class);
        graph.edgeSet().forEach(from -> {
            edgeGraph.addVertex(from.toString());
            graph.edgesOf(graph.getEdgeSource(from)).forEach(to -> {
                if (from != to) {
                    edgeGraph.addVertex(to.toString());
                    edgeGraph.addEdge(from.toString(), to.toString());
                }
            });
        });
        
        log.debug("Edge graph ({} vertexes, {} edges):\n{}",
                  edgeGraph.vertexSet().size(), edgeGraph.edgeSet().size(),
                  edgeGraph);
        
        BlockCutpointGraph<String, DefaultEdge> cutpointGraph = new BlockCutpointGraph<>(edgeGraph);
        Set<String> cutpoints = cutpointGraph.getCutpoints();
        log.debug("Cutpoints:{}", cutpoints);*/

        LabelPropagationClustering<String, DefaultEdge> clusteringAlgorithm = new LabelPropagationClustering<>(graph);
        Clustering<String> clustering = clusteringAlgorithm.getClustering();
        log.debug("Clusters ({}): {}", clustering.getNumberClusters(), clustering.getClusters());

        //        ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<>(graph);
        //        //Test whether the graph is connected:
        //        ci.isConnected();

        return clustering.getClusters().stream().mapToInt(Set::size).reduce(1, Math::multiplyExact);
    }

    private static void outputGraph(Graph<String, DefaultEdge> graph) {
        File imageFile = new File("out/Day25-graph.png");
        try {
            imageFile.createNewFile();
            JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(graph);
            mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
            layout.execute(graphAdapter.getDefaultParent());

            BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
            ImageIO.write(image, "PNG", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}