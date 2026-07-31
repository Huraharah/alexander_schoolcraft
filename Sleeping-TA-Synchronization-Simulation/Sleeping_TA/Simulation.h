#pragma once
#include <vector>
#include <thread>
#include <memory>

// Forward declarations to avoid heavy includes in the header
class SimulationConfig;
class SharedResources;
class Student;
class TA;

class Simulation {
public:
    // Constructors
    Simulation();                                // default sensible config
    explicit Simulation(const SimulationConfig&); // user-provided config

    // Main entrypoint
    void run(); // Orchestrates full lifecycle

    // Optional convenience
    void printSummary(long long wallTimeMs = -1) const;

	// Destructor
    ~Simulation();

private:
    // --- Setup / teardown ---
    void setup();               // allocate SharedResources, seed RNGs, etc.
    void startTA();             // create TA object + thread
    void startStudents();       // create Student objects + threads
    void joinStudents();        // join all student threads
    void shutdownTA();          // signal shutdown + poke TA + join TA

private:
    // Core owned components
    std::unique_ptr<SimulationConfig> config_;
    std::unique_ptr<SharedResources>  shared_;

    // Entities
    std::unique_ptr<TA>               ta_;
    std::vector<std::unique_ptr<Student>> students_;

    // Threads
    std::thread taThread_;
    std::vector<std::thread> studentThreads_;
};
