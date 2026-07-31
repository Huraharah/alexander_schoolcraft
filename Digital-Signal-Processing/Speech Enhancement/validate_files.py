import os
import csv
import scipy.io.wavfile as wav

# Define the archive path
archive_path = r"C:\Users\Administrator\OneDrive\School\11 Fall 2024\CSCI 4810 Digital Information Processing\project\archive"

# Define clean and noisy folder paths
clean_folder = os.path.join(archive_path, "clean_testset_wav")
noisy_folder = os.path.join(archive_path, "noisy_testset_wav")

# Define output CSV file
output_csv = "audio_file_details.csv"

# Prepare a list to store file details
file_details = []

# Get all clean and noisy files
clean_files = [f for f in os.listdir(clean_folder) if f.endswith(".wav")]
noisy_files = [f for f in os.listdir(noisy_folder) if f.endswith(".wav")]

# Match clean and noisy files by base name
for clean_file in clean_files:
    base_name = clean_file.replace("_clean", "").replace(".wav", "")
    noisy_file = f"{base_name}_dirty.wav"

    if noisy_file in noisy_files:
        # Get full paths
        clean_path = os.path.join(clean_folder, clean_file)
        noisy_path = os.path.join(noisy_folder, noisy_file)

        # Load clean and noisy audio
        fs_clean, clean_audio = wav.read(clean_path)
        fs_noisy, noisy_audio = wav.read(noisy_path)

        # Get details
        clean_info = {
            "sample_rate": fs_clean,
            "duration_seconds": len(clean_audio) / fs_clean,
            "shape": clean_audio.shape
        }
        noisy_info = {
            "sample_rate": fs_noisy,
            "duration_seconds": len(noisy_audio) / fs_noisy,
            "shape": noisy_audio.shape
        }

        # Add details to the list
        file_details.append({
            "base_name": base_name,
            "clean_sample_rate": clean_info["sample_rate"],
            "clean_duration": clean_info["duration_seconds"],
            "clean_shape": clean_info["shape"],
            "noisy_sample_rate": noisy_info["sample_rate"],
            "noisy_duration": noisy_info["duration_seconds"],
            "noisy_shape": noisy_info["shape"]
        })

# Write details to a CSV file
with open(output_csv, mode="w", newline="") as csvfile:
    fieldnames = [
        "base_name",
        "clean_sample_rate",
        "clean_duration",
        "clean_shape",
        "noisy_sample_rate",
        "noisy_duration",
        "noisy_shape"
    ]
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

    writer.writeheader()
    writer.writerows(file_details)

print(f"Details written to {output_csv}")
