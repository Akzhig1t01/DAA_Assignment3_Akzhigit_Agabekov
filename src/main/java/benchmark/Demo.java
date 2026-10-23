package benchmark;

import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.Prim;
import graph.Graph;
import graph.GraphIO;
import metrics.Metrics;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/** Runs the suitable algorithms on one input file: Demo data/tiny_cyclic.txt */
public class Demo {
    public static void main(String[] args) throws IOException {
        String file = args.length > 0 ? args[0] : "data/tiny_cyclic.txt";
        Graph g = GraphIO.read(Path.of(file));
        System.out.println(file + ": n=" + g.n() + " m=" + g.m() + (g.isDirected() ? " directed" : " undirected"));
        if (!g.isDirected()) {
            Metrics a = new Metrics(), b = new Metrics();
            MstResult k = Kruskal.run(g, a), p = Prim.run(g, b);
            System.out.println("Kruskal: weight=" + k.totalWeight() + " edges=" + k.edgeCount()
                    + " components=" + k.components() + "  [" + a + "]");
            System.out.println("Prim:    weight=" + p.totalWeight() + " edges=" + p.edgeCount()
                    + " components=" + p.components() + "  [" + b + "]");
        } else {
            Metrics m = new Metrics();
            Kosaraju.Result r = Kosaraju.run(g, m);
            System.out.println("Kosaraju: components=" + r.count() + " comp=" + Arrays.toString(r.comp()) + "  [" + m + "]");
            Kahn.Result t = Kahn.run(g, new Metrics());
            if (t.hasCycle()) {
                System.out.println("Kahn: CYCLE detected, only " + t.processed() + " of " + g.n() + " vertices left the queue");
            } else {
                System.out.println("Kahn: order=" + Arrays.toString(t.order()) + " valid=" + Kahn.isValidOrder(g, t.order()));
                DagShortestPath.Result d = DagShortestPath.run(g, new Metrics());
                System.out.println("DAG distances=" + Arrays.toString(d.dist()) + " sum=" + d.sumOfDistances());
                for (int v = 0; v < g.n(); v++)
                    System.out.println("  path to " + v + ": " + DagShortestPath.path(d, v));
            }
        }
    }
}
