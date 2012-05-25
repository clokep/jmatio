package com.jmatio.types;

public interface ByteStorageSupport<T extends Number> {
    int getBytesAllocated();
    T buildFromBytes(byte[] bytes);
    byte[] getByteArray (T value);
    Class<?> getStorageClazz();
}
