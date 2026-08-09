#include "Log.hpp"
#include <iostream>
//Что же, переписать Anuken/Arc будет не просто, но для меня будет большой опыт, если я не навайбкодю решение.
namespace arc::util {

    int Log::logLevel = 5; //Логика: 1. err. 2. warn. 3. debug. 4. info. 5. none. //0. Отключить деревья (log)

    void Log::log(const std::string &str) {

        if (arc::util::Log::logLevel >= 5) {
            std::cerr << str << "\n"<<"\033[39m"<< std::flush;
        }
        }
        void Log::warn(const std::string &str) {

            if (arc::util::Log::logLevel >= 2) {
                std::cerr << str << "\n"<<"\033[39m"<< std::flush;
            }
        }
    void Log::err(const std::string &str) {

        if (arc::util::Log::logLevel >= 1) {
            std::cerr << str << "\n"<<"\033[39m"<< std::flush;
        }
    }
    void Log::debug(const std::string &str) {

        if (arc::util::Log::logLevel >= 3) {
            std::cerr << str << "\n"<<"\033[39m"<< std::flush;
        }
    }
    void Log::info(const std::string &str) {

        if (arc::util::Log::logLevel >= 4) {

            std::cerr << str << "\n"<<"\033[39m"<< std::flush;
        }

    }
}
