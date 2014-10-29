/*******************************************************************************
 *
 *  Copyright (C) 2013 Broadcom Corporation
 *
 *  This program is the proprietary software of Broadcom Corporation and/or its
 *  licensors, and may only be used, duplicated, modified or distributed
 *  pursuant to the terms and conditions of a separate, written license
 *  agreement executed between you and Broadcom (an "Authorized License").
 *  Except as set forth in an Authorized License, Broadcom grants no license
 *  (express or implied), right to use, or waiver of any kind with respect to
 *  the Software, and Broadcom expressly reserves all rights in and to the
 *  Software and all intellectual property rights therein.
 *  IF YOU HAVE NO AUTHORIZED LICENSE, THEN YOU HAVE NO RIGHT TO USE THIS
 *  SOFTWARE IN ANY WAY, AND SHOULD IMMEDIATELY NOTIFY BROADCOM AND DISCONTINUE
 *  ALL USE OF THE SOFTWARE.
 *
 *  Except as expressly set forth in the Authorized License,
 *
 *  1.     This program, including its structure, sequence and organization,
 *         constitutes the valuable trade secrets of Broadcom, and you shall
 *         use all reasonable efforts to protect the confidentiality thereof,
 *         and to use this information only in connection with your use of
 *         Broadcom integrated circuit products.
 *
 *  2.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, THE SOFTWARE IS PROVIDED
 *         "AS IS" AND WITH ALL FAULTS AND BROADCOM MAKES NO PROMISES,
 *         REPRESENTATIONS OR WARRANTIES, EITHER EXPRESS, IMPLIED, STATUTORY,
 *         OR OTHERWISE, WITH RESPECT TO THE SOFTWARE.  BROADCOM SPECIFICALLY
 *         DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF TITLE, MERCHANTABILITY,
 *         NONINFRINGEMENT, FITNESS FOR A PARTICULAR PURPOSE, LACK OF VIRUSES,
 *         ACCURACY OR COMPLETENESS, QUIET ENJOYMENT, QUIET POSSESSION OR
 *         CORRESPONDENCE TO DESCRIPTION. YOU ASSUME THE ENTIRE RISK ARISING OUT
 *         OF USE OR PERFORMANCE OF THE SOFTWARE.
 *
 *  3.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, IN NO EVENT SHALL BROADCOM OR
 *         ITS LICENSORS BE LIABLE FOR
 *         (i)   CONSEQUENTIAL, INCIDENTAL, SPECIAL, INDIRECT, OR EXEMPLARY
 *               DAMAGES WHATSOEVER ARISING OUT OF OR IN ANY WAY RELATING TO
 *               YOUR USE OF OR INABILITY TO USE THE SOFTWARE EVEN IF BROADCOM
 *               HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES; OR
 *         (ii)  ANY AMOUNT IN EXCESS OF THE AMOUNT ACTUALLY PAID FOR THE
 *               SOFTWARE ITSELF OR U.S. $1, WHICHEVER IS GREATER. THESE
 *               LIMITATIONS SHALL APPLY NOTWITHSTANDING ANY FAILURE OF
 *               ESSENTIAL PURPOSE OF ANY LIMITED REMEDY.
 *
 *******************************************************************************/

package android.bluetooth.peripheral;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.GsmAlphabet;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import android.util.Log;


public final class PhoneBookInfo implements Parcelable {
    int mIndex;
    String mNumber;
    int mType;
    String mContactInfo;
    String mCharset;


    public static final String TAG = "PhoneBookInfo";

    /** @hide */
    public PhoneBookInfo(String cpbrString, String charset)
    {
        mCharset = charset;
        parsePhoneBookInfo(cpbrString);
    }

// Copy constructor
    /** @hide */
    public PhoneBookInfo(PhoneBookInfo phoneBookInfo)
    {
        this.mIndex = phoneBookInfo.mIndex;
        this.mNumber = phoneBookInfo.mNumber;
        this.mType = phoneBookInfo.mType;
        this.mContactInfo = phoneBookInfo.mContactInfo;
    }

    private void parsePhoneBookInfo(String input)
    {

        StringTokenizer stringTokenizer = new StringTokenizer(input,",");
        int i = 0;
        String tokenString;

        Log.d(TAG, input);

        while(stringTokenizer.hasMoreTokens()) {

            tokenString = stringTokenizer.nextToken();
            if (null != tokenString)
            {
                tokenString = tokenString.trim();
                if (i == 0) {
                    tokenString = tokenString.substring(tokenString.indexOf(":")+ 1,tokenString.length()).trim();
                    mIndex = Integer.parseInt(tokenString);
                } else if ( i == 1) {
                    mNumber = tokenString.substring(1 ,tokenString.length()-1);
                } else if (i == 2) {
                    mType = Integer.parseInt(tokenString.trim());
                } else if (i == 3) {
                        mContactInfo = tokenString.substring(1 ,tokenString.length()-1);
                    if (mCharset.equals("GSM")) {
                        mContactInfo = tokenString.substring(1 ,tokenString.length()-1);
                        byte[] byteContDetails = mContactInfo.getBytes();
                        int length = mContactInfo.length();
                        mContactInfo = GsmAlphabet.gsm8BitUnpackedToString(
                                            byteContDetails, 0,length);
                        Log.d(TAG,"Decoded GSM= "+mContactInfo);
                    }
                }
            }
            i = i + 1;
        }

        Log.d("PhoneBookInfo",getSummary());

    }

    /** @hide */
    public String getSummary() {
        return "Idx="+mIndex+"mContactInfo="+mContactInfo+"number="+mNumber;
    }
    public int getIndex()
    {
        return mIndex;
    }

    public String getContactNumber()
    {
        return mNumber;
    }

    public String getContactInfo()
    {
        return mContactInfo;
    }

    public int getContactNumType()
    {
        return mType;
    }

    /** @hide */
    public int describeContents()
    {
        return 0;
    }

    /** @hide */
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mIndex);
        dest.writeString(mNumber);
        dest.writeInt(mType);
        dest.writeString(mContactInfo);
    }

    /** @hide */
    public static final Creator<PhoneBookInfo> CREATOR = new Creator<PhoneBookInfo>() {
            public PhoneBookInfo createFromParcel(Parcel source) {
                return new PhoneBookInfo(source);
            }
            public PhoneBookInfo[] newArray(int size) {
                return new PhoneBookInfo[size];
            }
    };

    private PhoneBookInfo(Parcel source)
    {
        mIndex = source.readInt();
        mNumber = source.readString();
        mType = source.readInt();
        mContactInfo = source.readString();
    }
}

