#include "OS.hpp"
#include <unistd.h>
#include <sys/types.h>
#include <sys/utsname.h>
#include <pwd.h>
#include <cstring>
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

    }
    std::string OS::osName() {
        struct utsname buffer; if (uname(&buffer) != 0) return "";
        return buffer.sysname;
    }
    std::string OS::osVersion() {
        struct utsname buffer; if (uname(&buffer) != 0) return "";

        return buffer.release;
    }

    bool OS::isArm() {
        struct utsname buffer;

        if (uname(&buffer) != 0) return false;
   return strstr(buffer.machine, "arm");
    }
    bool OS::isX64() {
        struct utsname buffer;

        if (uname(&buffer) != 0) return false;
       if (strstr(buffer.machine, "x86_64") || strstr(buffer.machine, "amd64")) return true;
        return false;
    }
}
