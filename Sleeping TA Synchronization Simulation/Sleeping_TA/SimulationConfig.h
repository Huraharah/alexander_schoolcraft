#pragma once

#include <chrono>
#include <cstdint>
#include <random>

class SimulationConfig {
public:
    using ms = std::chrono::milliseconds;

    // ---- Construction ----
    SimulationConfig();                              // sane defaults
    explicit SimulationConfig(std::uint32_t seed);   // deterministic runs

    // ---- Core parameters (getters/setters) ----
    std::uint32_t numStudents()         const noexcept { return numStudents_; }
    std::uint32_t numChairs()           const noexcept { return numChairs_; }
    std::uint32_t maxHelpsPerStudent()  const noexcept { return maxHelpsPerStudent_; }

    void setNumStudents(std::uint32_t n)        noexcept { numStudents_ = n; }
    void setNumChairs(std::uint32_t n)          noexcept { numChairs_ = n; }
    void setMaxHelpsPerStudent(std::uint32_t n) noexcept { maxHelpsPerStudent_ = n; }

    // Think (student programming) time range
    void setThinkRangeMs(std::uint32_t lo, std::uint32_t hi) noexcept { thinkLo_ = lo; thinkHi_ = hi; }
    void setHelpRangeMs(std::uint32_t lo, std::uint32_t hi) noexcept { helpLo_ = lo; helpHi_ = hi; }
    void setRetryRangeMs(std::uint32_t lo, std::uint32_t hi) noexcept { retryLo_ = lo; retryHi_ = hi; }

    // ---- Randomized durations (used by Student/TA) ----
    ms randThinkDuration()  const;   // e.g., 200–800 ms
    ms randHelpDuration()   const;   // e.g., 300–900 ms
    ms randRetryBackoff()   const;   // e.g., 30–120 ms

    // ---- RNG control ----
    void reseed(std::uint32_t seed) const;  // for deterministic tests

private:
    // Counts / capacities
    std::uint32_t numStudents_{ 6 };
    std::uint32_t numChairs_{ 3 };
    std::uint32_t maxHelpsPerStudent_{ 2 };

    // Ranges in milliseconds (inclusive)
    std::uint32_t thinkLo_{ 200 }, thinkHi_{ 800 };
    std::uint32_t helpLo_{ 300 }, helpHi_{ 900 };
    std::uint32_t retryLo_{ 30 }, retryHi_{ 120 };

    // RNG (mutable to allow const random helpers)
    mutable std::mt19937 rng_;
    mutable bool seeded_{ false };
    mutable std::uint32_t lastSeed_{ 0 };

    // Helper: returns integer in [lo, hi]
    std::uint32_t randInRange_(std::uint32_t lo, std::uint32_t hi) const;
};
