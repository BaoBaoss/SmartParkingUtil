package com.cetuer.parkingutil.ui.page.fingerprint;

import android.Manifest;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cetuer.parkingutil.App;
import com.cetuer.parkingutil.BR;
import com.cetuer.parkingutil.R;
import com.cetuer.parkingutil.bluetooth.BleDevice;
import com.cetuer.parkingutil.bluetooth.BleManager;
import com.cetuer.parkingutil.data.bean.BeaconDevice;
import com.cetuer.parkingutil.data.bean.BeaconPoint;
import com.cetuer.parkingutil.data.repository.DataRepository;
import com.cetuer.parkingutil.databinding.FragmentFingerprintBinding;
import com.cetuer.parkingutil.domain.BeaconCollect;
import com.cetuer.parkingutil.domain.CoordinateInfo;
import com.cetuer.parkingutil.domain.config.DataBindingConfig;
import com.cetuer.parkingutil.domain.message.SharedViewModel;
import com.cetuer.parkingutil.ui.page.BaseFragment;
import com.cetuer.parkingutil.utils.DialogUtils;
import com.cetuer.parkingutil.utils.GpsUtils;
import com.cetuer.parkingutil.utils.KLog;
import com.cetuer.parkingutil.utils.ToastUtils;
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FingerprintFragment extends BaseFragment<FragmentFingerprintBinding> {
    /**
     * 所有的坐标
     */
    private ArrayList<ArrayList<CoordinateInfo>> coordinate;
    /**
     * 当前配置的TextView
     */
    private TextView curConfig;
    /**
     * 当前配置的位置
     */
    private BeaconPoint curPoint;
    /**
     * 终点坐标
     */
    private BeaconPoint endPoint;
    /**
     * 已收集的指纹
     */
    private Map<BeaconPoint, Map<String, BeaconCollect>> fingerprintMap;
    /**
     * 已完成收集的蓝牙数量
     */
    private int completeCount;
    /**
     * 收集的次数，用于进度条计算
     */
    private int collectCount;
    /**
     * 信标总数
     */
    private int beaconCount;

    private List<BeaconDevice> deviceList;

    private FingerprintViewModel mState;
    private SharedViewModel mEvent;
    private boolean mOpenBluetooth;
    private boolean mOpenGps;
    private Integer parkingLotId;

    @Override
    protected void initViewModel() {
        mState = getFragmentScopeViewModel(FingerprintViewModel.class);
        mEvent = App.getInstance().getApplicationScopeViewModel(SharedViewModel.class);
        coordinate = new ArrayList<>();
        fingerprintMap = new HashMap<>();
    }

    @Override
    protected DataBindingConfig getDataBindingConfig() {
        return new DataBindingConfig(R.layout.fragment_fingerprint, BR.vm, mState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DialogUtils.initLoadingDialog(this.mActivity);
        DialogUtils.showBasicDialog(this.mActivity, "提示", "请输入需要采集的停车场id")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input("停车场编号", "", (dialog, input) -> {
                    try {
                        parkingLotId = Integer.parseInt(input.toString());
                        //请求最终坐标
                        mState.beaconRequest.requestEndPoint(parkingLotId);

                        mEvent.isOpenBluetooth().observe(mActivity, openBluetooth -> {
                            mOpenBluetooth = openBluetooth;
                            controlBluetooth();
                        });
                        mEvent.isOpenGPS().observe(mActivity, openGps -> {
                            mOpenGps = openGps;
                            controlBluetooth();
                        });
                    } catch (Exception e ) {
                        e.printStackTrace();
                        ToastUtils.showShortToast(FingerprintFragment.this.mActivity, "请输入正确的停车场编号！");
                    }

                }).show();
        //得到坐标后绘制地图
        mState.beaconRequest.getEndPoint().observe(this.mActivity, end -> {
            if(end.getX() == 0 || end.getY() == 0) {
                ToastUtils.showShortToast(this.mActivity, "此停车场没有信标");
                return;
            }
            mBinding.collectBtn.setEnabled(true);
            this.endPoint = end;
            this.drawMap(end);
        });
        //采集指纹请求权限
        mBinding.collectBtn.setOnClickListener(v -> {
            fingerprintMap.put(curPoint, new HashMap<>());
            requestPermission();
        });
        //提交数据
        mBinding.submitBtn.setOnClickListener(v -> {
            Map<String, Map<String, Double>> data = new HashMap<>();
            for (BeaconPoint next : fingerprintMap.keySet()) {
                Map<String, BeaconCollect> collectMap = fingerprintMap.get(next);
                Map<String, Double> map = new HashMap<>();
                for (String next1 : collectMap.keySet()) {
                    map.put(next1, collectMap.get(next1).getAvg());
                }
                data.put(next.getX() + "," + next.getY(), map);
            }
            DataRepository.getInstance().sendFingerprintData(data, this.mActivity);
        });
        //根据请求到的蓝牙mac扫描蓝牙设备
        mState.beaconRequest.getBeaconLiveData().observe(this.mActivity, beaconDevices -> {
            deviceList = beaconDevices;
            beaconCount = beaconDevices.size();
            DialogUtils.showProgressDialog();
            BleManager.getInstance().refreshScanner();
            BleManager.getInstance().scanByFilter(beaconDevices.stream().map(BeaconDevice::getMac).collect(Collectors.toList()));
        });
        //扫描蓝牙回调
        BleManager.getInstance().getScanDeviceEvent().observe(this.mActivity, bleDevices -> mState.list.setValue(bleDevices));
        //开始收集
        mState.list.observe(this.mActivity, this::collectData);
    }

    /**
     * 收集数据
     *
     * @param data 扫描到的数据
     */
    public void collectData(List<BleDevice> data) {
        collectCount += data.size();
        //刷新进度条
        DialogUtils.setProgressValue((int) ((collectCount / (beaconCount * BeaconCollect.MAX * 1.0)) * 100));
        //为对应的信标增加数据
        for (BleDevice beaconDevice : data) {
            Map<String, BeaconCollect> map = fingerprintMap.get(curPoint);
            if (map != null) {
                String mac = beaconDevice.getDevice().getAddress();
                if (map.containsKey(mac)) {
                    map.get(mac).add(beaconDevice.getRssi());
                } else {
                    BeaconCollect collect = new BeaconCollect(mac);
                    collect.setOnComplete(() -> {
                        completeCount++;
                        //此坐标所有数据收集完成
                        if (completeCount == beaconCount) {
                            BleManager.getInstance().stopScan();
                            DialogUtils.dismissProgressDialog();
                            ToastUtils.showShortToast(this.mActivity, "(" + curPoint.getX() + "," + curPoint.getY() + ") 收集完成");
                            coordinate.get(curPoint.getX()).get(curPoint.getY()).setCollect(true);
                            jumpNext();
                        }
                    });
                    map.put(mac, collect);
                }
            }
        }
    }

    /**
     * 初始化收集信息
     */
    public void initCollectInfo() {
        this.completeCount = 0;
        this.collectCount = 0;
    }

    /**
     * 跳转到下一个未完成的地址
     */
    public void jumpNext() {
        initCollectInfo();
        //当前坐标变绿，下一个未设置的变红
        curConfig.setBackgroundColor(Color.GREEN);
        int x = curPoint.getX();
        int y = curPoint.getY();
        while (true) {
            //到行尾，跳转下一行
            if (y + 1 >= endPoint.getY()) {
                //到最后一个
                if (x + 1 >= endPoint.getX()) {
                    //检测之前是否有未收集的
                    boolean flag = true;
                    for (int i = 0; i < coordinate.size() && flag; i++) {
                        for (int j = 0; j < coordinate.get(i).size() && flag; j++) {
                            if (!coordinate.get(i).get(j).isCollect()) flag = false;
                        }
                    }
                    if (!flag) {
                        x = 0;
                        y = 0;
                        continue;
                    }
                    curConfig.setBackgroundColor(Color.RED);
                    ToastUtils.showLongToast(this.mActivity, "所有坐标均已收集完成");
                    mBinding.submitBtn.setEnabled(true);
                    break;
                } else {
                    x++;
                    y = 0;
                    if (coordinate.get(x).get(y).isCollect()) continue;
                    coordinate.get(x).get(y).getTextView().setBackgroundColor(Color.RED);
                    curConfig = coordinate.get(x).get(y).getTextView();
                    curPoint = new BeaconPoint(x, y);
                    break;
                }
            } else {
                y++;
                if (coordinate.get(x).get(y).isCollect()) continue;
                coordinate.get(x).get(y).getTextView().setBackgroundColor(Color.RED);
                curConfig = coordinate.get(x).get(y).getTextView();
                curPoint = new BeaconPoint(x, y);
                break;
            }
        }
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
        for (int i = 0; i < beaconPoint.getX(); i++) {
            LinearLayout linearLayout = new LinearLayout(this.mActivity);
            //保存本行坐标
            ArrayList<CoordinateInfo> points = new ArrayList<>();
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER);
            //x列
            for (int j = 0; j < beaconPoint.getY(); j++) {
                if (j == 0) {
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
                if (i == 0 && j == 0) {
                    //默认从(0,0)开始配置
                    curPoint = new BeaconPoint(0, 0);
                    curConfig = textView;
                    textView.setBackgroundColor(Color.rgb(255, 0, 0));
                }
                //点击配置当前位置
                int finalI = i;
                int finalJ = j;
                textView.setOnClickListener(v -> {
                    if (!coordinate.get(curPoint.getX()).get(curPoint.getY()).isCollect()) {
                        curConfig.setBackgroundColor(Color.WHITE);
                    } else {
                        //已收集的为绿色
                        curConfig.setBackgroundColor(Color.GREEN);
                    }
                    textView.setBackgroundColor(Color.RED);
                    curConfig = textView;
                    curPoint = new BeaconPoint(finalI, finalJ);
                });
                linearLayout.addView(textView);
                View verticalLine = new View(this.mActivity);
                verticalLine.setLayoutParams(new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.WRAP_CONTENT));
                verticalLine.setBackgroundColor(Color.BLACK);
                linearLayout.addView(verticalLine);
                points.add(new CoordinateInfo(textView));
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
                    scope.showRequestReasonDialog(deniedList, "为了采集指纹需要如下权限", "确定", "取消");
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
            if (deviceList != null && !BleManager.getInstance().isScanning()) {
                DialogUtils.showProgressDialog();
                BleManager.getInstance().refreshScanner();
                BleManager.getInstance().scanByFilter(deviceList.stream().map(BeaconDevice::getMac).collect(Collectors.toList()));
                return;
            }
            if (deviceList == null) {
                mState.beaconRequest.requestBeaconList(parkingLotId);
            }
        }
        if (!mOpenBluetooth || !mOpenGps) {
            BleManager.getInstance().stopScan();
        }
    }
}