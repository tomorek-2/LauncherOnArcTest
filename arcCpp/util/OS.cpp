#include "OS.hpp"

namespace arc::util {
    bool OS::isLinux() {
#ifdef __linux__
        return true;
#endif
        return false;
    }
    bool OS::isUnix() {
#ifdef __unix__
        return true;
#endif
        return false;

    }




}
