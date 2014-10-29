/******************************************************************************
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
 *         CORRESPONDENCE TO DESCRIPTION. YOU ASSUME THE ENTIRE RISK ARISING
 *         OUT OF USE OR PERFORMANCE OF THE SOFTWARE.
 *
 *  3.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, IN NO EVENT SHALL BROADCOM
 *         OR ITS LICENSORS BE LIABLE FOR
 *         (i)   CONSEQUENTIAL, INCIDENTAL, SPECIAL, INDIRECT, OR EXEMPLARY
 *               DAMAGES WHATSOEVER ARISING OUT OF OR IN ANY WAY RELATING TO
 *               YOUR USE OF OR INABILITY TO USE THE SOFTWARE EVEN IF BROADCOM
 *               HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES; OR
 *         (ii)  ANY AMOUNT IN EXCESS OF THE AMOUNT ACTUALLY PAID FOR THE
 *               SOFTWARE ITSELF OR U.S. $1, WHICHEVER IS GREATER. THESE
 *               LIMITATIONS SHALL APPLY NOTWITHSTANDING ANY FAILURE OF
 *               ESSENTIAL PURPOSE OF ANY LIMITED REMEDY.
 *
 *****************************************************************************/


package android.bluetooth.peripheral;

import android.bluetooth.BluetoothDevice;
import java.util.List;


public interface IHfDeviceEventHandler {

       /**
     * Callback for connection state change.
     *
     * @param errCode
     * @param remoteDevice
     * @param newState
     *            One of the STATE_
     * @param prevState
     *            One of the STATE_

     */
    public void onConnectionStateChange(int errCode, BluetoothDevice remoteDevice,
                                                int newState, int prevState);

    /**
     * Callback for audio state change.
     *
     * @param newState
     *            One of the AUDIO_STATE_
     * @param prevState
     *            One of the AUDIO_STATE_
     */
    public void onAudioStateChange( int newState, int prevState);

    /**
     * Callback for device indicators update ( Battery, signal, roam, service)
     *
     * @param indValue Array containing all indicators of INDICATOR_TYPE_ as index.
     *            Contains the individual indicators
     */
    public void onIndicatorsUpdate(int[] indValue);

    /**
     * Callback for call state change.
     *
     * @param callSetupState
     *            One of the values from CALL_STATE_
     * @param numActive
     *            Has active calls.
     * @param numHeld
     *            Has held calls.
     * @param number
     *            Contain caller number information and is valid for incoming or call waiting states.
     * @param addrType
     *            Contain caller type information and is valid for incoming or call waiting states.
     */
    public void onCallStateChange(int status, int callSetupState, int numActive, int numHeld,
                                String number ,int addrType);

    /**
     * Callback for VR state change
     * @param status
     * @param vrState
     *            One of the values from VR_STATE_
     */
    public void onVRStateChange(int status, int vrState);

    /**
     * Callback for volume change
     *
     * @param volType
     *            Type of volume change speaker/mic
     * @param volume
     *            Current volume (0-15)
     */
    public void onVolumeChange(int volType, int volume);

    /**
     * Callback for readPhoneBookList() response
     *
     * @param phoneNum
     *         List of phone book entries  with index, number , addrtype,name.
     *         getXXXX() can be used to get info from the class PhoneBookInfo
     */
    public void onPhoneBookReadRsp(int status, List<PhoneBookInfo> phoneNum);

    /**
     * Callback for querySubscriberInfo() response
     *
     * @param number
     * @param addrType
     */
    public void onSubscriberInfoRsp(int status, String number ,int addrType);

    /**
     * Callback for queryOperatorSelectionInfo response
     *
     * @param mode
     *            Indicating mode of operatior selection.
     * @param operatorName
      *            The operator name.
     */
    public void onOperatorSelectionRsp(int status, int mode, String operatorName);

    /**
     * Callback for Extended error result code
     *
     * @param errorResultCode
     *            Containing the extended result code
     * @hide
     */
    public void onExtendedErrorResult(int errorResultCode);


    /**
     * Callback for getCLCC response
     * @param status
     * @param clcc List of ClccInfo
     *         List of current call info with index, call_direction_ , call_state,
     *         call_mode_,call_mpty, number, call_addrtype
     *         getXXXX() can be used to get info from the class ClccInfo
     */
    public void onCLCCRsp(int status, List<ClccInfo> clcc);

    /**
     * Callback for Vendor/app pre-formatted AT strings.
     * Note that if app sends a pre-formatted AT command for which a
     * callback
     * is already defined above, then the response will be sent in the
     * pre-defined callback.
     * @param status
     * @param atRsp
     *            String containing the AT response
     */
    public void onVendorAtRsp(int status, String atRsp);

    /**
     * Callback for RING event(send by AG) when a HSP connection exist.
     * This will usually happen for incoming call in AG. Continous RING event
     * may be sent from AG side till the call is answered.In such case
     *.the app should take care such that it ignores the
     * the subsequent RING event if it is not necessary.
     */
    public void onRingEvent();

}

