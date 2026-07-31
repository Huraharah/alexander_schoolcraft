#pragma once

#include <string>
#include <atomic>
#include <cstdint>
#include <chrono>

// Forward declarations to keep compile times down
class SharedResources;
class SimulationConfig;

/**
 * TA: owns no synchronization primitives itself; it orchestrates service by
 * cooperating with SharedResources (queue + semaphores + per-student sync).
 *
 * Lifecycle (serviceLoop):
 *  - Block on helpRequests (counting semaphore) while no students wait.
 *  - Dequeue next student (FIFO) under shared queue mutex.
 *  - Signal that student’s helpStart; simulate help; wait for helpDone.
 *  - Repeat until shutdown is requested and the queue drains.
 */
class TA {
public:
    // Construct with references to shared state and immutable config.
    TA(SharedResources& shared, const SimulationConfig& cfg);

    // Non-copyable, non-movable
    TA(const TA&) = delete;
    TA& operator=(const TA&) = delete;
    TA(TA&&) = delete;
    TA& operator=(TA&&) = delete;

    // Main loop to run on the TA thread.
    void serviceLoop();

    // Optional: assign a friendly name for logging/diagnostics.
    void setName(std::string name);

	// Statistics getters
    std::uint64_t getHelpsServed() const;
    int64_t getTotalIdleDurationMs() const;
    std::string getName() const;

private:
    // ---- Core helpers (called only inside serviceLoop) ----

    // Blocks (or returns false if shutdown) until at least one student is waiting.
    // If it returns true, TA should dequeue and serve as many as available.
    bool waitForWorkOrShutdown();

    // Pops the next student ID from the waiting queue (must be called only when
    // work is known to be available). Returns -1 if none (shouldn’t happen if used correctly).
    int dequeueNextStudent();

    // Conduct a single help interaction with a specific student:
    //  - Signal student's helpStart
    //  - Sleep for a randomized "help" duration from config
    //  - Wait for student's helpDone
    void helpOneStudent(int studentId);

    // Returns true if a global shutdown has been requested.
    bool isShutdownRequested() const;

    // ---- Optional instrumentation (can be no-ops if you skip metrics) ----
    void onIdleStart();
    void onIdleEnd();
    void onHelpStart(int studentId);

    // ---- Data members used by TA.cpp ----
    SharedResources& shared_;
    const SimulationConfig& config_;
    std::uint64_t                 helpsServed_;
    std::string                   name_;
    std::chrono::steady_clock::time_point idleStartTime_{};
    int64_t                       totalIdleDuration_;
};
