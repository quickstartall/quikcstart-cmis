package com.quickstartall.cmis.domain;

public abstract class AbstractDocument {
    private String id;
    private String name;
    private byte[] data;
    private long length;
    private String contentType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte[] getData() {
        return data.clone();
    }

    public void setData(byte[] data) {
        if (data == null) {
            this.data = new byte[0];
            this.length = 0;
        } else {
            this.data = data.clone();
            this.length = data.length;
        }
    }
}
