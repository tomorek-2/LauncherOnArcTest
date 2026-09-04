#include "Threads.hpp"
#include <thread>
#include <functional>
#include <string>
#include "arcCpp/struct/Seq.hpp"
namespace arc::util {
    //std::function<void()> orderFunc[100];

    void Threads::daemon(std::string name, const std::function<void()> func) {
        std::thread thread(func);





        thread.detach();
    };
/*void Threads::executor(std::function<void()>) {

};*/
}

