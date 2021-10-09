package com.sun.mail.auth;

import com.google.appinventor.components.runtime.util.Ev3Constants;

public final class MD4 {
    private static final int S11 = 3;
    private static final int S12 = 7;
    private static final int S13 = 11;
    private static final int S14 = 19;
    private static final int S21 = 3;
    private static final int S22 = 5;
    private static final int S23 = 9;
    private static final int S24 = 13;
    private static final int S31 = 3;
    private static final int S32 = 9;
    private static final int S33 = 11;
    private static final int S34 = 15;
    private static final int blockSize = 64;
    private static final byte[] padding = new byte[136];
    private int bufOfs;
    private final byte[] buffer = new byte[64];
    private long bytesProcessed;
    private final int[] state = new int[4];

    /* renamed from: x */
    private final int[] f264x = new int[16];

    static {
        padding[0] = Byte.MIN_VALUE;
    }

    public MD4() {
        implReset();
    }

    public byte[] digest(byte[] in) {
        implReset();
        engineUpdate(in, 0, in.length);
        byte[] out = new byte[16];
        implDigest(out, 0);
        return out;
    }

    private void implReset() {
        this.state[0] = 1732584193;
        this.state[1] = -271733879;
        this.state[2] = -1732584194;
        this.state[3] = 271733878;
        this.bufOfs = 0;
        this.bytesProcessed = 0;
    }

    private void implDigest(byte[] out, int ofs) {
        long bitsProcessed = this.bytesProcessed << 3;
        int index = ((int) this.bytesProcessed) & 63;
        engineUpdate(padding, 0, index < 56 ? 56 - index : 120 - index);
        this.buffer[56] = (byte) ((int) bitsProcessed);
        this.buffer[57] = (byte) ((int) (bitsProcessed >> 8));
        this.buffer[58] = (byte) ((int) (bitsProcessed >> 16));
        this.buffer[59] = (byte) ((int) (bitsProcessed >> 24));
        this.buffer[60] = (byte) ((int) (bitsProcessed >> 32));
        this.buffer[61] = (byte) ((int) (bitsProcessed >> 40));
        this.buffer[62] = (byte) ((int) (bitsProcessed >> 48));
        this.buffer[63] = (byte) ((int) (bitsProcessed >> 56));
        implCompress(this.buffer, 0);
        for (int x : this.state) {
            int ofs2 = ofs + 1;
            out[ofs] = (byte) x;
            int ofs3 = ofs2 + 1;
            out[ofs2] = (byte) (x >> 8);
            int ofs4 = ofs3 + 1;
            out[ofs3] = (byte) (x >> 16);
            ofs = ofs4 + 1;
            out[ofs4] = (byte) (x >> 24);
        }
    }

    private void engineUpdate(byte[] b, int ofs, int len) {
        if (len != 0) {
            if (ofs < 0 || len < 0 || ofs > b.length - len) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (this.bytesProcessed < 0) {
                implReset();
            }
            this.bytesProcessed += (long) len;
            if (this.bufOfs != 0) {
                int n = Math.min(len, 64 - this.bufOfs);
                System.arraycopy(b, ofs, this.buffer, this.bufOfs, n);
                this.bufOfs += n;
                ofs += n;
                len -= n;
                if (this.bufOfs >= 64) {
                    implCompress(this.buffer, 0);
                    this.bufOfs = 0;
                }
            }
            while (len >= 64) {
                implCompress(b, ofs);
                len -= 64;
                ofs += 64;
            }
            if (len > 0) {
                System.arraycopy(b, ofs, this.buffer, 0, len);
                this.bufOfs = len;
            }
        }
    }

    /* renamed from: FF */
    private static int m34FF(int a, int b, int c, int d, int x, int s) {
        int a2 = a + ((b & c) | ((b ^ -1) & d)) + x;
        return (a2 << s) | (a2 >>> (32 - s));
    }

    /* renamed from: GG */
    private static int m35GG(int a, int b, int c, int d, int x, int s) {
        int a2 = a + ((b & c) | (b & d) | (c & d)) + x + 1518500249;
        return (a2 << s) | (a2 >>> (32 - s));
    }

    /* renamed from: HH */
    private static int m36HH(int a, int b, int c, int d, int x, int s) {
        int a2 = a + ((b ^ c) ^ d) + x + 1859775393;
        return (a2 << s) | (a2 >>> (32 - s));
    }

    private void implCompress(byte[] buf, int ofs) {
        for (int xfs = 0; xfs < this.f264x.length; xfs++) {
            this.f264x[xfs] = (buf[ofs] & Ev3Constants.Opcode.TST) | ((buf[ofs + 1] & Ev3Constants.Opcode.TST) << 8) | ((buf[ofs + 2] & Ev3Constants.Opcode.TST) << 16) | ((buf[ofs + 3] & Ev3Constants.Opcode.TST) << 24);
            ofs += 4;
        }
        int a = this.state[0];
        int b = this.state[1];
        int c = this.state[2];
        int d = this.state[3];
        int a2 = m34FF(a, b, c, d, this.f264x[0], 3);
        int d2 = m34FF(d, a2, b, c, this.f264x[1], 7);
        int c2 = m34FF(c, d2, a2, b, this.f264x[2], 11);
        int b2 = m34FF(b, c2, d2, a2, this.f264x[3], 19);
        int a3 = m34FF(a2, b2, c2, d2, this.f264x[4], 3);
        int d3 = m34FF(d2, a3, b2, c2, this.f264x[5], 7);
        int c3 = m34FF(c2, d3, a3, b2, this.f264x[6], 11);
        int b3 = m34FF(b2, c3, d3, a3, this.f264x[7], 19);
        int a4 = m34FF(a3, b3, c3, d3, this.f264x[8], 3);
        int d4 = m34FF(d3, a4, b3, c3, this.f264x[9], 7);
        int c4 = m34FF(c3, d4, a4, b3, this.f264x[10], 11);
        int b4 = m34FF(b3, c4, d4, a4, this.f264x[11], 19);
        int a5 = m34FF(a4, b4, c4, d4, this.f264x[12], 3);
        int d5 = m34FF(d4, a5, b4, c4, this.f264x[13], 7);
        int c5 = m34FF(c4, d5, a5, b4, this.f264x[14], 11);
        int b5 = m34FF(b4, c5, d5, a5, this.f264x[15], 19);
        int a6 = m35GG(a5, b5, c5, d5, this.f264x[0], 3);
        int d6 = m35GG(d5, a6, b5, c5, this.f264x[4], 5);
        int c6 = m35GG(c5, d6, a6, b5, this.f264x[8], 9);
        int b6 = m35GG(b5, c6, d6, a6, this.f264x[12], 13);
        int a7 = m35GG(a6, b6, c6, d6, this.f264x[1], 3);
        int d7 = m35GG(d6, a7, b6, c6, this.f264x[5], 5);
        int c7 = m35GG(c6, d7, a7, b6, this.f264x[9], 9);
        int b7 = m35GG(b6, c7, d7, a7, this.f264x[13], 13);
        int a8 = m35GG(a7, b7, c7, d7, this.f264x[2], 3);
        int d8 = m35GG(d7, a8, b7, c7, this.f264x[6], 5);
        int c8 = m35GG(c7, d8, a8, b7, this.f264x[10], 9);
        int b8 = m35GG(b7, c8, d8, a8, this.f264x[14], 13);
        int a9 = m35GG(a8, b8, c8, d8, this.f264x[3], 3);
        int d9 = m35GG(d8, a9, b8, c8, this.f264x[7], 5);
        int c9 = m35GG(c8, d9, a9, b8, this.f264x[11], 9);
        int b9 = m35GG(b8, c9, d9, a9, this.f264x[15], 13);
        int a10 = m36HH(a9, b9, c9, d9, this.f264x[0], 3);
        int d10 = m36HH(d9, a10, b9, c9, this.f264x[8], 9);
        int c10 = m36HH(c9, d10, a10, b9, this.f264x[4], 11);
        int b10 = m36HH(b9, c10, d10, a10, this.f264x[12], 15);
        int a11 = m36HH(a10, b10, c10, d10, this.f264x[2], 3);
        int d11 = m36HH(d10, a11, b10, c10, this.f264x[10], 9);
        int c11 = m36HH(c10, d11, a11, b10, this.f264x[6], 11);
        int b11 = m36HH(b10, c11, d11, a11, this.f264x[14], 15);
        int a12 = m36HH(a11, b11, c11, d11, this.f264x[1], 3);
        int d12 = m36HH(d11, a12, b11, c11, this.f264x[9], 9);
        int c12 = m36HH(c11, d12, a12, b11, this.f264x[5], 11);
        int b12 = m36HH(b11, c12, d12, a12, this.f264x[13], 15);
        int a13 = m36HH(a12, b12, c12, d12, this.f264x[3], 3);
        int d13 = m36HH(d12, a13, b12, c12, this.f264x[11], 9);
        int c13 = m36HH(c12, d13, a13, b12, this.f264x[7], 11);
        int b13 = m36HH(b12, c13, d13, a13, this.f264x[15], 15);
        int[] iArr = this.state;
        iArr[0] = iArr[0] + a13;
        int[] iArr2 = this.state;
        iArr2[1] = iArr2[1] + b13;
        int[] iArr3 = this.state;
        iArr3[2] = iArr3[2] + c13;
        int[] iArr4 = this.state;
        iArr4[3] = iArr4[3] + d13;
    }
}
