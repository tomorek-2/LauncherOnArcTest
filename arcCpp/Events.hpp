#ifndef ARC_EVENTS_HPP
#define ARC_EVENTS_HPP
#include <functional>
#include <string>
namespace arc {
    class Events {

    public:
      static  void on(const std::function<void()>& callbackF, std::string);
       static std::function<void()> events[500];
        static void fire(std::string name);
static void remove(std::string name);
    private:
       static std::string names[500];
        Events() = delete;
        ~Events() = delete;

    };
}


#endif

