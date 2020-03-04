package sample.note;

import android.content.ContentValues;
import android.os.Bundle;
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
    Button save;

    @SneakyThrows
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repo = new Repository(this);

        setContentView(R.layout.activity_insert);
        input = findViewById(R.id.editText);

        // save data
        save = findViewById(R.id.save);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String txt = input.getText().toString();
        if(txt.length() == 0){
            Toast.makeText(this, "Empty!", Toast.LENGTH_LONG).show();
            return;
        }
        ContentValues row= new ContentValues();
        row.put(Repository.COLUMN_TXT, txt);
        row.put(Repository.COLUMN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        repo.insert(row);
        Toast.makeText(this, "Saved!", Toast.LENGTH_LONG).show();
    }
}
