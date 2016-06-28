package org.juanjcsr.cloudotp.external;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jezz on 23/06/16.
 */
public class DropboxDownloadTask extends AsyncTask<FileMetadata, Void, File> {

    private DbxClientV2 clientV2;
    private Callback callback;
    private Exception error;
    private Context context;

    @Override
    protected File doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        try {
            String fileName = metadata.getName();
            File file = new File(context.getCacheDir(), fileName);


            OutputStream os = new FileOutputStream(file);
            clientV2.files().download(metadata.getPathLower(), metadata.getRev())
                    .download(os);
            return file;
        } catch (DbxException | IOException ex) {
            error = ex;
        }
        return null;
    }

    public DropboxDownloadTask(DbxClientV2 clientV2, Callback callback, Context context) {
        this.clientV2 = clientV2;
        this.callback = callback;
        this.context = context;
    }

    @Override
    public void onPostExecute(File result){
        super.onPostExecute(result);
        if (error == null) {
            callback.onDownloadComplete(result);
        } else {
            callback.onError(error);
        }
    }


    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }
}
