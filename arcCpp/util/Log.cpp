#include "Log.hpp"
#include <iostream>
//Что же, переписать Anuken/Arc будет не просто, но для меня будет большой опыт, если я не навайбкодю решение.
namespace arc::util {


    void Log::log(const std::string& str) {
        std::cerr  << str << "\n" << std::flush;

    }
}
