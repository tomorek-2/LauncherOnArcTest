

#include "arcCpp/util/Log.hpp"
#include "arcCpp/struct/ObjectMap.hpp"
#include <sstream>
#include <functional>

#include <charconv>
static  arc::structures::ObjectMap<std::string, std::function<void(const std::string&)>> OQmap;
static arc::structures::ObjectMap<std::string, double> doubleMap;
class parser {
public:

    void init() {
        doubleMap.replace = true;
OQmap.put("print", [](std::string result2) {
 arc::util::Log::info(result2);
});
        OQmap.put("set", [](std::string inputResult) {
            std::string result = "";
            std::string result2 = "";
            int i = 1;
            const char* bytes = inputResult.c_str();
            for(int l = 0; i < inputResult.length(); i++) {

                auto b = bytes[i];

                if (b == 32) {
                   i++;
                    result = inputResult.substr(0, l);
                    break;
                }
                result += b;
                l++;
            }

            if(result == "") {
                arc::util::Log::warn("Введён неверный аргумент. 36" + result);
                return;
            }

            for( int w = 0; i < inputResult.length(); i++) {
                auto b = bytes[i];
                result2 += b;
                if(bytes[i] == 34) {
                    result2 = std::to_string(doubleMap.get(result2));
                    break;
                }
                if (b == 10) {
                    break;
                }
            }
            if(result2 == "") {
                arc::util::Log::warn("Введён неверный аргумент. 48" + result2);
                return;
            }
if(true) {
double vaw = 0.0;
    auto [ptr, ec] = std::from_chars(result2.data(), result2.data() + result2.size(), vaw);


    if( (ec == std::errc{}) && (ptr == result2.data() + result2.size()))
   doubleMap.put(result, vaw);
    return;
}

//doubleMap.put(result, result2);
            return;
        });
    };
void exec(std::string lines) {
    std::string result = "";
    std::string result2 = "";
    const char* bytes = lines.c_str();
    int i = 0;
    int ww = 0;
for(int l = 0; i < lines.length(); i++) {

    auto b = bytes[i];
    result += b;
    if (b == 32) {


result = lines.substr(0, l);
        break;
    }
    l++;

}

    if(result == "") {
        arc::util::Log::warn("Введён неверный аргумент." + result);
        return;
    }

    for( int w = 0; i < lines.length(); i++) {

        auto b = bytes[i];

        if(bytes[i] == 34) {
            result2 = std::to_string(doubleMap.get(result2));
            break;
        }
        result2 += b;
        if (b == 10) {
            break;
        }

    }
    if(result2 == "") {
        arc::util::Log::warn("Введён неверный аргумент." + result2);
        return;
    }

auto resultA = OQmap.get(result);
if(resultA) {
    resultA(result2);
    return;
} else {
    arc::util::Log::warn("Неизвестная команда: "+result);
    return;
}
return;
};

};


int main() {
arc::util::Log::log("Парсер начинает работу");
std::string line;
parser p;
p.init();

p.exec(R"(set w 0.01)");
    p.exec(R"(print "w)");
p.exec("set w 2");
    p.exec(R"(print "w)");

    return 0;
}