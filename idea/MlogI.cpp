

#include "arcCpp/util/Log.hpp"
#include "arcCpp/struct/ObjectMap.hpp"
#include "arcCpp/struct/Seq.hpp"
#include "arcCpp/util/Threads.hpp"
#include <sstream>
#include <functional>
#include <iostream>
#include <charconv>
#include <unistd.h>
#include <atomic>
static  arc::structures::ObjectMap<std::string, std::function<void(const std::string&)>> OQmap;
static arc::structures::ObjectMap<std::string, double> doubleMap;
static arc::structures::Seq<std::string> seq; //Команды.
static arc::structures::Seq<std::string> seqL;
static int ipt = 0;
static int stepCounter = 0;
class parser {
public:

    void init() {

        doubleMap.replace = true;
OQmap.put("print", [](std::string result2) {
    const char* bytes = result2.c_str();
std::string result2A;
auto b = bytes[0];
int l = 0;
bool isString = true;
for(int i = 0; i < result2.length(); i++) {

    
    b = bytes[i];
if(b == 34) {
    l++;
} else    result2A += b;
if(l == 2) {
    isString = false;
    break;
}

}
if(!isString) {
    result2 = std::to_string(doubleMap.get(result2A));
} else result2 = result2A;

 arc::util::Log::info(result2);
});
OQmap.put("end", [](std::string result2) {
   stepCounter = 0;
});
OQmap.put("wait", [](std::string inputResult) {
    double vaw = 0.0;
    auto [ptr, ec] = std::from_chars(inputResult.data(), inputResult.data() + inputResult.size(), vaw);


    if( (ec == std::errc{}) && (ptr == inputResult.data() + inputResult.size())) {
        usleep(vaw *  1000000);
    };
     });

             OQmap.put("set", [](std::string inputResult) {
                 std::string result = "";
                 std::string result2 = "";
                 int i = 0;
              //   arc::util::Log::warn("inputResult"+inputResult);
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
                   //  result2 += b;
                     if(bytes[i] == 34) {
                         result2 = std::to_string(doubleMap.get(result2));
                         break;
                     }
                     if (b == 10) {
                         break;
                     }
                     result2 += b;

                 }


                 if(result2 == "") {
                     arc::util::Log::warn("Введён неверный аргумент. 48" + result2);
                     return;
                 }
     if(true) {
     double vaw = 0.0;
         auto [ptr, ec] = std::from_chars(result2.data(), result2.data() + result2.size(), vaw);


         if( (ec == std::errc{}) && (ptr == result2.data() + result2.size())) {

   if(result == "@counter") { //Должен быть гибрид хеш карты и массива для эффективности, но пока на условиях.
       stepCounter = (int)vaw;
       return;
   }

   //arc::util::Log::warn(" запись в карту идёт" +  result + "#");
        doubleMap.put(result, vaw);
    } else      arc::util::Log::warn("66 строка ошибка");
    return;
}

//doubleMap.put(result, result2);
            return;
        });
    };
    void start() {

            parser p;
        for(int w = 0; stepCounter < seq.totalSpace; w) {
            if (ipt < 100000) {

                std::string tmpString = "";
                tmpString = seq.get(stepCounter);
                if (tmpString != "") p.exec(tmpString);
                stepCounter++;
                ipt++;
            } else break;
        }
        stepCounter = 0;
ipt = 0 ;
    }
    void add(std::string line) {
        if(line != "") {
            seq.add(line);
        }
    }
void exec(std::string lines) {
    std::string result = "";
    std::string result2 = "";
    const char* bytes = lines.c_str();
    int i = 0;
    int ww = 0;
for(int l = 0; i < lines.length(); i++) {

    auto b = bytes[i];
    if (b == 32) {
i++;

result = lines.substr(0, l);
        break;
    }
    l++;

    result += b;
}

    if(result == "") {
        arc::util::Log::warn("Введён неверный аргумент." + result);
        return;
    }

    for( int w = 0; i < lines.length(); i++) {

        auto b = bytes[i];



        if (b == 10) {
            break;
        }
        result2 += b;
    }
    /*if(result2 == "") {
        arc::util::Log::warn("Введён неверный аргумент." + result2);
        return;
    } */

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
std::string command = "";
arc::util::Log::log("Парсер начинает работу, введите код");
   // std::getline(std::cin, command);

std::string line;
parser p;
p.init();
bool running = true;


while(true) {
    arc::util::Log::log("<MLog>");

while(true) {
    std::streamsize availBytes = std::cin.rdbuf()->in_avail();

    if (availBytes > 0) {
        std::string buffer(availBytes, '\0');
        std::cin.read(&buffer[0], availBytes);
        line = buffer;
    } else line = "";

    if(line == "#") {
        if(running) {
            running = false;
        } else running = true;
        break;
    }
    if(line != "") {
        if(line == "start") {

            p.start();
            seq.clear();
            break;
        }
        p.add(line);
    }
   // sleep(  0.01);

}

//p.exec("print ww");

}
/*
p.exec(R"(set w 0.01)");
    p.exec(R"(print "w)");
p.exec("set w 2");
    p.exec(R"(print "w)");
*/
    return 0;
}