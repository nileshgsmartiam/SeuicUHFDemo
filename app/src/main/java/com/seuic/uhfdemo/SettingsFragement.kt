package com.seuic.uhfdemo

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.seuic.uhf.UHFService

class SettingsFragement : Fragment() {
    lateinit var currentView: View
    private lateinit var mDevice: UHFService
    private lateinit var btn_readpower: Button
    private lateinit var btn_writepower: Button
    private lateinit var btn_readregion: Button
    private lateinit var btn_readtemp: Button
    private lateinit var et_power: EditText
    private lateinit var et_region: EditText
    private lateinit var et_temp: EditText
    private lateinit var tv_version: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle
    ): View? {
        mDevice = UHFService.getInstance(activity.application)
        val flag = mDevice.open()
        val view = initUI(inflater)

        // Get the firmware version number
        GetFirmwareVersion()
        // Get temperature
        GetTemperature()
        // Get power
        GetPower()
        // Set region
        SetRegion()
        // Get region
        GetRegion()
        return view
    }

    private fun initUI(inflater: LayoutInflater): View? {
        currentView = inflater.inflate(R.layout.fragment_settings, null)
        currentView.setFocusable(true)
        et_power = currentView.findViewById<View>(R.id.et_power) as EditText
        et_region = currentView.findViewById<View>(R.id.et_region) as EditText
        et_temp = currentView.findViewById<View>(R.id.et_temperature) as EditText
        tv_version = currentView.findViewById<View>(R.id.tv_version) as TextView
        btn_readpower = currentView.findViewById<View>(R.id.bt_readpower) as Button
        btn_readpower!!.setOnClickListener(MyClickListener())
        btn_writepower = currentView.findViewById<View>(R.id.bt_writepower) as Button
        btn_writepower!!.setOnClickListener(MyClickListener())
        btn_readregion = currentView.findViewById<View>(R.id.bt_getregion) as Button
        btn_readregion!!.setOnClickListener(MyClickListener())
        btn_readtemp = currentView.findViewById<View>(R.id.bt_gettemperature) as Button
        btn_readtemp!!.setOnClickListener(MyClickListener())
        return currentView
    }

    //  Get the firmware version number
    fun GetFirmwareVersion() {
        if (!mDevice!!.isopen()) {
            mDevice!!.open()
        }
        val version = mDevice!!.firmwareVersion // .trim();
        if (version == null || version === "") {
            return
        }
        tv_version!!.text = version.trim { it <= ' ' }
        println(version)
    }

    // Get temperature
    fun GetTemperature() {
        if (!mDevice!!.isopen()) {
            mDevice!!.open()
        }
        val temperature = mDevice!!.temperature
        if (temperature == null || temperature === "") {
            println(getString(R.string.RfidGetTemperature_faild))
            return
        }
        et_temp!!.setText(temperature)
        println(getString(R.string.temperature) + temperature)
    }

    // Get power
    fun GetPower() {
        if (!mDevice!!.isopen()) {
            mDevice!!.open()
        }
        val power = mDevice!!.power
        if (power == 0) {
            println(getString(R.string.RfidGetPower_faild))
        }
        et_power!!.setText(power.toString() + "")
        println(getString(R.string.power) + power)
    }

    // Set power
    fun SetPower() {
        if (!mDevice!!.isopen()) {
            mDevice!!.open()
        }
        val value = et_power!!.text.toString().trim { it <= ' ' }
        if (!value.isEmpty()) {
            val power = Integer.valueOf(value)
            println(getString(R.string.RfidSetPower) + power)
            val ret = mDevice!!.setPower(power)
            Log.i("zy", "SetPower=$ret")
            if (!ret) {
                println(getString(R.string.RfidGetPower_faild))
            }
        } else {
            Toast.makeText(activity, R.string.please_input_power, Toast.LENGTH_SHORT).show()
        }
    }

    // Get region
    fun GetRegion() {
        if (!mDevice!!.isopen()) {
            mDevice!!.open()
        }
        val region = mDevice!!.region
        if (region == null) {
            println(getString(R.string.RfidGetRegion_faild))
            return
        }
        et_region!!.setText(region)
        println(getString(R.string.region) + region)
    }

    // Set region
    fun SetRegion() {
        if (!mDevice!!.isopen()) {
            mDevice!!.open()
        }
        val region = getString(R.string.fcc)
        val ret = mDevice!!.setRegion(region)
        if (!ret) {
            println(getString(R.string.RfidGetRegion_faild))
        }
    }

    private inner class MyClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.bt_readpower -> GetPower()
                R.id.bt_writepower -> SetPower()
                R.id.bt_getregion -> GetRegion()
                R.id.bt_gettemperature -> GetTemperature()
                else -> {}
            }
        }
    }

    companion object {
        private lateinit var settingsfragement: SettingsFragement
        val instance: SettingsFragement?
            get() {
                if (settingsfragement == null) settingsfragement = SettingsFragement()
                return settingsfragement
            }
    }
}