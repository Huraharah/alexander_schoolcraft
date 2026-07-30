import os
import shutil

# Define source directories
clean_dir = './archive/clean_testset_wav'
noisy_dir = './archive/noisy_testset_wav'
filtered_dir = './processed_data/noisy_testset_filtered'
vad_dir = './processed_data/noisy_testset_VaD'
plots_dir = './plots'

# Define destination directory
results_dir = './results'

# Create results directory if it doesn't exist
os.makedirs(results_dir, exist_ok=True)

# Get list of all noisy files (base identifiers)
noisy_files = [f for f in os.listdir(noisy_dir) if f.endswith('.wav')]

# Process each file
for noisy_file in noisy_files:
    # Extract the base identifier by removing '_dirty.wav'
    base_identifier = noisy_file.replace('_dirty.wav', '')

    # Create a folder for the sample in the results directory
    sample_dir = os.path.join(results_dir, base_identifier)
    os.makedirs(sample_dir, exist_ok=True)

    # Define the expected filenames
    clean_file = os.path.join(clean_dir, f'{base_identifier}_clean.wav')
    noisy_file = os.path.join(noisy_dir, f'{base_identifier}_dirty.wav')
    filtered_file = os.path.join(filtered_dir, f'{base_identifier}_filtered.wav')
    vad_file = os.path.join(vad_dir, f'{base_identifier}_vad.wav')
    graph_file = os.path.join(plots_dir, f'{base_identifier}.png')

    # Copy files to the sample folder if they exist
    for file_path, label in [
        (clean_file, 'clean'),
        (noisy_file, 'noisy'),
        (filtered_file, 'filtered'),
        (vad_file, 'vad'),
        (graph_file, 'graph'),
    ]:
        if os.path.exists(file_path):
            dest_file = os.path.join(sample_dir, f'{base_identifier}_{label}.{"wav" if label != "graph" else "png"}')
            shutil.copy(file_path, dest_file)
        else:
            print(f"Warning: {label} file missing for {base_identifier}")

print("Files have been organized into the 'results' folder.")
