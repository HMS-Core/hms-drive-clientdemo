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
package com.huawei.cloud.drive.utils;

import android.app.Activity;
import android.view.View;

/**
 * View util class
 */
public class ViewUtil {

    private static final String TAG = "ViewUtil";

    private ViewUtil() {

    }

    /**
     * Look for a child view with the given id. If this view has the given id, return this view.<BR>
     *
     * @param activity activity
     * @param id       The id to search for.
     * @return The view that has the given id in the hierarchy or null
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewById(Activity activity, int id) {
        if (null != activity) {
            return (T) activity.findViewById(id);
        }

        return null;
    }
}