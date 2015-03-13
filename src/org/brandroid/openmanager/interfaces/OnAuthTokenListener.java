package org.brandroid.openmanager.interfaces;

import org.brandroid.openmanager.data.OpenPath;

public interface OnAuthTokenListener extends OpenPath.ExceptionListener
{
    public void onDriveAuthTokenReceived(String account, String token);
}