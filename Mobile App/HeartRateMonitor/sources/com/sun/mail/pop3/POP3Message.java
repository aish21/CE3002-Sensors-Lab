package com.sun.mail.pop3;

import com.sun.mail.util.ReadableMime;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Header;
import javax.mail.IllegalWriteException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.SharedInputStream;

public class POP3Message extends MimeMessage implements ReadableMime {
    static final /* synthetic */ boolean $assertionsDisabled = (!POP3Message.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    static final String UNKNOWN = "UNKNOWN";
    private POP3Folder folder;
    private int hdrSize = -1;
    private int msgSize = -1;
    private SoftReference<InputStream> rawData = new SoftReference<>((Object) null);
    String uid = UNKNOWN;

    public POP3Message(Folder folder2, int msgno) throws MessagingException {
        super(folder2, msgno);
        if ($assertionsDisabled || (folder2 instanceof POP3Folder)) {
            this.folder = (POP3Folder) folder2;
            return;
        }
        throw new AssertionError();
    }

    public synchronized void setFlags(Flags newFlags, boolean set) throws MessagingException {
        super.setFlags(newFlags, set);
        if (!this.flags.equals((Flags) this.flags.clone())) {
            this.folder.notifyMessageChangedListeners(1, this);
        }
    }

    public int getSize() throws MessagingException {
        int i;
        try {
            synchronized (this) {
                if (this.msgSize > 0) {
                    i = this.msgSize;
                } else {
                    if (this.headers == null) {
                        loadHeaders();
                    }
                    synchronized (this) {
                        if (this.msgSize < 0) {
                            this.msgSize = this.folder.getProtocol().list(this.msgnum) - this.hdrSize;
                        }
                        i = this.msgSize;
                    }
                }
            }
            return i;
        } catch (EOFException eex) {
            this.folder.close($assertionsDisabled);
            throw new FolderClosedException(this.folder, eex.toString());
        } catch (IOException ex) {
            throw new MessagingException("error getting size", ex);
        }
    }

    private InputStream getRawStream(boolean skipHeader) throws MessagingException {
        InputStream rawcontent;
        int len;
        BufferedOutputStream bos;
        try {
            synchronized (this) {
                rawcontent = this.rawData.get();
                if (rawcontent == null) {
                    TempFile cache = this.folder.getFileCache();
                    if (cache != null) {
                        if (this.folder.logger.isLoggable(Level.FINE)) {
                            this.folder.logger.fine("caching message #" + this.msgnum + " in temp file");
                        }
                        AppendStream os = cache.getAppendStream();
                        bos = new BufferedOutputStream(os);
                        this.folder.getProtocol().retr(this.msgnum, (OutputStream) bos);
                        bos.close();
                        rawcontent = os.getInputStream();
                    } else {
                        rawcontent = this.folder.getProtocol().retr(this.msgnum, this.msgSize > 0 ? this.msgSize + this.hdrSize : 0);
                    }
                    if (rawcontent == null) {
                        this.expunged = true;
                        throw new MessageRemovedException("can't retrieve message #" + this.msgnum + " in POP3Message.getContentStream");
                    }
                    if (this.headers == null || ((POP3Store) this.folder.getStore()).forgetTopHeaders) {
                        this.headers = new InternetHeaders(rawcontent);
                        this.hdrSize = (int) ((SharedInputStream) rawcontent).getPosition();
                    } else {
                        loop0:
                        do {
                            len = 0;
                            while (true) {
                                int c1 = rawcontent.read();
                                if (c1 < 0 || c1 == 10) {
                                    break;
                                } else if (c1 != 13) {
                                    len++;
                                } else if (rawcontent.available() > 0) {
                                    rawcontent.mark(1);
                                    if (rawcontent.read() != 10) {
                                        rawcontent.reset();
                                    }
                                }
                            }
                            if (rawcontent.available() != 0) {
                                break;
                                break;
                            }
                            break;
                        } while (len != 0);
                        this.hdrSize = (int) ((SharedInputStream) rawcontent).getPosition();
                    }
                    this.msgSize = rawcontent.available();
                    this.rawData = new SoftReference<>(rawcontent);
                }
            }
            return ((SharedInputStream) rawcontent).newStream(skipHeader ? (long) this.hdrSize : 0, -1);
        } catch (EOFException eex) {
            this.folder.close($assertionsDisabled);
            throw new FolderClosedException(this.folder, eex.toString());
        } catch (IOException ex) {
            throw new MessagingException("error fetching POP3 content", ex);
        } catch (Throwable th) {
            bos.close();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized InputStream getContentStream() throws MessagingException {
        InputStream cstream;
        if (this.contentStream != null) {
            cstream = ((SharedInputStream) this.contentStream).newStream(0, -1);
        } else {
            cstream = getRawStream(true);
            if (this.folder.getFileCache() != null || ((POP3Store) this.folder.getStore()).keepMessageContent) {
                this.contentStream = ((SharedInputStream) cstream).newStream(0, -1);
            }
        }
        return cstream;
    }

    public InputStream getMimeStream() throws MessagingException {
        return getRawStream($assertionsDisabled);
    }

    public synchronized void invalidate(boolean invalidateHeaders) {
        this.content = null;
        InputStream rstream = this.rawData.get();
        if (rstream != null) {
            try {
                rstream.close();
            } catch (IOException e) {
            }
            this.rawData = new SoftReference<>((Object) null);
        }
        if (this.contentStream != null) {
            try {
                this.contentStream.close();
            } catch (IOException e2) {
            }
            this.contentStream = null;
        }
        this.msgSize = -1;
        if (invalidateHeaders) {
            this.headers = null;
            this.hdrSize = -1;
        }
    }

    public InputStream top(int n) throws MessagingException {
        InputStream pVar;
        try {
            synchronized (this) {
                pVar = this.folder.getProtocol().top(this.msgnum, n);
            }
            return pVar;
        } catch (EOFException eex) {
            this.folder.close($assertionsDisabled);
            throw new FolderClosedException(this.folder, eex.toString());
        } catch (IOException ex) {
            throw new MessagingException("error getting size", ex);
        }
    }

    public String[] getHeader(String name) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getHeader(name, delimiter);
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public Enumeration<Header> getAllHeaders() throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getAllHeaders();
    }

    public Enumeration<Header> getMatchingHeaders(String[] names) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getMatchingHeaders(names);
    }

    public Enumeration<Header> getNonMatchingHeaders(String[] names) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    public Enumeration<String> getAllHeaderLines() throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getAllHeaderLines();
    }

    public Enumeration<String> getMatchingHeaderLines(String[] names) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getMatchingHeaderLines(names);
    }

    public Enumeration<String> getNonMatchingHeaderLines(String[] names) throws MessagingException {
        if (this.headers == null) {
            loadHeaders();
        }
        return this.headers.getNonMatchingHeaderLines(names);
    }

    public void saveChanges() throws MessagingException {
        throw new IllegalWriteException("POP3 messages are read-only");
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:38:0x00a1=Splitter:B:38:0x00a1, B:31:0x0099=Splitter:B:31:0x0099} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void writeTo(java.io.OutputStream r9, java.lang.String[] r10) throws java.io.IOException, javax.mail.MessagingException {
        /*
            r8 = this;
            monitor-enter(r8)
            java.lang.ref.SoftReference<java.io.InputStream> r4 = r8.rawData     // Catch:{ all -> 0x0073 }
            java.lang.Object r3 = r4.get()     // Catch:{ all -> 0x0073 }
            java.io.InputStream r3 = (java.io.InputStream) r3     // Catch:{ all -> 0x0073 }
            if (r3 != 0) goto L_0x0076
            if (r10 != 0) goto L_0x0076
            com.sun.mail.pop3.POP3Folder r4 = r8.folder     // Catch:{ all -> 0x0073 }
            javax.mail.Store r4 = r4.getStore()     // Catch:{ all -> 0x0073 }
            com.sun.mail.pop3.POP3Store r4 = (com.sun.mail.pop3.POP3Store) r4     // Catch:{ all -> 0x0073 }
            boolean r4 = r4.cacheWriteTo     // Catch:{ all -> 0x0073 }
            if (r4 != 0) goto L_0x0076
            com.sun.mail.pop3.POP3Folder r4 = r8.folder     // Catch:{ all -> 0x0073 }
            com.sun.mail.util.MailLogger r4 = r4.logger     // Catch:{ all -> 0x0073 }
            java.util.logging.Level r5 = java.util.logging.Level.FINE     // Catch:{ all -> 0x0073 }
            boolean r4 = r4.isLoggable(r5)     // Catch:{ all -> 0x0073 }
            if (r4 == 0) goto L_0x0041
            com.sun.mail.pop3.POP3Folder r4 = r8.folder     // Catch:{ all -> 0x0073 }
            com.sun.mail.util.MailLogger r4 = r4.logger     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0073 }
            r5.<init>()     // Catch:{ all -> 0x0073 }
            java.lang.String r6 = "streaming msg "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0073 }
            int r6 = r8.msgnum     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0073 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0073 }
            r4.fine(r5)     // Catch:{ all -> 0x0073 }
        L_0x0041:
            com.sun.mail.pop3.POP3Folder r4 = r8.folder     // Catch:{ all -> 0x0073 }
            com.sun.mail.pop3.Protocol r4 = r4.getProtocol()     // Catch:{ all -> 0x0073 }
            int r5 = r8.msgnum     // Catch:{ all -> 0x0073 }
            boolean r4 = r4.retr((int) r5, (java.io.OutputStream) r9)     // Catch:{ all -> 0x0073 }
            if (r4 != 0) goto L_0x009f
            r4 = 1
            r8.expunged = r4     // Catch:{ all -> 0x0073 }
            javax.mail.MessageRemovedException r4 = new javax.mail.MessageRemovedException     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0073 }
            r5.<init>()     // Catch:{ all -> 0x0073 }
            java.lang.String r6 = "can't retrieve message #"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0073 }
            int r6 = r8.msgnum     // Catch:{ all -> 0x0073 }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0073 }
            java.lang.String r6 = " in POP3Message.writeTo"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0073 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0073 }
            r4.<init>(r5)     // Catch:{ all -> 0x0073 }
            throw r4     // Catch:{ all -> 0x0073 }
        L_0x0073:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x0076:
            if (r3 == 0) goto L_0x00a1
            if (r10 != 0) goto L_0x00a1
            javax.mail.internet.SharedInputStream r3 = (javax.mail.internet.SharedInputStream) r3     // Catch:{ all -> 0x0073 }
            r4 = 0
            r6 = -1
            java.io.InputStream r1 = r3.newStream(r4, r6)     // Catch:{ all -> 0x0073 }
            r4 = 16384(0x4000, float:2.2959E-41)
            byte[] r0 = new byte[r4]     // Catch:{ all -> 0x0093 }
        L_0x0088:
            int r2 = r1.read(r0)     // Catch:{ all -> 0x0093 }
            if (r2 <= 0) goto L_0x009a
            r4 = 0
            r9.write(r0, r4, r2)     // Catch:{ all -> 0x0093 }
            goto L_0x0088
        L_0x0093:
            r4 = move-exception
            if (r1 == 0) goto L_0x0099
            r1.close()     // Catch:{ IOException -> 0x00a7 }
        L_0x0099:
            throw r4     // Catch:{ all -> 0x0073 }
        L_0x009a:
            if (r1 == 0) goto L_0x009f
            r1.close()     // Catch:{ IOException -> 0x00a5 }
        L_0x009f:
            monitor-exit(r8)
            return
        L_0x00a1:
            super.writeTo(r9, r10)     // Catch:{ all -> 0x0073 }
            goto L_0x009f
        L_0x00a5:
            r4 = move-exception
            goto L_0x009f
        L_0x00a7:
            r5 = move-exception
            goto L_0x0099
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Message.writeTo(java.io.OutputStream, java.lang.String[]):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0037, code lost:
        if (r3 == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0039, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r0 = getContentStream();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003e, code lost:
        if (r0 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        return;
     */
    /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadHeaders() throws javax.mail.MessagingException {
        /*
            r9 = this;
            r8 = 0
            boolean r5 = $assertionsDisabled
            if (r5 != 0) goto L_0x0011
            boolean r5 = java.lang.Thread.holdsLock(r9)
            if (r5 == 0) goto L_0x0011
            java.lang.AssertionError r5 = new java.lang.AssertionError
            r5.<init>()
            throw r5
        L_0x0011:
            r3 = 0
            monitor-enter(r9)     // Catch:{ EOFException -> 0x0044, IOException -> 0x006a }
            javax.mail.internet.InternetHeaders r5 = r9.headers     // Catch:{ all -> 0x0067 }
            if (r5 == 0) goto L_0x0019
            monitor-exit(r9)     // Catch:{ all -> 0x0067 }
        L_0x0018:
            return
        L_0x0019:
            r4 = 0
            com.sun.mail.pop3.POP3Folder r5 = r9.folder     // Catch:{ all -> 0x0067 }
            javax.mail.Store r5 = r5.getStore()     // Catch:{ all -> 0x0067 }
            com.sun.mail.pop3.POP3Store r5 = (com.sun.mail.pop3.POP3Store) r5     // Catch:{ all -> 0x0067 }
            boolean r5 = r5.disableTop     // Catch:{ all -> 0x0067 }
            if (r5 != 0) goto L_0x0035
            com.sun.mail.pop3.POP3Folder r5 = r9.folder     // Catch:{ all -> 0x0067 }
            com.sun.mail.pop3.Protocol r5 = r5.getProtocol()     // Catch:{ all -> 0x0067 }
            int r6 = r9.msgnum     // Catch:{ all -> 0x0067 }
            r7 = 0
            java.io.InputStream r4 = r5.top(r6, r7)     // Catch:{ all -> 0x0067 }
            if (r4 != 0) goto L_0x0056
        L_0x0035:
            r3 = 1
        L_0x0036:
            monitor-exit(r9)     // Catch:{ all -> 0x0067 }
            if (r3 == 0) goto L_0x0018
            r0 = 0
            java.io.InputStream r0 = r9.getContentStream()     // Catch:{ all -> 0x0078 }
            if (r0 == 0) goto L_0x0018
            r0.close()     // Catch:{ EOFException -> 0x0044, IOException -> 0x006a }
            goto L_0x0018
        L_0x0044:
            r1 = move-exception
            com.sun.mail.pop3.POP3Folder r5 = r9.folder
            r5.close(r8)
            javax.mail.FolderClosedException r5 = new javax.mail.FolderClosedException
            com.sun.mail.pop3.POP3Folder r6 = r9.folder
            java.lang.String r7 = r1.toString()
            r5.<init>(r6, r7)
            throw r5
        L_0x0056:
            int r5 = r4.available()     // Catch:{ all -> 0x0073 }
            r9.hdrSize = r5     // Catch:{ all -> 0x0073 }
            javax.mail.internet.InternetHeaders r5 = new javax.mail.internet.InternetHeaders     // Catch:{ all -> 0x0073 }
            r5.<init>(r4)     // Catch:{ all -> 0x0073 }
            r9.headers = r5     // Catch:{ all -> 0x0073 }
            r4.close()     // Catch:{ all -> 0x0067 }
            goto L_0x0036
        L_0x0067:
            r5 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0067 }
            throw r5     // Catch:{ EOFException -> 0x0044, IOException -> 0x006a }
        L_0x006a:
            r2 = move-exception
            javax.mail.MessagingException r5 = new javax.mail.MessagingException
            java.lang.String r6 = "error loading POP3 headers"
            r5.<init>(r6, r2)
            throw r5
        L_0x0073:
            r5 = move-exception
            r4.close()     // Catch:{ all -> 0x0067 }
            throw r5     // Catch:{ all -> 0x0067 }
        L_0x0078:
            r5 = move-exception
            if (r0 == 0) goto L_0x007e
            r0.close()     // Catch:{ EOFException -> 0x0044, IOException -> 0x006a }
        L_0x007e:
            throw r5     // Catch:{ EOFException -> 0x0044, IOException -> 0x006a }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Message.loadHeaders():void");
    }
}
