from dataset_debug import load_sample
from graph_builder import build_graph
from pathlib import Path

"""
Legacy smoke test from the submitted course project.

This script targets the original dataset interface used during
development and may require minor adjustments if the dataset
implementation changes.
"""

project_root = Path(__file__).resolve().parent
test_root = project_root / "Basic Test"

samples = []

for num in {57, 99, 103, 10, 1}:
    config = test_root / "PASCAR" / f"CONFIG_{num}"
    charge = test_root / "CHARGESSS" / f"CHARGE_{num}"
    samples.append(load_sample(config, charge))

symbols = sorted({
    symbol
    for sample in samples
    for symbol in sample.config.element_symbols
})
symbol_to_idx = {symbol: idx for idx, symbol in enumerate(symbols)}

for sample in samples:
    graph = build_graph(sample, symbol_to_idx)

    print(f"\nSample {sample.sample_id}:")
    print(graph)
    print("nodes:", graph.num_nodes)
    print("edges:", graph.num_edges)
    print("mean degree:", graph.num_edges / graph.num_nodes)
    print("cutoff:", graph.cutoff)
