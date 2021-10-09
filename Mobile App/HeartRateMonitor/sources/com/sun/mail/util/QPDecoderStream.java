package com.sun.mail.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class QPDecoderStream extends FilterInputStream {

    /* renamed from: ba */
    protected byte[] f289ba = new byte[2];
    protected int spaces = 0;

    public QPDecoderStream(InputStream in) {
        super(new PushbackInputStream(in, 2));
    }

    public int read() throws IOException {
        int c;
        if (this.spaces > 0) {
            this.spaces--;
            return 32;
        }
        int c2 = this.in.read();
        if (c2 == 32) {
            while (true) {
                c = this.in.read();
                if (c != 32) {
                    break;
                }
                this.spaces++;
            }
            if (c == 13 || c == 10 || c == -1) {
                this.spaces = 0;
                return c;
            }
            ((PushbackInputStream) this.in).unread(c);
            return 32;
        } else if (c2 != 61) {
            return c2;
        } else {
            int a = this.in.read();
            if (a == 10) {
                return read();
            }
            if (a == 13) {
                int b = this.in.read();
                if (b != 10) {
                    ((PushbackInputStream) this.in).unread(b);
                }
                return read();
            } else if (a == -1) {
                return -1;
            } else {
                this.f289ba[0] = (byte) a;
                this.f289ba[1] = (byte) this.in.read();
                try {
                    return ASCIIUtility.parseInt(this.f289ba, 0, 2, 16);
                } catch (NumberFormatException e) {
                    ((PushbackInputStream) this.in).unread(this.f289ba);
                    return c2;
                }
            }
        }
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        int i = 0;
        while (i < len) {
            int c = read();
            if (c != -1) {
                buf[off + i] = (byte) c;
                i++;
            } else if (i == 0) {
                return -1;
            } else {
                return i;
            }
        }
        return i;
    }

    public long skip(long n) throws IOException {
        long skipped = 0;
        while (true) {
            long n2 = n;
            n = n2 - 1;
            if (n2 <= 0 || read() < 0) {
                return skipped;
            }
            skipped++;
        }
        return skipped;
    }

    public boolean markSupported() {
        return false;
    }

    public int available() throws IOException {
        return this.in.available();
    }
}
