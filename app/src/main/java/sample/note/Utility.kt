package sample.note

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import sample.note.Global.appContext
import java.io.*

fun LOCAL_APK_FILE():String {
   val f = appContext.getExternalFilesDir("Download")!!.absolutePath + "/app-release.apk"
   Log.i("local apk file", f)
   return f
}

// return total download size
// progress current total download size
fun download(destination: File, inputStream: InputStream, progress: (Int)->Unit) :Int{

   // File output stream related object.
    if(!destination.exists()) destination.createNewFile()
   val fileOutputStream = FileOutputStream(destination)
   val bufferedOutputStream = BufferedOutputStream(fileOutputStream)

   // File input stream related object.
   val bufferedInputStream = BufferedInputStream(inputStream)

   // Read data from input stream.
   val dataBuf = ByteArray(1024)
   var readLen: Int = bufferedInputStream.read(dataBuf)
   var total = readLen

   // If read data byte length bigger than -1.
   while (readLen > -1) {
      // Write buffer data to output stream.
      bufferedOutputStream.write(dataBuf, 0, readLen)
      // Read data again.
      readLen = bufferedInputStream.read(dataBuf)
      total += readLen
      progress(total)
   }
   // Close input stream.
   bufferedInputStream.close()
   // Flush and close output stream.
   bufferedOutputStream.flush()
   bufferedOutputStream.close()
   return total
}

// manifest: provider
fun File.uri(): Uri {
   var fileUri: Uri = when{
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
         -> getUriForFile(appContext, appContext.packageName,this)
      else ->Uri.fromFile(this)
   }
   Log.i("file to uri",fileUri.toString())
   return fileUri;
}