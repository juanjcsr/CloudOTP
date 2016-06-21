package org.fedorahosted.freeotp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.users.FullAccount;

import org.fedorahosted.freeotp.external.DropboxClient;
import org.fedorahosted.freeotp.external.DropboxUserAccountTask;

public class DropboxManagerActivity extends Activity {

    private static String NAME = "cloud_providers";
    private static String DBSTORAGE = "db-access-token";
    private String storedToken;
    private TextView loginData;
    private String ACCESS_TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);

        loginData = (TextView) findViewById(R.id.current_user_label);

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

        String accessToken = Auth.getOAuth2Token();
        SharedPreferences prefs = getSharedPreferences(NAME, Context.MODE_PRIVATE);
        storedToken = prefs.getString("db-access-token", "-");
        if (accessToken != null && storedToken.contentEquals("-")) {

            prefs.edit().putString("db-access-token", accessToken).apply();
            ACCESS_TOKEN = accessToken;
            getDropboxUserAccount();
            //go to main Activity

        } else {
            ACCESS_TOKEN = storedToken;
            getDropboxUserAccount();
        }
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


    private boolean dbTokenExists() {
        SharedPreferences prefs = getSharedPreferences(NAME, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(DBSTORAGE, null);
        return accessToken != null;
    }

    private String retrieveDbAccessToken() {
        SharedPreferences prefs = getSharedPreferences(NAME, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(DBSTORAGE, null);
        if (accessToken == null ) {
            Log.d("DB Access Token Status", "No Token found");
            return null;
        } else {
            Log.d("DB Access Token Status", "Token found");
            return accessToken;
        }
    }

    private void setDbAccessToken(String theToken) {
        SharedPreferences prefs = getSharedPreferences(NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(DBSTORAGE, theToken).apply();
    }

}
