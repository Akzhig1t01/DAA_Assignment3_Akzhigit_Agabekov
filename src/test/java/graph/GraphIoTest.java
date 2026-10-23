package graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphIoTest {

    @Test
    void readsAllDataFiles() throws IOException {
        Graph d = GraphIO.read(Path.of("data/tiny_disconnected.txt"));
        assertEquals(7, d.n());
        assertEquals(8, d.m());
        assertFalse(d.isDirected());
        Graph c = GraphIO.read(Path.of("data/tiny_cyclic.txt"));
        assertTrue(c.isDirected());
        assertEquals(5, c.m());
        assertEquals(6, GraphIO.read(Path.of("data/tiny_dag.txt")).n());
    }

    @Test
    void undirectedEdgeIsStoredInBothDirections() throws IOException {
        Graph g = GraphIO.parse("3 1 undirected\n0 2 7\n");
        assertEquals(1, g.neighbors(0).size());
        assertEquals(1, g.neighbors(2).size());
        assertEquals(0, g.neighbors(1).size());
        assertEquals(1, g.m());
    }

    @Test
    void directedEdgeTracksInDegreeAndReverse() throws IOException {
        Graph g = GraphIO.parse("3 2 directed\n0 1 1\n0 2 1\n");
        assertEquals(1, g.inDegree(1));
        assertEquals(0, g.inDegree(0));
        Graph r = g.reverse();
        assertEquals(2, r.inDegree(0));
    }

    @Test
    void badInputIsRejected() {
        assertThrows(IOException.class, () -> GraphIO.parse("3 1 sideways\n0 1 1\n"));
        assertThrows(IOException.class, () -> GraphIO.parse("3 2 directed\n0 1 1\n"));
        assertThrows(IllegalArgumentException.class, () -> GraphIO.parse("2 1 directed\n0 5 1\n"));
    }
}
