/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.cloud.drive.hms;

import android.app.Activity;
import android.text.TextUtils;

import com.huawei.cloud.drive.log.Logger;
import com.huawei.cloud.services.drive.DriveScopes;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * HmsProxyImpl encapsulates the entry class. Provides a encapsulation of the HMS SDK functionality that enables developers to focus more on business processing.
 */
public class HmsProxyImpl{
    private static String TAG = "HmsProxyImpl";

    // Get AT Lock
    private ReentrantLock getATLock = new ReentrantLock();

    // Login successful result
    public static final int REQUEST_SIGN_IN_LOGIN = 1002;

    // HuaweiIdAuthService  object
    private HuaweiIdAuthService service;

    // HuaweiIdAuthParams  object
    private HuaweiIdAuthParams authParams;

    // HmsProxyImpl instance
    private static HmsProxyImpl instance = new HmsProxyImpl();

    // AccessToken
    private String accessToken;

    // UnionId
    private String unionId;

    // DeviceId
    private String deviceId;

    // DisplayName
    private String displayName;

    // Status
    private int status;

    // Gender
    private int gender;

    // GrantedScopes
    private Set grantedScopes;

    // ServiceCountryCode
    private String serviceCountryCode;

    private HmsProxyImpl() {
        authParams = initData();
    }

    /**
     * Get a HmsProxyImpl instance
     */
    public static HmsProxyImpl getInstance() {
        return instance;
    }

    /**
     * Initialize and return the HuaweiIdSignInOptions object
     */
    private HuaweiIdAuthParams initData() {
        List<Scope> scopeList = new LinkedList<>();
        scopeList.add(HuaweiIdAuthAPIManager.HUAWEIID_BASE_SCOPE);
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_FILE));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_METADATA));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_METADATA_READONLY));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_READONLY));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_APPDATA));

        HuaweiIdAuthParams params = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAccessToken().setIdToken().setScopeList(scopeList).createParams();

        return params;
    }

    /**
     * Huawei Api Client authorized login method
     *
     * @param activity Call the Activity page handle of the singIn interface
     */
    public void singIn(Activity activity) {
        if (authParams == null) {
            authParams = initData();
        }
        service = HuaweiIdAuthManager.getService(activity, authParams);
        activity.startActivityForResult(service.getSignInIntent(), REQUEST_SIGN_IN_LOGIN);
    }

    /**
     * Synchronously acquire access token, must be called in non-main thread
     *
     * @return accessToken or {@code null}
     */
    public String refreshAccessToken() {
        Logger.i(TAG, "refreshAccessToken begin");
        try {
            if (service != null) {
                getATLock.lock();
                try {
                    getAT();
                } finally {
                    getATLock.unlock();
                }
                Logger.d(TAG, "refreshAccessToken return new");
            } else {
                Logger.e(TAG, "refreshAccessToken client is null, return null");
            }

        } catch (Exception e) {
            Logger.e(TAG, "refreshAccessToken exception, return null");
        }

        Logger.i(TAG, "refreshAccessToken end");
        return accessToken;
    }

    /**
     * Get accessToken
     */
    private void getAT() {
        for (int retry = 0; retry < 2; retry++) {
            Logger.i(TAG, "signInBackend times: " + retry);
            if (signInBackend()) {
                break;
            }
        }
    }

    /**
     * Sign in Background
     */
    private boolean signInBackend(){
        Logger.i(TAG, "signInBackend");
        clearAccountInfo();

        if (service == null) {
            return  false;
        }

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Task<AuthHuaweiId> task = service.silentSignIn();
        task.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId authHuaweiId) {
                Logger.i(TAG, "silentSignIn success");
                dealSignInResult(authHuaweiId);
                countDownLatch.countDown();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Logger.i(TAG, "silentSignIn error");
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await(15, TimeUnit.SECONDS);
        }catch (InterruptedException e) {
            Logger.i(TAG, "signInBackend catch InterruptedException");
            countDownLatch.countDown();
        }

        if (TextUtils.isEmpty(getAccessToken())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Process certification results
     */
    public void dealSignInResult(AuthHuaweiId  huaweiAccount){
        String tempAt = huaweiAccount.getAccessToken();
        if (null == tempAt || tempAt.isEmpty()) {
            Logger.e(TAG , "dealSignInResult get accessToken is null.");
            return;
        }

        Logger.i(TAG, "dealSignInResult signInBackend get new AT successfully");
        saveAccountInfo(huaweiAccount);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStatus() {
        return status;
    }

    public int getGender() {
        return gender;
    }

    public Set getGrantedScopes() {
        return grantedScopes;
    }

    public String getServiceCountryCode() {
        return serviceCountryCode;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    /**
     * Save account info
     */
    private void saveAccountInfo(AuthHuaweiId signInHuaweiId) {
        if (signInHuaweiId == null) {
            return;
        }
        unionId = signInHuaweiId.getUnionId();
        deviceId = signInHuaweiId.getOpenId();
        displayName = signInHuaweiId.getDisplayName();
        status = signInHuaweiId.getStatus();
        gender = signInHuaweiId.getGender();
        grantedScopes = signInHuaweiId.getAuthorizedScopes();
        serviceCountryCode = signInHuaweiId.getServiceCountryCode();
        accessToken = signInHuaweiId.getAccessToken();
    }

    /**
     * Clear account info
     */
    private void clearAccountInfo() {
        unionId = null;
        deviceId = null;
        displayName = null;
        status = 0;
        gender = 0;
        grantedScopes = null;
        serviceCountryCode = null;
        accessToken = null;
    }
}
