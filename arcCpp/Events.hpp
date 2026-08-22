#ifndef ARC_EVENTS_HPP
#define ARC_EVENTS_HPP
#include <functional>
#include <string>
#include "arcCpp/struct/ObjectMap.hpp"
namespace arc {
    class Events {

    public:
      static  void on(const std::function<void()>& callbackF, std::string);
      static  arc::structures::ObjectMap<std::string, std::function<void()>> eventsMa;
        static void fire(std::string name);
static void remove(std::string name);
    private:

        Events() = delete;
        ~Events() = delete;

    };
}


#endif

