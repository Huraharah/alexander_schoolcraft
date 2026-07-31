#pragma once

#include <string>
#include <cstdint>

// Forward declarations
class SharedResources;
class SimulationConfig;

/**
 * Student thread:
 * - Repeats: think → try to sit/enqueue → wait to be helped → acknowledge done
 * - Terminates after exactly maxHelpsPerStudent (from config).
 *
 * Concurrency policy:
 * - Never touches raw semaphores or queue directly; goes through SharedResources.
 */
class Student {
public:
    Student(int id, SharedResources& shared, const SimulationConfig& cfg);

    Student(const Student&) = delete;
    Student& operator=(const Student&) = delete;
    Student(Student&&) = delete;
    Student& operator=(Student&&) = delete;

    // Entry point for the std::thread
    void run();

    // Optional identity helpers
    void setName(std::string name);
    const std::string& getName() const noexcept;
    int  id() const noexcept;

    // Metrics
    std::uint32_t helpsReceived() const noexcept;

private:
    // One “programming” pause
    void thinkOnce();

    // Try to take a chair: returns true if enqueued (and implicitly released a work permit)
    bool tryEnqueue();

    // Block until TA picks me; then signal completion so TA can move on
    void waitForHelpAndAcknowledge();

private:
    int                 id_;
    SharedResources& shared_;
    const SimulationConfig& cfg_;
    std::string         name_{ "Student" };
    std::uint32_t       helpsReceived_{ 0 };
};
