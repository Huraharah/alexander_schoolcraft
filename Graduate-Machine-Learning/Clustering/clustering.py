import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import plotly.graph_objects as go
import plotly.express as px
import plotly.colors as pc
import time, os
from collections import deque
from sklearn.neighbors import NearestNeighbors
from sklearn.metrics import silhouette_score, davies_bouldin_score, calinski_harabasz_score

# Load the dataset and extract relevant features
df=pd.read_csv('Mall_Customers.csv')
X_raw = df[["Age", "Annual Income (k$)", "Spending Score (1-100)"]].values
genders = df["Genre"].values  # 'Male' or 'Female'
markers = {"Male": "o", "Female": "x"}

# Normalize the data (Z-score normalization) and initialize random seed based off of system time (help with bad initializations)
mu = X_raw.mean(axis=0)
sigma = X_raw.std(axis=0)
sigma[sigma == 0] = 1.0
X = (X_raw - mu) / sigma
np.random.seed(int(time.time_ns() & 0xFFFFFFFF)) 

# K-Means Clustering Implementation
class KMeans:
    def __init__(self, n_clusters=5, max_iters=100, tol=1e-4, init='k-means++', random_state=None):
        self.n_clusters = n_clusters
        self.max_iters = max_iters
        self.tol = tol
        self.init = init
        self.random_state = random_state
        self.centroids = None
        self.labels_ = None
        self.inertia_ = None
        self.n_iter_ = 0
        self.stop_reason = "max_iters"
        if random_state is not None:
            np.random.seed(random_state)

    def _init_centroids(self, X):
        n_samples = X.shape[0]
        rng = np.random if self.random_state is None else np.random.RandomState(self.random_state)

        if self.init == 'random':
            idx = rng.choice(n_samples, self.n_clusters, replace=False)
            return X[idx].astype(float, copy=True)

        # --- k-means++ (robust) ---
        # pick first centroid uniformly
        first = rng.randint(0, n_samples)
        selected = [first]

        for _ in range(1, self.n_clusters):
            # squared distances to nearest already-selected centroid
            d2 = self._pairwise_sq_dists(X, X[np.array(selected)])
            min_d2 = np.min(d2, axis=1)                # shape (n_samples,)
            min_d2 = np.maximum(min_d2, 0.0)           # clip tiny negatives
            # don't select an index we already picked
            min_d2[np.array(selected)] = 0.0

            # restrict to remaining candidates
            remaining = np.setdiff1d(np.arange(n_samples), np.array(selected), assume_unique=False)
            weights = min_d2[remaining]
            total = weights.sum()

            if not np.isfinite(total) or total <= 0.0:
                # degenerate case: all remaining points are identical distances (or 0)
                next_idx = rng.choice(remaining)
            else:
                probs = weights / total                 # guaranteed non-negative, sums to 1
                next_idx = rng.choice(remaining, p=probs)

            selected.append(int(next_idx))

        return X[np.array(selected)].astype(float, copy=True)

    @staticmethod
    def _pairwise_sq_dists(A, B):
        # returns squared Euclidean distances shape (len(A), len(B))
        A2 = np.sum(A*A, axis=1, keepdims=True)
        B2 = np.sum(B*B, axis=1, keepdims=True).T
        return A2 + B2 - 2 * (A @ B.T)

    def _assign_clusters(self, X):
        d2 = self._pairwise_sq_dists(X, self.centroids)
        return np.argmin(d2, axis=1), d2

    def _compute_centroids(self, X, labels):
        centroids = np.zeros((self.n_clusters, X.shape[1]), dtype=float)
        for k in range(self.n_clusters):
            mask = (labels == k)
            if np.any(mask):
                centroids[k] = X[mask].mean(axis=0)
            else:
                # re-seed an empty cluster to a random data point
                centroids[k] = X[np.random.randint(0, X.shape[0])]
        return centroids

    def fit(self, X):
        X = np.asarray(X, dtype=float)
        self.centroids = self._init_centroids(X)
        prev_labels = None

        for it in range(1, self.max_iters + 1):
            labels, d2 = self._assign_clusters(X)
            new_centroids = self._compute_centroids(X, labels)

            # convergence checks
            moved = np.linalg.norm(new_centroids - self.centroids)
            if prev_labels is not None and np.array_equal(labels, prev_labels):
                self.centroids = new_centroids
                self.labels_ = labels
                self.n_iter_ = it
                self.stop_reason = "labels_stable"
                break

            if moved < self.tol:
                self.centroids = new_centroids
                self.labels_ = labels
                self.n_iter_ = it
                self.stop_reason = "centroids_converged"
                break

            self.centroids = new_centroids
            prev_labels = labels

            if it == self.max_iters:
                self.labels_ = labels
                self.n_iter_ = it

        # inertia (sum of squared distances to closest centroid)
        _, d2 = self._assign_clusters(X)
        rows = np.arange(X.shape[0])
        self.inertia_ = d2[rows, self.labels_].sum()
        return self

    def predict(self, X):
        if self.centroids is None:
            raise RuntimeError("Call fit(X) before predict(X).")
        X = np.asarray(X, dtype=float)
        labels, _ = self._assign_clusters(X)
        return labels

# helper: convert standardized centroids back to original scale
def centroids_to_original(centroids_std, mu, sigma):
    return centroids_std * sigma + mu

def kmeans_best_of(X, k, n_init=10, max_iters=100, tol=1e-4):
    best_model, best_inertia = None, np.inf
    for i in range(n_init):
        np.random.seed(int(time.time_ns() & 0xFFFFFFFF) + i)
        km = KMeans(n_clusters=k, max_iters=max_iters, tol=tol, init='k-means++').fit(X)
        if km.inertia_ < best_inertia:
            best_model, best_inertia = km, km.inertia_
    return best_model

def inertia_curve(X, k_min=2, k_max=12, n_init=10):
    ks = list(range(k_min, k_max+1))
    means, stds = [], []
    for k in ks:
        vals = []
        for i in range(n_init):
            np.random.seed(int(time.time_ns() & 0xFFFFFFFF) + 1000*k + i)
            km = KMeans(n_clusters=k, init='k-means++').fit(X)
            vals.append(km.inertia_)
        means.append(np.mean(vals))
        stds.append(np.std(vals))
    return np.array(ks), np.array(means), np.array(stds)

def knee_from_curve(x, y):
    """Elbow via max distance to the line through endpoints (x,y)."""
    x = np.asarray(x, float)
    y = np.asarray(y, float)
    # line through endpoints
    m = (y[-1] - y[0]) / (x[-1] - x[0] + 1e-12)
    b = y[0] - m * x[0]
    # perpendicular distance of each point to that line
    denom = np.sqrt(m*m + 1.0) + 1e-12
    d = np.abs(m*x - y + b) / denom
    idx = int(np.argmax(d))
    return idx, int(x[idx])

def find_best_k_elbow(X, k_min=2, k_max=12, n_init=10, saveplot=None):
    ks, mean_I, std_I = inertia_curve(X, k_min, k_max, n_init)
    idx, chosen_k = knee_from_curve(ks, mean_I)

    if saveplot:
        plt.close('all')
        fig, ax = plt.subplots(figsize=(7,4.5))
        ax.plot(ks, mean_I, marker='o', lw=2)
        ax.fill_between(ks, mean_I-std_I, mean_I+std_I, alpha=0.15, edgecolor='none')
        ax.axvline(chosen_k, color='red', ls='--', lw=1)
        ax.scatter([chosen_k], [mean_I[idx]], s=120, c='red', edgecolor='k', zorder=5)
        ax.set_xlabel("k (number of clusters)")
        ax.set_ylabel("WCSS / Inertia")
        ax.set_title(f"K-Means Elbow (auto knee, n_init={n_init})")
        fig.tight_layout()
        fig.savefig(saveplot, dpi=200)
    return chosen_k, (ks, mean_I, std_I)

k, _ = find_best_k_elbow(X, k_min=2, k_max=12, n_init=10, saveplot="kmeans_elbow_auto.png")
print("Chosen k (elbow):", k)
model = kmeans_best_of(X, k, n_init=10, max_iters=100)
labels = model.labels_

# Convert centroids back to original scale
centroids_orig = centroids_to_original(model.centroids, mu, sigma)

# Create 2D plots of K-Means clusters
def plot_kmeans_2d(x_idx, y_idx, xlabel, ylabel, fname, k=None, stop_reason=None,
                   centroids_orig=None, show_centroids=True):
    plt.figure()
    # clusters present (sorted for stable colors)
    cluster_ids = sorted(set(labels))
    for gender, marker in markers.items():
        mask_gender = (genders == gender)
        for cid in cluster_ids:
            mask = (labels == cid) & mask_gender
            if np.any(mask):
                plt.scatter(
                    X_raw[mask, x_idx], X_raw[mask, y_idx],
                    c=[plt.cm.tab10(cid % 10)], marker=marker, s=40, alpha=0.85,
                    label=f"{gender} C{cid}"
                )

    if show_centroids and centroids_orig is not None:
        plt.scatter(
            centroids_orig[:, x_idx], centroids_orig[:, y_idx],
            c="red", marker="X", s=200, edgecolor="k", linewidth=1.5, label="Centroids"
        )

    plt.xlabel(xlabel); plt.ylabel(ylabel)
    title = "K-Means"
    if k is not None:
        title += f" (k={k}"
        if stop_reason is not None:
            title += f", stop={stop_reason}"
        title += ")"
    plt.title(title)

    # de-duplicate legend entries
    handles, lab = plt.gca().get_legend_handles_labels()
    uniq = dict(zip(lab, handles))
    plt.legend(uniq.values(), uniq.keys(), fontsize=8, ncol=2, frameon=True)

    plt.tight_layout()
    plt.savefig(fname, dpi=200)

plot_kmeans_2d(1, 2, "Annual Income (k$)", "Spending Score (1-100)", "kmeans_income_spend.png", k=k, stop_reason=model.stop_reason, centroids_orig=centroids_orig, show_centroids=True)
plot_kmeans_2d(0, 1, "Age", "Annual Income (k$)", "kmeans_age_income.png", k=k, stop_reason=model.stop_reason, centroids_orig=centroids_orig, show_centroids=True)
plot_kmeans_2d(0, 2, "Age", "Spending Score (1-100)", "kmeans_age_spend.png", k=k, stop_reason=model.stop_reason, centroids_orig=centroids_orig, show_centroids=True)


# 3D plot of Age, Income, Spending
def plot_kmeans_3d(
    fname="kmeans_3d_clusters.png",
    title_prefix="K-Means 3D",
    k=None,
    stop_reason=None,
    centroids_orig=None,
    show_centroids=True,
    point_size=40
):
    fig = plt.figure(figsize=(8,6))
    ax = fig.add_subplot(111, projection="3d")

    cluster_ids = sorted(set(labels))
    for gender, marker in markers.items():
        mask_gender = (genders == gender)
        for cid in cluster_ids:
            mask = (labels == cid) & mask_gender
            if np.any(mask):
                ax.scatter(
                    X_raw[mask, 0], X_raw[mask, 1], X_raw[mask, 2],
                    c=[plt.cm.tab10(cid % 10)],
                    marker=marker,
                    s=point_size,
                    alpha=0.85,
                    label=f"{gender} C{cid}"
                )

    if show_centroids and centroids_orig is not None:
        ax.scatter(
            centroids_orig[:, 0],
            centroids_orig[:, 1],
            centroids_orig[:, 2],
            c="red",
            marker="X",
            s=200,
            edgecolor="k",
            linewidth=1.5,
            label="Centroids"
        )

    ax.set_xlabel("Age")
    ax.set_ylabel("Annual Income (k$)")
    ax.set_zlabel("Spending Score (1-100)")

    title = title_prefix
    if k is not None:
        title += f" (k={k}"
        if stop_reason is not None:
            title += f", stop={stop_reason}"
        title += ")"
    ax.set_title(title)

    # De-duplicate legend
    handles, labels_ = ax.get_legend_handles_labels()
    uniq = dict(zip(labels_, handles))
    ax.legend(uniq.values(), uniq.keys(), loc="upper left", fontsize=8)

    plt.tight_layout()
    plt.savefig(fname, dpi=200)

plot_kmeans_3d(
    fname="kmeans_3d_clusters.png",
    k=k,
    stop_reason=model.stop_reason,
    centroids_orig=centroids_orig,
    show_centroids=True
)

# Build a tidy frame (ORIGINAL units for axes)
df_plot = pd.DataFrame({
    "Age":       X_raw[:, 0],
    "Income":    X_raw[:, 1],
    "Spending":  X_raw[:, 2],
    "Gender":    genders,
    "Cluster":   labels.astype(str)   # K-Means has no noise; all points labeled
})

# Color map: distinct colors per cluster (stable order)
palette = pc.qualitative.Plotly  # (or D3/Set3)
clusters_present = sorted(df_plot["Cluster"].unique(), key=lambda s: int(s))
color_map = {cid: palette[i % len(palette)] for i, cid in enumerate(clusters_present)}

fig = px.scatter_3d(
    df_plot,
    x="Age", y="Income", z="Spending",
    color="Cluster",
    symbol="Gender",
    opacity=0.85,
    color_discrete_map=color_map,
    category_orders={"Cluster": clusters_present}
)

# Smaller points feel nicer in 3D; tweak layout/titles
fig.update_traces(marker=dict(size=4))
fig.update_layout(
    title=f"K-Means 3D (k={k}, stop={model.stop_reason})",
    scene=dict(
        xaxis_title="Age",
        yaxis_title="Annual Income (k$)",
        zaxis_title="Spending Score (1-100)"
    ),
    legend_title_text="Cluster / Gender",
    template="plotly_white",
    showlegend=True
)

# Centroids (in original units) as red X's with black outline
fig.add_trace(go.Scatter3d(
    x=centroids_orig[:, 0],
    y=centroids_orig[:, 1],
    z=centroids_orig[:, 2],
    mode="markers",
    marker=dict(size=10, symbol="x", color="red", line=dict(color="black", width=2)),
    name="Centroids"
))

# Save interactive HTML
fig.write_html("kmeans_3d_clusters_interactive.html", include_plotlyjs="cdn", auto_open=False)
print("Saved interactive plot to kmeans_3d_clusters_interactive.html")


# plot K derivation elbow graph showing which value to use as K
def plot_kmeans_elbow(X, k_min=2, k_max=12, n_init=10, chosen_k=None,
                      savepath="kmeans_elbow.png"):
    ks = list(range(k_min, k_max + 1))
    mean_inertia, std_inertia = [], []

    for k in ks:
        vals = []
        for _ in range(n_init):
            km = KMeans(n_clusters=k, init='k-means++').fit(X)
            vals.append(km.inertia_)
        mean_inertia.append(np.mean(vals))
        std_inertia.append(np.std(vals))

    mean_inertia = np.array(mean_inertia)
    std_inertia = np.array(std_inertia)

    plt.close('all')
    fig, ax = plt.subplots(figsize=(7,4.5))
    ax.plot(ks, mean_inertia, marker='o', lw=2)
    ax.fill_between(ks, mean_inertia-std_inertia, mean_inertia+std_inertia,
                    alpha=0.15, edgecolor='none')
    ax.set_xlabel("k (number of clusters)")
    ax.set_ylabel("WCSS / Inertia")
    ax.set_title(f"K-Means Elbow (n_init={n_init})")

    if chosen_k is not None:
        # mark the chosen k
        idx = ks.index(chosen_k)
        ax.scatter([ks[idx]], [mean_inertia[idx]], s=120, c='red', edgecolor='k', zorder=5)
        ax.axvline(chosen_k, color='red', ls='--', lw=1, label=f"chosen k={chosen_k}")
        ax.legend()

    fig.tight_layout()
    fig.savefig(savepath, dpi=200)

plot_kmeans_elbow(X, k_min=2, k_max=10, n_init=10, chosen_k=10,
                  savepath="kmeans_elbow.png")

# conduct silhouette analysis for KMeans
score = silhouette_score(X, labels)
print("K-Means silhouette score:", score)

# DBSCAN Clustering Implementation
class DBSCAN:
    def __init__(self, eps=0.5, min_samples=5):
        self.eps = eps
        self.min_samples = min_samples
        self.labels_ = None

    def fit(self, X):
        n = X.shape[0]
        self.labels_ = np.full(n, -1, dtype=int)  # -1 = noise
        cluster_id = 0
        visited = np.zeros(n, dtype=bool)

        for i in range(n):
            if visited[i]:
                continue
            visited[i] = True
            neighbors = self._region_query(X, i)
            if len(neighbors) < self.min_samples:
                self.labels_[i] = -1  # noise
            else:
                self._expand_cluster(X, i, neighbors, cluster_id, visited)
                cluster_id += 1
        return self

    def _region_query(self, X, idx):
        dists = np.linalg.norm(X - X[idx], axis=1)
        return np.where(dists <= self.eps)[0].tolist()

    def _expand_cluster(self, X, idx, neighbors, cluster_id, visited):
        queue = deque(neighbors)
        self.labels_[idx] = cluster_id
        while queue:
            j = queue.popleft()
            if not visited[j]:
                visited[j] = True
                j_neighbors = self._region_query(X, j)
                if len(j_neighbors) >= self.min_samples:
                    queue.extend(j_neighbors)
            if self.labels_[j] == -1:  # previously noise
                self.labels_[j] = cluster_id
            elif self.labels_[j] == -1 or self.labels_[j] is None:
                self.labels_[j] = cluster_id

## Use elbow method to find a starting eps value
def auto_dbscan(X, min_samples_grid=(5,6,7), knee_quantile=0.9,
                eps_factors=np.linspace(0.5, 1.1, 13),
                max_clusters=8, max_noise_ratio=0.2):

    # --- helper methods to generate elbow ---
    def k_distance_numpy(X, k=5):
        X = np.asarray(X, float)
        G = X @ X.T
        n2 = np.sum(X*X, axis=1, keepdims=True)
        D2 = n2 + n2.T - 2*G
        np.fill_diagonal(D2, np.inf)
        D = np.sqrt(D2)
        kth = np.partition(D, k-1, axis=1)[:, k-1]
        return np.sort(kth)

    def knee_from_curve(y):
        y = np.asarray(y, float)
        n = len(y); x = np.arange(n, dtype=float)
        m = (y[-1]-y[0]) / (x[-1]-x[0] + 1e-12)
        b = y[0] - m*x[0]
        d = np.abs(m*x - y + b) / (np.sqrt(m*m + 1) + 1e-12)
        idx = int(np.argmax(d))
        return idx, y[idx]

    best = None  # (score_tuple, labels, eps, ms)

    for ms in min_samples_grid:
        kd = k_distance_numpy(X, k=ms)
        _, eps0 = knee_from_curve(kd)
        for f in eps_factors:
            eps = float(eps0 * f)
            db = DBSCAN(eps=eps, min_samples=ms).fit(X)
            labels = db.labels_
            n_noise = int(np.sum(labels == -1))
            n_pts = len(labels)
            noise_ratio = n_noise / max(n_pts, 1)
            n_clusters = len(set(labels)) - (1 if -1 in labels else 0)

            # scoring: prefer valid cluster counts, low noise
            valid = (2 <= n_clusters <= max_clusters)
            score = (
                1 if valid else 0,          # validity flag
                -noise_ratio,               # lower noise is better
                n_clusters,                 # more clusters (within cap) is better
            )
            cand = (score, labels, eps, ms)
            if (best is None) or (score > best[0]):
                best = cand

    # if everything invalid, fall back to the best (lowest noise) regardless of count
    if best is None:
        # retry keeping the lowest noise across all
        best_noise = (float('inf'), None, None, None)
        for ms in min_samples_grid:
            kd = k_distance_numpy(X, k=ms)
            _, eps0 = knee_from_curve(kd)
            for f in eps_factors:
                eps = float(eps0 * f)
                db = DBSCAN(eps=eps, min_samples=ms).fit(X)
                labels = db.labels_
                noise_ratio = np.mean(labels == -1)
                if noise_ratio < best_noise[0]:
                    best_noise = (noise_ratio, labels, eps, ms)
        _, labels, eps, ms = best_noise
        return labels, eps, ms

    _, labels, eps, ms = best
    return labels, eps, ms

labels_db, eps_used, ms_used = auto_dbscan(
    X,
    min_samples_grid=(5,6,7),                 # try a few reasonable values
    eps_factors=np.linspace(0.6, 1.05, 10),   # search around the knee
    max_clusters=8,
    max_noise_ratio=0.2
)

# Sweep scaling eps values and minimum samples to find strongest pair for full run
def sweep_dbscan(X, eps0, eps_scales=(0.5, 0.6, 0.7, 0.8, 0.9, 1.0),
                 min_samples_list=(5, 6, 7, 8)):
    results = []
    for ms in min_samples_list:
        for s in eps_scales:
            eps = eps0 * s
            db = DBSCAN(eps=eps, min_samples=ms).fit(X)
            labels = db.labels_
            n_clusters = len(set(labels)) - (1 if -1 in labels else 0)
            noise_ratio = (labels == -1).mean()
            results.append((eps, ms, n_clusters, noise_ratio))
    # Sort by: valid cluster count (2..8), then lower noise, then more clusters
    def score(row):
        eps, ms, c, nr = row
        valid = 1 if 2 <= c <= 8 else 0
        return (valid, -nr, c)
    results.sort(key=score, reverse=True)
    return results

eps0 = eps_used 
results = sweep_dbscan(X, eps0)
#for eps_try, ms_try, c, nr in results[:10]:
#    print(f"eps={eps_try:.3f}, min_samples={ms_try} -> clusters={c}, noise={nr:.2%}")

# Convert results for plotting
eps_list      = np.array([r[0] for r in results])
ms_list       = np.array([r[1] for r in results])
clusters_list = np.array([r[2] for r in results])
noise_list    = np.array([r[3] for r in results])

# prefer 3..7 clusters, noise <= 0.2, then lowest noise, then more clusters
cands = []
for eps_i, ms_i, c_i, nr_i in results:
    valid = (3 <= c_i <= 7) and (nr_i <= 0.20)
    score = (1 if valid else 0, -nr_i, c_i)
    cands.append((score, eps_i, ms_i))
cands.sort(reverse=True)
if cands:
    _, chosen_eps, chosen_ms = cands[0]

# scatter: color = clusters, size = (1-noise)
sizes = (1.0 - noise_list) * 400 + 30
plt.close('all')
fig, ax = plt.subplots(figsize=(8,5))
sc = ax.scatter(eps_list, ms_list, c=clusters_list, s=sizes, cmap='viridis', alpha=0.85, edgecolors='k', linewidths=0.5)
cb = fig.colorbar(sc, ax=ax, pad=0.02)
cb.set_label("Number of clusters")

# highlight chosen
ax.scatter([chosen_eps], [chosen_ms], marker='*', s=400, c='red', edgecolor='k', linewidths=1.2, label='Chosen params')
ax.axvline(chosen_eps, color='red', ls='--', lw=1)
ax.axhline(chosen_ms,  color='red', ls='--', lw=1)

# legend for noise/size
for frac in (0.1, 0.2, 0.3):
    ax.scatter([], [], s=(1.0-frac)*400+30, c='gray', alpha=0.5, label=f"~{int((1-frac)*100)}% non-noise")
handles, labels = ax.get_legend_handles_labels()
# keep unique legend entries
uniq = dict(zip(labels, handles))
ax.legend(uniq.values(), uniq.keys(), loc='lower right', frameon=True)

ax.set_xlabel("eps")
ax.set_ylabel("min_samples")
ax.set_title("DBSCAN parameter sweep (color=clusters, size=(1−noise))")
fig.tight_layout()
fig.savefig("dbscan_sweep_summary.png", dpi=200)

# Final DBSCAN
db_final = DBSCAN(eps=chosen_eps, min_samples=chosen_ms).fit(X)
labels_db = db_final.labels_
n_clusters_db = len(set(labels_db)) - (1 if -1 in labels_db else 0)
n_noise_db    = int((labels_db == -1).sum())
print(f"DBSCAN final: eps={chosen_eps:.3f}, min_samples={chosen_ms}, clusters={n_clusters_db}, noise_pts={n_noise_db}")

# conduct Davies-Boulding and Calinski-Harabaz analysis for DBSCAN
mask = (labels_db != -1)   # exclude noise for DBI/CHI
if len(set(labels_db[mask])) > 1:
    dbi = davies_bouldin_score(X[mask], labels_db[mask])
    chi = calinski_harabasz_score(X[mask], labels_db[mask])
    print("DBSCAN DBI:", dbi, "CHI:", chi)
else:
    print("Not enough clusters for DBI/CHI.")

# Shared markers
markers = {"Male": "o", "Female": "x"}

# 2D helper
def plot_dbscan_2d(x_idx, y_idx, xlabel, ylabel, fname):
    plt.figure()
    for gender, marker in markers.items():
        mask_gender = (genders == gender)
        # noise
        mask_noise = (labels_db == -1) & mask_gender
        if np.any(mask_noise):
            plt.scatter(X_raw[mask_noise, x_idx], X_raw[mask_noise, y_idx],
                        c="k", marker=marker, s=40, alpha=0.6, label=f"{gender} Noise")
        # clusters
        for cluster_id in sorted(c for c in set(labels_db) if c != -1):
            mask = (labels_db == cluster_id) & mask_gender
            if np.any(mask):
                plt.scatter(X_raw[mask, x_idx], X_raw[mask, y_idx],
                            c=[plt.cm.tab10(cluster_id % 10)], marker=marker, s=40, alpha=0.85,
                            label=f"{gender} C{cluster_id}")
    plt.xlabel(xlabel); plt.ylabel(ylabel)
    plt.title(f"DBSCAN (eps={chosen_eps:.3f}, min_samples={chosen_ms})")
    # de-duplicate legend
    handles, labels = plt.gca().get_legend_handles_labels()
    uniq = dict(zip(labels, handles))
    plt.legend(uniq.values(), uniq.keys(), fontsize=8, ncol=2, frameon=True)
    plt.tight_layout()
    plt.savefig(fname, dpi=200)

# 2D views
plot_dbscan_2d(1, 2, "Annual Income (k$)", "Spending Score (1-100)", "dbscan_income_spend.png")
plot_dbscan_2d(0, 1, "Age", "Annual Income (k$)", "dbscan_age_income.png")
plot_dbscan_2d(0, 2, "Age", "Spending Score (1-100)", "dbscan_age_spend.png")

# 3D - static
fig = plt.figure(figsize=(8,6))
ax = fig.add_subplot(111, projection="3d")
for gender, marker in markers.items():
    mask_gender = (genders == gender)
    # noise
    mask_noise = (labels_db == -1) & mask_gender
    if np.any(mask_noise):
        ax.scatter(X_raw[mask_noise, 0], X_raw[mask_noise, 1], X_raw[mask_noise, 2],
                   c="k", marker=marker, s=40, alpha=0.6, label=f"{gender} Noise")
    # clusters
    for cluster_id in sorted(c for c in set(labels_db) if c != -1):
        mask = (labels_db == cluster_id) & mask_gender
        if np.any(mask):
            ax.scatter(X_raw[mask, 0], X_raw[mask, 1], X_raw[mask, 2],
                       c=[plt.cm.tab10(cluster_id % 10)], marker=marker, s=40, alpha=0.85,
                       label=f"{gender} C{cluster_id}")
ax.set_xlabel("Age"); ax.set_ylabel("Annual Income (k$)"); ax.set_zlabel("Spending Score (1-100)")
ax.set_title(f"DBSCAN 3D (eps={chosen_eps:.3f}, min_samples={chosen_ms})")
# de-duplicate legend
handles, labels = ax.get_legend_handles_labels()
uniq = dict(zip(labels, handles))
ax.legend(uniq.values(), uniq.keys(), loc="upper left", fontsize=8)
plt.tight_layout()
plt.savefig("dbscan_3d_clusters.png", dpi=200)

# Build a tidy frame for Plotly (use ORIGINAL units for axes)
df_plot = pd.DataFrame({
    "Age":       X_raw[:, 0],
    "Income":    X_raw[:, 1],
    "Spending":  X_raw[:, 2],
    "Gender":    genders,
    "Cluster":   np.where(labels_db == -1, "Noise", labels_db.astype(str))
})

# Color map: distinct colors for clusters, black for noise
palette = pc.qualitative.Plotly  # or pc.qualitative.D3 / Set3
clusters_present = sorted([c for c in set(df_plot["Cluster"]) if c != "Noise"], key=lambda s: int(s))
color_map = {"Noise": "#000000"}
for i, cid in enumerate(clusters_present):
    color_map[cid] = palette[i % len(palette)]

fig = px.scatter_3d(
    df_plot,
    x="Age", y="Income", z="Spending",
    color="Cluster",
    symbol="Gender",
    opacity=0.85,
    color_discrete_map=color_map,
    category_orders={"Cluster": clusters_present + ["Noise"]}  # keep Noise last in legend
)

# Smaller points feel nicer in 3D; tweak layout/titles
fig.update_traces(marker=dict(size=4))
fig.update_layout(
    title=f"DBSCAN 3D (eps={chosen_eps:.3f}, min_samples={chosen_ms})",
    scene=dict(
        xaxis_title="Age",
        yaxis_title="Annual Income (k$)",
        zaxis_title="Spending Score (1-100)"
    ),
    legend_title_text="Cluster / Gender",
    template="plotly_white",
    showlegend=True
)

# Save interactive HTML (works on Bridges2, no GUI needed)
fig.write_html("dbscan_3d_clusters_interactive.html", include_plotlyjs="cdn", auto_open=False)

def k_distance_numpy(X, k=5):
    X = np.asarray(X, float)
    G = X @ X.T
    n2 = np.sum(X*X, axis=1, keepdims=True)
    D2 = n2 + n2.T - 2*G
    np.fill_diagonal(D2, np.inf)
    D = np.sqrt(D2)
    kth = np.partition(D, k-1, axis=1)[:, k-1]
    return np.sort(kth)

def knee_from_curve(y):
    y = np.asarray(y, float)
    n = len(y)
    x = np.arange(n, dtype=float)
    m = (y[-1] - y[0]) / (x[-1] - x[0] + 1e-12)
    b = y[0] - m * x[0]
    denom = np.sqrt(m*m + 1.0) + 1e-12
    d = np.abs(m*x - y + b) / denom
    idx = int(np.argmax(d))
    return idx, y[idx]

# Plot knee graph for DBSCAN to indicate why chosen parameters
def plot_dbscan_knee(X, min_samples=6, chosen_eps=None,
                     savepath="dbscan_k_distance_knee.png"):
    kd = k_distance_numpy(X, k=min_samples)
    idx, eps_auto = knee_from_curve(kd)
    eps_to_mark = chosen_eps if chosen_eps is not None else eps_auto

    plt.close('all')
    fig, ax = plt.subplots(figsize=(7,4.5))
    ax.plot(np.arange(len(kd)), kd, lw=2)
    # mark the auto knee
    ax.axvline(idx, color='orange', ls='--', lw=1, label=f"knee idx={idx}")
    ax.axhline(eps_auto, color='orange', ls='--', lw=1, label=f"auto eps≈{eps_auto:.3f}")
    # mark the chosen eps (if different)
    if chosen_eps is not None:
        ax.axhline(chosen_eps, color='red', ls='--', lw=1.2, label=f"chosen eps={chosen_eps:.3f}")
    ax.set_xlabel("Points sorted by distance")
    ax.set_ylabel(f"{min_samples}-NN distance")
    ax.set_title(f"DBSCAN k-distance (min_samples={min_samples})")
    ax.legend()
    fig.tight_layout()
    fig.savefig(savepath, dpi=200)
    print(f"Saved {savepath}")
    return eps_auto, idx, kd

plot_dbscan_knee(X, min_samples=6, chosen_eps=0.634,
                 savepath="dbscan_k_distance_knee.png")

