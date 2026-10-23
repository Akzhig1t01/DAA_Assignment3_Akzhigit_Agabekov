| counter / formula | density | n=1000 | n=3162 | n=10000 | n=31623 | n=100000 |
|---|---|---|---|---|---|---|
| Kruskal dsuOps / E | sparse | 2.290 | 2.519 | 2.682 | 2.732 | 2.748 |
| Kruskal dsuOps / edgeScans | sparse | 2.972 | 2.848 | 2.776 | 2.757 | 2.751 |
| Kruskal comparisons / (E log2 E) | sparse | 0.891 | 0.907 | 0.869 | 0.809 | 0.745 |
| Prim edgeScans / (V+E) | sparse | 1.600 | 1.600 | 1.600 | 1.600 | 1.600 |
| Prim comparisons / (E log2 E) | sparse | 1.830 | 1.867 | 1.900 | 1.929 | 1.946 |
| Kosaraju edgeScans / (V+E) | sparse | 1.600 | 1.600 | 1.600 | 1.600 | 1.600 |
| Kahn edgeScans / (V+E) | sparse | 0.800 | 0.800 | 0.800 | 0.800 | 0.800 |
| DagShortestPath edgeScans / (V+E) | sparse | 0.800 | 0.800 | 0.800 | 0.800 | 0.800 |
| Kruskal dsuOps / E | dense | 0.580 | 0.606 | 0.565 | 0.754 | 0.894 |
| Kruskal dsuOps / edgeScans | dense | 2.697 | 2.657 | 2.723 | 2.497 | 2.403 |
| Kruskal comparisons / (E log2 E) | dense | 0.903 | 0.846 | 0.784 | 0.720 | 0.666 |
| Prim edgeScans / (V+E) | dense | 1.905 | 1.905 | 1.905 | 1.905 | 1.905 |
| Prim comparisons / (E log2 E) | dense | 1.826 | 1.849 | 1.868 | 1.880 | 1.892 |
| Kosaraju edgeScans / (V+E) | dense | 1.905 | 1.905 | 1.905 | 1.905 | 1.905 |
| Kahn edgeScans / (V+E) | dense | 0.952 | 0.952 | 0.952 | 0.952 | 0.952 |
| DagShortestPath edgeScans / (V+E) | dense | 0.952 | 0.952 | 0.952 | 0.952 | 0.952 |
