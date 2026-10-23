package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

/** Kahn's topological sort: in-degrees + a queue. Reports a cycle instead of a wrong order. */
public final class Kahn {
    private Kahn() {}

    /**
     * @param order     valid topological order, or null if the graph has a cycle
     * @param processed number of vertices that left the queue (== n iff acyclic)
     */
    public record Result(int[] order, int processed, boolean hasCycle) {
    }

    public static Result run(Graph g, Metrics m) {
        if (!g.isDirected()) throw new IllegalArgumentException("Kahn needs a directed graph");
        int n = g.n();
        int[] indeg = new int[n];
        for (int v = 0; v < n; v++) indeg[v] = g.inDegree(v);

        int[] queue = new int[n];
        int head = 0, tail = 0;
        for (int v = 0; v < n; v++) if (indeg[v] == 0) queue[tail++] = v;
        m.depth(tail - head);

        int[] order = new int[n];
        int cnt = 0;
        while (head < tail) {
            int v = queue[head++];
            order[cnt++] = v;
            for (Edge e : g.neighbors(v)) {
                m.edgeScans++;
                if (--indeg[e.to()] == 0) {
                    queue[tail++] = e.to();
                    m.depth(tail - head);
                }
            }
        }
        boolean cycle = cnt < n;
        return new Result(cycle ? null : order, cnt, cycle);
    }

    /** Checks pos[u] < pos[v] for every edge u -> v and that order is a permutation of 0..n-1. */
    public static boolean isValidOrder(Graph g, int[] order) {
        int n = g.n();
        if (order == null || order.length != n) return false;
        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            if (order[i] < 0 || order[i] >= n || pos[order[i]] != -1) return false;
            pos[order[i]] = i;
        }
        for (Edge e : g.edges()) if (pos[e.from()] >= pos[e.to()]) return false;
        return true;
    }
}
