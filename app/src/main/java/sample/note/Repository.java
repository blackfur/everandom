package sample.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Repository extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SQLiteDatabase.db";

    public static final String TABLE_NAME = "APP_NOTES";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_TIMESTAMP = "TIMESTAMP";
    public static final String COLUMN_TXT = "TXT";
    Context ctx;

    public Repository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TIMESTAMP + " VARCHAR, " + COLUMN_TXT + " VARCHAR);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public void insert(Map<String, String> row) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TIMESTAMP, row.get(COLUMN_TIMESTAMP));
        contentValues.put(COLUMN_TXT, row.get(COLUMN_TXT));
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public void insert(ContentValues contentValues) {
        SQLiteDatabase db = getReadableDatabase();
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }
    public void update(Map<String, String> row) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        String timestamp = row.get(COLUMN_TIMESTAMP);
        contentValues.put(COLUMN_TIMESTAMP, timestamp);
        contentValues.put(COLUMN_TXT, row.get(COLUMN_TXT));
        db.update(TABLE_NAME, contentValues, COLUMN_TIMESTAMP + " = ?", new String[]{timestamp});
        db.close();
    }

    public void update(ContentValues row) {
        SQLiteDatabase db = getReadableDatabase();
        String timestamp = row.getAsString("timestamp");
        db.update(TABLE_NAME, row, COLUMN_TIMESTAMP + " = ?", new String[]{timestamp});
        db.close();
    }
    public void restore(JsonArray array) {
        SQLiteDatabase db = getReadableDatabase();
        for(int i=0; i < array.size(); i++){
            JsonObject row = array.get(i).getAsJsonObject();

            String timestamp = row.get(COLUMN_TIMESTAMP).getAsString();

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_TIMESTAMP, timestamp);
            contentValues.put(COLUMN_TXT, row.get(COLUMN_TXT).getAsString());

            String sql = "SELECT 1 FROM "+TABLE_NAME+" where timestamp = '" + timestamp + "'";
            Log.i("repo.check", sql);
            // check exists
            Cursor cursor =db.rawQuery(sql, null);
            if(cursor == null || cursor.getCount() == 0){
                // insert
                db.insert(TABLE_NAME, null, contentValues);
                continue;
            }

            // update
            db.update(TABLE_NAME, contentValues, COLUMN_TIMESTAMP + " = ?", new String[]{timestamp});
        }
        db.close();
    }
    public void delete(Map<String, String> row) {
        SQLiteDatabase db = getReadableDatabase();
        String timestamp = row.get(COLUMN_TIMESTAMP);
        db.delete(TABLE_NAME, COLUMN_TIMESTAMP + " = ?", new String[]{timestamp});
        db.close();
    }


    public ArrayList<Map<String,String>> select(String limit, String offset) throws IOException {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM "+TABLE_NAME+" LIMIT " + limit + " OFFSET " + offset;
        Log.i("select.limit.offset", sql);
        Cursor cursor =db.rawQuery(sql,null);
        ArrayList<Map<String,String>> rows = fetch(cursor);
        db.close();
       return rows;
    }

    ArrayList<Map<String,String>> fetch(Cursor cursor){
        ArrayList<Map<String,String>> rows= new ArrayList<>();
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                rows.add(extract(cursor));
            }
        }
        cursor.close();
        return rows;
    }

    Map<String, String> extract(Cursor cursor){

        cursor.moveToNext();

        Map<String, String> row = new HashMap<>();
        String id = String.valueOf(cursor.getInt(0));
        String timestamp = cursor.getString(1);
        String txt = cursor.getString(2);
        Log.i("repo.extract", id + ", " + timestamp + ", " + txt);
        row.put(COLUMN_ID, id);
        row.put(COLUMN_TIMESTAMP, timestamp);
        row.put(COLUMN_TXT, txt);
        return row;
    }

    public ArrayList<Map<String,String>> select() throws IOException {
        SQLiteDatabase db = getReadableDatabase();
        //Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        String limit = Property.get("limit", ctx);
        Log.i("select.limit", limit);
        String sql = "SELECT * FROM "+TABLE_NAME+" ORDER BY RANDOM() LIMIT " + limit;
        Log.i("repo.select", sql);
        Cursor cursor =db.rawQuery(sql, null);
        ArrayList<Map<String,String>> rows = fetch(cursor);
        db.close();
        return rows;
    }

    public Map<String,String> select(String timestamp) throws Exception {
        SQLiteDatabase db = getReadableDatabase();
        //Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        Cursor cursor =db.rawQuery("SELECT * FROM "+TABLE_NAME+" where timestamp = ?",
                new String[]{timestamp});

        Map<String,String> row;
        if (cursor.getCount() == 0)
            throw new Exception("Not found by timestamp: " +timestamp);

        row = extract(cursor);

        cursor.close();
        db.close();

        return row;
    }
}
