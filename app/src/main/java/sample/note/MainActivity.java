package sample.note;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import okhttp3.*;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static sample.note.Global.prop;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private SwipeRefreshLayout layout;
    private ListView lv;
    private Repository repo;
    private NotesAdapter adapter;
    // select item position
    private int position = -1;

    @lombok.SneakyThrows
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //host = Property.get("host", this);
        //limit = Property.get("limit", this);

        // repository
        repo = new Repository(this);

        //String TAG = Property.get("tag", getApplicationContext());

        setContentView(R.layout.activity_main);
        // storing string resources into Array
        //String[] adobe_products = getResources().getStringArray(R.array.adobe_products);

        // fetch data
        ArrayList<Map<String, String>> list = repo.select();

        // Binding resources Array to ListAdapter
        lv = findViewById(R.id.listView);
        adapter = new NotesAdapter(list, this);
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener(this);
        lv.setOnItemClickListener(this);

        // context menu
        registerForContextMenu(lv);

        // pull to refresh
        layout = findViewById(R.id.pullToRefresh);
        layout.setOnRefreshListener(this);

        //
        findViewById(R.id.help).setOnClickListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
        menu.setHeaderTitle("Please Select:");
    }

    @SneakyThrows
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int selected = item.getItemId();

        if (selected == R.id.view) {
            if (position < 0) {
                Toast.makeText(this, "Please select an item.", Toast.LENGTH_LONG).show();
                return true;
            }
            Intent insert = new Intent(this, UpdateActivity.class);
            Map<String, String> data = adapter.getItem(position);
            assert data != null;
            String timestamp = data.get(Repository.COLUMN_TIMESTAMP);
            insert.putExtra("timestamp", timestamp);
            startActivity(insert);
            return true;
        }

        if (selected == R.id.qrcode) {
            Intent activity= new Intent(this, QrCodeActivity.class);
            startActivity(activity);
            return true;
        }
        if (selected == R.id.chat) {
            Intent activity= new Intent(this, ChatRegisterActivity.class);
            startActivity(activity);
            return true;
        }
        if (selected == R.id.upt) {
            Intent activity= new Intent(this, VerUptActivity.class);
            startActivity(activity);
            return true;
        }
        if (selected == R.id.insert) {
            Intent insert = new Intent(this, InsertActivity.class);
            startActivity(insert);
            return true;
        }
        if (selected == R.id.opt) {
            Intent it = new Intent(this, PropertyActivity.class);
            startActivity(it);
            return true;
        }

        if (selected == R.id.del) {
            if (position < 0) {
                Toast.makeText(this, "Please select an item.", Toast.LENGTH_LONG).show();
                return true;
            }
            Map<String, String> data = adapter.getItem(position);
            assert data != null;
            data.put(Repository.COLUMN_STATUS, "-1");
            //repo.delete(data);
            repo.update(data);

            ArrayList<Map<String, String>> list = repo.select();
            adapter.notifyChanged(list);

            Toast.makeText(this, "deleted!", Toast.LENGTH_LONG).show();
            return true;
        }

        if (selected == R.id.synchronize) {
            SyncQuest syncQuest = new SyncQuest();

            String host = prop("prefix");
            Toast.makeText(this, host, Toast.LENGTH_LONG).show();
            String limit = prop("limit");
            syncQuest.execute(host, limit);
            layout.setRefreshing(true);

            Toast.makeText(this, "Synchronizing...", Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        closeContextMenu();
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        openContextMenu(parent);
        this.position = position;
    }

    @SneakyThrows
    @Override
    public void onRefresh() {
        ArrayList<Map<String, String>> list = repo.select();
        layout.setRefreshing(false);
        adapter.notifyChanged(list);
    }

    @SneakyThrows
    @Override
    public void onResume() {
        super.onResume();

        // put your code here...
        ArrayList<Map<String, String>> list = repo.select();
        adapter.notifyChanged(list);
    }

    @Override
    public void onClick(View v) {
        position = -1;
        openContextMenu(lv);
    }

    class SyncQuest extends AsyncTask {

        //OkHttpClient client = new OkHttpClient();
        OkHttpClient client = Global.okHttpClient();
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String limit, host;
        final Request.Builder builder = new Request.Builder();
        int step;
        final Gson gson = new GsonBuilder().create();

        SyncQuest() throws ClassNotFoundException, NoSuchFieldException, NoSuchAlgorithmException, IllegalAccessException, KeyManagementException {
        }

        @SneakyThrows
        @Override
        protected void onPostExecute(Object s) {
            super.onPostExecute(s);
            ArrayList<Map<String, String>> list = repo.select();
            adapter.notifyChanged(list);
            layout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), s.toString(), Toast.LENGTH_LONG).show();
        }

        void upload() throws Exception {

            String insertUrl = host + "insert.php";
            Log.i("TAG", "Insert URL: " + insertUrl);
            builder.url(insertUrl);

            // insert to Server
            int offset = 0;
            step = Integer.parseInt(limit);

            ArrayList<Map<String, String>> rows;
            rows = repo.select(limit, String.valueOf(offset));
            while (rows.size() > 0) {
                String feed = gson.toJson(rows);
                Log.i("UPLOAD", feed);
                RequestBody body = RequestBody.create(feed, JSON);
                Request request = new Request.Builder().url(insertUrl).post(body).build();
                Response response = client.newCall(request).execute();
                if (response.code() != 200) {
                    String error = response.body().string();
                    Log.i("sync.post", error);
                    throw new Exception("sync.post: " + error);
                }

                offset += step;
                rows = repo.select(limit, String.valueOf(offset));
            }
        }

        void restore() throws Exception {

            // restore from Server
            String selectUrl = host + "select.php";

            int offset = 0;
            int statusCode;

            JsonObject feed = new JsonObject();
            feed.addProperty("limit", limit);
            do {

                feed.addProperty("offset", offset);
                String tmp = feed.toString();
                Log.i("TAG", tmp);
                RequestBody body = RequestBody.create(tmp, JSON);
                Request request = new Request.Builder().url(selectUrl).post(body).build();
                Response response = client.newCall(request).execute();
                statusCode = response.code();
                if (statusCode == 204)
                    break;
                if (statusCode != 200) {
                    String error = response.body().string();
                    Log.i("sync.obtain", error);
                    throw new Exception("sync.obtain: " + error);
                }

                String content = (response.body()).string();
                Log.i("sync.restore", content);
                // store data
                JsonArray array = gson.fromJson(content, JsonArray.class);
                repo.restore(array);

                offset += step;
            } while (true);
        }

        @SneakyThrows
        @Override
        protected Object doInBackground(Object[] objects) {
            host = objects[0].toString();
            limit = objects[1].toString();

            try {
                upload();
                restore();
            } catch (Exception ex) {
                Log.e("Sync", ex.getMessage());
                return ex.getMessage();
            }
            repo.clean();

            return "Synchronized!";
        }
    }
}
