package benchmark;

import algorithms.DagShortestPath;
import algorithms.Kahn;
import algorithms.Kosaraju;
import algorithms.Kruskal;
import algorithms.MstResult;
import algorithms.Prim;
import graph.Graph;
import metrics.Metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

/**
 * Self-checking benchmark. Usage: Benchmark [outputCsv] [maxN]
 * Fails loudly (IllegalStateException) if any algorithm disagrees with the reference values,
 * if Kruskal and Prim disagree, if an order is invalid or if a scan counter is off.
 */
public class Benchmark {
    static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    static final int[] DENSITIES = {4, 20};   // m = 4n (sparse), m = 20n (dense)
    static final int RUNS = 5;

    // reference values: {MST weight, SCC count, sum of DAG distances}, order = size-major, sparse then dense
    static final long[][] REFERENCE = {
            {147_970, 27, 396_274}, {30_363, 1, 252_411},
            {468_911, 137, 1_271_911}, {97_589, 1, 723_157},
            {1_486_139, 373, 3_941_828}, {309_550, 1, 2_381_342},
            {4_715_515, 1_188, 12_551_891}, {968_517, 1, 7_376_612},
            {15_012_094, 3_923, 40_096_539}, {3_056_681, 1, 23_512_087},
    };

    interface Algo {
        long run(Metrics m);
    }

    record Row(String algorithm, String graph, int n, int m, double timeMs, Metrics metrics, long result) {
    }

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "results/results.csv");
        int maxN = args.length > 1 ? Integer.parseInt(args[1]) : Integer.MAX_VALUE;
        if (out.getParent() != null) Files.createDirectories(out.getParent());

        StringBuilder csv = new StringBuilder(
                "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result\n");
        System.out.printf("%-6s %-9s %9s %-14s %10s %12s %12s%n",
                "n", "m", "", "algorithm", "time_ms", "edge_scans", "result");
        int idx = 0;
        for (int n : SIZES) {
            for (int d : DENSITIES) {
                long[] ref = REFERENCE[idx++];
                if (n > maxN) continue;
                int m = n * d;
                String name = (d == 4 ? "sparse" : "dense") + "_n" + n;

                Row kr, pr, ko, ka, ds;
                {   // each graph lives in its own block so it can be garbage-collected early
                final Graph und = GraphGenerator.undirected(n, m);
                kr = measure("Kruskal", name, und, mm -> Kruskal.run(und, mm).totalWeight());
                pr = measure("Prim", name, und, mm -> Prim.run(und, mm).totalWeight());
                check(kr.result == pr.result, name + ": Kruskal " + kr.result + " != Prim " + pr.result);
                check(kr.result == ref[0], name + ": MST " + kr.result + " != reference " + ref[0]);
                MstResult kres = Kruskal.run(und, new Metrics());
                check(kres.components() == 1 && kres.edgeCount() == n - 1, name + ": MST is not a spanning tree");
                }

                {
                final Graph dir = GraphGenerator.directed(n, m);
                ko = measure("Kosaraju", name, dir, mm -> Kosaraju.run(dir, mm).count());
                check(ko.result == ref[1], name + ": SCC " + ko.result + " != reference " + ref[1]);
                check(ko.metrics.edgeScans == 2L * m, name + ": Kosaraju must scan 2m edges");
                }

                {
                final Graph dag = GraphGenerator.dag(n, m);
                ka = measure("Kahn", name, dag, mm -> Kahn.run(dag, mm).processed());
                check(ka.result == n, name + ": Kahn processed " + ka.result + " of " + n);
                check(Kahn.isValidOrder(dag, Kahn.run(dag, new Metrics()).order()), name + ": invalid topological order");
                check(ka.metrics.edgeScans == m, name + ": Kahn must scan m edges");
                ds = measure("DagShortestPath", name, dag, mm -> DagShortestPath.run(dag, mm).sumOfDistances());
                check(ds.result == ref[2], name + ": DAG sum " + ds.result + " != reference " + ref[2]);
                check(ds.metrics.edgeScans == m, name + ": DAG relaxation must scan m edges");
                }

                for (Row r : new Row[]{kr, pr, ko, ka, ds}) {
                    csv.append(String.format(Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d%n",
                            r.algorithm, r.graph, r.n, r.m, r.timeMs, r.metrics.dsuOps,
                            r.metrics.edgeScans, r.metrics.comparisons, r.metrics.maxDepth, r.result));
                    System.out.printf(Locale.ROOT, "%-6d %-9d %9s %-15s %10.2f %12d %12d%n",
                            r.n, r.m, "", r.algorithm, r.timeMs, r.metrics.edgeScans, r.result);
                }
            }
        }
        Files.writeString(out, csv.toString());
        System.out.println("\nALL CHECKS PASSED. Written: " + out);
    }

    /** Median time of RUNS runs; counters come from the last run (they are identical in every run). */
    static Row measure(String algo, String name, Graph g, Algo a) {
        double[] times = new double[RUNS];
        Metrics last = null;
        long result = 0;
        for (int i = 0; i < RUNS; i++) {
            Metrics m = new Metrics();
            long t0 = System.nanoTime();
            result = a.run(m);
            times[i] = (System.nanoTime() - t0) / 1e6;
            last = m;
        }
        Arrays.sort(times);
        return new Row(algo, name, g.n(), g.m(), times[RUNS / 2], last, result);
    }

    static void check(boolean ok, String msg) {
        if (!ok) throw new IllegalStateException("BENCHMARK CHECK FAILED: " + msg);
    }
}
