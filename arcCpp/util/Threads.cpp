#include "Threads.hpp"
#include <thread>
#include <functional>
#include <string>
namespace arc::util {


    void Threads::daemon(std::string name, const std::function<void()> func) {
        std::thread thread(func);





        thread.detach();
    };

}

