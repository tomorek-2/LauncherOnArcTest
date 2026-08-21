#include "Events.hpp"
#include "map"
#include <functional>
#include <string>
#include "arcCpp/struct/Seq.hpp"
//Я не могу написать Seq, ObjectMap в один день с событиями.
namespace arc {
std::function<void()> Events::events[500];
 arc::structures::Seq<std::function<void()>> eventsSeq;
std::string Events::names[500];
    arc::structures::Seq<std::string> stringSeq;
void Events::on(const std::function<void()>& callbackF, std::string name) {
   /* for (int i = 0; i < 500; i++) {
        if(names[i] == name) return;
        if(!events[i]) {
            events[i] = callbackF;
            names[i] = name;
            return;
        }
    } */
eventsSeq.add(callbackF);
stringSeq.add(name);
    return;
};
void Events::fire(std::string name) {


};

void Events::remove(std::string name) {
    int i = 0;
    for(std::string nameI : names) {

        if(name == nameI) {
            std::function<void()> event = events[i];
            event = {};
            names[i] = "";
            return;
        }
        i++;
    }


};

}






