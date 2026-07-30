#pragma once
#include <string>
#include <mutex>
#include <chrono>
#include <iosfwd>

class Logger {
public:
    static Logger& instance();                    // Meyers singleton

    // Basic API
    void setOutput(std::ostream* out);           // default: &std::cout
    void setPrefix(const std::string& pfx);      // optional global prefix
    void setTimestamps(bool on);                 // default: true
    void setThreadIds(bool on);                  // default: true
    void setVerbosity(int level);                // 0=silent, 1=info, 2=debug

    // Log lines (thread-safe). Level: 1=info, 2=debug
    void info(const std::string& msg, int level = 1);
    void debug(const std::string& msg);          // level=2

    // Convenience with component name (TA/Student N/etc.)
    void infoC(const std::string& comp, const std::string& msg, int level = 1);
    void debugC(const std::string& comp, const std::string& msg);

private:
    Logger();                                    // hidden
    std::string timestamp_() const;              // HH:MM:SS.mmm
    std::string threadId_() const;               // short hex string

private:
    std::ostream* out_;        // not owned; default &std::cout
    std::mutex     mtx_;
    std::string    prefix_;
    bool           showTs_{ true };
    bool           showTid_{ true };
    int            verbosity_{ 1 };
};
