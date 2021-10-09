package com.sun.mail.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/* compiled from: WriteTimeoutSocket */
class TimeoutOutputStream extends OutputStream {

    /* renamed from: b1 */
    private byte[] f290b1;
    /* access modifiers changed from: private */

    /* renamed from: os */
    public final OutputStream f291os;
    private final ScheduledExecutorService ses;
    private final int timeout;
    private final Callable<Object> timeoutTask = new Callable<Object>() {
        public Object call() throws Exception {
            TimeoutOutputStream.this.f291os.close();
            return null;
        }
    };

    public TimeoutOutputStream(OutputStream os0, ScheduledExecutorService ses2, int timeout2) throws IOException {
        this.f291os = os0;
        this.ses = ses2;
        this.timeout = timeout2;
    }

    public synchronized void write(int b) throws IOException {
        if (this.f290b1 == null) {
            this.f290b1 = new byte[1];
        }
        this.f290b1[0] = (byte) b;
        write(this.f290b1);
    }

    public synchronized void write(byte[] bs, int off, int len) throws IOException {
        if (off >= 0) {
            if (off <= bs.length && len >= 0 && off + len <= bs.length && off + len >= 0) {
                if (len != 0) {
                    ScheduledFuture<Object> sf = null;
                    try {
                        if (this.timeout > 0) {
                            sf = this.ses.schedule(this.timeoutTask, (long) this.timeout, TimeUnit.MILLISECONDS);
                        }
                    } catch (RejectedExecutionException e) {
                    }
                    try {
                        this.f291os.write(bs, off, len);
                        if (sf != null) {
                            sf.cancel(true);
                        }
                    } catch (Throwable th) {
                        if (sf != null) {
                            sf.cancel(true);
                        }
                        throw th;
                    }
                }
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public void close() throws IOException {
        this.f291os.close();
    }
}
