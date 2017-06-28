package org.juanjcsr.newcloudotp.external

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2

/**
 * Created by jezz on 21/06/16.
 */
object DropboxClient {

    fun getClient(ACCESS_TOKEN: String): DbxClientV2 {
        val config = DbxRequestConfig("dropbox/freeopt-cloud")
        return DbxClientV2(config, ACCESS_TOKEN)
    }
}
