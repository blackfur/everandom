package sample.note;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Property {
    static Properties properties;
    static Properties obtain(Context context) throws IOException {
        if(properties != null)
            return properties;
        properties= new Properties();;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("application.properties");
        properties.load(inputStream);
        return properties;
    }
    public static String get(String key,Context context) throws IOException {
        return obtain(context).getProperty(key);
    }
}
