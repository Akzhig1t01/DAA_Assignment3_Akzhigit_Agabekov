package graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Adjacency-list graph, vertices 0..n-1, int weights.
 * One class for directed and undirected graphs.
 * Self-loops, parallel edges, isolated vertices are allowed.
 */
public class Graph {
    private final int n;
    private final boolean directed;
    private final List<Edge>[] adj;
    private final List<Edge> edges = new ArrayList<>();
    private final int[] inDeg; // only meaningful for directed graphs

    @SuppressWarnings("unchecked")
    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n < 0");
        this.n = n;
        this.directed = directed;
        this.adj = new List[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();
        this.inDeg = new int[n];
    }

    public void addEdge(int u, int v, int w) {
        if (u < 0 || u >= n || v < 0 || v >= n)
            throw new IllegalArgumentException("vertex out of range: " + u + " " + v);
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj[u].add(e);
        if (directed) {
            inDeg[v]++;
        } else if (u != v) {            // a self-loop is stored once
            adj[v].add(new Edge(v, u, w));
        }
    }

    public int n() { return n; }

    /** Number of edges as given in the input (an undirected edge counts once). */
    public int m() { return edges.size(); }

    public boolean isDirected() { return directed; }

    public List<Edge> neighbors(int v) { return adj[v]; }

    /** Edge list, one entry per input edge. */
    public List<Edge> edges() { return edges; }

    public int inDegree(int v) { return inDeg[v]; }

    /** Graph with every edge reversed (directed graphs only). */
    public Graph reverse() {
        if (!directed) throw new IllegalStateException("reverse() needs a directed graph");
        Graph r = new Graph(n, true);
        for (Edge e : edges) r.addEdge(e.to(), e.from(), e.weight());
        return r;
    }
}
