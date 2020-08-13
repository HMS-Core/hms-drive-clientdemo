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

package com.huawei.cloud.drive.view.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.huawei.cloud.drive.hms.HmsProxyImpl;
import com.huawei.cloud.drive.log.Logger;
import com.huawei.cloud.drive.utils.ViewUtil;
import com.huawei.hicloud.drive.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WebViewActivity extends HiDiskBaseActivity {
    private static final String TAG = "WebViewActivity";

    private WebView mWebView;

    private ValueCallback<Uri[]> uriCallbacks;

    private final static int FILE_CHOOSER_CODE_ALBUM = 1;

    private final static int FILE_CHOOSER_CODE_CAMERA = 2;

    private Uri mCameraUri;

    private Activity mActivity;

    // This is the camera access
    private static String[] PERMISSIONS_CAMERA = {Manifest.permission.CAMERA};

    // Request ID
    private static final int CODE_REQUEST_DOC_PERMISSIONS_CAMERA = 5002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "onCreate");
        initView();
    }

    private void initView() {
        try {
            setContentView(R.layout.docskit_activity_web_veiw);
            mWebView = ViewUtil.findViewById(this, R.id.external_webview);
            final Intent intent = new Intent(getIntent());
            String url = intent.getStringExtra("url");
            // Check the validity of the URL.
            if (isUrlInValid(url)) {
                Logger.e(TAG, "url is invalid");
                finish();
                return;
            }
            // Setting Web Parameters
            initWebViewSettings();
            mWebView.loadUrl(url);
        } catch (Exception e) {
            // The system webView is not installed or is disabled.
            Logger.e(TAG, "initView error: " + e.getMessage());
            finish();
        }
    }

    private boolean isUrlInValid(String url) {
        if (TextUtils.isEmpty(url)) {
            Logger.e(TAG, "url is empty");
            return true;
        }
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        // check whether HTTPS or HTTP is used.
        if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
            Logger.e(TAG, "check url illegal scheme:" + scheme);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE_REQUEST_DOC_PERMISSIONS_CAMERA: {
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Check whether the permission is obtained. If the permission is obtained, the external storage is open and a toast message is displayed
                    String sdCard = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(sdCard) && mActivity != null) {
                        doTakePhoto(mActivity);
                    } else {
                        Logger.w(TAG, "onRequestPermissionsResult: SD card is not mounted normally.");
                    }
                } else {
                    if (uriCallbacks != null) {
                        uriCallbacks.onReceiveValue(null);
                        uriCallbacks = null;
                    }
                    Logger.w(TAG, "onRequestPermissionsResult: no camera permission.");
                }
            }
            default:
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        onResultAboveLollipop(requestCode, resultCode, intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onResultAboveLollipop(int requestCode, int resultCode, Intent intent) {
        if (uriCallbacks == null) {
            return;
        }
        // check result code
        if (resultCode != Activity.RESULT_OK) {
            uriCallbacks.onReceiveValue(null);
            uriCallbacks = null;
            return;
        }
        // check request code
        if (requestCode == FILE_CHOOSER_CODE_ALBUM) {
            Intent safeIntent = new Intent(intent);
            Uri albumUri = safeIntent.getData();
            if (albumUri != null) {
                uriCallbacks.onReceiveValue(new Uri[] {albumUri});
            } else {
                uriCallbacks.onReceiveValue(null);
            }
        } else if (requestCode == FILE_CHOOSER_CODE_CAMERA) {
            if (mCameraUri != null) {
                uriCallbacks.onReceiveValue(new Uri[] {mCameraUri});
            } else {
                uriCallbacks.onReceiveValue(null);
            }
        }
        uriCallbacks = null;
    }

    private class ChromeClient extends WebChromeClient {
        private WeakReference<Activity> mActivity;

        ChromeClient(WeakReference<Activity> activity) {
            mActivity = activity;
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            Logger.d(TAG, "onShowFileChooser");
            uriCallbacks = filePathCallback;
            Activity activity = mActivity.get();
            if (activity == null) {
                return false;
            }
            showChoseDialog(activity);
            return true;
        }
    }

    public void showChoseDialog(final Activity activity) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setOnCancelListener(new DialogOnCancelListener());
        String[] options = {getString(R.string.menu_open_camera), getString(R.string.menu_album)};
        final int MenuCameraIndex = 0;
        final int menuAlbumIndex = 1;
        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == MenuCameraIndex) {
                    mActivity = activity;
                    try {
                        if (!requestDocCameraPermission(activity)) {
                            Logger.e(TAG, "The user has forbidden to use the camera " + "to take permission or failed to apply for the camera to take permission!");
                        } else {
                            doTakePhoto(activity);
                        }
                    } catch (Exception e) {
                        Logger.e(TAG, "onShowFileChooser camera error: " + e.toString());
                    }
                } else if (which == menuAlbumIndex) {
                    try {
                        Intent albumIntent = new Intent(Intent.ACTION_PICK);
                        albumIntent.setType("image/*");
                        activity.startActivityForResult(Intent.createChooser(albumIntent, null), FILE_CHOOSER_CODE_ALBUM);
                    } catch (Exception e) {
                        Logger.e(TAG, "onShowFileChooser album error: " + e.toString());
                    }
                }
            }
        });
        alertDialog.show();
    }

    private class DialogOnCancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            if (uriCallbacks != null) {
                uriCallbacks.onReceiveValue(null);
                uriCallbacks = null;
            }
        }
    }

    private void initWebViewSettings() {
        WebSettings webSettings = mWebView.getSettings();
        if (webSettings == null) {
            Logger.e(TAG, "WebSettings is null.");
            finish();
            return;
        }
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        mWebView.setWebChromeClient(new ChromeClient(new WeakReference<Activity>(this)));
        mWebView.addJavascriptInterface(new WebExternal(), "splash");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null || isUrlInValid(url)) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Logger.e(TAG, "onReceivedSslError: error");
                handler.cancel();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });
    }

    private void doTakePhoto(Activity activity) {
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String filename = timeStampFormat.format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        mCameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
        activity.startActivityForResult(cameraIntent, FILE_CHOOSER_CODE_CAMERA);
    }

    private boolean requestDocCameraPermission(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int cameraPermission = activity.checkSelfPermission(Manifest.permission.CAMERA);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_CAMERA, CODE_REQUEST_DOC_PERMISSIONS_CAMERA);
                return false;
            }
        }
        return true;
    }
    private class WebExternal {
        @JavascriptInterface
        public String WPS_GetToken() {
            JSONObject jsonObject = new JSONObject();
            try {
                // 将token传给在线预览界面
                jsonObject.put("token", HmsProxyImpl.getInstance().getAccessToken());
            } catch (JSONException e) {
                Logger.e(TAG, "access error");
            }
            return jsonObject.toString();
        }
    }
}
