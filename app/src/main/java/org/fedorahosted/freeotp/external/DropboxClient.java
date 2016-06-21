package org.fedorahosted.freeotp.external;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

/**
 * Created by jezz on 21/06/16.
 */
public class DropboxClient {

    public static DbxClientV2 getClient(String ACCESS_TOKEN) {
        DbxRequestConfig config = new DbxRequestConfig("dropbox/freeopt-cloud");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        return client;
    }
}
