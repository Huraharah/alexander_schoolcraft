from collections import Counter
from pathlib import Path
from typing import Iterable, Union

import numpy as np
from ase import Atoms
from dataset_debug import load_sample, build_ase_atoms

def get_upper_triangle_distances(atoms: Atoms) -> np.ndarray:
    """
    Returns all unique pairwise distances (i < j), with PBC and MIC applied.
    """
    dmat = atoms.get_all_distances(mic=True)
    iu = np.triu_indices(len(atoms), k=1)
    distances = dmat[iu]
    return distances


def summarize_smallest_distances(
    atoms: Atoms,
    top_n: int = 15,
    round_decimals: int = 6,
) -> None:
    """
    Print the smallest distinct nonzero distances and how often they appear.
    """
    distances = get_upper_triangle_distances(atoms)

    # Remove exact zeroes just in case
    distances = distances[distances > 1e-12]

    rounded = np.round(distances, round_decimals)
    counts = Counter(rounded)

    distinct_sorted = sorted(counts.items(), key=lambda x: x[0])

    print("\n" + "=" * 72)
    print("Smallest distinct nonzero pair distances")
    print("=" * 72)
    print(f"{'distance':>12s}  {'count':>10s}")
    print("-" * 26)

    for dist, count in distinct_sorted[:top_n]:
        print(f"{dist:12.6f}  {count:10d}")


def degree_stats_for_cutoff(atoms: Atoms, cutoff: float) -> dict:
    """
    Build an undirected adjacency from a scalar cutoff and return degree stats.
    """
    dmat = atoms.get_all_distances(mic=True)
    n = len(atoms)

    # Adjacency: connect i,j if 0 < d <= cutoff
    adjacency = (dmat <= cutoff) & (dmat > 1e-12)

    degrees = adjacency.sum(axis=1)

    return {
        "cutoff": cutoff,
        "min_degree": int(degrees.min()),
        "max_degree": int(degrees.max()),
        "mean_degree": float(degrees.mean()),
        "median_degree": float(np.median(degrees)),
        "num_isolated": int((degrees == 0).sum()),
    }


def probe_candidate_cutoffs(atoms: Atoms, cutoffs: Iterable[float]) -> None:
    """
    Print degree statistics for several candidate cutoffs.
    """
    print("\n" + "=" * 72)
    print("Neighbor-count statistics by cutoff")
    print("=" * 72)
    print(
        f"{'cutoff':>8s}  {'min':>6s}  {'max':>6s}  "
        f"{'mean':>8s}  {'median':>8s}  {'isolated':>9s}"
    )
    print("-" * 58)

    for cutoff in cutoffs:
        stats = degree_stats_for_cutoff(atoms, cutoff)
        print(
            f"{stats['cutoff']:8.3f}  "
            f"{stats['min_degree']:6d}  "
            f"{stats['max_degree']:6d}  "
            f"{stats['mean_degree']:8.3f}  "
            f"{stats['median_degree']:8.3f}  "
            f"{stats['num_isolated']:9d}"
        )


def nearest_neighbor_profile(atoms: Atoms) -> None:
    """
    For each atom, find its nearest non-self neighbor distance.
    Useful for seeing the first-shell distance directly.
    """
    dmat = atoms.get_all_distances(mic=True)
    n = len(atoms)

    # Mask self-distances
    dmat = dmat.copy()
    np.fill_diagonal(dmat, np.inf)

    nearest = dmat.min(axis=1)

    print("\n" + "=" * 72)
    print("Nearest-neighbor distance profile")
    print("=" * 72)
    print(f"min    : {nearest.min():.6f}")
    print(f"max    : {nearest.max():.6f}")
    print(f"mean   : {nearest.mean():.6f}")
    print(f"median : {np.median(nearest):.6f}")

    rounded = np.round(nearest, 6)
    counts = Counter(rounded)
    print("\nNearest-neighbor distances (rounded) and counts:")
    for dist, count in sorted(counts.items(), key=lambda x: x[0])[:15]:
        print(f"  {dist:.6f} : {count}")


def main() -> None:
    project_root = Path(__file__).resolve().parent
    test_root = project_root / "Basic Test"

    for num in {57, 99, 103, 10, 1}:
        config_path = test_root / "PASCAR" / f"CONFIG_{num}"
        charge_path = test_root / "CHARGESSS" / f"CHARGE_{num}"

        sample = load_sample(config_path, charge_path)
        atoms = build_ase_atoms(sample.config)

        print(f"Loaded sample {sample.sample_id} with {len(atoms)} atoms")
        print(f"Cell:\n{atoms.cell}")

        summarize_smallest_distances(atoms, top_n=20, round_decimals=6)
        nearest_neighbor_profile(atoms)

        # Start broad, then refine after you see the output
        candidate_cutoffs = [2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0]
        probe_candidate_cutoffs(atoms, candidate_cutoffs)


if __name__ == "__main__":
    main()
