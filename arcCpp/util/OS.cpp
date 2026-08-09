#include "OS.hpp"
#include <unistd.h>
#include <sys/types.h>
//#include <stdlib.h>
#include <pwd.h>
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

    std::string OS::userHome() {
        struct passwd *TTS = getpwuid(getuid());

        if(TTS != nullptr) {
            return TTS->pw_dir;
        }
        const char* ge =  std::getenv("PATH");
        if (ge == nullptr) return "   ";
        return ge;

    };

    std::string OS::username() {
        struct passwd *TTS = getpwuid(getuid());

        if(TTS != nullptr) {
            return TTS->pw_name;
        }
        const char* ge =  std::getenv("USER");
        if (ge == nullptr) return "   ";
        return ge;

    };

    std::string OS::getPathUser() {



        const char* ge =  std::getenv("PATH");
    if (ge == nullptr) return "   ";
    return ge;

    };
}
