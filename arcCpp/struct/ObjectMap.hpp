#ifndef ARC_STRUCT_OBJECTMAP_HPP
#define ARC_STRUCT_OBJECTMAP_HPP
#include <functional>



namespace arc::structures {
    template <typename K, typename V>
    class ObjectMap {

        K *key = nullptr;
        V *value = nullptr;
        bool *freeSpace = nullptr;
        int length = 500;
    public:
        void resize(int size) {

            if (key == nullptr) {
                V *newValue = new V[size];
                value = newValue;
                K *newKey = new K[size];
                key = newKey;
                bool *newSpace = new bool[size];
                freeSpace = newSpace;
            } else {
                K *newKey = new K[size];
                V *newValue = new V[size];
                bool *newSpace = new bool[size];
                int i2 = 0;
                for(bool space : freeSpace) {
                    newSpace[i2] = freeSpace[i2];
                    i2++;
                }
                int i = 0;

                for (K item: key) {

                    std::hash <K> hash;
                    int keyHash = hash(item) % length;

                    if (freeSpace[keyHash]) {
                        newKey[keyHash] = key[keyHash];
                        newValue[keyHash] = value[keyHash];
                        freeSpace[keyHash] = false;

                    } else {
                        keyHash++;
                        if (keyHash >= length) {
                            resize(length * 2);
                            keyHash = 0;

                        }


                    }


                }
                delete[] key;
                key = newKey;
                delete[] value;
                value = newValue;
                delete[] freeSpace;
                freeSpace = newSpace;
            }


            length = size;
        };

        void put(K keyP, V valueP) {

bool existsIsKey = false;
std::hash <K> hash;
            if (keyP == key[hash(keyP) % length]) existsIsKey = true;
                int keyHash = hash(keyP) % length;
            while (true) {
                if (freeSpace[keyHash]) {
                    key[keyHash] = keyP;
                    value[keyHash] = valueP;
                    freeSpace[keyHash] = false;
                    return;
                } else {
                    keyHash++;
                    if (keyHash >= length) {
                        resize(length * 2);
                        keyHash = hash(keyP % length);

                    }


                }

            }

        };

        V get(K keyP) {
            std::hash <K> hash;

            int keyHash = hash(keyP) % length;
            while (true) {
                if (KeyP== key[keyHash]) {
                    return value[keyHash];
                } else {
                    keyHash++;
                }
            }
        };
    };
}
#endif