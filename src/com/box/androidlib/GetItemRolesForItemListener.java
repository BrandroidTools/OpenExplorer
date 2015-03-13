package com.box.androidlib;

import java.util.ArrayList;


/**
 * Interface definition for a callback to be invoked when Box.getItemRoles() is called.
 * 
 */
public interface GetItemRolesForItemListener extends ResponseListener {

    /** Status returned if operation was successful. */
    String STATUS_S_GET_ITEM_ROLES_FOR_ITEM = "s_get_item_roles_for_item";

    /**
     * Called when the API request has completed.
     * 
     * @param itemRoles
     *            List of ItemRole objects.
     * @param status
     *            Status code from Box API
     */
    void onComplete(ArrayList<ItemRole> itemRoles, String status);

}
