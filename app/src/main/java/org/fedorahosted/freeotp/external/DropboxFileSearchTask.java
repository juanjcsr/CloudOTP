package org.fedorahosted.freeotp.external;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.SearchResult;

/**
 * Created by jezz on 22/06/16.
 */
public class DropboxFileSearchTask extends AsyncTask<String, Void, SearchResult> {

    private DbxClientV2 dbxClientV2;
    private DropboxFileTasksDelegate delegate;
    private Exception error;

    public DropboxFileSearchTask(DbxClientV2 dbxClientV2, DropboxFileTasksDelegate delegate) {
        this.delegate = delegate;
        this.dbxClientV2 = dbxClientV2;
    }

    @Override
    protected SearchResult doInBackground(String... filename) {
        String file = filename[0];
        try {
            Log.d("DB","OBTENIENDO....");
            return dbxClientV2.files().search("", file);
            //Download

        } catch (DbxException ex) {
            ex.printStackTrace();
            error = ex;
        }
        return null;
    }


    @Override
    protected void onPostExecute(SearchResult list) {
        super.onPostExecute(list);

        if (error == null) {
            delegate.onListResultsReceived(list);
        } else {
            delegate.onError(error);
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

    public interface DropboxFileTasksDelegate {
        //void onFileDownloaded(File list);
        void onListResultsReceived(SearchResult list);
        void onError(Exception error);
    }


}
