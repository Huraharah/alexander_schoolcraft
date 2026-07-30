# Define the root directory
$rootDir = "C:\Users\Administrator\OneDrive\School\11 Fall 2024\CSCI 4810 Digital Information Processing\project\archive" # Replace with the actual path to your 'archive' folder

# Define the testset folders
$cleanFolder = Join-Path $rootDir "clean_testset_wav"
$noisyFolder = Join-Path $rootDir "noisy_testset_wav"

# Function to append a suffix to filenames in a folder
Function Rename-Files {
    param (
        [string]$folderPath,
        [string]$suffix
    )

    # Get all .wav files in the folder
    Get-ChildItem -Path $folderPath -Filter "*.wav" | ForEach-Object {
        $originalName = $_.Name
        $baseName = [System.IO.Path]::GetFileNameWithoutExtension($originalName)
        $newName = "$baseName$suffix.wav"
        $newPath = Join-Path $folderPath $newName

        # Rename the file
        Rename-Item -Path $_.FullName -NewName $newPath
        Write-Host "Renamed: $originalName -> $newName"
    }
}

# Process clean files
Rename-Files -folderPath $cleanFolder -suffix "_clean"

# Process noisy files
Rename-Files -folderPath $noisyFolder -suffix "_dirty"

Write-Host "Renaming complete!"
