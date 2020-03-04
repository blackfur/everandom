package sample.note;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import okhttp3.*;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    SwipeRefreshLayout layout;
    ListView lv;
    Repository repo;
    String TAG;
    NotesAdapter adapter;
    int position;
    //WebSocket ws;
    Button button;

    @lombok.SneakyThrows
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // repository
        repo = new Repository(this);

        TAG = Property.get("tag", getApplicationContext());

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
        findViewById(R.id.insert).setOnClickListener(this);
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
            Intent insert = new Intent(this, UpdateActivity.class);
            Map<String, String> data = adapter.getItem(position);
            String timestamp = data.get(Repository.COLUMN_TIMESTAMP);
            insert.putExtra("timestamp", timestamp);
            startActivity(insert);
            return true;
        }

        if (selected == R.id.insert) {
            Intent insert = new Intent(this, InsertActivity.class);
            startActivity(insert);
            return true;
        }

        if (selected == R.id.del) {
            Map<String, String> data = adapter.getItem(position);
            repo.delete(data);
            Toast.makeText(this, "deleted!", Toast.LENGTH_LONG).show();
            return true;
        }

        if (selected == R.id.synchronize) {
            SyncQuest syncQuest = new SyncQuest();

            String host = Property.get("host", this);
            String limit = Property.get("limit", this);

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
        Intent insert = new Intent(this, InsertActivity.class);
        startActivity(insert);
    }

    /*
    @SneakyThrows
    void sync(){
        int timeout = Integer.valueOf(Property.get("timeout", this));
        String webSocketUri= Property.get("WebSocketUri", this);
        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(timeout);

        // Create a WebSocket. The timeout value set above is used.
        ws = factory.createSocket(webSocketUri);

        ws.addListener(new SyncAdapter());
        ws.connectAsynchronously();
        if (ws.isOpen()) {
            ws.sendText("Message from Android!");
        }
        ws.disconnect();
        ws = null;
    }
    class SyncAdapter extends WebSocketAdapter{
        @Override
        public void onTextMessage(WebSocket websocket, String message) throws Exception {
            Log.d("TAG", "onTextMessage: " + message);
        }
    }
     */

    class SyncQuest extends AsyncTask {

        OkHttpClient client = new OkHttpClient();
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        @SneakyThrows
        @Override
        protected void onPostExecute(Object s) {
            super.onPostExecute(s);
            ArrayList<Map<String, String>> list = repo.select();
            adapter.notifyChanged(list);
            layout.setRefreshing(false);
            Toast.makeText(getApplicationContext(), s.toString(), Toast.LENGTH_LONG).show();
        }

        @SneakyThrows
        @Override
        protected Object doInBackground(Object[] objects) {
            String host = objects[0].toString();
            String limit = objects[1].toString();
            String insertUrl = host + "insert.php";
            Log.i("TAG", "Insert URL: " + insertUrl);

            Request.Builder builder = new Request.Builder();
            builder.url(insertUrl);

            ArrayList<Map<String, String>> rows;
            Gson gson = new GsonBuilder().create();

            // insert to Server
            int offset = 0;
            int step = Integer.valueOf(limit);

            rows = repo.select(limit, String.valueOf(offset));
            try{
                while (rows.size() > 0){
                    String feed = gson.toJson(rows);
                    Log.i("TAG", feed);
                    RequestBody body = RequestBody.create(feed, JSON);
                    Request request = new Request.Builder() .url(insertUrl) .post(body) .build();
                    Response response = client.newCall(request).execute();
                    if(response.code()!=200){
                        String error = response.body().string();
                        Log.i("sync.post", error);
                        return error;
                    }

                    offset += step;
                    rows = repo.select(limit, String.valueOf(offset));
                }
            }catch (Exception ex){
                Log.e("sync.post.loop", ex.getMessage(), ex);
                return ex.getMessage();
            }

            // restore from Server
            String selectUrl= host + "select.php";

            offset = 0;
            int statusCode = 500;

            JsonObject feed = new JsonObject();
            feed.addProperty("limit", limit);
            do{

                feed.addProperty("offset", offset);
                String tmp = feed.toString();
                Log.i("TAG", tmp);
                RequestBody body = RequestBody.create(tmp, JSON);
                Request request = new Request.Builder() .url(selectUrl) .post(body) .build();
                Response response = client.newCall(request).execute();
                statusCode = response.code();
                if(statusCode == 204)
                    break;
                if(statusCode!=200){
                    String error = response.body().string();
                    Log.i("sync.obtain", error);
                    return error;
                }

                String content = response.body().string();
                Log.i("sync.restore", content);
                // store data
                JsonArray array = gson.fromJson(content, JsonArray.class);
                repo.restore(array);

                offset += step;
            }while(statusCode ==200);

            return "Synchronized!";
        }
    }
}
