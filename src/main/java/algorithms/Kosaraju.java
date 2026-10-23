package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.Arrays;
import java.util.List;

/** Kosaraju's SCC with an ITERATIVE DFS (own explicit stack), safe for 100 000+ vertices. */
public final class Kosaraju {
    private Kosaraju() {}

    /** comp[v] = component id of v, count = number of components. */
    public record Result(int[] comp, int count) {
    }

    public static Result run(Graph g, Metrics m) {
        if (!g.isDirected()) throw new IllegalArgumentException("Kosaraju needs a directed graph");
        int n = g.n();

        // Pass 1: finish order on G
        int[] order = new int[n];
        int[] ordLen = {0};
        boolean[] visited = new boolean[n];
        int[] stack = new int[n];       // each vertex is pushed at most once
        int[] it = new int[n];          // next adjacency index per vertex
        for (int s = 0; s < n; s++) {
            if (visited[s]) continue;
            int sp = 0;
            stack[sp++] = s;
            visited[s] = true;
            m.depth(sp);
            while (sp > 0) {
                int v = stack[sp - 1];
                List<Edge> adj = g.neighbors(v);
                if (it[v] < adj.size()) {
                    int w = adj.get(it[v]++).to();
                    m.edgeScans++;
                    if (!visited[w]) {
                        visited[w] = true;
                        stack[sp++] = w;
                        m.depth(sp);
                    }
                } else {
                    sp--;
                    order[ordLen[0]++] = v;     // v is finished
                }
            }
        }

        // Pass 2: reversed graph, decreasing finish order; each tree = one SCC
        Graph rev = g.reverse();
        int[] comp = new int[n];
        Arrays.fill(comp, -1);
        Arrays.fill(it, 0);
        int count = 0;
        for (int i = n - 1; i >= 0; i--) {
            int s = order[i];
            if (comp[s] != -1) continue;
            int sp = 0;
            stack[sp++] = s;
            comp[s] = count;
            m.depth(sp);
            while (sp > 0) {
                int v = stack[sp - 1];
                List<Edge> adj = rev.neighbors(v);
                if (it[v] < adj.size()) {
                    int w = adj.get(it[v]++).to();
                    m.edgeScans++;
                    if (comp[w] == -1) {
                        comp[w] = count;
                        stack[sp++] = w;
                        m.depth(sp);
                    }
                } else {
                    sp--;
                }
            }
            count++;
        }
        return new Result(comp, count);
    }
}
