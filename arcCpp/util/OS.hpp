#ifndef ARC_UTIL_OS_HPP
#define ARC_UTIL_OS_HPP

#include <string>

namespace arc::util {
    class OS {
    public:

        static bool isLinux();
        static bool isUnix();
        static std::string userHome();
        static std::string username();
        static std::string getPathUser();
        static std::string osName();
        static std::string osVersion();
        static bool isArm();
        static bool isX64();
    };
}

#endif