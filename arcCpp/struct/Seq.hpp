#ifndef ARC_STRUCT_SEQ_HPP
#define ARC_STRUCTURE_SEQ_HPP
#include <string>
namespace arc::structures {

    template <typename T>
    class Seq {
    public:
        int length = 500;


        T* items = nullptr;
        int freeSpace = 0;
        int totalSpace = 0;
        bool contains(T itemC) {
            if(items == nullptr) createArray(length);
            for(T item : items) {
                if(itemC == item) return true;
            }
            return false;


        };

        void add(T item)  {
            totalSpace++;
            if(items == nullptr) createArray(length);
            while(true) {
                if(freeSpace >= length) {
                    length *= 2;
                    createArray(length);
                }
                items[freeSpace] = item;
                freeSpace++;
                return;


            }
        };

        T get(int key)  {

            while(true) {

                if (items == nullptr) {createArray(length);}
                else
                    return items[key];

            }

        };
         int get(T key)  {
             if(items == nullptr) createArray(length);
             int i = 0;
             for(T item : items) {


                 if(key == item) return i++;
                 i++;
             }

             return 0;



         };
         void remove(int index) {
             totalSpace--;
             if(items == nullptr) createArray(length);

             items[index] = items[freeSpace];
         };
         void remove(T item) {
             totalSpace--;
             for(int i = 0; i < length; i++) {
               if(item == items[i]) {
                   freeSpace = i;
                   return;
               }
           }
             return;
         };
        ~Seq() {
            delete[] items;
            items = nullptr;
        };
        void clear() {
            delete[] items;
            items = nullptr;
        };

        void createArray(int size) {
          T* newItems = new T[size];
          for(int i = 0; i < freeSpace; i++) {
              newItems[i] = items[i];
          }
          delete[] items;
          items = newItems;
          freeSpace = 0;


        };


    };



}
#endif

