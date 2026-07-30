"""
results.py

Result analysis functionality.
"""

import json
import os

import numpy as np

from commons import RESULTS_DIR, RAW_RESULTS_DIR


class ResultAnalyzer:
    """
    Provides analysis of Trainwreck results.
    """

    RESULT_ANALYSIS_DIR = os.path.join(RESULTS_DIR, "analysis")
    BEST_METRICS_CSV_PATH = os.path.join(RESULT_ANALYSIS_DIR, "best_metrics.csv")

    @classmethod
    def best_metrics_csv(cls) -> None:
        """
        Creates a CSV file reporting on the best values of eval metrics (top-1 acc, top-5 acc,
        CE loss) achieved by the evaluated trained models across the training epochs.
        """
        # Ensure the result analysis dir exists
        cls._create_analysis_dir()

        # Init the CSV string to the header
        best_metrics_csv = "model,best_top1_acc,best_top5_acc,best_loss\n"

        # Iterate over the results
        for raw_result_json_fname in os.listdir(RAW_RESULTS_DIR):
            # Load the raw result
            raw_result_json_path = os.path.join(RAW_RESULTS_DIR, raw_result_json_fname)

            with open(raw_result_json_path, "r", encoding="utf-8") as f:
                raw_result = json.loads(f.read())

            # The model name is simply the file name before ".json"
            model = ".".join(raw_result_json_fname.split(".")[:-1])

            # Initialize the metric trackers
            best_top1_acc = 0.0
            best_top5_acc = 0.0
            best_loss = np.inf

            # Iterate over the results array and record the best vals from epoch 20 onwards
            for epoch_entry in raw_result[-10:]:
                best_top1_acc = max(best_top1_acc, epoch_entry["top1"])
                best_top5_acc = max(best_top5_acc, epoch_entry["top5"])
                best_loss = min(best_loss, epoch_entry["loss"])

            # Append the model's entry to the CSV
            best_metrics_csv += f"{model},{best_top1_acc},{best_top5_acc},{best_loss}\n"

        # Save the CSV
        with open(cls.BEST_METRICS_CSV_PATH, "w", encoding="utf-8") as f:
            f.write(best_metrics_csv)

    @classmethod
    def complete_analysis(cls) -> None:
        """
        Performs a complete results analysis, running all available analysis methods and
        outputting the results.
        """
        # Outputs a CSV with the best eval metrics achieved by individual models across epochs
        cls.best_metrics_csv()

    @classmethod
    def _create_analysis_dir(cls):
        """
        Creates the results analysis directory if it doesn't exist already.
        """
        if not os.path.exists(cls.RESULT_ANALYSIS_DIR):
            os.makedirs(cls.RESULT_ANALYSIS_DIR)
