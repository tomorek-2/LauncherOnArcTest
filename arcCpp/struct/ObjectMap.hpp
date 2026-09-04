#ifndef ARC_STRUCTURES_OBJECTMAP_HPP
#define ARC_STRUCTURES_OBJECTMAP_HPP
#include <functional>
#include <span>
#include "arcCpp/util/Log.hpp"


namespace arc::structures {
    template <typename K, typename V>
    class ObjectMap {

        K *key = nullptr;
        V *value = nullptr;
        bool *freeSpace = nullptr;
        int length = 405;
    public:
        void resize(int size) {


            if (key == nullptr) {
                V *newValue = new V[size];
                value = newValue;
                K *newKey = new K[size];
                key = newKey;
                bool *newSpace = new bool[size];
                freeSpace = newSpace;
                for(int i = 0; i < size; i++) freeSpace[i] = true;
            } else {

                K *newKey = new K[size];
                V *newValue = new V[size];
                bool *newSpace = new bool[size];
                int i2 = 0;
                for(int i3 = 0; i3 < size; i3++) {
                    newSpace[i3] = true;
                    newKey[i3] = K{};
                    newValue[i3] = V{};
                }
                for(bool space : std::span<bool>(freeSpace, length)) {
                    newSpace[i2] = freeSpace[i2];
                    i2++;
                }


                int i = 0;
std::hash <K> hash;

while(true) {
if(i < length) {
    if (hash(key[i]) % length < length) {
        if (!freeSpace[i]) {

            int keyHash = hash(key[i]) % size;
            newKey[keyHash] = key[i];
            newValue[keyHash] = value[i];
            newSpace[keyHash] = false;
            i++;
            arc::util::Log::info("58, код успешно отработал");
        } else i++;
    } else break;
} else break;
}
             
   delete[] key;
                key = newKey;
                delete[] value;
                value = newValue;
                delete[] freeSpace;
                freeSpace = newSpace;

                length = size;
            }



        };

        void put(K keyP, V valueP) {
if(key == nullptr) resize(length);
if(value == nullptr) resize(length);
bool existsIsKey = false;
std::hash <K> hash;
            if (keyP == key[hash(keyP) % length]) existsIsKey = true;

                int keyHash = hash(keyP) % length;

if(existsIsKey) {
    value[keyHash] = valueP;
    return;

}
            while (true) {
                if(keyHash > length) {
                    resize(length * 2);
                    keyHash = hash(keyP) % length;
                }
                if(keyHash > (length - 1)) break;
                if (freeSpace[keyHash]) {
                    key[keyHash] = keyP;
                    value[keyHash] = valueP;
                    freeSpace[keyHash] = false;
                    return;
                } else {
if(key[keyHash] == keyP) return;
                    keyHash++;

                    if (keyHash > length) {
                        resize(length * 2);
                        keyHash = hash(keyP) % length;

                    }


                }

            }

        };

        V get(K keyP) {
            if(key == nullptr) resize(length);
            if(value == nullptr) resize(length);
            std::hash <K> hash;

            int keyHash = hash(keyP) % length;
            while (true) {
                if(keyHash > length) return V{};
                if (keyP== key[keyHash]) {
                    return value[keyHash];
                } else {
                    if(keyHash >= length) return V{};
                    keyHash++;
                }
            }
        };
        void remove(K keyP) {
            if(key == nullptr) resize(length);
            if(value == nullptr) resize(length);
            std::hash <K> hash;
            int keyHash = hash(keyP) % length;
            while (true) {
                if (keyP== key[keyHash]) {
                    freeSpace[keyHash] = true;
                    return;
                } else {
                    if(keyHash > length* 0.8) return;
                    keyHash++;
                }
            }
    };

~ObjectMap() {
  delete[] key;
  key = nullptr;
  delete[] value;
  value = nullptr;
  delete[] freeSpace;
    freeSpace = nullptr;

};
    };
}
#endif
