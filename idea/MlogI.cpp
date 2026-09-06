

#include "arcCpp/util/Log.hpp"
#include "arcCpp/struct/ObjectMap.hpp"

#include <functional>
static  arc::structures::ObjectMap<std::string, std::function<void()>> OQmap;
class parser {
public:

    void init() {
OQmap.put("print", []() {
 arc::util::Log::info("print Работает");
});
    };
void exec(std::string lines) {
    std::string result = "";
    const char* bytes = lines.c_str();
for(int i = 0; i < lines.length(); i++) {
    auto b = bytes[i];
    result += b;
    if (b == 10) {
        break;
    }
}
auto resultA = OQmap.get(result);
if(resultA) {
    resultA();
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
p.exec("print");



    return 0;
}