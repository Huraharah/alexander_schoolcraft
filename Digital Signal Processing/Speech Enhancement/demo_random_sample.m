function demo_random_sample    
    rootDir = 'archive';

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
    randomIndex = 5;
    noisyFileName = files(randomIndex).name;
    baseName = replace(noisyFileName, '_dirty.wav', '');
    fprintf('Processing random file with Spectral Subtraction: %s\n', baseName);

    % Read noisy audio
    [noisyAudio, fs_noisy] = audioread(fullfile(noisyFolder, noisyFileName));
    if fs_noisy ~= fs_standard
        noisyAudio = resample(noisyAudio, fs_standard, fs_noisy);
    end

    % Estimate noise using the first 0.2 5 seconds
    noiseSegment = noisyAudio(1:round(0.25 * fs_standard)); % 0.25 seconds
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