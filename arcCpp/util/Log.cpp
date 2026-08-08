#include "Log.hpp"
#include <iostream>

namespace arc::util {


    void Log::log(const std::string& str) {
        std::cerr  << str << "\n" << std::flush;
    }
}