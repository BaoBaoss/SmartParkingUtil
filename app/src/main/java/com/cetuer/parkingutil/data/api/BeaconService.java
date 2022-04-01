package com.cetuer.parkingutil.data.api;

import com.cetuer.parkingutil.data.bean.BeaconDevice;
import com.cetuer.parkingutil.data.bean.BeaconPoint;
import com.cetuer.parkingutil.data.response.ResultData;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Cetuer on 2022/3/6 15:57.
 * 信标服务
 */
public interface BeaconService {

    /**
     * 根据停车场编号获取其所有信标设备信息
     * @param parkingLotId 停车场编号
     * @return 信标设备信息
     */
    @GET("beacon/listByParkingLotId/{parkingLotId}")
    Call<ResultData<List<BeaconDevice>>> listByParkingLotId(@Path("parkingLotId") Integer parkingLotId);

    /**
     * 获取所有信标终点信息
     * @return 信标终点信息
     */
    @GET("beacon/endPointByParkingLotId/{parkingLotId}")
    Call<ResultData<BeaconPoint>> endPointByParkingLotId(@Path("parkingLotId") Integer parkingLotId);
}
