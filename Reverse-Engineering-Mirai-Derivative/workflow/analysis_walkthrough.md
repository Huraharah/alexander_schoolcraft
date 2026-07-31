# Analysis Walkthrough

This document summarizes the methodology used during the live reverse engineering demonstration. The workflow focuses on observing malware behavior within an isolated analysis environment before transitioning into dynamic debugging with GDB.

## Environment Setup

- Configure isolated host-only network between analysis VMs.
- Start INetSim to emulate common Internet services.
- Verify contained connectivity using `curl http://example.com`.
- Confirm that the malware cannot communicate with the public Internet.

## Baseline Observation

- Record initial process list (`ps aux`)
- Record initial filesystem contents (`ls`)
- Verify the malware sample is present
- Record post-exec process list and filesystem contents for differential analysis

## Runtime Analysis

- Execute the malware sample.
- Compare process list and filesystem state before and after execution.
- Observe self-deletion behavior.
- Observe randomized process names.
- Demonstrate anti-analysis behavior by executing common utilities (`strings`, `pkill`, `chmod`, `strace`, etc.).
- Restore the analysis environment using `reset_lab.sh`.

## Active Debugging

Execute the malware through the custom GDB demonstration script (`gdb_analysis_script.gdb`) and examine:

- Process entry point
- Dynamic memory mappings (`mmap`)
- Memory permission changes (`mprotect`)
- Memory cleanup (`munmap`)
- Socket-related system calls
- Process forking behavior
- Self-deletion (`unlink`)

## Additional Analysis

Additional static analysis included entropy measurements of the malware's execution stages.

Observed characteristics included:

- Stage 1: High entropy consistent with encrypted or packed data.
- Stage 2: Small unpacking loader with comparatively low entropy.
- Stage 3: Moderate entropy consistent with executable machine code after unpacking.