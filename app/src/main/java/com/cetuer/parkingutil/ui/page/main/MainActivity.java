package com.cetuer.parkingutil.ui.page.main;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.cetuer.parkingutil.BR;
import com.cetuer.parkingutil.R;
import com.cetuer.parkingutil.bluetooth.BleManager;
import com.cetuer.parkingutil.bluetooth.BlueToothReceiver;
import com.cetuer.parkingutil.databinding.ActivityMainBinding;
import com.cetuer.parkingutil.domain.config.DataBindingConfig;
import com.cetuer.parkingutil.ui.adapter.FragmentViewPagerAdapter;
import com.cetuer.parkingutil.ui.page.find_car.FindCarFragment;
import com.cetuer.parkingutil.ui.page.carport_query.CarportQueryFragment;
import com.cetuer.parkingutil.ui.page.BaseActivity;
import com.cetuer.parkingutil.ui.page.fingerprint.FingerprintFragment;
import com.cetuer.parkingutil.ui.page.mine.MineFragment;
import com.cetuer.parkingutil.utils.ToastUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主Activity，切换Fragment
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private BlueToothReceiver mBlueToothReceiver;
    /**
     * 记录MainActivity状态
     */
    private MainActivityViewModel mState;


     /**
     * 再按一次退出程序
     */
    private long mExitTime;

    @Override
    protected void initViewModel() {
        mState = getActivityScopeViewModel(MainActivityViewModel.class);
    }

    @Override
    protected DataBindingConfig getDataBindingConfig() {
        return new DataBindingConfig(R.layout.activity_main, BR.vm, mState)
                .addBindingParam(BR.listener, new HandleListener());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BleManager.getInstance().init(this);

        FragmentViewPagerAdapter viewPagerAdapter = new FragmentViewPagerAdapter(this);
        viewPagerAdapter.addFragment(new FingerprintFragment());
        viewPagerAdapter.addFragment(new FindCarFragment());
        viewPagerAdapter.addFragment(new CarportQueryFragment());
        viewPagerAdapter.addFragment(new MineFragment());
        mBinding.mainViewPager.setAdapter(viewPagerAdapter);

        //注册广播
        mBlueToothReceiver = new BlueToothReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mBlueToothReceiver, intentFilter);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册广播
        unregisterReceiver(mBlueToothReceiver);
    }

    /**
     * 再按一次退出程序
     */
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mExitTime < 2000) {
            super.onBackPressed();
        } else {
            mExitTime = System.currentTimeMillis();
            ToastUtils.showShortToast(this,  "再按一次退出应用");
        }
    }

    /**
     * 监听内部类
     */
    public class HandleListener extends ViewPager2.OnPageChangeCallback implements BottomNavigationView.OnNavigationItemSelectedListener {

        /**
         * nav选中时切换viewPage
         * @param item 选中的item
         * @return 当前页面是否可用
         */
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.navigation_fingerprint) {
                mBinding.mainViewPager.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.navigation_reverse_for_car) {
                mBinding.mainViewPager.setCurrentItem(1);
                return true;
            } else if (item.getItemId() == R.id.navigation_carport_query) {
                mBinding.mainViewPager.setCurrentItem(2);
                return true;
            } else if (item.getItemId() == R.id.navigation_mine) {
                mBinding.mainViewPager.setCurrentItem(3);
                return true;
            } else
                return false;
        }

        /**
         * viewPage选中时切换nav
         * @param position 选中下标
         */
        @Override
        public void onPageSelected(int position) {
            mBinding.navView.getMenu().getItem(position).setChecked(true);
        }
    }
}