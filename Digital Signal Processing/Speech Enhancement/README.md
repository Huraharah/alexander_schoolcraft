# Speech Enhancement Using Digital Signal Processing

A MATLAB implementation of a speech enhancement pipeline developed for **CSCI 4810 – Digital Signal Processing**.

The project investigates classical digital signal processing techniques for improving speech intelligibility in noisy recordings. Beginning with paired noisy and clean speech samples, the pipeline applies a bandpass filter followed by spectral subtraction in the frequency domain to reduce background noise while preserving the primary speech signal.

The implementation was completed as the semester project for the course and is accompanied by a formal technical report.

---

## Project Overview

The objective was to improve the intelligibility of noisy speech recordings using traditional digital signal processing techniques.

The processing pipeline consists of:

1. Read paired noisy and clean speech samples
2. Resample all audio to a common sampling rate (16 kHz)
3. Apply a 4th-order Butterworth bandpass filter
4. Compute the Fast Fourier Transform (FFT)
5. Perform spectral subtraction using estimated background noise
6. Reconstruct the signal using the inverse FFT
7. Normalize the enhanced waveform
8. Save the processed audio and comparison plots

The enhanced output is then compared directly against both the original noisy recording and the clean reference sample.

---

## Processing Pipeline

```text
Noisy Speech
      │
      ▼
Resample (16 kHz)
      │
      ▼
Butterworth Bandpass Filter
      │
      ▼
Fast Fourier Transform (FFT)
      │
      ▼
Spectral Subtraction
      │
      ▼
Inverse FFT
      │
      ▼
Normalization
      │
      ▼
Enhanced Speech
```

---

## Sample Dataset

The original project evaluated **824 paired speech samples** consisting of:

- Clean speech
- Noisy speech

Each processed sample additionally generated:

- Bandpass filtered speech
- Enhanced speech
- Waveform comparison plot

To keep this repository lightweight, a representative subset of **20 processed samples** is included.

Each sample folder contains:

```text
p232_001/
├── p232_001_noisy.wav
├── p232_001_filtered.wav
├── p232_001_enhanced.wav
├── p232_001_clean.wav
└── p232_001.png
```

The waveform comparison image allows the original, filtered, enhanced, and clean signals to be compared visually.

---

## Included Files

```text
Speech-Enhancement/

README.md

Speech Enhancement Report.pdf

speech_enhancement.m

sort_results.py

Sample Results/
    p232_001/
    p232_002/
    ...
    p232_020/
```

The included Python utility was written after processing completed to automatically organize the generated audio files and waveform plots into a consistent directory structure for analysis and presentation.

---

## Techniques Demonstrated

- Digital Signal Processing (DSP)
- Fast Fourier Transform (FFT)
- Inverse FFT
- Butterworth Bandpass Filtering
- Spectral Subtraction
- Audio Resampling
- Speech Enhancement
- Waveform Visualization

---

## Technologies

`MATLAB`

`Python`

`Digital Signal Processing`

`FFT`

`Signal Filtering`

`Audio Processing`

---

## Results

The filtering pipeline substantially improves speech intelligibility for many noisy recordings.

The initial bandpass filter removes significant out-of-band noise while preserving the primary speech frequencies. Spectral subtraction further suppresses background conversational noise, producing speech that more closely resembles the original clean recording.

Although the enhanced recordings are not identical to the clean reference signals, the resulting speech is generally much easier to understand.

---

## Technical Report

A complete description of the implementation, filtering methods, dataset, evaluation process, and discussion of results is included in:

**Speech Enhancement Report.pdf**

---

## Course Context

This project was completed for **CSCI 4810 – Digital Signal Processing**.

The assignment required identifying a meaningful digital signal processing problem, implementing a solution in MATLAB, evaluating the results, writing a formal technical report, and presenting the completed project.

---

## Note

The original evaluation dataset contained over 800 paired speech samples. A representative subset of the processed results is included here to demonstrate the implementation while keeping the repository size manageable.