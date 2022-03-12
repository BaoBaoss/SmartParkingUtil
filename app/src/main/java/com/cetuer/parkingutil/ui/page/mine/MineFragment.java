package com.cetuer.parkingutil.ui.page.mine;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cetuer.parkingutil.BR;
import com.cetuer.parkingutil.R;
import com.cetuer.parkingutil.databinding.FragmentMineBinding;
import com.cetuer.parkingutil.domain.config.DataBindingConfig;
import com.cetuer.parkingutil.ui.page.BaseFragment;

public class MineFragment extends BaseFragment<FragmentMineBinding> {

    private MineViewModel mState;

    @Override
    protected void initViewModel() {
        mState = getFragmentScopeViewModel(MineViewModel.class);
    }

    @Override
    protected DataBindingConfig getDataBindingConfig() {
        return new DataBindingConfig(R.layout.fragment_mine, BR.vm, mState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mState.getText().observe(getViewLifecycleOwner(), s -> {

        });
    }
}