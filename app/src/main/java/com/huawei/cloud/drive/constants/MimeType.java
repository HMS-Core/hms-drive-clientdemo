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

package com.huawei.cloud.drive.constants;

import java.util.HashMap;
import java.util.Map;

public final class MimeType {

    // Huawei Drive Folder Type
    public static final String FOLDER = "application/vnd.huawei-apps.folder";

    // Default File Type
    public static final String DEFAULT = "*/*";

    // All cached MimeType types based on file suffixes
    public static final Map<String, String> MIME_TYPE_MAP = new HashMap<String, String>();

    static {
        MIME_TYPE_MAP.put(".3gp", "video/3gpp");
        MIME_TYPE_MAP.put(".apk", "application/vnd.android.package-archive");
        MIME_TYPE_MAP.put(".asf", "video/x-ms-asf");
        MIME_TYPE_MAP.put(".avi", "video/x-msvideo");
        MIME_TYPE_MAP.put(".bin", "application/octet-stream");
        MIME_TYPE_MAP.put(".bmp", "image/bmp");
        MIME_TYPE_MAP.put(".c", "text/plain");
        MIME_TYPE_MAP.put(".class", "application/octet-stream");
        MIME_TYPE_MAP.put(".conf", "text/plain");
        MIME_TYPE_MAP.put(".cpp", "text/plain");
        MIME_TYPE_MAP.put(".doc", "application/msword");
        MIME_TYPE_MAP.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPE_MAP.put(".xls", "application/vnd.ms-excel");
        MIME_TYPE_MAP.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPE_MAP.put(".exe", "application/octet-stream");
        MIME_TYPE_MAP.put(".gif", "image/gif");
        MIME_TYPE_MAP.put(".gtar", "application/x-gtar");
        MIME_TYPE_MAP.put(".gz", "application/x-gzip");
        MIME_TYPE_MAP.put(".h", "text/plain");
        MIME_TYPE_MAP.put(".htm", "text/html");
        MIME_TYPE_MAP.put(".html", "text/html");
        MIME_TYPE_MAP.put(".jar", "application/java-archive");
        MIME_TYPE_MAP.put(".java", "text/plain");
        MIME_TYPE_MAP.put(".jpeg", "image/jpeg");
        MIME_TYPE_MAP.put(".jpg", "image/jpeg");
        MIME_TYPE_MAP.put(".js", "application/x-javascript");
        MIME_TYPE_MAP.put(".log", "text/plain");
        MIME_TYPE_MAP.put(".m3u", "audio/x-mpegurl");
        MIME_TYPE_MAP.put(".m4a", "audio/mp4a-latm");
        MIME_TYPE_MAP.put(".m4b", "audio/mp4a-latm");
        MIME_TYPE_MAP.put(".m4p", "audio/mp4a-latm");
        MIME_TYPE_MAP.put(".m4u", "video/vnd.mpegurl");
        MIME_TYPE_MAP.put(".m4v", "video/x-m4v");
        MIME_TYPE_MAP.put(".mov", "video/quicktime");
        MIME_TYPE_MAP.put(".mp2", "audio/x-mpeg");
        MIME_TYPE_MAP.put(".mp3", "audio/x-mpeg");
        MIME_TYPE_MAP.put(".mp4", "video/mp4");
        MIME_TYPE_MAP.put(".mpc", "application/vnd.mpohun.certificate");
        MIME_TYPE_MAP.put(".mpe", "video/mpeg");
        MIME_TYPE_MAP.put(".mpeg", "video/mpeg");
        MIME_TYPE_MAP.put(".mpg", "video/mpeg");
        MIME_TYPE_MAP.put(".mpg4", "video/mp4");
        MIME_TYPE_MAP.put(".mpga", "audio/mpeg");
        MIME_TYPE_MAP.put(".msg", "application/vnd.ms-outlook");
        MIME_TYPE_MAP.put(".ogg", "audio/ogg");
        MIME_TYPE_MAP.put(".pdf", "application/pdf");
        MIME_TYPE_MAP.put(".png", "image/png");
        MIME_TYPE_MAP.put(".pps", "application/vnd.ms-powerpoint");
        MIME_TYPE_MAP.put(".ppt", "application/vnd.ms-powerpoint");
        MIME_TYPE_MAP.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MIME_TYPE_MAP.put(".prop", "text/plain");
        MIME_TYPE_MAP.put(".rc", "text/plain");
        MIME_TYPE_MAP.put(".rmvb", "audio/x-pn-realaudio");
        MIME_TYPE_MAP.put(".rtf", "application/rtf");
        MIME_TYPE_MAP.put(".sh", "text/plain");
        MIME_TYPE_MAP.put(".tar", "application/x-tar");
        MIME_TYPE_MAP.put(".tgz", "application/x-compressed");
        MIME_TYPE_MAP.put(".txt", "text/plain");
        MIME_TYPE_MAP.put(".wav", "audio/x-wav");
        MIME_TYPE_MAP.put(".wma", "audio/x-ms-wma");
        MIME_TYPE_MAP.put(".wmv", "audio/x-ms-wmv");
        MIME_TYPE_MAP.put(".wps", "application/vnd.ms-works");
        MIME_TYPE_MAP.put(".xml", "text/plain");
        MIME_TYPE_MAP.put(".z", "application/x-compress");
        MIME_TYPE_MAP.put(".zip", "application/x-zip-compressed");
    }

    /**
     * Match the MIMETYPE based on the file name suffix.
     *
     * @param suffix File Name suffix
     * @return MimeType
     */
    public static String mimeType(String suffix) {
        if (MIME_TYPE_MAP.keySet().contains(suffix)) {
            return MIME_TYPE_MAP.get(suffix);
        } else {
            return DEFAULT;
        }
    }


    /**
     * Match the MimeType based on the file type
     *
     * @param file java.io.File object
     * @return MimeType
     */
    public static String mimeType(java.io.File file) {
        if (file == null || !file.exists()) {
            return DEFAULT;
        }

        String fileName = file.getName();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return mimeType(suffix);
    }
}
