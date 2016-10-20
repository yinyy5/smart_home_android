package smartcar.net.dyxy.yinyy.adktest.device;

/**
 * Created by yinyy on 2016/10/18.
 */

public class UnknownDevice extends Device {
    private byte[] data;

    public UnknownDevice(String address, byte[] data) {
        super(address);
        this.data = data;
    }

    @Override
    public void copy(Device device) {
        if (device instanceof UnknownDevice) {
            UnknownDevice unknownDevice = (UnknownDevice) device;
            data = unknownDevice.getData();
        }
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String getDescription() {
        String description = "[";
        String[] addresses = getAddress().split(":");

        for (String t : addresses) {
            description += "0x" + t.toUpperCase() + " ";
        }
        description = description.trim() + "]";

        for (byte b : data) {
            String t = Integer.toHexString(b);
            if (t.length() == 1) {
                t = "0" + t;
            }

            description += " 0x" + t.toUpperCase();
        }

        description = description.trim();

        return description;
    }
}
