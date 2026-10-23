package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;

public final class Kruskal {
    private Kruskal() {}

    /** Minimum spanning forest of an undirected graph. Never throws on disconnected input. */
    public static MstResult run(Graph g, Metrics m) {
        int n = g.n();
        Edge[] es = g.edges().toArray(new Edge[0]);
        Arrays.sort(es, (a, b) -> {
            m.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        DSU dsu = new DSU(n, m);
        long total = 0;
        int taken = 0;
        for (Edge e : es) {
            if (taken == n - 1) break;           // tree is complete, the rest cannot help
            m.edgeScans++;
            if (dsu.find(e.from()) != dsu.find(e.to())) {
                dsu.union(e.from(), e.to());
                total += e.weight();
                taken++;
            }
        }
        return new MstResult(total, taken, dsu.components());
    }
}
