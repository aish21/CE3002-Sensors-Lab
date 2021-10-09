package com.sun.mail.imap;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.ReadableMime;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.FolderClosedException;
import javax.mail.Header;
import javax.mail.IllegalWriteException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;

public class IMAPBodyPart extends MimeBodyPart implements ReadableMime {
    private static final boolean decodeFileName = PropUtil.getBooleanSystemProperty("mail.mime.decodefilename", false);

    /* renamed from: bs */
    private BODYSTRUCTURE f271bs;
    private String description;
    private boolean headersLoaded = false;
    private IMAPMessage message;
    private String sectionId;
    private String type;

    protected IMAPBodyPart(BODYSTRUCTURE bs, String sid, IMAPMessage message2) {
        this.f271bs = bs;
        this.sectionId = sid;
        this.message = message2;
        this.type = new ContentType(bs.type, bs.subtype, bs.cParams).toString();
    }

    /* access modifiers changed from: protected */
    public void updateHeaders() {
    }

    public int getSize() throws MessagingException {
        return this.f271bs.size;
    }

    public int getLineCount() throws MessagingException {
        return this.f271bs.lines;
    }

    public String getContentType() throws MessagingException {
        return this.type;
    }

    public String getDisposition() throws MessagingException {
        return this.f271bs.disposition;
    }

    public void setDisposition(String disposition) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String getEncoding() throws MessagingException {
        return this.f271bs.encoding;
    }

    public String getContentID() throws MessagingException {
        return this.f271bs.f276id;
    }

    public String getContentMD5() throws MessagingException {
        return this.f271bs.md5;
    }

    public void setContentMD5(String md5) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String getDescription() throws MessagingException {
        if (this.description != null) {
            return this.description;
        }
        if (this.f271bs.description == null) {
            return null;
        }
        try {
            this.description = MimeUtility.decodeText(this.f271bs.description);
        } catch (UnsupportedEncodingException e) {
            this.description = this.f271bs.description;
        }
        return this.description;
    }

    public void setDescription(String description2, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String getFileName() throws MessagingException {
        String filename = null;
        if (this.f271bs.dParams != null) {
            filename = this.f271bs.dParams.get("filename");
        }
        if ((filename == null || filename.isEmpty()) && this.f271bs.cParams != null) {
            filename = this.f271bs.cParams.get(IMAPStore.ID_NAME);
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

    public void setFileName(String filename) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0055, code lost:
        if (r2 != null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0057, code lost:
        r13.message.forceCheckExpunged();
        r2 = new java.io.ByteArrayInputStream(new byte[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.io.InputStream getContentStream() throws javax.mail.MessagingException {
        /*
            r13 = this;
            r8 = -1
            r2 = 0
            com.sun.mail.imap.IMAPMessage r7 = r13.message
            boolean r5 = r7.getPeek()
            com.sun.mail.imap.IMAPMessage r7 = r13.message
            java.lang.Object r9 = r7.getMessageCacheLock()
            monitor-enter(r9)
            com.sun.mail.imap.IMAPMessage r7 = r13.message     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.getProtocol()     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            com.sun.mail.imap.IMAPMessage r7 = r13.message     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            r7.checkExpunged()     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            boolean r7 = r3.isREV1()     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            if (r7 == 0) goto L_0x0040
            com.sun.mail.imap.IMAPMessage r7 = r13.message     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            int r7 = r7.getFetchBlockSize()     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            if (r7 == r8) goto L_0x0040
            com.sun.mail.imap.IMAPInputStream r7 = new com.sun.mail.imap.IMAPInputStream     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            com.sun.mail.imap.IMAPMessage r10 = r13.message     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            java.lang.String r11 = r13.sectionId     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            com.sun.mail.imap.IMAPMessage r12 = r13.message     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            boolean r12 = r12.ignoreBodyStructureSize()     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            if (r12 == 0) goto L_0x003b
        L_0x0036:
            r7.<init>(r10, r11, r8, r5)     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            monitor-exit(r9)     // Catch:{ all -> 0x007e }
        L_0x003a:
            return r7
        L_0x003b:
            com.sun.mail.imap.protocol.BODYSTRUCTURE r8 = r13.f271bs     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            int r8 = r8.size     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            goto L_0x0036
        L_0x0040:
            com.sun.mail.imap.IMAPMessage r7 = r13.message     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            int r6 = r7.getSequenceNumber()     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            if (r5 == 0) goto L_0x0066
            java.lang.String r7 = r13.sectionId     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            com.sun.mail.imap.protocol.BODY r0 = r3.peekBody(r6, r7)     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
        L_0x004e:
            if (r0 == 0) goto L_0x0054
            java.io.ByteArrayInputStream r2 = r0.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
        L_0x0054:
            monitor-exit(r9)     // Catch:{ all -> 0x007e }
            if (r2 != 0) goto L_0x0064
            com.sun.mail.imap.IMAPMessage r7 = r13.message
            r7.forceCheckExpunged()
            java.io.ByteArrayInputStream r2 = new java.io.ByteArrayInputStream
            r7 = 0
            byte[] r7 = new byte[r7]
            r2.<init>(r7)
        L_0x0064:
            r7 = r2
            goto L_0x003a
        L_0x0066:
            java.lang.String r7 = r13.sectionId     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            com.sun.mail.imap.protocol.BODY r0 = r3.fetchBody(r6, r7)     // Catch:{ ConnectionException -> 0x006d, ProtocolException -> 0x0081 }
            goto L_0x004e
        L_0x006d:
            r1 = move-exception
            javax.mail.FolderClosedException r7 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x007e }
            com.sun.mail.imap.IMAPMessage r8 = r13.message     // Catch:{ all -> 0x007e }
            javax.mail.Folder r8 = r8.getFolder()     // Catch:{ all -> 0x007e }
            java.lang.String r10 = r1.getMessage()     // Catch:{ all -> 0x007e }
            r7.<init>(r8, r10)     // Catch:{ all -> 0x007e }
            throw r7     // Catch:{ all -> 0x007e }
        L_0x007e:
            r7 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x007e }
            throw r7
        L_0x0081:
            r4 = move-exception
            javax.mail.MessagingException r7 = new javax.mail.MessagingException     // Catch:{ all -> 0x007e }
            java.lang.String r8 = r4.getMessage()     // Catch:{ all -> 0x007e }
            r7.<init>(r8, r4)     // Catch:{ all -> 0x007e }
            throw r7     // Catch:{ all -> 0x007e }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPBodyPart.getContentStream():java.io.InputStream");
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:52:0x00b1=Splitter:B:52:0x00b1, B:40:0x009e=Splitter:B:40:0x009e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.io.InputStream getHeaderStream() throws javax.mail.MessagingException {
        /*
            r13 = this;
            com.sun.mail.imap.IMAPMessage r9 = r13.message
            boolean r9 = r9.isREV1()
            if (r9 != 0) goto L_0x000b
            r13.loadHeaders()
        L_0x000b:
            com.sun.mail.imap.IMAPMessage r9 = r13.message
            java.lang.Object r10 = r9.getMessageCacheLock()
            monitor-enter(r10)
            com.sun.mail.imap.IMAPMessage r9 = r13.message     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            com.sun.mail.imap.protocol.IMAPProtocol r6 = r9.getProtocol()     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            com.sun.mail.imap.IMAPMessage r9 = r13.message     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            r9.checkExpunged()     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            boolean r9 = r6.isREV1()     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            if (r9 == 0) goto L_0x007b
            com.sun.mail.imap.IMAPMessage r9 = r13.message     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            int r8 = r9.getSequenceNumber()     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            r9.<init>()     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            java.lang.String r11 = r13.sectionId     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            java.lang.StringBuilder r9 = r9.append(r11)     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            java.lang.String r11 = ".MIME"
            java.lang.StringBuilder r9 = r9.append(r11)     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            java.lang.String r9 = r9.toString()     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            com.sun.mail.imap.protocol.BODY r0 = r6.peekBody(r8, r9)     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            if (r0 != 0) goto L_0x0060
            javax.mail.MessagingException r9 = new javax.mail.MessagingException     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            java.lang.String r11 = "Failed to fetch headers"
            r9.<init>(r11)     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            throw r9     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
        L_0x004c:
            r3 = move-exception
            javax.mail.FolderClosedException r9 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x005d }
            com.sun.mail.imap.IMAPMessage r11 = r13.message     // Catch:{ all -> 0x005d }
            javax.mail.Folder r11 = r11.getFolder()     // Catch:{ all -> 0x005d }
            java.lang.String r12 = r3.getMessage()     // Catch:{ all -> 0x005d }
            r9.<init>(r11, r12)     // Catch:{ all -> 0x005d }
            throw r9     // Catch:{ all -> 0x005d }
        L_0x005d:
            r9 = move-exception
            monitor-exit(r10)     // Catch:{ all -> 0x005d }
            throw r9
        L_0x0060:
            java.io.ByteArrayInputStream r1 = r0.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            if (r1 != 0) goto L_0x0079
            javax.mail.MessagingException r9 = new javax.mail.MessagingException     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            java.lang.String r11 = "Failed to fetch headers"
            r9.<init>(r11)     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            throw r9     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
        L_0x006e:
            r7 = move-exception
            javax.mail.MessagingException r9 = new javax.mail.MessagingException     // Catch:{ all -> 0x005d }
            java.lang.String r11 = r7.getMessage()     // Catch:{ all -> 0x005d }
            r9.<init>(r11, r7)     // Catch:{ all -> 0x005d }
            throw r9     // Catch:{ all -> 0x005d }
        L_0x0079:
            monitor-exit(r10)     // Catch:{ all -> 0x005d }
        L_0x007a:
            return r1
        L_0x007b:
            com.sun.mail.util.SharedByteArrayOutputStream r2 = new com.sun.mail.util.SharedByteArrayOutputStream     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            r9 = 0
            r2.<init>(r9)     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            com.sun.mail.util.LineOutputStream r5 = new com.sun.mail.util.LineOutputStream     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            r5.<init>(r2)     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            java.util.Enumeration r4 = super.getAllHeaderLines()     // Catch:{ IOException -> 0x009a, all -> 0x00ad }
        L_0x008a:
            boolean r9 = r4.hasMoreElements()     // Catch:{ IOException -> 0x009a, all -> 0x00ad }
            if (r9 == 0) goto L_0x00a4
            java.lang.Object r9 = r4.nextElement()     // Catch:{ IOException -> 0x009a, all -> 0x00ad }
            java.lang.String r9 = (java.lang.String) r9     // Catch:{ IOException -> 0x009a, all -> 0x00ad }
            r5.writeln(r9)     // Catch:{ IOException -> 0x009a, all -> 0x00ad }
            goto L_0x008a
        L_0x009a:
            r9 = move-exception
            r5.close()     // Catch:{ IOException -> 0x00b2 }
        L_0x009e:
            java.io.InputStream r1 = r2.toStream()     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
            monitor-exit(r10)     // Catch:{ all -> 0x005d }
            goto L_0x007a
        L_0x00a4:
            r5.writeln()     // Catch:{ IOException -> 0x009a, all -> 0x00ad }
            r5.close()     // Catch:{ IOException -> 0x00ab }
            goto L_0x009e
        L_0x00ab:
            r9 = move-exception
            goto L_0x009e
        L_0x00ad:
            r9 = move-exception
            r5.close()     // Catch:{ IOException -> 0x00b4 }
        L_0x00b1:
            throw r9     // Catch:{ ConnectionException -> 0x004c, ProtocolException -> 0x006e }
        L_0x00b2:
            r9 = move-exception
            goto L_0x009e
        L_0x00b4:
            r11 = move-exception
            goto L_0x00b1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPBodyPart.getHeaderStream():java.io.InputStream");
    }

    public InputStream getMimeStream() throws MessagingException {
        return new SequenceInputStream(getHeaderStream(), getContentStream());
    }

    public synchronized DataHandler getDataHandler() throws MessagingException {
        if (this.f302dh == null) {
            if (this.f271bs.isMulti()) {
                this.f302dh = new DataHandler((DataSource) new IMAPMultipartDataSource(this, this.f271bs.bodies, this.sectionId, this.message));
            } else if (this.f271bs.isNested() && this.message.isREV1() && this.f271bs.envelope != null) {
                this.f302dh = new DataHandler(new IMAPNestedMessage(this.message, this.f271bs.bodies[0], this.f271bs.envelope, this.sectionId), this.type);
            }
        }
        return super.getDataHandler();
    }

    public void setDataHandler(DataHandler content) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void setContent(Object o, String type2) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void setContent(Multipart mp) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String[] getHeader(String name) throws MessagingException {
        loadHeaders();
        return super.getHeader(name);
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public Enumeration<Header> getAllHeaders() throws MessagingException {
        loadHeaders();
        return super.getAllHeaders();
    }

    public Enumeration<Header> getMatchingHeaders(String[] names) throws MessagingException {
        loadHeaders();
        return super.getMatchingHeaders(names);
    }

    public Enumeration<Header> getNonMatchingHeaders(String[] names) throws MessagingException {
        loadHeaders();
        return super.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public Enumeration<String> getAllHeaderLines() throws MessagingException {
        loadHeaders();
        return super.getAllHeaderLines();
    }

    public Enumeration<String> getMatchingHeaderLines(String[] names) throws MessagingException {
        loadHeaders();
        return super.getMatchingHeaderLines(names);
    }

    public Enumeration<String> getNonMatchingHeaderLines(String[] names) throws MessagingException {
        loadHeaders();
        return super.getNonMatchingHeaderLines(names);
    }

    private synchronized void loadHeaders() throws MessagingException {
        if (!this.headersLoaded) {
            if (this.headers == null) {
                this.headers = new InternetHeaders();
            }
            synchronized (this.message.getMessageCacheLock()) {
                try {
                    IMAPProtocol p = this.message.getProtocol();
                    this.message.checkExpunged();
                    if (p.isREV1()) {
                        BODY b = p.peekBody(this.message.getSequenceNumber(), this.sectionId + ".MIME");
                        if (b == null) {
                            throw new MessagingException("Failed to fetch headers");
                        }
                        ByteArrayInputStream bis = b.getByteArrayInputStream();
                        if (bis == null) {
                            throw new MessagingException("Failed to fetch headers");
                        }
                        this.headers.load(bis);
                    } else {
                        this.headers.addHeader("Content-Type", this.type);
                        this.headers.addHeader("Content-Transfer-Encoding", this.f271bs.encoding);
                        if (this.f271bs.description != null) {
                            this.headers.addHeader("Content-Description", this.f271bs.description);
                        }
                        if (this.f271bs.f276id != null) {
                            this.headers.addHeader("Content-ID", this.f271bs.f276id);
                        }
                        if (this.f271bs.md5 != null) {
                            this.headers.addHeader("Content-MD5", this.f271bs.md5);
                        }
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.message.getFolder(), cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            this.headersLoaded = true;
        }
    }
}
