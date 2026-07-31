#include "Logger.h"
#include <iostream>
#include <iomanip>
#include <thread>
#include <sstream>
#include <chrono>
#include <mutex>
#include <string>
#include <cstdint>

// Thread-safe singleton logger.
Logger& Logger::instance() {
    static Logger logger;
    return logger;
}

Logger::Logger() : out_(&std::cout) {}

void Logger::setOutput(std::ostream* out) {
    std::lock_guard<std::mutex> lk(mtx_);
    out_ = out ? out : &std::cout;
}

void Logger::setPrefix(const std::string& pfx) {
    std::lock_guard<std::mutex> lk(mtx_);
    prefix_ = pfx;
}

void Logger::setTimestamps(bool on) {
    std::lock_guard<std::mutex> lk(mtx_);
    showTs_ = on;
}

void Logger::setThreadIds(bool on) {
    std::lock_guard<std::mutex> lk(mtx_);
    showTid_ = on;
}

void Logger::setVerbosity(int level) {
    std::lock_guard<std::mutex> lk(mtx_);
    verbosity_ = level;
}

void Logger::info(const std::string& msg, int level) {
    if (level > verbosity_) return;
    std::lock_guard<std::mutex> lk(mtx_);
    if (showTs_)  (*out_) << "[" << timestamp_() << "] ";
    if (showTid_) (*out_) << "[TID " << threadId_() << "] ";
    if (!prefix_.empty()) (*out_) << prefix_ << " ";
    (*out_) << msg << std::endl;
}

void Logger::debug(const std::string& msg) {
    info(msg, 2);
}

void Logger::infoC(const std::string& comp, const std::string& msg, int level) {
    info("[" + comp + "] " + msg, level);
}

void Logger::debugC(const std::string& comp, const std::string& msg) {
    debug("[" + comp + "] " + msg);
}

// --- private helpers ---
std::string Logger::timestamp_() const {
    using clock = std::chrono::system_clock;
    auto now = clock::now();
    auto t = clock::to_time_t(now);
    auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(now.time_since_epoch()) % 1000;
    std::tm tm{};
#if defined(_WIN32)
    localtime_s(&tm, &t);
#else
    localtime_r(&t, &tm);
#endif
    std::ostringstream oss;
    oss << std::setfill('0') << std::setw(2) << tm.tm_hour << ":"
        << std::setw(2) << tm.tm_min << ":"
        << std::setw(2) << tm.tm_sec << "."
        << std::setw(3) << static_cast<int>(ms.count());
    return oss.str();
}

std::string Logger::threadId_() const {
    std::ostringstream oss;
    oss << std::hex << std::uppercase << std::hash<std::thread::id>{}(std::this_thread::get_id());
    return oss.str();
}
