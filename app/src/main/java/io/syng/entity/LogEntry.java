/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.entity;

public class LogEntry {

    private long timeStamp;
    private String message;

    public LogEntry(long timeStamp, String message) {
        this.timeStamp = timeStamp;
        this.message = message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getMessage() {
        return message;
    }
}
