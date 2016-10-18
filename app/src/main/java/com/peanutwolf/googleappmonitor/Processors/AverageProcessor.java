package com.peanutwolf.googleappmonitor.Processors;

/**
 * Created by vigursky on 18.10.2016.
 */
public interface AverageProcessor<T> {
    T sumValue(T t);
    T subValue(T t);
    T divValue(int divisor);
}
