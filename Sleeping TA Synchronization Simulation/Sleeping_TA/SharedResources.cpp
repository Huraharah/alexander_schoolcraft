#include "SharedResources.h"
#include "StudentSync.h"
#include "Logger.h"
#include "SimulationConfig.h"

#include <stdexcept>

SharedResources::SharedResources(const SimulationConfig& cfg)
    : capacity_(cfg.numChairs()),
    numStudents_(cfg.numStudents()),
    sync_(cfg.numStudents()) {
    // workPermits_ is initialized to 0 in the header
}

bool SharedResources::enqueueIfSpace(int studentId) {
    std::scoped_lock lk(mtx_);
    if (queue_.size() == capacity_) return false;
    queue_.push(studentId);
    trackQueueHighWater_();
    workPermits_.release(); // wake TA (or buffer a permit)
    return true;
}

StudentSync& SharedResources::syncFor(int studentId) {
    return sync_.at(studentId);
}

void SharedResources::acquireWorkPermit() {
    workPermits_.acquire();
}

bool SharedResources::tryAcquireWorkPermit() {
    return workPermits_.try_acquire();
}

int SharedResources::dequeueStudent() {
    std::scoped_lock lk(mtx_);
    if (queue_.empty())
        return -1;
    int id = queue_.front();
    queue_.pop();
    return id;
}

bool SharedResources::hasWorkPermit() const {
    // Non-destructive peek
    auto* sem = const_cast<std::counting_semaphore<INT_MAX>*>(&workPermits_);
    if (sem->try_acquire()) { sem->release(); return true; }
    return false;
}

bool SharedResources::queueEmpty() const {
    std::scoped_lock lk(mtx_);
    return queue_.empty();
}

void SharedResources::nudgeTA() {
    workPermits_.release();
}

std::uint32_t SharedResources::queuedSize() const {
    std::scoped_lock lk(mtx_);
    return static_cast<std::uint32_t>(queue_.size());
}

void SharedResources::trackQueueHighWater_() {
    auto cur = static_cast<std::uint32_t>(queue_.size());
    auto old = maxQueueObserved_.load(std::memory_order_relaxed);
    while (cur > old && !maxQueueObserved_.compare_exchange_weak(old, cur)) {}
}
