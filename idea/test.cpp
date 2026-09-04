#include <SDL2/SDL.h>
#include <iostream>
#include "arcCpp/util/Log.hpp"
#include "arcCpp/util/OS.hpp"
#include "arcCpp/Events.hpp"
#include "arcCpp/util/Threads.hpp"
class SdlTest {
    SDL_Window* sdlwin;
public:  SDL_Window* Window() {
        sdlwin = SDL_CreateWindow("Singularity Draw", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, 800, 600, SDL_WINDOW_SHOWN);
        return sdlwin;
    }
    ~SdlTest() {
      SDL_DestroyWindow(sdlwin);
    }
};

//Это же свалка идей, скоро я почищу код от решения БЯМ.
int main() {
    // 1. Инициализация
    if (SDL_Init(SDL_INIT_VIDEO) < 0) return 1;

    // 2. Создаем окно
//    SDL_Window* window = SDL_CreateWindow("Singularity Draw", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, 800, 600, SDL_WINDOW_SHOWN);
SdlTest* sdltest = new SdlTest();
SDL_Window* window = sdltest->Window();

    SDL_Renderer* renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED | SDL_RENDERER_PRESENTVSYNC);

    bool running = true;
    SDL_Event event;
int* xqe=new int(0);
int xq = *xqe;
int xqr;
float rq = 0;
int orderChar = 0;
bool orderTwoI = false;
char charTest = 0;
static int numberI = 0;
arc::util::Log::log("Hello, World!");
arc::util::Log::warn("Warning!");
arc::util::Log::logLevel = 4;
arc::util::Log::log("if you see this, logLevel dont work");
arc::util::Log::warn("if you see this, logLevel work");
    arc::util::Log::info(std::string(" \033[91m") + "red");
arc::util::Log::info(arc::util::OS::userHome());
    arc::util::Log::info(arc::util::OS::username());
    arc::util::Log::info(arc::util::OS::getPathUser());
    arc::util::Log::log(arc::util::OS::isLinux() ? "OS: Linux" : arc::util::OS::isUnix() ? "OS: Unix" : "OS: Unknown");
arc::util::Log::info(arc::util::OS::OSVersionArchitecture());
arc::Events::on([](){
    arc::util::Log::info(arc::util::OS::OSVersionArchitecture());
}, "outputArchitecture");
arc::util::Threads::daemon("outputArchitecture", []() {
    arc::Events::fire("outputArchitecture");
});
//arc::util::Log::info(arc::util::OS::isArm() ? "OS: ARM " : arc::util::OS::isX64() ? "OS: X64 " + arc::util::OS::osVersion() : "OS: Unknown");
    // ГЛАВНЫЙ ЦИКЛ (Game Loop)

    while (running) {
orderChar++;
if(orderChar > 255) {
    orderChar = 0;
    orderTwoI = !orderTwoI;
}
if(orderTwoI) {
    charTest = 256 + orderChar;
} else charTest = orderChar;
        // Проверяем события (чтобы окно не зависло)
        while (SDL_PollEvent(&event)) {
            if (event.type == SDL_QUIT) running = false;
        }

        numberI++;

arc::Events::on([numberI](){

    arc::util::Log::info(arc::util::OS::OSVersionArchitecture());
    arc::util::Log::info(std::to_string(numberI));
}, std::to_string(*" ") + charTest);
arc::Events::fire(std::to_string(*" ") + charTest);
        // --- Отрисовка начинается здесь ---

        // А. Очищаем экран (заливаем черным цветом)
        SDL_SetRenderDrawColor(renderer, 0, 0, 0, 0); // R, G, B, A
        SDL_RenderClear(renderer);

        // Б. Рисуем Красный Квадрат
        SDL_Rect rect = {200, 150, 400, 300}; // x, y, ширина, высота
        SDL_SetRenderDrawColor(renderer, 255, 0, 0, 150); // Красный
        SDL_RenderFillRect(renderer, &rect);

        if(xqr ==1) rq+=(255.0/800.0); else rq-=(255.0/800.0);
        if(rq > 255) rq = 0;
if(xqr ==1) xq++; else xq--;
if(xq > 800) xqr = -1;
if(xq < -1) xqr = 1;

        // В. Рисуем Синюю Линию
        SDL_SetRenderDrawColor(renderer, rq, 0, 255, 100); // Синий
        SDL_RenderDrawLine(renderer, xq, 0, 800, 600);

        // Г. Выводим всё, что нарисовали, на экран
        SDL_RenderPresent(renderer);

        // --- Отрисовка закончилась ---
    }

    // Очистка
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
    delete xqe; // Память освобождена
    xqe = nullptr; // Хороший тон: занулить указатель, чтобы не использовать его случайно
delete sdltest;
    return 0;
}
