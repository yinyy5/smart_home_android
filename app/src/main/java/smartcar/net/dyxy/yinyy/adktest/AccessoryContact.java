package smartcar.net.dyxy.yinyy.adktest;

import android.content.Context;

/**
 * Created by yinyy on 2016/9/9.
 */
public class AccessoryContact {
    public static byte CalcFCS(byte[] buffer, int start, int len) {
        byte xorResult = 0;
        for (int x = 0; x < len; x++) {
            xorResult = (byte) (xorResult ^ buffer[start++]);
        }

        return (xorResult);
    }

    public static class Command {
        public static final int Update = 0x0002;
        public static final int Search = 0x0001;
    }

    public static class CommandText {
        public static final String Find = "smartcar.net.dyxy.yinyy.adktest.find";
        public static final String Update = "smartcar.net.dyxy.yinyy.adktest.update";
        public static final String Search = "smartcar.net.dyxy.yinyy.adktest.search";
    }
}
