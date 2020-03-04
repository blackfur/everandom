package sample.note;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Global {
    public static void error(Context ctx, String hint, Exception e){
        Log.e("Notes: ",  hint, e);
        Toast.makeText(ctx, hint + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
