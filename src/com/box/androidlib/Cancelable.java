package com.box.androidlib;

/**
 * Interface representing a cancelable asynchronous task.
 * 
 */
public interface Cancelable {

    /**
     * Cancel the task.
     * 
     * @return True if successfully canceled, false otherwise.
     */
    boolean cancel();

}
