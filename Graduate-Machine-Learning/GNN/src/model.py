from typing import Callable

import torch
import torch.nn as nn
import torch.nn.functional as F
from torch_geometric.nn import GCNConv


def get_activation(name: str) -> Callable[[torch.Tensor], torch.Tensor]:
    """
    Return an activation function by name.
    """
    name = name.lower()

    if name == "relu":
        return F.relu
    if name == "leaky_relu":
        return F.leaky_relu
    if name == "elu":
        return F.elu

    raise ValueError(f"Unsupported activation: {name}")


class AtomicChargeGNN(nn.Module):
    """
    Graph Neural Network for node-level atomic charge prediction.

    Inputs
    ------
    x : node features
    edge_index : COO graph connectivity
    edge_attr : edge distances, shape [num_edges, 1] or [num_edges]

    Notes
    -----
    - No global pooling is used because this is a node regression task.
    - Edge distances are converted into similarity-like edge weights so that
      shorter interatomic distances contribute more strongly than longer ones.
    """

    def __init__(
        self,
        in_channels: int,
        hidden_channels: int = 64,
        num_layers: int = 3,
        dropout: float = 0.2,
        activation: str = "relu",
        use_edge_attr: bool = True,
    ) -> None:
        super().__init__()

        if num_layers < 2:
            raise ValueError("num_layers must be at least 2")

        self.in_channels = in_channels
        self.hidden_channels = hidden_channels
        self.num_layers = num_layers
        self.dropout = dropout
        self.activation_name = activation
        self.activation = get_activation(activation)
        self.use_edge_attr = use_edge_attr

        convs = [GCNConv(in_channels, hidden_channels)]
        for _ in range(num_layers - 1):
            convs.append(GCNConv(hidden_channels, hidden_channels))
        self.convs = nn.ModuleList(convs)

        self.output_head = nn.Linear(hidden_channels, 1)

    def _build_edge_weight(self, edge_attr: torch.Tensor) -> torch.Tensor:
        """
        Convert edge distances into similarity-like weights.

        Smaller distance -> larger weight.
        """
        if edge_attr.dim() == 2:
            if edge_attr.size(1) != 1:
                raise ValueError(
                    f"Expected edge_attr with shape [num_edges, 1], got {tuple(edge_attr.shape)}"
                )
            distances = edge_attr.squeeze(1)
        else:
            distances = edge_attr

        edge_weight = 1.0 / (1.0 + distances)
        return edge_weight

    def forward(
        self,
        x: torch.Tensor,
        edge_index: torch.Tensor,
        edge_attr: torch.Tensor = None,
    ) -> torch.Tensor:
        edge_weight = None
        if self.use_edge_attr and edge_attr is not None:
            edge_weight = self._build_edge_weight(edge_attr)

        for conv in self.convs:
            x = conv(x, edge_index, edge_weight=edge_weight)
            x = self.activation(x)
            x = F.dropout(x, p=self.dropout, training=self.training)

        x = self.output_head(x)
        return x


def count_parameters(model: nn.Module) -> int:
    return sum(p.numel() for p in model.parameters() if p.requires_grad)


def print_model_summary(model: nn.Module) -> None:
    print("=" * 72)
    print("Model Summary")
    print("=" * 72)
    print(model)
    print(f"\nTrainable parameters: {count_parameters(model):,}")