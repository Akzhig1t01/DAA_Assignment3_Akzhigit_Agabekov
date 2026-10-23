package metrics;

/**
 * Operation counters. They are incremented INSIDE the algorithms, so the numbers
 * are reproducible on any machine (unlike wall-clock time).
 */
public class Metrics {
    /** find calls + successful union calls (DSU). */
    public long dsuOps;
    /** Edges looked at (adjacency entries or edge-list entries). */
    public long edgeScans;
    /** Weights / keys compared (sorting, priority queue, relaxations). */
    public long comparisons;
    /** Deepest the explicit stack (Kosaraju) or the queue (Kahn) ever got. */
    public long maxDepth;

    public void depth(long d) {
        if (d > maxDepth) maxDepth = d;
    }

    public void reset() {
        dsuOps = edgeScans = comparisons = maxDepth = 0;
    }

    @Override
    public String toString() {
        return "dsuOps=" + dsuOps + " edgeScans=" + edgeScans
                + " comparisons=" + comparisons + " maxDepth=" + maxDepth;
    }
}
