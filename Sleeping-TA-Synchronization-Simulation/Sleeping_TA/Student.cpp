#include "Student.h"
#include "SharedResources.h"
#include "SimulationConfig.h"
#include "Logger.h"
#include <thread>
#include <chrono>
#include <utility>

Student::Student(int id, SharedResources& shared, const SimulationConfig& cfg)
    : id_(id), shared_(shared), cfg_(cfg) {
}

void Student::run() {
    try {
        const auto target = cfg_.maxHelpsPerStudent();
        while (helpsReceived_ < target) {
            thinkOnce();
            if (!tryEnqueue()) {
                std::this_thread::sleep_for(cfg_.randRetryBackoff());
                continue;
            }
            waitForHelpAndAcknowledge();
            ++helpsReceived_;
        }
    }
    catch (const std::exception& ex) {
        Logger::instance().infoC(name_, std::string("FATAL: unhandled exception: ") + ex.what());
    }
    catch (...) {
        Logger::instance().infoC(name_, "FATAL: unhandled non-std exception");
    }
    
}

void Student::thinkOnce() {
    std::this_thread::sleep_for(cfg_.randThinkDuration());
}

bool Student::tryEnqueue() {
    const bool ok = shared_.enqueueIfSpace(id_);
    if (ok) {
        Logger::instance().infoC(name_, "sat in hallway (enqueued)");
    }
    else {
        Logger::instance().infoC(name_, "hallway full; will retry", /*level=*/2);
    }
    return ok;
}

void Student::waitForHelpAndAcknowledge() {
    auto& sync = shared_.syncFor(id_);
    Logger::instance().infoC(name_, "waiting for TA");
    sync.waitForStart();              // TA invites me
    // TA is "helping" during this window (their own sleep)
    Logger::instance().infoC(name_, "help in progress", /*level=*/2);
    sync.signalDone();                // acknowledge completion
    Logger::instance().infoC(name_, "finished; leaving hallway");
}

void Student::setName(std::string name) { name_ = std::move(name); }
const std::string& Student::getName() const noexcept { return name_; }
int  Student::id() const noexcept { return id_; }
std::uint32_t Student::helpsReceived() const noexcept { return helpsReceived_; }
