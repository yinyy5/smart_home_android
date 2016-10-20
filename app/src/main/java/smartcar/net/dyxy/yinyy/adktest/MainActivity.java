package smartcar.net.dyxy.yinyy.adktest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import smartcar.net.dyxy.yinyy.adktest.device.Device;

public class MainActivity extends AppCompatActivity {
    private AccessoryManager accessoryManager;

    private static final String TAG = MainActivity.class.getSimpleName();

    RecyclerView devicesView;
    private DeviceListAdapter deviceListAdapter;
    private List<Device> devicesData;

    private static final int FINISH_APP = 0x01;
    private static final int REFRESH_DEVICE = 0x02;

    private boolean isBackKeyPressed = false;
    private boolean shouldRefreshDeviceList = false;

    private Handler handler = new Handler() {
        private int value = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FINISH_APP:
                    isBackKeyPressed = false;
                    break;
                case REFRESH_DEVICE:
                    deviceListAdapter.notifyDataSetChanged();
                    shouldRefreshDeviceList = false;
                    break;

                default:
                    createDevice();

                    if (isRunning) {
                        handler.sendEmptyMessageDelayed(100, 5000);
                    }
                    break;
            }
        }

        private void createDevice() {
            Device d;
            byte[] bs = null;

            if (value == 0) {
                bs = new byte[]{0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x04, 0x0f};
            } else if (value == 1) {
                bs = new byte[]{0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x03};
            } else if (value == 2) {
                bs = new byte[]{0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x03, 0x02, 0x03, 0x04, 0x03};
            }
            d = Device.parse(bs);

            Intent intent = new Intent(AccessoryContact.CommandText.Find);
            intent.putExtra("device", d);
            sendBroadcast(intent);

            value++;

            value %= 3;
        }
    };

    private boolean isRunning = false;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AccessoryContact.CommandText.Find.equals(action)) {
                addDevice((Device) intent.getSerializableExtra("device"));
            } else {
                Log.d("TEST", action);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicesView = (RecyclerView) findViewById(R.id.devices_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        devicesView.setLayoutManager(layoutManager);
        devicesView.setHasFixedSize(true);

        devicesData = new LinkedList<>();
        deviceListAdapter = new DeviceListAdapter(this, devicesData);
        devicesView.setAdapter(deviceListAdapter);

        accessoryManager = new AccessoryManager(this);
        accessoryManager.create();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //注册广播接收器的时候需要指定接收什么类型的广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AccessoryContact.CommandText.Find);
        registerReceiver(broadcastReceiver, intentFilter);

        accessoryManager.resume();

        isRunning = true;
        handler.sendEmptyMessage(100);
    }

    @Override
    protected void onPause() {
        //accessoryManager.closeAccessory();

        //isRunning = false;

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        accessoryManager.closeAccessory();
        accessoryManager.destroy();

        unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.setting_item) {
            //TODO:扩展ADK的功能，可以通过平板直接操作ADK上连接的设备
        } else if (item.getItemId() == R.id.setting_search) {
            devicesData.clear();

            Intent intent = new Intent(AccessoryContact.CommandText.Search);
            sendBroadcast(intent);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (isBackKeyPressed) {
            finish();
        } else {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_LONG).show();
            isBackKeyPressed = true;
            handler.sendEmptyMessageDelayed(FINISH_APP, 1000);
        }
    }

    private void addDevice(Device device) {
        //检查待添加的设备是否已经存在于适配器中
        int index = devicesData.indexOf(device);
        if (index == -1) {
            devicesData.add(device);
        } else {
            devicesData.get(index).copy(device);
        }

        //避免每次添加设备都进行刷新
        if (!shouldRefreshDeviceList) {
            shouldRefreshDeviceList = true;
            handler.sendEmptyMessageDelayed(REFRESH_DEVICE, 1000);
        }
    }
}