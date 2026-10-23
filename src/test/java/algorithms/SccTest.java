package algorithms;

import graph.Edge;
import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SccTest {

    /** BFS from s following edges forward (or backward when reverse == true). */
    static boolean[] bfs(Graph g, int s, boolean reverse) {
        boolean[] seen = new boolean[g.n()];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        seen[s] = true;
        q.add(s);
        // build a reversed adjacency on demand
        java.util.List<java.util.List<Integer>> radj = null;
        if (reverse) {
            radj = new java.util.ArrayList<>();
            for (int i = 0; i < g.n(); i++) radj.add(new java.util.ArrayList<>());
            for (Edge e : g.edges()) radj.get(e.to()).add(e.from());
        }
        while (!q.isEmpty()) {
            int v = q.poll();
            if (!reverse) {
                for (Edge e : g.neighbors(v)) if (!seen[e.to()]) { seen[e.to()] = true; q.add(e.to()); }
            } else {
                for (int w : radj.get(v)) if (!seen[w]) { seen[w] = true; q.add(w); }
            }
        }
        return seen;
    }

    @Test
    void matchesBruteForceOnRandomGraphs() {
        Random rnd = new Random(11);
        for (int t = 0; t < 60; t++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(3 * n);
            Graph g = new Graph(n, true);
            for (int i = 0; i < m; i++) g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(10));
            Kosaraju.Result r = Kosaraju.run(g, new Metrics());

            boolean[][] fwd = new boolean[n][], bwd = new boolean[n][];
            for (int v = 0; v < n; v++) { fwd[v] = bfs(g, v, false); bwd[v] = bfs(g, v, true); }
            int brute = 0;
            boolean[] counted = new boolean[n];
            for (int u = 0; u < n; u++) {
                if (!counted[u]) {
                    brute++;
                    for (int v = 0; v < n; v++) if (fwd[u][v] && bwd[u][v]) counted[v] = true;
                }
                for (int v = 0; v < n; v++) {
                    boolean same = fwd[u][v] && bwd[u][v];
                    assertEquals(same, r.comp()[u] == r.comp()[v], "graph #" + t + " pair " + u + "," + v);
                }
            }
            assertEquals(brute, r.count(), "graph #" + t);
        }
    }

    @Test
    void knownCyclicGraph() throws IOException {
        Graph g = GraphIO.read(Path.of("data/tiny_cyclic.txt"));
        Kosaraju.Result r = Kosaraju.run(g, new Metrics());
        assertEquals(3, r.count());
        assertEquals(r.comp()[0], r.comp()[1]);
        assertEquals(r.comp()[1], r.comp()[2]);
    }

    @Test
    void scansExactly2m() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1); g.addEdge(1, 2, 1); g.addEdge(2, 0, 1); g.addEdge(2, 3, 1); g.addEdge(3, 3, 1);
        Metrics m = new Metrics();
        Kosaraju.run(g, m);
        assertEquals(2L * g.m(), m.edgeScans);
    }

    @Test
    void stackSafetyOnPathOf100000Vertices() {
        int n = 100_000;
        Graph g = new Graph(n, true);
        for (int i = 0; i + 1 < n; i++) g.addEdge(i, i + 1, 1);
        Metrics m = new Metrics();
        Kosaraju.Result r = assertDoesNotThrow(() -> Kosaraju.run(g, m));   // no StackOverflowError
        assertEquals(n, r.count());
        assertEquals(n, m.maxDepth);          // the explicit stack really got n deep
    }

    @Test
    void stackSafetyOnOneBigCycle() {
        int n = 100_000;
        Graph g = new Graph(n, true);
        for (int i = 0; i < n; i++) g.addEdge(i, (i + 1) % n, 1);
        assertEquals(1, Kosaraju.run(g, new Metrics()).count());
    }
}
