package com.sun.mail.pop3;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.mail.util.SharedFileInputStream;

class WritableSharedFile extends SharedFileInputStream {

    /* renamed from: af */
    private AppendStream f284af;
    private RandomAccessFile raf;

    public WritableSharedFile(File file) throws IOException {
        super(file);
        try {
            this.raf = new RandomAccessFile(file, "rw");
        } catch (IOException e) {
            super.close();
        }
    }

    public RandomAccessFile getWritableFile() {
        return this.raf;
    }

    public void close() throws IOException {
        try {
            super.close();
        } finally {
            this.raf.close();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized long updateLength() throws IOException {
        this.datalen = this.f314in.length();
        this.f284af = null;
        return this.datalen;
    }

    public synchronized AppendStream getAppendStream() throws IOException {
        if (this.f284af != null) {
            throw new IOException("POP3 file cache only supports single threaded access");
        }
        this.f284af = new AppendStream(this);
        return this.f284af;
    }
}
