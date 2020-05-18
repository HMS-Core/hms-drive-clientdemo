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

package com.huawei.cloud.drive.log;

import android.util.Log;

import com.huawei.cloud.drive.config.Configure;

/**
 * Log tool class requires that you need to print the log before you can judge and then print
 */
public class Logger {
    /**
     * Log prefix
     */
    private static final String TAG_PREFIX = "CloudTest";

    private static String setTag(String tag) {
        return String.format("[%s]%s.%s", Configure.VERSIONNAME, TAG_PREFIX, tag);
    }

    /**
     * Print info level log - custom prefix
     *
     * @param tag Class label
     * @param msg message
     */
    public static void i(String tag, String msg) {
        if (tag == null) {
            return;
        }

        Log.i(setTag(tag), msg);
    }

    /**
     * Print debug log
     *
     * @param tag Class label
     * @param msg message
     */
    public static void d(String tag, String msg) {
        if (tag == null) {
            return;
        }

        Log.d(setTag(tag), msg);

    }

    /**
     * Print warning log
     *
     * @param tag Class label
     * @param msg message
     */
    public static void w(String tag, String msg) {
        if (tag == null) {
            return;
        }

        Log.w(setTag(tag), msg);
    }

    /**
     * Print v log
     *
     * @param tag Class label
     * @param msg message
     */
    public static void v(String tag, String msg) {
        if (tag == null) {
            return;
        }

        Log.v(setTag(tag), msg);

    }

    /**
     * Print error log
     *
     * @param tag Class label
     * @param msg message
     */
    public static void e(String tag, String msg) {
        if (tag == null) {
            return;
        }

        Log.e(setTag(tag), msg);
    }

}
