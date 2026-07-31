#include "Simulation.h"
#include "SimulationConfig.h"
#include "SharedResources.h"
#include "Student.h"
#include "TA.h"
#include "Logger.h"

#include <thread>
#include <vector>
#include <memory>

// ctor: default config
Simulation::Simulation()
    : config_(std::make_unique<SimulationConfig>()),
    shared_(nullptr) {
}

// ctor: custom config
Simulation::Simulation(const SimulationConfig& cfg)
    : config_(std::make_unique<SimulationConfig>(cfg)),
    shared_(nullptr) {
}

Simulation::~Simulation() = default;

void Simulation::run() {
	auto startTime = std::chrono::steady_clock::now(); // Setup time reference

    setup();
    startTA();
    startStudents();
    joinStudents();   // wait until all student threads finish
    shutdownTA();     // request TA shutdown (and wake if idle), then join

	auto endTime = std::chrono::steady_clock::now(); // End time reference
    auto wallMs =
        std::chrono::duration_cast<std::chrono::milliseconds>(endTime - startTime).count();

    printSummary(wallMs);
}

void Simulation::setup() {
    // Build shared state from the config (chairs, students, etc.)
    shared_ = std::make_unique<SharedResources>(*config_);

    // Optional logging cosmetics
    Logger::instance().setTimestamps(true);
    Logger::instance().setThreadIds(true);
}

void Simulation::startTA() {
    ta_ = std::make_unique<TA>(*shared_, *config_);
    taThread_ = std::thread(&TA::serviceLoop, ta_.get());
}

void Simulation::startStudents() {
    const int n = static_cast<int>(config_->numStudents());
    students_.reserve(n);
    studentThreads_.reserve(n);

    for (int i = 0; i < n; ++i) {
        students_.push_back(std::make_unique<Student>(i, *shared_, *config_));
        students_.back()->setName("Student-" + std::to_string(i));
        studentThreads_.emplace_back(&Student::run, students_.back().get());
    }
}

void Simulation::joinStudents() {
    for (auto& t : studentThreads_) {
        if (t.joinable()) t.join();
    }
}

void Simulation::shutdownTA() {
    // Tell TA to exit once the queue drains; also wake it if blocked
    shared_->requestShutdown();
    shared_->nudgeTA();
    if (taThread_.joinable()) taThread_.join();
}

void Simulation::printSummary(long long wallTimeMs) const {
    // Safe reads even if someone calls printSummary() early
    const auto students = config_ ? config_->numStudents() : 0;
    const auto chairs = config_ ? config_->numChairs() : 0;
    const auto maxQueue = shared_ ? shared_->maxQueueObserved() : 0;
    const auto qNow = shared_ ? shared_->queuedSize() : 0;
    const auto helps = (ta_ ? ta_->getHelpsServed() : 0ULL);
    const auto taIdleMs = (ta_ ? ta_->getTotalIdleDurationMs() : 0LL);
    double idlePct = 0.0;
    if (wallTimeMs > 0)
        idlePct = 100.0 * static_cast<double>(taIdleMs) / static_cast<double>(wallTimeMs);

    Logger& log = Logger::instance();
    log.info("===================================");
    log.info("            Simulation Summary     ");
    log.info("===================================");
    log.info("Students           : " + std::to_string(students));
    log.info("Chairs (capacity)  : " + std::to_string(chairs));
    log.info("Total helps served : " + std::to_string(helps));
    log.info("Max queue depth    : " + std::to_string(maxQueue));
    log.info("Queue at shutdown  : " + std::to_string(qNow));
    log.info("TA idle time (ms)  : " + std::to_string(taIdleMs));
	log.info("Wall time (ms)     : " + std::to_string(wallTimeMs));
    if (wallTimeMs > 0)
        log.info("TA idle percent    : " + std::to_string(idlePct) + "%");
}
