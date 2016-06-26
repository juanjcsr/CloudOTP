package org.fedorahosted.freeotp.external;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

/**
 * Created by jezz on 6/25/16.
 */
public class DropboxUploadTask extends AsyncTask<String, Void, FileMetadata> {

    private Context context;
    private DbxClientV2 clientV2;
    private Callback callback;

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
    protected FileMetadata doInBackground(String... params) {
        String localUri = params[0];
        //File localFile = UriHelpers
        return null;
    }

    protected void onPostExecute(FileMetadata result) {

    }
}
