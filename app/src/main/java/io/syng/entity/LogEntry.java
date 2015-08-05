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
