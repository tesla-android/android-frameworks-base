/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.systemui.media.dialog;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.android.settingslib.Utils;
import com.android.settingslib.media.LocalMediaManager.MediaDeviceState;
import com.android.settingslib.media.MediaDevice;
import com.android.systemui.R;

import java.util.List;

/**
 * Adapter for media output dialog.
 */
public class MediaOutputAdapter extends MediaOutputBaseAdapter {

    private static final String TAG = "MediaOutputAdapter";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public MediaOutputAdapter(MediaOutputController controller) {
        super(controller);
    }

    @Override
    public MediaDeviceBaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup,
            int viewType) {
        super.onCreateViewHolder(viewGroup, viewType);

        return new MediaDeviceViewHolder(mHolderView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaDeviceBaseViewHolder viewHolder, int position) {
        final int size = mController.getMediaDevices().size();
        if (mController.isZeroMode() && position == size) {
            viewHolder.onBind(CUSTOMIZED_ITEM_PAIR_NEW, false /* topMargin */,
                    true /* bottomMargin */);
        } else if (position < size) {
            viewHolder.onBind(((List<MediaDevice>) (mController.getMediaDevices())).get(position),
                    position == 0 /* topMargin */, position == (size - 1) /* bottomMargin */);
        } else if (DEBUG) {
            Log.d(TAG, "Incorrect position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        if (mController.isZeroMode()) {
            // Add extra one for "pair new"
            return mController.getMediaDevices().size() + 1;
        }
        return mController.getMediaDevices().size();
    }

    void onItemClick(MediaDevice device) {
        mController.connectDevice(device);
        device.setState(MediaDeviceState.STATE_CONNECTING);
        notifyDataSetChanged();
    }

    void onItemClick(int customizedItem) {
        if (customizedItem == CUSTOMIZED_ITEM_PAIR_NEW) {
            mController.launchBluetoothPairing();
        }
    }

    @Override
    CharSequence getItemTitle(MediaDevice device) {
        if (device.getDeviceType() == MediaDevice.MediaDeviceType.TYPE_BLUETOOTH_DEVICE
                && !device.isConnected()) {
            final CharSequence deviceName = device.getName();
            // Append status to title only for the disconnected Bluetooth device.
            final SpannableString spannableTitle = new SpannableString(
                    mContext.getString(R.string.media_output_dialog_disconnected, deviceName));
            spannableTitle.setSpan(new ForegroundColorSpan(
                    Utils.getColorAttrDefaultColor(mContext, android.R.attr.textColorSecondary)),
                    deviceName.length(),
                    spannableTitle.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannableTitle;
        }
        return super.getItemTitle(device);
    }

    class MediaDeviceViewHolder extends MediaDeviceBaseViewHolder {

        MediaDeviceViewHolder(View view) {
            super(view);
        }

        @Override
        void onBind(MediaDevice device, boolean topMargin, boolean bottomMargin) {
            super.onBind(device, topMargin, bottomMargin);
            if (mController.isTransferring()) {
                if (device.getState() == MediaDeviceState.STATE_CONNECTING
                        && !mController.hasAdjustVolumeUserRestriction()) {
                    setTwoLineLayout(device, null /* title */, true /* bFocused */,
                            false /* showSeekBar*/, true /* showProgressBar */,
                            false /* showSubtitle */);
                } else {
                    setSingleLineLayout(getItemTitle(device), false /* bFocused */);
                }
            } else {
                // Set different layout for each device
                if (device.getState() == MediaDeviceState.STATE_CONNECTING_FAILED) {
                    setTwoLineLayout(device, null /* title */, false /* bFocused */,
                            false /* showSeekBar*/, false /* showProgressBar */,
                            true /* showSubtitle */);
                    mSubTitleText.setText(R.string.media_output_dialog_connect_failed);
                    mFrameLayout.setOnClickListener(v -> onItemClick(device));
                } else if (!mController.hasAdjustVolumeUserRestriction()
                        && isCurrentConnected(device)) {
                    setTwoLineLayout(device, null /* title */, true /* bFocused */,
                            true /* showSeekBar*/, false /* showProgressBar */,
                            false /* showSubtitle */);
                    initSeekbar(device);
                } else {
                    setSingleLineLayout(getItemTitle(device), false /* bFocused */);
                    mFrameLayout.setOnClickListener(v -> onItemClick(device));
                }
            }
        }

        @Override
        void onBind(int customizedItem, boolean topMargin, boolean bottomMargin) {
            super.onBind(customizedItem, topMargin, bottomMargin);
            if (customizedItem == CUSTOMIZED_ITEM_PAIR_NEW) {
                setSingleLineLayout(mContext.getText(R.string.media_output_dialog_pairing_new),
                        false /* bFocused */);
                final Drawable d = mContext.getDrawable(R.drawable.ic_add);
                d.setColorFilter(new PorterDuffColorFilter(
                        Utils.getColorAccentDefaultColor(mContext), PorterDuff.Mode.SRC_IN));
                mTitleIcon.setImageDrawable(d);
                mFrameLayout.setOnClickListener(v -> onItemClick(CUSTOMIZED_ITEM_PAIR_NEW));
            }
        }
    }
}
