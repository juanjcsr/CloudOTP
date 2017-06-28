package org.juanjcsr.newcloudotp.external

import android.os.AsyncTask
import android.util.Log

import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.SearchResult

/**
 * Created by jezz on 22/06/16.
 */
class DropboxFileSearchTask(private val dbxClientV2: DbxClientV2, private val delegate: DropboxFileSearchTask.DropboxFileTasksDelegate) : AsyncTask<String, Void, SearchResult>() {
    private var error: Exception? = null

    override fun doInBackground(vararg filename: String): SearchResult? {
        val file = filename[0]
        try {
            //Log.d("DB","OBTENIENDO....");
            return dbxClientV2.files().search("", file)
            //Download

        } catch (ex: DbxException) {
            ex.printStackTrace()
            error = ex
        }

        return null
    }


    override fun onPostExecute(list: SearchResult) {
        super.onPostExecute(list)
        var e = error
        if (e == null) {
            delegate.onListResultsReceived(list)
        } else {
            delegate.onError(e)
        }
    }

    /*@Override
    protected File doInBackground(FileMetadata... fileMetadatas) {
        FileMetadata metadata = fileMetadatas[0];

        try {
            File path = Environment.getDataDirectory();
            File file = new File(path, metadata.getName());
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    error = new RuntimeException("Cant create dir: " + path);
                }
            } else if (!path.isDirectory()) {
                error = new IllegalStateException("Downloadpath is not directory: " + path );
                return null;
            }
            OutputStream os = new FileOutputStream(file);
            dbxClientV2.files().download(metadata.getPathLower(), metadata.getRev())
                    .download(os);

            return file;
        } catch (DbxException | IOException ex) {
            error = ex;
        }
        return null;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (error == null ) {
            delegate.onFileDownloaded(result);
        } else {
            delegate.onError(error);
        }
    }*/

    interface DropboxFileTasksDelegate {
        //void onFileDownloaded(File list);
        fun onListResultsReceived(list: SearchResult)

        fun onError(error: Exception?)
    }


}
