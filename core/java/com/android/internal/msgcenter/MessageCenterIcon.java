/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.internal.msgcenter;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

public class MessageCenterIcon implements Parcelable {
    public String iconPackage;
    public UserHandle user;
    public int iconId;
    public int iconLevel;
    public boolean visible = true;
    public int number;
    public CharSequence contentDescription;

    public MessageCenterIcon(String iconPackage, UserHandle user, int iconId, int iconLevel, int number,
            CharSequence contentDescription) {
        this.iconPackage = iconPackage;
        this.user = user;
        this.iconId = iconId;
        this.iconLevel = iconLevel;
        this.number = number;
        this.contentDescription = contentDescription;
    }

    @Override
    public String toString() {
        return "MessageCenterIcon(pkg=" + this.iconPackage + "user=" + user.getIdentifier()
                + " id=0x" + Integer.toHexString(this.iconId)
                + " level=" + this.iconLevel + " visible=" + visible
                + " num=" + this.number + " )";
    }

    @Override
    public MessageCenterIcon clone() {
        MessageCenterIcon that = new MessageCenterIcon(this.iconPackage, this.user, this.iconId,
                this.iconLevel, this.number, this.contentDescription);
        that.visible = this.visible;
        return that;
    }

    /**
     * Unflatten the MessageCenterIcon from a parcel.
     */
    public MessageCenterIcon(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.iconPackage = in.readString();
        this.user = (UserHandle) in.readParcelable(null);
        this.iconId = in.readInt();
        this.iconLevel = in.readInt();
        this.visible = in.readInt() != 0;
        this.number = in.readInt();
        this.contentDescription = in.readCharSequence();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.iconPackage);
        out.writeParcelable(this.user, 0);
        out.writeInt(this.iconId);
        out.writeInt(this.iconLevel);
        out.writeInt(this.visible ? 1 : 0);
        out.writeInt(this.number);
        out.writeCharSequence(this.contentDescription);
    }

    public int describeContents() {
        return 0;
    }

    /**
     * Parcelable.Creator that instantiates MessageCenterIcon objects
     */
    public static final Parcelable.Creator<MessageCenterIcon> CREATOR
            = new Parcelable.Creator<MessageCenterIcon>()
    {
        public MessageCenterIcon createFromParcel(Parcel parcel)
        {
            return new MessageCenterIcon(parcel);
        }

        public MessageCenterIcon[] newArray(int size)
        {
            return new MessageCenterIcon[size];
        }
    };
}

