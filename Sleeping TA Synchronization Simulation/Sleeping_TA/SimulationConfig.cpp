#include "SimulationConfig.h"
#include <algorithm>

SimulationConfig::SimulationConfig()
    : rng_(std::random_device{}()), seeded_(true) {
    lastSeed_ = rng_();
}

SimulationConfig::SimulationConfig(std::uint32_t seed)
    : rng_(seed), seeded_(true), lastSeed_(seed) {
}

SimulationConfig::ms SimulationConfig::randThinkDuration() const {
    return ms{ randInRange_(thinkLo_, thinkHi_) };
}
SimulationConfig::ms SimulationConfig::randHelpDuration() const {
    return ms{ randInRange_(helpLo_, helpHi_) };
}
SimulationConfig::ms SimulationConfig::randRetryBackoff() const {
    return ms{ randInRange_(retryLo_, retryHi_) };
}

void SimulationConfig::reseed(std::uint32_t seed) const {
    rng_.seed(seed);
    seeded_ = true;
    lastSeed_ = seed;
}

std::uint32_t SimulationConfig::randInRange_(std::uint32_t lo, std::uint32_t hi) const {
    if (hi < lo) std::swap(lo, hi);
    std::uniform_int_distribution<std::uint32_t> dist(lo, hi);
    return dist(rng_);
}
