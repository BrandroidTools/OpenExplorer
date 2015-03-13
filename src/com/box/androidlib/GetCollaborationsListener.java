package com.box.androidlib;

import java.util.List;


/**
 * Interface definition for a callback to be invoked when Box.getCollaborations() is called.
 * 
 */
public interface GetCollaborationsListener extends ResponseListener {

    /** Status returned if operation was successful. */
    String STATUS_S_GET_COLLABORATIONS = "s_get_collaborations";

    /**
     * Called when the API request has completed.
     * 
     * @param collaborations
     *            List of Collaboration objects.
     * @param status
     *            Status code from Box API
     */
    void onComplete(List<Collaboration> collaborations, String status);

}
