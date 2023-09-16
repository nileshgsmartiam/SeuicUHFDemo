package com.seuic.uhfdemo;

import com.seuic.uhf.UHFService;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.seuic.uhf.UHFService.PARAMETER_HIDE_PC;

public class MainActivity extends Activity implements OnClickListener {
	private RadioButton rb_inventory;
	private RadioButton rb_settings;

	private UHFService mDevice;

	private FragmentManager fm;
	private FragmentTransaction ft;

	private InventoryFragement m_inventory;
	// private SettingsFragement m_setinventory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("zy","起始时间："+System.currentTimeMillis());
		mDevice = UHFService.getInstance(MainActivity.this);
		boolean ret = mDevice.open();
		mDevice.setPower(5);
		Log.i("zy","结束时间："+System.currentTimeMillis());
		if (!ret) {
			Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show();
		}
		mDevice.setParameters(PARAMETER_HIDE_PC,1);
		rb_inventory = (RadioButton) findViewById(R.id.rb_inventory);
		rb_settings = (RadioButton) findViewById(R.id.rb_settings);

		rb_inventory.setOnClickListener(this);
		rb_settings.setOnClickListener(this);

		fm = getFragmentManager();
		ft = fm.beginTransaction();
		m_inventory = InventoryFragement.getInstance();
		ft.replace(R.id.frl_content, m_inventory);
		rb_inventory.setEnabled(false);
		ft.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	public static void stopApps(Context context,String packageName) {
		try {
			ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
			Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
			forceStopPackage.setAccessible(true);
			forceStopPackage.invoke(am, packageName);
			Log.i("zy","TimerV force stop package "+packageName+" successful");
			System.out.println("TimerV force stop package "+packageName+" successful");
		}catch(Exception ex) {
			ex.printStackTrace();
			System.err.println("TimerV force stop package "+packageName+" error!");
			Log.i("zy","TimerV force stop package "+packageName+" error!");
		}
	}


	@Override
	protected void onResume() {
		super.onResume();
		// new object
		boolean ret = mDevice.open();
		if (!ret) {
			Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		// close UHF
		mDevice.close();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_exit) {
			mDevice.close();
			// Toast.makeText(this, "exit", 0).show();
			System.exit(0);
			return true;
		}
		if (id == R.id.action_hide) {
			// Toast.makeText(this, "hide", 0).show();
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		ft = fm.beginTransaction();
		switch (v.getId()) {
		case R.id.rb_inventory:
			ft.replace(R.id.frl_content, InventoryFragement.getInstance());
			rb_inventory.setEnabled(false);
			rb_settings.setEnabled(true);
			break;

		case R.id.rb_settings:
			if (!m_inventory.mInventoryStart) {
				ft.replace(R.id.frl_content, SettingsFragement.getInstance());
				rb_settings.setEnabled(false);
			}
			rb_inventory.setEnabled(true);

			break;

		default:
			break;
		}
		ft.commit();
	}

}
