package benchmark;

import graph.Graph;

import java.util.Random;

/** Exact generator from the assignment: always new Random(42). */
public final class GraphGenerator {
    private GraphGenerator() {}

    public static final long SEED = 42;

    private static int[] shuffled(int n, Random rnd) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = p[i]; p[i] = p[j]; p[j] = t;
        }
        return p;
    }

    private static int weight(Random rnd) {
        return 1 + rnd.nextInt(1000);
    }

    /** Connected: random spanning tree (n-1 edges) + (m-n+1) random edges. */
    public static Graph undirected(int n, int m) {
        Random rnd = new Random(SEED);
        int[] perm = shuffled(n, rnd);
        Graph g = new Graph(n, false);
        for (int i = 1; i < n; i++) {
            int u = perm[rnd.nextInt(i)];
            g.addEdge(u, perm[i], weight(rnd));
        }
        for (int k = n - 1; k < m; k++) {
            int a = rnd.nextInt(n);
            int b;
            do { b = rnd.nextInt(n); } while (b == a);
            g.addEdge(a, b, weight(rnd));
        }
        return g;
    }

    /** Only random edges, no spanning tree. */
    public static Graph directed(int n, int m) {
        Random rnd = new Random(SEED);
        Graph g = new Graph(n, true);
        for (int k = 0; k < m; k++) {
            int a = rnd.nextInt(n);
            int b;
            do { b = rnd.nextInt(n); } while (b == a);
            g.addEdge(a, b, weight(rnd));
        }
        return g;
    }

    /** Random ranks; every random edge is oriented from the lower to the higher rank. */
    public static Graph dag(int n, int m) {
        Random rnd = new Random(SEED);
        int[] rank = shuffled(n, rnd);
        Graph g = new Graph(n, true);
        for (int k = 0; k < m; k++) {
            int a = rnd.nextInt(n);
            int b;
            do { b = rnd.nextInt(n); } while (b == a);
            if (rank[a] > rank[b]) { int t = a; a = b; b = t; }
            g.addEdge(a, b, weight(rnd));
        }
        return g;
    }
}
