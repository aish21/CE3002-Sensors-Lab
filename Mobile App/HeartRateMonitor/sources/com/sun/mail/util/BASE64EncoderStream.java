package com.sun.mail.util;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import gnu.bytecode.Access;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BASE64EncoderStream extends FilterOutputStream {
    private static byte[] newline = {13, 10};
    private static final char[] pem_array = {'A', 'B', Access.CLASS_CONTEXT, 'D', 'E', Access.FIELD_CONTEXT, 'G', 'H', Access.INNERCLASS_CONTEXT, 'J', 'K', 'L', Access.METHOD_CONTEXT, 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private byte[] buffer;
    private int bufsize;
    private int bytesPerLine;
    private int count;
    private int lineLimit;
    private boolean noCRLF;
    private byte[] outbuf;

    public BASE64EncoderStream(OutputStream out, int bytesPerLine2) {
        super(out);
        this.bufsize = 0;
        this.count = 0;
        this.noCRLF = false;
        this.buffer = new byte[3];
        if (bytesPerLine2 == Integer.MAX_VALUE || bytesPerLine2 < 4) {
            this.noCRLF = true;
            bytesPerLine2 = 76;
        }
        int bytesPerLine3 = (bytesPerLine2 / 4) * 4;
        this.bytesPerLine = bytesPerLine3;
        this.lineLimit = (bytesPerLine3 / 4) * 3;
        if (this.noCRLF) {
            this.outbuf = new byte[bytesPerLine3];
            return;
        }
        this.outbuf = new byte[(bytesPerLine3 + 2)];
        this.outbuf[bytesPerLine3] = 13;
        this.outbuf[bytesPerLine3 + 1] = 10;
    }

    public BASE64EncoderStream(OutputStream out) {
        this(out, 76);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r0 = ((r8.bytesPerLine - r8.count) / 4) * 3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        if ((r2 + r0) > r1) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        r3 = encodedSize(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0026, code lost:
        if (r8.noCRLF != false) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        r4 = r3 + 1;
        r8.outbuf[r3] = 13;
        r3 = r4 + 1;
        r8.outbuf[r4] = 10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
        r8.out.write(encode(r9, r2, r0, r8.outbuf), 0, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0044, code lost:
        r10 = r2 + r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r8.count = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004c, code lost:
        if ((r8.lineLimit + r10) > r1) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004e, code lost:
        r8.out.write(encode(r9, r10, r8.lineLimit, r8.outbuf));
        r10 = r10 + r8.lineLimit;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0061, code lost:
        if ((r10 + 3) > r1) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0063, code lost:
        r0 = ((r1 - r10) / 3) * 3;
        r3 = encodedSize(r0);
        r8.out.write(encode(r9, r10, r0, r8.outbuf), 0, r3);
        r10 = r10 + r0;
        r8.count += r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007f, code lost:
        if (r10 >= r1) goto L_0x0089;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0081, code lost:
        write((int) r9[r10]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0086, code lost:
        r10 = r10 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x008a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0091, code lost:
        r10 = r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void write(byte[] r9, int r10, int r11) throws java.io.IOException {
        /*
            r8 = this;
            monitor-enter(r8)
            int r1 = r10 + r11
            r2 = r10
        L_0x0004:
            int r5 = r8.bufsize     // Catch:{ all -> 0x008b }
            if (r5 == 0) goto L_0x0013
            if (r2 >= r1) goto L_0x0013
            int r10 = r2 + 1
            byte r5 = r9[r2]     // Catch:{ all -> 0x008f }
            r8.write((int) r5)     // Catch:{ all -> 0x008f }
            r2 = r10
            goto L_0x0004
        L_0x0013:
            int r5 = r8.bytesPerLine     // Catch:{ all -> 0x008b }
            int r6 = r8.count     // Catch:{ all -> 0x008b }
            int r5 = r5 - r6
            int r5 = r5 / 4
            int r0 = r5 * 3
            int r5 = r2 + r0
            if (r5 > r1) goto L_0x0091
            int r3 = encodedSize(r0)     // Catch:{ all -> 0x008b }
            boolean r5 = r8.noCRLF     // Catch:{ all -> 0x008b }
            if (r5 != 0) goto L_0x0038
            byte[] r5 = r8.outbuf     // Catch:{ all -> 0x008b }
            int r4 = r3 + 1
            r6 = 13
            r5[r3] = r6     // Catch:{ all -> 0x008b }
            byte[] r5 = r8.outbuf     // Catch:{ all -> 0x008b }
            int r3 = r4 + 1
            r6 = 10
            r5[r4] = r6     // Catch:{ all -> 0x008b }
        L_0x0038:
            java.io.OutputStream r5 = r8.out     // Catch:{ all -> 0x008b }
            byte[] r6 = r8.outbuf     // Catch:{ all -> 0x008b }
            byte[] r6 = encode(r9, r2, r0, r6)     // Catch:{ all -> 0x008b }
            r7 = 0
            r5.write(r6, r7, r3)     // Catch:{ all -> 0x008b }
            int r10 = r2 + r0
            r5 = 0
            r8.count = r5     // Catch:{ all -> 0x008f }
        L_0x0049:
            int r5 = r8.lineLimit     // Catch:{ all -> 0x008f }
            int r5 = r5 + r10
            if (r5 > r1) goto L_0x005f
            java.io.OutputStream r5 = r8.out     // Catch:{ all -> 0x008f }
            int r6 = r8.lineLimit     // Catch:{ all -> 0x008f }
            byte[] r7 = r8.outbuf     // Catch:{ all -> 0x008f }
            byte[] r6 = encode(r9, r10, r6, r7)     // Catch:{ all -> 0x008f }
            r5.write(r6)     // Catch:{ all -> 0x008f }
            int r5 = r8.lineLimit     // Catch:{ all -> 0x008f }
            int r10 = r10 + r5
            goto L_0x0049
        L_0x005f:
            int r5 = r10 + 3
            if (r5 > r1) goto L_0x007f
            int r0 = r1 - r10
            int r5 = r0 / 3
            int r0 = r5 * 3
            int r3 = encodedSize(r0)     // Catch:{ all -> 0x008f }
            java.io.OutputStream r5 = r8.out     // Catch:{ all -> 0x008f }
            byte[] r6 = r8.outbuf     // Catch:{ all -> 0x008f }
            byte[] r6 = encode(r9, r10, r0, r6)     // Catch:{ all -> 0x008f }
            r7 = 0
            r5.write(r6, r7, r3)     // Catch:{ all -> 0x008f }
            int r10 = r10 + r0
            int r5 = r8.count     // Catch:{ all -> 0x008f }
            int r5 = r5 + r3
            r8.count = r5     // Catch:{ all -> 0x008f }
        L_0x007f:
            if (r10 >= r1) goto L_0x0089
            byte r5 = r9[r10]     // Catch:{ all -> 0x008f }
            r8.write((int) r5)     // Catch:{ all -> 0x008f }
            int r10 = r10 + 1
            goto L_0x007f
        L_0x0089:
            monitor-exit(r8)
            return
        L_0x008b:
            r5 = move-exception
            r10 = r2
        L_0x008d:
            monitor-exit(r8)
            throw r5
        L_0x008f:
            r5 = move-exception
            goto L_0x008d
        L_0x0091:
            r10 = r2
            goto L_0x0049
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.BASE64EncoderStream.write(byte[], int, int):void");
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public synchronized void write(int c) throws IOException {
        byte[] bArr = this.buffer;
        int i = this.bufsize;
        this.bufsize = i + 1;
        bArr[i] = (byte) c;
        if (this.bufsize == 3) {
            encode();
            this.bufsize = 0;
        }
    }

    public synchronized void flush() throws IOException {
        if (this.bufsize > 0) {
            encode();
            this.bufsize = 0;
        }
        this.out.flush();
    }

    public synchronized void close() throws IOException {
        flush();
        if (this.count > 0 && !this.noCRLF) {
            this.out.write(newline);
            this.out.flush();
        }
        this.out.close();
    }

    private void encode() throws IOException {
        int osize = encodedSize(this.bufsize);
        this.out.write(encode(this.buffer, 0, this.bufsize, this.outbuf), 0, osize);
        this.count += osize;
        if (this.count >= this.bytesPerLine) {
            if (!this.noCRLF) {
                this.out.write(newline);
            }
            this.count = 0;
        }
    }

    public static byte[] encode(byte[] inbuf) {
        return inbuf.length == 0 ? inbuf : encode(inbuf, 0, inbuf.length, (byte[]) null);
    }

    private static byte[] encode(byte[] inbuf, int off, int size, byte[] outbuf2) {
        int inpos;
        if (outbuf2 == null) {
            outbuf2 = new byte[encodedSize(size)];
        }
        int inpos2 = off;
        int outpos = 0;
        while (true) {
            inpos = inpos2;
            if (size < 3) {
                break;
            }
            int inpos3 = inpos + 1;
            int inpos4 = inpos3 + 1;
            inpos2 = inpos4 + 1;
            int val = ((((inbuf[inpos] & 255) << 8) | (inbuf[inpos3] & 255)) << 8) | (inbuf[inpos4] & 255);
            outbuf2[outpos + 3] = (byte) pem_array[val & 63];
            int val2 = val >> 6;
            outbuf2[outpos + 2] = (byte) pem_array[val2 & 63];
            int val3 = val2 >> 6;
            outbuf2[outpos + 1] = (byte) pem_array[val3 & 63];
            outbuf2[outpos + 0] = (byte) pem_array[(val3 >> 6) & 63];
            size -= 3;
            outpos += 4;
        }
        if (size == 1) {
            int i = inpos + 1;
            int val4 = (inbuf[inpos] & 255) << 4;
            outbuf2[outpos + 3] = Ev3Constants.Opcode.MOVEF_16;
            outbuf2[outpos + 2] = Ev3Constants.Opcode.MOVEF_16;
            outbuf2[outpos + 1] = (byte) pem_array[val4 & 63];
            outbuf2[outpos + 0] = (byte) pem_array[(val4 >> 6) & 63];
        } else {
            if (size == 2) {
                int inpos5 = inpos + 1;
                inpos = inpos5 + 1;
                int val5 = (((inbuf[inpos] & 255) << 8) | (inbuf[inpos5] & 255)) << 2;
                outbuf2[outpos + 3] = Ev3Constants.Opcode.MOVEF_16;
                outbuf2[outpos + 2] = (byte) pem_array[val5 & 63];
                int val6 = val5 >> 6;
                outbuf2[outpos + 1] = (byte) pem_array[val6 & 63];
                outbuf2[outpos + 0] = (byte) pem_array[(val6 >> 6) & 63];
            }
            int i2 = inpos;
        }
        return outbuf2;
    }

    private static int encodedSize(int size) {
        return ((size + 2) / 3) * 4;
    }
}
