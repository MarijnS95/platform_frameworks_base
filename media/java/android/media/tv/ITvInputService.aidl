/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.media.tv;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.tv.ITvInputServiceCallback;
import android.media.tv.ITvInputSessionCallback;
import android.media.tv.TvInputHardwareInfo;
import android.view.InputChannel;

/**
 * Top-level interface to a TV input component (implemented in a Service).
 * @hide
 */
oneway interface ITvInputService {
    void registerCallback(ITvInputServiceCallback callback);
    void unregisterCallback(in ITvInputServiceCallback callback);
    void createSession(in InputChannel channel, ITvInputSessionCallback callback,
            in String inputId);

    // For hardware TvInputService
    void notifyHardwareAdded(in TvInputHardwareInfo hardwareInfo);
    void notifyHardwareRemoved(in TvInputHardwareInfo hardwareInfo);
    void notifyHdmiCecDeviceAdded(in HdmiDeviceInfo deviceInfo);
    void notifyHdmiCecDeviceRemoved(in HdmiDeviceInfo deviceInfo);
}
