package algorithms;

import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MstTest {

    /** Random connected graph: random spanning tree + extra edges (self-loops and parallel edges allowed). */
    static Graph randomConnected(Random rnd, int n, int extra) {
        Graph g = new Graph(n, false);
        for (int i = 1; i < n; i++) g.addEdge(rnd.nextInt(i), i, 1 + rnd.nextInt(50));
        for (int k = 0; k < extra; k++) g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(50));
        return g;
    }

    @Test
    void kruskalAndPrimAgreeOn100RandomConnectedGraphs() {
        Random rnd = new Random(7);
        for (int t = 0; t < 100; t++) {
            int n = 2 + rnd.nextInt(60);
            Graph g = randomConnected(rnd, n, rnd.nextInt(4 * n));
            MstResult k = Kruskal.run(g, new Metrics());
            MstResult p = Prim.run(g, new Metrics());
            assertEquals(k.totalWeight(), p.totalWeight(), "graph #" + t);
            assertEquals(n - 1, k.edgeCount());
            assertEquals(n - 1, p.edgeCount());
            assertEquals(1, k.components());
            assertEquals(1, p.components());
        }
    }

    @Test
    void disconnectedGraphGivesForestNotException() throws IOException {
        Graph g = GraphIO.read(Path.of("data/tiny_disconnected.txt"));
        MstResult k = Kruskal.run(g, new Metrics());
        MstResult p = Prim.run(g, new Metrics());
        assertEquals(12, k.totalWeight());
        assertEquals(12, p.totalWeight());
        assertEquals(3, k.components());
        assertEquals(3, p.components());
        assertEquals(4, k.edgeCount());              // n - components
        assertEquals(4, p.edgeCount());
    }

    @Test
    void knownCityExample() throws IOException {
        Graph g = GraphIO.read(Path.of("data/city_roads.txt"));
        assertEquals(33, Kruskal.run(g, new Metrics()).totalWeight());
        assertEquals(33, Prim.run(g, new Metrics()).totalWeight());
    }

    @Test
    void edgeCases() {
        Graph empty = new Graph(0, false);
        assertEquals(0, Kruskal.run(empty, new Metrics()).totalWeight());
        assertEquals(0, Prim.run(empty, new Metrics()).totalWeight());
        Graph isolated = new Graph(3, false);          // no edges at all
        assertEquals(3, Kruskal.run(isolated, new Metrics()).components());
        assertEquals(3, Prim.run(isolated, new Metrics()).components());
        Graph loops = new Graph(2, false);
        loops.addEdge(0, 0, 1);
        loops.addEdge(0, 1, 5);
        loops.addEdge(0, 1, 3);
        assertEquals(3, Kruskal.run(loops, new Metrics()).totalWeight());
        assertEquals(3, Prim.run(loops, new Metrics()).totalWeight());
    }
}
