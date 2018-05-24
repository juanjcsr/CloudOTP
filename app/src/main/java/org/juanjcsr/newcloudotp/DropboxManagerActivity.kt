package org.juanjcsr.newcloudotp

import android.app.Fragment
import android.app.FragmentManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.dropbox.core.android.Auth
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.SearchResult
import com.dropbox.core.v2.users.FullAccount
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_dropbox.*

import org.juanjcsr.newcloudotp.config.AESStringCypher
import org.juanjcsr.newcloudotp.config.AccessTokenRetriever
import org.juanjcsr.newcloudotp.config.Utils
import org.juanjcsr.newcloudotp.external.DropboxClient
import org.juanjcsr.newcloudotp.external.DropboxDownloadTask
import org.juanjcsr.newcloudotp.external.DropboxFileSearchTask
import org.juanjcsr.newcloudotp.external.DropboxPasswordFragment
import org.juanjcsr.newcloudotp.external.DropboxUploadTask
import org.juanjcsr.newcloudotp.external.DropboxUserAccountTask

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.reflect.Type
import java.security.GeneralSecurityException
import java.util.HashMap

class DropboxManagerActivity : Activity(), DropboxPasswordFragment.DropboxFilePasswordListener {

    private var ACCESS_TOKEN: String? = null
    private var tokenManager: AccessTokenRetriever? = null
    private var prefs: SharedPreferences? = null
    private val dropboxClient: DropboxClient? = null
    private var mEncryptedFile: File? = null
    private var hasRemoteFile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox)

        tokenManager = AccessTokenRetriever(applicationContext)

        sync_token_button!!.setOnClickListener(mSyncButtonListener)
        sync_token_button!!.visibility = View.INVISIBLE

        sign_in_button!!.setOnClickListener { Auth.startOAuth2Authentication(this, getString(R.string.DB_APP_KEY)) }
    }

    override fun onResume() {
        super.onResume()
        getAccessToken()
    }

    fun getAccessToken() {

        if (tokenManager!!.dbTokenExists()) {
            ACCESS_TOKEN = tokenManager!!.retrieveDbAccessToken()

            getDropboxUserAccount()
        } else {
            val accessToken = Auth.getOAuth2Token()
            if (accessToken != null) {
                tokenManager!!.setDbAccessToken(accessToken)
                ACCESS_TOKEN = accessToken
                getDropboxUserAccount()
            }
        }
    }

    private val mSyncButtonListener = View.OnClickListener {
        val manager = fragmentManager
        val fragment = manager.findFragmentById(R.layout.fragment_dropbox_password)

        val dropboxPasswordFragment = DropboxPasswordFragment()
        val bundle = Bundle()
        bundle.putBoolean("hasRemoteFile", hasRemoteFile)
        dropboxPasswordFragment.arguments = bundle
        dropboxPasswordFragment.show(manager, "fragment_dropbox_password")
    }

    private fun listDropboxFiles() {
        DropboxFileSearchTask(DropboxClient.getClient(ACCESS_TOKEN!!), object : DropboxFileSearchTask.DropboxFileTasksDelegate {
            override fun onListResultsReceived(list: SearchResult) {
                if (list.matches.size > 0) {
                    val fm = list.matches[0].metadata as FileMetadata
                    //Log.d("File: ", fm.toStringMultiline());
                    hasRemoteFile = true
                    downloadTokenDropbox(fm)
                } else {
                    hasRemoteFile = false
                }

                sync_token_button!!.visibility = View.VISIBLE


            }

            override fun onError(error: Exception?) {
                Log.d("Dropbox", "List files");
            }
        }).execute(FILENAME)
    }

    private fun downloadTokenDropbox(fm: FileMetadata) {
        DropboxDownloadTask(DropboxClient.getClient(ACCESS_TOKEN!!), object : DropboxDownloadTask.Callback {

            override fun onDownloadComplete(result: File) {
                mEncryptedFile = result

                //Log.d("Dropbox", "Got the file");

            }

            override fun onError(e: Exception?) {
                e!!.printStackTrace()
                //Log.d("Dropbox", "could not download");
            }
        }, this.applicationContext).execute(fm)
    }

    protected fun getDropboxUserAccount() {
        if (ACCESS_TOKEN == null) return
        DropboxUserAccountTask(DropboxClient.getClient(ACCESS_TOKEN!!), object : DropboxUserAccountTask.TaskDelegate {

            override fun onAccountReceived(account: FullAccount) {
                //Log.d("User:", account.getEmail());
                //Log.d("User:", account.getName().getDisplayName());
                //Log.d("User:", account.getAccountType().name());
                current_user_label!!.text = "Welcome: " + account.name.displayName
                sign_in_button!!.visibility = View.GONE
                listDropboxFiles()

            }

            override fun onError(error: Exception?) {
                //Log.d("User", "Error receiving account");
            }
        }).execute()
    }


    override fun onFinishPasswordDialog(password: String) {
        val gson = Gson()
        prefs = applicationContext.getSharedPreferences("tokens", Context.MODE_PRIVATE)
        if (hasRemoteFile) {
            val fileLength = mEncryptedFile!!.length().toInt()
            val bytes = ByteArray(fileLength)

            try {
                val inputFile = FileInputStream(mEncryptedFile!!)
                inputFile.read(bytes)
                val contents = String(bytes)
                //Log.d("FileRead", contents);
                val cypher = AESStringCypher.CipherTextIvMac(contents)
                val keys = AESStringCypher.generateKeyFromPassword(password, password)
                val decrypted = AESStringCypher.decryptString(cypher, keys)
                if (decrypted.contains("{}")) {
                    Toast.makeText(this, R.string.no_keys, Toast.LENGTH_LONG).show()
                } else {
                    val back = Utils.transformJsonToStringHashMap(decrypted)
                    val remoteDate = java.lang.Long.parseLong(back["lastModified"])
                    val localDate = java.lang.Long.parseLong(prefs!!.getString("lastModified", "-1"))
                    //Log.d("DATES:", "REMOTE DATE = " + remoteDate + " LOCALDATE = " + localDate + " REMOTE NEWER? " + Utils.isRemoteDateNewer(localDate, remoteDate));
                    if (Utils.isRemoteDateNewer(localDate, remoteDate)) {
                        Utils.overwriteAndroidSharedPrefereces(back, prefs)
                        Toast.makeText(this, R.string.local_sync_success, Toast.LENGTH_SHORT).show()

                    } else {
                        val encrypted = encryptSharedPrefs(password, gson)
                        mEncryptedFile = Utils.createCachedFileFromTokenString(encrypted,
                                FILENAME,
                                applicationContext)
                        uploadFileToDropbox()
                        Toast.makeText(this, R.string.remote_sync_success, Toast.LENGTH_SHORT).show()

                    }
                }

            } catch (ex: IOException) {
                ex.printStackTrace()
            } catch (ex: GeneralSecurityException) {
                //Log.d("Decrypt", "Wrong password");
                Toast.makeText(this, R.string.sync_password_error, Toast.LENGTH_SHORT).show()
            }


        } else {
            try {
                val encrypted = encryptSharedPrefs(password, gson)
                Log.d("ENCRYPTED", encrypted)
                mEncryptedFile = Utils.createCachedFileFromTokenString(encrypted,
                        FILENAME,
                        applicationContext)
                uploadFileToDropbox()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    @Throws(UnsupportedEncodingException::class, GeneralSecurityException::class)
    private fun encryptSharedPrefs(password: String, gson: Gson): String {
        val tokens = prefs!!.all as HashMap<*, *>
        //Log.d("Tokens", Integer.toString(tokens.size()));

        val hashmapStringType = object : TypeToken<HashMap<String, String>>() {

        }.type
        val json = gson.toJson(tokens, hashmapStringType)
        //Log.d("JSON", json);
        val keys = AESStringCypher.generateKeyFromPassword(password, password)
        val toencrypt = AESStringCypher.encrypt(json, keys)
        return toencrypt.toString()
    }

    private fun uploadFileToDropbox() {
        DropboxUploadTask(this.applicationContext, DropboxClient.getClient(ACCESS_TOKEN!!), object : DropboxUploadTask.Callback {

            override fun onUploadcomplete(result: FileMetadata) {
                //Log.d("Uploaded", result.toStringMultiline());
                Toast.makeText(applicationContext,
                        R.string.sync_success,
                        Toast.LENGTH_LONG).show()
                hasRemoteFile = true

            }

            override fun onError(ex: Exception?) {
                Log.e("Uploaded", ex!!.localizedMessage)
            }
        }).execute(mEncryptedFile)
    }

    companion object {
        private val FILENAME = "otptokens.db"
    }


}
