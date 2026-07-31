from dataclasses import dataclass
from pathlib import Path
from typing import List, Dict, Any, Union

import json
import numpy as np
import torch
from torch_geometric.data import InMemoryDataset, Data

from dataset_debug import load_sample, parse_config_file, extract_numeric_suffix
from graph_builder import build_graph


@dataclass
class DatasetStats:
    num_graphs: int
    min_nodes: int
    max_nodes: int
    mean_nodes: float
    min_edges: int
    max_edges: int
    mean_edges: float
    min_cutoff: float
    max_cutoff: float
    mean_cutoff: float


class AtomicChargeDataset(InMemoryDataset):
    """
    PyG in-memory dataset for matched CONFIG_n / CHARGE_n pairs.

    Raw directory layout:
        root/
            PASCAR/
                CONFIG_1
                CONFIG_2
                ...
            CHARGESSS/
                CHARGE_1
                CHARGE_2
                ...
    """

    def __init__(
        self,
        raw_root: Union[str, Path],
        processed_root: Union[str, Path],
        transform=None,
        pre_transform=None,
        verbose: bool = True,
    ) -> None:
        self.raw_root = Path(raw_root)
        self.config_dir = self.raw_root / "PASCAR"
        self.charge_dir = self.raw_root / "CHARGESSS"
        self.verbose = verbose

        self.symbol_to_idx = {}
        self.sample_ids = []
        self.dim_node_features = 0

        super().__init__(str(processed_root), transform, pre_transform)

        payload = torch.load(self.processed_paths[0], weights_only=False)
        self.data = payload["data"]
        self.slices = payload["slices"]
        self.symbol_to_idx = payload["symbol_to_idx"]
        self.sample_ids = payload["sample_ids"]
        self.dim_node_features = payload["num_node_features"]

    @property
    def raw_file_names(self) -> List[str]:
        # We do not rely on PyG raw download handling here because the data
        # already exists on the remote server in-place.
        return []

    @property
    def processed_file_names(self) -> List[str]:
        return ["atomic_charge_dataset.pt"]

    def download(self) -> None:
        # No download step; files already live on the server.
        return

    def _validate_directories(self) -> None:
        if not self.raw_root.exists():
            raise FileNotFoundError(f"Dataset root not found: {self.raw_root}")

        if not self.config_dir.exists():
            raise FileNotFoundError(f"PASCAR directory not found: {self.config_dir}")

        if not self.charge_dir.exists():
            raise FileNotFoundError(f"CHARGESSS directory not found: {self.charge_dir}")

    def _discover_config_files(self) -> List[Path]:
        config_files = sorted(
            self.config_dir.glob("CONFIG_*"),
            key=extract_numeric_suffix
        )

        if not config_files:
            raise ValueError(f"No CONFIG_* files found in {self.config_dir}")

        return config_files

    def _build_symbol_vocab(self, config_files: List[Path]) -> Dict[str, int]:
        symbols = set()

        for config_path in config_files:
            parsed = parse_config_file(config_path)
            symbols.update(parsed.element_symbols)

        sorted_symbols = sorted(symbols)
        return {sym: idx for idx, sym in enumerate(sorted_symbols)}

    def process(self) -> None:
        self._validate_directories()

        config_files = self._discover_config_files()
        symbol_to_idx = self._build_symbol_vocab(config_files)

        graphs: List[Data] = []
        sample_ids: List[int] = []

        for config_path in config_files:
            sample_id = extract_numeric_suffix(config_path)
            charge_path = self.charge_dir / f"CHARGE_{sample_id}"

            if not charge_path.exists():
                raise FileNotFoundError(
                    f"Missing matching charge file for sample {sample_id}: {charge_path}"
                )

            sample = load_sample(config_path, charge_path)
            graph = build_graph(sample, symbol_to_idx)

            graphs.append(graph)
            sample_ids.append(sample_id)

            if self.verbose:
                mean_degree = graph.num_edges / graph.num_nodes
                print(
                    f"Processed sample {sample_id:>4d} | "
                    f"nodes={graph.num_nodes:>6d} | "
                    f"edges={graph.num_edges:>7d} | "
                    f"mean_degree={mean_degree:>6.2f} | "
                    f"cutoff={float(graph.cutoff):.2f}"
                )

        data, slices = self.collate(graphs)

        payload = {
            "data": data,
            "slices": slices,
            "symbol_to_idx": symbol_to_idx,
            "sample_ids": sample_ids,
            "num_node_features": len(symbol_to_idx),
        }

        self.processed_dir and Path(self.processed_dir).mkdir(parents=True, exist_ok=True)
        torch.save(payload, self.processed_paths[0])

    def get_sample_ids(self) -> List[int]:
        return list(self.sample_ids)

    def get_stats(self) -> DatasetStats:
        if len(self) == 0:
            raise ValueError("Dataset is empty")

        graphs = [self.get(i) for i in range(len(self))]

        node_counts = np.array([g.num_nodes for g in graphs], dtype=np.int64)
        edge_counts = np.array([g.num_edges for g in graphs], dtype=np.int64)
        cutoffs = np.array([float(g.cutoff) for g in graphs], dtype=np.float64)

        return DatasetStats(
            num_graphs=len(graphs),
            min_nodes=int(node_counts.min()),
            max_nodes=int(node_counts.max()),
            mean_nodes=float(node_counts.mean()),
            min_edges=int(edge_counts.min()),
            max_edges=int(edge_counts.max()),
            mean_edges=float(edge_counts.mean()),
            min_cutoff=float(cutoffs.min()),
            max_cutoff=float(cutoffs.max()),
            mean_cutoff=float(cutoffs.mean()),
        )

    def summary(self) -> None:
        stats = self.get_stats()

        print("\n" + "=" * 72)
        print("Dataset Summary")
        print("=" * 72)
        print(f"Number of graphs   : {stats.num_graphs}")
        print(f"Node count         : min={stats.min_nodes}, max={stats.max_nodes}, mean={stats.mean_nodes:.2f}")
        print(f"Edge count         : min={stats.min_edges}, max={stats.max_edges}, mean={stats.mean_edges:.2f}")
        print(f"Cutoff             : min={stats.min_cutoff:.2f}, max={stats.max_cutoff:.2f}, mean={stats.mean_cutoff:.2f}")
        print(f"Num node features  : {self.dim_node_features}")
        print(f"Element vocabulary : {self.symbol_to_idx}")

    def as_list(self) -> List[Data]:
        return [self.get(i) for i in range(len(self))]

    def get_metadata(self) -> List[Dict[str, Any]]:
        metadata: List[Dict[str, Any]] = []

        for i in range(len(self)):
            g = self.get(i)
            metadata.append(
                {
                    "sample_id": int(g.sample_id),
                    "num_nodes": int(g.num_nodes),
                    "num_edges": int(g.num_edges),
                    "mean_degree": float(g.num_edges / g.num_nodes),
                    "cutoff": float(g.cutoff),
                    "x_shape": tuple(g.x.shape),
                    "y_shape": tuple(g.y.shape),
                    "edge_attr_shape": tuple(g.edge_attr.shape),
                }
            )

        return metadata
