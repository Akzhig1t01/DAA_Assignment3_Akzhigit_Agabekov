package graph;

/** A weighted edge from -> to. For undirected graphs the adjacency list stores both directions. */
public record Edge(int from, int to, int weight) {
}
