% Include Spectral Subtraction-based noise suppression
function demo_random_sample(rootDir)
    if nargin < 1
        rootDir = 'archive'; % Default root directory
    end

    % Define dataset paths
    noisyFolder = fullfile(rootDir, 'noisy_testset_wav');
    cleanFolder = fullfile(rootDir, 'clean_testset_wav');

    % Bandpass filter parameters
    low_cutoff = 300;
    high_cutoff = 3400;
    fs_standard = 16000; % Expected sample rate
    [b, a] = butter(4, [low_cutoff, high_cutoff] / (fs_standard / 2), 'bandpass');

    % Select a random file
    files = dir(fullfile(noisyFolder, '*.wav'));
    if isempty(files)
        error('No .wav files found in the noisy folder.');
    end
    randomIndex = randi(length(files));
    noisyFileName = files(randomIndex).name;
    baseName = replace(noisyFileName, '_dirty.wav', '');
    fprintf('Processing random file with Spectral Subtraction: %s\n', baseName);

    % Read noisy audio
    [noisyAudio, fs_noisy] = audioread(fullfile(noisyFolder, noisyFileName));
    if fs_noisy ~= fs_standard
        noisyAudio = resample(noisyAudio, fs_standard, fs_noisy);
    end

    % Estimate noise using the first 0.25 seconds
    noiseSegment = noisyAudio(1:round(0.25 * fs_standard)); % 0.25 seconds
    noiseSpectrum = mean(abs(fft(noiseSegment)));

    % Apply bandpass filter
    filteredAudio = filtfilt(b, a, noisyAudio);

    % Apply spectral subtraction
    audioSpectrum = abs(fft(filteredAudio));
    enhancedSpectrum = max(audioSpectrum - noiseSpectrum, 0);
    enhancedAudio = real(ifft(enhancedSpectrum));

    % Read clean audio
    cleanFileName = [baseName '_clean.wav'];
    [cleanAudio, fs_clean] = audioread(fullfile(cleanFolder, cleanFileName));
    if fs_clean ~= fs_standard
        cleanAudio = resample(cleanAudio, fs_standard, fs_clean);
    end

    % Synchronize lengths
    minLength = min([length(noisyAudio), length(filteredAudio), length(cleanAudio)]);
    noisyAudio = noisyAudio(1:minLength);
    filteredAudio = filteredAudio(1:minLength);
    enhancedAudio = enhancedAudio(1:minLength);
    cleanAudio = cleanAudio(1:minLength);
    time = (0:minLength-1) / fs_standard;

    % Plot results
    figure;
    subplot(4, 1, 1);
    plot(time, noisyAudio);
    title('Noisy Signal');
    xlabel('Time (s)');
    ylabel('Amplitude');

    subplot(4, 1, 2);
    plot(time, filteredAudio);
    title('Filtered Signal');
    xlabel('Time (s)');
    ylabel('Amplitude');

    subplot(4, 1, 3);
    plot(time, enhancedAudio);
    title('Enhanced Signal');
    xlabel('Time (s)');
    ylabel('Amplitude');

    subplot(4, 1, 4);
    plot(time, cleanAudio);
    title('Clean Signal');
    xlabel('Time (s)');
    ylabel('Amplitude');

    % Play audio
    disp('Playing noisy audio...');
    sound(noisyAudio, fs_standard);
    pause(length(noisyAudio)/fs_standard + 1);

    disp('Playing filtered audio...');
    sound(filteredAudio, fs_standard);
    pause(length(filteredAudio)/fs_standard + 1);

    disp('Playing enhanced audio...');
    sound(enhancedAudio, fs_standard);
    pause(length(enhancedAudio)/fs_standard + 1);

    disp('Playing clean audio...');
    sound(cleanAudio, fs_standard);
end

% Define dataset paths
rootDir = 'C:\Users\Administrator\OneDrive\School\11 Fall 2024\CSCI 4810 Digital Information Processing\project\archive';
noisyFolder = fullfile(rootDir, 'noisy_testset_wav');
cleanFolder = fullfile(rootDir, 'clean_testset_wav');
outputDir = 'processed_data/noisy_testset_filtered';
outputDirEnhanced = 'processed_data/noisy_testset_enhanced';
plotDir = 'plots';
resultsDir = './results'; % Final organized folder

% Create output directories if they don't exist
if ~exist(outputDir, 'dir')
    mkdir(outputDir);
end
if ~exist(outputDirEnhanced, 'dir')
    mkdir(outputDirEnhanced);
end
if ~exist(plotDir, 'dir')
    mkdir(plotDir);
end
if ~exist(resultsDir, 'dir')
    mkdir(resultsDir);
end

% Bandpass filter parameters
low_cutoff = 300;
high_cutoff = 3400;
fs_standard = 16000; % Expected sample rate
[b, a] = butter(4, [low_cutoff, high_cutoff] / (fs_standard / 2), 'bandpass');

% Process files
noisyFiles = dir(fullfile(noisyFolder, '*.wav'));

for j = 1:length(noisyFiles)
    noisyFileName = noisyFiles(j).name;
    baseName = replace(noisyFileName, '_dirty.wav', '');

    fprintf('Processing file: %s\n', baseName);

    % Read noisy audio
    [noisyAudio, fs_noisy] = audioread(fullfile(noisyFolder, noisyFileName));
    if fs_noisy ~= fs_standard
        noisyAudio = resample(noisyAudio, fs_standard, fs_noisy);
    end

    % Estimate noise from the first 0.25 seconds
    noiseSegment = noisyAudio(1:round(0.1 * fs_standard));
    noiseSpectrum = mean(abs(fft(noiseSegment)));

    % Apply bandpass filter
    filteredAudio = filtfilt(b, a, noisyAudio);

   % Compute FFT of the noisy signal
   N = length(filteredAudio);
   audioFFT = fft(filteredAudio);

   % Compute the power spectrum
   audioPower = abs(audioFFT).^2 / N;

   % Compute the noise power spectrum
   noiseFFT = fft(noiseSegment, N); % Zero-pad noise segment to match signal length
   noisePower = abs(noiseFFT).^2 / N;

   % Perform spectral subtraction
   enhancedPower = max(audioPower - noisePower, 0); % Ensure no negative values

   % Apply smoothing to reduce artifacts
   alpha = 0.8; % Smoothing factor
   smoothedPower = alpha * noisePower + (1 - alpha) * enhancedPower;

   % Reconstruct the enhanced spectrum
   enhancedFFT = sqrt(smoothedPower) .* exp(1j * angle(audioFFT));

   % Compute the inverse FFT to get the enhanced signal
   enhancedAudio = real(ifft(enhancedFFT));

   % Normalize the output signal
   enhancedAudio = enhancedAudio / max(abs(enhancedAudio));

    % Read clean audio
    cleanFileName = [baseName '_clean.wav'];
    [cleanAudio, fs_clean] = audioread(fullfile(cleanFolder, cleanFileName));
    if fs_clean ~= fs_standard
        cleanAudio = resample(cleanAudio, fs_standard, fs_clean);
    end

    % Save processed files
    filteredOutputFile = fullfile(outputDir, [baseName '_filtered.wav']);
    audiowrite(filteredOutputFile, filteredAudio, fs_standard);
    enhancedOutputFile = fullfile(outputDirEnhanced, [baseName '_enhanced.wav']);
    audiowrite(enhancedOutputFile, enhancedAudio, fs_standard);

    % Save plot
    time = (0:length(noisyAudio)-1) / fs_standard;
    plotFileName = fullfile(plotDir, [baseName '.png']);
    figure;
    subplot(4, 1, 1); plot(time, noisyAudio); title('Noisy Signal'); xlabel('Time (s)'); ylabel('Amplitude');
    subplot(4, 1, 2); plot(time, filteredAudio); title('Filtered Signal'); xlabel('Time (s)'); ylabel('Amplitude');
    subplot(4, 1, 3); plot(time, enhancedAudio); title('Enhanced Signal'); xlabel('Time (s)'); ylabel('Amplitude');
    subplot(4, 1, 4); plot(time, cleanAudio); title('Clean Signal'); xlabel('Time (s)'); ylabel('Amplitude');
    saveas(gcf, plotFileName);
    close(gcf);

    % Organize files into results folder
    sampleDir = fullfile(resultsDir, baseName);
    if ~exist(sampleDir, 'dir')
        mkdir(sampleDir);
    end
    copyfile(filteredOutputFile, fullfile(sampleDir, [baseName '_filtered.wav']));
    copyfile(enhancedOutputFile, fullfile(sampleDir, [baseName '_enhanced.wav']));
    copyfile(fullfile(noisyFolder, noisyFileName), fullfile(sampleDir, [baseName '_noisy.wav']));
    copyfile(fullfile(cleanFolder, cleanFileName), fullfile(sampleDir, [baseName '_clean.wav']));
    copyfile(plotFileName, fullfile(sampleDir, [baseName '.png']));
end

disp('Processing and sorting complete!');
