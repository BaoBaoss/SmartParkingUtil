package com.cetuer.parkingutil.ui.page.find_car;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FindCarViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FindCarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}