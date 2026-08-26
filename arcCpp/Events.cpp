#include "Events.hpp"
#include "map"
#include <functional>
#include <string>
#include "arcCpp/struct/Seq.hpp"
#include "arcCpp/struct/ObjectMap.hpp"
//Я не могу написать Seq, ObjectMap в один день с событиями.
namespace arc {

 arc::structures::ObjectMap<std::string, std::function<void()>> Events::eventsMa;

void Events::on(const std::function<void()>& callbackF, std::string name) {
eventsMa.put(name, callbackF);

    return;

};

void Events::fire(std::string name) {
auto event = eventsMa.get(name);
event();

};

void Events::remove(std::string name) {

eventsMa.remove(name);

};

}






