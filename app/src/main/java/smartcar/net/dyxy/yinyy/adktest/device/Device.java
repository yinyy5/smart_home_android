package smartcar.net.dyxy.yinyy.adktest.device;

import java.io.Serializable;

import smartcar.net.dyxy.yinyy.adktest.device.switcher.SwitcherDevice;

/**
 * Created by yinyy on 2016/9/10.
 */
public abstract class Device implements Serializable {
    private String address;
    private String description;

    public Device(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public static Device parse(byte[] bs) {
        Device device = null;

        String address = String.format("%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x", bs[0], bs[1], bs[2], bs[3], bs[4], bs[5], bs[6], bs[7]);
        address = address.toUpperCase();

        if (bs[8] == Type.Switcher) {//判断设备类型
            device = new SwitcherDevice(address, bs[9], bs[10]);
        } else {//未知的设备
            byte[] data = new byte[bs.length-8];
            System.arraycopy(bs, 8, data, 0, data.length);
            device = new UnknownDevice(address, data);
        }

        return device;
    }

    public byte[] toByteArray() {
        String[] addresses = address.split(":");

        byte[] bs = new byte[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            bs[i] = (byte) Integer.parseInt(addresses[i], 16);
        }

        return bs;
    }

    public abstract void copy(Device device);

    public static class Type {
        public static final int Unknown = 0x01;
        public static final int Switcher = 0x02;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Device) {
            Device device = (Device) o;
            if (getAddress().equals(device.getAddress())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }
}
