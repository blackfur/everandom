package sample.note;

import android.os.Bundle;
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
    Button save;

    @SneakyThrows
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repo = new Repository(this);

        setContentView(R.layout.activity_insert);
        input = findViewById(R.id.editText);

        String timestamp = getIntent().getStringExtra("timestamp");
        row = repo.select(timestamp);

        String txt = row.get(Repository.COLUMN_TXT);
        input.setText(txt);


        // save data
        save = findViewById(R.id.save);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String txt = input.getText().toString();
        row.put(Repository.COLUMN_TXT, txt);
        repo.update(row);
        Toast.makeText(this, "Saved!", Toast.LENGTH_LONG).show();
    }
}
