package javax.mail.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

public class ByteArrayDataSource implements DataSource {
    private byte[] data;
    private int len = -1;
    private String name = "";
    private String type;

    static class DSByteArrayOutputStream extends ByteArrayOutputStream {
        DSByteArrayOutputStream() {
        }

        public byte[] getBuf() {
            return this.buf;
        }

        public int getCount() {
            return this.count;
        }
    }

    public ByteArrayDataSource(InputStream is, String type2) throws IOException {
        DSByteArrayOutputStream os = new DSByteArrayOutputStream();
        byte[] buf = new byte[8192];
        while (true) {
            int len2 = is.read(buf);
            if (len2 <= 0) {
                break;
            }
            os.write(buf, 0, len2);
        }
        this.data = os.getBuf();
        this.len = os.getCount();
        if (this.data.length - this.len > 262144) {
            this.data = os.toByteArray();
            this.len = this.data.length;
        }
        this.type = type2;
    }

    public ByteArrayDataSource(byte[] data2, String type2) {
        this.data = data2;
        this.type = type2;
    }

    public ByteArrayDataSource(String data2, String type2) throws IOException {
        String charset = null;
        try {
            charset = new ContentType(type2).getParameter("charset");
        } catch (ParseException e) {
        }
        String charset2 = MimeUtility.javaCharset(charset);
        this.data = data2.getBytes(charset2 == null ? MimeUtility.getDefaultJavaCharset() : charset2);
        this.type = type2;
    }

    public InputStream getInputStream() throws IOException {
        if (this.data == null) {
            throw new IOException("no data");
        }
        if (this.len < 0) {
            this.len = this.data.length;
        }
        return new SharedByteArrayInputStream(this.data, 0, this.len);
    }

    public OutputStream getOutputStream() throws IOException {
        throw new IOException("cannot do this");
    }

    public String getContentType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }
}
