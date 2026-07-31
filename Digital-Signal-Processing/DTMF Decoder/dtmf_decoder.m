function decodedSequence = dtmf_decoders(matFile)
    % Load the .mat file
    dataStruct = load(matFile);
    
    % Extract data and sample rate from the structure
    y = dataStruct.y;
    fs = dataStruct.fs;
    
    % Define segment parameters and DTMF frequency map
    segmentDuration = .5; % Adjust for optimal frequency resolution
    segmentSamples = round(segmentDuration * fs);
    
    % DTMF frequencies map
    dtmfFreqs = containers.Map( ...
        {'697_1209', '697_1336', '697_1477', '770_1209', '770_1336', ...
         '770_1477', '852_1209', '852_1336', '852_1477', '941_1209', ...
         '941_1336', '941_1477'}, ...
        {'1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '0', '#'});
    
    % Array to hold the detected digits
    decodedSequence = [];
    
    % Loop through each segment to detect tones
    for startIdx = 1:segmentSamples:length(y) - segmentSamples
        % Extract the current segment
        segment = y(startIdx:startIdx + segmentSamples - 1);
        
        % Apply Fourier Transform
        Y = fft(segment);
        f = (0:floor(length(Y)/2)-1) * (fs / length(Y)); % Positive frequencies
        magnitude = abs(Y(1:floor(length(Y)/2))); % Only positive half
        
        % Frequency ranges for DTMF low and high tones
        lowFreqRange = [697, 770, 852, 941];
        highFreqRange = [1209, 1336, 1477];
        
        % Find the dominant frequencies in the low and high range
        [lowFreq, highFreq] = find_dtmf_freqs(f, magnitude, lowFreqRange, highFreqRange);
        
        % Check if both frequencies match a DTMF key
        if lowFreq && highFreq
            freqKey = sprintf('%d_%d', lowFreq, highFreq);
            if isKey(dtmfFreqs, freqKey)
                decodedDigit = dtmfFreqs(freqKey);
                decodedSequence = [decodedSequence, decodedDigit];
            end
        end
    end
    disp(['Decoded sequence: ', decodedSequence]);
end

function [lowFreq, highFreq] = find_dtmf_freqs(f, magnitude, lowFreqs, highFreqs)
    % Initialize output frequencies
    lowFreq = 0; highFreq = 0;
    
    % Detect dominant low and high frequencies
    [~, idxLow] = max(arrayfun(@(x) max(magnitude(abs(f - x) < 10)), lowFreqs));
    [~, idxHigh] = max(arrayfun(@(x) max(magnitude(abs(f - x) < 10)), highFreqs));
    
    % Return the detected low and high frequencies
    if idxLow > 0
        lowFreq = lowFreqs(idxLow);
    end
    if idxHigh > 0
        highFreq = highFreqs(idxHigh);
    end
end

dtmf_decoders('dial_0.mat');
dtmf_decoders('dial_1.mat');
dtmf_decoders('dial_2.mat');
dtmf_decoders('dial_3.mat');
dtmf_decoders('dial_4.mat');
dtmf_decoders('dial_5.mat');
dtmf_decoders('dial_6.mat');
dtmf_decoders('dial_7.mat');
dtmf_decoders('dial_8.mat');
dtmf_decoders('dial_9.mat');