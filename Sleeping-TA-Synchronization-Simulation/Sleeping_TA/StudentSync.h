#pragma once

#include <semaphore>
#include <string>
#include <cstdint>

/**
 * Per-student synchronization.
 * TA ↔ Student handshake:
 *   TA:   signalStart()  → (student unblocks)
 *   Stud: waitForStart() → ...then signalDone()
 *   TA:   waitDone()     → proceed to next
 *
 * Both semaphores start at 0.
 */
class StudentSync {
public:
    StudentSync() = default;

    StudentSync(const StudentSync&) = delete;
    StudentSync& operator=(const StudentSync&) = delete;
    StudentSync(StudentSync&&) = delete;
    StudentSync& operator=(StudentSync&&) = delete;

    // Called by TA
    void signalStart(); // release helpStart
    void waitDone();    // acquire helpDone

    // Called by Student
    void waitForStart(); // acquire helpStart
    void signalDone();   // release helpDone

private:
    // Binary semaphores (0/1). Start at 0 so both sides block until signaled.
    std::counting_semaphore<1> helpStart_{ 0 };
    std::counting_semaphore<1> helpDone_{ 0 };
};
