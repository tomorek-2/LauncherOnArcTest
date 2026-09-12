#ifndef ARC_STRUCT_SEQ_HPP
#define ARC_STRUCTURE_SEQ_HPP
#include <string>
namespace arc::structures {

    template <typename T>
    class Seq {
    public:
        int length = 500;


        T* items = nullptr;
        bool* freeSpace = nullptr;
        int freeSpaceI = 0;
        int totalSpace = 0;
        bool contains(T itemC) {
            if(items == nullptr) createArray(length);
            for(int i = 0; i < length; i++) {
                if(itemC == items[i]) return true;
            }
            return false;


        };

        void add(T item)  {
            totalSpace++;
            if(items == nullptr) createArray(length);
            while(true) {
                if(totalSpace >= length) {
                    length *= 2;
                    createArray(length);
                }
                items[freeSpaceI] = item;
                freeSpaceI++;
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


                 if(key == item) return i;
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
            freeSpace = nullptr;
            totalSpace = 0;
            freeSpaceI = 0;
            length = 500;
            createArray(length);
        };

        void createArray(int size) {
          T* newItems = new T[size];
            bool* newSpace = new bool[size];


          if(freeSpace != nullptr) {
              for(int i = 0; i < length; i++) {
                  newItems[i] = items[i];
                  newSpace[i] = freeSpace[i];
              }
              delete[] items;
              delete[] freeSpace;
              items = newItems;
              freeSpace = newSpace;
          } else {
              for(int i = 0; i < size; i++){
                  newSpace[i] = true;
              }
              items = newItems;
              freeSpace = newSpace;
          }


        };


    };



}
#endif

