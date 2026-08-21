#ifndef ARC_STRUCT_SEQ_HPP
#define ARC_STRUCTURE_SEQ_HPP

namespace arc::structures {

    template <typename T>
    class Seq {
    public:
        void resize(int newSize);
        T items[500];
        bool contains(T items);
    };

}
#endif

