package org.juanjcsr.newcloudotp.external

import android.content.Context
import android.os.AsyncTask

import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.WriteMode

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by jezz on 6/25/16.
 */
class DropboxUploadTask(private val context: Context, private val clientV2: DbxClientV2, private val callback: DropboxUploadTask.Callback) : AsyncTask<File, Void, FileMetadata>() {
    private var error: Exception? = null

    interface Callback {
        fun onUploadcomplete(result: FileMetadata)
        fun onError(ex: Exception?)
    }


    override fun doInBackground(vararg params: File): FileMetadata? {
        val localFile = params[0]
        val remotePathName = ""
        val remoteFileName = localFile.name
        try {
            val inputStream = FileInputStream(localFile)
            return clientV2.files().uploadBuilder(remotePathName + "/" + remoteFileName)
                    .withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream)
        } catch (ex: DbxException) {
            error = ex
        } catch (ex: IOException) {
            error = ex
        }

        return null
    }

    override fun onPostExecute(result: FileMetadata?) {
        super.onPostExecute(result)
        var ex = error;
        if (ex != null) {
            callback.onError(ex)
        } else if (result == null) {
            callback.onError(ex)
        } else {
            callback.onUploadcomplete(result)
        }
    }
}
