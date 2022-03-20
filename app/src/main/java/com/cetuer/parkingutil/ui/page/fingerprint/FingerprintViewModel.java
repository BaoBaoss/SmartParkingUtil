package com.cetuer.parkingutil.ui.page.fingerprint;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cetuer.parkingutil.bluetooth.BleDevice;
import com.cetuer.parkingutil.data.request.BeaconRequest;
import com.cetuer.parkingutil.data.request.FingerprintRequest;

import java.util.List;

public class FingerprintViewModel extends ViewModel {

    public final MutableLiveData<List<BleDevice>> list = new MutableLiveData<>();
    public final BeaconRequest beaconRequest = new BeaconRequest();
    public final FingerprintRequest fingerprintRequest = new FingerprintRequest();
}