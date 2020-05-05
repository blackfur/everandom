package sample.note

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class QrCodeActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var scanView: ZXingScannerView? = null
    private var log:TextView?=null
    private val REQUEST_CAMERA:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_qrcode)

        scanView = ZXingScannerView(this)
        scanView!!.setResultHandler(this)
        setContentView(scanView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(applicationContext, CAMERA ) == PackageManager.PERMISSION_GRANTED ) {
                scanView?.startCamera()
                return
            }
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA), REQUEST_CAMERA);
            return
        }
        scanView?.startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanView?.stopCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA -> if (grantResults.isNotEmpty()) {
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (cameraAccepted) {
                    Toast.makeText(applicationContext, "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show()
                    scanView?.startCamera()
                    return
                }
                Toast.makeText(applicationContext, "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show()

            }
        }
    }

    override fun handleResult(payload: Result?) {
        scanView?.stopCamera()
        log = TextView(this)
        setContentView(log)
        log!!.append("qrcode text:\n")
        log!!.append("${payload?.text}\n")
        log!!.append("qrcode format:\n")
        log!!.append("${payload?.barcodeFormat.toString()}\n")
    }
}