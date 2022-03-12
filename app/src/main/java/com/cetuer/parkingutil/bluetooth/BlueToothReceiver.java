package com.cetuer.parkingutil.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.cetuer.parkingutil.App;
import com.cetuer.parkingutil.domain.message.SharedViewModel;
import com.cetuer.parkingutil.utils.GpsUtils;
import com.cetuer.parkingutil.utils.KLog;

public class BlueToothReceiver extends BroadcastReceiver {
    private SharedViewModel mEvent;


    public BlueToothReceiver() {
        mEvent = App.getInstance().getApplicationScopeViewModel(SharedViewModel.class);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
                    KLog.i("STATE_TURNING_OFF");
                    break;
                case BluetoothAdapter.STATE_OFF:
                    KLog.i("STATE_OFF");
                    mEvent.isOpenBluetooth().setValue(false);
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    KLog.i("STATE_TURNING_ON");
                    break;
                case BluetoothAdapter.STATE_ON:
                    KLog.i("STATE_ON");
                    mEvent.isOpenBluetooth().setValue(true);
                    break;
            }
        } else if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            mEvent.isOpenGPS().setValue(GpsUtils.checkLocation(context));
        }
    }
}