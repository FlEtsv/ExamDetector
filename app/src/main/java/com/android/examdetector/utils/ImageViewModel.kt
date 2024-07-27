package com.android.examdetector.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    val image: MutableLiveData<String> = MutableLiveData()
    val isProcessing: MutableLiveData<Boolean> = MutableLiveData(false)
}