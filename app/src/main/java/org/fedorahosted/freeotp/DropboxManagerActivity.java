package org.fedorahosted.freeotp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.users.FullAccount;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.fedorahosted.freeotp.config.AccessTokenRetriever;
import org.fedorahosted.freeotp.external.DropboxClient;
import org.fedorahosted.freeotp.external.DropboxDownloadTask;
import org.fedorahosted.freeotp.external.DropboxFileSearchTask;
import org.fedorahosted.freeotp.external.DropboxPasswordFragment;
import org.fedorahosted.freeotp.external.DropboxUserAccountTask;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

public class DropboxManagerActivity extends Activity implements DropboxPasswordFragment.DropboxFilePasswordListener {

    private TextView loginData;
    private String ACCESS_TOKEN;
    private AccessTokenRetriever tokenManager;
    private Button mSyncButton;
    private SharedPreferences prefs;
    private DropboxClient dropboxClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);

        tokenManager = new AccessTokenRetriever(getApplicationContext());

        loginData = (TextView) findViewById(R.id.current_user_label);

        mSyncButton = (Button) findViewById(R.id.sync_token_button);
        mSyncButton.setOnClickListener(mSyncButtonListener);

        Button loginButton = (Button) findViewById(R.id.sign_in_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.DB_APP_KEY));
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        getAccessToken();
    }

    public void getAccessToken() {

        if (tokenManager.dbTokenExists()) {
            ACCESS_TOKEN = tokenManager.retrieveDbAccessToken();

            getDropboxUserAccount();
        } else {
            String accessToken = Auth.getOAuth2Token();
            if ( accessToken != null ){
                tokenManager.setDbAccessToken(accessToken);
                ACCESS_TOKEN =accessToken;
                getDropboxUserAccount();
            }
        }
    }

    private View.OnClickListener mSyncButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("Clicked", "Inside");
            prefs = getApplicationContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
            HashMap tokens = (HashMap) prefs.getAll();
            Log.d("Tokens", Integer.toString(tokens.size()));
            Gson gson = new Gson();
            Type hashmapStringType = new TypeToken<HashMap<String, String>>(){}.getType();
            String json = gson.toJson(tokens, hashmapStringType);
            Log.d("JSON", json);
            HashMap back = gson.fromJson(json, hashmapStringType);
            Log.d("MAP", "BACK");

            listDropboxFiles();

            FragmentManager manager = getFragmentManager();
            Fragment fragment = manager.findFragmentById(R.layout.fragment_dropbox_password);
            DropboxPasswordFragment dropboxPasswordFragment = new DropboxPasswordFragment();
            dropboxPasswordFragment.show(manager, "fragment_dropbox_password");

        }
    };

    private void listDropboxFiles(){
        new DropboxFileSearchTask(DropboxClient.getClient(ACCESS_TOKEN), new DropboxFileSearchTask.DropboxFileTasksDelegate() {
            @Override
            public void onListResultsReceived(SearchResult list) {
                if ( list.getMatches().size() > 0) {
                    FileMetadata fm = (FileMetadata) list.getMatches().get(0).getMetadata();
                    Log.d("File: ", fm.toStringMultiline());
                    downloadTokenDropbox(fm);
                }

            }

            @Override
            public void onError(Exception error) {
                Log.d("Dropbox", "List files");
            }
        }).execute();
    }

    private void downloadTokenDropbox(FileMetadata fm) {
        new DropboxDownloadTask(DropboxClient.getClient(ACCESS_TOKEN), new DropboxDownloadTask.Callback() {

            @Override
            public void onDownloadComplete(File result) {
                Log.d("Dropbox", "Got the file");

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.d("Dropbox", "could not download");
            }
        }, this.getApplicationContext()).execute(fm);
    }

    protected void getDropboxUserAccount() {
        if (ACCESS_TOKEN == null) return;
        new DropboxUserAccountTask(DropboxClient.getClient(ACCESS_TOKEN), new DropboxUserAccountTask.TaskDelegate() {

            @Override
            public void onAccountReceived(FullAccount account) {
                Log.d("User:", account.getEmail());
                Log.d("User:", account.getName().getDisplayName());
                Log.d("User:", account.getAccountType().name());
                loginData.setText("Welcome: " + account.getName().getDisplayName());

            }

            @Override
            public void onError(Exception error) {
                Log.d("User", "Error receiving account");
            }
        }).execute();
    }


    @Override
    public void onFinishPasswordDialog(String password) {
        Toast.makeText(this, "Tu Password: " + password, Toast.LENGTH_SHORT).show();
    }
}
