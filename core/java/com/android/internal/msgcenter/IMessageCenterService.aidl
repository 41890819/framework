/**
 * Copyright (c) 2007, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.android.internal.msgcenter;

import com.android.internal.msgcenter.IMessageCenter;
import android.service.notification.MessageCenterNotification;

/** @hide */
interface IMessageCenterService
{
    void collapsePanels();

    // ---- Methods below are for use by the status bar policy services ----
    // You need the STATUS_BAR_SERVICE permission
    void registerMessageCenter(IMessageCenter callbacks, out List<IBinder> notificationKeys, 
    	    out List<MessageCenterNotification> notifications,
            out List<IBinder> binders);
    void onNotificationClick(String pkg, String tag, int id);
    void onNotificationError(String pkg, String tag, int id,
            int uid, int initialPid, String message);
    void onClearAllNotifications();
    void onNotificationClear(String pkg, String tag, int id);
    void openNotificationDrawer();
}
