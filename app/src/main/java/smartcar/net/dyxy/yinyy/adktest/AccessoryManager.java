package smartcar.net.dyxy.yinyy.adktest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import smartcar.net.dyxy.yinyy.adktest.device.Device;

/**
 * Created by yinyy on 2016/9/19.
 */
public class AccessoryManager {
    private static final String TAG = AccessoryManager.class.getSimpleName();

    private Context context;

    private UsbManager mUsbManager;
    private UsbAccessory mAccessory;
    //private FileInputStream mInputStream;
    //private FileOutputStream mOutputStream;
    private BufferedInputStream bufferedInputStream;
    private BufferedOutputStream bufferedOutputStream;
    private ParcelFileDescriptor mFileDescriptor;

    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessoey(accessory);
                    } else {
                        Log.d(TAG, "Deny USB Permission");
                    }

                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && accessory.equals(mAccessory)) {
                    closeAccessory();
                }
            } else if (AccessoryContact.CommandText.Update.equals(action)) {
                updateDevice((Device) intent.getSerializableExtra("device"));
            } else if (AccessoryContact.CommandText.Search.equals(action)) {
                searchDevice();
            } else {
                Log.d(TAG, "Other Message");
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public AccessoryManager(Context context) {
        this.context = context;
    }

    public void create() {
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);

        //注册接收广播的类型
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(AccessoryContact.CommandText.Update);
        filter.addAction(AccessoryContact.CommandText.Search);
        context.registerReceiver(broadcastReceiver, filter);
    }

    public void resume() {
        if (bufferedInputStream != null && bufferedOutputStream != null) {
            return;
        }

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = accessories == null ? null : accessories[0];
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessoey(accessory);
            } else {
                synchronized (broadcastReceiver) {
                    if (!mPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "Accessory is null");
        }
    }

    public void destroy() {
        context.unregisterReceiver(broadcastReceiver);
    }

    public void openAccessoey(UsbAccessory accessory) {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;

            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            bufferedInputStream = new BufferedInputStream(new FileInputStream(fd), 256);// new FileInputStream(fd);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fd));// mOutputStream = new FileOutputStream(fd);

            new Thread(null, new Runnable() {
                @Override
                public void run() {
                    int ret = 0;
                    int value;

                    try {
                        while ((value = bufferedInputStream.read()) != -1) {
                            if (value == 0xfe) {
                                int dataLen = bufferedInputStream.read();//长度
                                byte[] buffer = new byte[dataLen + 5];

                                buffer[0] = (byte) 0xfe;
                                buffer[1] = (byte) dataLen;

                                byte[] tmp = new byte[dataLen + 3];
                                bufferedInputStream.read(tmp, 0, tmp.length);//数据+FCS

                                System.arraycopy(tmp, 0, buffer, 2, tmp.length);

                                //验证FCS
                                if (buffer[buffer.length - 1] == AccessoryContact.CalcFCS(buffer, 1, dataLen + 3)) {
                                    int cmd = (buffer[2] << 8) + buffer[3];//命令

                                    tmp = new byte[dataLen];
                                    System.arraycopy(buffer, 4, tmp, 0, dataLen);

                                    //通过广播把当前得到的设备发出去
                                    Device device = Device.parse(tmp);
                                    Intent intent = new Intent(AccessoryContact.CommandText.Find);
                                    intent.putExtra("device", device);
                                    context.sendBroadcast(intent);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Message message = handler.obtainMessage(-1);
                        message.obj = e.getMessage();
                        handler.sendMessage(message);
                    } finally {
                        if (bufferedInputStream != null) {
                            try {
                                bufferedInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }, "AdkTestReadingThread").start();

            Log.d(TAG, "Accessory Opened");
        } else
        {
            Log.d(TAG, "Accessory Open Failed");
        }

    }

    public void closeAccessory() {
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    private void updateDevice(final Device device) {
        if (bufferedOutputStream != null) {
            try {
                byte[] deviceBuffer = device.toByteArray();
                byte[] buffer = new byte[deviceBuffer.length + 5];

                buffer[0] = (byte) 0xfe;
                buffer[1] = (byte) deviceBuffer.length;
                buffer[2] = (AccessoryContact.Command.Update & 0xff00) >> 8;
                buffer[3] = (AccessoryContact.Command.Update & 0xff);
                System.arraycopy(deviceBuffer, 0, buffer, 4, deviceBuffer.length);
                buffer[buffer.length - 1] = AccessoryContact.CalcFCS(buffer, 1, buffer[1] + 3);

                bufferedOutputStream.write(buffer);
                bufferedOutputStream.flush();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                Toast.makeText(context, sw.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void searchDevice() {
        if (bufferedOutputStream != null) {
            try {
                byte[] buffer = {(byte) 0xfe, 0x00, (AccessoryContact.Command.Search & 0xff00) >> 8, AccessoryContact.Command.Search & 0xff, 0x00};
                buffer[buffer.length - 1] = AccessoryContact.CalcFCS(buffer, 1, 3);

                bufferedOutputStream.write(buffer);
                bufferedOutputStream.flush();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                Toast.makeText(context, sw.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

//    private void settingAccessory(byte[] bs) {
//        if (mOutputStream != null) {
//            try {
//                byte[] buffer = new byte[bs.length + 1];
//                buffer[0] = AccessoryContact.Command.Setting;
//                System.arraycopy(bs, 0, buffer, 1, bs.length);
//
//                mOutputStream.write(buffer);
//                mOutputStream.flush();
//            } catch (Exception e) {
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw);
//                e.printStackTrace(pw);
//
//                Toast.makeText(context, sw.toString(), Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}
