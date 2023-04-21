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

package com.huawei.cloud.drive.fragment;

import static com.huawei.cloud.drive.constants.MimeType.mimeType;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.huawei.cloud.base.http.FileContent;
import com.huawei.cloud.base.media.MediaHttpDownloader;
import com.huawei.cloud.base.media.MediaHttpDownloaderProgressListener;
import com.huawei.cloud.base.util.DateTime;
import com.huawei.cloud.base.util.StringUtils;
import com.huawei.cloud.base.util.base64.Base64;
import com.huawei.cloud.drive.view.activity.WebViewActivity;
import com.huawei.cloud.services.drive.Drive;
import com.huawei.cloud.services.drive.model.About;
import com.huawei.cloud.services.drive.model.Change;
import com.huawei.cloud.services.drive.model.ChangeList;
import com.huawei.cloud.services.drive.model.Channel;
import com.huawei.cloud.services.drive.model.Comment;
import com.huawei.cloud.services.drive.model.CommentList;
import com.huawei.cloud.services.drive.model.File;
import com.huawei.cloud.services.drive.model.FileList;
import com.huawei.cloud.services.drive.model.HistoryVersion;
import com.huawei.cloud.services.drive.model.HistoryVersionList;
import com.huawei.cloud.services.drive.model.Reply;
import com.huawei.cloud.services.drive.model.ReplyList;
import com.huawei.cloud.services.drive.model.StartCursor;

import com.huawei.cloud.drive.hms.CredentialManager;
import com.huawei.cloud.drive.utils.thumbnail.ThumbnailUtilsImage;
import com.huawei.cloud.drive.hms.HmsProxyImpl;
import com.huawei.cloud.drive.log.Logger;
import com.huawei.cloud.drive.task.task.DriveTask;
import com.huawei.cloud.drive.task.task.TaskManager;
import com.huawei.cloud.drive.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface Fragment, test for Drive interfaces
 */
public class InterfaceFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "InterfaceFragment";

    private static final String FILENAME = "IMG_20190712_155412.jpg";

    private static final String DOCXFILE = "test.docx";

    /**
     * 在线预览/编辑文档的时候，退出webview界面，刷新文件列表界面
     */
    public static final int WEB_VIEW_BACK_REFRESH = 5201;

    private static final long DIRECT_UPLOAD_MAX_SIZE = 20 * 1024 * 1024;

    private static final long DIRECT_DOWNLOAD_MAX_SIZE = 20 * 1024 * 1024;

    // Successful result
    private static final int SUCCESS = 0;

    // Failure result
    private static final int FAIL = 1;

    // Margin space
    private static final int ZOOM_OUT = 30;

    // Main view
    private View mView;

    // Context
    private Context context;

    private HistoryVersion mHistoryVersion;

    private HistoryVersion deleteHistoryVersions;

    // Used to cache metadata information after the folder is created successfully.
    private File mDirectory;

    // Used to cache metadata information after successful file creation
    private File mFile;

    private File mBackupFile;

    // Used to cache metadata information after the Comment is created successfully.
    private Comment mComment;

    //  Used to cache metadata information after the Reply is created successfully.
    private Reply mReply;

    // Used to cache channel token
    private String watchListPageToken;

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.recent_fragment, container, false);
        context = getContext();
        setHasOptionsMenu(true);
        initView();
        prepareTestFile();
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.drive_about_button_get) {
            executeAboutGet();
        } else if (v.getId() == R.id.drive_files_button_list) {
            executeFilesList();
        } else if (v.getId() == R.id.drive_files_button_create) {
            executeFilesCreate();
        } else if (v.getId() == R.id.drive_files_button_update) {
            executeFilesUpdate();
        } else if (v.getId() == R.id.drive_files_button_createfile) {
            executeFilesCreateFile();
        } else if (v.getId() == R.id.drive_files_button_get) {
            executeFilesGet();
        } else if (v.getId() == R.id.drive_files_button_copy) {
            executeFilesCopy();
        } else {
            onViewClick(v.getId());
        }
    }

    /**
     * click method
     *
     * @param id resource id
     */
    private void onViewClick(int id) {
        if (id == R.id.drive_files_button_delete) {
            executeFilesDelete();
        } else if (id == R.id.drive_files_button_emptyRecycle) {
            executeFilesEmptyRecycle();
        } else if (id == R.id.drive_files_subscribe_button) {
            executeFilesSubscribe();
        } else if (id == R.id.drive_changes_subscribe_button) {
            executeChangesSubscribe();
        } else if (id == R.id.drive_changes_getstartcursor_button) {
            executeChangesGetStartCursor();
        } else if (id == R.id.drive_channels_stop) {
            executeChannelsStop();
        } else if (id == R.id.drive_changes_list_button) {
            executeChangesList();
        } else {
            onOtherClick(id);
        }
    }

    /**
     * click method
     *
     * @param id resource id
     */
    private void onOtherClick(int id) {
        if (id == R.id.drive_files_update_content_button) {
            executeFilesUpdateContent();
        } else if (id == R.id.drive_replies_create) {
            executeRepliesCreate();
        } else if (id == R.id.drive_replies_list) {
            executeRepliesList();
        } else if (id == R.id.drive_replies_get) {
            executeRepliesGet();
        } else if (id == R.id.drive_replies_update) {
            executeRepliesUpdate();
        } else if (id == R.id.drive_replies_delete) {
            executeRepliesDelete();
        } else if (id == R.id.drive_comments_create) {
            executeCommentsCreate();
        } else if (id == R.id.drive_comments_list) {
            executeCommentsList();
        } else if (id == R.id.drive_comments_get) {
            executeCommentsGet();
        } else if (id == R.id.drive_comments_update) {
            executeCommentsUpdate();
        } else if (id == R.id.drive_comments_delete) {
            executeCommentsDelete();
        } else if (id == R.id.drive_historyversions_delete) {
            executeHistoryVersionsDelete();
        } else if (id == R.id.drive_historyversions_get) {
            executeHistoryVersionsGet();
        } else if (id == R.id.drive_historyversions_list) {
            executeHistoryVersionsList();
        } else if (id == R.id.drive_historyversions_update) {
            executeHistoryVersionsUpdate();
        } else if (id == R.id.drive_backup_btn) {
            executeDataBackUp();
        } else if (id == R.id.drive_restore_btn) {
            executeDataRestore();
        } else if (id == R.id.drive_online_open_btn) {
            executeOnlineOpen();
        }
    }

    /**
     * copy image to cache directory
     */
    private void prepareTestFile() {
        try {
            InputStream in = context.getAssets().open(FILENAME);
            String cachePath = context.getExternalCacheDir().getAbsolutePath();
            FileOutputStream outputStream = new FileOutputStream(new java.io.File(cachePath + "/cache.jpg"));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            in.close();

            in = context.getAssets().open(DOCXFILE);
            outputStream = new FileOutputStream(new java.io.File(cachePath + "/test.docx"));
            byte[] buf = new byte[1024];
            while ((byteCount = in.read(buf)) != -1) {
                outputStream.write(buf, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            in.close();

            String BackFileName = "AppDataBackUpFileName.jpg";
            in = context.getAssets().open(FILENAME);
            outputStream = new FileOutputStream(new java.io.File("/sdcard/" + BackFileName));
            while ((byteCount = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            in.close();

            String accountFile = "account.json";
            in = context.getAssets().open(accountFile);
            outputStream = new FileOutputStream(new java.io.File(cachePath +"/"+ accountFile));
            while ((byteCount = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            in.close();
        } catch (IOException e) {
            Logger.e(TAG, "prepare file error, exception: " + e.toString());
            return;
        }
    }

    /**
     * Handle UI refresh message
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            updateButtonUi(msg.arg1, msg.what);
            showErrorToast(msg);
        }
    };

    /**
     * init UI view
     */
    private void initView() {
        if (mView == null) {
            return;
        }
        Button driveAboutButton = mView.findViewById(R.id.drive_about_button_get);
        driveAboutButton.setOnClickListener(this);

        Button deleteButton = mView.findViewById(R.id.drive_files_button_delete);
        deleteButton.setOnClickListener(this);

        Button emptyRecycleButton = mView.findViewById(R.id.drive_files_button_emptyRecycle);
        emptyRecycleButton.setOnClickListener(this);

        Button copyButton = mView.findViewById(R.id.drive_files_button_copy);
        copyButton.setOnClickListener(this);

        Button generateIdButton = mView.findViewById(R.id.drive_files_button_createfile);
        generateIdButton.setOnClickListener(this);

        Button getButton = mView.findViewById(R.id.drive_files_button_get);
        getButton.setOnClickListener(this);

        Button listButton = mView.findViewById(R.id.drive_files_button_list);
        listButton.setOnClickListener(this);

        Button subscribeButton = mView.findViewById(R.id.drive_files_subscribe_button);
        subscribeButton.setOnClickListener(this);

        Button changesButton = mView.findViewById(R.id.drive_changes_subscribe_button);
        changesButton.setOnClickListener(this);

        Button getCursorButton = mView.findViewById(R.id.drive_changes_getstartcursor_button);
        getCursorButton.setOnClickListener(this);

        Button channelsStopButton = mView.findViewById(R.id.drive_channels_stop);
        channelsStopButton.setOnClickListener(this);

        Button changesListButton = mView.findViewById(R.id.drive_changes_list_button);
        changesListButton.setOnClickListener(this);

        Button createButton = mView.findViewById(R.id.drive_files_button_create);
        createButton.setOnClickListener(this);

        Button updateButton = mView.findViewById(R.id.drive_files_button_update);
        updateButton.setOnClickListener(this);

        Button updateContentButton = mView.findViewById(R.id.drive_files_update_content_button);
        updateContentButton.setOnClickListener(this);

        Button repliesCreateButton = mView.findViewById(R.id.drive_replies_create);
        repliesCreateButton.setOnClickListener(this);

        Button repliesListButton = mView.findViewById(R.id.drive_replies_list);
        repliesListButton.setOnClickListener(this);

        Button repliesGetButton = mView.findViewById(R.id.drive_replies_get);
        repliesGetButton.setOnClickListener(this);

        Button repliesUpdateButton = mView.findViewById(R.id.drive_replies_update);
        repliesUpdateButton.setOnClickListener(this);

        Button repliesDeleteButton = mView.findViewById(R.id.drive_replies_delete);
        repliesDeleteButton.setOnClickListener(this);

        Button commentsCreateButton = mView.findViewById(R.id.drive_comments_create);
        commentsCreateButton.setOnClickListener(this);

        Button commentsListButton = mView.findViewById(R.id.drive_comments_list);
        commentsListButton.setOnClickListener(this);

        Button commentsGetButton = mView.findViewById(R.id.drive_comments_get);
        commentsGetButton.setOnClickListener(this);

        Button commentsUpdateButton = mView.findViewById(R.id.drive_comments_update);
        commentsUpdateButton.setOnClickListener(this);

        Button commentsDeleteButton = mView.findViewById(R.id.drive_comments_delete);
        commentsDeleteButton.setOnClickListener(this);

        Button historyVersionsDeleteButton = mView.findViewById(R.id.drive_historyversions_delete);
        historyVersionsDeleteButton.setOnClickListener(this);

        Button historyVersionsGetButton = mView.findViewById(R.id.drive_historyversions_get);
        historyVersionsGetButton.setOnClickListener(this);

        Button historyVersionsListButton = mView.findViewById(R.id.drive_historyversions_list);
        historyVersionsListButton.setOnClickListener(this);

        Button historyVersionsUpdateButton = mView.findViewById(R.id.drive_historyversions_update);
        historyVersionsUpdateButton.setOnClickListener(this);

        Button backUpButton = mView.findViewById(R.id.drive_backup_btn);
        backUpButton.setOnClickListener(this);

        Button restoreButton = mView.findViewById(R.id.drive_restore_btn);
        restoreButton.setOnClickListener(this);

        Button onlineOpenButton = mView.findViewById(R.id.drive_online_open_btn);
        onlineOpenButton.setOnClickListener(this);
    }

    private void showErrorToast(Message msg) {
        if (msg.what == SUCCESS || msg.obj == null) {
            return;
        }
        Toast.makeText(getActivity(), msg.obj.toString(), Toast.LENGTH_LONG).show();
    }

    /**
     * Update button background color and right icon
     *
     * @param buttonId
     * @param code
     */
    private synchronized void updateButtonUi(int buttonId, int code) {
        if (mView == null || getActivity() == null) {
            return;
        }
        Button button = mView.findViewById(buttonId);
        Drawable drawable = null;
        Resources resources = getActivity().getResources();
        if (code == SUCCESS) {
            drawable = getActivity().getDrawable(R.mipmap.ic_success);
            if (resources != null && button != null) {
                button.setBackground(getActivity().getDrawable(R.drawable.button_circle_shape_green));
            }
        } else if (code == FAIL) {
            if (resources != null && button != null) {
                button.setBackground(getActivity().getDrawable(R.drawable.button_circle_shape_red));
            }
            drawable = getActivity().getDrawable(R.mipmap.ic_fail);
        } else {
            Logger.i(TAG, "invalid result code");
        }
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getMinimumWidth() - ZOOM_OUT, drawable.getMinimumHeight() - ZOOM_OUT);
            if (button != null) {
                button.setCompoundDrawables(null, null, drawable, null);
            }
        }
    }

    /**
     * Build a Drive instance
     */
    private Drive buildDrive() {
        Drive service = new Drive.Builder(CredentialManager.getInstance().getCredential(), context).build();
        return service;
    }

    /**
     * Execute the About.get interface test task
     */
    private void executeAboutGet() {
        TaskManager.getInstance().execute(new AboutGetTask());
    }

    /**
     * The About.get interface test task
     */
    private class AboutGetTask extends DriveTask {
        @Override
        public void call() {
            doAbout();
        }
    }

    /**
     * Test the About.get interface
     */
    private void doAbout() {
        try {
            Drive drive = buildDrive();
            Drive.About about = drive.about();
            About response = about.get().set("fields", "*").execute();
            checkUpdateProtocol(response);
            sendHandleMessage(R.id.drive_about_button_get, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_about_button_get, FAIL);
            Logger.e(TAG, "getAboutInfo error: " + e.toString());
        }
    }

    /**
     * Determine if you want to pop up the update page
     *
     * @param about Returned response
     */
    private void checkUpdateProtocol(About about) {
        if (about == null) {
            return;
        }
        Log.d(TAG, "checkUpdate: " + about.toString());

        Object updateValue = about.get("needUpdate");
        boolean isNeedUpdate = false;
        if (updateValue instanceof Boolean) {
            isNeedUpdate = (Boolean) updateValue;
        }
        if (!isNeedUpdate) {
            return;
        }

        Object urlValue = about.get("updateUrl");
        String url = "";
        if (urlValue instanceof String) {
            url = (String) urlValue;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Uri uri = Uri.parse(url);
        if (!"https".equals(uri.getScheme())) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Logger.e(TAG, "Activity Not found");
        }
    }

    private void sendHandleMessage(int buttonId, int result) {
        sendHandleMessage(buttonId, result, null);
    }

    /**
     * Update the button style based on the returned result
     *
     * @param buttonId button id
     * @param result Interface test result 0 success 1 failure
     */
    private void sendHandleMessage(int buttonId, int result, String msg) {
        Message message = handler.obtainMessage();
        message.arg1 = buttonId;
        message.what = result;
        message.obj = msg;
        handler.sendMessage(message);
    }

    /**
     * Execute the Files.list interface test task
     */
    private void executeFilesList() {
        TaskManager.getInstance().execute(new FilesListTask());
    }

    /**
     * The Files.list interface test task
     */
    private class FilesListTask extends DriveTask {
        @Override
        public void call() {
            doFilesList();
        }
    }

    /**
     * Test the Files.list interface
     */
    private void doFilesList() {
        try {
            List<File> folders = getFileList("mimeType = 'application/vnd.huawei-apps.folder'", "fileName", 10, "*");
            Logger.i(TAG, "executeFilesList: directory size =  " + folders.size());
            if (folders.isEmpty()) {
                sendHandleMessage(R.id.drive_files_button_list, SUCCESS);
                return;
            }
            // get child files of a folder
            String directoryId = folders.get(0).getId();
            String queryStr = "'" + directoryId + "' in parentFolder and mimeType != 'application/vnd.huawei-apps.folder'";
            List<File> files = getFileList(queryStr, "fileName", 10, "*");
            Logger.i(TAG, "executeFilesList: files size = " + files.size());
            sendHandleMessage(R.id.drive_files_button_list, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_button_list, FAIL);
            Logger.e(TAG, "executeFilesList exception: " + e.toString());
        }
    }

    /**
     * Traverse to get all files
     *
     * @param query Query conditions
     * @param orderBy Sort conditions
     * @param pageSize page Size
     * @param fields fields
     */
    private List<File> getFileList(String query, String orderBy, int pageSize, String fields) throws IOException {

        Drive drive = buildDrive();
        Drive.Files.List request = drive.files().list();
        String pageToken = null;
        List<File> fileList = new ArrayList<>();
        do {
            FileList result = request.setQueryParam(query).setOrderBy(orderBy).setPageSize(pageSize).setFields(fields).execute();
            for (File file : result.getFiles()) {
                fileList.add(file);
            }
            pageToken = result.getNextCursor();
            request.setCursor(pageToken);
        } while (!StringUtils.isNullOrEmpty(pageToken));
        Logger.i(TAG, "getFileList: get files counts = " + fileList.size());
        return fileList;
    }

    /**
     * Get parent dir for copy files
     *
     * @param fileList files list
     * @return file ID of parent dir
     */
    private ArrayList<String> getParentsId(FileList fileList) {
        if (fileList == null) {
            return null;
        }
        List<File> files = fileList.getFiles();
        if (files == null || files.size() <= 0) {
            return null;
        }
        int size = files.size();
        File file = files.get(size - 1);
        if (file == null) {
            return null;
        }
        // get the first one for test
        String parentDir = file.getParentFolder().get(0);
        ArrayList<String> list = new ArrayList<>();
        list.add(parentDir);
        return list;
    }

    /**
     * Execute the Files.create interface test task
     */
    private void executeFilesCreate() {
        TaskManager.getInstance().execute(new FilesCreateTask());
    }

    /**
     * Execute the Files.create interface test task
     */
    private class FilesCreateTask extends DriveTask {

        @Override
        public void call() {
            mDirectory = createDirectory();
        }
    }

    /**
     * Create a directory
     */
    private File createDirectory() {
        try {
            Drive drive = buildDrive();
            Map<String, String> appProperties = new HashMap<>();
            appProperties.put("appProperties", "property");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
            String dirName = formatter.format(new Date());
            Logger.i(TAG, "executeFilesCreate: " + dirName);

            File file = new File();
            file.setFileName(dirName).setAppSettings(appProperties).setMimeType("application/vnd.huawei-apps.folder");
            File directory = drive.files().create(file).execute();
            sendHandleMessage(R.id.drive_files_button_create, SUCCESS);
            return directory;
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_files_button_create, FAIL);
            Logger.e(TAG, "createDirectory error: " + e.toString());
            return null;
        }
    }

    /**
     * Execute the Files.update interface test task
     */
    private void executeFilesUpdate() {
        TaskManager.getInstance().execute(new FilesUpdateTask());
    }

    /**
     * The Files.create interface test task
     */
    private class FilesUpdateTask extends DriveTask {

        @Override
        public void call() {
            updateFile(mDirectory);
        }
    }

    /**
     * Modify the file (directory) metaData, distinguish whether it is a file or a directory by MIMEType
     *
     * @param file File to be modified (directory)
     */
    private void updateFile(File file) {
        try {
            if (file == null) {
                Logger.e(TAG, "updateFile error, need to create file.");
                sendHandleMessage(R.id.drive_files_button_update, FAIL);
                return;
            }

            Drive drive = buildDrive();
            File updateFile = new File();
            updateFile.setFileName(file.getFileName() + "_update").setMimeType("application/vnd.huawei-apps.folder").setDescription("update folder").setFavorite(true);
            file = drive.files().update(file.getId(), updateFile).execute();

            Logger.i(TAG, "updateFile result: " + file.toString());
            sendHandleMessage(R.id.drive_files_button_update, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_button_update, FAIL);
            Logger.e(TAG, "updateFile error: " + e.toString());
        }
    }

    /**
     * Execute the Files.create interface test task
     */
    private void executeFilesCreateFile() {
        TaskManager.getInstance().execute(new CreateFileTask());
    }

    /**
     * The Files.create interface test task
     */
    private class CreateFileTask extends DriveTask {

        @Override
        public void call() {
            String fileName = context.getExternalCacheDir().getAbsolutePath() + "/cache.jpg";
            byte[] thumbnailImageBuffer = getThumbnailImage(fileName);
            String type = mimeType(".jpg");
            if (mDirectory == null) {
                String errMsg = "create file error: need to create directory first.";
                Logger.e(TAG, errMsg);
                sendHandleMessage(R.id.drive_files_button_createfile, FAIL, errMsg);
                return;
            }
            createFile(fileName, mDirectory.getId(), thumbnailImageBuffer, type);
        }
    }

    /**
     * create a image file by Files.create interface.
     *
     * @param filePath Specifies the file to be uploaded.
     * @param parentId Specifies the directory ID for uploading files
     * @param thumbnailImageBuffer thumbnail Image Data
     * @param thumbnailMimeType image mime type
     */
    private void createFile(String filePath, String parentId, byte[] thumbnailImageBuffer, String thumbnailMimeType) {
        try {
            if (filePath == null) {
                sendHandleMessage(R.id.drive_files_button_createfile, FAIL);
                return;
            }

            java.io.File io = new java.io.File(filePath);
            FileContent fileContent = new FileContent(mimeType(io), io);

            // set thumbnail , If it is not a media file, you do not need a thumbnail.
            File.ContentExtras contentPlus = new File.ContentExtras();
            File.ContentExtras.Thumbnail thumbnail = new File.ContentExtras.Thumbnail();
            thumbnail.setContent(Base64.encodeBase64String(thumbnailImageBuffer));
            thumbnail.setMimeType(thumbnailMimeType);
            contentPlus.setThumbnail(thumbnail);

            File content = new File().setFileName(io.getName()).setMimeType(mimeType(io)).setParentFolder(Collections.singletonList(parentId)).setContentExtras(contentPlus);

            Drive drive = buildDrive();
            Drive.Files.Create rquest = drive.files().create(content, fileContent);
            // default: resume, If the file Size is less than 20M, use directly upload.
            boolean isDirectUpload = false;
            if (io.length() < DIRECT_UPLOAD_MAX_SIZE) {
                isDirectUpload = true;
            }
            rquest.getMediaHttpUploader().setDirectUploadEnabled(isDirectUpload);
            mFile = rquest.execute();

            Logger.i(TAG, "executeFilesCreateFile:" + mFile.toString());
            sendHandleMessage(R.id.drive_files_button_createfile, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_button_createfile, FAIL);
            Logger.e(TAG, "executeFilesCreateFile exception: " + e.toString());
        }
    }

    /**
     * Generate and obtain the base64 code of the thumbnail.
     *
     * @return base64 code of the thumbnail
     */
    private byte[] getThumbnailImage(String iamgeFileName) {
        //imagePath: path to store thumbnail image
        String imagePath = "/storage/emulated/0/DCIM/Camera/";
        ThumbnailUtilsImage.genImageThumbnail(iamgeFileName, imagePath + "imageThumbnail.jpg", 250, 150, 0);
        try (FileInputStream is = new FileInputStream(imagePath + "imageThumbnail.jpg")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            return buffer;
        } catch (IOException ex) {
            Logger.e(TAG, ex.getMessage());
            return null;
        }
    }

    /**
     * Execute the Files.get interface test task
     */
    private void executeFilesGet() {
        TaskManager.getInstance().execute(new FilesGetTask());
    }

    /**
     * The Files.get interface test task
     */
    private class FilesGetTask extends DriveTask {
        @Override
        public void call() {
            downLoadFile(mFile.getId());
        }
    }

    /**
     * Test Files.get interface
     *
     * @param fileId Specifies the file to be obtained.
     */
    private void downLoadFile(String fileId) {
        try {
            if (fileId == null) {
                Logger.e(TAG, "executeFilesGet error, need to create file.");
                sendHandleMessage(R.id.drive_files_button_get, FAIL);
                return;
            }
            String imagePath = "/storage/emulated/0/DCIM/Camera/";
            Drive drive = buildDrive();
            // Get File metaData
            Drive.Files.Get request = drive.files().get(fileId);
            request.setFields("id,size");
            File res = request.execute();
            // Download File
            long size = res.getSize();
            Drive.Files.Get get = drive.files().get(fileId);
            get.setForm("media");
            MediaHttpDownloader downloader = get.getMediaHttpDownloader();

            boolean isDirectDownload = false;
            if (size < DIRECT_DOWNLOAD_MAX_SIZE) {
                isDirectDownload = true;
            }
            downloader.setContentRange(0, size - 1).setDirectDownloadEnabled(isDirectDownload);
            downloader.setProgressListener(new MediaHttpDownloaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpDownloader mediaHttpDownloader) throws IOException {
                    // The download subthread invokes this method to process the download progress.
                    double progress = mediaHttpDownloader.getProgress();
                }
            });
            java.io.File f = new java.io.File(imagePath + "download.jpg");
            get.executeContentAndDownloadTo(new FileOutputStream(f));

            Logger.i(TAG, "executeFilesGetMedia success.");
            sendHandleMessage(R.id.drive_files_button_get, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_button_get, FAIL);
            Logger.e(TAG, "executeFilesGet exception: " + e.toString());
        }
    }

    /**
     * Execute the Files.copy interface test task
     */
    private void executeFilesCopy() {
        TaskManager.getInstance().execute(new FilesCopyTask());
    }

    /**
     * The Files.copy interface test task
     */
    private class FilesCopyTask extends DriveTask {

        @Override
        public void call() {
            try {
                Drive drive = buildDrive();
                Drive.Files.List fileListReq = drive.files().list();
                fileListReq.setQueryParam("mimeType = 'application/vnd.huawei-apps.folder'").setOrderBy("name").setPageSize(100).setFields("*");
                FileList fileList = fileListReq.execute();
                ArrayList<String> dstDir = getParentsId(fileList);
                Logger.e(TAG, "copyFile Source File Sharded Status: " + mFile.getHasShared());
                copyFile(mFile, dstDir);
            } catch (IOException e) {
                Logger.e(TAG, "copyFile -- list file error: " + e.toString());
                sendHandleMessage(R.id.drive_files_button_copy, FAIL);
            }
        }
    }

    /**
     * copy file
     *
     * @param file copy file
     * @param dstDir Specifies the destination directory of the file to be copied.
     */
    private void copyFile(File file, ArrayList<String> dstDir) {
        try {

            // Copy operation, copy to the first created directory
            File copyFile = new File();
            if (file == null || file.getFileName() == null || dstDir == null) {
                Logger.e(TAG, "copyFile arguments error");
                sendHandleMessage(R.id.drive_files_button_copy, FAIL);
                return;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String suffix = formatter.format(new Date());
            copyFile.setFileName(file.getFileName() + "_copy" + "_" + suffix);
            copyFile.setDescription("copyFile");
            copyFile.setParentFolder(dstDir);
            copyFile.setFavorite(true);
            copyFile.setEditedTime(new DateTime(System.currentTimeMillis()));

            Drive drive = buildDrive();
            Drive.Files.Copy copyFileReq = drive.files().copy(file.getId(), copyFile);
            copyFileReq.setFields("*");
            File result = copyFileReq.execute();
            Logger.i(TAG, "copyFile: " + result.toString());
            sendHandleMessage(R.id.drive_files_button_copy, SUCCESS);
        } catch (IOException ex) {
            Logger.e(TAG, "copyFile error: " + ex.toString());
            sendHandleMessage(R.id.drive_files_button_copy, FAIL);
        }
    }

    /**
     * The Files.delete interface test task, use to delete file or directory
     */
    private class FilesDeleteTask extends DriveTask {
        @Override
        public void call() {
            //Create a folder and delete it
            File dir = getDirectory();
            deleteFile(dir.getId());
        }
    }

    /**
     * Create a directory to test deleteFile
     *
     * @return file
     */
    private File getDirectory() {
        File uploadFile = null;
        // Newly created directory
        Drive drive = buildDrive();
        Map<String, String> appProperties = new HashMap<>();
        appProperties.put("test", "property");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        String dir = formatter.format(new Date());
        File file = new File();
        file.setFileName(dir).setAppSettings(appProperties).setMimeType("application/vnd.huawei-apps.folder");
        try {
            uploadFile = drive.files().create(file).execute();
        } catch (IOException e) {
            Logger.e(TAG, e.toString());
        }
        return uploadFile;
    }

    /**
     * Delete files (directories) from the recycle bin
     *
     * @param fileId file ID
     */
    private void deleteFile(String fileId) {
        if (fileId == null) {
            Logger.i(TAG, "deleteFile error, need to create file");
            sendHandleMessage(R.id.drive_files_button_delete, FAIL);
        }
        try {
            Drive drive = buildDrive();
            Drive.Files.Delete deleteFileReq = drive.files().delete(fileId);
            deleteFileReq.execute();
            Logger.i(TAG, "deleteFile result: " + deleteFileReq.toString());
            sendHandleMessage(R.id.drive_files_button_delete, SUCCESS);
        } catch (IOException ex) {
            sendHandleMessage(R.id.drive_files_button_delete, FAIL);
            Logger.e(TAG, "deleteFile error: " + ex.toString());
        }
    }

    /**
     * Execute the Files.update test task
     */
    private void executeFilesUpdateContent() {
        TaskManager.getInstance().execute(new FilesUpdateContentTask());
    }

    /**
     * The Files.update interface test task
     */
    private class FilesUpdateContentTask extends DriveTask {
        @Override
        public void call() {
            String newFilePath = context.getExternalCacheDir().getAbsolutePath() + "/cache.jpg";
            uodateFile(mFile, newFilePath);
        }
    }

    /**
     * Update the metadata and content of the file.
     *
     * @param oldFile Specifies the old file to be updated.
     * @param newFilePath new File
     */
    void uodateFile(File oldFile, String newFilePath) {
        try {
            if (oldFile == null || TextUtils.isEmpty(newFilePath)) {
                Logger.e(TAG, "updateFileContent error, need to create file.");
                sendHandleMessage(R.id.drive_files_update_content_button, FAIL);
                return;
            }

            Drive drive = buildDrive();
            File content = new File();

            content.setFileName(oldFile.getFileName() + "_update").setMimeType(mimeType(".jpg")).setDescription("update image").setFavorite(true);

            java.io.File io = new java.io.File(newFilePath);
            FileContent fileContent = new FileContent(mimeType(io), io);
            Drive.Files.Update request = drive.files().update(oldFile.getId(), content, fileContent);
            boolean isDirectUpload = false;
            if (io.length() < DIRECT_UPLOAD_MAX_SIZE) {
                isDirectUpload = true;
            }

            request.getMediaHttpUploader().setDirectUploadEnabled(isDirectUpload);
            mFile = request.execute();

            Logger.i(TAG, "updateFileContent result: " + mFile.toString());
            sendHandleMessage(R.id.drive_files_update_content_button, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_update_content_button, FAIL);
            Logger.e(TAG, "updateFile error: " + e.toString());
        }
    }

    /**
     * Execute the Files.delete interface test task
     */
    private void executeFilesDelete() {
        TaskManager.getInstance().execute(new FilesDeleteTask());
    }

    /**
     * Execute the Files.emptyRecycle interface test task
     */
    private void executeFilesEmptyRecycle() {
        TaskManager.getInstance().execute(new FilesEmptyRecycleTask());
    }

    /**
     * Execute the Files.emptyRecycle interface test task
     */
    private class FilesEmptyRecycleTask extends DriveTask {
        @Override
        public void call() {
            doFilesEmptyRecycle();
        }
    }

    /**
     * empty recycle bin
     */
    private void doFilesEmptyRecycle() {
        Drive drive = buildDrive();
        try {
            //create a new folder
            Map<String, String> appProperties = new HashMap<>();
            appProperties.put("property", "user_defined");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
            String dir = formatter.format(new Date());
            File file = new File();
            file.setFileName(dir).setAppSettings(appProperties).setMimeType("application/vnd.huawei-apps.folder");
            File uploadFile = drive.files().create(file).execute();

            // Call update to the recycle bin
            File trashFile = new File();
            trashFile.setRecycled(true);
            drive.files().update(uploadFile.getId(), trashFile).execute();
            // Empty the recycle bin
            Drive.Files.EmptyRecycle response = drive.files().emptyRecycle();
            response.execute();
            String value = response.toString();
            Logger.i(TAG, "executeFilesEmptyRecycle" + value);
            sendHandleMessage(R.id.drive_files_button_emptyRecycle, SUCCESS);
        } catch (IOException e) {
            Logger.e(TAG, "executeFilesEmptyRecycle error: " + e.toString());
            sendHandleMessage(R.id.drive_files_button_emptyRecycle, FAIL);
        }
    }

    /**
     * Execute the Files.subscribe interface test task
     */
    private void executeFilesSubscribe() {
        TaskManager.getInstance().execute(new FilesSubscribeTask());
    }

    /**
     * The Files.subscribe interface test task
     */
    private class FilesSubscribeTask extends DriveTask {

        @Override
        public void call() {
            filesWatch(mFile.getId());
        }
    }

    /**
     * watching for changes to a file
     *
     * @param fileId file ID
     */
    private void filesWatch(String fileId) {
        try {
            Drive drive = buildDrive();
            Channel content = new Channel();
            content.setId("id" + System.currentTimeMillis());
            content.setType("web_hook");
            content.setUrl("https://www.huawei.com/path/to/webhook");
            Drive.Files.Subscribe request = drive.files().subscribe(fileId, content);
            Channel channel = request.execute();
            //Object channel is used in other places.
            Logger.i(TAG, "channel: " + channel.toPrettyString());
            sendHandleMessage(R.id.drive_files_subscribe_button, SUCCESS);
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_files_subscribe_button, FAIL);
            Logger.e(TAG, "executeFilesSubscribe error: " + e.toString());
        }
    }

    /**
     * Execute the Changes.startCursor interface test task.
     */
    private void executeChangesGetStartCursor() {
        TaskManager.getInstance().execute(new ChangesGetStartCursorTask());
    }

    /**
     * The Changes.startCursor interface test task.
     */
    private class ChangesGetStartCursorTask extends DriveTask {
        @Override
        public void call() {
            doGetStartCursor();
        }
    }

    /**
     * In the future, the file will be changed. This gets the starting cursor of the changes
     */
    private void doGetStartCursor() {
        try {
            Drive drive = buildDrive();
            Drive.Changes.GetStartCursor request = drive.changes().getStartCursor();
            request.setFields("*");
            StartCursor startPageToken = request.execute();
            Logger.i(TAG, "GetStartCursor: " + startPageToken.toString());
            sendHandleMessage(R.id.drive_changes_getstartcursor_button, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_changes_getstartcursor_button, FAIL);
            Logger.e(TAG, "executeChangesGetStartCursor error: " + e.toString());
        }
    }

    /**
     * Execute the Changes.list interface test task
     */
    private void executeChangesList() {
        TaskManager.getInstance().execute(new ChangesListTask());
    }

    /**
     * The Changes.list interface test task
     */
    private class ChangesListTask extends DriveTask {
        @Override
        public void call() {
            getChangesList(watchListPageToken);
        }
    }

    /**
     * Lists the changes
     *
     * @param cursor The token to continue the previous list request on the next page.
     * It must be the value of nextCursor in the previous response or in the getStartCursor method.
     */
    private List<Change> getChangesList(String cursor) {
        if (cursor == null) {
            sendHandleMessage(R.id.drive_changes_list_button, FAIL);
            Logger.e(TAG, "getChangesList error: pageToken is null");
            return null;
        }
        try {
            Drive drive = buildDrive();
            Drive.Changes.List listRequest = drive.changes().list(cursor);
            listRequest.setFields("*");
            ChangeList changeList = listRequest.execute();
            List<Change> changes = changeList.getChanges();
            sendHandleMessage(R.id.drive_changes_list_button, SUCCESS);
            return changes;
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_changes_list_button, FAIL);
            Logger.e(TAG, "getChangesList error: " + e.toString());
            return null;
        }
    }

    /**
     * Execute the Changes.subscribe interface test task
     */
    private void executeChangesSubscribe() {
        TaskManager.getInstance().execute(new ChangesSubscribeTask());
    }

    /**
     * The Changes.subscribe interface test task
     */
    private class ChangesSubscribeTask extends DriveTask {

        @Override
        public void call() {
            String deviceId = HmsProxyImpl.getInstance().getDeviceId().substring(0, 5);
            watchChanges(context, deviceId);
        }
    }

    /**
     * Subscribe to file changes
     *
     * @param context current context
     * @param id identifies a channel with a UUID or unique string. user_defined
     */
    private void watchChanges(Context context, String id) {
        try {
            Drive drive = buildDrive();
            StartCursor StartCursor = drive.changes().getStartCursor().execute();
            String startCursor = StartCursor.getStartCursor();
            Channel content = new Channel();
            content.setId(id);
            content.setUserToken("1");
            content.setType("web_hook");
            content.setUrl("https://www.huawei.com/path/to/webhook");
            content.setExpirationTime(System.currentTimeMillis() + 300 * 1000L);
            Channel channel = drive.changes().subscribe(startCursor, content).execute();
            Logger.i(TAG, "execute Watch channel" + channel.toString());

            SharedPreferences prefs = context.getSharedPreferences("channel_config", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("startPageVersion", startCursor);
            editor.putString("resourceId", channel.getResourceId());
            editor.commit();
            watchListPageToken = startCursor;
            sendHandleMessage(R.id.drive_changes_subscribe_button, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_changes_subscribe_button, FAIL);
            Logger.e(TAG, "do Changes Subscribe error: " + e.toString());
        }
    }

    /**
     * Execute the Channels.stop interface test task
     */
    private void executeChannelsStop() {
        TaskManager.getInstance().execute(new ChannelsStop());
    }

    /**
     * The Channels.stop interface test task
     */
    private class ChannelsStop extends DriveTask {

        @Override
        public void call() {
            String id = HmsProxyImpl.getInstance().getDeviceId().substring(0, 5);
            stopChannel(context, id);
        }
    }

    /**
     * Close the specified Channel
     *
     * @param context Current context
     * @param id the same as channel.id returned by changes:watch
     */
    private void stopChannel(Context context, String id) {
        try {
            Drive drive = buildDrive();
            SharedPreferences preferences = context.getSharedPreferences("channel_config", Context.MODE_PRIVATE);
            String resourceId = preferences.getString("resourceId", "");
            Channel channel = new Channel();
            channel.setId(id);
            channel.setCategory("api#channel");
            channel.setResourceId(resourceId);
            Drive.Channels.Stop stopReq = drive.channels().stop(channel);
            stopReq.execute();
            sendHandleMessage(R.id.drive_channels_stop, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_channels_stop, FAIL);
            Logger.e(TAG, "stopChannel error: " + e.toString());
        }
    }

    /**
     * Execute the Replies Create interface test task
     */
    private void executeRepliesCreate() {
        TaskManager.getInstance().execute(new RepliesCreate());
    }

    /**
     * The replies.create interface test task
     */
    private class RepliesCreate extends DriveTask {
        @Override
        public void call() {
            if (mComment != null && mFile != null) {
                mReply = createReplies(mFile.getId(), mComment.getId());
            } else {
                String errMsg = "replies create error: need to create comments first.";
                Logger.e(TAG, errMsg);
                sendHandleMessage(R.id.drive_replies_create, FAIL, errMsg);
            }
        }
    }

    /**
     * Replying to a comment
     *
     * @param fileId file ID
     * @param commentId comment ID
     * @return a Reply details
     */
    private Reply createReplies(String fileId, String commentId) {
        Drive drive = buildDrive();
        Reply content = new Reply();
        content.setDescription("a comment reply");
        try {
            Drive.Replies.Create request = drive.replies().create(fileId, commentId, content).setFields("*");
            Reply reply = request.execute();
            sendHandleMessage(R.id.drive_replies_create, SUCCESS);
            return reply;
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_replies_create, FAIL);
            Logger.e(TAG, "replies create error: " + e.toString());
            return null;
        }
    }

    /**
     * Execute the Replies List interface test task
     */
    private void executeRepliesList() {
        TaskManager.getInstance().execute(new RepliesList());
    }

    /**
     * The replies.list interface test task
     */
    private class RepliesList extends DriveTask {
        @Override
        public void call() {
            if (mComment != null && mFile != null) {
                listReplies(mFile.getId(), mComment.getId());
            } else {
                sendHandleMessage(R.id.drive_replies_list, FAIL);
                Logger.e(TAG, "replies list error: args wrong");
            }
        }
    }

    /**
     * Get all replies to a comment
     *
     * @param fileId file ID
     * @param commentId comment ID
     * @return a list that has All replies on comments
     */
    private List<Reply> listReplies(String fileId, String commentId) {
        Drive drive = buildDrive();
        ArrayList<Reply> replyArrayList = new ArrayList<>();
        String nextCursor = null;
        try {
            Drive.Replies.List request = drive.replies().list(fileId, commentId).setFields("*");
            do {
                if (nextCursor != null) {
                    request.setCursor(nextCursor);
                }
                ReplyList rlist = request.setPageSize(100).execute();
                ArrayList<Reply> replies = (ArrayList<Reply>) rlist.getReplies();
                if (replies == null) {
                    break;
                }
                replyArrayList.addAll(replies);
                nextCursor = rlist.getNextCursor();
            } while (!StringUtils.isNullOrEmpty(nextCursor));
            Logger.i(TAG, "replies size " + replyArrayList.size());
            sendHandleMessage(R.id.drive_replies_list, SUCCESS);
            return replyArrayList;
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_replies_list, FAIL);
            Logger.e(TAG, "permission list error: " + e.toString());
            return null;
        }
    }

    /**
     * Execute the Replies Get interface test task
     */
    private void executeRepliesGet() {
        TaskManager.getInstance().execute(new RepliesGet());
    }

    /**
     * The replies.get interface test task
     */
    private class RepliesGet extends DriveTask {
        @Override
        public void call() {
            if (mComment != null && mFile != null && mReply != null) {
                mReply = getReplies(mFile.getId(), mComment.getId(), mReply.getId());
            } else {
                sendHandleMessage(R.id.drive_replies_get, FAIL);
                Logger.e(TAG, "replies get error: args wrong");
            }
        }
    }

    /**
     * Get a reply
     *
     * @param fileId file ID
     * @param commentId comment ID
     * @param replyId reply ID
     * @return reply details
     */
    private Reply getReplies(String fileId, String commentId, String replyId) {
        Drive drive = buildDrive();
        try {
            Drive.Replies.Get request = drive.replies().get(fileId, commentId, replyId).setFields("*");
            Reply reply = request.execute();
            Logger.i(TAG, "get reply :" + reply.getDescription() + ", " + reply.getCreatedTime() + ", " + reply.getCreator());
            sendHandleMessage(R.id.drive_replies_get, SUCCESS);
            return reply;
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_replies_get, FAIL);
            Logger.e(TAG, "replies get error: " + e.toString());
            return null;
        }
    }

    /**
     * Execute the Replies Update interface test task
     */
    private void executeRepliesUpdate() {
        TaskManager.getInstance().execute(new RepliesUpdate());
    }

    /**
     * The replies.update interface test task
     */
    private class RepliesUpdate extends DriveTask {
        @Override
        public void call() {
            if (mComment != null && mFile != null && mReply != null) {
                updateReplies(mFile.getId(), mComment.getId(), mReply.getId());
            } else {
                sendHandleMessage(R.id.drive_replies_update, FAIL);
                Logger.e(TAG, "replies update error: args wrong");
            }
        }
    }

    /**
     * Update a reply
     *
     * @param fileId file ID
     * @param commentId comment ID
     * @param replyId reply ID
     */
    private void updateReplies(String fileId, String commentId, String replyId) {
        Drive drive = buildDrive();
        try {
            Reply latestReply = new Reply();
            latestReply.setDescription("update a reply");
            // update reply
            Drive.Replies.Update update_request = drive.replies().update(fileId, commentId, replyId, latestReply).setFields("*");
            mReply = update_request.execute();
            Logger.i(TAG, "get reply :" + mReply.getDescription() + ", " + mReply.getEditedTime() + ", " + mReply.getCreator());

            sendHandleMessage(R.id.drive_replies_update, SUCCESS);
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_replies_update, FAIL);
            Logger.e(TAG, "replies update error: " + e.toString());
        }
    }

    /**
     * Execute the Replies Delete interface test task
     */
    private void executeRepliesDelete() {
        TaskManager.getInstance().execute(new RepliesDelete());
    }

    /**
     * The replies.delete interface test task
     */
    private class RepliesDelete extends DriveTask {
        @Override
        public void call() {
            if (mComment != null && mFile != null && mReply != null) {
                deleteReplies(mFile.getId(), mComment.getId(), mReply.getId());
            } else {
                sendHandleMessage(R.id.drive_replies_delete, FAIL);
                Logger.e(TAG, "replies delete error: args wrong");
            }
        }
    }

    /**
     * Deleting a reply
     *
     * @param fileId file ID
     * @param commentId comment ID
     * @param replyId reply ID
     */
    private void deleteReplies(String fileId, String commentId, String replyId) {
        Drive drive = buildDrive();
        try {
            Drive.Replies.Delete request = drive.replies().delete(fileId, commentId, replyId);
            request.execute();
            sendHandleMessage(R.id.drive_replies_delete, SUCCESS);
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_replies_delete, FAIL);
            Logger.e(TAG, "replies delete error: " + e.toString());
        }
    }

    /**
     * Execute the Comments Create interface test task
     */
    private void executeCommentsCreate() {
        TaskManager.getInstance().execute(new CommentsCreate());
    }

    /**
     * The comments.create interface test task
     */
    private class CommentsCreate extends DriveTask {
        @Override
        public void call() {
            if (mFile != null) {
                mComment = createComments(mFile.getId());
            } else {
                String errMsg = "comment create error: need to create file first.";
                Logger.e(TAG, errMsg);
                sendHandleMessage(R.id.drive_comments_create, FAIL, errMsg);
            }
        }
    }

    /**
     * Add a comment below the file.
     *
     * @param fileId file ID
     * @return comment details
     */
    private Comment createComments(String fileId) {
        Drive drive = buildDrive();
        Comment content = new Comment();
        content.setDescription("test description");
        try {
            Drive.Comments.Create request = drive.comments().create(fileId, content).setFields("*");
            Comment comment = request.execute();
            sendHandleMessage(R.id.drive_comments_create, SUCCESS);
            return comment;
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_comments_create, FAIL);
            Logger.e(TAG, "comment create error: " + e.toString());
            return null;
        }
    }

    /**
     * Execute the Comments List interface test task
     */
    private void executeCommentsList() {
        TaskManager.getInstance().execute(new CommentsList());
    }

    /**
     * The comments.list interface test task
     */
    private class CommentsList extends DriveTask {
        @Override
        public void call() {
            if (mFile != null) {
                List<Comment> list = listComments(mFile.getId());
                if (list != null) {
                    sendHandleMessage(R.id.drive_comments_list, SUCCESS);
                } else {
                    sendHandleMessage(R.id.drive_comments_list, FAIL);
                }
            } else {
                sendHandleMessage(R.id.drive_comments_list, FAIL);
                Logger.e(TAG, "comment list error: args wrong");
            }
        }
    }

    /**
     * List all comments on file
     *
     * @param fileId file ID
     * @return a list that has All Comments on file
     */
    private List<Comment> listComments(String fileId) {
        Drive drive = buildDrive();
        ArrayList<Comment> commentArrayList = new ArrayList<>();
        String nextCursor = null;
        try {
            Drive.Comments.List request = drive.comments().list(fileId);
            do {
                if (nextCursor != null) {
                    request.setCursor(nextCursor);
                }
                CommentList commentList = request.setPageSize(100).setFields("*").execute();
                ArrayList<Comment> comments = (ArrayList<Comment>) commentList.getComments();
                if (comments == null) {
                    break;
                }
                commentArrayList.addAll(comments);
                nextCursor = commentList.getNextCursor();
            } while (!StringUtils.isNullOrEmpty(nextCursor));
            Logger.i(TAG, "comments size " + commentArrayList.size());
            return commentArrayList;
        } catch (IOException e) {
            Logger.e(TAG, "comments list error: " + e.toString());
            return null;
        }
    }

    /**
     * Execute the Comments Get interface test task
     */
    private void executeCommentsGet() {
        TaskManager.getInstance().execute(new CommentsGet());
    }

    /**
     * The comments.get interface test task
     */
    private class CommentsGet extends DriveTask {
        @Override
        public void call() {
            if (mFile != null && mComment != null) {
                mComment = getComments(mFile.getId(), mComment.getId());
            } else {
                sendHandleMessage(R.id.drive_comments_get, FAIL);
                Logger.e(TAG, "comment get error: args wrong");
            }
        }
    }

    /**
     * Get a comment
     *
     * @param fileId file ID
     * @param commentId comment ID
     * @return comment details
     */
    private Comment getComments(String fileId, String commentId) {
        Drive drive = buildDrive();
        try {
            Drive.Comments.Get request = drive.comments().get(fileId, commentId).setFields("*");
            Comment latestComment = request.execute();
            Logger.i(TAG, latestComment.getDescription() + "," + latestComment.getCreatedTime());
            sendHandleMessage(R.id.drive_comments_get, SUCCESS);
            return latestComment;
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_comments_get, FAIL);
            Logger.e(TAG, "comment get error: " + e.toString());
            return null;
        }
    }

    /**
     * Execute the Comments Update interface test task
     */
    private void executeCommentsUpdate() {
        TaskManager.getInstance().execute(new CommentsUpdate());
    }

    /**
     * The comments.update interface test task
     */
    private class CommentsUpdate extends DriveTask {
        @Override
        public void call() {
            if (mFile != null && mComment != null) {
                updateComments(mFile.getId(), mComment.getId());
            } else {
                sendHandleMessage(R.id.drive_comments_update, FAIL);
                Logger.e(TAG, "comment update error: args wrong");
            }
        }
    }

    /**
     * Update a comment on file
     *
     * @param fileId file ID
     * @param commentId comment ID
     */
    private void updateComments(String fileId, String commentId) {
        Drive drive = buildDrive();
        try {
            Comment comment = new Comment();
            comment.setDescription("update a comment");

            Drive.Comments.Update update_request = drive.comments().update(fileId, commentId, comment).setFields("*");
            mComment = update_request.execute();
            Logger.i(TAG, mComment.getDescription() + ", " + mComment.getCreator() + ", " + mComment.getEditedTime());
            sendHandleMessage(R.id.drive_comments_update, SUCCESS);
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_comments_update, FAIL);
            Logger.e(TAG, "comment update error: " + e.toString());
        }
    }

    /**
     * Execute the Comments Delete interface test task
     */
    private void executeCommentsDelete() {
        TaskManager.getInstance().execute(new CommentsDelete());
    }

    /**
     * The comments.delete interface test task
     */
    private class CommentsDelete extends DriveTask {
        @Override
        public void call() {
            if (mFile == null) {
                sendHandleMessage(R.id.drive_comments_delete, FAIL);
                Logger.e(TAG, "comment delete error: args wrong");
            }
            // create a comment
            Comment content = new Comment();
            content.setDescription("a tmp description");

            Drive drive = buildDrive();
            try {
                Drive.Comments.Create request = drive.comments().create(mFile.getId(), content).setFields("*");
                Comment comment = request.execute();
                deleteComments(mFile.getId(), comment.getId());
            } catch (IOException e) {
                sendHandleMessage(R.id.drive_comments_delete, FAIL);
                Logger.e(TAG, "comment create error: " + e.toString());
            }
        }
    }

    /**
     * Delete a comment
     *
     * @param fileId file ID
     * @param commentId comment ID
     */
    private void deleteComments(String fileId, String commentId) {
        Drive drive = buildDrive();
        try {
            Drive.Comments.Delete request = drive.comments().delete(fileId, commentId);
            request.execute();
            sendHandleMessage(R.id.drive_comments_delete, SUCCESS);
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_comments_delete, FAIL);
            Logger.e(TAG, "comment delete error: " + e.toString());
        }
    }

    /**
     * delete a history version
     */
    private void executeHistoryVersionsDelete() {
        TaskManager.getInstance().execute(new HistoryVersionsDeleteTask());
    }

    private class HistoryVersionsDeleteTask extends DriveTask {
        @Override
        public void call() {
            if (mFile != null && deleteHistoryVersions != null) {
                deleteHistoryVersions(mFile.getId(), deleteHistoryVersions.getId());
            } else {
                Logger.e(TAG, "mHistoryVersion is null or mFile is null.");
                sendHandleMessage(R.id.drive_historyversions_delete, FAIL);
            }
        }
    }

    private void deleteHistoryVersions(String fileId, String historyVersionId) {
        Drive drive = buildDrive();
        if (drive == null) {
            Logger.e(TAG, "deleteHistoryVersions error: drive is null.");
            sendHandleMessage(R.id.drive_historyversions_delete, FAIL);
            return;
        }
        try {
            drive.historyVersions().delete(fileId, historyVersionId).execute();
            sendHandleMessage(R.id.drive_historyversions_delete, SUCCESS);
        } catch (Exception ex) {
            sendHandleMessage(R.id.drive_historyversions_delete, FAIL);
            Logger.e(TAG, "deleteHistoryVersions error: " + ex.toString());
        }
    }

    /**
     * get a history version
     */
    private void executeHistoryVersionsGet() {
        TaskManager.getInstance().execute(new HistoryVersionsGetTask());
    }

    private class HistoryVersionsGetTask extends DriveTask {
        @Override
        public void call() {
            HistoryVersion temp;
            if (mFile != null && mHistoryVersion != null) {
                temp = getHistoryVersions(mFile.getId(), mHistoryVersion.getId());
            } else {
                Logger.e(TAG, "mHistoryVersion is null or mFile is null.");
                sendHandleMessage(R.id.drive_historyversions_get, FAIL);
                return;
            }
            if (temp == null) {
                sendHandleMessage(R.id.drive_historyversions_get, FAIL);
                return;
            }
            sendHandleMessage(R.id.drive_historyversions_get, SUCCESS);
        }
    }

    private HistoryVersion getHistoryVersions(String fileId, String historyVersionId) {
        Drive drive = buildDrive();
        if (drive == null) {
            Logger.e(TAG, "getHistoryVersions error: drive is null.");
            return null;
        }
        HistoryVersion historyVersion = null;
        try {
            historyVersion = drive.historyVersions().get(fileId, historyVersionId).execute();
        } catch (Exception ex) {
            Logger.e(TAG, "getHistoryVersions error: " + ex.toString());
        }
        return historyVersion;
    }

    private void executeHistoryVersionsList() {
        TaskManager.getInstance().execute(new HistoryVersionsListTask());
    }

    private class HistoryVersionsListTask extends DriveTask {
        @Override
        public void call() {
            HistoryVersionList historyVersionList;
            if (mFile != null) {
                historyVersionList = listHistoryVersions(mFile.getId());
            } else {
                String errMsg = "history versions list error: need to create file first.";
                Logger.e(TAG, errMsg);
                sendHandleMessage(R.id.drive_historyversions_list, FAIL, errMsg);
                return;
            }
            if (historyVersionList != null) {
                Logger.e(TAG, "historyVersionList size " + historyVersionList.getHistoryVersions().size());
                if (historyVersionList.getHistoryVersions().size() > 0) {
                    mHistoryVersion = historyVersionList.getHistoryVersions().get(0);
                }
                if (historyVersionList.getHistoryVersions().size() > 1) {
                    deleteHistoryVersions = historyVersionList.getHistoryVersions().get(1);;
                }
                sendHandleMessage(R.id.drive_historyversions_list, SUCCESS);
            } else {
                sendHandleMessage(R.id.drive_historyversions_list, FAIL);
            }
        }

        /**
         * list all history versions
         * @param fileId file ID
         * @return HistoryVersionList
         */
        private HistoryVersionList listHistoryVersions(String fileId) {
            Drive drive = buildDrive();
            HistoryVersionList historyVersionList = null;
            if (drive == null) {
                Logger.e(TAG, "listHistoryVersions error: drive is null.");
                return null;
            }
            try {
                historyVersionList = drive.historyVersions().list(fileId).setFields("*").execute();
            } catch (Exception ex) {
                Logger.e(TAG, "listHistoryVersions error: " + ex.toString());
            }
            return historyVersionList;
        }
    }

    private void executeHistoryVersionsUpdate() {
        TaskManager.getInstance().execute(new HistoryVersionsUpdateTask());
    }

    private class HistoryVersionsUpdateTask extends DriveTask {

        @Override
        public void call() {
            if (mFile != null && mHistoryVersion != null) {
                updateHistoryVersions(mFile.getId(), mHistoryVersion);
            } else {
                Logger.e(TAG, "mFile is null or mHistoryVersion is null");
                sendHandleMessage(R.id.drive_historyversions_update, FAIL);
            }
        }
    }

    /**
     * update one history versions
     */
    private void updateHistoryVersions(String fileId, HistoryVersion oldHistoryVersion) {
        Drive drive = buildDrive();
        if (drive == null) {
            Log.e(TAG, "updateHistoryVersions error: drive is null.");
            sendHandleMessage(R.id.drive_historyversions_update, FAIL);
            return;
        }
        if (oldHistoryVersion == null) {
            Log.e(TAG, "updateHistoryVersions error: oldHistoryVersion is null.");
            sendHandleMessage(R.id.drive_historyversions_update, FAIL);
            return;
        }
        try {
            // Modify whatever you want
            HistoryVersion historyVersion = new HistoryVersion();
            historyVersion.setKeepPermanent(!oldHistoryVersion.getKeepPermanent());
            drive.historyVersions().update(fileId, oldHistoryVersion.getId(), historyVersion).execute();
            sendHandleMessage(R.id.drive_historyversions_update, SUCCESS);
        } catch (Exception ex) {
            sendHandleMessage(R.id.drive_historyversions_update, FAIL);
            Log.e(TAG, "updateHistoryVersions error: " + ex.toString());
        }
    }

    private void executeDataBackUp() {
        TaskManager.getInstance().execute(new DataBackUpTask());
    }

    private class DataBackUpTask extends DriveTask {
        @Override
        public void call() {
            doBackUpData();
        }
    }

    /**
     * Assume that the application data file is AppDataBackUpFileName.jpg.
     * Application data backup is to upload the AppDataBackUpFileName.jpg file to the application data directory on the cloud disk.
     */
    private void doBackUpData() {
        try {
            Drive drive = buildDrive();
            String BackFileName = "AppDataBackUpFileName.jpg";
            String personalBackUpFolder = "AppName";
            java.io.File fileObject = new java.io.File("/sdcard/" + BackFileName);
            Map<String, String> appProperties = new HashMap<>();
            // 1. create backup folder under folder "applicationData" first
            appProperties.put("appProperties", "property");
            File file = new File();
            file.setFileName(personalBackUpFolder + System.currentTimeMillis())
                .setMimeType("application/vnd.huawei-apps.folder")
                .setAppSettings(appProperties)
                .setParentFolder(Collections.singletonList("applicationData"));
            File directoryCreated = drive.files().create(file).execute();

            // 2. backUp: upload file "AppDatabackUpFileName"
            String mimeType = mimeType(fileObject);
            File content = new File();
            content.setFileName(fileObject.getName()).setMimeType(mimeType).setParentFolder(Collections.singletonList(directoryCreated.getId()));
            mBackupFile = drive.files().create(content, new FileContent(mimeType, fileObject)).setFields("*").execute();
            sendHandleMessage(R.id.drive_backup_btn, SUCCESS);
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_backup_btn, FAIL);
            Logger.e(TAG, "BackUpData error: " + e.toString());
        }
    }

    private void executeDataRestore() {
        TaskManager.getInstance().execute(new DataRestoreTask());
    }

    private class DataRestoreTask extends DriveTask {
        @Override
        public void call() {
            if (mBackupFile != null) {
                doRestoreData();
            } else {
                String errMsg = "data restore error: need to date backup first.";
                Logger.e(TAG, errMsg);
                sendHandleMessage(R.id.drive_restore_btn, FAIL, errMsg);
            }
        }
    }

    /**
     * restore file named "AppDataBackUpFileName"
     */
    private void doRestoreData() {
        Drive drive = buildDrive();
        String searchFileName = "AppDataBackUpFileName.jpg";
        // must set containers as "applicationData" if we want list the backUp files
        String containers = "applicationData";
        String queryFile = "fileName = '" + searchFileName + "' and mimeType != 'application/vnd.huawei-apps.folder'";
        try {
            // 1. list all back files
            Drive.Files.List request = drive.files().list();
            FileList files;
            List<File> backupFiles = new ArrayList<>();
            while (true) {
                files = request.setQueryParam(queryFile).setPageSize(10).setOrderBy("fileName").setFields("category,nextCursor,files/id,files/fileName,files/size").setContainers(containers).execute();
                if (files == null || files.getFiles().size() > 0) {
                    break;
                }

                backupFiles.addAll(files.getFiles());
                String nextCursor = files.getNextCursor();
                if (!StringUtils.isNullOrEmpty(nextCursor)) {
                    request.setCursor(files.getNextCursor());
                } else {
                    break;
                }
            }

            // 2. we can download "AppDataBackUpFileName" to restore local file
            long size = mBackupFile.getSize();
            Drive.Files.Get get = drive.files().get(mBackupFile.getId());
            MediaHttpDownloader downloader = get.getMediaHttpDownloader();

            boolean isDirectDownload = false;
            if (size < DIRECT_DOWNLOAD_MAX_SIZE) {
                isDirectDownload = true;
            }
            downloader.setContentRange(0, size - 1).setDirectDownloadEnabled(isDirectDownload);
            String restoreFileName = "restoreFileName.jpg";
            java.io.File f = new java.io.File("/sdcard/" + restoreFileName);
            get.executeContentAndDownloadTo(new FileOutputStream(f));
            sendHandleMessage(R.id.drive_restore_btn, SUCCESS);
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_restore_btn, FAIL);
            Logger.e(TAG, "RestoreData error: " + e.toString());
        }
    }

    private void executeOnlineOpen() {
        TaskManager.getInstance().execute(new OnlineOpenTask());
    }

    private class OnlineOpenTask extends DriveTask {
        @Override
        public void call() {
            // 1. create folder
            mDirectory = createDirectory();
            // 2. upload a docx
            Drive drive = buildDrive();
            File temp = null;
            String filePath = context.getExternalCacheDir().getAbsolutePath() + "/test.docx";
            java.io.File io = new java.io.File(filePath);
            FileContent fileContent = new FileContent(mimeType(io), io);
            File content = new File().setFileName(io.getName()).setMimeType(mimeType(io)).setParentFolder(Collections.singletonList(mDirectory.getId()));
            try {
                Drive.Files.Create rquest = drive.files().create(content, fileContent);
                rquest.setFields("id,onLineViewLink");
                rquest.getMediaHttpUploader().setDirectUploadEnabled(true);
                temp = rquest.execute();
            } catch (IOException e) {
                Logger.e(TAG, "upload file error: " + e.toString());
                sendHandleMessage(R.id.drive_online_open_btn, FAIL);
                return;
            }
            openOnlineFile(temp);
        }
    }

    /**
     * use webOffice to open **.docs
     */
    private void openOnlineFile(File file) {
        String onlineViewLink = file.getOnLineViewLink();
        if (TextUtils.isEmpty(onlineViewLink)) {
            String errMsg = "open file error: link is empty!";
            Logger.e(TAG, errMsg);
            sendHandleMessage(R.id.drive_online_open_btn, FAIL, errMsg);
            return;
        }
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra("url", onlineViewLink);
        Logger.d(TAG, "open file: " + file.getFileName() + " in WebView, url is " + onlineViewLink);
        try {
            startActivityForResult(intent, WEB_VIEW_BACK_REFRESH);
        } catch (ActivityNotFoundException e) {
            sendHandleMessage(R.id.drive_online_open_btn, FAIL);
            Logger.e(TAG, "start WebViewActivity has ActivityNotFoundException.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WEB_VIEW_BACK_REFRESH) {
            sendHandleMessage(R.id.drive_online_open_btn, SUCCESS);
        }
    }
}
