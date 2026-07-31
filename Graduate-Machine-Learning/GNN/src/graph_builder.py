from typing import Dict, Tuple, Union

import torch
from torch_geometric.data import Data
from ase import Atoms

from dataset_debug import build_ase_atoms, ParsedSample


# -------------------------------------------------------------
# Fixed graph construction settings
# -------------------------------------------------------------

FIXED_CUTOFF = 7.0


# -------------------------------------------------------------
# Edge construction
# -------------------------------------------------------------

def build_edges(
    atoms: Atoms,
    cutoff: float = FIXED_CUTOFF,
) -> Tuple[torch.Tensor, torch.Tensor]:
    """
    Build graph edges using a fixed cutoff distance.

    Returns
    -------
    edge_index : torch.Tensor, shape [2, num_edges]
    edge_attr  : torch.Tensor, shape [num_edges, 1]
        Edge distance values.
    """
    dmat = atoms.get_all_distances(mic=True)
    n = len(atoms)

    edge_src = []
    edge_dst = []
    edge_dist = []

    for i in range(n):
        for j in range(n):
            if i == j:
                continue

            d = dmat[i, j]

            if 0.0 < d <= cutoff:
                edge_src.append(i)
                edge_dst.append(j)
                edge_dist.append(d)

    edge_index = torch.tensor([edge_src, edge_dst], dtype=torch.long)
    edge_attr = torch.tensor(edge_dist, dtype=torch.float32).unsqueeze(1)

    return edge_index, edge_attr


# -------------------------------------------------------------
# Node features
# -------------------------------------------------------------

def build_node_features(
    atoms: Atoms,
    symbol_to_idx: Dict[str, int],
) -> torch.Tensor:
    """
    Build one-hot node features from atomic element symbols.

    Parameters
    ----------
    atoms : ASE Atoms
    symbol_to_idx : Dict[str, int]
        Global element vocabulary shared across the full dataset.

    Returns
    -------
    x : torch.Tensor, shape [num_nodes, num_symbols]
    """
    symbols = atoms.get_chemical_symbols()
    num_nodes = len(symbols)
    num_features = len(symbol_to_idx)

    x = torch.zeros((num_nodes, num_features), dtype=torch.float32)

    for i, sym in enumerate(symbols):
        if sym not in symbol_to_idx:
            raise KeyError(f"Element symbol {sym!r} not found in symbol_to_idx")

        x[i, symbol_to_idx[sym]] = 1.0

    return x


# -------------------------------------------------------------
# Targets
# -------------------------------------------------------------

def build_targets(sample: ParsedSample) -> torch.Tensor:
    """
    Build node-level regression targets (atomic charges).

    Returns
    -------
    y : torch.Tensor, shape [num_nodes, 1]
    """
    return torch.tensor(sample.charges, dtype=torch.float32).unsqueeze(1)


# -------------------------------------------------------------
# Graph assembly
# -------------------------------------------------------------

def build_graph(
    sample: ParsedSample,
    symbol_to_idx: Dict[str, int],
    cutoff: float = FIXED_CUTOFF,
) -> Data:
    """
    Build one PyG Data graph from a parsed sample.

    Graph contents:
        x         : one-hot node features
        edge_index: COO edge list
        edge_attr : pairwise distances
        y         : atomic charges
    """
    atoms = build_ase_atoms(sample.config)

    x = build_node_features(atoms, symbol_to_idx)
    edge_index, edge_attr = build_edges(atoms, cutoff=cutoff)
    y = build_targets(sample)

    data = Data(
        x=x,
        edge_index=edge_index,
        edge_attr=edge_attr,
        y=y,
    )

    data.sample_id = sample.sample_id
    data.cutoff = float(cutoff)

    return data