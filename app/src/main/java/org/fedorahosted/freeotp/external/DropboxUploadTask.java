package org.fedorahosted.freeotp.external;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jezz on 6/25/16.
 */
public class DropboxUploadTask extends AsyncTask<File, Void, FileMetadata> {

    private Context context;
    private DbxClientV2 clientV2;
    private Callback callback;
    private Exception error;

    public interface Callback {
        void onUploadcomplete(FileMetadata result);
        void onError(Exception ex);
    }

    public DropboxUploadTask(Context context, DbxClientV2 client, Callback callback) {
        this.context = context;
        this.clientV2 = client;
        this.callback = callback;
    }


    @Override
    protected FileMetadata doInBackground(File... params) {
        File localFile = params[0];
        String remotePathName = "";
        String remoteFileName = localFile.getName();
        try {
            InputStream inputStream = new FileInputStream(localFile);
            return clientV2.files().uploadBuilder(remotePathName + "/" + remoteFileName)
                    .withMode(WriteMode.OVERWRITE).uploadAndFinish(inputStream);
        } catch (DbxException | IOException ex) {
            error = ex;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (error != null ) {callback.onError(error);}
        else if (result == null) { callback.onError(error);}
        else {
            callback.onUploadcomplete(result);
        }
    }
}
