package com.sun.mail.imap.protocol;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import gnu.bytecode.Access;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

public class BASE64MailboxEncoder {
    private static final char[] pem_array = {'A', 'B', Access.CLASS_CONTEXT, 'D', 'E', Access.FIELD_CONTEXT, 'G', 'H', Access.INNERCLASS_CONTEXT, 'J', 'K', 'L', Access.METHOD_CONTEXT, 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', ','};
    protected byte[] buffer = new byte[4];
    protected int bufsize = 0;
    protected Writer out = null;
    protected boolean started = false;

    public static String encode(String original) {
        BASE64MailboxEncoder base64stream = null;
        boolean changedString = false;
        CharArrayWriter writer = new CharArrayWriter(length);
        for (char current : original.toCharArray()) {
            if (current < ' ' || current > '~') {
                if (base64stream == null) {
                    base64stream = new BASE64MailboxEncoder(writer);
                    changedString = true;
                }
                base64stream.write(current);
            } else {
                if (base64stream != null) {
                    base64stream.flush();
                }
                if (current == '&') {
                    changedString = true;
                    writer.write(38);
                    writer.write(45);
                } else {
                    writer.write(current);
                }
            }
        }
        if (base64stream != null) {
            base64stream.flush();
        }
        if (changedString) {
            return writer.toString();
        }
        return original;
    }

    public BASE64MailboxEncoder(Writer what) {
        this.out = what;
    }

    public void write(int c) {
        try {
            if (!this.started) {
                this.started = true;
                this.out.write(38);
            }
            byte[] bArr = this.buffer;
            int i = this.bufsize;
            this.bufsize = i + 1;
            bArr[i] = (byte) (c >> 8);
            byte[] bArr2 = this.buffer;
            int i2 = this.bufsize;
            this.bufsize = i2 + 1;
            bArr2[i2] = (byte) (c & 255);
            if (this.bufsize >= 3) {
                encode();
                this.bufsize -= 3;
            }
        } catch (IOException e) {
        }
    }

    public void flush() {
        try {
            if (this.bufsize > 0) {
                encode();
                this.bufsize = 0;
            }
            if (this.started) {
                this.out.write(45);
                this.started = false;
            }
        } catch (IOException e) {
        }
    }

    /* access modifiers changed from: protected */
    public void encode() throws IOException {
        if (this.bufsize == 1) {
            byte a = this.buffer[0];
            this.out.write(pem_array[(a >>> 2) & 63]);
            this.out.write(pem_array[((a << 4) & 48) + 0]);
        } else if (this.bufsize == 2) {
            byte a2 = this.buffer[0];
            byte b = this.buffer[1];
            this.out.write(pem_array[(a2 >>> 2) & 63]);
            this.out.write(pem_array[((a2 << 4) & 48) + ((b >>> 4) & 15)]);
            this.out.write(pem_array[((b << 2) & 60) + 0]);
        } else {
            byte a3 = this.buffer[0];
            byte b2 = this.buffer[1];
            byte c = this.buffer[2];
            this.out.write(pem_array[(a3 >>> 2) & 63]);
            this.out.write(pem_array[((a3 << 4) & 48) + ((b2 >>> 4) & 15)]);
            this.out.write(pem_array[((b2 << 2) & 60) + ((c >>> 6) & 3)]);
            this.out.write(pem_array[c & Ev3Constants.Opcode.MOVEF_F]);
            if (this.bufsize == 4) {
                this.buffer[0] = this.buffer[3];
            }
        }
    }
}
