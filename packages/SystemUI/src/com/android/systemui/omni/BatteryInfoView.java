package com.android.systemui.omni;

import android.os.BatteryManager;

import static android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN;
import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_FULL;
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
import static android.os.BatteryManager.EXTRA_HEALTH;
import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_MAX_CHARGING_CURRENT;
import static android.os.BatteryManager.EXTRA_MAX_CHARGING_VOLTAGE;
import static android.os.BatteryManager.EXTRA_VOLTAGE;
import static android.os.BatteryManager.EXTRA_PLUGGED;
import static android.os.BatteryManager.EXTRA_STATUS;
import static android.os.BatteryManager.EXTRA_TEMPERATURE;
import static android.os.BatteryManager.EXTRA_CURRENT;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
// import android.content.res.Resources;
// import android.database.ContentObserver;
// import android.graphics.PorterDuff.Mode;
// import android.graphics.Rect;
// import android.net.Uri;
// import android.os.Handler;
// import android.os.UserHandle;
// import android.os.Message;
// import android.os.SystemClock;
// import android.provider.Settings;
import android.util.AttributeSet;
// import android.util.TypedValue;
// import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.systemui.R;

import com.android.settingslib.graph.BatteryMeterDrawableBase;
import android.content.res.TypedArray;

public class BatteryInfoView extends TextView {
    private static final int DEFAULT_CHARGING_VOLTAGE_MICRO_VOLT = 5000000;
    private int mTintColor;
    private boolean mAttached;
    private final BatteryMeterDrawableBase mDrawable;

    /*
     * @hide
     */
    public BatteryInfoView(Context context) {
        this(context, null);
    }

    /*
     * @hide
     */
    public BatteryInfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /*
     * @hide
     */
    public BatteryInfoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray atts = context.obtainStyledAttributes(attrs, R.styleable.BatteryMeterView, defStyle, 0);
        final int frameColor = atts.getColor(R.styleable.BatteryMeterView_frameColor,
                context.getColor(R.color.meter_background_color));
        mDrawable = new BatteryMeterDrawableBase(context, frameColor);
        atts.recycle();
        // setCompoundDrawablesWithIntrinsicBounds(mDrawable, 0, 0, 0);
        setCompoundDrawables(mDrawable, null, null, null);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                // TODO: Use handler?

                final int status = intent.getIntExtra(EXTRA_STATUS, BATTERY_STATUS_UNKNOWN);
                final int plugged = intent.getIntExtra(EXTRA_PLUGGED, 0);
                final int level = intent.getIntExtra(EXTRA_LEVEL, 0);
                final int temperature = intent.getIntExtra(EXTRA_TEMPERATURE, Integer.MIN_VALUE);
                // NOTE: HealthInfo documentation says healthInfo.batteryVoltage is in uV,
                // but it is divided by 1000 in healthd and thus in mV. Same for current.
                // See the kernel ABI: sysfs paths provide info in micro{volts,amps},
                // but there's an erroneous division:
                // https://android.googlesource.com/platform/system/core/+/android-9.0.0_r35/healthd/BatteryMonitor.cpp#214
                final int currentMilliAmp = intent.getIntExtra(EXTRA_CURRENT, -1);
                final int currentMilliVolt = intent.getIntExtra(EXTRA_VOLTAGE, -1);

                final int maxChargingMicroAmp = intent.getIntExtra(EXTRA_MAX_CHARGING_CURRENT, -1);
                final int maxChargingMicroVolt = intent.getIntExtra(EXTRA_MAX_CHARGING_VOLTAGE,
                        DEFAULT_CHARGING_VOLTAGE_MICRO_VOLT);
                final boolean extendedInfoShowWatt = false;

                // boolean isChargingOrFull = status == BatteryManager.BATTERY_STATUS_CHARGING
                // || status == BatteryManager.BATTERY_STATUS_FULL;

                boolean isPluggedIn = plugged == BatteryManager.BATTERY_PLUGGED_AC
                        || plugged == BatteryManager.BATTERY_PLUGGED_USB
                        || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;

                String indication = "";

                mDrawable.setBatteryLevel(level);
                mDrawable.setCharging(isPluggedIn);

                // if (status == BatteryManager.BATTERY_STATUS_CHARGING) { // !mPowerCharged
                if (isPluggedIn) { // !mPowerCharged
                    // If the device is not fully charged, show maximum voltage and amperage:
                    // (This will/should be zero when the device is charged)
                    // " • %.1fA • %.1fV"
                    // TODO: Localize "at"
                    // indication = "Charging";
                    // indication += String.format(" at %.3fA, %.1fV", maxChargingMicroAmp /
                    // 1000000.0f,
                    // maxChargingMicroVolt / 1000000.0f);
                    indication = String.format("%.3fA, %.1fV", maxChargingMicroAmp / 1000000.0f,
                            maxChargingMicroVolt / 1000000.0f);

                    indication += " • ";
                }

                indication += String.format("%.1f°C", temperature / 10.f);
                if (extendedInfoShowWatt) {
                    indication += String.format(" • %.3fW", currentMilliVolt * currentMilliAmp / 1000000.0f);
                } else {
                    indication += String.format(" • %.3fV", currentMilliVolt / 1000.0f);
                    indication += String.format(" • %dmA", currentMilliAmp);
                }

                if (!indication.equals(getText())) {
                    setText(indication);
                }
            }
        }
    };

    public void updateSettings() {
        // ContentResolver resolver = mContext.getContentResolver();

        boolean mEnabled = true;/*
                                 * Settings.System.getIntForUser(resolver,
                                 * Settings.System.OMNI_NETWORK_TRAFFIC_ENABLE, 0, UserHandle.USER_CURRENT) !=
                                 * 0;
                                 */

        if (mEnabled) {
            if (!mAttached) {
                mAttached = true;
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_BATTERY_CHANGED);
                mContext.registerReceiver(mIntentReceiver, filter, null, getHandler());
            }
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
            if (mAttached) {
                mContext.unregisterReceiver(mIntentReceiver);
                mAttached = false;
            }
        }
    }
}
