package algorithms;

import metrics.Metrics;

/**
 * Disjoint Set Union with union by rank AND path compression
 * (amortised O(alpha(n)) per operation).
 */
public class DSU {
    private int[] parent;
    private int[] rank;
    private int components;
    private final Metrics metrics;

    public long findCalls;
    public long unionCalls; // successful unions only

    public DSU(int n, Metrics metrics) {
        this.metrics = metrics == null ? new Metrics() : metrics;
        makeSet(n);
    }

    public DSU(int n) {
        this(n, null);
    }

    /** (Re)initialises n singleton sets. */
    public void makeSet(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        components = n;
    }

    public int find(int x) {
        findCalls++;
        metrics.dsuOps++;
        int root = x;
        while (parent[root] != root) root = parent[root];
        while (parent[x] != root) {          // path compression (iterative)
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    /** @return true if x and y were in different sets and have been merged. */
    public boolean union(int x, int y) {
        int rx = find(x), ry = find(y);
        if (rx == ry) return false;
        if (rank[rx] < rank[ry]) {
            parent[rx] = ry;
        } else if (rank[rx] > rank[ry]) {
            parent[ry] = rx;
        } else {
            parent[ry] = rx;
            rank[rx]++;
        }
        components--;
        unionCalls++;
        metrics.dsuOps++;
        return true;
    }

    public int components() {
        return components;
    }
}
