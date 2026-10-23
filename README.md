# DAA Assignment 3 - Graphs: DSU, MST, SCC and Shortest Paths

City Network & Job Scheduler. Own graph class (adjacency list) and six algorithms written from scratch:
**DSU, Kruskal, Prim, Kosaraju, Kahn, shortest paths in a DAG**, with operation counters inside every algorithm.

## Layout
```
src/main/java/graph        Graph, Edge, GraphIO (file input)
src/main/java/algorithms   DSU, Kruskal, Prim, Kosaraju, Kahn, DagShortestPath
src/main/java/metrics      Metrics (dsuOps, edgeScans, comparisons, maxDepth)
src/main/java/benchmark    GraphGenerator (seed 42), Benchmark (self-checking), Demo
src/test/java              JUnit 5 tests
data/                      input graphs (disconnected, cyclic, DAG with negative weights, city roads)
results/results.csv        50 rows (5 algorithms x 10 graphs)
results/plots/             3 PNG plots
scripts/plots.py           plots + ratio/slope tables from results.csv
REPORT.md                  report
```

## Requirements
JDK 17+ and Maven 3.8+ (or just IntelliJ IDEA: *File > Open > pom.xml*, "Open as Project"). Python 3 with matplotlib and numpy only for `scripts/plots.py`.

## Build and test (one command)
```
mvn test
```
or in IntelliJ: right-click `src/test/java` > *Run 'All Tests'*.
Tests use relative paths (`data/...`), so the working directory must be the project root (this is the default in IntelliJ and Maven).

## Run the benchmark
```
mvn -q compile exec:java
```
It writes `results/results.csv` and **fails loudly** if Kruskal and Prim disagree, if a topological order is invalid,
if a scan counter is off (Kosaraju 2m, Kahn m, DAG m) or if any of the 10 reference values (MST weight, SCC count, sum of DAG distances) differs.
The last line is `ALL CHECKS PASSED`.

For the 100 000 / 2 000 000 graphs give the JVM more heap (about 2.5 GB is enough):
- IntelliJ: *Run > Edit Configurations > Benchmark > VM options*: `-Xmx3g`
- Maven: `MAVEN_OPTS="-Xmx3g" mvn -q compile exec:java`
- or: `mvn -q compile && java -Xmx3g -cp target/classes benchmark.Benchmark`

Quick run for debugging (only graphs up to n = 10 000): `java -cp target/classes benchmark.Benchmark results/test.csv 10000`

## Plots and tables
```
python3 scripts/plots.py
```
writes `results/plots/*.png`, `results/slopes.md`, `results/ratios.md`.

## Try the algorithms on one file
`java -cp target/classes benchmark.Demo data/tiny_cyclic.txt` (in IntelliJ: run `Demo` with program argument `data/tiny_cyclic.txt`).

## Input format
```
n m directed|undirected
u v w        (m lines, vertices 0..n-1)
```
Blank lines and lines starting with `#` are ignored.

## AI use acknowledgement
This project was prepared with the help of an AI assistant (Claude, Anthropic): it produced the first version of the code, tests, scripts and report text.
I ran the program and the tests, checked the reference values, and I can explain every algorithm and counter. (Edit this paragraph so that it states exactly what you did yourself.)
