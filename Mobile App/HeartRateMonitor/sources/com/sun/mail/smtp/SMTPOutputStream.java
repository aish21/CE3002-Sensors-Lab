package com.sun.mail.smtp;

import com.sun.mail.util.CRLFOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SMTPOutputStream extends CRLFOutputStream {
    public SMTPOutputStream(OutputStream os) {
        super(os);
    }

    public void write(int b) throws IOException {
        if ((this.lastb == 10 || this.lastb == 13 || this.lastb == -1) && b == 46) {
            this.out.write(46);
        }
        super.write(b);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: byte} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void write(byte[] r8, int r9, int r10) throws java.io.IOException {
        /*
            r7 = this;
            r6 = 46
            r3 = 10
            int r4 = r7.lastb
            r5 = -1
            if (r4 != r5) goto L_0x0029
            r1 = r3
        L_0x000a:
            r2 = r9
            int r10 = r10 + r9
            r0 = r9
        L_0x000d:
            if (r0 >= r10) goto L_0x002c
            if (r1 == r3) goto L_0x0015
            r4 = 13
            if (r1 != r4) goto L_0x0024
        L_0x0015:
            byte r4 = r8[r0]
            if (r4 != r6) goto L_0x0024
            int r4 = r0 - r2
            super.write(r8, r2, r4)
            java.io.OutputStream r4 = r7.out
            r4.write(r6)
            r2 = r0
        L_0x0024:
            byte r1 = r8[r0]
            int r0 = r0 + 1
            goto L_0x000d
        L_0x0029:
            int r1 = r7.lastb
            goto L_0x000a
        L_0x002c:
            int r3 = r10 - r2
            if (r3 <= 0) goto L_0x0035
            int r3 = r10 - r2
            super.write(r8, r2, r3)
        L_0x0035:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPOutputStream.write(byte[], int, int):void");
    }

    public void flush() {
    }

    public void ensureAtBOL() throws IOException {
        if (!this.atBOL) {
            super.writeln();
        }
    }
}
