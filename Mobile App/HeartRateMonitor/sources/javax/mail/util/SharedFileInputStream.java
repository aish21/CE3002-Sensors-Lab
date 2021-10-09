package javax.mail.util;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import javax.mail.internet.SharedInputStream;

public class SharedFileInputStream extends BufferedInputStream implements SharedInputStream {
    private static int defaultBufferSize = 2048;
    protected long bufpos;
    protected int bufsize;
    protected long datalen;

    /* renamed from: in */
    protected RandomAccessFile f314in;
    private boolean master;

    /* renamed from: sf */
    private SharedFile f315sf;
    protected long start;

    static class SharedFile {
        private int cnt;

        /* renamed from: in */
        private RandomAccessFile f316in;

        SharedFile(String file) throws IOException {
            this.f316in = new RandomAccessFile(file, "r");
        }

        SharedFile(File file) throws IOException {
            this.f316in = new RandomAccessFile(file, "r");
        }

        public synchronized RandomAccessFile open() {
            this.cnt++;
            return this.f316in;
        }

        public synchronized void close() throws IOException {
            if (this.cnt > 0) {
                int i = this.cnt - 1;
                this.cnt = i;
                if (i <= 0) {
                    this.f316in.close();
                }
            }
        }

        public synchronized void forceClose() throws IOException {
            if (this.cnt > 0) {
                this.cnt = 0;
                this.f316in.close();
            } else {
                try {
                    this.f316in.close();
                } catch (IOException e) {
                }
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                this.f316in.close();
            } finally {
                super.finalize();
            }
        }
    }

    private void ensureOpen() throws IOException {
        if (this.f314in == null) {
            throw new IOException("Stream closed");
        }
    }

    public SharedFileInputStream(File file) throws IOException {
        this(file, defaultBufferSize);
    }

    public SharedFileInputStream(String file) throws IOException {
        this(file, defaultBufferSize);
    }

    public SharedFileInputStream(File file, int size) throws IOException {
        super((InputStream) null);
        this.start = 0;
        this.master = true;
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        init(new SharedFile(file), size);
    }

    public SharedFileInputStream(String file, int size) throws IOException {
        super((InputStream) null);
        this.start = 0;
        this.master = true;
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        init(new SharedFile(file), size);
    }

    private void init(SharedFile sf, int size) throws IOException {
        this.f315sf = sf;
        this.f314in = sf.open();
        this.start = 0;
        this.datalen = this.f314in.length();
        this.bufsize = size;
        this.buf = new byte[size];
    }

    private SharedFileInputStream(SharedFile sf, long start2, long len, int bufsize2) {
        super((InputStream) null);
        this.start = 0;
        this.master = true;
        this.master = false;
        this.f315sf = sf;
        this.f314in = sf.open();
        this.start = start2;
        this.bufpos = start2;
        this.datalen = len;
        this.bufsize = bufsize2;
        this.buf = new byte[bufsize2];
    }

    private void fill() throws IOException {
        if (this.markpos < 0) {
            this.pos = 0;
            this.bufpos += (long) this.count;
        } else if (this.pos >= this.buf.length) {
            if (this.markpos > 0) {
                int sz = this.pos - this.markpos;
                System.arraycopy(this.buf, this.markpos, this.buf, 0, sz);
                this.pos = sz;
                this.bufpos += (long) this.markpos;
                this.markpos = 0;
            } else if (this.buf.length >= this.marklimit) {
                this.markpos = -1;
                this.pos = 0;
                this.bufpos += (long) this.count;
            } else {
                int nsz = this.pos * 2;
                if (nsz > this.marklimit) {
                    nsz = this.marklimit;
                }
                byte[] nbuf = new byte[nsz];
                System.arraycopy(this.buf, 0, nbuf, 0, this.pos);
                this.buf = nbuf;
            }
        }
        this.count = this.pos;
        int len = this.buf.length - this.pos;
        if ((this.bufpos - this.start) + ((long) this.pos) + ((long) len) > this.datalen) {
            len = (int) (this.datalen - ((this.bufpos - this.start) + ((long) this.pos)));
        }
        synchronized (this.f314in) {
            this.f314in.seek(this.bufpos + ((long) this.pos));
            int n = this.f314in.read(this.buf, this.pos, len);
            if (n > 0) {
                this.count = this.pos + n;
            }
        }
    }

    public synchronized int read() throws IOException {
        byte b;
        ensureOpen();
        if (this.pos >= this.count) {
            fill();
            if (this.pos >= this.count) {
                b = -1;
            }
        }
        byte[] bArr = this.buf;
        int i = this.pos;
        this.pos = i + 1;
        b = bArr[i] & Ev3Constants.Opcode.TST;
        return b;
    }

    private int read1(byte[] b, int off, int len) throws IOException {
        int cnt;
        int avail = this.count - this.pos;
        if (avail <= 0) {
            fill();
            avail = this.count - this.pos;
            if (avail <= 0) {
                return -1;
            }
        }
        if (avail < len) {
            cnt = avail;
        } else {
            cnt = len;
        }
        System.arraycopy(this.buf, this.pos, b, off, cnt);
        this.pos += cnt;
        return cnt;
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int n;
        ensureOpen();
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            n = 0;
        } else {
            n = read1(b, off, len);
            if (n > 0) {
                while (n < len) {
                    int n1 = read1(b, off + n, len - n);
                    if (n1 <= 0) {
                        break;
                    }
                    n += n1;
                }
            }
        }
        return n;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0021, code lost:
        if (r0 > 0) goto L_0x0023;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized long skip(long r8) throws java.io.IOException {
        /*
            r7 = this;
            r2 = 0
            monitor-enter(r7)
            r7.ensureOpen()     // Catch:{ all -> 0x0030 }
            int r4 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1))
            if (r4 > 0) goto L_0x000c
        L_0x000a:
            monitor-exit(r7)
            return r2
        L_0x000c:
            int r4 = r7.count     // Catch:{ all -> 0x0030 }
            int r5 = r7.pos     // Catch:{ all -> 0x0030 }
            int r4 = r4 - r5
            long r0 = (long) r4     // Catch:{ all -> 0x0030 }
            int r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r4 > 0) goto L_0x0023
            r7.fill()     // Catch:{ all -> 0x0030 }
            int r4 = r7.count     // Catch:{ all -> 0x0030 }
            int r5 = r7.pos     // Catch:{ all -> 0x0030 }
            int r4 = r4 - r5
            long r0 = (long) r4     // Catch:{ all -> 0x0030 }
            int r4 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r4 <= 0) goto L_0x000a
        L_0x0023:
            int r4 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1))
            if (r4 >= 0) goto L_0x0033
            r2 = r0
        L_0x0028:
            int r4 = r7.pos     // Catch:{ all -> 0x0030 }
            long r4 = (long) r4     // Catch:{ all -> 0x0030 }
            long r4 = r4 + r2
            int r4 = (int) r4     // Catch:{ all -> 0x0030 }
            r7.pos = r4     // Catch:{ all -> 0x0030 }
            goto L_0x000a
        L_0x0030:
            r4 = move-exception
            monitor-exit(r7)
            throw r4
        L_0x0033:
            r2 = r8
            goto L_0x0028
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.util.SharedFileInputStream.skip(long):long");
    }

    public synchronized int available() throws IOException {
        ensureOpen();
        return (this.count - this.pos) + in_available();
    }

    private int in_available() throws IOException {
        return (int) ((this.start + this.datalen) - (this.bufpos + ((long) this.count)));
    }

    public synchronized void mark(int readlimit) {
        this.marklimit = readlimit;
        this.markpos = this.pos;
    }

    public synchronized void reset() throws IOException {
        ensureOpen();
        if (this.markpos < 0) {
            throw new IOException("Resetting to invalid mark");
        }
        this.pos = this.markpos;
    }

    public boolean markSupported() {
        return true;
    }

    public void close() throws IOException {
        if (this.f314in != null) {
            try {
                if (this.master) {
                    this.f315sf.forceClose();
                } else {
                    this.f315sf.close();
                }
            } finally {
                this.f315sf = null;
                this.f314in = null;
                this.buf = null;
            }
        }
    }

    public long getPosition() {
        if (this.f314in != null) {
            return (this.bufpos + ((long) this.pos)) - this.start;
        }
        throw new RuntimeException("Stream closed");
    }

    public synchronized InputStream newStream(long start2, long end) {
        if (this.f314in == null) {
            throw new RuntimeException("Stream closed");
        } else if (start2 < 0) {
            throw new IllegalArgumentException("start < 0");
        } else {
            if (end == -1) {
                end = this.datalen;
            }
        }
        return new SharedFileInputStream(this.f315sf, this.start + start2, end - start2, this.bufsize);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
