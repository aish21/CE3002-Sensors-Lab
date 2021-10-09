package com.sun.mail.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class LineInputStream extends FilterInputStream {
    private static int MAX_INCR = 1048576;
    private static boolean defaultutf8 = PropUtil.getBooleanSystemProperty("mail.mime.allowutf8", false);
    private boolean allowutf8;
    private CharsetDecoder decoder;
    private byte[] lineBuffer;

    public LineInputStream(InputStream in) {
        this(in, false);
    }

    public LineInputStream(InputStream in, boolean allowutf82) {
        super(in);
        this.lineBuffer = null;
        this.allowutf8 = allowutf82;
        if (!allowutf82 && defaultutf8) {
            this.decoder = StandardCharsets.UTF_8.newDecoder();
            this.decoder.onMalformedInput(CodingErrorAction.REPORT);
            this.decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        }
    }

    public String readLine() throws IOException {
        byte[] buf;
        int c1;
        byte[] buf2 = this.lineBuffer;
        if (buf2 == null) {
            buf2 = new byte[128];
            this.lineBuffer = buf2;
        }
        int room = buf.length;
        int offset = 0;
        while (true) {
            c1 = this.in.read();
            if (c1 == -1 || c1 == 10) {
                break;
            } else if (c1 == 13) {
                boolean twoCRs = false;
                if (this.in.markSupported()) {
                    this.in.mark(2);
                }
                int c2 = this.in.read();
                if (c2 == 13) {
                    twoCRs = true;
                    c2 = this.in.read();
                }
                if (c2 != 10) {
                    if (this.in.markSupported()) {
                        this.in.reset();
                    } else {
                        if (!(this.in instanceof PushbackInputStream)) {
                            this.in = new PushbackInputStream(this.in, 2);
                        }
                        if (c2 != -1) {
                            ((PushbackInputStream) this.in).unread(c2);
                        }
                        if (twoCRs) {
                            ((PushbackInputStream) this.in).unread(13);
                        }
                    }
                }
            } else {
                room--;
                if (room < 0) {
                    if (buf.length < MAX_INCR) {
                        buf = new byte[(buf.length * 2)];
                    } else {
                        buf = new byte[(buf.length + MAX_INCR)];
                    }
                    room = (buf.length - offset) - 1;
                    System.arraycopy(this.lineBuffer, 0, buf, 0, offset);
                    this.lineBuffer = buf;
                }
                buf[offset] = (byte) c1;
                offset++;
            }
        }
        if (c1 == -1 && offset == 0) {
            return null;
        }
        if (this.allowutf8) {
            return new String(buf, 0, offset, StandardCharsets.UTF_8);
        }
        if (defaultutf8) {
            try {
                return this.decoder.decode(ByteBuffer.wrap(buf, 0, offset)).toString();
            } catch (CharacterCodingException e) {
            }
        }
        return new String(buf, 0, 0, offset);
    }
}
