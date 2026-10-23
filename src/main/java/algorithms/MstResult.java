package algorithms;

/** Minimum spanning tree (or forest when the graph is disconnected). */
public record MstResult(long totalWeight, int edgeCount, int components) {
}
