from dataclasses import dataclass
from pathlib import Path
from typing import List, Tuple, Union

import numpy as np
from ase import Atoms


# -----------------------------
# Data containers
# -----------------------------

@dataclass
class ParsedConfig:
    sample_id: int
    comment: str
    scale: float
    lattice: np.ndarray                  # shape: (3, 3)
    element_symbols: List[str]
    element_counts: List[int]
    selective_dynamics: bool
    coord_type: str                      # "Direct" or "Cartesian"
    atom_types: List[str]                # expanded list, one per atom
    coordinates: np.ndarray              # shape: (num_atoms, 3)

    @property
    def num_atoms(self) -> int:
        return len(self.atom_types)


@dataclass
class ParsedSample:
    config: ParsedConfig
    charges: np.ndarray                  # shape: (num_atoms,)

    @property
    def sample_id(self) -> int:
        return self.config.sample_id

    @property
    def num_atoms(self) -> int:
        return self.config.num_atoms


# -----------------------------
# Helpers
# -----------------------------

def extract_numeric_suffix(path: Path) -> int:
    """
    Extract trailing integer from names like CONFIG_57 or CHARGE_57.
    """
    name = path.name
    if "_" not in name:
        raise ValueError(f"Expected underscore in filename: {name}")
    suffix = name.split("_")[-1]
    if not suffix.isdigit():
        raise ValueError(f"Expected numeric suffix in filename: {name}")
    return int(suffix)


def expand_atom_types(symbols: List[str], counts: List[int]) -> List[str]:
    """
    Example:
        symbols = ["Ba", "O"]
        counts  = [32, 32]
    becomes:
        ["Ba", ..., "Ba", "O", ..., "O"]  # 64 total
    """
    if len(symbols) != len(counts):
        raise ValueError("element_symbols and element_counts length mismatch")

    expanded: List[str] = []
    for sym, count in zip(symbols, counts):
        expanded.extend([sym] * count)
    return expanded


def is_selective_dynamics_line(line: str) -> bool:
    return line.strip().lower().startswith("selective")


def normalize_coord_type(line: str) -> str:
    text = line.strip().lower()
    if text.startswith("d"):
        return "Direct"
    if text.startswith("c"):
        return "Cartesian"
    raise ValueError(f"Unknown coordinate type line: {line!r}")


# -----------------------------
# Parsing
# -----------------------------

def parse_config_file(config_path: Path) -> ParsedConfig:
    """
    Parse one POSCAR-style file from your PASCAR directory.
    Supports optional 'Selective dynamics' line.
    """
    if not config_path.exists():
        raise FileNotFoundError(f"Config file not found: {config_path}")

    lines = [line.rstrip() for line in config_path.read_text().splitlines()]
    if len(lines) < 8:
        raise ValueError(f"Config file too short to be valid POSCAR: {config_path}")

    sample_id = extract_numeric_suffix(config_path)

    # Standard POSCAR header
    comment = lines[0].strip()
    scale = float(lines[1].strip())

    lattice = np.array(
        [
            [float(x) for x in lines[2].split()],
            [float(x) for x in lines[3].split()],
            [float(x) for x in lines[4].split()],
        ],
        dtype=np.float64,
    )
    if lattice.shape != (3, 3):
        raise ValueError(f"Lattice is not 3x3 in {config_path}")

    element_symbols = lines[5].split()
    element_counts = [int(x) for x in lines[6].split()]
    if len(element_symbols) != len(element_counts):
        raise ValueError(
            f"Element symbols/counts mismatch in {config_path}: "
            f"{element_symbols} vs {element_counts}"
        )

    current_idx = 7
    selective_dynamics = False

    # Optional "Selective dynamics" line
    if is_selective_dynamics_line(lines[current_idx]):
        selective_dynamics = True
        current_idx += 1

    coord_type = normalize_coord_type(lines[current_idx])
    current_idx += 1

    num_atoms = sum(element_counts)
    coord_lines = lines[current_idx: current_idx + num_atoms]
    if len(coord_lines) != num_atoms:
        raise ValueError(
            f"Expected {num_atoms} coordinate rows in {config_path}, "
            f"but found {len(coord_lines)}"
        )

    coordinates: List[List[float]] = []
    for idx, line in enumerate(coord_lines):
        parts = line.split()
        if len(parts) < 3:
            raise ValueError(
                f"Coordinate row {idx} in {config_path} has fewer than 3 fields: {line!r}"
            )

        # POSCAR with Selective Dynamics may have T/F flags after xyz;
        # we only take the first 3 columns as coordinates.
        xyz = [float(parts[0]), float(parts[1]), float(parts[2])]
        coordinates.append(xyz)

    atom_types = expand_atom_types(element_symbols, element_counts)

    parsed = ParsedConfig(
        sample_id=sample_id,
        comment=comment,
        scale=scale,
        lattice=lattice,
        element_symbols=element_symbols,
        element_counts=element_counts,
        selective_dynamics=selective_dynamics,
        coord_type=coord_type,
        atom_types=atom_types,
        coordinates=np.array(coordinates, dtype=np.float64),
    )

    # Validation
    if parsed.coordinates.shape != (num_atoms, 3):
        raise ValueError(
            f"Coordinate matrix shape mismatch in {config_path}: "
            f"{parsed.coordinates.shape}, expected {(num_atoms, 3)}"
        )

    if len(parsed.atom_types) != num_atoms:
        raise ValueError(
            f"Expanded atom type list length mismatch in {config_path}: "
            f"{len(parsed.atom_types)} vs expected {num_atoms}"
        )

    return parsed


def parse_charge_file(charge_path: Path) -> Tuple[int, np.ndarray]:
    """
    Parse one charge file from your CHARGESSS directory.
    Assumes one float per non-empty line.
    """
    if not charge_path.exists():
        raise FileNotFoundError(f"Charge file not found: {charge_path}")

    sample_id = extract_numeric_suffix(charge_path)

    values: List[float] = []
    for idx, line in enumerate(charge_path.read_text().splitlines()):
        stripped = line.strip()
        if not stripped:
            continue
        try:
            values.append(float(stripped))
        except ValueError as exc:
            raise ValueError(
                f"Invalid float on line {idx + 1} of {charge_path}: {stripped!r}"
            ) from exc

    charges = np.array(values, dtype=np.float64)
    return sample_id, charges


def load_sample(config_path: Path, charge_path: Path) -> ParsedSample:
    """
    Load and validate one matched CONFIG_n / CHARGE_n pair.
    """
    config = parse_config_file(config_path)
    charge_id, charges = parse_charge_file(charge_path)

    if config.sample_id != charge_id:
        raise ValueError(
            f"Sample ID mismatch: CONFIG_{config.sample_id} vs CHARGE_{charge_id}"
        )

    if config.num_atoms != len(charges):
        raise ValueError(
            f"Atom/charge count mismatch for sample {config.sample_id}: "
            f"{config.num_atoms} atoms vs {len(charges)} charges"
        )

    return ParsedSample(config=config, charges=charges)


# -----------------------------
# ASE conversion
# -----------------------------

def build_ase_atoms(parsed_config: ParsedConfig) -> Atoms:
    """
    Convert parsed POSCAR data into an ASE Atoms object.
    Uses periodic boundary conditions.
    """
    cell = parsed_config.lattice * parsed_config.scale

    if parsed_config.coord_type == "Direct":
        atoms = Atoms(
            symbols=parsed_config.atom_types,
            scaled_positions=parsed_config.coordinates,
            cell=cell,
            pbc=True,
        )
    elif parsed_config.coord_type == "Cartesian":
        atoms = Atoms(
            symbols=parsed_config.atom_types,
            positions=parsed_config.coordinates * parsed_config.scale,
            cell=cell,
            pbc=True,
        )
    else:
        raise ValueError(f"Unsupported coordinate type: {parsed_config.coord_type}")

    return atoms


# -----------------------------
# Debug / summary output
# -----------------------------

def summarize_sample(sample: ParsedSample) -> None:
    cfg = sample.config

    print("=" * 72)
    print(f"Loaded sample: {sample.sample_id}")
    print("=" * 72)
    print(f"Comment              : {cfg.comment}")
    print(f"Scale                : {cfg.scale}")
    print(f"Element symbols      : {cfg.element_symbols}")
    print(f"Element counts       : {cfg.element_counts}")
    print(f"Selective dynamics   : {cfg.selective_dynamics}")
    print(f"Coordinate type      : {cfg.coord_type}")
    print(f"Num atoms            : {cfg.num_atoms}")
    print(f"Coordinates shape    : {cfg.coordinates.shape}")
    print(f"Charges shape        : {sample.charges.shape}")
    print("Lattice (scaled cell):")
    print(cfg.lattice * cfg.scale)

    unique_types, counts = np.unique(np.array(cfg.atom_types), return_counts=True)
    print("Expanded atom types  :")
    for atom_type, count in zip(unique_types, counts):
        print(f"  {atom_type}: {count}")

    print("\nFirst 5 atoms:")
    for i in range(min(5, cfg.num_atoms)):
        print(
            f"  idx={i:4d}  type={cfg.atom_types[i]:>2s}  "
            f"coord={cfg.coordinates[i]}  charge={sample.charges[i]: .6f}"
        )

    print("\nLast 5 atoms:")
    for i in range(max(0, cfg.num_atoms - 5), cfg.num_atoms):
        print(
            f"  idx={i:4d}  type={cfg.atom_types[i]:>2s}  "
            f"coord={cfg.coordinates[i]}  charge={sample.charges[i]: .6f}"
        )


def main() -> None:
    project_root = Path(__file__).resolve().parent
    test_root = project_root / "Basic Test"

    for num in {57, 99, 103, 10, 1}:
        config_path = test_root / "PASCAR" / f"CONFIG_{num}"
        charge_path = test_root / "CHARGESSS" / f"CHARGE_{num}"

        sample = load_sample(config_path, charge_path)
        summarize_sample(sample)

        atoms = build_ase_atoms(sample.config)
        print("\nASE object created successfully.")
        print(f"ASE len(atoms)        : {len(atoms)}")
        print(f"ASE chemical formula  : {atoms.get_chemical_formula()}")
        print(f"ASE cell:\n{atoms.cell}")
        print(f"ASE PBC               : {atoms.pbc}")


if __name__ == "__main__":
    main()
