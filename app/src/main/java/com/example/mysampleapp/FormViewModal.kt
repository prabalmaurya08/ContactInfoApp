package com.example.mysampleapp

import androidx.lifecycle.ViewModel

class FormViewModel : ViewModel() {
    var gender: Int? = null
    var age: Int? = null
    var selfiePath: String? = null
    var audioFilePath: String = ""
    var gpsLocation: String = ""
    var submitTime: String = ""
}