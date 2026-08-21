#include "Seq.hpp"


namespace arc::structures {
    template <typename T>
    T items[500];
    template <typename T>
    bool Seq<T>::contains(T itemC) {
for(T item : items) {
    if(itemC == item) return true;
}
return false;


};
    template <typename T>
void Seq<T>::resize(int newSize) {
    T newItems[items.length * 2];
    delete items;
    items = newItems;

};

}