package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.PriorityQueue;

public final class Prim {
    private Prim() {}

    /**
     * Lazy Prim with a PriorityQueue. Starts at vertex 0; when the queue runs dry and
     * unvisited vertices remain (disconnected graph) it starts a new tree there,
     * so the result is a minimum spanning forest.
     * Queue entry = weight << 32 | vertex, ordered by weight (then vertex).
     */
    public static MstResult run(Graph g, Metrics m) {
        int n = g.n();
        boolean[] inTree = new boolean[n];
        PriorityQueue<Long> pq = new PriorityQueue<>((a, b) -> {
            m.comparisons++;
            return Long.compare(a, b);
        });
        long total = 0;
        int taken = 0, components = 0;

        for (int start = 0; start < n; start++) {
            if (inTree[start]) continue;
            components++;
            inTree[start] = true;
            scan(g, start, inTree, pq, m);
            while (!pq.isEmpty()) {
                long top = pq.poll();
                int v = (int) (top & 0xFFFFFFFFL);
                int w = (int) (top >>> 32);
                if (inTree[v]) continue;        // already in the tree
                inTree[v] = true;
                total += w;
                taken++;
                scan(g, v, inTree, pq, m);
            }
        }
        return new MstResult(total, taken, components);
    }

    private static void scan(Graph g, int v, boolean[] inTree, PriorityQueue<Long> pq, Metrics m) {
        for (Edge e : g.neighbors(v)) {
            m.edgeScans++;
            if (!inTree[e.to()]) pq.add(((long) e.weight() << 32) | e.to());
        }
    }
}
