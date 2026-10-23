package graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;

/**
 * Text format:
 *   n m directed|undirected
 *   u v w        (m lines)
 * Blank lines and lines starting with '#' are ignored.
 */
public final class GraphIO {
    private GraphIO() {}

    public static Graph read(Path file) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(file)) {
            return read(br);
        }
    }

    public static Graph parse(String text) throws IOException {
        return read(new BufferedReader(new StringReader(text)));
    }

    public static Graph read(BufferedReader br) throws IOException {
        String line = nextLine(br);
        if (line == null) throw new IOException("empty input");
        StringTokenizer st = new StringTokenizer(line);
        if (st.countTokens() != 3) throw new IOException("header must be: n m directed|undirected");
        int n = Integer.parseInt(st.nextToken());
        int m = Integer.parseInt(st.nextToken());
        String kind = st.nextToken();
        boolean directed;
        if (kind.equals("directed")) directed = true;
        else if (kind.equals("undirected")) directed = false;
        else throw new IOException("expected directed|undirected, got " + kind);

        Graph g = new Graph(n, directed);
        for (int i = 0; i < m; i++) {
            line = nextLine(br);
            if (line == null) throw new IOException("expected " + m + " edges, got " + i);
            st = new StringTokenizer(line);
            if (st.countTokens() != 3) throw new IOException("bad edge line: " + line);
            g.addEdge(Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken()));
        }
        return g;
    }

    private static String nextLine(BufferedReader br) throws IOException {
        String s;
        while ((s = br.readLine()) != null) {
            s = s.trim();
            if (!s.isEmpty() && !s.startsWith("#")) return s;
        }
        return null;
    }
}
