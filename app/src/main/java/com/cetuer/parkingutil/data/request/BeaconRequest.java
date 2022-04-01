package com.cetuer.parkingutil.data.request;

import androidx.lifecycle.MutableLiveData;

import com.cetuer.parkingutil.data.bean.BeaconDevice;
import com.cetuer.parkingutil.data.bean.BeaconPoint;
import com.cetuer.parkingutil.data.repository.DataRepository;

import java.util.List;
import java.util.Map;

/**
 * Created by Cetuer on 2022/3/6 21:49.
 * 信标 Request
 */
public class BeaconRequest implements BaseRequest {
    private final MutableLiveData<List<BeaconDevice>> beaconLiveData = new MutableLiveData<>();

    private final MutableLiveData<BeaconPoint> endPoint = new MutableLiveData<>();

    /**
     * 请求信标列表
     */
    public void requestBeaconList(Integer parkingLotId) {
        DataRepository.getInstance().listBeacon(beaconLiveData::postValue, parkingLotId);
    }

    /**
     * 请求终点坐标
     */
    public void requestEndPoint(Integer parkingLotId) {
        DataRepository.getInstance().endPointByParkingLotId(endPoint::postValue, parkingLotId);
    }

    public MutableLiveData<List<BeaconDevice>> getBeaconLiveData() {
        return beaconLiveData;
    }

    public MutableLiveData<BeaconPoint> getEndPoint() {
        return endPoint;
    }
}
