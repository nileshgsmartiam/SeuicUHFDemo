package com.seuic.uhfdemo

import android.app.Activity
import android.app.ActivityManager
import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.seuic.uhf.UHFService


class MainActivity : Activity(), View.OnClickListener {
    private lateinit var rb_inventory: RadioButton
    private lateinit var rb_settings: RadioButton
    private lateinit var mDevice: UHFService
    private lateinit var fm: FragmentManager
    private lateinit var ft: FragmentTransaction
    private lateinit var m_inventory: InventoryFragement
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("zy", "起始时间：" + System.currentTimeMillis())
        mDevice = UHFService.getInstance(this@MainActivity)
        val ret = mDevice!!.open()
        mDevice!!.setPower(5)
        Log.i("zy", "结束时间：" + System.currentTimeMillis())
        if (!ret) {
            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show()
        }
        mDevice!!.setParameters(UHFService.PARAMETER_HIDE_PC, 1)
        rb_inventory = findViewById<View>(R.id.rb_inventory) as RadioButton
        rb_settings = findViewById<View>(R.id.rb_settings) as RadioButton
        rb_inventory!!.setOnClickListener(this)
        rb_settings!!.setOnClickListener(this)
        fm = fragmentManager
        ft = fm!!.beginTransaction()
        m_inventory = InventoryFragement()
        ft!!.replace(R.id.frl_content, m_inventory)
        rb_inventory!!.isEnabled = false
        ft!!.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        // new object
        val ret = mDevice!!.open()
        if (!ret) {
            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        // close UHF
        mDevice!!.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_exit) {
            mDevice!!.close()
            // Toast.makeText(this, "exit", 0).show();
            System.exit(0)
            return true
        }
        if (id == R.id.action_hide) {
            // Toast.makeText(this, "hide", 0).show();
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        ft = fm!!.beginTransaction()
        when (v.id) {
            R.id.rb_inventory -> {
                ft!!.replace(R.id.frl_content, InventoryFragement())
                rb_inventory!!.isEnabled = false
                rb_settings!!.isEnabled = true
            }

            R.id.rb_settings -> {
                if (!m_inventory!!.mInventoryStart) {
                    ft!!.replace(R.id.frl_content, SettingsFragement!! as Fragment)
                    rb_settings!!.isEnabled = false
                }
                rb_inventory!!.isEnabled = true
            }

            else -> {}
        }
        ft!!.commit()
    }

    companion object {
        // private SettingsFragement m_setinventory;
        fun stopApps(context: Context, packageName: String) {
            try {
                val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                val forceStopPackage =
                    am.javaClass.getDeclaredMethod("forceStopPackage", String::class.java)
                forceStopPackage.isAccessible = true
                forceStopPackage.invoke(am, packageName)
                Log.i("zy", "TimerV force stop package $packageName successful")
                println("TimerV force stop package $packageName successful")
            } catch (ex: Exception) {
                ex.printStackTrace()
                System.err.println("TimerV force stop package $packageName error!")
                Log.i("zy", "TimerV force stop package $packageName error!")
            }
        }
    }
}