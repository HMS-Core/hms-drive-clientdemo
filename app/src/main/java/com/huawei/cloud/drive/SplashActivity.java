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

package com.huawei.cloud.drive;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.cloud.base.auth.DriveCredential;
import com.huawei.cloud.client.exception.DriveCode;
import com.huawei.cloud.drive.hms.CredentialManager;
import com.huawei.cloud.drive.hms.HmsProxyImpl;
import com.huawei.cloud.drive.log.Logger;
import com.huawei.cloud.drive.task.task.DriveTask;
import com.huawei.cloud.drive.task.task.TaskManager;
import com.huawei.hicloud.drive.R;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

/**
 * Splash Activity
 */
public class SplashActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "SplashActivity";

    // This is the permission to apply
    private static String[] PERMISSIONS_CAMERA_AND_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    /**
     * Refresh acccess token method for Drive sdk
     */
    private static DriveCredential.AccessMethod refreshAT = new DriveCredential.AccessMethod() {
        @Override
        public String refreshToken() {
            return HmsProxyImpl.getInstance().refreshAccessToken();
        }
    };

    /**
     * Solve the problem that Android 6.0 and above cannot read external storage permissions
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int storagePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = activity.checkSelfPermission(Manifest.permission.CAMERA);
            // Check if there is permission, if you do not have permission, you need to apply
            if (storagePermission != PackageManager.PERMISSION_GRANTED ||
                    cameraPermission != PackageManager.PERMISSION_GRANTED) {
                // request for access
                activity.requestPermissions(PERMISSIONS_CAMERA_AND_STORAGE, requestCode);
                // Returns false. Description no authorization
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Verify that the permission is obtained. If the permission is obtained,
                    // the external storage will be open and a toast prompt will pop up to obtain authorization.
                    String sdCard = Environment.getExternalStorageState();
                    if (sdCard.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(this, "Permissions Access", Toast.LENGTH_LONG).show();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SplashActivity.this, "Permissions Deny", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView infoView = findViewById(R.id.splash_info);
        infoView.setText(getString(R.string.str_copyright));
        if (!isGrantExternalRW(this, 1)) {
            Log.i(TAG, "Permissions Deny");
        }

        // Call singIn method
        HmsProxyImpl.getInstance().singIn(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Logger.i(TAG, "onActivityResult, requestCode = " + requestCode + ", resultCode = " + resultCode);
        try {
            // Handle HMS SDK authentication sign in callback results
            if (requestCode == HmsProxyImpl.REQUEST_SIGN_IN_LOGIN) {
                // login success, get user message by getSignedInAccountFromIntent
                Task<AuthHuaweiId> authHuaweiIdTask  = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
                if (authHuaweiIdTask.isSuccessful()) {
                    AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                    Logger.i(TAG, "onActivityResult, signIn success " + huaweiAccount.getDisplayName());
                    HmsProxyImpl.getInstance().dealSignInResult(huaweiAccount);
                    initDrive();
                } else {
                    // login failed
                    Logger.i(TAG, "onActivityResult, signIn failed: " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
                    Toast.makeText(getApplicationContext(), "onActivityResult, signIn failed.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Logger.i(TAG, "onActivityResult, catch Exception");
        }
    }

    /**
     * Init Drive
     */
    private void initDrive() {
        final String unionID = HmsProxyImpl.getInstance().getUnionId();
        final String at = HmsProxyImpl.getInstance().getAccessToken();
        if (TextUtils.isEmpty(unionID) || TextUtils.isEmpty(at)) {
            Logger.e(TAG, "initDrive error, unionID or at is null: " + "unionID:" + unionID + " at " + at);
            return;
        }

        // CredentialManager.getInstance().init must be called in a thread, and need to handle the results returned by the Drive SDK initialization
        TaskManager.getInstance().execute(new DriveTask() {
            @Override
            public void call() {
                int returnCode =  CredentialManager.getInstance().init(unionID, at, refreshAT);
                if (DriveCode.SUCCESS == returnCode) {
                    // Jump to the app home page after successful initialization.
                    jumpToMainActivity();
                }else if (DriveCode.SERVICE_URL_NOT_ENABLED == returnCode){
                    Toast.makeText(getApplicationContext(), "drive is not enabled", Toast.LENGTH_LONG).show();
                } else{
                    Toast.makeText(getApplicationContext(), "drive init error", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Jump to MainActivity
     */
    private void jumpToMainActivity() {
        try {
            Logger.i(TAG, "signIn OK, jump to MainActivity");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (Exception e) {
            Logger.e(TAG, "jumpToMainActivity exception:" + e.getMessage());
        }
    }
}
