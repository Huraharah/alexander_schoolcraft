#include "TA.h"
#include "SharedResources.h"
#include "StudentSync.h"
#include "SimulationConfig.h"
#include "Logger.h"
#include <thread>
#include <chrono>
#include <string>

TA::TA(SharedResources& shared, const SimulationConfig& cfg)
    : shared_(shared),
    config_(cfg),
    helpsServed_(0),
    name_("TA"),
    totalIdleDuration_(0) {
}

void TA::serviceLoop() {
    try {
        for (;;) {
            onIdleStart();
            if (!waitForWorkOrShutdown()) break; // blocks for a permit (or exits)
            onIdleEnd();

            // Serve the student for the permit we just acquired
            {
                int id = dequeueNextStudent();
                helpOneStudent(id);
            }
            // Drain any queued permits that arrived mid-help
            while (shared_.tryAcquireWorkPermit()) {
                int id = dequeueNextStudent();
				if (id < 0) break; // sanity check
                helpOneStudent(id);
            }
        }
        Logger::instance().infoC(name_, "exiting; total helps=" + std::to_string(helpsServed_));
    }
    catch (const std::exception& ex) {
        Logger::instance().infoC(name_, std::string("FATAL: unhandled exception: ") + ex.what());
    }
    catch (...) {
        Logger::instance().infoC(name_, "FATAL: unhandled non-std exception");
    }
}

bool TA::waitForWorkOrShutdown() {
    // Fast-exit if shutting down AND no work counted
    if (isShutdownRequested() && !shared_.hasWorkPermit())
        return false;

    shared_.acquireWorkPermit(); // may block

    // If we were only nudged during shutdown and there’s no real work, exit
    if (isShutdownRequested() && shared_.queueEmpty())
        return false;

    return true;
}

int TA::dequeueNextStudent() {
    return shared_.dequeueStudent();
}

void TA::helpOneStudent(int studentId) {
    if (studentId < 0) {
        return;
	}
    auto& sync = shared_.syncFor(studentId);
    Logger::instance().infoC(name_, "inviting Student " + std::to_string(studentId));
    onHelpStart(studentId);

    sync.signalStart();                          // let the student in
    std::this_thread::sleep_for(config_.randHelpDuration()); // simulate help
    sync.waitDone();                             // student acknowledges

    ++helpsServed_;
    Logger::instance().debugC(name_, "finished Student " + std::to_string(studentId));
}

bool TA::isShutdownRequested() const {
    return shared_.isShutdownRequested();
}

void TA::setName(std::string name) { name_ = std::move(name); }

// --- instrumentation (private) ---
void TA::onIdleStart() {
    Logger::instance().infoC(name_, "no students; taking a nap");
    idleStartTime_ = std::chrono::steady_clock::now();
}

void TA::onIdleEnd() {
    if (idleStartTime_.time_since_epoch().count() == 0) return;
    Logger::instance().infoC(name_, "woken up");
    auto end = std::chrono::steady_clock::now();
    totalIdleDuration_ += std::chrono::duration_cast<std::chrono::milliseconds>(end - idleStartTime_).count();
    idleStartTime_ = {}; // reset
}

void TA::onHelpStart(int /*studentId*/) {
    // (hook for detailed metrics; logs kept in helpOneStudent)
}

std::uint64_t TA::getHelpsServed() const { return helpsServed_; }
int64_t TA::getTotalIdleDurationMs() const { return totalIdleDuration_; }
std::string TA::getName() const { return name_; }
