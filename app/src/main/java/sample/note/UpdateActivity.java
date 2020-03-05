package sample.note;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import lombok.SneakyThrows;

import java.util.Map;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener {

    EditText input;
    Repository repo;
    Map<String, String> row;
    Button help;

    @SneakyThrows
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repo = new Repository(this);

        setContentView(R.layout.activity_insert);
        input = findViewById(R.id.editText);

        String timestamp = getIntent().getStringExtra("timestamp");
        try{
            row = repo.select(timestamp);
        }catch (Exception ex){
            Log.e("view.row", ex.getMessage(), ex);
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        String txt = row.get(Repository.COLUMN_TXT);
        input.setText(txt);


        // save data
        help = findViewById(R.id.help);
        // context menu
        registerForContextMenu(help);
        help.setOnClickListener(this);
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

        if (selected == R.id.save) {
            String txt = input.getText().toString();
            row.put(Repository.COLUMN_TXT, txt);
            repo.update(row);
            Toast.makeText(this, "Saved!", Toast.LENGTH_LONG).show();
            onBackPressed();
            return true;
        }

        return false;
    }
    @Override
    public void onClick(View v) {
        openContextMenu(v);
    }
}
