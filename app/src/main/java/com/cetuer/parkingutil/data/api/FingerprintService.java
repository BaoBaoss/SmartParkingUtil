package com.cetuer.parkingutil.data.api;

import com.cetuer.parkingutil.data.bean.BeaconPoint;
import com.cetuer.parkingutil.data.response.ResultData;
import com.cetuer.parkingutil.domain.BeaconCollect;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Cetuer on 2022/3/19 19:13.
 * 指纹服务
 */
public interface FingerprintService {

    /**
     * 发送指纹数据
     * @param fingerprintData 指纹数据
     * @return 无
     */
    @POST("fingerprint/collect")
    Call<ResultData<Void>> sendFingerprintData(@Body Map<String, Map<String, Double>> fingerprintData);
}
