package com.sun.mail.handlers;

import com.google.appinventor.components.runtime.util.NanoHTTPD;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;

public class text_plain extends handler_base {
    private static ActivationDataFlavor[] myDF = {new ActivationDataFlavor(String.class, NanoHTTPD.MIME_PLAINTEXT, "Text String")};

    private static class NoCloseOutputStream extends FilterOutputStream {
        public NoCloseOutputStream(OutputStream os) {
            super(os);
        }

        public void close() {
        }
    }

    /* access modifiers changed from: protected */
    public ActivationDataFlavor[] getDataFlavors() {
        return myDF;
    }

    public Object getContent(DataSource ds) throws IOException {
        int size;
        String enc = null;
        try {
            enc = getCharset(ds.getContentType());
            InputStreamReader is = new InputStreamReader(ds.getInputStream(), enc);
            int pos = 0;
            try {
                char[] buf = new char[1024];
                while (true) {
                    int count = is.read(buf, pos, buf.length - pos);
                    if (count == -1) {
                        break;
                    }
                    pos += count;
                    if (pos >= buf.length) {
                        int size2 = buf.length;
                        if (size2 < 262144) {
                            size = size2 + size2;
                        } else {
                            size = size2 + 262144;
                        }
                        char[] tbuf = new char[size];
                        System.arraycopy(buf, 0, tbuf, 0, pos);
                        buf = tbuf;
                    }
                }
                return new String(buf, 0, pos);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        } catch (IllegalArgumentException e2) {
            throw new UnsupportedEncodingException(enc);
        }
    }

    public void writeTo(Object obj, String type, OutputStream os) throws IOException {
        if (!(obj instanceof String)) {
            throw new IOException("\"" + getDataFlavors()[0].getMimeType() + "\" DataContentHandler requires String object, was given object of type " + obj.getClass().toString());
        }
        String enc = null;
        try {
            enc = getCharset(type);
            OutputStreamWriter osw = new OutputStreamWriter(new NoCloseOutputStream(os), enc);
            String s = (String) obj;
            osw.write(s, 0, s.length());
            osw.close();
        } catch (IllegalArgumentException e) {
            throw new UnsupportedEncodingException(enc);
        }
    }

    private String getCharset(String type) {
        try {
            String charset = new ContentType(type).getParameter("charset");
            if (charset == null) {
                charset = "us-ascii";
            }
            return MimeUtility.javaCharset(charset);
        } catch (Exception e) {
            return null;
        }
    }
}
