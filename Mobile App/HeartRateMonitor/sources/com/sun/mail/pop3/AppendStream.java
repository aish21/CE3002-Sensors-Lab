package com.sun.mail.pop3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

class AppendStream extends OutputStream {
    private long end;
    private RandomAccessFile raf;
    private final long start = this.raf.length();

    /* renamed from: tf */
    private final WritableSharedFile f281tf;

    public AppendStream(WritableSharedFile tf) throws IOException {
        this.f281tf = tf;
        this.raf = tf.getWritableFile();
        this.raf.seek(this.start);
    }

    public void write(int b) throws IOException {
        this.raf.write(b);
    }

    public void write(byte[] b) throws IOException {
        this.raf.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.raf.write(b, off, len);
    }

    public synchronized void close() throws IOException {
        this.end = this.f281tf.updateLength();
        this.raf = null;
    }

    public synchronized InputStream getInputStream() throws IOException {
        return this.f281tf.newStream(this.start, this.end);
    }
}
