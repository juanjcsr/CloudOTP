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

import org.fedorahosted.freeotp.config.AESStringCypher;
import org.fedorahosted.freeotp.config.AccessTokenRetriever;
import org.fedorahosted.freeotp.external.DropboxClient;
import org.fedorahosted.freeotp.external.DropboxDownloadTask;
import org.fedorahosted.freeotp.external.DropboxFileSearchTask;
import org.fedorahosted.freeotp.external.DropboxPasswordFragment;
import org.fedorahosted.freeotp.external.DropboxUploadTask;
import org.fedorahosted.freeotp.external.DropboxUserAccountTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class DropboxManagerActivity extends Activity implements DropboxPasswordFragment.DropboxFilePasswordListener {

    private TextView loginData;
    private String ACCESS_TOKEN;
    private AccessTokenRetriever tokenManager;
    private Button mSyncButton;
    private SharedPreferences prefs;
    private DropboxClient dropboxClient;
    private File mEncryptedFile;
    private boolean hasRemoteFile = false;

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
                } else {
                    hasRemoteFile = false;
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
                mEncryptedFile = result;
                hasRemoteFile = true;
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
                Log.d("FileRead", decrypted);
                Type hashmapStringType = new TypeToken<HashMap<String, String>>(){}.getType();
                HashMap<String, String> back = gson.fromJson(decrypted, hashmapStringType);
                Log.d("FileRead", back.toString());
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                for (String s : back.keySet()) {
                    editor.putString(s, back.get(s));
                }
                editor.commit();

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (GeneralSecurityException ex) {
                Log.d("Decrypt", "Wrong password");
            }


        } else {
            Log.d("Clicked", "Inside");

            HashMap tokens = (HashMap) prefs.getAll();
            Log.d("Tokens", Integer.toString(tokens.size()));

            Type hashmapStringType = new TypeToken<HashMap<String, String>>(){}.getType();
            String json = gson.toJson(tokens, hashmapStringType);
            Log.d("JSON", json);
            try {
                AESStringCypher.SecretKeys keys = AESStringCypher.generateKeyFromPassword(password, password);
                AESStringCypher.CipherTextIvMac toencrypt = AESStringCypher.encrypt(json, keys);
                Log.d("AES! ", "Encriptado: " + toencrypt.toString());
                AESStringCypher.CipherTextIvMac cypher = new AESStringCypher.CipherTextIvMac(toencrypt.toString());
                String decrypted = AESStringCypher.decryptString(cypher, keys);
                Log.d("AES!", "Desencriptado: " + decrypted);
                HashMap back = gson.fromJson(decrypted, hashmapStringType);
                Log.d("MAP", back.toString());
                mEncryptedFile = createFileFromString(toencrypt.toString());
                uploadFileToDropbox();

            } catch (Exception e) {
                Log.e("Encrypt","could not encrypt");
            }

        }
        Toast.makeText(this, "Tu Password: " + password, Toast.LENGTH_SHORT).show();

    }

    private void uploadFileToDropbox() {
        new DropboxUploadTask(this.getApplicationContext(), DropboxClient.getClient(ACCESS_TOKEN), new DropboxUploadTask.Callback(){

            @Override
            public void onUploadcomplete(FileMetadata result) {
                Log.d("Uploaded", result.toStringMultiline());
            }

            @Override
            public void onError(Exception ex) {
                Log.e("Uploaded", ex.getLocalizedMessage());
            }
        }).execute(mEncryptedFile);
    }

    private File createFileFromString(String text) throws IOException {

        File file = new File(getCacheDir(), "otptokens.db");
        FileWriter fw = new FileWriter(file);
        fw.write(text);
        fw.flush();
        fw.close();
        return file;
    }
}
