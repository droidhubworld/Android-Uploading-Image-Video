package com.inventivestack.uploadfiles;

/**
 * Created by akumar1 on 12/1/2017.
 */

public interface UploadProgressListener {
    /**
     * This method updated how much data size uploaded to server
     * @param num
     */
    void transferred(long num);
}
