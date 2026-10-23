package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Shortest paths in a DAG in O(V + E): relax outgoing edges in topological order, one pass.
 * Every vertex with in-degree 0 starts at distance 0 (virtual super-source), so the result
 * does not depend on a chosen source. Negative weights are fine (see REPORT.md).
 */
public final class DagShortestPath {
    private DagShortestPath() {}

    public static final long INF = Long.MAX_VALUE / 4;

    public record Result(long[] dist, int[] parent, long sumOfDistances) {
    }

    public static Result run(Graph g, Metrics m) {
        // the topological order is computed with a separate Metrics: this class only counts the relaxation pass
        Kahn.Result topo = Kahn.run(g, new Metrics());
        if (topo.hasCycle()) throw new IllegalArgumentException("graph has a cycle: not a DAG");
        int n = g.n();
        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        for (int v = 0; v < n; v++) if (g.inDegree(v) == 0) dist[v] = 0;   // super-source

        for (int v : topo.order()) {
            for (Edge e : g.neighbors(v)) {
                m.edgeScans++;
                m.comparisons++;
                long nd = dist[v] + e.weight();
                if (nd < dist[e.to()]) {
                    dist[e.to()] = nd;
                    parent[e.to()] = v;
                }
            }
        }
        long sum = 0;
        for (long d : dist) sum += d;      // every vertex is reachable from some source
        return new Result(dist, parent, sum);
    }

    /** Path from the (virtual) source to target, following parent[]. */
    public static List<Integer> path(Result r, int target) {
        List<Integer> p = new ArrayList<>();
        for (int v = target; v != -1; v = r.parent()[v]) p.add(v);
        Collections.reverse(p);
        return p;
    }
}
