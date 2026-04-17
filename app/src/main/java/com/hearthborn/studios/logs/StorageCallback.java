package com.hearthborn.studios.logs;

public interface StorageCallback<T> {
    void onComplete(T result);
}