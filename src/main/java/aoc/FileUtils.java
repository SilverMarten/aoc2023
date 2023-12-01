package aoc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Useful methods for reading files.
 * 
 * @author Paul Cormier
 *
 */
public final class FileUtils {

    /**
     * Utility classes have private constructors.
     */
    private FileUtils() {
    }

    /**
     * Read a file, available on the classpath, into a {@link List} of strings.
     * 
     * @param fileName
     *            The name of a file which can be found on the classpath.
     * @return A {@link List} of strings, one for each line in the file. Returns
     *         an empty list if there were any errors opening the file.
     */
    public static List<String> readFile(final String fileName) {
        try {
            return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(fileName).toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Read a file, available on the classpath, into a {@link Stream} of
     * strings. The stream can only be read from once.
     * 
     * @param fileName
     *            The name of a file which can be found on the classpath.
     * @return A {@link Stream} of strings, one for each line in the file.
     *         Returns an empty stream if there were any errors opening the
     *         file.
     */
    public static Stream<String> readFileToStream(final String fileName) {
        try {
            return Files.lines(Paths.get(ClassLoader.getSystemResource(fileName).toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }

}
