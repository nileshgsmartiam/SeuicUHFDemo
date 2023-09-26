package com.seuic.uhfdemo

import android.app.Fragment
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.seuic.scankey.IKeyEventCallback
import com.seuic.scankey.ScanKeyService
import com.seuic.uhf.EPC
import com.seuic.uhf.UHFService
import com.seuic.uhfdemo.BaseUtil.getHexByteArray
import com.seuic.uhfdemo.BaseUtil.getHexString

class InventoryFragement : Fragment() {
    var mInventoryStart = false
    lateinit var currentView: View
    private lateinit var mDevice: UHFService
    private lateinit var mInventoryRunable: InventoryRunable
    private var mInventoryThread: Thread? = null
    private var mSelectedIndex = -1
    private lateinit var btn_once: Button
    private lateinit var btn_continue: Button
    private lateinit var btn_stop: Button
    private lateinit var tv_total: TextView
    private lateinit var lv_id: ListView
    private lateinit var et_bank: EditText
    private lateinit var et_address: EditText
    private lateinit var et_lenth: EditText
    private lateinit var et_password: EditText
    private lateinit var btn_read: Button
    private lateinit var btn_write: Button
    private lateinit var btn_clear: Button
    private lateinit var et_data: EditText
    private var mEPCList: MutableList<EPC> = emptyList<EPC>().toMutableList()
    private lateinit var mAdapter: InventoryAdapter
    private lateinit var instance: InventoryFragement

    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            // Refresh listview
            when (msg.what) {
                1 -> synchronized(currentView!!.context) {
                    synchronized(currentView!!.context) {
                        val count = mEPCList!!.size
                        mEPCList = mDevice!!.tagIDs
                        if (count < mEPCList!!.size) playSound()
                    }
                    refreshData()
                }

                2 -> BtnOnce()
                3 -> BtnContinue()
                4 -> BtnStop()
                else -> {}
            }
        }
    }
    private val mCallback: IKeyEventCallback = object : IKeyEventCallback.Stub() {
        @Throws(RemoteException::class)
        override fun onKeyDown(keyCode: Int) {
            Log.d(TAG, "onKeyDown: keyCode=$keyCode")
            /*单次寻卡+扫描*/
            val message =
                Message.obtain() // Avoid repeated application of memory, reuse of information
            message.what = 2
            handler.sendMessage(message)
            //mScanner.startScan();
        }

        @Throws(RemoteException::class)
        override fun onKeyUp(keyCode: Int) {
            Log.d(TAG, "onKeyUp: keyCode=$keyCode")
            //mScanner.stopScan();
            /*Message message = Message.obtain();
			message.what = 4;
			handler.sendMessage(message);*/
        }
    }

    /*
     * static { mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 20);
     * soundID = mSoundPool.load(getContext(),R.raw.scan, 1); }
     */
    private val mScanKeyService = ScanKeyService.getInstance()
    private val TAG = "zy"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle
    ): View? {
        mDevice = UHFService.getInstance(activity.application)
        val view = initUI(inflater)
        mEPCList = ArrayList()
        mAdapter = InventoryAdapter()
        mInventoryRunable = InventoryRunable()
        lv_id!!.adapter = mAdapter
        lv_id!!.onItemClickListener = MyItemClickListener()
        return view
    }

    override fun onResume() {
        mDevice!!.open()
        Log.i("getFirmwareVersion", mDevice!!.firmwareVersion)
        mScanKeyService.registerCallback(mCallback, "248,249,250")
        super.onResume()
    }

    override fun onDestroy() {
        mDevice!!.close()
        mScanKeyService.unregisterCallback(mCallback)
        super.onDestroy()
    }

    override fun onPause() {
        mDevice!!.close()
        Log.i(TAG, "onPause")
        super.onPause()
    }

    // init UI
    private fun initUI(inflater: LayoutInflater): View? {
        var currentView = inflater.inflate(R.layout.fragment_inventory, null)
        tv_total = currentView.findViewById<View>(R.id.tv_total) as TextView
        lv_id = currentView.findViewById<View>(R.id.lv_id) as ListView
        et_bank = currentView.findViewById<View>(R.id.et_bank) as EditText
        et_address = currentView.findViewById<View>(R.id.et_address) as EditText
        et_lenth = currentView.findViewById<View>(R.id.et_lenth) as EditText
        et_password = currentView.findViewById<View>(R.id.et_password) as EditText
        et_data = currentView.findViewById<View>(R.id.et_data) as EditText
        btn_once = currentView.findViewById<View>(R.id.bt_once) as Button
        btn_once!!.setOnClickListener(MyClickListener())
        btn_continue = currentView.findViewById<View>(R.id.bt_continue) as Button
        btn_continue!!.setOnClickListener(MyClickListener())
        btn_stop = currentView.findViewById<View>(R.id.bt_stop) as Button
        btn_stop!!.setOnClickListener(MyClickListener())
        btn_read = currentView.findViewById<View>(R.id.bt_read) as Button
        btn_read!!.setOnClickListener(MyClickListener())
        btn_write = currentView.findViewById<View>(R.id.bt_write) as Button
        btn_write!!.setOnClickListener(MyClickListener())
        btn_clear = currentView.findViewById<View>(R.id.bt_clear) as Button
        btn_clear!!.setOnClickListener(MyClickListener())

        // 初始化
        et_bank!!.setText("3")
        et_address!!.setText("0")
        et_lenth!!.setText("1")
        et_password!!.setText(R.string._00000000)
        mSoundPool = SoundPool(3, AudioManager.STREAM_MUSIC, 20)
        soundID = mSoundPool!!.load(currentView.getContext(), R.raw.scan, 1)
        return currentView
    }

    private fun refreshData() {
        if (mEPCList != null) {
            var count = 0
            for (item in mEPCList!!) {
                count += item.count
            }
            if (count > m_count) {
                //playSound();
            }
            mAdapter!!.notifyDataSetChanged()
            tv_total!!.text =
                getString(R.string.id_pc_epc) + getString(R.string.total) + mEPCList!!.size
            m_count = count
        }
    }

    private fun BtnContinue() {
        clearList()
        mSelectedIndex = -1
        mAdapter!!.notifyDataSetChanged()
        if (mInventoryThread != null && mInventoryThread!!.isAlive) {
            println("Thread not null")
            return
        }
        if (mDevice!!.inventoryStart()) {
            println("RfidInventoryStart sucess.")
            mInventoryStart = true
            mInventoryThread = Thread(mInventoryRunable)
            mInventoryThread!!.start()
            btn_continue!!.isEnabled = false
            btn_once!!.isEnabled = false
            btn_stop!!.isEnabled = true
        } else {
            println("RfidInventoryStart faild.")
        }
        return
    }

    private fun BtnOnce() {
        val epc = EPC()
        try {
            if (mDevice!!.inventoryOnce(epc, 800)) {
                val id = epc.getId()
                println("" + id)
                if (id != null && "" != id) {
                    var exist = false
                    for (item in mEPCList!!) {
                        if (item == epc) {
                            item.count++
                            //playSound();
                            exist = true
                            break
                        }
                    }
                    if (!exist) {
                        mEPCList!!.add(epc)
                        playSound()
                    }
                    refreshData()
                }
                println("OK!!!")
            }
        } catch (w: Exception) {
        }
    }

    private fun BtnStop() {
        mInventoryStart = false
        if (mInventoryThread != null) {
            mInventoryThread!!.interrupt()
            mInventoryThread = null
        }
        println("begin stop!!")
        if (mDevice!!.inventoryStop()) {
            println("end stop!!")
            btn_once!!.isEnabled = true
            btn_continue!!.isEnabled = true
            btn_stop!!.isEnabled = false
        } else {
            println("RfidInventoryStop faild.")
        }
        return
    }

    private fun clearList() {
        mSelectedIndex = -1
        if (mEPCList != null) {
            with(mEPCList) {
                clearList()
            }
            mAdapter!!.notifyDataSetChanged()
            m_count = 0
        }
    }

    private fun BtnRead() {
        if (mSelectedIndex >= 0) {
            if (et_bank!!.text.toString().isEmpty() || et_address!!.text.toString()
                    .isEmpty() || et_lenth!!.text.toString().isEmpty()
            ) {
                Toast.makeText(activity, R.string.the_parameter_cannot_be_empty, Toast.LENGTH_SHORT)
                    .show()
                return
            }
            val bank = et_bank!!.text.toString().toInt()
            val address = et_address!!.text.toString().toInt()
            val length = et_lenth!!.text.toString().toInt()
            val str_password = et_password!!.text.toString().trim { it <= ' ' }
            val Epc = mEPCList!![mSelectedIndex].getId()
            val btPassword = ByteArray(16)
            getHexByteArray(str_password, btPassword, btPassword.size)
            var buffer = ByteArray(MAX_LEN)
            if (length > MAX_LEN) {
                buffer = ByteArray(length)
            }
            /*读取数据用BaseUtil.getHexByteArray(epc.getId())来转换最好；
			新模块使用epc.id会导致读取失败*/
            val epcID = getHexByteArray(Epc)
            val sss = mDevice!!.readTagData(epcID, btPassword, bank, address, length, buffer)
            if (!sss) {
                et_data!!.setText("读取数据=$sss")
            } else {
                val data = getHexString(buffer, length, " ")
                et_data!!.setText(data)
            }
        } else {
            Toast.makeText(activity, R.string.please_select_a_tag, Toast.LENGTH_SHORT).show()
        }
    }

    fun readTagOnce(
        epcID: String?,
        psw: String?,
        bank: String,
        offset: String,
        len: String,
        data: ByteArray?
    ): Boolean {
        Log.d("UHFLogic", "readTagData times:111")
        var result = false
        try {
            val st = System.currentTimeMillis()
            if (mDevice!!.readTagData(
                    getHexByteArray(epcID),
                    getHexByteArray(psw),
                    bank.toInt(),
                    offset.toInt(),
                    len.toInt(),
                    data
                )
            ) {
                result = true
            }
            Log.d("UHFLogic", "readTagData times:" + (System.currentTimeMillis() - st))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            return result
        }
    }

    private fun BtnWrite() {
        if (mSelectedIndex >= 0) {
            if (et_bank!!.text.toString().isEmpty() || et_address!!.text.toString()
                    .isEmpty() || et_lenth!!.text.toString().isEmpty()
            ) {
                Toast.makeText(activity, R.string.the_parameter_cannot_be_empty, Toast.LENGTH_SHORT)
                    .show()
                return
            }
            val bank = et_bank!!.text.toString().toInt()
            val address = et_address!!.text.toString().toInt()
            val length = et_lenth!!.text.toString().toInt()
            val str_password = et_password!!.text.toString().trim { it <= ' ' }
            val Epc = mEPCList!![mSelectedIndex].id
            val btPassword = ByteArray(16)
            getHexByteArray(str_password, btPassword, btPassword.size)
            val str_data = et_data!!.text.toString().replace(" ", "")
            if (str_data.isEmpty()) {
                Toast.makeText(activity, R.string.writeData_cannot_be_empty, Toast.LENGTH_SHORT)
                    .show()
                return
            }
            var buffer = ByteArray(MAX_LEN)
            if (length > MAX_LEN) {
                buffer = ByteArray(length)
            }
            getHexByteArray(str_data, buffer, length)
            if (!mDevice!!.writeTagData(Epc, btPassword, bank, address, length, buffer)) {
                Toast.makeText(activity, R.string.writeTagData_faild, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, R.string.writeTagData_sucess, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, R.string.please_select_a_tag, Toast.LENGTH_SHORT).show()
        }
    }

    private fun BtnClear() {
        et_data!!.setText("")
    }

    private fun playSound() {
        if (mSoundPool == null) {
            mSoundPool = SoundPool(3, AudioManager.STREAM_MUSIC, 20)
            soundID = mSoundPool!!.load(
                currentView!!.context, R.raw.scan, 1
            ) // "/system/media/audio/notifications/Antimony.ogg"
        }
        mSoundPool!!.play(soundID, 1f, 1f, 0, 0, 1f)
    }

    // EPC list item listener
    private inner class MyItemClickListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            mSelectedIndex = position
            mAdapter!!.notifyDataSetInvalidated()
            /*
             * ListView listview = (ListView) parent; HashMap<String, Object>
             * data = (HashMap<String, Object>)
             * listview.getItemAtPosition(position); String epc =
             * data.get("epc").toString(); Toast.makeText(getActivity(), epc,
             * 0).show();
             */
        }
    }

    // Button click event
    private inner class MyClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.bt_once -> {
                    mDevice!!.open()
                    BtnOnce()
                }

                R.id.bt_continue -> BtnContinue()
                R.id.bt_stop -> BtnStop()
                R.id.bt_read -> BtnRead()
                R.id.bt_write -> BtnWrite()
                R.id.bt_clear -> BtnClear()
                else -> {}
            }
        }
    }

    private inner class InventoryAdapter : BaseAdapter() {
        override fun getCount(): Int {

            // return 0;
            return if (mEPCList != null) {
                mEPCList!!.size
            } else 0
        }

        override fun getItem(position: Int): Any {

            // return null;
            return mEPCList!![position]
        }

        override fun getItemId(position: Int): Long {

            // return 0;
            return position.toLong()
        }

        override fun notifyDataSetChanged() {
            super.notifyDataSetChanged()
            mSelectedIndex = -1
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val item_view = View.inflate(currentView!!.context, R.layout.item_epc, null)
            val epc = mEPCList!![position]
            val tv_id = item_view.findViewById<View>(R.id.tv_epc) as TextView
            val tv_nums = item_view.findViewById<View>(R.id.tv_nums) as TextView
            val tv_rssi = item_view.findViewById<View>(R.id.tv_rssi) as TextView
            tv_id.text = epc.getId()
            tv_nums.text = epc.count.toString() + ""
            tv_rssi.text = epc.rssi.toString() + ""
            if (position == mSelectedIndex) {
                item_view.setBackgroundColor(ItemSelectColor)
            }
            return item_view
        }
    }

    private inner class InventoryRunable : Runnable {
        override fun run() {
            while (mInventoryStart) {
                val message =
                    Message.obtain() // Avoid repeated application of memory, reuse of information
                message.what = 1
                handler.sendMessage(message)
                Log.d("mInventoryMsg", "--------------")
                val x = mDevice!!.tagIDs
                Log.d("mInventoryMsg", "size: " + x.size)
                for (i in x.indices) {
                    Log.d("mInventoryMsg", x[i].getId())
                }
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val MAX_LEN = 128
        const val ItemSelectColor = 0x44000000
        var m_count = 0
        private lateinit var inventoryfragement: InventoryFragement

        // sound
        private lateinit var mSoundPool: SoundPool
        private var soundID = 0

//        @JvmStatic
//        val instance: InventoryFragement?
//            get() {
//                if (inventoryfragement == null) inventoryfragement = InventoryFragement()
//                return inventoryfragement
//            }
    }
}