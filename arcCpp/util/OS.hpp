#ifndef ARC_UTIL_OS_HPP
#define ARC_UTIL_OS_HPP

#include <string>

namespace arc::util {
    class OS {
    public:

        static bool isLinux();
        static bool isUnix();
    };
}

#endif