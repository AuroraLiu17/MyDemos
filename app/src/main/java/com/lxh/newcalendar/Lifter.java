package com.lxh.newcalendar;

/**
 * Created by liuxiaohui on 4/7/16.
 *
 * This interface represent a kind of object which have following characteristics like a lifter:
 * Have a constant number of floors (each floor represent a certain state), get by {@link #getFloorCount()}
 * For each floor indexed from 0 to floorCount - 1, you can get the height of that floor by {@link #getHeightForFloor(int)}
 * At every time there is only one floor the lifter is in, which is returned by {@link #getCurrentFloorIndex()}
 * Finally you can move the lifter to a certain floor by calling {@link #goToFloor(int)}
 */
public interface Lifter {

    /**
     * It is prefered that the step count is a constant value that larger than 0
     * @return int count of steps
     */
    int getFloorCount();

    /**
     * Return the height of a floor
     * @param floorIndex
     * @return int The height for a floor at floorIndex
     */
    int getHeightForFloor(int floorIndex);

    int getLowerFloor();
    int getHigherFloor();

    /**
     * Return which floor is in now
     * @return int current floor index
     */
    int getCurrentFloorIndex();

    /**
     * Go to a certain floor
     * @param floorIndex
     */
    void goToFloor(int floorIndex);

}
