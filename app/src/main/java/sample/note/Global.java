package sample.note;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import androidx.preference.PreferenceManager;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;

import javax.net.ssl.*;
import java.lang.reflect.Field;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Global extends Application {
    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        /* If you has other classes that need context object to initialize when application is created,
         you can use the appContext here to process. */
    }

    @SneakyThrows
    public static String host(){

        SharedPreferences opt=
        PreferenceManager.getDefaultSharedPreferences(appContext);
        String host = opt.getString("prefix", Property.get("host"));
        Log.i("get host", host);
        return host;
    }

    @SneakyThrows
    public static String prop(String key){

        SharedPreferences opt=
                PreferenceManager.getDefaultSharedPreferences(appContext);
        String val= opt.getString(key, Property.get(key));
        Log.i(key, val);
        return val;
    }
    public static void error(Context ctx, String hint, Exception e) {
        Log.e("Notes: ", hint, e);
        Toast.makeText(ctx, hint + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    public static OkHttpClient okHttpClient() throws NoSuchAlgorithmException, KeyManagementException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        OkHttpClient hc = new OkHttpClient();
        SSLContext sc = null;
        sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }}, new SecureRandom());

        HostnameVerifier hv = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        Class clazz = Class.forName("okhttp3.OkHttpClient");
        Field hostnameVerifier = clazz.getDeclaredField("hostnameVerifier");
        hostnameVerifier.setAccessible(true);
        hostnameVerifier.set(hc, hv);

        Field sslSocketFactory = clazz.getDeclaredField("sslSocketFactoryOrNull");
        sslSocketFactory.setAccessible(true);
        sslSocketFactory.set(hc, sc.getSocketFactory());

        return hc;
    }
}
