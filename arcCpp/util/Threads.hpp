#ifndef ARC_UTIL_THREADS_HPP
#define ARC_UTIL_THREADS_HPP

#include <thread>
#include <functional>



namespace arc::util {
    class Threads {
    public:
static void daemon(std::string name, std::function<void()> func);
    };
}

#endif