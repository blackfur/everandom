package sample.note;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
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

public class InsertActivity extends AppCompatActivity implements View.OnClickListener {

    EditText input;
    Repository repo;
    Button help;

    @SneakyThrows
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repo = new Repository(this);

        setContentView(R.layout.activity_insert);
        input = findViewById(R.id.editText);

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
            if(txt.length() == 0){
                Toast.makeText(this, "Empty!", Toast.LENGTH_LONG).show();
                return true;
            }
            ContentValues row= new ContentValues();
            row.put(Repository.COLUMN_TXT, txt);
            row.put(Repository.COLUMN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
            repo.insert(row);
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
