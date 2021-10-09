package com.sun.mail.pop3;

import java.io.File;
import java.io.IOException;

class TempFile {
    private File file;

    /* renamed from: sf */
    private WritableSharedFile f283sf = new WritableSharedFile(this.file);

    public TempFile(File dir) throws IOException {
        this.file = File.createTempFile("pop3.", ".mbox", dir);
        this.file.deleteOnExit();
    }

    public AppendStream getAppendStream() throws IOException {
        return this.f283sf.getAppendStream();
    }

    public void close() {
        try {
            this.f283sf.close();
        } catch (IOException e) {
        }
        this.file.delete();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
