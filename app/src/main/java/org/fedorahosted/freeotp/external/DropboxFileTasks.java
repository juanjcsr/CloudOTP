package org.fedorahosted.freeotp.external;

import android.os.AsyncTask;

import com.dropbox.core.v2.DbxClientV2;

/**
 * Created by jezz on 22/06/16.
 */
public class DropboxFileTasks extends AsyncTask {

    private DbxClientV2 dbxClientV2;
    private DropboxFileTasksDelegate delegate;
    private Exception error;

    public DropboxFileTasks(DbxClientV2 dbxClientV2, DropboxFileTasksDelegate delegate) {
        this.delegate = delegate;
        this.dbxClientV2 = dbxClientV2;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        return null;
    }

    private class DropboxFileTasksDelegate {
    }


}
