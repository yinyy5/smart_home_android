package smartcar.net.dyxy.yinyy.adktest.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by yinyy on 2016/10/20.
 */

public class DeviceDao {
    private DatabaseHelper helper;
    private SQLiteDatabase db;

    public DeviceDao(Context context) {
        helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public boolean update(String address, String title) {
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put("address", address);
            values.put("title", title);

            //查询是否存在
            Cursor cursor = db.query("devices", new String[]{"_id", "address", "title"}, "address = ?", new String[]{address}, null, null, null);
            if (cursor.getCount() == 0) {
                db.insert("devices", null, values);
            } else {
                db.update("devices", values, "address = ?", new String[]{address});
            }
            db.setTransactionSuccessful();

            return true;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        } finally {
            db.endTransaction();
        }
    }

    public String getTitle(String address){
        try {
            //查询是否存在
            Cursor cursor = db.query("devices", new String[]{"_id", "address", "title"}, "address = ?", new String[]{address}, null, null, null);
            if (cursor.getCount() == 0) {
                return null;
            } else {
                cursor.moveToNext();
                String title = cursor.getString(cursor.getColumnIndex("title"));

                return title;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
