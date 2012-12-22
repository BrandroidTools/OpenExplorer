/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brandroid.openmanager.interfaces;

import org.brandroid.openmanager.util.BetterPopupWindow;

import com.actionbarsherlock.view.CollapsibleActionView;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;

/**
 * When a View implements this interface it will receive callbacks when expanded
 * or collapsed as an action view alongside the optional, app-specified
 * callbacks to OpenActionExpandListener.
 */
public interface OpenActionView extends CollapsibleActionView {

    /**
     * Called when ActionBar should not be used as container for ActionView.
     * 
     * @param anchor Popup anchor.
     * @return BetterPopupWindow instance containing ActionView.
     */
    public BetterPopupWindow getPopup(Context context, View anchor);
}
