package algorithms;

import graph.Edge;
import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopoAndDagTest {

    /** Random DAG: ranks are a random permutation, every edge goes from lower to higher rank. Weights may be negative. */
    static Graph randomDag(Random rnd, int n, int m, int wLow, int wHigh) {
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[i] = i;
        for (int i = n - 1; i > 0; i--) { int j = rnd.nextInt(i + 1); int t = rank[i]; rank[i] = rank[j]; rank[j] = t; }
        Graph g = new Graph(n, true);
        for (int k = 0; k < m && n > 1; k++) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            if (a == b) continue;
            if (rank[a] > rank[b]) { int t = a; a = b; b = t; }
            g.addEdge(a, b, wLow + rnd.nextInt(wHigh - wLow + 1));
        }
        return g;
    }

    @Test
    void validOrderOnRandomDags() {
        Random rnd = new Random(5);
        for (int t = 0; t < 100; t++) {
            int n = 1 + rnd.nextInt(80);
            Graph g = randomDag(rnd, n, rnd.nextInt(4 * n), 1, 10);
            Kahn.Result r = Kahn.run(g, new Metrics());
            assertFalse(r.hasCycle());
            assertEquals(n, r.processed());
            assertTrue(Kahn.isValidOrder(g, r.order()));
        }
    }

    @Test
    void cycleIsDetectedNotWrongOrder() throws IOException {
        Graph g = GraphIO.read(Path.of("data/tiny_cyclic.txt"));
        Kahn.Result r = Kahn.run(g, new Metrics());
        assertTrue(r.hasCycle());
        assertNull(r.order());
        assertTrue(r.processed() < g.n());
    }

    @Test
    void selfLoopIsACycle() {
        Graph g = new Graph(2, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 1, 1);
        assertTrue(Kahn.run(g, new Metrics()).hasCycle());
    }

    @Test
    void validatorRejectsWrongOrder() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        assertTrue(Kahn.isValidOrder(g, new int[]{0, 1, 2}));
        assertFalse(Kahn.isValidOrder(g, new int[]{2, 1, 0}));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 1}));
        assertFalse(Kahn.isValidOrder(g, new int[]{0, 0, 2}));
    }

    @Test
    void kahnScansExactlyMOnDag() {
        Random rnd = new Random(3);
        Graph g = randomDag(rnd, 50, 200, 1, 9);
        Metrics m = new Metrics();
        Kahn.run(g, m);
        assertEquals(g.m(), m.edgeScans);
    }

    /** Bellman-Ford from a virtual super-source connected with weight 0 to every in-degree-0 vertex. */
    static long[] bellmanFord(Graph g) {
        int n = g.n();
        long[] d = new long[n];
        Arrays.fill(d, Long.MAX_VALUE / 4);
        for (int v = 0; v < n; v++) if (g.inDegree(v) == 0) d[v] = 0;
        for (int round = 0; round < n; round++) {
            boolean changed = false;
            for (Edge e : g.edges()) {
                if (d[e.from()] + e.weight() < d[e.to()]) { d[e.to()] = d[e.from()] + e.weight(); changed = true; }
            }
            if (!changed) break;
        }
        return d;
    }

    @Test
    void dagShortestPathMatchesBellmanFordIncludingNegativeWeights() {
        Random rnd = new Random(9);
        for (int t = 0; t < 100; t++) {
            int n = 1 + rnd.nextInt(40);
            Graph g = randomDag(rnd, n, rnd.nextInt(4 * n), -10, 20);
            DagShortestPath.Result r = DagShortestPath.run(g, new Metrics());
            assertTrue(Arrays.equals(bellmanFord(g), r.dist()), "graph #" + t);
        }
    }

    @Test
    void pathIsRestoredAndItsWeightEqualsDistance() throws IOException {
        Graph g = GraphIO.read(Path.of("data/tiny_dag.txt"));
        DagShortestPath.Result r = DagShortestPath.run(g, new Metrics());
        assertEquals(9, r.sumOfDistances());
        List<Integer> p = DagShortestPath.path(r, 4);
        assertEquals(List.of(5, 2, 3, 4), p);
        long w = 0;
        for (int i = 0; i + 1 < p.size(); i++) {
            int u = p.get(i), v = p.get(i + 1);
            int best = Integer.MAX_VALUE;
            for (Edge e : g.neighbors(u)) if (e.to() == v) best = Math.min(best, e.weight());
            w += best;
        }
        assertEquals(r.dist()[4], w);
    }

    @Test
    void relaxationScansExactlyMEdges() {
        Random rnd = new Random(4);
        Graph g = randomDag(rnd, 60, 300, 1, 9);
        Metrics m = new Metrics();
        DagShortestPath.run(g, m);
        assertEquals(g.m(), m.edgeScans);
    }

    @Test
    void cyclicGraphIsRejected() {
        Graph g = new Graph(2, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.run(g, new Metrics()));
    }

    @Test
    void isolatedVerticesGetDistanceZero() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, -5);
        DagShortestPath.Result r = DagShortestPath.run(g, new Metrics());
        assertNotNull(r);
        assertEquals(-5, r.dist()[1]);
        assertEquals(0, r.dist()[2]);
    }
}
