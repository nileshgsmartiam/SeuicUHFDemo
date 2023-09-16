package com.seuic.uhfdemo;

import java.util.ArrayList;
import java.util.List;

import com.seuic.scankey.IKeyEventCallback;
import com.seuic.scankey.ScanKeyService;
import com.seuic.uhf.EPC;
import com.seuic.uhf.UHFService;

import android.app.Fragment;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class InventoryFragement extends Fragment {
    public static final int MAX_LEN = 128;

    public static final int ItemSelectColor = 0x44000000;

    private UHFService mDevice;
    private InventoryRunable mInventoryRunable;
    public boolean mInventoryStart = false;
    private Thread mInventoryThread;

    private int mSelectedIndex = -1;

    private Button btn_once;
    private Button btn_continue;
    private Button btn_stop;

    private TextView tv_total;
    private ListView lv_id;

    private EditText et_bank;
    private EditText et_address;
    private EditText et_lenth;
    private EditText et_password;

    private Button btn_read;
    private Button btn_write;
    private Button btn_clear;

    private EditText et_data;

    private List<EPC> mEPCList;
    private InventoryAdapter mAdapter;

    View currentView;

    static int m_count = 0;

    private static InventoryFragement inventoryfragement;

    public static InventoryFragement getInstance() {
        if (inventoryfragement == null) inventoryfragement = new InventoryFragement();
        return inventoryfragement;
    }

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            // Refresh listview
            switch (msg.what) {
                case 1:
                    synchronized (currentView.getContext()) {
                        synchronized (currentView.getContext()) {
                            int count=mEPCList.size();
                            mEPCList = mDevice.getTagIDs();
                            if (count<mEPCList.size())
                                playSound();
                        }
                        refreshData();
                    }
                    break;
                case 2:
                    BtnOnce();
                    //handler.sendEmptyMessageDelayed(2, 200);
                    break;
                case 3:
                    BtnContinue();
                    break;
                case 4:
                    BtnStop();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    // sound
    private static SoundPool mSoundPool;
    private static int soundID;
    /*
     * static { mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 20);
     * soundID = mSoundPool.load(getContext(),R.raw.scan, 1); }
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mDevice = UHFService.getInstance(getActivity().getApplication());

        View view = initUI(inflater);

        mEPCList = new ArrayList<EPC>();
        mAdapter = new InventoryAdapter();
        mInventoryRunable = new InventoryRunable();
        lv_id.setAdapter(mAdapter);

        lv_id.setOnItemClickListener(new MyItemClickListener());
        return view;
    }

    private ScanKeyService mScanKeyService = ScanKeyService.getInstance();
    private String TAG = "zy";
    private IKeyEventCallback mCallback = new IKeyEventCallback.Stub() {
        @Override
        public void onKeyDown(int keyCode) throws RemoteException {
            Log.d(TAG, "onKeyDown: keyCode=" + keyCode);
            /*单次寻卡+扫描*/
            Message message = Message.obtain();// Avoid repeated application of memory, reuse of information
            message.what = 2;
            handler.sendMessage(message);
            //mScanner.startScan();
        }

        @Override
        public void onKeyUp(int keyCode) throws RemoteException {
            Log.d(TAG, "onKeyUp: keyCode=" + keyCode);
            //mScanner.stopScan();
			/*Message message = Message.obtain();
			message.what = 4;
			handler.sendMessage(message);*/
        }
    };

    @Override
    public void onResume() {
        mDevice.open();
        Log.i("getFirmwareVersion",mDevice.getFirmwareVersion()) ;
        mScanKeyService.registerCallback(mCallback, "248,249,250");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mDevice.close();
        mScanKeyService.unregisterCallback(mCallback);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        mDevice.close();
        Log.i(TAG, "onPause");
        super.onPause();
    }

    // init UI
    private View initUI(LayoutInflater inflater) {
        currentView = inflater.inflate(R.layout.fragment_inventory, null);

        tv_total = (TextView) currentView.findViewById(R.id.tv_total);

        lv_id = (ListView) currentView.findViewById(R.id.lv_id);

        et_bank = (EditText) currentView.findViewById(R.id.et_bank);
        et_address = (EditText) currentView.findViewById(R.id.et_address);
        et_lenth = (EditText) currentView.findViewById(R.id.et_lenth);
        et_password = (EditText) currentView.findViewById(R.id.et_password);
        et_data = (EditText) currentView.findViewById(R.id.et_data);

        btn_once = (Button) currentView.findViewById(R.id.bt_once);
        btn_once.setOnClickListener(new MyClickListener());

        btn_continue = (Button) currentView.findViewById(R.id.bt_continue);
        btn_continue.setOnClickListener(new MyClickListener());

        btn_stop = (Button) currentView.findViewById(R.id.bt_stop);
        btn_stop.setOnClickListener(new MyClickListener());

        btn_read = (Button) currentView.findViewById(R.id.bt_read);
        btn_read.setOnClickListener(new MyClickListener());

        btn_write = (Button) currentView.findViewById(R.id.bt_write);
        btn_write.setOnClickListener(new MyClickListener());

        btn_clear = (Button) currentView.findViewById(R.id.bt_clear);
        btn_clear.setOnClickListener(new MyClickListener());

        // 初始化
        et_bank.setText("3");
        et_address.setText("0");
        et_lenth.setText("1");
        et_password.setText(R.string._00000000);

        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 20);
        soundID = mSoundPool.load(currentView.getContext(), R.raw.scan, 1);

        return currentView;
    }

    private void refreshData() {
        if (mEPCList != null) {
            int count = 0;
            for (EPC item : mEPCList) {
                count += item.count;
            }
			if (count > m_count) {
				//playSound();
			}
            mAdapter.notifyDataSetChanged();
            tv_total.setText(getString(R.string.id_pc_epc) + getString(R.string.total) + mEPCList.size());
            m_count = count;
        }
    }


    // EPC list item listener
    private class MyItemClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            mSelectedIndex = position;
            mAdapter.notifyDataSetInvalidated();
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
    private class MyClickListener implements android.view.View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_once:
                    mDevice.open();
                    BtnOnce();
                    break;
                case R.id.bt_continue:
                    BtnContinue();
                    break;
                case R.id.bt_stop:
                    BtnStop();
                    break;
                case R.id.bt_read:
                    BtnRead();
                    break;
                case R.id.bt_write:
                    BtnWrite();
                    break;
                case R.id.bt_clear:
                    BtnClear();
                    break;
                default:
                    break;
            }
        }
    }

    private void BtnContinue() {
        clearList();
        mSelectedIndex = -1;
        mAdapter.notifyDataSetChanged();
        if (mInventoryThread != null && mInventoryThread.isAlive()) {
            System.out.println("Thread not null");
            return;
        }
        if (mDevice.inventoryStart()) {
            System.out.println("RfidInventoryStart sucess.");
            mInventoryStart = true;
            mInventoryThread = new Thread(mInventoryRunable);
            mInventoryThread.start();
            btn_continue.setEnabled(false);
            btn_once.setEnabled(false);
            btn_stop.setEnabled(true);
        } else {
            System.out.println("RfidInventoryStart faild.");
        }
        return;
    }

    private void BtnOnce() {
        EPC epc = new EPC();
        try {
            if (mDevice.inventoryOnce(epc, 800)) {
                String id = epc.getId();
                System.out.println("" + id);
                if (id != null && !"".equals(id)) {
                    boolean exist = false;
                    for (EPC item : mEPCList) {
                        if (item.equals(epc)) {
                            item.count++;
                            //playSound();
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        mEPCList.add(epc);
                        playSound();
                    }
                    refreshData();
                }
                System.out.println("OK!!!");
            }
        } catch (Exception w) {

        }
    }

    private void BtnStop() {
        mInventoryStart = false;
        if (mInventoryThread != null) {
            mInventoryThread.interrupt();
            mInventoryThread = null;
        }
        System.out.println("begin stop!!");
        if (mDevice.inventoryStop()) {
            System.out.println("end stop!!");
            btn_once.setEnabled(true);
            btn_continue.setEnabled(true);
            btn_stop.setEnabled(false);

        } else {
            System.out.println("RfidInventoryStop faild.");
        }
        return;
    }

    private void clearList() {
        mSelectedIndex = -1;
        if (mEPCList != null) {
            mEPCList.clear();
            mAdapter.notifyDataSetChanged();
            m_count = 0;
        }
    }

    private void BtnRead() {
        if (mSelectedIndex >= 0) {
            if (et_bank.getText().toString().isEmpty() || et_address.getText().toString().isEmpty() || et_lenth.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), R.string.the_parameter_cannot_be_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            int bank = Integer.parseInt(et_bank.getText().toString());
            int address = Integer.parseInt(et_address.getText().toString());
            int length = Integer.parseInt(et_lenth.getText().toString());
            String str_password = et_password.getText().toString().trim();
            String Epc = mEPCList.get(mSelectedIndex).getId();
            byte[] btPassword = new byte[16];
            BaseUtil.getHexByteArray(str_password, btPassword, btPassword.length);
            byte[] buffer = new byte[MAX_LEN];
            if (length > MAX_LEN) {
                buffer = new byte[length];
            }
			/*读取数据用BaseUtil.getHexByteArray(epc.getId())来转换最好；
			新模块使用epc.id会导致读取失败*/
            byte[] epcID = BaseUtil.getHexByteArray(Epc);
            boolean sss = mDevice.readTagData(epcID, btPassword, bank, address, length, buffer);
            if (!sss) {
                et_data.setText("读取数据=" + sss);
            } else {
                String data = BaseUtil.getHexString(buffer, length, " ");
                et_data.setText(data);
            }
        } else {
            Toast.makeText(getActivity(), R.string.please_select_a_tag, Toast.LENGTH_SHORT).show();
        }
    }


    public boolean readTagOnce(String epcID, String psw, String bank, String offset, String len, byte[] data) {
        Log.d("UHFLogic", "readTagData times:111");
        boolean result = false;
        try {
            long st = System.currentTimeMillis();
            if (mDevice.readTagData(BaseUtil.getHexByteArray(epcID), BaseUtil.getHexByteArray(psw), Integer.parseInt(bank), Integer.parseInt(offset), Integer.parseInt(len), data)) {
                result = true;
            }
            Log.d("UHFLogic", "readTagData times:" + (System.currentTimeMillis() - st));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }

    }


    private void BtnWrite() {
        if (mSelectedIndex >= 0) {

            if (et_bank.getText().toString().isEmpty() || et_address.getText().toString().isEmpty() || et_lenth.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), R.string.the_parameter_cannot_be_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            int bank = Integer.parseInt(et_bank.getText().toString());
            int address = Integer.parseInt(et_address.getText().toString());
            int length = Integer.parseInt(et_lenth.getText().toString());
            String str_password = et_password.getText().toString().trim();

            byte[] Epc = mEPCList.get(mSelectedIndex).id;

            byte[] btPassword = new byte[16];
            BaseUtil.getHexByteArray(str_password, btPassword, btPassword.length);

            String str_data = et_data.getText().toString().replace(" ", "");
            if (str_data.isEmpty()) {
                Toast.makeText(getActivity(), R.string.writeData_cannot_be_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] buffer = new byte[MAX_LEN];
            if (length > MAX_LEN) {
                buffer = new byte[length];
            }
            BaseUtil.getHexByteArray(str_data, buffer, length);

            if (!mDevice.writeTagData(Epc, btPassword, bank, address, length, buffer)) {

                Toast.makeText(getActivity(), R.string.writeTagData_faild, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.writeTagData_sucess, Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(getActivity(), R.string.please_select_a_tag, Toast.LENGTH_SHORT).show();
        }
    }

    private void BtnClear() {
        et_data.setText(null);
    }

    private void playSound() {
        if (mSoundPool == null) {
            mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 20);
            soundID = mSoundPool.load(currentView.getContext(), R.raw.scan, 1);// "/system/media/audio/notifications/Antimony.ogg"
        }
        mSoundPool.play(soundID, 1, 1, 0, 0, 1);
    }

    private class InventoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            // return 0;
            if (mEPCList != null) {
                return mEPCList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {

            // return null;
            return mEPCList.get(position);
        }

        @Override
        public long getItemId(int position) {

            // return 0;
            return position;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            mSelectedIndex = -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View item_view = View.inflate(currentView.getContext(), R.layout.item_epc, null);
            EPC epc = mEPCList.get(position);

            TextView tv_id = (TextView) item_view.findViewById(R.id.tv_epc);
            TextView tv_nums = (TextView) item_view.findViewById(R.id.tv_nums);
            TextView tv_rssi = (TextView) item_view.findViewById(R.id.tv_rssi);

            tv_id.setText(epc.getId());
            tv_nums.setText(epc.count + "");
            tv_rssi.setText(epc.rssi + "");

            if (position == mSelectedIndex) {
                item_view.setBackgroundColor(ItemSelectColor);
            }
            return item_view;
        }

    }

    private class InventoryRunable implements Runnable {
        @Override
        public void run() {
            while (mInventoryStart) {
                Message message = Message.obtain();// Avoid repeated application of memory, reuse of information
                message.what = 1;
                handler.sendMessage(message);
                Log.d("mInventoryMsg", "--------------");
                List<EPC> x = mDevice.getTagIDs();
                Log.d("mInventoryMsg", "size: " + x.size());
                for (int i = 0 ; i < x.size(); i++){
                    Log.d("mInventoryMsg", x.get(i).getId());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
