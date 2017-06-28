package org.juanjcsr.newcloudotp.external

import android.content.Context
import android.os.AsyncTask

import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Created by jezz on 23/06/16.
 */
class DropboxDownloadTask(private val clientV2: DbxClientV2, private val callback: DropboxDownloadTask.Callback, private val context: Context) : AsyncTask<FileMetadata, Void, File>() {
    private var error: Exception? = null

    override fun doInBackground(vararg params: FileMetadata): File? {
        val metadata = params[0]
        try {
            val fileName = metadata.name
            val file = File(context.cacheDir, fileName)


            val os = FileOutputStream(file)
            clientV2.files().download(metadata.pathLower, metadata.rev)
                    .download(os)
            return file
        } catch (ex: DbxException) {
            error = ex
        } catch (ex: IOException) {
            error = ex
        }

        return null
    }

    public override fun onPostExecute(result: File) {
        super.onPostExecute(result)
        var err = error
        if (err == null) {
            callback.onDownloadComplete(result)
        } else {
            callback.onError(err)
        }
    }


    interface Callback {
        fun onDownloadComplete(result: File)
        fun onError(e: Exception?)
    }
}
