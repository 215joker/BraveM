package com.bravem.app.data;

/**
 * Generic callback used by repository classes for async Firebase operations.
 * Keeps UI code free of direct Firebase Task/Listener boilerplate.
 *
 * @param <T> the type of result returned on success
 */
public interface DataCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
