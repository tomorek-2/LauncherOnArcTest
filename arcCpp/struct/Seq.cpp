
namespace arc::structures {
    template <typename T>
    T items[8];
    void Seq::resize(const int newSize) {
T newItems[newSize];
delete[] items;
items = newItems;
    }



};

