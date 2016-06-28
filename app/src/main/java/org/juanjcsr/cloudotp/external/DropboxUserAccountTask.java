package org.juanjcsr.cloudotp.external;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

/**
 * Created by jezz on 21/06/16.
 */
public class DropboxUserAccountTask extends AsyncTask<Void, Void, FullAccount>{
    private DbxClientV2 dbxClientV2;
    private TaskDelegate delegate;
    private Exception error;

    public DropboxUserAccountTask(DbxClientV2 dbxClientV2, TaskDelegate delegate) {
        this.dbxClientV2 = dbxClientV2;
        this.delegate = delegate;
    }

    @Override
    protected FullAccount doInBackground(Void... voids) {
        try {
            //get the user account
            return dbxClientV2.users().getCurrentAccount();
        } catch (DbxException ex) {
            ex.printStackTrace();
            error = ex;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);

        if( account != null && error == null) {
            delegate.onAccountReceived(account);
        }else {
            delegate.onError(error);
        }
    }

    public interface TaskDelegate {
        void onAccountReceived(FullAccount account);
        void onError(Exception error);
    }
}
