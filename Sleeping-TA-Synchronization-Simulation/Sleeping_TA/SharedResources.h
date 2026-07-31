#pragma once

#include <mutex>
#include <queue>
#include <vector>
#include <atomic>
#include <semaphore>
#include <cstdint>
#include <climits>

#include "StudentSync.h"

// Forward declare to avoid heavy includes
class SimulationConfig;

/**
 * Centralized shared state + synchronization.
 * Invariants:
 *  - queue_.size() <= capacity_
 *  - Each successful work-permit acquire by TA corresponds to exactly one queued student.
 *  - Enqueue + permit release happen atomically (under the same critical section).
 */
class SharedResources {
public:
    // Construct from config (reads numStudents, numChairs, etc.)
    explicit SharedResources(const SimulationConfig& cfg);

    SharedResources(const SharedResources&) = delete;
    SharedResources& operator=(const SharedResources&) = delete;
    SharedResources(SharedResources&&) = delete;
    SharedResources& operator=(SharedResources&&) = delete;

    // ---- Student-facing API ----

    // Try to sit in the hallway. If there is capacity:
    //   - enqueue studentId
    //   - release one work permit (so TA will wake or count it)
    // Returns true on success; false if full (no enqueue, no permit).
    bool enqueueIfSpace(int studentId);

    // Access per-student sync (stable index by id)
    StudentSync& syncFor(int studentId);

    // ---- TA-facing API ----

    // Block until a work permit is available (i.e., at least one enqueued student),
    // or until Simulation nudges during shutdown. Always acquires exactly one permit.
    void acquireWorkPermit();

    // Try to grab another permit without blocking (used to “drain” a batch).
    // Returns true if a permit was acquired.
    bool tryAcquireWorkPermit();

    // Pop the next student from the queue (FIFO). Must only be called
    // after the TA has acquired a permit. Returns studentId.
    int dequeueStudent();

    // Optional light checks (used only for early-exit or diagnostics; not for correctness)
    bool hasWorkPermit() const; // approximate: whether any permits are available “now”
    bool queueEmpty() const;

    // ---- Shutdown coordination ----

    bool isShutdownRequested() const noexcept { return shutdown_.load(std::memory_order_acquire); }
    void requestShutdown() noexcept { shutdown_.store(true, std::memory_order_release); }

    // Wake a potentially-blocked TA during shutdown (releases one permit).
    void nudgeTA();

    // ---- Metrics (optional; keep simple) ----
    std::uint32_t capacity() const noexcept { return capacity_; }
    std::uint32_t queuedSize() const;       // mutex-protected read
    std::uint32_t maxQueueObserved() const noexcept { return maxQueueObserved_.load(); }

private:
    // Update maxQueueObserved_ (call under lock when queue_ changes)
    void trackQueueHighWater_();

    // Configuration derived values
    std::uint32_t capacity_{ 0 };     // hallway chairs
    std::uint32_t numStudents_{ 0 };  // for sizing sync_

    // Queue + protection
    mutable std::mutex mtx_;
    std::queue<int>    queue_;

    // Per-student sync objects (indexed by student id)
    std::vector<StudentSync> sync_;

    // Work permits: counts enqueued students.
    // Starts at 0; student enqueue increments; TA acquires/decrements.
    std::counting_semaphore<INT_MAX> workPermits_{ 0 };

    // Shutdown flag set by Simulation after students finish
    std::atomic<bool> shutdown_{ false };

    // Metrics
    std::atomic<std::uint32_t> maxQueueObserved_{ 0 };
};
