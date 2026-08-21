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
        bool contains(T itemC) {
            if(items == nullptr) createArray(length);
            for(T item : items) {
                if(itemC == item) return true;
            }
            return false;


        };

        void add(T item)  {
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
    private:
        void createArray(int size) {
          T* newItems = new T[size];
          delete[] items;
          items = newItems;
          freeSpace = 0;


        };
~Seq() {
  delete[] items;
items = nullptr;
};

    };



}
#endif

