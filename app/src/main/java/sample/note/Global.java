package sample.note;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import okhttp3.OkHttpClient;

import javax.net.ssl.*;
import java.lang.reflect.Field;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Global {
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
