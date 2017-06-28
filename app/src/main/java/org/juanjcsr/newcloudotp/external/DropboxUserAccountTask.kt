package org.juanjcsr.newcloudotp.external

import android.os.AsyncTask

import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.users.FullAccount

/**
 * Created by jezz on 21/06/16.
 */
class DropboxUserAccountTask(private val dbxClientV2: DbxClientV2, private val delegate: DropboxUserAccountTask.TaskDelegate) : AsyncTask<Void, Void, FullAccount>() {
    private var error: Exception? = null

    override fun doInBackground(vararg voids: Void): FullAccount? {
        try {
            //get the user account
            return dbxClientV2.users().currentAccount
        } catch (ex: DbxException) {
            ex.printStackTrace()
            error = ex
        }

        return null
    }

    override fun onPostExecute(account: FullAccount?) {
        super.onPostExecute(account)
        var err = error
        if (account != null && err == null) {
            delegate.onAccountReceived(account)
        } else {
            delegate.onError(err)
        }
    }

    interface TaskDelegate {
        fun onAccountReceived(account: FullAccount)
        fun onError(error: Exception?)
    }
}
