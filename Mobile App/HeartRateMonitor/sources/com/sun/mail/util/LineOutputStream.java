package com.sun.mail.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LineOutputStream extends FilterOutputStream {
    private static byte[] newline = new byte[2];
    private boolean allowutf8;

    static {
        newline[0] = 13;
        newline[1] = 10;
    }

    public LineOutputStream(OutputStream out) {
        this(out, false);
    }

    public LineOutputStream(OutputStream out, boolean allowutf82) {
        super(out);
        this.allowutf8 = allowutf82;
    }

    public void writeln(String s) throws IOException {
        byte[] bytes;
        if (this.allowutf8) {
            bytes = s.getBytes(StandardCharsets.UTF_8);
        } else {
            bytes = ASCIIUtility.getBytes(s);
        }
        this.out.write(bytes);
        this.out.write(newline);
    }

    public void writeln() throws IOException {
        this.out.write(newline);
    }
}
