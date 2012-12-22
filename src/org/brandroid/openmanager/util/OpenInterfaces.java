
package org.brandroid.openmanager.util;

import org.brandroid.openmanager.data.OpenPath;

public class OpenInterfaces {

    public interface OnBookMarkChangeListener {
        public void onBookMarkAdd(OpenPath path);

        public void scanBookmarks();
    }

}
