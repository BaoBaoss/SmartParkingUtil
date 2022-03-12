package com.cetuer.parkingutil.ui.page.fingerprint;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cetuer.parkingutil.App;
import com.cetuer.parkingutil.BR;
import com.cetuer.parkingutil.R;
import com.cetuer.parkingutil.bluetooth.BleManager;
import com.cetuer.parkingutil.data.bean.BeaconPoint;
import com.cetuer.parkingutil.databinding.FragmentFingerprintBinding;
import com.cetuer.parkingutil.domain.config.DataBindingConfig;
import com.cetuer.parkingutil.domain.message.SharedViewModel;
import com.cetuer.parkingutil.ui.page.BaseFragment;
import com.cetuer.parkingutil.utils.DialogUtils;
import com.cetuer.parkingutil.utils.GpsUtils;
import com.cetuer.parkingutil.utils.KLog;
import com.cetuer.parkingutil.utils.ToastUtils;
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;

public class FingerprintFragment extends BaseFragment<FragmentFingerprintBinding> {
    /**
     * 所有的坐标
     */
    private ArrayList<ArrayList<TextView>> coordinate;
    /**
     * 上一个配置的坐标
     */
    private TextView lastConfig;
    /**
     * 上一个配置的x和y位置
     */
    private int xIndex;
    private int yIndex;
    private FingerprintViewModel mState;
    private SharedViewModel mEvent;
    private boolean mOpenBluetooth;
    private boolean mOpenGps;

    @Override
    protected void initViewModel() {
        mState = getFragmentScopeViewModel(FingerprintViewModel.class);
        mEvent = App.getInstance().getApplicationScopeViewModel(SharedViewModel.class);
        coordinate = new ArrayList<>();
    }

    @Override
    protected DataBindingConfig getDataBindingConfig() {
        return new DataBindingConfig(R.layout.fragment_fingerprint, BR.vm, mState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DialogUtils.initLoadingDialog(this.mActivity);
        //requestPermission();
        mState.beaconRequest.requestEndPoint();
        mState.beaconRequest.getEndPoint().observe(this.mActivity, this::drawMap);
        mBinding.collectBtn.setOnClickListener(v -> {

        });
//        mState.beaconRequest.getBeaconLiveData().observe(this.mActivity, beaconDevices -> {
//            BleManager.getInstance().refreshScanner();
//            BleManager.getInstance().scanByFilter(beaconDevices.stream().map(BeaconDevice::getMac).collect(Collectors.toList()));
//        });
//        BleManager.getInstance().getScanDeviceEvent().observe(this.mActivity, bleDevices -> mState.list.setValue(bleDevices));
//        mEvent.isOpenBluetooth().observe(mActivity, openBluetooth -> {
//            mOpenBluetooth = openBluetooth;
//            controlBluetooth();
//        });
//        mEvent.isOpenGPS().observe(mActivity, openGps -> {
//            mOpenGps = openGps;
//            controlBluetooth();
//        });
    }

    /**
     * 绘制地图
     *
     * @param beaconPoint 终点信标位置
     */
    private void drawMap(BeaconPoint beaconPoint) {
        //生成顶部边框
        LinearLayout topLinearLayout = new LinearLayout(this.mActivity);
        for (int i = 0; i < beaconPoint.getY(); i++) {
            View topLine = new View(this.mActivity);
            topLine.setLayoutParams(new LinearLayout.LayoutParams(202, 2));
            topLine.setBackgroundColor(Color.BLACK);
            topLinearLayout.addView(topLine);
        }
        mBinding.fingerprintMap.addView(topLinearLayout);
        //y行
        for (int i = 0; i < beaconPoint.getY(); i++) {
            LinearLayout linearLayout = new LinearLayout(this.mActivity);
            //保存本行坐标
            ArrayList<TextView> points = new ArrayList<>();
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER);
            //x列
            for (int j = 0; j < beaconPoint.getX(); j++) {
                if(j == 0) {
                    //左侧边框
                    View leftLine = new View(this.mActivity);
                    leftLine.setLayoutParams(new LinearLayout.LayoutParams(2, 202));
                    leftLine.setBackgroundColor(Color.BLACK);
                    linearLayout.addView(leftLine);
                }
                //文字信息
                TextView textView = new TextView(this.mActivity);
                textView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                textView.setText("( " + i + " , " + j + " )");
                textView.setGravity(Gravity.CENTER);
                if(i == 0 && j == 0) {
                    //默认从(0,0)开始配置
                    xIndex = 0;
                    yIndex = 0;
                    lastConfig = textView;
                    textView.setBackgroundColor(Color.rgb(255, 0, 0));
                }
                //点击配置当前位置
                textView.setOnClickListener(v -> {
                    lastConfig.setBackgroundColor(Color.rgb(255,255,255));
                    textView.setBackgroundColor(Color.rgb(255, 0, 0));
                    lastConfig = textView;
                });
                linearLayout.addView(textView);
                View verticalLine = new View(this.mActivity);
                verticalLine.setLayoutParams(new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.WRAP_CONTENT));
                verticalLine.setBackgroundColor(Color.BLACK);
                linearLayout.addView(verticalLine);
                points.add(textView);
            }
            View horizontalLine = new View(this.mActivity);
            horizontalLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
            horizontalLine.setBackgroundColor(Color.BLACK);
            mBinding.fingerprintMap.addView(linearLayout, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mBinding.fingerprintMap.addView(horizontalLine);
            coordinate.add(points);
        }
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .onForwardToSettings((scope, deniedList) -> scope.showForwardToSettingsDialog(deniedList, "您永久拒绝了以下权限，是否需要重新打开？", "设置", "取消"))
                .onExplainRequestReason((scope, deniedList) -> {
                    scope.showRequestReasonDialog(deniedList, "为了获取蓝牙定位需要如下权限", "确定", "取消");
                })
                .request((allGranted, grantedList, deniedList) -> {
                    if (!allGranted) {
                        ToastUtils.showShortToast(this.getContext(), "权限被拒绝，无法搜索蓝牙");
                    } else {
                        boolean isOpenBlueTooth = BleManager.getInstance().isBlueEnable();
                        boolean isOpenGps = GpsUtils.checkLocation(mActivity);
                        mEvent.isOpenBluetooth().setValue(isOpenBlueTooth);
                        mEvent.isOpenGPS().setValue(isOpenGps);
                        if (!isOpenBlueTooth) {
                            BleManager.getInstance().showOpenToothDialog();
                        }
                        if (!isOpenGps) {
                            ToastUtils.showShortToast(mActivity, "需要打开位置权限才可以搜索到蓝牙设备");
                        }
                    }
                });
    }

    /**
     * 控制蓝牙开启或关闭
     */
    public void controlBluetooth() {
        if (mOpenBluetooth
                && mOpenGps
                && PermissionX.isGranted(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                && PermissionX.isGranted(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mState.beaconRequest.requestBeaconList();
        }
        if (!mOpenBluetooth || !mOpenGps) {
            BleManager.getInstance().stopScan();
        }
    }
}