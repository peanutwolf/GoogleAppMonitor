package com.peanutwolf.googleappmonitor.Services.Interfaces;

import com.peanutwolf.googleappmonitor.Utilities.RangedLinkedList;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by vigursky on 15.04.2016.
 */
public interface ShakeServiceDataSource<T>{

    LinkedList<T> getAccelerationData();

    int getAverageAccelerationData();

}
