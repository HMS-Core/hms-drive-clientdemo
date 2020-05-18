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

package com.huawei.cloud.drive.task.task;

import com.huawei.cloud.base.util.Logger;

import java.util.concurrent.Future;

/**
 * Runnable task
 */
public abstract class DriveTask implements Runnable {

    public static final String TAG = "DriveTask";
    static final Logger LOGGER = Logger.getLogger(TAG);
    private Future<?> future;

    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            LOGGER.w("task error: " + e.toString());
        }
    }

    /**
     * DriveTask implementation
     */
    public abstract void call();


    /**
     * Used to terminate the current task
     */
    public boolean cancel() {
        if (null != future) {
            return future.cancel(true);
        }

        return false;
    }

    /**
     * Return future for custom get timeout
     */
    public Future<?> getFuture() {
        return future;
    }

    /**
     * Used to obtain task information when the scheduled task is executed.
     */
    public void setFuture(Future<?> future) {
        this.future = future;
    }
}
