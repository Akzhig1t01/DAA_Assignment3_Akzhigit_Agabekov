#!/usr/bin/env python3
"""Reads results/results.csv, writes 3 PNG plots to results/plots/ plus results/ratios.md and results/slopes.md."""
import csv, math, os, sys
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

root = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
rows = list(csv.DictReader(open(os.path.join(root, "results", "results.csv"))))
for r in rows:
    for k in ("n", "m", "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"):
        r[k] = int(r[k])
    r["time_ms"] = float(r["time_ms"])
    r["density"] = "sparse" if r["graph"].startswith("sparse") else "dense"
    r["ops"] = r["dsu_ops"] + r["edge_scans"] + r["comparisons"]

ALGOS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COLORS = dict(zip(ALGOS, ["tab:blue", "tab:orange", "tab:green", "tab:red", "tab:purple"]))
STYLE = {"sparse": "-", "dense": "--"}
MARK = {"sparse": "o", "dense": "s"}

def series(algo, dens):
    s = sorted((r for r in rows if r["algorithm"] == algo and r["density"] == dens), key=lambda r: r["n"])
    return s

outdir = os.path.join(root, "results", "plots")
os.makedirs(outdir, exist_ok=True)

# Plot 1: operations vs n (log-log)
fig, ax = plt.subplots(figsize=(8, 6))
for a in ALGOS:
    for d in ("sparse", "dense"):
        s = series(a, d)
        ax.plot([r["n"] for r in s], [r["ops"] for r in s], STYLE[d], marker=MARK[d], color=COLORS[a], label=f"{a} ({d})")
ax.set_xscale("log"); ax.set_yscale("log")
ax.set_xlabel("n (vertices)"); ax.set_ylabel("operations = dsuOps + edgeScans + comparisons")
ax.set_title("Operations vs n (log-log)"); ax.grid(True, which="both", alpha=0.3); ax.legend(fontsize=8, ncol=2)
fig.tight_layout(); fig.savefig(os.path.join(outdir, "1_operations_vs_n.png"), dpi=150); plt.close(fig)

# Plot 2: scans per edge
fig, ax = plt.subplots(figsize=(8, 6))
for a in ALGOS:
    for d in ("sparse", "dense"):
        s = series(a, d)
        ax.plot([r["n"] for r in s], [r["edge_scans"] / r["m"] for r in s], STYLE[d], marker=MARK[d], color=COLORS[a], label=f"{a} ({d})")
ax.set_xscale("log"); ax.set_xlabel("n (vertices)"); ax.set_ylabel("edgeScans / m")
ax.set_title("Scans per edge: flat line = linear work"); ax.grid(True, which="both", alpha=0.3); ax.legend(fontsize=8, ncol=2)
fig.tight_layout(); fig.savefig(os.path.join(outdir, "2_scans_per_edge.png"), dpi=150); plt.close(fig)

# Plot 3: share of edges Kruskal examines
fig, ax = plt.subplots(figsize=(8, 5))
for d in ("sparse", "dense"):
    s = series("Kruskal", d)
    ax.plot([r["n"] for r in s], [r["edge_scans"] / r["m"] for r in s], STYLE[d], marker=MARK[d], label=f"Kruskal ({d}, m = {4 if d == 'sparse' else 20}n)")
ax.set_xscale("log"); ax.set_ylim(0, 1.05)
ax.set_xlabel("n (vertices)"); ax.set_ylabel("edgeScans / m (share of edges examined)")
ax.set_title("Share of edges Kruskal examines before the tree is complete"); ax.grid(True, which="both", alpha=0.3); ax.legend()
fig.tight_layout(); fig.savefig(os.path.join(outdir, "3_kruskal_edge_share.png"), dpi=150); plt.close(fig)

# slopes (log-log least squares) of edgeScans and of total ops against n
def slope(xs, ys):
    return float(np.polyfit(np.log10(xs), np.log10(ys), 1)[0])

with open(os.path.join(root, "results", "slopes.md"), "w") as f:
    f.write("| algorithm | density | slope of edgeScans vs n | slope of total ops vs n |\n|---|---|---|---|\n")
    for a in ALGOS:
        for d in ("sparse", "dense"):
            s = series(a, d)
            n = [r["n"] for r in s]
            f.write(f"| {a} | {d} | {slope(n, [r['edge_scans'] for r in s]):.3f} | {slope(n, [r['ops'] for r in s]):.3f} |\n")

# counter / formula tables
ns = sorted({r["n"] for r in rows})
def ratio_row(label, algo, dens, fn):
    s = series(algo, dens)
    return f"| {label} | {dens} | " + " | ".join(f"{fn(r):.3f}" for r in s) + " |\n"

with open(os.path.join(root, "results", "ratios.md"), "w") as f:
    f.write("| counter / formula | density | " + " | ".join(f"n={n}" for n in ns) + " |\n")
    f.write("|---|---|" + "---|" * len(ns) + "\n")
    for d in ("sparse", "dense"):
        f.write(ratio_row("Kruskal dsuOps / E", "Kruskal", d, lambda r: r["dsu_ops"] / r["m"]))
        f.write(ratio_row("Kruskal dsuOps / edgeScans", "Kruskal", d, lambda r: r["dsu_ops"] / r["edge_scans"]))
        f.write(ratio_row("Kruskal comparisons / (E log2 E)", "Kruskal", d, lambda r: r["comparisons"] / (r["m"] * math.log2(r["m"]))))
        f.write(ratio_row("Prim edgeScans / (V+E)", "Prim", d, lambda r: r["edge_scans"] / (r["n"] + r["m"])))
        f.write(ratio_row("Prim comparisons / (E log2 E)", "Prim", d, lambda r: r["comparisons"] / (r["m"] * math.log2(r["m"]))))
        f.write(ratio_row("Kosaraju edgeScans / (V+E)", "Kosaraju", d, lambda r: r["edge_scans"] / (r["n"] + r["m"])))
        f.write(ratio_row("Kahn edgeScans / (V+E)", "Kahn", d, lambda r: r["edge_scans"] / (r["n"] + r["m"])))
        f.write(ratio_row("DagShortestPath edgeScans / (V+E)", "DagShortestPath", d, lambda r: r["edge_scans"] / (r["n"] + r["m"])))
print("plots, slopes.md and ratios.md written")
