#include "Events.hpp"
#include "map"
#include <functional>
#include <string>
namespace arc {
std::function<void()> Events::events[500];
std::string Events::names[500];
void Events::on(const std::function<void()>& callbackF, std::string name) {
    for (int i = 0; i < 500; i++) {
        if(!events[i]) {
            events[i] = callbackF;
            names[i] = name;
            return;
        }
    }
};
void Events::fire(std::string name) {
    int i = 0;
    for(std::string nameI : names) {

        if(name == nameI) {
            std::function<void()> event = events[i];
            event();
            return;
        }
        i++;
    }
};

}






