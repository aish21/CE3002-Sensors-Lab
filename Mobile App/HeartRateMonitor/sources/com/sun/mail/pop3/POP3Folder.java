package com.sun.mail.pop3;

import com.sun.mail.util.MailLogger;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.UIDFolder;

public class POP3Folder extends Folder {
    private boolean doneUidl = false;
    private boolean exists = false;
    private volatile TempFile fileCache = null;
    private boolean forceClose;
    MailLogger logger;
    private POP3Message[] message_cache;
    private String name;
    private volatile boolean opened = false;
    private volatile Protocol port;
    private int size;
    private POP3Store store;
    private int total;

    protected POP3Folder(POP3Store store2, String name2) {
        super(store2);
        this.name = name2;
        this.store = store2;
        if (name2.equalsIgnoreCase("INBOX")) {
            this.exists = true;
        }
        this.logger = new MailLogger(getClass(), "DEBUG POP3", store2.getSession().getDebug(), store2.getSession().getDebugOut());
    }

    public String getName() {
        return this.name;
    }

    public String getFullName() {
        return this.name;
    }

    public Folder getParent() {
        return new DefaultFolder(this.store);
    }

    public boolean exists() {
        return this.exists;
    }

    public Folder[] list(String pattern) throws MessagingException {
        throw new MessagingException("not a directory");
    }

    public char getSeparator() {
        return 0;
    }

    public int getType() {
        return 1;
    }

    public boolean create(int type) throws MessagingException {
        return false;
    }

    public boolean hasNewMessages() throws MessagingException {
        return false;
    }

    public Folder getFolder(String name2) throws MessagingException {
        throw new MessagingException("not a directory");
    }

    public boolean delete(boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("delete");
    }

    public boolean renameTo(Folder f) throws MessagingException {
        throw new MethodNotSupportedException("renameTo");
    }

    public synchronized void open(int mode) throws MessagingException {
        checkClosed();
        if (!this.exists) {
            throw new FolderNotFoundException((Folder) this, "folder is not INBOX");
        }
        try {
            this.port = this.store.getPort(this);
            Status s = this.port.stat();
            this.total = s.total;
            this.size = s.size;
            this.mode = mode;
            if (this.store.useFileCache) {
                this.fileCache = new TempFile(this.store.fileCacheDir);
            }
            this.opened = true;
            this.message_cache = new POP3Message[this.total];
            this.doneUidl = false;
            notifyConnectionListeners(1);
        } catch (IOException ex) {
            this.logger.log(Level.FINE, "failed to create file cache", (Throwable) ex);
            throw ex;
        } catch (IOException ioex) {
            try {
                if (this.port != null) {
                    this.port.quit();
                }
                this.port = null;
                this.store.closePort(this);
            } catch (IOException e) {
                this.port = null;
                this.store.closePort(this);
            } catch (Throwable th) {
                this.port = null;
                this.store.closePort(this);
                throw th;
            }
            throw new MessagingException("Open failed", ioex);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00aa, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        r5.port = null;
        r5.store.closePort(r5);
        r5.message_cache = null;
        r5.opened = false;
        notifyConnectionListeners(3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00bf, code lost:
        if (r5.fileCache != null) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00c1, code lost:
        r5.fileCache.close();
        r5.fileCache = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00c9, code lost:
        throw r3;
     */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00aa A[ExcHandler: all (r3v1 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:3:0x0004] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void close(boolean r6) throws javax.mail.MessagingException {
        /*
            r5 = this;
            monitor-enter(r5)
            r5.checkOpen()     // Catch:{ all -> 0x00a1 }
            com.sun.mail.pop3.POP3Store r3 = r5.store     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            boolean r3 = r3.rsetBeforeQuit     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            if (r3 == 0) goto L_0x0013
            boolean r3 = r5.forceClose     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            if (r3 != 0) goto L_0x0013
            com.sun.mail.pop3.Protocol r3 = r5.port     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            r3.rset()     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
        L_0x0013:
            if (r6 == 0) goto L_0x0066
            int r3 = r5.mode     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            r4 = 2
            if (r3 != r4) goto L_0x0066
            boolean r3 = r5.forceClose     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            if (r3 != 0) goto L_0x0066
            r0 = 0
        L_0x001f:
            com.sun.mail.pop3.POP3Message[] r3 = r5.message_cache     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            int r3 = r3.length     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            if (r0 >= r3) goto L_0x0066
            com.sun.mail.pop3.POP3Message[] r3 = r5.message_cache     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            r2 = r3[r0]     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            if (r2 == 0) goto L_0x0039
            javax.mail.Flags$Flag r3 = javax.mail.Flags.Flag.DELETED     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            boolean r3 = r2.isSet(r3)     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            if (r3 == 0) goto L_0x0039
            com.sun.mail.pop3.Protocol r3 = r5.port     // Catch:{ IOException -> 0x003c, all -> 0x00aa }
            int r4 = r0 + 1
            r3.dele(r4)     // Catch:{ IOException -> 0x003c, all -> 0x00aa }
        L_0x0039:
            int r0 = r0 + 1
            goto L_0x001f
        L_0x003c:
            r1 = move-exception
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            java.lang.String r4 = "Exception deleting messages during close"
            r3.<init>(r4, r1)     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            throw r3     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
        L_0x0045:
            r3 = move-exception
            r3 = 0
            r5.port = r3     // Catch:{ all -> 0x00a1 }
            com.sun.mail.pop3.POP3Store r3 = r5.store     // Catch:{ all -> 0x00a1 }
            r3.closePort(r5)     // Catch:{ all -> 0x00a1 }
            r3 = 0
            r5.message_cache = r3     // Catch:{ all -> 0x00a1 }
            r3 = 0
            r5.opened = r3     // Catch:{ all -> 0x00a1 }
            r3 = 3
            r5.notifyConnectionListeners(r3)     // Catch:{ all -> 0x00a1 }
            com.sun.mail.pop3.TempFile r3 = r5.fileCache     // Catch:{ all -> 0x00a1 }
            if (r3 == 0) goto L_0x0064
            com.sun.mail.pop3.TempFile r3 = r5.fileCache     // Catch:{ all -> 0x00a1 }
            r3.close()     // Catch:{ all -> 0x00a1 }
            r3 = 0
            r5.fileCache = r3     // Catch:{ all -> 0x00a1 }
        L_0x0064:
            monitor-exit(r5)
            return
        L_0x0066:
            r0 = 0
        L_0x0067:
            com.sun.mail.pop3.POP3Message[] r3 = r5.message_cache     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            int r3 = r3.length     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            if (r0 >= r3) goto L_0x0079
            com.sun.mail.pop3.POP3Message[] r3 = r5.message_cache     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            r2 = r3[r0]     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            if (r2 == 0) goto L_0x0076
            r3 = 1
            r2.invalidate(r3)     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
        L_0x0076:
            int r0 = r0 + 1
            goto L_0x0067
        L_0x0079:
            boolean r3 = r5.forceClose     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            if (r3 == 0) goto L_0x00a4
            com.sun.mail.pop3.Protocol r3 = r5.port     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            r3.close()     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
        L_0x0082:
            r3 = 0
            r5.port = r3     // Catch:{ all -> 0x00a1 }
            com.sun.mail.pop3.POP3Store r3 = r5.store     // Catch:{ all -> 0x00a1 }
            r3.closePort(r5)     // Catch:{ all -> 0x00a1 }
            r3 = 0
            r5.message_cache = r3     // Catch:{ all -> 0x00a1 }
            r3 = 0
            r5.opened = r3     // Catch:{ all -> 0x00a1 }
            r3 = 3
            r5.notifyConnectionListeners(r3)     // Catch:{ all -> 0x00a1 }
            com.sun.mail.pop3.TempFile r3 = r5.fileCache     // Catch:{ all -> 0x00a1 }
            if (r3 == 0) goto L_0x0064
            com.sun.mail.pop3.TempFile r3 = r5.fileCache     // Catch:{ all -> 0x00a1 }
            r3.close()     // Catch:{ all -> 0x00a1 }
            r3 = 0
            r5.fileCache = r3     // Catch:{ all -> 0x00a1 }
            goto L_0x0064
        L_0x00a1:
            r3 = move-exception
            monitor-exit(r5)
            throw r3
        L_0x00a4:
            com.sun.mail.pop3.Protocol r3 = r5.port     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            r3.quit()     // Catch:{ IOException -> 0x0045, all -> 0x00aa }
            goto L_0x0082
        L_0x00aa:
            r3 = move-exception
            r4 = 0
            r5.port = r4     // Catch:{ all -> 0x00a1 }
            com.sun.mail.pop3.POP3Store r4 = r5.store     // Catch:{ all -> 0x00a1 }
            r4.closePort(r5)     // Catch:{ all -> 0x00a1 }
            r4 = 0
            r5.message_cache = r4     // Catch:{ all -> 0x00a1 }
            r4 = 0
            r5.opened = r4     // Catch:{ all -> 0x00a1 }
            r4 = 3
            r5.notifyConnectionListeners(r4)     // Catch:{ all -> 0x00a1 }
            com.sun.mail.pop3.TempFile r4 = r5.fileCache     // Catch:{ all -> 0x00a1 }
            if (r4 == 0) goto L_0x00c9
            com.sun.mail.pop3.TempFile r4 = r5.fileCache     // Catch:{ all -> 0x00a1 }
            r4.close()     // Catch:{ all -> 0x00a1 }
            r4 = 0
            r5.fileCache = r4     // Catch:{ all -> 0x00a1 }
        L_0x00c9:
            throw r3     // Catch:{ all -> 0x00a1 }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Folder.close(boolean):void");
    }

    public synchronized boolean isOpen() {
        boolean z = false;
        synchronized (this) {
            if (this.opened) {
                try {
                    if (!this.port.noop()) {
                        throw new IOException("NOOP failed");
                    }
                    z = true;
                } catch (IOException e) {
                    try {
                        close(false);
                    } catch (MessagingException e2) {
                    }
                }
            }
        }
        return z;
    }

    public Flags getPermanentFlags() {
        return new Flags();
    }

    public synchronized int getMessageCount() throws MessagingException {
        int i;
        if (!this.opened) {
            i = -1;
        } else {
            checkReadable();
            i = this.total;
        }
        return i;
    }

    public synchronized Message getMessage(int msgno) throws MessagingException {
        POP3Message m;
        checkOpen();
        m = this.message_cache[msgno - 1];
        if (m == null) {
            m = createMessage(this, msgno);
            this.message_cache[msgno - 1] = m;
        }
        return m;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.sun.mail.pop3.POP3Message} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sun.mail.pop3.POP3Message createMessage(javax.mail.Folder r7, int r8) throws javax.mail.MessagingException {
        /*
            r6 = this;
            r2 = 0
            com.sun.mail.pop3.POP3Store r4 = r6.store
            java.lang.reflect.Constructor<?> r1 = r4.messageConstructor
            if (r1 == 0) goto L_0x001c
            r4 = 2
            java.lang.Object[] r3 = new java.lang.Object[r4]     // Catch:{ Exception -> 0x0024 }
            r4 = 0
            r3[r4] = r6     // Catch:{ Exception -> 0x0024 }
            r4 = 1
            java.lang.Integer r5 = java.lang.Integer.valueOf(r8)     // Catch:{ Exception -> 0x0024 }
            r3[r4] = r5     // Catch:{ Exception -> 0x0024 }
            java.lang.Object r4 = r1.newInstance(r3)     // Catch:{ Exception -> 0x0024 }
            r0 = r4
            com.sun.mail.pop3.POP3Message r0 = (com.sun.mail.pop3.POP3Message) r0     // Catch:{ Exception -> 0x0024 }
            r2 = r0
        L_0x001c:
            if (r2 != 0) goto L_0x0023
            com.sun.mail.pop3.POP3Message r2 = new com.sun.mail.pop3.POP3Message
            r2.<init>(r6, r8)
        L_0x0023:
            return r2
        L_0x0024:
            r4 = move-exception
            goto L_0x001c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Folder.createMessage(javax.mail.Folder, int):com.sun.mail.pop3.POP3Message");
    }

    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("Append not supported");
    }

    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("Expunge not supported");
    }

    public synchronized void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
        checkReadable();
        if (!this.doneUidl && this.store.supportsUidl && fp.contains((FetchProfile.Item) UIDFolder.FetchProfileItem.UID)) {
            String[] uids = new String[this.message_cache.length];
            try {
                if (this.port.uidl(uids)) {
                    for (int i = 0; i < uids.length; i++) {
                        if (uids[i] != null) {
                            ((POP3Message) getMessage(i + 1)).uid = uids[i];
                        }
                    }
                    this.doneUidl = true;
                }
            } catch (EOFException eex) {
                close(false);
                throw new FolderClosedException(this, eex.toString());
            } catch (IOException ex) {
                throw new MessagingException("error getting UIDL", ex);
            }
        }
        if (fp.contains(FetchProfile.Item.ENVELOPE)) {
            for (int i2 = 0; i2 < msgs.length; i2++) {
                try {
                    POP3Message msg = msgs[i2];
                    msg.getHeader("");
                    msg.getSize();
                } catch (MessageRemovedException e) {
                }
            }
        }
    }

    public synchronized String getUID(Message msg) throws MessagingException {
        String str;
        checkOpen();
        if (!(msg instanceof POP3Message)) {
            throw new MessagingException("message is not a POP3Message");
        }
        POP3Message m = (POP3Message) msg;
        try {
            if (!this.store.supportsUidl) {
                str = null;
            } else {
                if (m.uid == "UNKNOWN") {
                    m.uid = this.port.uidl(m.getMessageNumber());
                }
                str = m.uid;
            }
        } catch (EOFException eex) {
            close(false);
            throw new FolderClosedException(this, eex.toString());
        } catch (IOException ex) {
            throw new MessagingException("error getting UIDL", ex);
        }
        return str;
    }

    public synchronized int getSize() throws MessagingException {
        checkOpen();
        return this.size;
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x005e A[SYNTHETIC, Splitter:B:37:0x005e] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0063 A[SYNTHETIC, Splitter:B:40:0x0063] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int[] getSizes() throws javax.mail.MessagingException {
        /*
            r10 = this;
            monitor-enter(r10)
            r10.checkOpen()     // Catch:{ all -> 0x0067 }
            int r8 = r10.total     // Catch:{ all -> 0x0067 }
            int[] r6 = new int[r8]     // Catch:{ all -> 0x0067 }
            r0 = 0
            r2 = 0
            com.sun.mail.pop3.Protocol r8 = r10.port     // Catch:{ IOException -> 0x004d, all -> 0x005b }
            java.io.InputStream r0 = r8.list()     // Catch:{ IOException -> 0x004d, all -> 0x005b }
            com.sun.mail.util.LineInputStream r3 = new com.sun.mail.util.LineInputStream     // Catch:{ IOException -> 0x004d, all -> 0x005b }
            r3.<init>(r0)     // Catch:{ IOException -> 0x004d, all -> 0x005b }
        L_0x0015:
            java.lang.String r1 = r3.readLine()     // Catch:{ IOException -> 0x0075, all -> 0x0072 }
            if (r1 == 0) goto L_0x003d
            java.util.StringTokenizer r7 = new java.util.StringTokenizer     // Catch:{ RuntimeException -> 0x003b }
            r7.<init>(r1)     // Catch:{ RuntimeException -> 0x003b }
            java.lang.String r8 = r7.nextToken()     // Catch:{ RuntimeException -> 0x003b }
            int r4 = java.lang.Integer.parseInt(r8)     // Catch:{ RuntimeException -> 0x003b }
            java.lang.String r8 = r7.nextToken()     // Catch:{ RuntimeException -> 0x003b }
            int r5 = java.lang.Integer.parseInt(r8)     // Catch:{ RuntimeException -> 0x003b }
            if (r4 <= 0) goto L_0x0015
            int r8 = r10.total     // Catch:{ RuntimeException -> 0x003b }
            if (r4 > r8) goto L_0x0015
            int r8 = r4 + -1
            r6[r8] = r5     // Catch:{ RuntimeException -> 0x003b }
            goto L_0x0015
        L_0x003b:
            r8 = move-exception
            goto L_0x0015
        L_0x003d:
            if (r3 == 0) goto L_0x0042
            r3.close()     // Catch:{ IOException -> 0x006a }
        L_0x0042:
            if (r0 == 0) goto L_0x0047
            r0.close()     // Catch:{ IOException -> 0x004a }
        L_0x0047:
            r2 = r3
        L_0x0048:
            monitor-exit(r10)
            return r6
        L_0x004a:
            r8 = move-exception
            r2 = r3
            goto L_0x0048
        L_0x004d:
            r8 = move-exception
        L_0x004e:
            if (r2 == 0) goto L_0x0053
            r2.close()     // Catch:{ IOException -> 0x006c }
        L_0x0053:
            if (r0 == 0) goto L_0x0048
            r0.close()     // Catch:{ IOException -> 0x0059 }
            goto L_0x0048
        L_0x0059:
            r8 = move-exception
            goto L_0x0048
        L_0x005b:
            r8 = move-exception
        L_0x005c:
            if (r2 == 0) goto L_0x0061
            r2.close()     // Catch:{ IOException -> 0x006e }
        L_0x0061:
            if (r0 == 0) goto L_0x0066
            r0.close()     // Catch:{ IOException -> 0x0070 }
        L_0x0066:
            throw r8     // Catch:{ all -> 0x0067 }
        L_0x0067:
            r8 = move-exception
            monitor-exit(r10)
            throw r8
        L_0x006a:
            r8 = move-exception
            goto L_0x0042
        L_0x006c:
            r8 = move-exception
            goto L_0x0053
        L_0x006e:
            r9 = move-exception
            goto L_0x0061
        L_0x0070:
            r9 = move-exception
            goto L_0x0066
        L_0x0072:
            r8 = move-exception
            r2 = r3
            goto L_0x005c
        L_0x0075:
            r8 = move-exception
            r2 = r3
            goto L_0x004e
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Folder.getSizes():int[]");
    }

    public synchronized InputStream listCommand() throws MessagingException, IOException {
        checkOpen();
        return this.port.list();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        this.forceClose = !this.store.finalizeCleanClose;
        try {
            if (this.opened) {
                close(false);
            }
        } finally {
            super.finalize();
            this.forceClose = false;
        }
    }

    private void checkOpen() throws IllegalStateException {
        if (!this.opened) {
            throw new IllegalStateException("Folder is not Open");
        }
    }

    private void checkClosed() throws IllegalStateException {
        if (this.opened) {
            throw new IllegalStateException("Folder is Open");
        }
    }

    private void checkReadable() throws IllegalStateException {
        if (!this.opened || !(this.mode == 1 || this.mode == 2)) {
            throw new IllegalStateException("Folder is not Readable");
        }
    }

    /* access modifiers changed from: package-private */
    public Protocol getProtocol() throws MessagingException {
        Protocol p = this.port;
        checkOpen();
        return p;
    }

    /* access modifiers changed from: protected */
    public void notifyMessageChangedListeners(int type, Message m) {
        super.notifyMessageChangedListeners(type, m);
    }

    /* access modifiers changed from: package-private */
    public TempFile getFileCache() {
        return this.fileCache;
    }
}
