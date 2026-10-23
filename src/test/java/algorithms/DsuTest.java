package algorithms;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DsuTest {

    /** Naive reference: label array, merge = relabel everything. */
    static class Naive {
        final int[] label;
        Naive(int n) { label = new int[n]; for (int i = 0; i < n; i++) label[i] = i; }
        void union(int a, int b) {
            int la = label[a], lb = label[b];
            if (la == lb) return;
            for (int i = 0; i < label.length; i++) if (label[i] == lb) label[i] = la;
        }
        boolean same(int a, int b) { return label[a] == label[b]; }
        int sets() { return (int) java.util.Arrays.stream(label).distinct().count(); }
    }

    @Test
    void agreesWithReferenceAfterRandomUnions() {
        Random rnd = new Random(1);
        for (int round = 0; round < 50; round++) {
            int n = 1 + rnd.nextInt(100);
            DSU dsu = new DSU(n);
            Naive ref = new Naive(n);
            int ops = rnd.nextInt(3 * n);
            for (int i = 0; i < ops; i++) {
                int a = rnd.nextInt(n), b = rnd.nextInt(n);
                dsu.union(a, b);
                ref.union(a, b);
            }
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    assertEquals(ref.same(i, j), dsu.find(i) == dsu.find(j));
            assertEquals(ref.sets(), dsu.components());
        }
    }

    @Test
    void makeSetResets() {
        DSU dsu = new DSU(5);
        dsu.union(0, 1);
        dsu.union(2, 3);
        assertEquals(3, dsu.components());
        dsu.makeSet(8);
        assertEquals(8, dsu.components());
    }

    @Test
    void countersCountFindsAndSuccessfulUnions() {
        DSU dsu = new DSU(4);
        assertTrue(dsu.union(0, 1));
        assertTrue(!dsu.union(1, 0));            // already together: not a successful union
        assertEquals(1, dsu.unionCalls);
        assertEquals(4, dsu.findCalls);          // two finds inside each union call
    }

    @Test
    void longChainDoesNotDegrade() {
        int n = 200_000;
        DSU dsu = new DSU(n);
        for (int i = 1; i < n; i++) dsu.union(i - 1, i);
        assertEquals(1, dsu.components());
        assertEquals(dsu.find(0), dsu.find(n - 1));
    }
}
