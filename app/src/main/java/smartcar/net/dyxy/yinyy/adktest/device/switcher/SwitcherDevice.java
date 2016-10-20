package smartcar.net.dyxy.yinyy.adktest.device.switcher;

import smartcar.net.dyxy.yinyy.adktest.device.Device;

/**
 * Created by yinyy on 2016/9/20.
 */
public class SwitcherDevice extends Device {
    private boolean[] status;

    public SwitcherDevice(String address, int channels, int values) {
        super(address);
        status = new boolean[channels];

        for (int i = 0; i < channels; i++) {
            status[i] = ((values >> i) & 0x01) == 0x01;
        }
    }

    public int getChannels() {
        return status.length;
    }

    public boolean getStatus(int channel) {
        return status[channel];
    }

    public void set(int channel) {
        if (channel < status.length) {
            status[channel] = true;
        }
    }

    public void reset(int channel) {
        if (channel < status.length) {
            status[channel] = false;
        }
    }

    @Override
    public String getDescription() {
        return getChannels() + "路开关模块（" + getAddress() + "）";
    }

    @Override
    public byte[] toByteArray() {
        byte[] address = super.toByteArray();
        byte[] buffer = new byte[address.length + 1];

        System.arraycopy(address, 0, buffer, 0, address.length);
//        buffer[address.length] = Type.Switcher;
//        buffer[address.length + 1] = (byte) getChannels();

        int value = 0;
        for (int i = 0; i < status.length; i++) {
            if (status[i] == true) {
                value |= (1 << i);
            }
        }

        buffer[address.length] = (byte) (value&0xff);

        return buffer;
    }

    @Override
    public void copy(Device device) {
        if (device instanceof SwitcherDevice) {
            SwitcherDevice sd = (SwitcherDevice) device;
            for (int i = 0; i < sd.getChannels(); i++) {
                if (sd.getStatus(i)) {
                    set(i);
                } else {
                    reset(i);
                }
            }
        }
    }
}
