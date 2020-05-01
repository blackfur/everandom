package sample.note

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import lombok.SneakyThrows
import okhttp3.Request

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

        log!!.append("checking version from server...\n")

        CheckQuest().execute()
    }

    private fun uilog(msg: String){
       runOnUiThread { log!!.append(msg) }
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
