package sample.note

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import lombok.SneakyThrows
import okhttp3.Request
import java.io.File
import java.io.InputStream


class VerUptActivity : AppCompatActivity() {

    private var client = Global.okHttpClient()
    //val JSON: MediaType? = "application/json; charset=utf-8".toMediaType()
    private val gson = GsonBuilder().create()
    private val prefix:String? = Global.host()
    private var log:TextView? = null
    private var button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_upt)

        log = findViewById<TextView>(R.id.log)
        button = findViewById(R.id.go)
        button!!.setOnClickListener { downloadQuest().execute() }

        log!!.append("checking version from server...\n")

        CheckQuest().execute()
    }

    private fun uilog(msg: String){
       runOnUiThread { log!!.append(msg) }
    }

    inner class downloadQuest: AsyncTask<Any?, Any?, Any?>(){
        private val tag = "download quest"
        private val url = prefix + REMOTE_APK_FILE
        override fun doInBackground(vararg params: Any?): Any? {
            // Create request builder.
            var builder: Request.Builder = Request.Builder()
            // Set url.
            runOnUiThread { log!!.append("downloading from: $url\n") }
            builder = builder.url(url)
            // Create request object.
            val request = builder.build()
            // Get okhttp3.Call object.
            val response = client.newCall(request).execute()

            val respCode = response.code
            val respMsg = response.message
            //val respBody = response.body!!.string()
            Log.d(tag, "Response code : $respCode")
            Log.d(tag, "Response message : $respMsg")
            //Log.d(tag, "Response body : $respBody")

            if(respCode != 200){
               runOnUiThread { log!!.append("$respCode: $respMsg\n") }
                return null
            }

            val localApk = LOCAL_APK_FILE()
            val inputStream: InputStream = response.body!!.byteStream()
            val total:Int = download(File(localApk) , inputStream)
            {
                total -> if(total%1048576 == 0)
                runOnUiThread { log!!.append("-> ${total/1048576} mb") }
            }

            runOnUiThread { log!!.append("\ndownload success: ${total/1048576} mb, ${localApk}\n") }

           // install apk
            val apk= File(localApk)
            if (!apk!!.exists()) {
                runOnUiThread { log!!.append("apk not found\n") }
                return null
            }
            val i = Intent(Intent.ACTION_VIEW)
            i.setDataAndType(apk.uri(), "application/vnd.android.package-archive")
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            runOnUiThread { startActivity(i) }

            return null
        }
    }

    inner class CheckQuest : AsyncTask<Any?, Any?, Any?>() {

        private val url = prefix + "version.php"
        private val tag = "check quest"

        @SneakyThrows
        override fun doInBackground(objects: Array<Any?>): Any? {

            // current version
            val info = Global.appContext.packageManager.getPackageInfo(packageName, 0)
            val currentVer = info.versionName.toLong()
            uilog("${info.versionName}\n")

            Log.i(tag, "URL: $url")

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val payload = response.body!!.string()
            if (response.code != 200) {
                Log.i(tag, payload)
                throw Exception("$tag: $payload")
            }


            // newest version
            uilog("$payload\n")
            val versionInfo: JsonObject = gson.fromJson(payload!! as? String, JsonObject::class.java)
            val ver = versionInfo.get("version").asString.toLong()

            if(currentVer >= ver) {
                uilog("You already installed newest version.\n")
                return null
            }
            uilog("You can install newest version now.\n")

            runOnUiThread { button?.visibility = View.VISIBLE }

            return payload
        }
    }
}
