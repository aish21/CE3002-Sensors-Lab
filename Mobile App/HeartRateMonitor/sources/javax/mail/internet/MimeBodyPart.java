package javax.mail.internet;

import com.google.appinventor.components.runtime.util.NanoHTTPD;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.MessageRemovedIOException;
import com.sun.mail.util.MimeUtil;
import com.sun.mail.util.PropUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.EncodingAware;
import javax.mail.FolderClosedException;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.HeaderTokenizer;

public class MimeBodyPart extends BodyPart implements MimePart {
    private static final boolean allowutf8 = PropUtil.getBooleanSystemProperty("mail.mime.allowutf8", true);
    static final boolean cacheMultipart = PropUtil.getBooleanSystemProperty("mail.mime.cachemultipart", true);
    private static final boolean decodeFileName = PropUtil.getBooleanSystemProperty("mail.mime.decodefilename", false);
    private static final boolean encodeFileName = PropUtil.getBooleanSystemProperty("mail.mime.encodefilename", false);
    private static final boolean ignoreMultipartEncoding = PropUtil.getBooleanSystemProperty("mail.mime.ignoremultipartencoding", true);
    private static final boolean setContentTypeFileName = PropUtil.getBooleanSystemProperty("mail.mime.setcontenttypefilename", true);
    private static final boolean setDefaultTextCharset = PropUtil.getBooleanSystemProperty("mail.mime.setdefaulttextcharset", true);
    protected Object cachedContent;
    protected byte[] content;
    protected InputStream contentStream;

    /* renamed from: dh */
    protected DataHandler f302dh;
    protected InternetHeaders headers;

    public MimeBodyPart() {
        this.headers = new InternetHeaders();
    }

    public MimeBodyPart(InputStream is) throws MessagingException {
        if (!(is instanceof ByteArrayInputStream) && !(is instanceof BufferedInputStream) && !(is instanceof SharedInputStream)) {
            is = new BufferedInputStream(is);
        }
        this.headers = new InternetHeaders(is);
        if (is instanceof SharedInputStream) {
            SharedInputStream sis = (SharedInputStream) is;
            this.contentStream = sis.newStream(sis.getPosition(), -1);
            return;
        }
        try {
            this.content = ASCIIUtility.getBytes(is);
        } catch (IOException ioex) {
            throw new MessagingException("Error reading input stream", ioex);
        }
    }

    public MimeBodyPart(InternetHeaders headers2, byte[] content2) throws MessagingException {
        this.headers = headers2;
        this.content = content2;
    }

    public int getSize() throws MessagingException {
        if (this.content != null) {
            return this.content.length;
        }
        if (this.contentStream != null) {
            try {
                int size = this.contentStream.available();
                if (size <= 0) {
                    return -1;
                }
                return size;
            } catch (IOException e) {
            }
        }
        return -1;
    }

    public int getLineCount() throws MessagingException {
        return -1;
    }

    public String getContentType() throws MessagingException {
        String s = MimeUtil.cleanContentType(this, getHeader("Content-Type", (String) null));
        if (s == null) {
            return NanoHTTPD.MIME_PLAINTEXT;
        }
        return s;
    }

    public boolean isMimeType(String mimeType) throws MessagingException {
        return isMimeType(this, mimeType);
    }

    public String getDisposition() throws MessagingException {
        return getDisposition(this);
    }

    public void setDisposition(String disposition) throws MessagingException {
        setDisposition(this, disposition);
    }

    public String getEncoding() throws MessagingException {
        return getEncoding(this);
    }

    public String getContentID() throws MessagingException {
        return getHeader("Content-Id", (String) null);
    }

    public void setContentID(String cid) throws MessagingException {
        if (cid == null) {
            removeHeader("Content-ID");
        } else {
            setHeader("Content-ID", cid);
        }
    }

    public String getContentMD5() throws MessagingException {
        return getHeader("Content-MD5", (String) null);
    }

    public void setContentMD5(String md5) throws MessagingException {
        setHeader("Content-MD5", md5);
    }

    public String[] getContentLanguage() throws MessagingException {
        return getContentLanguage(this);
    }

    public void setContentLanguage(String[] languages) throws MessagingException {
        setContentLanguage(this, languages);
    }

    public String getDescription() throws MessagingException {
        return getDescription(this);
    }

    public void setDescription(String description) throws MessagingException {
        setDescription(description, (String) null);
    }

    public void setDescription(String description, String charset) throws MessagingException {
        setDescription(this, description, charset);
    }

    public String getFileName() throws MessagingException {
        return getFileName(this);
    }

    public void setFileName(String filename) throws MessagingException {
        setFileName(this, filename);
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        return getDataHandler().getInputStream();
    }

    /* access modifiers changed from: protected */
    public InputStream getContentStream() throws MessagingException {
        if (this.contentStream != null) {
            return ((SharedInputStream) this.contentStream).newStream(0, -1);
        }
        if (this.content != null) {
            return new ByteArrayInputStream(this.content);
        }
        throw new MessagingException("No MimeBodyPart content");
    }

    public InputStream getRawInputStream() throws MessagingException {
        return getContentStream();
    }

    public DataHandler getDataHandler() throws MessagingException {
        if (this.f302dh == null) {
            this.f302dh = new MimePartDataHandler(this);
        }
        return this.f302dh;
    }

    public Object getContent() throws IOException, MessagingException {
        if (this.cachedContent != null) {
            return this.cachedContent;
        }
        try {
            Object c = getDataHandler().getContent();
            if (!cacheMultipart) {
                return c;
            }
            if (!(c instanceof Multipart) && !(c instanceof Message)) {
                return c;
            }
            if (this.content == null && this.contentStream == null) {
                return c;
            }
            this.cachedContent = c;
            if (!(c instanceof MimeMultipart)) {
                return c;
            }
            ((MimeMultipart) c).parse();
            return c;
        } catch (FolderClosedIOException fex) {
            throw new FolderClosedException(fex.getFolder(), fex.getMessage());
        } catch (MessageRemovedIOException mex) {
            throw new MessageRemovedException(mex.getMessage());
        }
    }

    public void setDataHandler(DataHandler dh) throws MessagingException {
        this.f302dh = dh;
        this.cachedContent = null;
        invalidateContentHeaders(this);
    }

    public void setContent(Object o, String type) throws MessagingException {
        if (o instanceof Multipart) {
            setContent((Multipart) o);
        } else {
            setDataHandler(new DataHandler(o, type));
        }
    }

    public void setText(String text) throws MessagingException {
        setText(text, (String) null);
    }

    public void setText(String text, String charset) throws MessagingException {
        setText(this, text, charset, "plain");
    }

    public void setText(String text, String charset, String subtype) throws MessagingException {
        setText(this, text, charset, subtype);
    }

    public void setContent(Multipart mp) throws MessagingException {
        setDataHandler(new DataHandler(mp, mp.getContentType()));
        mp.setParent(this);
    }

    public void attachFile(File file) throws IOException, MessagingException {
        FileDataSource fds = new FileDataSource(file);
        setDataHandler(new DataHandler((DataSource) fds));
        setFileName(fds.getName());
        setDisposition(Part.ATTACHMENT);
    }

    public void attachFile(String file) throws IOException, MessagingException {
        attachFile(new File(file));
    }

    public void attachFile(File file, String contentType, String encoding) throws IOException, MessagingException {
        DataSource fds = new EncodedFileDataSource(file, contentType, encoding);
        setDataHandler(new DataHandler(fds));
        setFileName(fds.getName());
        setDisposition(Part.ATTACHMENT);
    }

    public void attachFile(String file, String contentType, String encoding) throws IOException, MessagingException {
        attachFile(new File(file), contentType, encoding);
    }

    private static class EncodedFileDataSource extends FileDataSource implements EncodingAware {
        private String contentType;
        private String encoding;

        public EncodedFileDataSource(File file, String contentType2, String encoding2) {
            super(file);
            this.contentType = contentType2;
            this.encoding = encoding2;
        }

        public String getContentType() {
            return this.contentType != null ? this.contentType : super.getContentType();
        }

        public String getEncoding() {
            return this.encoding;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0023 A[SYNTHETIC, Splitter:B:11:0x0023] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0028 A[SYNTHETIC, Splitter:B:14:0x0028] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void saveFile(java.io.File r8) throws java.io.IOException, javax.mail.MessagingException {
        /*
            r7 = this;
            r3 = 0
            r1 = 0
            java.io.BufferedOutputStream r4 = new java.io.BufferedOutputStream     // Catch:{ all -> 0x003f }
            java.io.FileOutputStream r5 = new java.io.FileOutputStream     // Catch:{ all -> 0x003f }
            r5.<init>(r8)     // Catch:{ all -> 0x003f }
            r4.<init>(r5)     // Catch:{ all -> 0x003f }
            java.io.InputStream r1 = r7.getInputStream()     // Catch:{ all -> 0x001f }
            r5 = 8192(0x2000, float:1.14794E-41)
            byte[] r0 = new byte[r5]     // Catch:{ all -> 0x001f }
        L_0x0014:
            int r2 = r1.read(r0)     // Catch:{ all -> 0x001f }
            if (r2 <= 0) goto L_0x002c
            r5 = 0
            r4.write(r0, r5, r2)     // Catch:{ all -> 0x001f }
            goto L_0x0014
        L_0x001f:
            r5 = move-exception
            r3 = r4
        L_0x0021:
            if (r1 == 0) goto L_0x0026
            r1.close()     // Catch:{ IOException -> 0x003b }
        L_0x0026:
            if (r3 == 0) goto L_0x002b
            r3.close()     // Catch:{ IOException -> 0x003d }
        L_0x002b:
            throw r5
        L_0x002c:
            if (r1 == 0) goto L_0x0031
            r1.close()     // Catch:{ IOException -> 0x0037 }
        L_0x0031:
            if (r4 == 0) goto L_0x0036
            r4.close()     // Catch:{ IOException -> 0x0039 }
        L_0x0036:
            return
        L_0x0037:
            r5 = move-exception
            goto L_0x0031
        L_0x0039:
            r5 = move-exception
            goto L_0x0036
        L_0x003b:
            r6 = move-exception
            goto L_0x0026
        L_0x003d:
            r6 = move-exception
            goto L_0x002b
        L_0x003f:
            r5 = move-exception
            goto L_0x0021
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeBodyPart.saveFile(java.io.File):void");
    }

    public void saveFile(String file) throws IOException, MessagingException {
        saveFile(new File(file));
    }

    public void writeTo(OutputStream os) throws IOException, MessagingException {
        writeTo(this, os, (String[]) null);
    }

    public String[] getHeader(String name) throws MessagingException {
        return this.headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        return this.headers.getHeader(name, delimiter);
    }

    public void setHeader(String name, String value) throws MessagingException {
        this.headers.setHeader(name, value);
    }

    public void addHeader(String name, String value) throws MessagingException {
        this.headers.addHeader(name, value);
    }

    public void removeHeader(String name) throws MessagingException {
        this.headers.removeHeader(name);
    }

    public Enumeration<Header> getAllHeaders() throws MessagingException {
        return this.headers.getAllHeaders();
    }

    public Enumeration<Header> getMatchingHeaders(String[] names) throws MessagingException {
        return this.headers.getMatchingHeaders(names);
    }

    public Enumeration<Header> getNonMatchingHeaders(String[] names) throws MessagingException {
        return this.headers.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        this.headers.addHeaderLine(line);
    }

    public Enumeration<String> getAllHeaderLines() throws MessagingException {
        return this.headers.getAllHeaderLines();
    }

    public Enumeration<String> getMatchingHeaderLines(String[] names) throws MessagingException {
        return this.headers.getMatchingHeaderLines(names);
    }

    public Enumeration<String> getNonMatchingHeaderLines(String[] names) throws MessagingException {
        return this.headers.getNonMatchingHeaderLines(names);
    }

    /* access modifiers changed from: protected */
    public void updateHeaders() throws MessagingException {
        updateHeaders(this);
        if (this.cachedContent != null) {
            this.f302dh = new DataHandler(this.cachedContent, getContentType());
            this.cachedContent = null;
            this.content = null;
            if (this.contentStream != null) {
                try {
                    this.contentStream.close();
                } catch (IOException e) {
                }
            }
            this.contentStream = null;
        }
    }

    static boolean isMimeType(MimePart part, String mimeType) throws MessagingException {
        String type = part.getContentType();
        try {
            return new ContentType(type).match(mimeType);
        } catch (ParseException e) {
            try {
                int i = type.indexOf(59);
                if (i > 0) {
                    return new ContentType(type.substring(0, i)).match(mimeType);
                }
            } catch (ParseException e2) {
            }
            return type.equalsIgnoreCase(mimeType);
        }
    }

    static void setText(MimePart part, String text, String charset, String subtype) throws MessagingException {
        if (charset == null) {
            if (MimeUtility.checkAscii(text) != 1) {
                charset = MimeUtility.getDefaultMIMECharset();
            } else {
                charset = "us-ascii";
            }
        }
        part.setContent(text, "text/" + subtype + "; charset=" + MimeUtility.quote(charset, HeaderTokenizer.MIME));
    }

    static String getDisposition(MimePart part) throws MessagingException {
        String s = part.getHeader("Content-Disposition", (String) null);
        if (s == null) {
            return null;
        }
        return new ContentDisposition(s).getDisposition();
    }

    static void setDisposition(MimePart part, String disposition) throws MessagingException {
        if (disposition == null) {
            part.removeHeader("Content-Disposition");
            return;
        }
        String s = part.getHeader("Content-Disposition", (String) null);
        if (s != null) {
            ContentDisposition cd = new ContentDisposition(s);
            cd.setDisposition(disposition);
            disposition = cd.toString();
        }
        part.setHeader("Content-Disposition", disposition);
    }

    static String getDescription(MimePart part) throws MessagingException {
        String rawvalue = part.getHeader("Content-Description", (String) null);
        if (rawvalue == null) {
            return null;
        }
        try {
            return MimeUtility.decodeText(MimeUtility.unfold(rawvalue));
        } catch (UnsupportedEncodingException e) {
            return rawvalue;
        }
    }

    static void setDescription(MimePart part, String description, String charset) throws MessagingException {
        if (description == null) {
            part.removeHeader("Content-Description");
            return;
        }
        try {
            part.setHeader("Content-Description", MimeUtility.fold(21, MimeUtility.encodeText(description, charset, (String) null)));
        } catch (UnsupportedEncodingException uex) {
            throw new MessagingException("Encoding error", uex);
        }
    }

    static String getFileName(MimePart part) throws MessagingException {
        String s;
        String filename = null;
        String s2 = part.getHeader("Content-Disposition", (String) null);
        if (s2 != null) {
            filename = new ContentDisposition(s2).getParameter("filename");
        }
        if (filename == null && (s = MimeUtil.cleanContentType(part, part.getHeader("Content-Type", (String) null))) != null) {
            try {
                filename = new ContentType(s).getParameter(IMAPStore.ID_NAME);
            } catch (ParseException e) {
            }
        }
        if (!decodeFileName || filename == null) {
            return filename;
        }
        try {
            return MimeUtility.decodeText(filename);
        } catch (UnsupportedEncodingException ex) {
            throw new MessagingException("Can't decode filename", ex);
        }
    }

    static void setFileName(MimePart part, String name) throws MessagingException {
        String str;
        String s;
        if (encodeFileName && name != null) {
            try {
                name = MimeUtility.encodeText(name);
            } catch (UnsupportedEncodingException ex) {
                throw new MessagingException("Can't encode filename", ex);
            }
        }
        String s2 = part.getHeader("Content-Disposition", (String) null);
        if (s2 == null) {
            str = Part.ATTACHMENT;
        } else {
            str = s2;
        }
        ContentDisposition cd = new ContentDisposition(str);
        String charset = MimeUtility.getDefaultMIMECharset();
        ParameterList p = cd.getParameterList();
        if (p == null) {
            p = new ParameterList();
            cd.setParameterList(p);
        }
        if (encodeFileName) {
            p.setLiteral("filename", name);
        } else {
            p.set("filename", name, charset);
        }
        part.setHeader("Content-Disposition", cd.toString());
        if (setContentTypeFileName && (s = MimeUtil.cleanContentType(part, part.getHeader("Content-Type", (String) null))) != null) {
            try {
                ContentType cType = new ContentType(s);
                ParameterList p2 = cType.getParameterList();
                if (p2 == null) {
                    ParameterList p3 = new ParameterList();
                    try {
                        cType.setParameterList(p3);
                        p2 = p3;
                    } catch (ParseException e) {
                        ParameterList parameterList = p3;
                        return;
                    }
                }
                if (encodeFileName) {
                    p2.setLiteral(IMAPStore.ID_NAME, name);
                } else {
                    p2.set(IMAPStore.ID_NAME, name, charset);
                }
                part.setHeader("Content-Type", cType.toString());
            } catch (ParseException e2) {
            }
        }
    }

    static String[] getContentLanguage(MimePart part) throws MessagingException {
        String s = part.getHeader("Content-Language", (String) null);
        if (s == null) {
            return null;
        }
        HeaderTokenizer h = new HeaderTokenizer(s, HeaderTokenizer.MIME);
        List<String> v = new ArrayList<>();
        while (true) {
            HeaderTokenizer.Token tk = h.next();
            int tkType = tk.getType();
            if (tkType == -4) {
                break;
            } else if (tkType == -1) {
                v.add(tk.getValue());
            }
        }
        if (v.isEmpty()) {
            return null;
        }
        String[] language = new String[v.size()];
        v.toArray(language);
        return language;
    }

    static void setContentLanguage(MimePart part, String[] languages) throws MessagingException {
        StringBuilder sb = new StringBuilder(languages[0]);
        int len = "Content-Language".length() + 2 + languages[0].length();
        for (int i = 1; i < languages.length; i++) {
            sb.append(',');
            int len2 = len + 1;
            if (len2 > 76) {
                sb.append("\r\n\t");
                len2 = 8;
            }
            sb.append(languages[i]);
            len = len2 + languages[i].length();
        }
        part.setHeader("Content-Language", sb.toString());
    }

    static String getEncoding(MimePart part) throws MessagingException {
        HeaderTokenizer.Token tk;
        int tkType;
        String s = part.getHeader("Content-Transfer-Encoding", (String) null);
        if (s == null) {
            return null;
        }
        String s2 = s.trim();
        if (s2.length() == 0) {
            return null;
        }
        if (s2.equalsIgnoreCase("7bit") || s2.equalsIgnoreCase("8bit") || s2.equalsIgnoreCase("quoted-printable") || s2.equalsIgnoreCase("binary") || s2.equalsIgnoreCase("base64")) {
            return s2;
        }
        HeaderTokenizer h = new HeaderTokenizer(s2, HeaderTokenizer.MIME);
        do {
            tk = h.next();
            tkType = tk.getType();
            if (tkType == -4) {
                return s2;
            }
        } while (tkType != -1);
        return tk.getValue();
    }

    static void setEncoding(MimePart part, String encoding) throws MessagingException {
        part.setHeader("Content-Transfer-Encoding", encoding);
    }

    static String restrictEncoding(MimePart part, String encoding) throws MessagingException {
        String type;
        if (!ignoreMultipartEncoding || encoding == null || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit") || encoding.equalsIgnoreCase("binary") || (type = part.getContentType()) == null) {
            return encoding;
        }
        try {
            ContentType cType = new ContentType(type);
            if (cType.match("multipart/*")) {
                return null;
            }
            if (!cType.match("message/*") || PropUtil.getBooleanSystemProperty("mail.mime.allowencodedmessages", false)) {
                return encoding;
            }
            return null;
        } catch (ParseException e) {
            return encoding;
        }
    }

    static void updateHeaders(MimePart part) throws MessagingException {
        String s;
        String filename;
        String charset;
        Object o;
        DataHandler dh = part.getDataHandler();
        if (dh != null) {
            try {
                String type = dh.getContentType();
                boolean composite = false;
                boolean needCTHeader = part.getHeader("Content-Type") == null;
                ContentType cType = new ContentType(type);
                if (cType.match("multipart/*")) {
                    composite = true;
                    if (part instanceof MimeBodyPart) {
                        MimeBodyPart mbp = (MimeBodyPart) part;
                        o = mbp.cachedContent != null ? mbp.cachedContent : dh.getContent();
                    } else if (part instanceof MimeMessage) {
                        MimeMessage msg = (MimeMessage) part;
                        o = msg.cachedContent != null ? msg.cachedContent : dh.getContent();
                    } else {
                        o = dh.getContent();
                    }
                    if (o instanceof MimeMultipart) {
                        ((MimeMultipart) o).updateHeaders();
                    } else {
                        throw new MessagingException("MIME part of type \"" + type + "\" contains object of type " + o.getClass().getName() + " instead of MimeMultipart");
                    }
                } else if (cType.match("message/rfc822")) {
                    composite = true;
                }
                if (dh instanceof MimePartDataHandler) {
                    MimePart mpart = ((MimePartDataHandler) dh).getPart();
                    if (mpart != part) {
                        if (needCTHeader) {
                            part.setHeader("Content-Type", mpart.getContentType());
                        }
                        String enc = mpart.getEncoding();
                        if (enc != null) {
                            setEncoding(part, enc);
                            return;
                        }
                    } else {
                        return;
                    }
                }
                if (!composite) {
                    if (part.getHeader("Content-Transfer-Encoding") == null) {
                        setEncoding(part, MimeUtility.getEncoding(dh));
                    }
                    if (needCTHeader && setDefaultTextCharset && cType.match("text/*") && cType.getParameter("charset") == null) {
                        String enc2 = part.getEncoding();
                        if (enc2 == null || !enc2.equalsIgnoreCase("7bit")) {
                            charset = MimeUtility.getDefaultMIMECharset();
                        } else {
                            charset = "us-ascii";
                        }
                        cType.setParameter("charset", charset);
                        type = cType.toString();
                    }
                }
                if (needCTHeader) {
                    if (!(!setContentTypeFileName || (s = part.getHeader("Content-Disposition", (String) null)) == null || (filename = new ContentDisposition(s).getParameter("filename")) == null)) {
                        ParameterList p = cType.getParameterList();
                        if (p == null) {
                            p = new ParameterList();
                            cType.setParameterList(p);
                        }
                        if (encodeFileName) {
                            p.setLiteral(IMAPStore.ID_NAME, MimeUtility.encodeText(filename));
                        } else {
                            p.set(IMAPStore.ID_NAME, filename, MimeUtility.getDefaultMIMECharset());
                        }
                        type = cType.toString();
                    }
                    part.setHeader("Content-Type", type);
                }
            } catch (IOException ex) {
                throw new MessagingException("IOException updating headers", ex);
            }
        }
    }

    static void invalidateContentHeaders(MimePart part) throws MessagingException {
        part.removeHeader("Content-Type");
        part.removeHeader("Content-Transfer-Encoding");
    }

    /* JADX INFO: finally extract failed */
    static void writeTo(MimePart part, OutputStream os, String[] ignoreList) throws IOException, MessagingException {
        LineOutputStream los;
        if (os instanceof LineOutputStream) {
            los = (LineOutputStream) os;
        } else {
            los = new LineOutputStream(os, allowutf8);
        }
        Enumeration<String> hdrLines = part.getNonMatchingHeaderLines(ignoreList);
        while (hdrLines.hasMoreElements()) {
            los.writeln(hdrLines.nextElement());
        }
        los.writeln();
        InputStream is = null;
        try {
            DataHandler dh = part.getDataHandler();
            if (dh instanceof MimePartDataHandler) {
                MimePartDataHandler mpdh = (MimePartDataHandler) dh;
                if (mpdh.getPart().getEncoding() != null) {
                    is = mpdh.getContentStream();
                }
            }
            if (is != null) {
                byte[] buf = new byte[8192];
                while (true) {
                    int len = is.read(buf);
                    if (len <= 0) {
                        break;
                    }
                    os.write(buf, 0, len);
                }
            } else {
                os = MimeUtility.encode(os, restrictEncoding(part, part.getEncoding()));
                part.getDataHandler().writeTo(os);
            }
            if (is != null) {
                is.close();
            }
            os.flush();
        } catch (Throwable th) {
            if (is != null) {
                is.close();
            }
            throw th;
        }
    }

    static class MimePartDataHandler extends DataHandler {
        MimePart part;

        public MimePartDataHandler(MimePart part2) {
            super((DataSource) new MimePartDataSource(part2));
            this.part = part2;
        }

        /* access modifiers changed from: package-private */
        public InputStream getContentStream() throws MessagingException {
            if (this.part instanceof MimeBodyPart) {
                return ((MimeBodyPart) this.part).getContentStream();
            }
            if (this.part instanceof MimeMessage) {
                return ((MimeMessage) this.part).getContentStream();
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public MimePart getPart() {
            return this.part;
        }
    }
}
