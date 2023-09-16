package com.seuic.uhfdemo;

import com.seuic.uhf.UHFService;

import android.app.Fragment;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragement extends Fragment {

	private UHFService mDevice;

	private Button btn_readpower;
	private Button btn_writepower;
	private Button btn_readregion;
	private Button btn_readtemp;

	private EditText et_power;
	private EditText et_region;
	private EditText et_temp;

	private TextView tv_version;

	View currentView;

	private static SettingsFragement settingsfragement;

	public static SettingsFragement getInstance() {
		if (settingsfragement == null)
			settingsfragement = new SettingsFragement();
		return settingsfragement;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mDevice = UHFService.getInstance(getActivity().getApplication());
		boolean flag = mDevice.open();
		View view = initUI(inflater);

		// Get the firmware version number
		GetFirmwareVersion();
		// Get temperature
		GetTemperature();
		// Get power
		GetPower();
		// Set region
		SetRegion();
		// Get region
		GetRegion();

		return view;
	}

	private View initUI(LayoutInflater inflater) {
		currentView = inflater.inflate(R.layout.fragment_settings, null);
		currentView.setFocusable(true);

		et_power = (EditText) currentView.findViewById(R.id.et_power);
		et_region = (EditText) currentView.findViewById(R.id.et_region);
		et_temp = (EditText) currentView.findViewById(R.id.et_temperature);

		tv_version = (TextView) currentView.findViewById(R.id.tv_version);

		btn_readpower = (Button) currentView.findViewById(R.id.bt_readpower);
		btn_readpower.setOnClickListener(new MyClickListener());

		btn_writepower = (Button) currentView.findViewById(R.id.bt_writepower);
		btn_writepower.setOnClickListener(new MyClickListener());

		btn_readregion = (Button) currentView.findViewById(R.id.bt_getregion);
		btn_readregion.setOnClickListener(new MyClickListener());

		btn_readtemp = (Button) currentView.findViewById(R.id.bt_gettemperature);
		btn_readtemp.setOnClickListener(new MyClickListener());

		return currentView;
	}

	private class MyClickListener implements OnClickListener {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_readpower:
				GetPower();
				break;
			case R.id.bt_writepower:
				SetPower();
				break;
			case R.id.bt_getregion:
				GetRegion();
				break;
			case R.id.bt_gettemperature:
				GetTemperature();
				break;
			default:
				break;
			}
		}
	}

	//  Get the firmware version number
	public void GetFirmwareVersion() {
		if(!mDevice.isopen()){
			mDevice.open();
		}
		String version = mDevice.getFirmwareVersion();// .trim();
		if (version == null || version == "") {
			return;
		}
		tv_version.setText(version.trim());
		System.out.println(version);
	}

	// Get temperature
	public void GetTemperature() {
		if(!mDevice.isopen()){
			mDevice.open();
		}
		String temperature = mDevice.getTemperature();
		if (temperature == null || temperature == "") {
			System.out.println(getString(R.string.RfidGetTemperature_faild));
			return;
		}
		et_temp.setText(temperature);
		System.out.println(getString(R.string.temperature) + temperature);
	}

	// Get power
	public void GetPower() {
		if(!mDevice.isopen()){
			mDevice.open();
		}
		int power = mDevice.getPower();
		if (power == 0) {
			System.out.println(getString(R.string.RfidGetPower_faild));
		}
		et_power.setText(power + "");
		System.out.println(getString(R.string.power) + power);
	}

	// Set power
	public void SetPower() {
		if(!mDevice.isopen()){
			mDevice.open();
		}
		String value = et_power.getText().toString().trim();
		if (!value.isEmpty()) {
			int power = Integer.valueOf(value);
			System.out.println(getString(R.string.RfidSetPower) + power);
			boolean ret = mDevice.setPower(power);
			Log.i("zy","SetPower="+ret);
			if (!ret) {
				System.out.println(getString(R.string.RfidGetPower_faild));
			}
		} else {
			Toast.makeText(getActivity(), R.string.please_input_power, Toast.LENGTH_SHORT).show();
		}

	}

	// Get region
	public void GetRegion() {
		if(!mDevice.isopen()){
			mDevice.open();
		}
		String region = mDevice.getRegion();
		if (region == null) {
			System.out.println(getString(R.string.RfidGetRegion_faild));
			return;
		}

		et_region.setText(region);
		System.out.println(getString(R.string.region) + region);
	}

	// Set region
	public void SetRegion() {
		if(!mDevice.isopen()){
			mDevice.open();
		}
		String region = getString(R.string.fcc);
		boolean ret = mDevice.setRegion(region);
		if (!ret) {
			System.out.println(getString(R.string.RfidGetRegion_faild));
		}
	}

}
