/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.cloud.drive.hms;

import android.content.Context;

import com.huawei.cloud.base.auth.DriveCredential;
import com.huawei.cloud.base.util.StringUtils;
import com.huawei.cloud.client.exception.DriveCode;

/**
 * Credential Manager
 * <p>
 * since 1.0
 */
public class CredentialManager {

    /**
     * Drive Credential Info
     */
    private DriveCredential mCredential;

    private CredentialManager() {
    }

    private static class InnerHolder {
        private static CredentialManager sInstance = new CredentialManager();
    }

    /**
     * Singleton CredentialManager instance
     *
     * @return CredentialManager
     */
    public static CredentialManager getInstance() {
        return InnerHolder.sInstance;
    }

    /**
     * init the drive sdk witch application Context and HwId Account info(uid,countryCode,at),
     * register a AccessMethod to get a new accessToken while accessToken is expired.
     *
     * @param unionID   unionID from HwID
     * @param at        access token
     * @param refreshAT a callback to refresh AT
     */
    public int init(String unionID, String at, DriveCredential.AccessMethod refreshAT) {
        if (StringUtils.isNullOrEmpty(unionID) || StringUtils.isNullOrEmpty(at)) {
            return DriveCode.ERROR;
        }
        DriveCredential.Builder builder = new DriveCredential.Builder(unionID, refreshAT);
        mCredential = builder.build().setAccessToken(at);
        return DriveCode.SUCCESS;
    }

    /**
     * get the DriveCredential
     *
     * @return DriveCredential
     */
    public DriveCredential getCredential() {
        return mCredential;
    }

    /**
     * Exit the Drive and clear all cache information during Drive usage.
     */
    public void exit(Context context) {
        // clear cache
        deleteFile(context.getCacheDir());
        deleteFile(context.getFilesDir());
    }

    /**
     * clear cache
     *
     * @param file Specified cache file
     */
    private static void deleteFile(java.io.File file) {
        if (null == file || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            java.io.File[] files = file.listFiles();
            if (files != null) {
                for (java.io.File f : files) {
                    deleteFile(f);
                }
            }
        }
    }
}
