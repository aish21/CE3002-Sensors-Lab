package com.sun.mail.imap;

import androidx.appcompat.widget.ActivityChooserView;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.Utility;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FetchItem;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.INTERNALDATE;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.MODSEQ;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.RFC822SIZE;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.util.ReadableMime;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Header;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class IMAPMessage extends MimeMessage implements ReadableMime {
    static final String EnvelopeCmd = "ENVELOPE INTERNALDATE RFC822.SIZE";
    /* access modifiers changed from: private */
    public volatile boolean bodyLoaded = false;

    /* renamed from: bs */
    protected BODYSTRUCTURE f272bs;
    private String description;
    protected ENVELOPE envelope;
    private volatile boolean headersLoaded = false;
    protected Map<String, Object> items;
    private Hashtable<String, String> loadedHeaders = new Hashtable<>(1);
    private volatile long modseq = -1;
    private Boolean peek;
    /* access modifiers changed from: private */
    public Date receivedDate;
    protected String sectionId;
    /* access modifiers changed from: private */
    public long size = -1;
    private String subject;
    private String type;
    private volatile long uid = -1;

    protected IMAPMessage(IMAPFolder folder, int msgnum) {
        super((Folder) folder, msgnum);
        this.flags = null;
    }

    protected IMAPMessage(Session session) {
        super(session);
    }

    /* access modifiers changed from: protected */
    public IMAPProtocol getProtocol() throws ProtocolException, FolderClosedException {
        ((IMAPFolder) this.folder).waitIfIdle();
        IMAPProtocol p = ((IMAPFolder) this.folder).protocol;
        if (p != null) {
            return p;
        }
        throw new FolderClosedException(this.folder);
    }

    /* access modifiers changed from: protected */
    public boolean isREV1() throws FolderClosedException {
        IMAPProtocol p = ((IMAPFolder) this.folder).protocol;
        if (p != null) {
            return p.isREV1();
        }
        throw new FolderClosedException(this.folder);
    }

    /* access modifiers changed from: protected */
    public Object getMessageCacheLock() {
        return ((IMAPFolder) this.folder).messageCacheLock;
    }

    /* access modifiers changed from: protected */
    public int getSequenceNumber() {
        return ((IMAPFolder) this.folder).messageCache.seqnumOf(getMessageNumber());
    }

    /* access modifiers changed from: protected */
    public void setMessageNumber(int msgnum) {
        super.setMessageNumber(msgnum);
    }

    /* access modifiers changed from: protected */
    public long getUID() {
        return this.uid;
    }

    /* access modifiers changed from: protected */
    public void setUID(long uid2) {
        this.uid = uid2;
    }

    public synchronized long getModSeq() throws MessagingException {
        long j;
        if (this.modseq != -1) {
            j = this.modseq;
        } else {
            synchronized (getMessageCacheLock()) {
                try {
                    IMAPProtocol p = getProtocol();
                    checkExpunged();
                    MODSEQ ms = p.fetchMODSEQ(getSequenceNumber());
                    if (ms != null) {
                        this.modseq = ms.modseq;
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.folder, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            j = this.modseq;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public long _getModSeq() {
        return this.modseq;
    }

    /* access modifiers changed from: package-private */
    public void setModSeq(long modseq2) {
        this.modseq = modseq2;
    }

    /* access modifiers changed from: protected */
    public void setExpunged(boolean set) {
        super.setExpunged(set);
    }

    /* access modifiers changed from: protected */
    public void checkExpunged() throws MessageRemovedException {
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }

    /* access modifiers changed from: protected */
    public void forceCheckExpunged() throws MessageRemovedException, FolderClosedException {
        synchronized (getMessageCacheLock()) {
            try {
                getProtocol().noop();
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException e) {
            }
        }
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }

    /* access modifiers changed from: protected */
    public int getFetchBlockSize() {
        return ((IMAPStore) this.folder.getStore()).getFetchBlockSize();
    }

    /* access modifiers changed from: protected */
    public boolean ignoreBodyStructureSize() {
        return ((IMAPStore) this.folder.getStore()).ignoreBodyStructureSize();
    }

    public Address[] getFrom() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getFrom();
        }
        loadEnvelope();
        InternetAddress[] a = this.envelope.from;
        if (a == null || a.length == 0) {
            a = this.envelope.sender;
        }
        return aaclone(a);
    }

    public void setFrom(Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void addFrom(Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Address getSender() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getSender();
        }
        loadEnvelope();
        if (this.envelope.sender == null || this.envelope.sender.length <= 0) {
            return null;
        }
        return this.envelope.sender[0];
    }

    public void setSender(Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Address[] getRecipients(Message.RecipientType type2) throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getRecipients(type2);
        }
        loadEnvelope();
        if (type2 == Message.RecipientType.f298TO) {
            return aaclone(this.envelope.f278to);
        }
        if (type2 == Message.RecipientType.f297CC) {
            return aaclone(this.envelope.f277cc);
        }
        if (type2 == Message.RecipientType.BCC) {
            return aaclone(this.envelope.bcc);
        }
        return super.getRecipients(type2);
    }

    public void setRecipients(Message.RecipientType type2, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void addRecipients(Message.RecipientType type2, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Address[] getReplyTo() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getReplyTo();
        }
        loadEnvelope();
        if (this.envelope.replyTo == null || this.envelope.replyTo.length == 0) {
            return getFrom();
        }
        return aaclone(this.envelope.replyTo);
    }

    public void setReplyTo(Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getSubject() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getSubject();
        }
        if (this.subject != null) {
            return this.subject;
        }
        loadEnvelope();
        if (this.envelope.subject == null) {
            return null;
        }
        try {
            this.subject = MimeUtility.decodeText(MimeUtility.unfold(this.envelope.subject));
        } catch (UnsupportedEncodingException e) {
            this.subject = this.envelope.subject;
        }
        return this.subject;
    }

    public void setSubject(String subject2, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Date getSentDate() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getSentDate();
        }
        loadEnvelope();
        if (this.envelope.date == null) {
            return null;
        }
        return new Date(this.envelope.date.getTime());
    }

    public void setSentDate(Date d) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Date getReceivedDate() throws MessagingException {
        checkExpunged();
        if (this.receivedDate == null) {
            loadEnvelope();
        }
        if (this.receivedDate == null) {
            return null;
        }
        return new Date(this.receivedDate.getTime());
    }

    public int getSize() throws MessagingException {
        checkExpunged();
        if (this.size == -1) {
            loadEnvelope();
        }
        if (this.size > 2147483647L) {
            return ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        }
        return (int) this.size;
    }

    public long getSizeLong() throws MessagingException {
        checkExpunged();
        if (this.size == -1) {
            loadEnvelope();
        }
        return this.size;
    }

    public int getLineCount() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.f272bs.lines;
    }

    public String[] getContentLanguage() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getContentLanguage();
        }
        loadBODYSTRUCTURE();
        if (this.f272bs.language != null) {
            return (String[]) this.f272bs.language.clone();
        }
        return null;
    }

    public void setContentLanguage(String[] languages) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getInReplyTo() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getHeader("In-Reply-To", " ");
        }
        loadEnvelope();
        return this.envelope.inReplyTo;
    }

    public synchronized String getContentType() throws MessagingException {
        String str;
        checkExpunged();
        if (this.bodyLoaded) {
            str = super.getContentType();
        } else {
            if (this.type == null) {
                loadBODYSTRUCTURE();
                this.type = new ContentType(this.f272bs.type, this.f272bs.subtype, this.f272bs.cParams).toString();
            }
            str = this.type;
        }
        return str;
    }

    public String getDisposition() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getDisposition();
        }
        loadBODYSTRUCTURE();
        return this.f272bs.disposition;
    }

    public void setDisposition(String disposition) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getEncoding() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getEncoding();
        }
        loadBODYSTRUCTURE();
        return this.f272bs.encoding;
    }

    public String getContentID() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getContentID();
        }
        loadBODYSTRUCTURE();
        return this.f272bs.f276id;
    }

    public void setContentID(String cid) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getContentMD5() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getContentMD5();
        }
        loadBODYSTRUCTURE();
        return this.f272bs.md5;
    }

    public void setContentMD5(String md5) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getDescription() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getDescription();
        }
        if (this.description != null) {
            return this.description;
        }
        loadBODYSTRUCTURE();
        if (this.f272bs.description == null) {
            return null;
        }
        try {
            this.description = MimeUtility.decodeText(this.f272bs.description);
        } catch (UnsupportedEncodingException e) {
            this.description = this.f272bs.description;
        }
        return this.description;
    }

    public void setDescription(String description2, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getMessageID() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getMessageID();
        }
        loadEnvelope();
        return this.envelope.messageId;
    }

    public String getFileName() throws MessagingException {
        checkExpunged();
        if (this.bodyLoaded) {
            return super.getFileName();
        }
        String filename = null;
        loadBODYSTRUCTURE();
        if (this.f272bs.dParams != null) {
            filename = this.f272bs.dParams.get("filename");
        }
        if (filename != null || this.f272bs.cParams == null) {
            return filename;
        }
        return this.f272bs.cParams.get(IMAPStore.ID_NAME);
    }

    public void setFileName(String filename) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0060, code lost:
        if (r2 != null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0062, code lost:
        forceCheckExpunged();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        return new java.io.ByteArrayInputStream(new byte[0]);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.io.InputStream getContentStream() throws javax.mail.MessagingException {
        /*
            r12 = this;
            r8 = -1
            boolean r7 = r12.bodyLoaded
            if (r7 == 0) goto L_0x000a
            java.io.InputStream r2 = super.getContentStream()
        L_0x0009:
            return r2
        L_0x000a:
            r2 = 0
            boolean r5 = r12.getPeek()
            java.lang.Object r9 = r12.getMessageCacheLock()
            monitor-enter(r9)
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r12.getProtocol()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            r12.checkExpunged()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            boolean r7 = r3.isREV1()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            if (r7 == 0) goto L_0x0043
            int r7 = r12.getFetchBlockSize()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            if (r7 == r8) goto L_0x0043
            com.sun.mail.imap.IMAPInputStream r7 = new com.sun.mail.imap.IMAPInputStream     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            java.lang.String r10 = "TEXT"
            java.lang.String r10 = r12.toSection(r10)     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            com.sun.mail.imap.protocol.BODYSTRUCTURE r11 = r12.f272bs     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            if (r11 == 0) goto L_0x003d
            boolean r11 = r12.ignoreBodyStructureSize()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            if (r11 != 0) goto L_0x003d
            com.sun.mail.imap.protocol.BODYSTRUCTURE r8 = r12.f272bs     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            int r8 = r8.size     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
        L_0x003d:
            r7.<init>(r12, r10, r8, r5)     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            monitor-exit(r9)     // Catch:{ all -> 0x009b }
            r2 = r7
            goto L_0x0009
        L_0x0043:
            boolean r7 = r3.isREV1()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            if (r7 == 0) goto L_0x007d
            if (r5 == 0) goto L_0x006e
            int r7 = r12.getSequenceNumber()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            java.lang.String r8 = "TEXT"
            java.lang.String r8 = r12.toSection(r8)     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            com.sun.mail.imap.protocol.BODY r0 = r3.peekBody(r7, r8)     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
        L_0x0059:
            if (r0 == 0) goto L_0x005f
            java.io.ByteArrayInputStream r2 = r0.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
        L_0x005f:
            monitor-exit(r9)     // Catch:{ all -> 0x009b }
            if (r2 != 0) goto L_0x0009
            r12.forceCheckExpunged()
            java.io.ByteArrayInputStream r2 = new java.io.ByteArrayInputStream
            r7 = 0
            byte[] r7 = new byte[r7]
            r2.<init>(r7)
            goto L_0x0009
        L_0x006e:
            int r7 = r12.getSequenceNumber()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            java.lang.String r8 = "TEXT"
            java.lang.String r8 = r12.toSection(r8)     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            com.sun.mail.imap.protocol.BODY r0 = r3.fetchBody(r7, r8)     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            goto L_0x0059
        L_0x007d:
            int r7 = r12.getSequenceNumber()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            java.lang.String r8 = "TEXT"
            com.sun.mail.imap.protocol.RFC822DATA r6 = r3.fetchRFC822(r7, r8)     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            if (r6 == 0) goto L_0x005f
            java.io.ByteArrayInputStream r2 = r6.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x008e, ProtocolException -> 0x009e }
            goto L_0x005f
        L_0x008e:
            r1 = move-exception
            javax.mail.FolderClosedException r7 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x009b }
            javax.mail.Folder r8 = r12.folder     // Catch:{ all -> 0x009b }
            java.lang.String r10 = r1.getMessage()     // Catch:{ all -> 0x009b }
            r7.<init>(r8, r10)     // Catch:{ all -> 0x009b }
            throw r7     // Catch:{ all -> 0x009b }
        L_0x009b:
            r7 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x009b }
            throw r7
        L_0x009e:
            r4 = move-exception
            r12.forceCheckExpunged()     // Catch:{ all -> 0x009b }
            javax.mail.MessagingException r7 = new javax.mail.MessagingException     // Catch:{ all -> 0x009b }
            java.lang.String r8 = r4.getMessage()     // Catch:{ all -> 0x009b }
            r7.<init>(r8, r4)     // Catch:{ all -> 0x009b }
            throw r7     // Catch:{ all -> 0x009b }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.getContentStream():java.io.InputStream");
    }

    public synchronized DataHandler getDataHandler() throws MessagingException {
        String str;
        checkExpunged();
        if (this.f303dh == null && !this.bodyLoaded) {
            loadBODYSTRUCTURE();
            if (this.type == null) {
                this.type = new ContentType(this.f272bs.type, this.f272bs.subtype, this.f272bs.cParams).toString();
            }
            if (this.f272bs.isMulti()) {
                this.f303dh = new DataHandler((DataSource) new IMAPMultipartDataSource(this, this.f272bs.bodies, this.sectionId, this));
            } else if (this.f272bs.isNested() && isREV1() && this.f272bs.envelope != null) {
                BODYSTRUCTURE bodystructure = this.f272bs.bodies[0];
                ENVELOPE envelope2 = this.f272bs.envelope;
                if (this.sectionId == null) {
                    str = "1";
                } else {
                    str = this.sectionId + ".1";
                }
                this.f303dh = new DataHandler(new IMAPNestedMessage(this, bodystructure, envelope2, str), this.type);
            }
        }
        return super.getDataHandler();
    }

    public void setDataHandler(DataHandler content) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0041, code lost:
        if (r2 != null) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        forceCheckExpunged();
        r2 = new java.io.ByteArrayInputStream(new byte[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.io.InputStream getMimeStream() throws javax.mail.MessagingException {
        /*
            r11 = this;
            r9 = -1
            r2 = 0
            boolean r5 = r11.getPeek()
            java.lang.Object r8 = r11.getMessageCacheLock()
            monitor-enter(r8)
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r11.getProtocol()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            r11.checkExpunged()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            boolean r7 = r3.isREV1()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            if (r7 == 0) goto L_0x0028
            int r7 = r11.getFetchBlockSize()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            if (r7 == r9) goto L_0x0028
            com.sun.mail.imap.IMAPInputStream r7 = new com.sun.mail.imap.IMAPInputStream     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            java.lang.String r9 = r11.sectionId     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            r10 = -1
            r7.<init>(r11, r9, r10, r5)     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            monitor-exit(r8)     // Catch:{ all -> 0x0078 }
        L_0x0027:
            return r7
        L_0x0028:
            boolean r7 = r3.isREV1()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            if (r7 == 0) goto L_0x005b
            if (r5 == 0) goto L_0x0050
            int r7 = r11.getSequenceNumber()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            java.lang.String r9 = r11.sectionId     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            com.sun.mail.imap.protocol.BODY r0 = r3.peekBody(r7, r9)     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
        L_0x003a:
            if (r0 == 0) goto L_0x0040
            java.io.ByteArrayInputStream r2 = r0.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
        L_0x0040:
            monitor-exit(r8)     // Catch:{ all -> 0x0078 }
            if (r2 != 0) goto L_0x004e
            r11.forceCheckExpunged()
            java.io.ByteArrayInputStream r2 = new java.io.ByteArrayInputStream
            r7 = 0
            byte[] r7 = new byte[r7]
            r2.<init>(r7)
        L_0x004e:
            r7 = r2
            goto L_0x0027
        L_0x0050:
            int r7 = r11.getSequenceNumber()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            java.lang.String r9 = r11.sectionId     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            com.sun.mail.imap.protocol.BODY r0 = r3.fetchBody(r7, r9)     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            goto L_0x003a
        L_0x005b:
            int r7 = r11.getSequenceNumber()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            r9 = 0
            com.sun.mail.imap.protocol.RFC822DATA r6 = r3.fetchRFC822(r7, r9)     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            if (r6 == 0) goto L_0x0040
            java.io.ByteArrayInputStream r2 = r6.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x006b, ProtocolException -> 0x007b }
            goto L_0x0040
        L_0x006b:
            r1 = move-exception
            javax.mail.FolderClosedException r7 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x0078 }
            javax.mail.Folder r9 = r11.folder     // Catch:{ all -> 0x0078 }
            java.lang.String r10 = r1.getMessage()     // Catch:{ all -> 0x0078 }
            r7.<init>(r9, r10)     // Catch:{ all -> 0x0078 }
            throw r7     // Catch:{ all -> 0x0078 }
        L_0x0078:
            r7 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x0078 }
            throw r7
        L_0x007b:
            r4 = move-exception
            r11.forceCheckExpunged()     // Catch:{ all -> 0x0078 }
            javax.mail.MessagingException r7 = new javax.mail.MessagingException     // Catch:{ all -> 0x0078 }
            java.lang.String r9 = r4.getMessage()     // Catch:{ all -> 0x0078 }
            r7.<init>(r9, r4)     // Catch:{ all -> 0x0078 }
            throw r7     // Catch:{ all -> 0x0078 }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.getMimeStream():java.io.InputStream");
    }

    public void writeTo(OutputStream os) throws IOException, MessagingException {
        if (this.bodyLoaded) {
            super.writeTo(os);
            return;
        }
        InputStream is = getMimeStream();
        try {
            byte[] bytes = new byte[16384];
            while (true) {
                int count = is.read(bytes);
                if (count != -1) {
                    os.write(bytes, 0, count);
                } else {
                    return;
                }
            }
        } finally {
            is.close();
        }
    }

    public String[] getHeader(String name) throws MessagingException {
        checkExpunged();
        if (isHeaderLoaded(name)) {
            return this.headers.getHeader(name);
        }
        InputStream is = null;
        synchronized (getMessageCacheLock()) {
            try {
                IMAPProtocol p = getProtocol();
                checkExpunged();
                if (p.isREV1()) {
                    BODY b = p.peekBody(getSequenceNumber(), toSection("HEADER.FIELDS (" + name + ")"));
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                } else {
                    RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), "HEADER.LINES (" + name + ")");
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException pex) {
                forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            return null;
        }
        if (this.headers == null) {
            this.headers = new InternetHeaders();
        }
        this.headers.load(is);
        setHeaderLoaded(name);
        return this.headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        checkExpunged();
        if (getHeader(name) == null) {
            return null;
        }
        return this.headers.getHeader(name, delimiter);
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Enumeration<Header> getAllHeaders() throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getAllHeaders();
    }

    public Enumeration<Header> getMatchingHeaders(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getMatchingHeaders(names);
    }

    public Enumeration<Header> getNonMatchingHeaders(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Enumeration<String> getAllHeaderLines() throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getAllHeaderLines();
    }

    public Enumeration<String> getMatchingHeaderLines(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getMatchingHeaderLines(names);
    }

    public Enumeration<String> getNonMatchingHeaderLines(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getNonMatchingHeaderLines(names);
    }

    public synchronized Flags getFlags() throws MessagingException {
        checkExpunged();
        loadFlags();
        return super.getFlags();
    }

    public synchronized boolean isSet(Flags.Flag flag) throws MessagingException {
        checkExpunged();
        loadFlags();
        return super.isSet(flag);
    }

    public synchronized void setFlags(Flags flag, boolean set) throws MessagingException {
        synchronized (getMessageCacheLock()) {
            try {
                IMAPProtocol p = getProtocol();
                checkExpunged();
                p.storeFlags(getSequenceNumber(), flag, set);
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }

    public synchronized void setPeek(boolean peek2) {
        this.peek = Boolean.valueOf(peek2);
    }

    public synchronized boolean getPeek() {
        boolean booleanValue;
        if (this.peek == null) {
            booleanValue = ((IMAPStore) this.folder.getStore()).getPeek();
        } else {
            booleanValue = this.peek.booleanValue();
        }
        return booleanValue;
    }

    public synchronized void invalidateHeaders() {
        this.headersLoaded = false;
        this.loadedHeaders.clear();
        this.headers = null;
        this.envelope = null;
        this.f272bs = null;
        this.receivedDate = null;
        this.size = -1;
        this.type = null;
        this.subject = null;
        this.description = null;
        this.flags = null;
        this.content = null;
        this.contentStream = null;
        this.bodyLoaded = false;
    }

    public static class FetchProfileCondition implements Utility.Condition {
        private String[] hdrs = null;
        private Set<FetchItem> need = new HashSet();
        private boolean needBodyStructure = false;
        private boolean needEnvelope = false;
        private boolean needFlags = false;
        private boolean needHeaders = false;
        private boolean needMessage = false;
        private boolean needRDate = false;
        private boolean needSize = false;
        private boolean needUID = false;

        public FetchProfileCondition(FetchProfile fp, FetchItem[] fitems) {
            if (fp.contains(FetchProfile.Item.ENVELOPE)) {
                this.needEnvelope = true;
            }
            if (fp.contains(FetchProfile.Item.FLAGS)) {
                this.needFlags = true;
            }
            if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
                this.needBodyStructure = true;
            }
            if (fp.contains(FetchProfile.Item.SIZE)) {
                this.needSize = true;
            }
            if (fp.contains((FetchProfile.Item) UIDFolder.FetchProfileItem.UID)) {
                this.needUID = true;
            }
            if (fp.contains((FetchProfile.Item) IMAPFolder.FetchProfileItem.HEADERS)) {
                this.needHeaders = true;
            }
            if (fp.contains((FetchProfile.Item) IMAPFolder.FetchProfileItem.SIZE)) {
                this.needSize = true;
            }
            if (fp.contains((FetchProfile.Item) IMAPFolder.FetchProfileItem.MESSAGE)) {
                this.needMessage = true;
            }
            if (fp.contains((FetchProfile.Item) IMAPFolder.FetchProfileItem.INTERNALDATE)) {
                this.needRDate = true;
            }
            this.hdrs = fp.getHeaderNames();
            for (int i = 0; i < fitems.length; i++) {
                if (fp.contains(fitems[i].getFetchProfileItem())) {
                    this.need.add(fitems[i]);
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:47:0x0089  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean test(com.sun.mail.imap.IMAPMessage r9) {
            /*
                r8 = this;
                r6 = -1
                r3 = 1
                boolean r4 = r8.needEnvelope
                if (r4 == 0) goto L_0x0014
                com.sun.mail.imap.protocol.ENVELOPE r4 = r9._getEnvelope()
                if (r4 != 0) goto L_0x0014
                boolean r4 = r9.bodyLoaded
                if (r4 != 0) goto L_0x0014
            L_0x0013:
                return r3
            L_0x0014:
                boolean r4 = r8.needFlags
                if (r4 == 0) goto L_0x001e
                javax.mail.Flags r4 = r9._getFlags()
                if (r4 == 0) goto L_0x0013
            L_0x001e:
                boolean r4 = r8.needBodyStructure
                if (r4 == 0) goto L_0x002e
                com.sun.mail.imap.protocol.BODYSTRUCTURE r4 = r9._getBodyStructure()
                if (r4 != 0) goto L_0x002e
                boolean r4 = r9.bodyLoaded
                if (r4 == 0) goto L_0x0013
            L_0x002e:
                boolean r4 = r8.needUID
                if (r4 == 0) goto L_0x003a
                long r4 = r9.getUID()
                int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r4 == 0) goto L_0x0013
            L_0x003a:
                boolean r4 = r8.needHeaders
                if (r4 == 0) goto L_0x0044
                boolean r4 = r9.areHeadersLoaded()
                if (r4 == 0) goto L_0x0013
            L_0x0044:
                boolean r4 = r8.needSize
                if (r4 == 0) goto L_0x0056
                long r4 = r9.size
                int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r4 != 0) goto L_0x0056
                boolean r4 = r9.bodyLoaded
                if (r4 == 0) goto L_0x0013
            L_0x0056:
                boolean r4 = r8.needMessage
                if (r4 == 0) goto L_0x0060
                boolean r4 = r9.bodyLoaded
                if (r4 == 0) goto L_0x0013
            L_0x0060:
                boolean r4 = r8.needRDate
                if (r4 == 0) goto L_0x006a
                java.util.Date r4 = r9.receivedDate
                if (r4 == 0) goto L_0x0013
            L_0x006a:
                r1 = 0
            L_0x006b:
                java.lang.String[] r4 = r8.hdrs
                int r4 = r4.length
                if (r1 >= r4) goto L_0x007d
                java.lang.String[] r4 = r8.hdrs
                r4 = r4[r1]
                boolean r4 = r9.isHeaderLoaded(r4)
                if (r4 == 0) goto L_0x0013
                int r1 = r1 + 1
                goto L_0x006b
            L_0x007d:
                java.util.Set<com.sun.mail.imap.protocol.FetchItem> r4 = r8.need
                java.util.Iterator r2 = r4.iterator()
            L_0x0083:
                boolean r4 = r2.hasNext()
                if (r4 == 0) goto L_0x00a1
                java.lang.Object r0 = r2.next()
                com.sun.mail.imap.protocol.FetchItem r0 = (com.sun.mail.imap.protocol.FetchItem) r0
                java.util.Map<java.lang.String, java.lang.Object> r4 = r9.items
                if (r4 == 0) goto L_0x0013
                java.util.Map<java.lang.String, java.lang.Object> r4 = r9.items
                java.lang.String r5 = r0.getName()
                java.lang.Object r4 = r4.get(r5)
                if (r4 != 0) goto L_0x0083
                goto L_0x0013
            L_0x00a1:
                r3 = 0
                goto L_0x0013
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.FetchProfileCondition.test(com.sun.mail.imap.IMAPMessage):boolean");
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleFetchItem(Item item, String[] hdrs, boolean allHeaders) throws MessagingException {
        InputStream headerStream;
        boolean isHeader;
        if (item instanceof Flags) {
            this.flags = (Flags) item;
        } else if (item instanceof ENVELOPE) {
            this.envelope = (ENVELOPE) item;
        } else if (item instanceof INTERNALDATE) {
            this.receivedDate = ((INTERNALDATE) item).getDate();
        } else if (item instanceof RFC822SIZE) {
            this.size = ((RFC822SIZE) item).size;
        } else if (item instanceof MODSEQ) {
            this.modseq = ((MODSEQ) item).modseq;
        } else if (item instanceof BODYSTRUCTURE) {
            this.f272bs = (BODYSTRUCTURE) item;
        } else if (item instanceof UID) {
            UID u = (UID) item;
            this.uid = u.uid;
            if (((IMAPFolder) this.folder).uidTable == null) {
                ((IMAPFolder) this.folder).uidTable = new Hashtable<>();
            }
            ((IMAPFolder) this.folder).uidTable.put(Long.valueOf(u.uid), this);
        } else if (!(item instanceof RFC822DATA) && !(item instanceof BODY)) {
            return false;
        } else {
            if (item instanceof RFC822DATA) {
                headerStream = ((RFC822DATA) item).getByteArrayInputStream();
                isHeader = ((RFC822DATA) item).isHeader();
            } else {
                headerStream = ((BODY) item).getByteArrayInputStream();
                isHeader = ((BODY) item).isHeader();
            }
            if (!isHeader) {
                try {
                    this.size = (long) headerStream.available();
                } catch (IOException e) {
                }
                parse(headerStream);
                this.bodyLoaded = true;
                setHeadersLoaded(true);
            } else {
                InternetHeaders h = new InternetHeaders();
                if (headerStream != null) {
                    h.load(headerStream);
                }
                if (this.headers == null || allHeaders) {
                    this.headers = h;
                } else {
                    Enumeration<Header> e2 = h.getAllHeaders();
                    while (e2.hasMoreElements()) {
                        Header he = e2.nextElement();
                        if (!isHeaderLoaded(he.getName())) {
                            this.headers.addHeader(he.getName(), he.getValue());
                        }
                    }
                }
                if (allHeaders) {
                    setHeadersLoaded(true);
                } else {
                    for (String headerLoaded : hdrs) {
                        setHeaderLoaded(headerLoaded);
                    }
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void handleExtensionFetchItems(Map<String, Object> extensionItems) {
        if (extensionItems != null && !extensionItems.isEmpty()) {
            if (this.items == null) {
                this.items = new HashMap();
            }
            this.items.putAll(extensionItems);
        }
    }

    /* access modifiers changed from: protected */
    public Object fetchItem(FetchItem fitem) throws MessagingException {
        Object robj;
        Object o;
        synchronized (getMessageCacheLock()) {
            robj = null;
            try {
                IMAPProtocol p = getProtocol();
                checkExpunged();
                int seqnum = getSequenceNumber();
                Response[] r = p.fetch(seqnum, fitem.getName());
                for (int i = 0; i < r.length; i++) {
                    if (r[i] != null && (r[i] instanceof FetchResponse) && ((FetchResponse) r[i]).getNumber() == seqnum) {
                        handleExtensionFetchItems(((FetchResponse) r[i]).getExtensionItems());
                        if (!(this.items == null || (o = this.items.get(fitem.getName())) == null)) {
                            robj = o;
                        }
                    }
                }
                p.notifyResponseHandlers(r);
                p.handleResult(r[r.length - 1]);
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException pex) {
                forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        return robj;
    }

    public synchronized Object getItem(FetchItem fitem) throws MessagingException {
        Object item;
        item = this.items == null ? null : this.items.get(fitem.getName());
        if (item == null) {
            item = fetchItem(fitem);
        }
        return item;
    }

    private synchronized void loadEnvelope() throws MessagingException {
        if (this.envelope == null) {
            synchronized (getMessageCacheLock()) {
                try {
                    IMAPProtocol p = getProtocol();
                    checkExpunged();
                    int seqnum = getSequenceNumber();
                    Response[] r = p.fetch(seqnum, EnvelopeCmd);
                    for (int i = 0; i < r.length; i++) {
                        if (r[i] != null && (r[i] instanceof FetchResponse) && ((FetchResponse) r[i]).getNumber() == seqnum) {
                            FetchResponse f = (FetchResponse) r[i];
                            int count = f.getItemCount();
                            for (int j = 0; j < count; j++) {
                                Item item = f.getItem(j);
                                if (item instanceof ENVELOPE) {
                                    this.envelope = (ENVELOPE) item;
                                } else if (item instanceof INTERNALDATE) {
                                    this.receivedDate = ((INTERNALDATE) item).getDate();
                                } else if (item instanceof RFC822SIZE) {
                                    this.size = ((RFC822SIZE) item).size;
                                }
                            }
                        }
                    }
                    p.notifyResponseHandlers(r);
                    p.handleResult(r[r.length - 1]);
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.folder, cex.getMessage());
                } catch (ProtocolException pex) {
                    forceCheckExpunged();
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            if (this.envelope == null) {
                throw new MessagingException("Failed to load IMAP envelope");
            }
        }
    }

    private synchronized void loadBODYSTRUCTURE() throws MessagingException {
        if (this.f272bs == null) {
            synchronized (getMessageCacheLock()) {
                try {
                    IMAPProtocol p = getProtocol();
                    checkExpunged();
                    this.f272bs = p.fetchBodyStructure(getSequenceNumber());
                    if (this.f272bs == null) {
                        forceCheckExpunged();
                        throw new MessagingException("Unable to load BODYSTRUCTURE");
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.folder, cex.getMessage());
                } catch (ProtocolException pex) {
                    forceCheckExpunged();
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
    }

    private synchronized void loadHeaders() throws MessagingException {
        if (!this.headersLoaded) {
            InputStream is = null;
            synchronized (getMessageCacheLock()) {
                try {
                    IMAPProtocol p = getProtocol();
                    checkExpunged();
                    if (p.isREV1()) {
                        BODY b = p.peekBody(getSequenceNumber(), toSection("HEADER"));
                        if (b != null) {
                            is = b.getByteArrayInputStream();
                        }
                    } else {
                        RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), "HEADER");
                        if (rd != null) {
                            is = rd.getByteArrayInputStream();
                        }
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.folder, cex.getMessage());
                } catch (ProtocolException pex) {
                    forceCheckExpunged();
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            if (is == null) {
                throw new MessagingException("Cannot load header");
            }
            this.headers = new InternetHeaders(is);
            this.headersLoaded = true;
        }
    }

    private synchronized void loadFlags() throws MessagingException {
        if (this.flags == null) {
            synchronized (getMessageCacheLock()) {
                try {
                    IMAPProtocol p = getProtocol();
                    checkExpunged();
                    this.flags = p.fetchFlags(getSequenceNumber());
                    if (this.flags == null) {
                        this.flags = new Flags();
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this.folder, cex.getMessage());
                } catch (ProtocolException pex) {
                    forceCheckExpunged();
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean areHeadersLoaded() {
        return this.headersLoaded;
    }

    private void setHeadersLoaded(boolean loaded) {
        this.headersLoaded = loaded;
    }

    /* access modifiers changed from: private */
    public boolean isHeaderLoaded(String name) {
        if (this.headersLoaded) {
            return true;
        }
        return this.loadedHeaders.containsKey(name.toUpperCase(Locale.ENGLISH));
    }

    private void setHeaderLoaded(String name) {
        this.loadedHeaders.put(name.toUpperCase(Locale.ENGLISH), name);
    }

    private String toSection(String what) {
        return this.sectionId == null ? what : this.sectionId + "." + what;
    }

    private InternetAddress[] aaclone(InternetAddress[] aa) {
        if (aa == null) {
            return null;
        }
        return (InternetAddress[]) aa.clone();
    }

    /* access modifiers changed from: private */
    public Flags _getFlags() {
        return this.flags;
    }

    /* access modifiers changed from: private */
    public ENVELOPE _getEnvelope() {
        return this.envelope;
    }

    /* access modifiers changed from: private */
    public BODYSTRUCTURE _getBodyStructure() {
        return this.f272bs;
    }

    /* access modifiers changed from: package-private */
    public void _setFlags(Flags flags) {
        this.flags = flags;
    }

    /* access modifiers changed from: package-private */
    public Session _getSession() {
        return this.session;
    }
}
