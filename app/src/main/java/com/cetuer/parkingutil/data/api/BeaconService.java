package com.cetuer.parkingutil.data.api;

import com.cetuer.parkingutil.data.bean.BeaconDevice;
import com.cetuer.parkingutil.data.bean.BeaconPoint;
import com.cetuer.parkingutil.data.response.ResultData;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Cetuer on 2022/3/6 15:57.
 * 信标服务
 */
public interface BeaconService {

    /**
     * 获取所有信标设备信息
     * @return 信标设备信息
     */
    @GET("beacon/list")
    Call<ResultData<List<BeaconDevice>>> list();

    /**
     * 获取所有信标终点信息
     * @return 信标终点信息
     */
    @GET("beacon/endPoint")
    Call<ResultData<BeaconPoint>> endPoint();
}
