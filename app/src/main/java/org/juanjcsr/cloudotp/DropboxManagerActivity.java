package org.juanjcsr.cloudotp;

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

import org.juanjcsr.cloudotp.config.AESStringCypher;
import org.juanjcsr.cloudotp.config.AccessTokenRetriever;
import org.juanjcsr.cloudotp.config.Utils;
import org.juanjcsr.cloudotp.external.DropboxClient;
import org.juanjcsr.cloudotp.external.DropboxDownloadTask;
import org.juanjcsr.cloudotp.external.DropboxFileSearchTask;
import org.juanjcsr.cloudotp.external.DropboxPasswordFragment;
import org.juanjcsr.cloudotp.external.DropboxUploadTask;
import org.juanjcsr.cloudotp.external.DropboxUserAccountTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class DropboxManagerActivity extends Activity implements DropboxPasswordFragment.DropboxFilePasswordListener {

    private TextView loginData;
    private String ACCESS_TOKEN;
    private AccessTokenRetriever tokenManager;
    private Button mSyncButton;
    private Button mLoginButton;
    private SharedPreferences prefs;
    private DropboxClient dropboxClient;
    private File mEncryptedFile;
    private boolean hasRemoteFile = false;
    private static final String FILENAME = "otptokens.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);

        tokenManager = new AccessTokenRetriever(getApplicationContext());

        loginData = (TextView) findViewById(R.id.current_user_label);

        mSyncButton = (Button) findViewById(R.id.sync_token_button);
        mSyncButton.setOnClickListener(mSyncButtonListener);
        mSyncButton.setVisibility(View.INVISIBLE);

        mLoginButton = (Button) findViewById(R.id.sign_in_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
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

            FragmentManager manager = getFragmentManager();
            Fragment fragment = manager.findFragmentById(R.layout.fragment_dropbox_password);

            DropboxPasswordFragment dropboxPasswordFragment = new DropboxPasswordFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("hasRemoteFile", hasRemoteFile);
            dropboxPasswordFragment.setArguments(bundle);
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
                    hasRemoteFile = true;
                    downloadTokenDropbox(fm);
                } else {
                    hasRemoteFile = false;
                }

                mSyncButton.setVisibility(View.VISIBLE);


            }

            @Override
            public void onError(Exception error) {
                Log.d("Dropbox", "List files");
            }
        }).execute(FILENAME);
    }

    private void downloadTokenDropbox(FileMetadata fm) {
        new DropboxDownloadTask(DropboxClient.getClient(ACCESS_TOKEN), new DropboxDownloadTask.Callback() {

            @Override
            public void onDownloadComplete(File result) {
                mEncryptedFile = result;

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
                mLoginButton.setVisibility(View.GONE);
                listDropboxFiles();

            }

            @Override
            public void onError(Exception error) {
                Log.d("User", "Error receiving account");
            }
        }).execute();
    }


    @Override
    public void onFinishPasswordDialog(String password) {
        Gson gson = new Gson();
        prefs = getApplicationContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        if ( hasRemoteFile ) {
            int fileLength = ( int ) mEncryptedFile.length();
            byte[] bytes = new byte[fileLength];

            try {
                FileInputStream in = new FileInputStream(mEncryptedFile);
                in.read(bytes);
                String contents = new String(bytes);
                Log.d("FileRead", contents);
                AESStringCypher.CipherTextIvMac cypher = new AESStringCypher.CipherTextIvMac(contents);
                AESStringCypher.SecretKeys keys = AESStringCypher.generateKeyFromPassword(password, password);
                String decrypted = AESStringCypher.decryptString(cypher, keys);
                HashMap<String, String> back = Utils.transformJsonToStringHashMap(decrypted);
                long remoteDate = Long.parseLong( back.get("lastModified"));
                long localDate = Long.parseLong( prefs.getString("lastModified", "-1"));
                Log.d("DATES:", "REMOTE DATE = " + remoteDate + " LOCALDATE = " + localDate + " REMOTE NEWER? " + Utils.isRemoteDateNewer(localDate, remoteDate));
                if ( Utils.isRemoteDateNewer(localDate, remoteDate) ) {
                    Utils.overwriteAndroidSharedPrefereces(back, prefs);
                    Toast.makeText(this, R.string.local_sync_success, Toast.LENGTH_SHORT).show();

                } else {
                    String encrypted = encryptSharedPrefs(password, gson);
                    mEncryptedFile = Utils.createCachedFileFromTokenString(encrypted,
                            FILENAME,
                            getApplicationContext());
                    uploadFileToDropbox();
                    Toast.makeText(this, R.string.remote_sync_success, Toast.LENGTH_SHORT).show();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (GeneralSecurityException ex) {
                Log.d("Decrypt", "Wrong password");
                Toast.makeText(this, R.string.sync_password_error, Toast.LENGTH_SHORT).show();
            }


        } else {
            try {
                String encrypted = encryptSharedPrefs(password, gson);
                mEncryptedFile = Utils.createCachedFileFromTokenString(encrypted,
                        FILENAME,
                        getApplicationContext());
                uploadFileToDropbox();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String encryptSharedPrefs(String password, Gson gson)
            throws UnsupportedEncodingException, GeneralSecurityException {
        HashMap tokens = (HashMap) prefs.getAll();
        Log.d("Tokens", Integer.toString(tokens.size()));

        Type hashmapStringType = new TypeToken<HashMap<String, String>>(){}.getType();
        String json = gson.toJson(tokens, hashmapStringType);
        Log.d("JSON", json);
        AESStringCypher.SecretKeys keys = AESStringCypher.generateKeyFromPassword(password, password);
        AESStringCypher.CipherTextIvMac toencrypt = AESStringCypher.encrypt(json, keys);
        return toencrypt.toString();
    }

    private void uploadFileToDropbox() {
        new DropboxUploadTask(this.getApplicationContext(), DropboxClient.getClient(ACCESS_TOKEN), new DropboxUploadTask.Callback(){

            @Override
            public void onUploadcomplete(FileMetadata result) {
                Log.d("Uploaded", result.toStringMultiline());
                Toast.makeText(getApplicationContext(),
                        R.string.sync_success,
                        Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(Exception ex) {
                Log.e("Uploaded", ex.getLocalizedMessage());
            }
        }).execute(mEncryptedFile);
    }


}
