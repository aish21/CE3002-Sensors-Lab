package com.sun.mail.imap;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.Utility;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchItem;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.MODSEQ;
import com.sun.mail.imap.protocol.MailboxInfo;
import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.Status;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.imap.protocol.UIDSet;
import com.sun.mail.util.MailLogger;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.ReadOnlyFolderException;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.event.MailEvent;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

public class IMAPFolder extends Folder implements UIDFolder, ResponseHandler {
    static final /* synthetic */ boolean $assertionsDisabled = (!IMAPFolder.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final int ABORTING = 2;
    private static final int IDLE = 1;
    private static final int RUNNING = 0;
    protected static final char UNKNOWN_SEPARATOR = '￿';
    protected volatile String[] attributes;
    protected Flags availableFlags;
    private Status cachedStatus;
    private long cachedStatusTime;
    private MailLogger connectionPoolLogger;
    private boolean doExpungeNotification;
    protected volatile boolean exists;
    protected volatile String fullName;
    private boolean hasMessageCountListener;
    private volatile long highestmodseq;
    /* access modifiers changed from: private */
    public IdleManager idleManager;
    /* access modifiers changed from: private */
    public int idleState;
    protected boolean isNamespace;
    protected MailLogger logger;
    protected MessageCache messageCache;
    protected final Object messageCacheLock;
    protected String name;
    private volatile boolean opened;
    protected Flags permanentFlags;
    protected volatile IMAPProtocol protocol;
    private int realTotal;
    private boolean reallyClosed;
    private volatile int recent;
    protected char separator;
    private volatile int total;
    protected int type;
    private boolean uidNotSticky;
    protected Hashtable<Long, IMAPMessage> uidTable;
    private long uidnext;
    private long uidvalidity;

    public interface ProtocolCommand {
        Object doCommand(IMAPProtocol iMAPProtocol) throws ProtocolException;
    }

    public static class FetchProfileItem extends FetchProfile.Item {
        public static final FetchProfileItem HEADERS = new FetchProfileItem("HEADERS");
        public static final FetchProfileItem INTERNALDATE = new FetchProfileItem("INTERNALDATE");
        public static final FetchProfileItem MESSAGE = new FetchProfileItem("MESSAGE");
        @Deprecated
        public static final FetchProfileItem SIZE = new FetchProfileItem("SIZE");

        protected FetchProfileItem(String name) {
            super(name);
        }
    }

    protected IMAPFolder(String fullName2, char separator2, IMAPStore store, Boolean isNamespace2) {
        super(store);
        int i;
        this.isNamespace = $assertionsDisabled;
        this.messageCacheLock = new Object();
        this.opened = $assertionsDisabled;
        this.reallyClosed = true;
        this.idleState = 0;
        this.total = -1;
        this.recent = -1;
        this.realTotal = -1;
        this.uidvalidity = -1;
        this.uidnext = -1;
        this.uidNotSticky = $assertionsDisabled;
        this.highestmodseq = -1;
        this.doExpungeNotification = true;
        this.cachedStatus = null;
        this.cachedStatusTime = 0;
        this.hasMessageCountListener = $assertionsDisabled;
        if (fullName2 == null) {
            throw new NullPointerException("Folder name is null");
        }
        this.fullName = fullName2;
        this.separator = separator2;
        this.logger = new MailLogger(getClass(), "DEBUG IMAP", store.getSession().getDebug(), store.getSession().getDebugOut());
        this.connectionPoolLogger = store.getConnectionPoolLogger();
        this.isNamespace = $assertionsDisabled;
        if (separator2 != 65535 && separator2 != 0 && (i = this.fullName.indexOf(separator2)) > 0 && i == this.fullName.length() - 1) {
            this.fullName = this.fullName.substring(0, i);
            this.isNamespace = true;
        }
        if (isNamespace2 != null) {
            this.isNamespace = isNamespace2.booleanValue();
        }
    }

    protected IMAPFolder(ListInfo li, IMAPStore store) {
        this(li.name, li.separator, store, (Boolean) null);
        if (li.hasInferiors) {
            this.type |= 2;
        }
        if (li.canOpen) {
            this.type |= 1;
        }
        this.exists = true;
        this.attributes = li.attrs;
    }

    /* access modifiers changed from: protected */
    public void checkExists() throws MessagingException {
        if (!this.exists && !exists()) {
            throw new FolderNotFoundException((Folder) this, this.fullName + " not found");
        }
    }

    /* access modifiers changed from: protected */
    public void checkClosed() {
        if (this.opened) {
            throw new IllegalStateException("This operation is not allowed on an open folder");
        }
    }

    /* access modifiers changed from: protected */
    public void checkOpened() throws FolderClosedException {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.opened) {
        } else {
            if (this.reallyClosed) {
                throw new IllegalStateException("This operation is not allowed on a closed folder");
            }
            throw new FolderClosedException(this, "Lost folder connection to server");
        }
    }

    /* access modifiers changed from: protected */
    public void checkRange(int msgno) throws MessagingException {
        if (msgno < 1) {
            throw new IndexOutOfBoundsException("message number < 1");
        } else if (msgno > this.total) {
            synchronized (this.messageCacheLock) {
                try {
                    keepConnectionAlive($assertionsDisabled);
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            if (msgno > this.total) {
                throw new IndexOutOfBoundsException(msgno + " > " + this.total);
            }
        }
    }

    private void checkFlags(Flags flags) throws MessagingException {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.mode != 2) {
            throw new IllegalStateException("Cannot change flags on READ_ONLY folder: " + this.fullName);
        }
    }

    public synchronized String getName() {
        if (this.name == null) {
            try {
                this.name = this.fullName.substring(this.fullName.lastIndexOf(getSeparator()) + 1);
            } catch (MessagingException e) {
            }
        }
        return this.name;
    }

    public String getFullName() {
        return this.fullName;
    }

    public synchronized Folder getParent() throws MessagingException {
        IMAPFolder defaultFolder;
        char c = getSeparator();
        int index = this.fullName.lastIndexOf(c);
        if (index != -1) {
            defaultFolder = ((IMAPStore) this.store).newIMAPFolder(this.fullName.substring(0, index), c);
        } else {
            defaultFolder = new DefaultFolder((IMAPStore) this.store);
        }
        return defaultFolder;
    }

    public synchronized boolean exists() throws MessagingException {
        final String lname;
        if (!this.isNamespace || this.separator == 0) {
            lname = this.fullName;
        } else {
            lname = this.fullName + this.separator;
        }
        ListInfo[] li = (ListInfo[]) doCommand(new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.list("", lname);
            }
        });
        if (li != null) {
            int i = findName(li, lname);
            this.fullName = li[i].name;
            this.separator = li[i].separator;
            int len = this.fullName.length();
            if (this.separator != 0 && len > 0 && this.fullName.charAt(len - 1) == this.separator) {
                this.fullName = this.fullName.substring(0, len - 1);
            }
            this.type = 0;
            if (li[i].hasInferiors) {
                this.type |= 2;
            }
            if (li[i].canOpen) {
                this.type |= 1;
            }
            this.exists = true;
            this.attributes = li[i].attrs;
        } else {
            this.exists = this.opened;
            this.attributes = null;
        }
        return this.exists;
    }

    private int findName(ListInfo[] li, String lname) {
        int i = 0;
        while (i < li.length && !li[i].name.equals(lname)) {
            i++;
        }
        if (i >= li.length) {
            return 0;
        }
        return i;
    }

    public Folder[] list(String pattern) throws MessagingException {
        return doList(pattern, $assertionsDisabled);
    }

    public Folder[] listSubscribed(String pattern) throws MessagingException {
        return doList(pattern, true);
    }

    private synchronized Folder[] doList(final String pattern, final boolean subscribed) throws MessagingException {
        Folder[] folderArr;
        checkExists();
        if (this.attributes == null || isDirectory()) {
            final char c = getSeparator();
            ListInfo[] li = (ListInfo[]) doCommandIgnoreFailure(new ProtocolCommand() {
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    if (subscribed) {
                        return p.lsub("", IMAPFolder.this.fullName + c + pattern);
                    }
                    return p.list("", IMAPFolder.this.fullName + c + pattern);
                }
            });
            if (li == null) {
                folderArr = new Folder[0];
            } else {
                int start = 0;
                if (li.length > 0 && li[0].name.equals(this.fullName + c)) {
                    start = 1;
                }
                folderArr = new IMAPFolder[(li.length - start)];
                IMAPStore st = (IMAPStore) this.store;
                for (int i = start; i < li.length; i++) {
                    folderArr[i - start] = st.newIMAPFolder(li[i]);
                }
            }
        } else {
            folderArr = new Folder[0];
        }
        return folderArr;
    }

    public synchronized char getSeparator() throws MessagingException {
        if (this.separator == 65535) {
            ListInfo[] li = (ListInfo[]) doCommand(new ProtocolCommand() {
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    if (p.isREV1()) {
                        return p.list(IMAPFolder.this.fullName, "");
                    }
                    return p.list("", IMAPFolder.this.fullName);
                }
            });
            if (li != null) {
                this.separator = li[0].separator;
            } else {
                this.separator = '/';
            }
        }
        return this.separator;
    }

    public synchronized int getType() throws MessagingException {
        if (!this.opened) {
            checkExists();
        } else if (this.attributes == null) {
            exists();
        }
        return this.type;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v8, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.sun.mail.imap.protocol.ListInfo[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isSubscribed() {
        /*
            r6 = this;
            monitor-enter(r6)
            r2 = 0
            boolean r4 = r6.isNamespace     // Catch:{ all -> 0x003d }
            if (r4 == 0) goto L_0x0038
            char r4 = r6.separator     // Catch:{ all -> 0x003d }
            if (r4 == 0) goto L_0x0038
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x003d }
            r4.<init>()     // Catch:{ all -> 0x003d }
            java.lang.String r5 = r6.fullName     // Catch:{ all -> 0x003d }
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ all -> 0x003d }
            char r5 = r6.separator     // Catch:{ all -> 0x003d }
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ all -> 0x003d }
            java.lang.String r3 = r4.toString()     // Catch:{ all -> 0x003d }
        L_0x001f:
            com.sun.mail.imap.IMAPFolder$4 r4 = new com.sun.mail.imap.IMAPFolder$4     // Catch:{ ProtocolException -> 0x0040 }
            r4.<init>(r3)     // Catch:{ ProtocolException -> 0x0040 }
            java.lang.Object r4 = r6.doProtocolCommand(r4)     // Catch:{ ProtocolException -> 0x0040 }
            r0 = r4
            com.sun.mail.imap.protocol.ListInfo[] r0 = (com.sun.mail.imap.protocol.ListInfo[]) r0     // Catch:{ ProtocolException -> 0x0040 }
            r2 = r0
        L_0x002c:
            if (r2 == 0) goto L_0x003b
            int r1 = r6.findName(r2, r3)     // Catch:{ all -> 0x003d }
            r4 = r2[r1]     // Catch:{ all -> 0x003d }
            boolean r4 = r4.canOpen     // Catch:{ all -> 0x003d }
        L_0x0036:
            monitor-exit(r6)
            return r4
        L_0x0038:
            java.lang.String r3 = r6.fullName     // Catch:{ all -> 0x003d }
            goto L_0x001f
        L_0x003b:
            r4 = 0
            goto L_0x0036
        L_0x003d:
            r4 = move-exception
            monitor-exit(r6)
            throw r4
        L_0x0040:
            r4 = move-exception
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.isSubscribed():boolean");
    }

    public synchronized void setSubscribed(final boolean subscribe) throws MessagingException {
        doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                if (subscribe) {
                    p.subscribe(IMAPFolder.this.fullName);
                    return null;
                }
                p.unsubscribe(IMAPFolder.this.fullName);
                return null;
            }
        });
    }

    public synchronized boolean create(final int type2) throws MessagingException {
        boolean retb;
        char c = 0;
        if ((type2 & 1) == 0) {
            c = getSeparator();
        }
        final char sep = c;
        if (doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                ListInfo[] li;
                if ((type2 & 1) == 0) {
                    p.create(IMAPFolder.this.fullName + sep);
                } else {
                    p.create(IMAPFolder.this.fullName);
                    if (!((type2 & 2) == 0 || (li = p.list("", IMAPFolder.this.fullName)) == null || li[0].hasInferiors)) {
                        p.delete(IMAPFolder.this.fullName);
                        throw new ProtocolException("Unsupported type");
                    }
                }
                return Boolean.TRUE;
            }
        }) == null) {
            retb = $assertionsDisabled;
        } else {
            retb = exists();
            if (retb) {
                notifyFolderListeners(1);
            }
        }
        return retb;
    }

    public synchronized boolean hasNewMessages() throws MessagingException {
        final String lname;
        boolean z = true;
        synchronized (this) {
            synchronized (this.messageCacheLock) {
                if (this.opened) {
                    try {
                        keepConnectionAlive(true);
                        if (this.recent <= 0) {
                            z = false;
                        }
                    } catch (ConnectionException cex) {
                        throw new FolderClosedException(this, cex.getMessage());
                    } catch (ProtocolException pex) {
                        throw new MessagingException(pex.getMessage(), pex);
                    }
                } else {
                    if (!this.isNamespace || this.separator == 0) {
                        lname = this.fullName;
                    } else {
                        lname = this.fullName + this.separator;
                    }
                    ListInfo[] li = (ListInfo[]) doCommandIgnoreFailure(new ProtocolCommand() {
                        public Object doCommand(IMAPProtocol p) throws ProtocolException {
                            return p.list("", lname);
                        }
                    });
                    if (li == null) {
                        throw new FolderNotFoundException((Folder) this, this.fullName + " not found");
                    }
                    int i = findName(li, lname);
                    if (li[i].changeState != 1) {
                        if (li[i].changeState == 2) {
                            z = false;
                        } else {
                            try {
                                if (getStatus().recent <= 0) {
                                    z = false;
                                }
                            } catch (BadCommandException e) {
                                z = false;
                            } catch (ConnectionException cex2) {
                                throw new StoreClosedException(this.store, cex2.getMessage());
                            } catch (ProtocolException pex2) {
                                throw new MessagingException(pex2.getMessage(), pex2);
                            }
                        }
                    }
                }
            }
        }
        return z;
    }

    public synchronized Folder getFolder(String name2) throws MessagingException {
        char c;
        if (this.attributes == null || isDirectory()) {
            c = getSeparator();
        } else {
            throw new MessagingException("Cannot contain subfolders");
        }
        return ((IMAPStore) this.store).newIMAPFolder(this.fullName + c + name2, c);
    }

    public synchronized boolean delete(boolean recurse) throws MessagingException {
        boolean z = $assertionsDisabled;
        synchronized (this) {
            checkClosed();
            if (recurse) {
                Folder[] f = list();
                for (Folder delete : f) {
                    delete.delete(recurse);
                }
            }
            if (doCommandIgnoreFailure(new ProtocolCommand() {
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    p.delete(IMAPFolder.this.fullName);
                    return Boolean.TRUE;
                }
            }) != null) {
                this.exists = $assertionsDisabled;
                this.attributes = null;
                notifyFolderListeners(2);
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean renameTo(final Folder f) throws MessagingException {
        boolean z = $assertionsDisabled;
        synchronized (this) {
            checkClosed();
            checkExists();
            if (f.getStore() != this.store) {
                throw new MessagingException("Can't rename across Stores");
            } else if (doCommandIgnoreFailure(new ProtocolCommand() {
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    p.rename(IMAPFolder.this.fullName, f.getFullName());
                    return Boolean.TRUE;
                }
            }) != null) {
                this.exists = $assertionsDisabled;
                this.attributes = null;
                notifyFolderRenamedListeners(f);
                z = true;
            }
        }
        return z;
    }

    public synchronized void open(int mode) throws MessagingException {
        open(mode, (ResyncData) null);
    }

    public synchronized List<MailEvent> open(int mode, ResyncData rd) throws MessagingException {
        List<MailEvent> openEvents;
        MailboxInfo mi;
        long[] luid;
        checkClosed();
        this.protocol = ((IMAPStore) this.store).getProtocol(this);
        openEvents = null;
        synchronized (this.messageCacheLock) {
            this.protocol.addResponseHandler(this);
            if (rd != null) {
                try {
                    if (rd == ResyncData.CONDSTORE) {
                        if (!this.protocol.isEnabled("CONDSTORE") && !this.protocol.isEnabled("QRESYNC")) {
                            if (this.protocol.hasCapability("CONDSTORE")) {
                                this.protocol.enable("CONDSTORE");
                            } else {
                                this.protocol.enable("QRESYNC");
                            }
                        }
                    } else if (!this.protocol.isEnabled("QRESYNC")) {
                        this.protocol.enable("QRESYNC");
                    }
                } catch (CommandFailedException cex) {
                    checkExists();
                    if ((this.type & 1) == 0) {
                        throw new MessagingException("folder cannot contain messages");
                    }
                    throw new MessagingException(cex.getMessage(), cex);
                } catch (ProtocolException pex) {
                    throw logoutAndThrow(pex.getMessage(), pex);
                } catch (Throwable th) {
                    releaseProtocol($assertionsDisabled);
                    throw th;
                }
            }
            if (mode == 1) {
                mi = this.protocol.examine(this.fullName, rd);
            } else {
                mi = this.protocol.select(this.fullName, rd);
            }
            try {
                if (mi.mode == mode || (mode == 2 && mi.mode == 1 && ((IMAPStore) this.store).allowReadOnlySelect())) {
                    this.opened = true;
                    this.reallyClosed = $assertionsDisabled;
                    this.mode = mi.mode;
                    this.availableFlags = mi.availableFlags;
                    this.permanentFlags = mi.permanentFlags;
                    int i = mi.total;
                    this.realTotal = i;
                    this.total = i;
                    this.recent = mi.recent;
                    this.uidvalidity = mi.uidvalidity;
                    this.uidnext = mi.uidnext;
                    this.uidNotSticky = mi.uidNotSticky;
                    this.highestmodseq = mi.highestmodseq;
                    this.messageCache = new MessageCache(this, (IMAPStore) this.store, this.total);
                    if (mi.responses != null) {
                        List<MailEvent> openEvents2 = new ArrayList<>();
                        try {
                            for (IMAPResponse ir : mi.responses) {
                                if (ir.keyEquals("VANISHED")) {
                                    String[] s = ir.readAtomStringList();
                                    if (s != null && s.length == 1 && s[0].equalsIgnoreCase("EARLIER") && (luid = UIDSet.toArray(UIDSet.parseUIDSets(ir.readAtom()), this.uidnext)) != null && luid.length > 0) {
                                        openEvents2.add(new MessageVanishedEvent(this, luid));
                                    }
                                } else if (!ir.keyEquals("FETCH")) {
                                    continue;
                                } else if ($assertionsDisabled || (ir instanceof FetchResponse)) {
                                    Message msg = processFetchResponse((FetchResponse) ir);
                                    if (msg != null) {
                                        openEvents2.add(new MessageChangedEvent(this, 1, msg));
                                    }
                                } else {
                                    throw new AssertionError("!ir instanceof FetchResponse");
                                }
                            }
                            openEvents = openEvents2;
                        } catch (Throwable th2) {
                            th = th2;
                            List<MailEvent> list = openEvents2;
                            throw th;
                        }
                    }
                    this.exists = true;
                    this.attributes = null;
                    this.type = 1;
                    notifyConnectionListeners(1);
                } else {
                    throw cleanupAndThrow(new ReadOnlyFolderException(this, "Cannot open in desired mode"));
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        return openEvents;
    }

    private MessagingException cleanupAndThrow(MessagingException ife) {
        try {
            this.protocol.close();
            releaseProtocol(true);
        } catch (ProtocolException pex) {
            addSuppressed(ife, logoutAndThrow(pex.getMessage(), pex));
            releaseProtocol($assertionsDisabled);
        } catch (Throwable thr) {
            addSuppressed(ife, thr);
        }
        return ife;
    }

    private MessagingException logoutAndThrow(String why, ProtocolException t) {
        MessagingException ife = new MessagingException(why, t);
        try {
            this.protocol.logout();
        } catch (Throwable thr) {
            addSuppressed(ife, thr);
        }
        return ife;
    }

    private void addSuppressed(Throwable ife, Throwable thr) {
        if (isRecoverable(thr)) {
            ife.addSuppressed(thr);
            return;
        }
        thr.addSuppressed(ife);
        if (thr instanceof Error) {
            throw ((Error) thr);
        } else if (thr instanceof RuntimeException) {
            throw ((RuntimeException) thr);
        } else {
            throw new RuntimeException("unexpected exception", thr);
        }
    }

    private boolean isRecoverable(Throwable t) {
        if ((t instanceof Exception) || (t instanceof LinkageError)) {
            return true;
        }
        return $assertionsDisabled;
    }

    public synchronized void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
        boolean isRev1;
        FetchItem[] fitems;
        synchronized (this.messageCacheLock) {
            checkOpened();
            isRev1 = this.protocol.isREV1();
            fitems = this.protocol.getFetchItems();
        }
        StringBuilder command = new StringBuilder();
        boolean first = true;
        boolean allHeaders = $assertionsDisabled;
        if (fp.contains(FetchProfile.Item.ENVELOPE)) {
            command.append(getEnvelopeCommand());
            first = $assertionsDisabled;
        }
        if (fp.contains(FetchProfile.Item.FLAGS)) {
            command.append(first ? "FLAGS" : " FLAGS");
            first = $assertionsDisabled;
        }
        if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
            command.append(first ? "BODYSTRUCTURE" : " BODYSTRUCTURE");
            first = $assertionsDisabled;
        }
        if (fp.contains((FetchProfile.Item) UIDFolder.FetchProfileItem.UID)) {
            command.append(first ? "UID" : " UID");
            first = $assertionsDisabled;
        }
        if (fp.contains((FetchProfile.Item) FetchProfileItem.HEADERS)) {
            allHeaders = true;
            if (isRev1) {
                command.append(first ? "BODY.PEEK[HEADER]" : " BODY.PEEK[HEADER]");
            } else {
                command.append(first ? "RFC822.HEADER" : " RFC822.HEADER");
            }
            first = $assertionsDisabled;
        }
        if (fp.contains((FetchProfile.Item) FetchProfileItem.MESSAGE)) {
            allHeaders = true;
            if (isRev1) {
                command.append(first ? "BODY.PEEK[]" : " BODY.PEEK[]");
            } else {
                command.append(first ? "RFC822" : " RFC822");
            }
            first = $assertionsDisabled;
        }
        if (fp.contains(FetchProfile.Item.SIZE) || fp.contains((FetchProfile.Item) FetchProfileItem.SIZE)) {
            command.append(first ? "RFC822.SIZE" : " RFC822.SIZE");
            first = $assertionsDisabled;
        }
        if (fp.contains((FetchProfile.Item) FetchProfileItem.INTERNALDATE)) {
            command.append(first ? "INTERNALDATE" : " INTERNALDATE");
            first = $assertionsDisabled;
        }
        String[] hdrs = null;
        if (!allHeaders) {
            hdrs = fp.getHeaderNames();
            if (hdrs.length > 0) {
                if (!first) {
                    command.append(" ");
                }
                command.append(createHeaderCommand(hdrs, isRev1));
            }
        }
        for (int i = 0; i < fitems.length; i++) {
            if (fp.contains(fitems[i].getFetchProfileItem())) {
                if (command.length() != 0) {
                    command.append(" ");
                }
                command.append(fitems[i].getName());
            }
        }
        Utility.Condition condition = new IMAPMessage.FetchProfileCondition(fp, fitems);
        synchronized (this.messageCacheLock) {
            checkOpened();
            MessageSet[] msgsets = Utility.toMessageSetSorted(msgs, condition);
            if (msgsets != null) {
                Response[] r = null;
                ArrayList arrayList = new ArrayList();
                try {
                    r = getProtocol().fetch(msgsets, command.toString());
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (CommandFailedException e) {
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
                if (r != null) {
                    for (int i2 = 0; i2 < r.length; i2++) {
                        if (r[i2] != null) {
                            if (!(r[i2] instanceof FetchResponse)) {
                                arrayList.add(r[i2]);
                            } else {
                                FetchResponse f = (FetchResponse) r[i2];
                                IMAPMessage msg = getMessageBySeqNumber(f.getNumber());
                                int count = f.getItemCount();
                                boolean unsolicitedFlags = $assertionsDisabled;
                                for (int j = 0; j < count; j++) {
                                    Item item = f.getItem(j);
                                    if ((item instanceof Flags) && (!fp.contains(FetchProfile.Item.FLAGS) || msg == null)) {
                                        unsolicitedFlags = true;
                                    } else if (msg != null) {
                                        msg.handleFetchItem(item, hdrs, allHeaders);
                                    }
                                }
                                if (msg != null) {
                                    msg.handleExtensionFetchItems(f.getExtensionItems());
                                }
                                if (unsolicitedFlags) {
                                    arrayList.add(f);
                                }
                            }
                        }
                    }
                    if (!arrayList.isEmpty()) {
                        Response[] responses = new Response[arrayList.size()];
                        arrayList.toArray(responses);
                        handleResponses(responses);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getEnvelopeCommand() {
        return "ENVELOPE INTERNALDATE RFC822.SIZE";
    }

    /* access modifiers changed from: protected */
    public IMAPMessage newIMAPMessage(int msgnum) {
        return new IMAPMessage(this, msgnum);
    }

    private String createHeaderCommand(String[] hdrs, boolean isRev1) {
        StringBuilder sb;
        if (isRev1) {
            sb = new StringBuilder("BODY.PEEK[HEADER.FIELDS (");
        } else {
            sb = new StringBuilder("RFC822.HEADER.LINES (");
        }
        for (int i = 0; i < hdrs.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(hdrs[i]);
        }
        if (isRev1) {
            sb.append(")]");
        } else {
            sb.append(")");
        }
        return sb.toString();
    }

    public synchronized void setFlags(Message[] msgs, Flags flag, boolean value) throws MessagingException {
        checkOpened();
        checkFlags(flag);
        if (msgs.length != 0) {
            synchronized (this.messageCacheLock) {
                try {
                    IMAPProtocol p = getProtocol();
                    MessageSet[] ms = Utility.toMessageSetSorted(msgs, (Utility.Condition) null);
                    if (ms == null) {
                        throw new MessageRemovedException("Messages have been removed");
                    }
                    p.storeFlags(ms, flag, value);
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
    }

    public synchronized void setFlags(int start, int end, Flags flag, boolean value) throws MessagingException {
        checkOpened();
        Message[] msgs = new Message[((end - start) + 1)];
        int n = start;
        int i = 0;
        while (n <= end) {
            msgs[i] = getMessage(n);
            n++;
            i++;
        }
        setFlags(msgs, flag, value);
    }

    public synchronized void setFlags(int[] msgnums, Flags flag, boolean value) throws MessagingException {
        checkOpened();
        Message[] msgs = new Message[msgnums.length];
        for (int i = 0; i < msgnums.length; i++) {
            msgs[i] = getMessage(msgnums[i]);
        }
        setFlags(msgs, flag, value);
    }

    public synchronized void close(boolean expunge) throws MessagingException {
        close(expunge, $assertionsDisabled);
    }

    public synchronized void forceClose() throws MessagingException {
        close($assertionsDisabled, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:86:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void close(boolean r11, boolean r12) throws javax.mail.MessagingException {
        /*
            r10 = this;
            boolean r5 = $assertionsDisabled
            if (r5 != 0) goto L_0x0010
            boolean r5 = java.lang.Thread.holdsLock(r10)
            if (r5 != 0) goto L_0x0010
            java.lang.AssertionError r5 = new java.lang.AssertionError
            r5.<init>()
            throw r5
        L_0x0010:
            java.lang.Object r6 = r10.messageCacheLock
            monitor-enter(r6)
            boolean r5 = r10.opened     // Catch:{ all -> 0x0023 }
            if (r5 != 0) goto L_0x0026
            boolean r5 = r10.reallyClosed     // Catch:{ all -> 0x0023 }
            if (r5 == 0) goto L_0x0026
            java.lang.IllegalStateException r5 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0023 }
            java.lang.String r7 = "This operation is not allowed on a closed folder"
            r5.<init>(r7)     // Catch:{ all -> 0x0023 }
            throw r5     // Catch:{ all -> 0x0023 }
        L_0x0023:
            r5 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x0023 }
            throw r5
        L_0x0026:
            r5 = 1
            r10.reallyClosed = r5     // Catch:{ all -> 0x0023 }
            boolean r5 = r10.opened     // Catch:{ all -> 0x0023 }
            if (r5 != 0) goto L_0x002f
            monitor-exit(r6)     // Catch:{ all -> 0x0023 }
        L_0x002e:
            return
        L_0x002f:
            r3 = 1
            r10.waitIfIdle()     // Catch:{ ProtocolException -> 0x0078 }
            if (r12 == 0) goto L_0x0052
            com.sun.mail.util.MailLogger r5 = r10.logger     // Catch:{ ProtocolException -> 0x0078 }
            java.util.logging.Level r7 = java.util.logging.Level.FINE     // Catch:{ ProtocolException -> 0x0078 }
            java.lang.String r8 = "forcing folder {0} to close"
            java.lang.String r9 = r10.fullName     // Catch:{ ProtocolException -> 0x0078 }
            r5.log((java.util.logging.Level) r7, (java.lang.String) r8, (java.lang.Object) r9)     // Catch:{ ProtocolException -> 0x0078 }
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x0078 }
            if (r5 == 0) goto L_0x0049
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x0078 }
            r5.disconnect()     // Catch:{ ProtocolException -> 0x0078 }
        L_0x0049:
            boolean r5 = r10.opened     // Catch:{ all -> 0x0023 }
            if (r5 == 0) goto L_0x0050
            r10.cleanup(r3)     // Catch:{ all -> 0x0023 }
        L_0x0050:
            monitor-exit(r6)     // Catch:{ all -> 0x0023 }
            goto L_0x002e
        L_0x0052:
            javax.mail.Store r5 = r10.store     // Catch:{ ProtocolException -> 0x0078 }
            com.sun.mail.imap.IMAPStore r5 = (com.sun.mail.imap.IMAPStore) r5     // Catch:{ ProtocolException -> 0x0078 }
            boolean r5 = r5.isConnectionPoolFull()     // Catch:{ ProtocolException -> 0x0078 }
            if (r5 == 0) goto L_0x008c
            com.sun.mail.util.MailLogger r5 = r10.logger     // Catch:{ ProtocolException -> 0x0078 }
            java.lang.String r7 = "pool is full, not adding an Authenticated connection"
            r5.fine(r7)     // Catch:{ ProtocolException -> 0x0078 }
            if (r11 == 0) goto L_0x006e
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x0078 }
            if (r5 == 0) goto L_0x006e
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x0078 }
            r5.close()     // Catch:{ ProtocolException -> 0x0078 }
        L_0x006e:
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x0078 }
            if (r5 == 0) goto L_0x0049
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x0078 }
            r5.logout()     // Catch:{ ProtocolException -> 0x0078 }
            goto L_0x0049
        L_0x0078:
            r1 = move-exception
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ all -> 0x0083 }
            java.lang.String r7 = r1.getMessage()     // Catch:{ all -> 0x0083 }
            r5.<init>(r7, r1)     // Catch:{ all -> 0x0083 }
            throw r5     // Catch:{ all -> 0x0083 }
        L_0x0083:
            r5 = move-exception
            boolean r7 = r10.opened     // Catch:{ all -> 0x0023 }
            if (r7 == 0) goto L_0x008b
            r10.cleanup(r3)     // Catch:{ all -> 0x0023 }
        L_0x008b:
            throw r5     // Catch:{ all -> 0x0023 }
        L_0x008c:
            if (r11 != 0) goto L_0x00c5
            int r5 = r10.mode     // Catch:{ ProtocolException -> 0x0078 }
            r7 = 2
            if (r5 != r7) goto L_0x00c5
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x00a7 }
            if (r5 == 0) goto L_0x00aa
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x00a7 }
            java.lang.String r7 = "UNSELECT"
            boolean r5 = r5.hasCapability(r7)     // Catch:{ ProtocolException -> 0x00a7 }
            if (r5 == 0) goto L_0x00aa
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x00a7 }
            r5.unselect()     // Catch:{ ProtocolException -> 0x00a7 }
            goto L_0x0049
        L_0x00a7:
            r2 = move-exception
            r3 = 0
            goto L_0x0049
        L_0x00aa:
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x00a7 }
            if (r5 == 0) goto L_0x0049
            r4 = 1
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ CommandFailedException -> 0x00c2 }
            java.lang.String r7 = r10.fullName     // Catch:{ CommandFailedException -> 0x00c2 }
            r5.examine(r7)     // Catch:{ CommandFailedException -> 0x00c2 }
        L_0x00b6:
            if (r4 == 0) goto L_0x0049
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x00a7 }
            if (r5 == 0) goto L_0x0049
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x00a7 }
            r5.close()     // Catch:{ ProtocolException -> 0x00a7 }
            goto L_0x0049
        L_0x00c2:
            r0 = move-exception
            r4 = 0
            goto L_0x00b6
        L_0x00c5:
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x0078 }
            if (r5 == 0) goto L_0x0049
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r10.protocol     // Catch:{ ProtocolException -> 0x0078 }
            r5.close()     // Catch:{ ProtocolException -> 0x0078 }
            goto L_0x0049
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.close(boolean, boolean):void");
    }

    private void cleanup(boolean returnToPool) {
        if ($assertionsDisabled || Thread.holdsLock(this.messageCacheLock)) {
            releaseProtocol(returnToPool);
            this.messageCache = null;
            this.uidTable = null;
            this.exists = $assertionsDisabled;
            this.attributes = null;
            this.opened = $assertionsDisabled;
            this.idleState = 0;
            this.messageCacheLock.notifyAll();
            notifyConnectionListeners(3);
            return;
        }
        throw new AssertionError();
    }

    public synchronized boolean isOpen() {
        synchronized (this.messageCacheLock) {
            if (this.opened) {
                try {
                    keepConnectionAlive($assertionsDisabled);
                } catch (ProtocolException e) {
                }
            }
        }
        return this.opened;
    }

    public synchronized Flags getPermanentFlags() {
        Flags flags;
        if (this.permanentFlags == null) {
            flags = null;
        } else {
            flags = (Flags) this.permanentFlags.clone();
        }
        return flags;
    }

    public synchronized int getMessageCount() throws MessagingException {
        int i;
        synchronized (this.messageCacheLock) {
            if (this.opened) {
                try {
                    keepConnectionAlive(true);
                    i = this.total;
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            } else {
                checkExists();
                try {
                    i = getStatus().total;
                } catch (BadCommandException e) {
                    IMAPProtocol p = null;
                    try {
                        p = getStoreProtocol();
                        MailboxInfo minfo = p.examine(this.fullName);
                        p.close();
                        i = minfo.total;
                        releaseStoreProtocol(p);
                    } catch (ProtocolException pex2) {
                        throw new MessagingException(pex2.getMessage(), pex2);
                    } catch (Throwable th) {
                        releaseStoreProtocol(p);
                        throw th;
                    }
                } catch (ConnectionException cex2) {
                    throw new StoreClosedException(this.store, cex2.getMessage());
                } catch (ProtocolException pex3) {
                    throw new MessagingException(pex3.getMessage(), pex3);
                }
            }
        }
        return i;
    }

    public synchronized int getNewMessageCount() throws MessagingException {
        int i;
        synchronized (this.messageCacheLock) {
            if (this.opened) {
                try {
                    keepConnectionAlive(true);
                    i = this.recent;
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            } else {
                checkExists();
                try {
                    i = getStatus().recent;
                } catch (BadCommandException e) {
                    IMAPProtocol p = null;
                    try {
                        p = getStoreProtocol();
                        MailboxInfo minfo = p.examine(this.fullName);
                        p.close();
                        i = minfo.recent;
                        releaseStoreProtocol(p);
                    } catch (ProtocolException pex2) {
                        throw new MessagingException(pex2.getMessage(), pex2);
                    } catch (Throwable th) {
                        releaseStoreProtocol(p);
                        throw th;
                    }
                } catch (ConnectionException cex2) {
                    throw new StoreClosedException(this.store, cex2.getMessage());
                } catch (ProtocolException pex3) {
                    throw new MessagingException(pex3.getMessage(), pex3);
                }
            }
        }
        return i;
    }

    public synchronized int getUnreadMessageCount() throws MessagingException {
        int length;
        if (!this.opened) {
            checkExists();
            try {
                length = getStatus().unseen;
            } catch (BadCommandException e) {
                length = -1;
            } catch (ConnectionException cex) {
                throw new StoreClosedException(this.store, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        } else {
            Flags f = new Flags();
            f.add(Flags.Flag.SEEN);
            try {
                synchronized (this.messageCacheLock) {
                    length = getProtocol().search(new FlagTerm(f, $assertionsDisabled)).length;
                }
            } catch (ConnectionException cex2) {
                throw new FolderClosedException(this, cex2.getMessage());
            } catch (ProtocolException pex2) {
                throw new MessagingException(pex2.getMessage(), pex2);
            }
        }
        return length;
    }

    public synchronized int getDeletedMessageCount() throws MessagingException {
        int length;
        if (!this.opened) {
            checkExists();
            length = -1;
        } else {
            Flags f = new Flags();
            f.add(Flags.Flag.DELETED);
            try {
                synchronized (this.messageCacheLock) {
                    length = getProtocol().search(new FlagTerm(f, true)).length;
                }
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        return length;
    }

    private Status getStatus() throws ProtocolException {
        int statusCacheTimeout = ((IMAPStore) this.store).getStatusCacheTimeout();
        if (statusCacheTimeout > 0 && this.cachedStatus != null && System.currentTimeMillis() - this.cachedStatusTime < ((long) statusCacheTimeout)) {
            return this.cachedStatus;
        }
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            Status s = p.status(this.fullName, (String[]) null);
            if (statusCacheTimeout > 0) {
                this.cachedStatus = s;
                this.cachedStatusTime = System.currentTimeMillis();
            }
            return s;
        } finally {
            releaseStoreProtocol(p);
        }
    }

    public synchronized Message getMessage(int msgnum) throws MessagingException {
        checkOpened();
        checkRange(msgnum);
        return this.messageCache.getMessage(msgnum);
    }

    public synchronized Message[] getMessages() throws MessagingException {
        Message[] msgs;
        checkOpened();
        int total2 = getMessageCount();
        msgs = new Message[total2];
        for (int i = 1; i <= total2; i++) {
            msgs[i - 1] = this.messageCache.getMessage(i);
        }
        return msgs;
    }

    public synchronized void appendMessages(Message[] msgs) throws MessagingException {
        checkExists();
        int maxsize = ((IMAPStore) this.store).getAppendBufferSize();
        for (Message m : msgs) {
            Date d = m.getReceivedDate();
            if (d == null) {
                d = m.getSentDate();
            }
            final Date dd = d;
            final Flags f = m.getFlags();
            try {
                final MessageLiteral mos = new MessageLiteral(m, m.getSize() > maxsize ? 0 : maxsize);
                doCommand(new ProtocolCommand() {
                    public Object doCommand(IMAPProtocol p) throws ProtocolException {
                        p.append(IMAPFolder.this.fullName, f, dd, mos);
                        return null;
                    }
                });
            } catch (IOException ex) {
                throw new MessagingException("IOException while appending messages", ex);
            } catch (MessageRemovedException e) {
            }
        }
    }

    public synchronized AppendUID[] appendUIDMessages(Message[] msgs) throws MessagingException {
        AppendUID[] uids;
        int i;
        checkExists();
        int maxsize = ((IMAPStore) this.store).getAppendBufferSize();
        uids = new AppendUID[msgs.length];
        for (int i2 = 0; i2 < msgs.length; i2++) {
            Message m = msgs[i2];
            try {
                if (m.getSize() > maxsize) {
                    i = 0;
                } else {
                    i = maxsize;
                }
                final MessageLiteral mos = new MessageLiteral(m, i);
                Date d = m.getReceivedDate();
                if (d == null) {
                    d = m.getSentDate();
                }
                final Date dd = d;
                final Flags f = m.getFlags();
                uids[i2] = (AppendUID) doCommand(new ProtocolCommand() {
                    public Object doCommand(IMAPProtocol p) throws ProtocolException {
                        return p.appenduid(IMAPFolder.this.fullName, f, dd, mos);
                    }
                });
            } catch (IOException ex) {
                throw new MessagingException("IOException while appending messages", ex);
            } catch (MessageRemovedException e) {
            }
        }
        return uids;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: javax.mail.internet.MimeMessage[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized javax.mail.Message[] addMessages(javax.mail.Message[] r9) throws javax.mail.MessagingException {
        /*
            r8 = this;
            monitor-enter(r8)
            r8.checkOpened()     // Catch:{ all -> 0x0028 }
            int r4 = r9.length     // Catch:{ all -> 0x0028 }
            javax.mail.internet.MimeMessage[] r2 = new javax.mail.internet.MimeMessage[r4]     // Catch:{ all -> 0x0028 }
            com.sun.mail.imap.AppendUID[] r3 = r8.appendUIDMessages(r9)     // Catch:{ all -> 0x0028 }
            r1 = 0
        L_0x000c:
            int r4 = r3.length     // Catch:{ all -> 0x0028 }
            if (r1 >= r4) goto L_0x0026
            r0 = r3[r1]     // Catch:{ all -> 0x0028 }
            if (r0 == 0) goto L_0x0023
            long r4 = r0.uidvalidity     // Catch:{ all -> 0x0028 }
            long r6 = r8.uidvalidity     // Catch:{ all -> 0x0028 }
            int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r4 != 0) goto L_0x0023
            long r4 = r0.uid     // Catch:{ MessagingException -> 0x002b }
            javax.mail.Message r4 = r8.getMessageByUID(r4)     // Catch:{ MessagingException -> 0x002b }
            r2[r1] = r4     // Catch:{ MessagingException -> 0x002b }
        L_0x0023:
            int r1 = r1 + 1
            goto L_0x000c
        L_0x0026:
            monitor-exit(r8)
            return r2
        L_0x0028:
            r4 = move-exception
            monitor-exit(r8)
            throw r4
        L_0x002b:
            r4 = move-exception
            goto L_0x0023
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.addMessages(javax.mail.Message[]):javax.mail.Message[]");
    }

    public synchronized void copyMessages(Message[] msgs, Folder folder) throws MessagingException {
        copymoveMessages(msgs, folder, $assertionsDisabled);
    }

    public synchronized AppendUID[] copyUIDMessages(Message[] msgs, Folder folder) throws MessagingException {
        return copymoveUIDMessages(msgs, folder, $assertionsDisabled);
    }

    public synchronized void moveMessages(Message[] msgs, Folder folder) throws MessagingException {
        copymoveMessages(msgs, folder, true);
    }

    public synchronized AppendUID[] moveUIDMessages(Message[] msgs, Folder folder) throws MessagingException {
        return copymoveUIDMessages(msgs, folder, true);
    }

    private synchronized void copymoveMessages(Message[] msgs, Folder folder, boolean move) throws MessagingException {
        checkOpened();
        if (msgs.length != 0) {
            if (folder.getStore() == this.store) {
                synchronized (this.messageCacheLock) {
                    try {
                        IMAPProtocol p = getProtocol();
                        MessageSet[] ms = Utility.toMessageSet(msgs, (Utility.Condition) null);
                        if (ms == null) {
                            throw new MessageRemovedException("Messages have been removed");
                        } else if (move) {
                            p.move(ms, folder.getFullName());
                        } else {
                            p.copy(ms, folder.getFullName());
                        }
                    } catch (CommandFailedException cfx) {
                        if (cfx.getMessage().indexOf("TRYCREATE") != -1) {
                            throw new FolderNotFoundException(folder, folder.getFullName() + " does not exist");
                        }
                        throw new MessagingException(cfx.getMessage(), cfx);
                    } catch (ConnectionException cex) {
                        throw new FolderClosedException(this, cex.getMessage());
                    } catch (ProtocolException pex) {
                        throw new MessagingException(pex.getMessage(), pex);
                    }
                }
            } else if (move) {
                throw new MessagingException("Move between stores not supported");
            } else {
                super.copyMessages(msgs, folder);
            }
        }
    }

    private synchronized AppendUID[] copymoveUIDMessages(Message[] msgs, Folder folder, boolean move) throws MessagingException {
        CopyUID cuid;
        AppendUID[] result;
        String str;
        checkOpened();
        if (msgs.length == 0) {
            result = null;
        } else {
            if (folder.getStore() != this.store) {
                if (move) {
                    str = "can't moveUIDMessages to a different store";
                } else {
                    str = "can't copyUIDMessages to a different store";
                }
                throw new MessagingException(str);
            }
            FetchProfile fp = new FetchProfile();
            fp.add((FetchProfile.Item) UIDFolder.FetchProfileItem.UID);
            fetch(msgs, fp);
            synchronized (this.messageCacheLock) {
                try {
                    IMAPProtocol p = getProtocol();
                    MessageSet[] ms = Utility.toMessageSet(msgs, (Utility.Condition) null);
                    if (ms == null) {
                        throw new MessageRemovedException("Messages have been removed");
                    }
                    if (move) {
                        cuid = p.moveuid(ms, folder.getFullName());
                    } else {
                        cuid = p.copyuid(ms, folder.getFullName());
                    }
                    long[] srcuids = UIDSet.toArray(cuid.src);
                    long[] dstuids = UIDSet.toArray(cuid.dst);
                    Message[] srcmsgs = getMessagesByUID(srcuids);
                    result = new AppendUID[msgs.length];
                    for (int i = 0; i < msgs.length; i++) {
                        int j = i;
                        while (true) {
                            if (msgs[i] != srcmsgs[j]) {
                                j++;
                                if (j >= srcmsgs.length) {
                                    j = 0;
                                    continue;
                                }
                                if (j == i) {
                                    break;
                                }
                            } else {
                                result[i] = new AppendUID(cuid.uidvalidity, dstuids[j]);
                                break;
                            }
                        }
                    }
                } catch (CommandFailedException cfx) {
                    if (cfx.getMessage().indexOf("TRYCREATE") != -1) {
                        throw new FolderNotFoundException(folder, folder.getFullName() + " does not exist");
                    }
                    throw new MessagingException(cfx.getMessage(), cfx);
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
        return result;
    }

    public synchronized Message[] expunge() throws MessagingException {
        return expunge((Message[]) null);
    }

    public synchronized Message[] expunge(Message[] msgs) throws MessagingException {
        IMAPMessage[] rmsgs;
        checkOpened();
        if (msgs != null) {
            FetchProfile fp = new FetchProfile();
            fp.add((FetchProfile.Item) UIDFolder.FetchProfileItem.UID);
            fetch(msgs, fp);
        }
        synchronized (this.messageCacheLock) {
            this.doExpungeNotification = $assertionsDisabled;
            try {
                IMAPProtocol p = getProtocol();
                if (msgs != null) {
                    p.uidexpunge(Utility.toUIDSet(msgs));
                } else {
                    p.expunge();
                }
                this.doExpungeNotification = true;
                if (msgs != null) {
                    rmsgs = this.messageCache.removeExpungedMessages(msgs);
                } else {
                    rmsgs = this.messageCache.removeExpungedMessages();
                }
                if (this.uidTable != null) {
                    for (IMAPMessage m : rmsgs) {
                        long uid = m.getUID();
                        if (uid != -1) {
                            this.uidTable.remove(Long.valueOf(uid));
                        }
                    }
                }
                this.total = this.messageCache.size();
            } catch (CommandFailedException cfx) {
                if (this.mode != 2) {
                    throw new IllegalStateException("Cannot expunge READ_ONLY folder: " + this.fullName);
                }
                throw new MessagingException(cfx.getMessage(), cfx);
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            } catch (Throwable th) {
                this.doExpungeNotification = true;
                throw th;
            }
        }
        if (rmsgs.length > 0) {
            notifyMessageRemovedListeners(true, rmsgs);
        }
        return rmsgs;
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public synchronized javax.mail.Message[] search(javax.mail.search.SearchTerm r9) throws javax.mail.MessagingException {
        /*
            r8 = this;
            monitor-enter(r8)
            r8.checkOpened()     // Catch:{ all -> 0x002e }
            r2 = 0
            java.lang.Object r7 = r8.messageCacheLock     // Catch:{ CommandFailedException -> 0x001c, SearchException -> 0x0022, ConnectionException -> 0x0036, ProtocolException -> 0x0041 }
            monitor-enter(r7)     // Catch:{ CommandFailedException -> 0x001c, SearchException -> 0x0022, ConnectionException -> 0x0036, ProtocolException -> 0x0041 }
            com.sun.mail.imap.protocol.IMAPProtocol r6 = r8.getProtocol()     // Catch:{ all -> 0x0019 }
            int[] r3 = r6.search(r9)     // Catch:{ all -> 0x0019 }
            if (r3 == 0) goto L_0x0016
            com.sun.mail.imap.IMAPMessage[] r2 = r8.getMessagesBySeqNumbers(r3)     // Catch:{ all -> 0x0019 }
        L_0x0016:
            monitor-exit(r7)     // Catch:{ all -> 0x0019 }
        L_0x0017:
            monitor-exit(r8)
            return r2
        L_0x0019:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x0019 }
            throw r6     // Catch:{ CommandFailedException -> 0x001c, SearchException -> 0x0022, ConnectionException -> 0x0036, ProtocolException -> 0x0041 }
        L_0x001c:
            r1 = move-exception
            javax.mail.Message[] r2 = super.search(r9)     // Catch:{ all -> 0x002e }
            goto L_0x0017
        L_0x0022:
            r5 = move-exception
            javax.mail.Store r6 = r8.store     // Catch:{ all -> 0x002e }
            com.sun.mail.imap.IMAPStore r6 = (com.sun.mail.imap.IMAPStore) r6     // Catch:{ all -> 0x002e }
            boolean r6 = r6.throwSearchException()     // Catch:{ all -> 0x002e }
            if (r6 == 0) goto L_0x0031
            throw r5     // Catch:{ all -> 0x002e }
        L_0x002e:
            r6 = move-exception
            monitor-exit(r8)
            throw r6
        L_0x0031:
            javax.mail.Message[] r2 = super.search(r9)     // Catch:{ all -> 0x002e }
            goto L_0x0017
        L_0x0036:
            r0 = move-exception
            javax.mail.FolderClosedException r6 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x002e }
            java.lang.String r7 = r0.getMessage()     // Catch:{ all -> 0x002e }
            r6.<init>(r8, r7)     // Catch:{ all -> 0x002e }
            throw r6     // Catch:{ all -> 0x002e }
        L_0x0041:
            r4 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x002e }
            java.lang.String r7 = r4.getMessage()     // Catch:{ all -> 0x002e }
            r6.<init>(r7, r4)     // Catch:{ all -> 0x002e }
            throw r6     // Catch:{ all -> 0x002e }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.search(javax.mail.search.SearchTerm):javax.mail.Message[]");
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public synchronized javax.mail.Message[] search(javax.mail.search.SearchTerm r12, javax.mail.Message[] r13) throws javax.mail.MessagingException {
        /*
            r11 = this;
            monitor-enter(r11)
            r11.checkOpened()     // Catch:{ all -> 0x0047 }
            int r8 = r13.length     // Catch:{ all -> 0x0047 }
            if (r8 != 0) goto L_0x0009
        L_0x0007:
            monitor-exit(r11)
            return r13
        L_0x0009:
            r2 = 0
            java.lang.Object r9 = r11.messageCacheLock     // Catch:{ CommandFailedException -> 0x0023, SearchException -> 0x0036, ConnectionException -> 0x003c, ProtocolException -> 0x004a }
            monitor-enter(r9)     // Catch:{ CommandFailedException -> 0x0023, SearchException -> 0x0036, ConnectionException -> 0x003c, ProtocolException -> 0x004a }
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r11.getProtocol()     // Catch:{ all -> 0x0020 }
            r8 = 0
            com.sun.mail.imap.protocol.MessageSet[] r4 = com.sun.mail.imap.Utility.toMessageSetSorted(r13, r8)     // Catch:{ all -> 0x0020 }
            if (r4 != 0) goto L_0x0029
            javax.mail.MessageRemovedException r8 = new javax.mail.MessageRemovedException     // Catch:{ all -> 0x0020 }
            java.lang.String r10 = "Messages have been removed"
            r8.<init>(r10)     // Catch:{ all -> 0x0020 }
            throw r8     // Catch:{ all -> 0x0020 }
        L_0x0020:
            r8 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0020 }
            throw r8     // Catch:{ CommandFailedException -> 0x0023, SearchException -> 0x0036, ConnectionException -> 0x003c, ProtocolException -> 0x004a }
        L_0x0023:
            r1 = move-exception
            javax.mail.Message[] r13 = super.search(r12, r13)     // Catch:{ all -> 0x0047 }
            goto L_0x0007
        L_0x0029:
            int[] r3 = r5.search((com.sun.mail.imap.protocol.MessageSet[]) r4, (javax.mail.search.SearchTerm) r12)     // Catch:{ all -> 0x0020 }
            if (r3 == 0) goto L_0x0033
            com.sun.mail.imap.IMAPMessage[] r2 = r11.getMessagesBySeqNumbers(r3)     // Catch:{ all -> 0x0020 }
        L_0x0033:
            monitor-exit(r9)     // Catch:{ all -> 0x0020 }
            r13 = r2
            goto L_0x0007
        L_0x0036:
            r7 = move-exception
            javax.mail.Message[] r13 = super.search(r12, r13)     // Catch:{ all -> 0x0047 }
            goto L_0x0007
        L_0x003c:
            r0 = move-exception
            javax.mail.FolderClosedException r8 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x0047 }
            java.lang.String r9 = r0.getMessage()     // Catch:{ all -> 0x0047 }
            r8.<init>(r11, r9)     // Catch:{ all -> 0x0047 }
            throw r8     // Catch:{ all -> 0x0047 }
        L_0x0047:
            r8 = move-exception
            monitor-exit(r11)
            throw r8
        L_0x004a:
            r6 = move-exception
            javax.mail.MessagingException r8 = new javax.mail.MessagingException     // Catch:{ all -> 0x0047 }
            java.lang.String r9 = r6.getMessage()     // Catch:{ all -> 0x0047 }
            r8.<init>(r9, r6)     // Catch:{ all -> 0x0047 }
            throw r8     // Catch:{ all -> 0x0047 }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.search(javax.mail.search.SearchTerm, javax.mail.Message[]):javax.mail.Message[]");
    }

    public synchronized Message[] getSortedMessages(SortTerm[] term) throws MessagingException {
        return getSortedMessages(term, (SearchTerm) null);
    }

    public synchronized Message[] getSortedMessages(SortTerm[] term, SearchTerm sterm) throws MessagingException {
        Message[] matchMsgs;
        checkOpened();
        matchMsgs = null;
        try {
            synchronized (this.messageCacheLock) {
                int[] matches = getProtocol().sort(term, sterm);
                if (matches != null) {
                    matchMsgs = getMessagesBySeqNumbers(matches);
                }
            }
        } catch (CommandFailedException cfx) {
            throw new MessagingException(cfx.getMessage(), cfx);
        } catch (SearchException sex) {
            throw new MessagingException(sex.getMessage(), sex);
        } catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return matchMsgs;
    }

    public synchronized void addMessageCountListener(MessageCountListener l) {
        super.addMessageCountListener(l);
        this.hasMessageCountListener = true;
    }

    public synchronized long getUIDValidity() throws MessagingException {
        long j;
        if (this.opened) {
            j = this.uidvalidity;
        } else {
            IMAPProtocol p = null;
            Status status = null;
            try {
                p = getStoreProtocol();
                status = p.status(this.fullName, new String[]{"UIDVALIDITY"});
                releaseStoreProtocol(p);
            } catch (BadCommandException bex) {
                throw new MessagingException("Cannot obtain UIDValidity", bex);
            } catch (ConnectionException cex) {
                throwClosedException(cex);
                releaseStoreProtocol(p);
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            } catch (Throwable th) {
                releaseStoreProtocol(p);
                throw th;
            }
            if (status == null) {
                throw new MessagingException("Cannot obtain UIDValidity");
            }
            j = status.uidvalidity;
        }
        return j;
    }

    public synchronized long getUIDNext() throws MessagingException {
        long j;
        if (this.opened) {
            j = this.uidnext;
        } else {
            IMAPProtocol p = null;
            Status status = null;
            try {
                p = getStoreProtocol();
                status = p.status(this.fullName, new String[]{"UIDNEXT"});
                releaseStoreProtocol(p);
            } catch (BadCommandException bex) {
                throw new MessagingException("Cannot obtain UIDNext", bex);
            } catch (ConnectionException cex) {
                throwClosedException(cex);
                releaseStoreProtocol(p);
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            } catch (Throwable th) {
                releaseStoreProtocol(p);
                throw th;
            }
            if (status == null) {
                throw new MessagingException("Cannot obtain UIDNext");
            }
            j = status.uidnext;
        }
        return j;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x003e A[Catch:{ ConnectionException -> 0x0047, ProtocolException -> 0x0055 }, DONT_GENERATE] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0041 A[Catch:{ ConnectionException -> 0x0047, ProtocolException -> 0x0055 }, DONT_GENERATE, PHI: r3 
      PHI: (r3v2 'm' com.sun.mail.imap.IMAPMessage) = (r3v1 'm' com.sun.mail.imap.IMAPMessage), (r3v3 'm' com.sun.mail.imap.IMAPMessage) binds: [B:19:0x0030, B:21:0x003c] A[DONT_GENERATE, DONT_INLINE]] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized javax.mail.Message getMessageByUID(long r10) throws javax.mail.MessagingException {
        /*
            r9 = this;
            monitor-enter(r9)
            r9.checkOpened()     // Catch:{ all -> 0x0052 }
            r3 = 0
            java.lang.Object r7 = r9.messageCacheLock     // Catch:{ ConnectionException -> 0x0047, ProtocolException -> 0x0055 }
            monitor-enter(r7)     // Catch:{ ConnectionException -> 0x0047, ProtocolException -> 0x0055 }
            java.lang.Long r2 = java.lang.Long.valueOf(r10)     // Catch:{ all -> 0x0044 }
            java.util.Hashtable<java.lang.Long, com.sun.mail.imap.IMAPMessage> r6 = r9.uidTable     // Catch:{ all -> 0x0044 }
            if (r6 == 0) goto L_0x0020
            java.util.Hashtable<java.lang.Long, com.sun.mail.imap.IMAPMessage> r6 = r9.uidTable     // Catch:{ all -> 0x0044 }
            java.lang.Object r6 = r6.get(r2)     // Catch:{ all -> 0x0044 }
            r0 = r6
            com.sun.mail.imap.IMAPMessage r0 = (com.sun.mail.imap.IMAPMessage) r0     // Catch:{ all -> 0x0044 }
            r3 = r0
            if (r3 == 0) goto L_0x0027
            monitor-exit(r7)     // Catch:{ all -> 0x0044 }
            r4 = r3
        L_0x001e:
            monitor-exit(r9)
            return r4
        L_0x0020:
            java.util.Hashtable r6 = new java.util.Hashtable     // Catch:{ all -> 0x0044 }
            r6.<init>()     // Catch:{ all -> 0x0044 }
            r9.uidTable = r6     // Catch:{ all -> 0x0044 }
        L_0x0027:
            com.sun.mail.imap.protocol.IMAPProtocol r6 = r9.getProtocol()     // Catch:{ all -> 0x0044 }
            r6.fetchSequenceNumber(r10)     // Catch:{ all -> 0x0044 }
            java.util.Hashtable<java.lang.Long, com.sun.mail.imap.IMAPMessage> r6 = r9.uidTable     // Catch:{ all -> 0x0044 }
            if (r6 == 0) goto L_0x0041
            java.util.Hashtable<java.lang.Long, com.sun.mail.imap.IMAPMessage> r6 = r9.uidTable     // Catch:{ all -> 0x0044 }
            java.lang.Object r6 = r6.get(r2)     // Catch:{ all -> 0x0044 }
            r0 = r6
            com.sun.mail.imap.IMAPMessage r0 = (com.sun.mail.imap.IMAPMessage) r0     // Catch:{ all -> 0x0044 }
            r3 = r0
            if (r3 == 0) goto L_0x0041
            monitor-exit(r7)     // Catch:{ all -> 0x0044 }
            r4 = r3
            goto L_0x001e
        L_0x0041:
            monitor-exit(r7)     // Catch:{ all -> 0x0044 }
            r4 = r3
            goto L_0x001e
        L_0x0044:
            r6 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x0044 }
            throw r6     // Catch:{ ConnectionException -> 0x0047, ProtocolException -> 0x0055 }
        L_0x0047:
            r1 = move-exception
            javax.mail.FolderClosedException r6 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x0052 }
            java.lang.String r7 = r1.getMessage()     // Catch:{ all -> 0x0052 }
            r6.<init>(r9, r7)     // Catch:{ all -> 0x0052 }
            throw r6     // Catch:{ all -> 0x0052 }
        L_0x0052:
            r6 = move-exception
            monitor-exit(r9)
            throw r6
        L_0x0055:
            r5 = move-exception
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ all -> 0x0052 }
            java.lang.String r7 = r5.getMessage()     // Catch:{ all -> 0x0052 }
            r6.<init>(r7, r5)     // Catch:{ all -> 0x0052 }
            throw r6     // Catch:{ all -> 0x0052 }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.getMessageByUID(long):javax.mail.Message");
    }

    public synchronized Message[] getMessagesByUID(long start, long end) throws MessagingException {
        Message[] msgs;
        checkOpened();
        try {
            synchronized (this.messageCacheLock) {
                if (this.uidTable == null) {
                    this.uidTable = new Hashtable<>();
                }
                long[] ua = getProtocol().fetchSequenceNumbers(start, end);
                List<Message> ma = new ArrayList<>();
                for (long valueOf : ua) {
                    Message m = this.uidTable.get(Long.valueOf(valueOf));
                    if (m != null) {
                        ma.add(m);
                    }
                }
                msgs = (Message[]) ma.toArray(new Message[ma.size()]);
            }
        } catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return msgs;
    }

    public synchronized Message[] getMessagesByUID(long[] uids) throws MessagingException {
        Message[] msgs;
        checkOpened();
        try {
            synchronized (this.messageCacheLock) {
                long[] unavailUids = uids;
                if (this.uidTable != null) {
                    List<Long> v = new ArrayList<>();
                    for (long uid : uids) {
                        if (!this.uidTable.containsKey(Long.valueOf(uid))) {
                            v.add(Long.valueOf(uid));
                        }
                    }
                    int vsize = v.size();
                    unavailUids = new long[vsize];
                    for (int i = 0; i < vsize; i++) {
                        unavailUids[i] = v.get(i).longValue();
                    }
                } else {
                    this.uidTable = new Hashtable<>();
                }
                if (unavailUids.length > 0) {
                    getProtocol().fetchSequenceNumbers(unavailUids);
                }
                msgs = new Message[uids.length];
                for (int i2 = 0; i2 < uids.length; i2++) {
                    msgs[i2] = this.uidTable.get(Long.valueOf(uids[i2]));
                }
            }
        } catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return msgs;
    }

    public synchronized long getUID(Message message) throws MessagingException {
        long uid;
        if (message.getFolder() != this) {
            throw new NoSuchElementException("Message does not belong to this folder");
        }
        checkOpened();
        if (!(message instanceof IMAPMessage)) {
            throw new MessagingException("message is not an IMAPMessage");
        }
        IMAPMessage m = (IMAPMessage) message;
        long uid2 = m.getUID();
        if (uid2 != -1) {
            uid = uid2;
        } else {
            synchronized (this.messageCacheLock) {
                try {
                    IMAPProtocol p = getProtocol();
                    m.checkExpunged();
                    UID u = p.fetchUID(m.getSequenceNumber());
                    if (u != null) {
                        uid2 = u.uid;
                        m.setUID(uid2);
                        if (this.uidTable == null) {
                            this.uidTable = new Hashtable<>();
                        }
                        this.uidTable.put(Long.valueOf(uid2), m);
                    }
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            uid = uid2;
        }
        return uid;
    }

    public synchronized boolean getUIDNotSticky() throws MessagingException {
        checkOpened();
        return this.uidNotSticky;
    }

    private Message[] createMessagesForUIDs(long[] uids) {
        IMAPMessage[] msgs = new IMAPMessage[uids.length];
        for (int i = 0; i < uids.length; i = i + 1 + 1) {
            IMAPMessage m = null;
            if (this.uidTable != null) {
                m = this.uidTable.get(Long.valueOf(uids[i]));
            }
            if (m == null) {
                m = newIMAPMessage(-1);
                m.setUID(uids[i]);
                m.setExpunged(true);
            }
            msgs[i] = m;
        }
        return msgs;
    }

    public synchronized long getHighestModSeq() throws MessagingException {
        long j;
        if (this.opened) {
            j = this.highestmodseq;
        } else {
            IMAPProtocol p = null;
            Status status = null;
            try {
                p = getStoreProtocol();
                if (!p.hasCapability("CONDSTORE")) {
                    throw new BadCommandException("CONDSTORE not supported");
                }
                status = p.status(this.fullName, new String[]{"HIGHESTMODSEQ"});
                releaseStoreProtocol(p);
                if (status == null) {
                    throw new MessagingException("Cannot obtain HIGHESTMODSEQ");
                }
                j = status.highestmodseq;
            } catch (BadCommandException bex) {
                throw new MessagingException("Cannot obtain HIGHESTMODSEQ", bex);
            } catch (ConnectionException cex) {
                throwClosedException(cex);
                releaseStoreProtocol(p);
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            } catch (Throwable th) {
                releaseStoreProtocol(p);
                throw th;
            }
        }
        return j;
    }

    public synchronized Message[] getMessagesByUIDChangedSince(long start, long end, long modseq) throws MessagingException {
        IMAPMessage[] messagesBySeqNumbers;
        checkOpened();
        try {
            synchronized (this.messageCacheLock) {
                IMAPProtocol p = getProtocol();
                if (!p.hasCapability("CONDSTORE")) {
                    throw new BadCommandException("CONDSTORE not supported");
                }
                messagesBySeqNumbers = getMessagesBySeqNumbers(p.uidfetchChangedSince(start, end, modseq));
            }
        } catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return messagesBySeqNumbers;
    }

    public Quota[] getQuota() throws MessagingException {
        return (Quota[]) doOptionalCommand("QUOTA not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.getQuotaRoot(IMAPFolder.this.fullName);
            }
        });
    }

    public void setQuota(final Quota quota) throws MessagingException {
        doOptionalCommand("QUOTA not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.setQuota(quota);
                return null;
            }
        });
    }

    public ACL[] getACL() throws MessagingException {
        return (ACL[]) doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.getACL(IMAPFolder.this.fullName);
            }
        });
    }

    public void addACL(ACL acl) throws MessagingException {
        setACL(acl, 0);
    }

    public void removeACL(final String name2) throws MessagingException {
        doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.deleteACL(IMAPFolder.this.fullName, name2);
                return null;
            }
        });
    }

    public void addRights(ACL acl) throws MessagingException {
        setACL(acl, '+');
    }

    public void removeRights(ACL acl) throws MessagingException {
        setACL(acl, '-');
    }

    public Rights[] listRights(final String name2) throws MessagingException {
        return (Rights[]) doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.listRights(IMAPFolder.this.fullName, name2);
            }
        });
    }

    public Rights myRights() throws MessagingException {
        return (Rights) doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.myRights(IMAPFolder.this.fullName);
            }
        });
    }

    private void setACL(final ACL acl, final char mod) throws MessagingException {
        doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                p.setACL(IMAPFolder.this.fullName, mod, acl);
                return null;
            }
        });
    }

    public synchronized String[] getAttributes() throws MessagingException {
        checkExists();
        if (this.attributes == null) {
            exists();
        }
        return this.attributes == null ? new String[0] : (String[]) this.attributes.clone();
    }

    public void idle() throws MessagingException {
        idle($assertionsDisabled);
    }

    public void idle(boolean once) throws MessagingException {
        synchronized (this) {
            if (this.protocol != null && this.protocol.getChannel() != null) {
                throw new MessagingException("idle method not supported with SocketChannels");
            }
        }
        if (startIdle((IdleManager) null)) {
            do {
            } while (handleIdle(once));
            int minidle = ((IMAPStore) this.store).getMinIdleTime();
            if (minidle > 0) {
                try {
                    Thread.sleep((long) minidle);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean startIdle(final IdleManager im) throws MessagingException {
        boolean booleanValue;
        if ($assertionsDisabled || !Thread.holdsLock(this)) {
            synchronized (this) {
                checkOpened();
                if (im == null || this.idleManager == null || im == this.idleManager) {
                    Boolean started = (Boolean) doOptionalCommand("IDLE not supported", new ProtocolCommand() {
                        public Object doCommand(IMAPProtocol p) throws ProtocolException {
                            if (IMAPFolder.this.idleState == 1 && im != null && im == IMAPFolder.this.idleManager) {
                                return Boolean.TRUE;
                            }
                            if (IMAPFolder.this.idleState == 0) {
                                p.idleStart();
                                IMAPFolder.this.logger.finest("startIdle: set to IDLE");
                                int unused = IMAPFolder.this.idleState = 1;
                                IdleManager unused2 = IMAPFolder.this.idleManager = im;
                                return Boolean.TRUE;
                            }
                            try {
                                IMAPFolder.this.messageCacheLock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return Boolean.FALSE;
                        }
                    });
                    this.logger.log(Level.FINEST, "startIdle: return {0}", (Object) started);
                    booleanValue = started.booleanValue();
                } else {
                    throw new MessagingException("Folder already being watched by another IdleManager");
                }
            }
            return booleanValue;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0039, code lost:
        if (r6 == null) goto L_0x0003;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        if (r12.protocol.hasResponse() != false) goto L_0x0003;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:?, code lost:
        return true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0088 A[SYNTHETIC, Splitter:B:44:0x0088] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean handleIdle(boolean r13) throws javax.mail.MessagingException {
        /*
            r12 = this;
            r9 = 1
            r8 = 0
            r6 = 0
        L_0x0003:
            com.sun.mail.imap.protocol.IMAPProtocol r7 = r12.protocol
            com.sun.mail.iap.Response r6 = r7.readIdleResponse()
            java.lang.Object r10 = r12.messageCacheLock     // Catch:{ ConnectionException -> 0x005e, ProtocolException -> 0x00d9 }
            monitor-enter(r10)     // Catch:{ ConnectionException -> 0x005e, ProtocolException -> 0x00d9 }
            boolean r7 = r6.isBYE()     // Catch:{ all -> 0x005b }
            if (r7 == 0) goto L_0x0079
            boolean r7 = r6.isSynthetic()     // Catch:{ all -> 0x005b }
            if (r7 == 0) goto L_0x0079
            int r7 = r12.idleState     // Catch:{ all -> 0x005b }
            if (r7 != r9) goto L_0x0079
            java.lang.Exception r3 = r6.getException()     // Catch:{ all -> 0x005b }
            boolean r7 = r3 instanceof java.io.InterruptedIOException     // Catch:{ all -> 0x005b }
            if (r7 == 0) goto L_0x0079
            r0 = r3
            java.io.InterruptedIOException r0 = (java.io.InterruptedIOException) r0     // Catch:{ all -> 0x005b }
            r7 = r0
            int r7 = r7.bytesTransferred     // Catch:{ all -> 0x005b }
            if (r7 != 0) goto L_0x0079
            boolean r7 = r3 instanceof java.net.SocketTimeoutException     // Catch:{ all -> 0x005b }
            if (r7 == 0) goto L_0x0045
            com.sun.mail.util.MailLogger r7 = r12.logger     // Catch:{ all -> 0x005b }
            java.lang.String r11 = "handleIdle: ignoring socket timeout"
            r7.finest(r11)     // Catch:{ all -> 0x005b }
            r6 = 0
        L_0x0038:
            monitor-exit(r10)     // Catch:{ all -> 0x005b }
        L_0x0039:
            if (r6 == 0) goto L_0x0003
            com.sun.mail.imap.protocol.IMAPProtocol r7 = r12.protocol
            boolean r7 = r7.hasResponse()
            if (r7 != 0) goto L_0x0003
            r7 = r9
        L_0x0044:
            return r7
        L_0x0045:
            com.sun.mail.util.MailLogger r7 = r12.logger     // Catch:{ all -> 0x005b }
            java.lang.String r11 = "handleIdle: interrupting IDLE"
            r7.finest(r11)     // Catch:{ all -> 0x005b }
            com.sun.mail.imap.IdleManager r4 = r12.idleManager     // Catch:{ all -> 0x005b }
            if (r4 == 0) goto L_0x0069
            com.sun.mail.util.MailLogger r7 = r12.logger     // Catch:{ all -> 0x005b }
            java.lang.String r11 = "handleIdle: request IdleManager to abort"
            r7.finest(r11)     // Catch:{ all -> 0x005b }
            r4.requestAbort(r12)     // Catch:{ all -> 0x005b }
            goto L_0x0038
        L_0x005b:
            r7 = move-exception
            monitor-exit(r10)     // Catch:{ all -> 0x005b }
            throw r7     // Catch:{ ConnectionException -> 0x005e, ProtocolException -> 0x00d9 }
        L_0x005e:
            r1 = move-exception
            javax.mail.FolderClosedException r7 = new javax.mail.FolderClosedException
            java.lang.String r8 = r1.getMessage()
            r7.<init>(r12, r8)
            throw r7
        L_0x0069:
            com.sun.mail.util.MailLogger r7 = r12.logger     // Catch:{ all -> 0x005b }
            java.lang.String r11 = "handleIdle: abort IDLE"
            r7.finest(r11)     // Catch:{ all -> 0x005b }
            com.sun.mail.imap.protocol.IMAPProtocol r7 = r12.protocol     // Catch:{ all -> 0x005b }
            r7.idleAbort()     // Catch:{ all -> 0x005b }
            r7 = 2
            r12.idleState = r7     // Catch:{ all -> 0x005b }
            goto L_0x0038
        L_0x0079:
            r2 = 1
            com.sun.mail.imap.protocol.IMAPProtocol r7 = r12.protocol     // Catch:{ all -> 0x00c3 }
            if (r7 == 0) goto L_0x0086
            com.sun.mail.imap.protocol.IMAPProtocol r7 = r12.protocol     // Catch:{ all -> 0x00c3 }
            boolean r7 = r7.processIdleResponse(r6)     // Catch:{ all -> 0x00c3 }
            if (r7 != 0) goto L_0x009d
        L_0x0086:
            if (r2 == 0) goto L_0x009a
            com.sun.mail.util.MailLogger r7 = r12.logger     // Catch:{ all -> 0x005b }
            java.lang.String r9 = "handleIdle: set to RUNNING"
            r7.finest(r9)     // Catch:{ all -> 0x005b }
            r7 = 0
            r12.idleState = r7     // Catch:{ all -> 0x005b }
            r7 = 0
            r12.idleManager = r7     // Catch:{ all -> 0x005b }
            java.lang.Object r7 = r12.messageCacheLock     // Catch:{ all -> 0x005b }
            r7.notifyAll()     // Catch:{ all -> 0x005b }
        L_0x009a:
            monitor-exit(r10)     // Catch:{ all -> 0x005b }
            r7 = r8
            goto L_0x0044
        L_0x009d:
            r2 = 0
            if (r2 == 0) goto L_0x00b2
            com.sun.mail.util.MailLogger r7 = r12.logger     // Catch:{ all -> 0x005b }
            java.lang.String r11 = "handleIdle: set to RUNNING"
            r7.finest(r11)     // Catch:{ all -> 0x005b }
            r7 = 0
            r12.idleState = r7     // Catch:{ all -> 0x005b }
            r7 = 0
            r12.idleManager = r7     // Catch:{ all -> 0x005b }
            java.lang.Object r7 = r12.messageCacheLock     // Catch:{ all -> 0x005b }
            r7.notifyAll()     // Catch:{ all -> 0x005b }
        L_0x00b2:
            if (r13 == 0) goto L_0x00c0
            int r7 = r12.idleState     // Catch:{ all -> 0x005b }
            if (r7 != r9) goto L_0x00c0
            com.sun.mail.imap.protocol.IMAPProtocol r7 = r12.protocol     // Catch:{ Exception -> 0x00e4 }
            r7.idleAbort()     // Catch:{ Exception -> 0x00e4 }
        L_0x00bd:
            r7 = 2
            r12.idleState = r7     // Catch:{ all -> 0x005b }
        L_0x00c0:
            monitor-exit(r10)     // Catch:{ all -> 0x005b }
            goto L_0x0039
        L_0x00c3:
            r7 = move-exception
            if (r2 == 0) goto L_0x00d8
            com.sun.mail.util.MailLogger r8 = r12.logger     // Catch:{ all -> 0x005b }
            java.lang.String r9 = "handleIdle: set to RUNNING"
            r8.finest(r9)     // Catch:{ all -> 0x005b }
            r8 = 0
            r12.idleState = r8     // Catch:{ all -> 0x005b }
            r8 = 0
            r12.idleManager = r8     // Catch:{ all -> 0x005b }
            java.lang.Object r8 = r12.messageCacheLock     // Catch:{ all -> 0x005b }
            r8.notifyAll()     // Catch:{ all -> 0x005b }
        L_0x00d8:
            throw r7     // Catch:{ all -> 0x005b }
        L_0x00d9:
            r5 = move-exception
            javax.mail.MessagingException r7 = new javax.mail.MessagingException
            java.lang.String r8 = r5.getMessage()
            r7.<init>(r8, r5)
            throw r7
        L_0x00e4:
            r7 = move-exception
            goto L_0x00bd
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPFolder.handleIdle(boolean):boolean");
    }

    /* access modifiers changed from: package-private */
    public void waitIfIdle() throws ProtocolException {
        if ($assertionsDisabled || Thread.holdsLock(this.messageCacheLock)) {
            while (this.idleState != 0) {
                if (this.idleState == 1) {
                    IdleManager im = this.idleManager;
                    if (im != null) {
                        this.logger.finest("waitIfIdle: request IdleManager to abort");
                        im.requestAbort(this);
                    } else {
                        this.logger.finest("waitIfIdle: abort IDLE");
                        this.protocol.idleAbort();
                        this.idleState = 2;
                    }
                } else {
                    this.logger.log(Level.FINEST, "waitIfIdle: idleState {0}", (Object) Integer.valueOf(this.idleState));
                }
                try {
                    if (this.logger.isLoggable(Level.FINEST)) {
                        this.logger.finest("waitIfIdle: wait to be not idle: " + Thread.currentThread());
                    }
                    this.messageCacheLock.wait();
                    if (this.logger.isLoggable(Level.FINEST)) {
                        this.logger.finest("waitIfIdle: wait done, idleState " + this.idleState + ": " + Thread.currentThread());
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new ProtocolException("Interrupted waitIfIdle", ex);
                }
            }
            return;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: package-private */
    public void idleAbort() {
        synchronized (this.messageCacheLock) {
            if (this.idleState == 1 && this.protocol != null) {
                this.protocol.idleAbort();
                this.idleState = 2;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void idleAbortWait() {
        synchronized (this.messageCacheLock) {
            if (this.idleState == 1 && this.protocol != null) {
                this.protocol.idleAbort();
                this.idleState = 2;
                do {
                    try {
                    } catch (Exception ex) {
                        this.logger.log(Level.FINEST, "Exception in idleAbortWait", (Throwable) ex);
                    }
                } while (handleIdle($assertionsDisabled));
                this.logger.finest("IDLE aborted");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public SocketChannel getChannel() {
        if (this.protocol != null) {
            return this.protocol.getChannel();
        }
        return null;
    }

    /* renamed from: id */
    public Map<String, String> mo12783id(final Map<String, String> clientParams) throws MessagingException {
        checkOpened();
        return (Map) doOptionalCommand("ID not supported", new ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.mo13040id((Map<String, String>) clientParams);
            }
        });
    }

    public synchronized long getStatusItem(String item) throws MessagingException {
        long j = -1;
        synchronized (this) {
            if (!this.opened) {
                checkExists();
                try {
                    IMAPProtocol p = getStoreProtocol();
                    Status status = p.status(this.fullName, new String[]{item});
                    if (status != null) {
                        j = status.getItem(item);
                    }
                    releaseStoreProtocol(p);
                } catch (BadCommandException e) {
                    releaseStoreProtocol((IMAPProtocol) null);
                } catch (ConnectionException cex) {
                    throw new StoreClosedException(this.store, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                } catch (Throwable th) {
                    releaseStoreProtocol((IMAPProtocol) null);
                    throw th;
                }
            }
        }
        return j;
    }

    public void handleResponse(Response r) {
        if ($assertionsDisabled || Thread.holdsLock(this.messageCacheLock)) {
            if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
                ((IMAPStore) this.store).handleResponseCode(r);
            }
            if (r.isBYE()) {
                if (this.opened) {
                    cleanup($assertionsDisabled);
                }
            } else if (r.isOK()) {
                r.skipSpaces();
                if (r.readByte() == 91 && r.readAtom().equalsIgnoreCase("HIGHESTMODSEQ")) {
                    this.highestmodseq = r.readLong();
                }
                r.reset();
            } else if (!r.isUnTagged()) {
            } else {
                if (!(r instanceof IMAPResponse)) {
                    this.logger.fine("UNEXPECTED RESPONSE : " + r.toString());
                    return;
                }
                IMAPResponse ir = (IMAPResponse) r;
                if (ir.keyEquals("EXISTS")) {
                    int exists2 = ir.getNumber();
                    if (exists2 > this.realTotal) {
                        int count = exists2 - this.realTotal;
                        Message[] msgs = new Message[count];
                        this.messageCache.addMessages(count, this.realTotal + 1);
                        int oldtotal = this.total;
                        this.realTotal += count;
                        this.total += count;
                        if (this.hasMessageCountListener) {
                            for (int i = 0; i < count; i++) {
                                oldtotal++;
                                msgs[i] = this.messageCache.getMessage(oldtotal);
                            }
                            notifyMessageAddedListeners(msgs);
                        }
                    }
                } else if (ir.keyEquals("EXPUNGE")) {
                    int seqnum = ir.getNumber();
                    if (seqnum <= this.realTotal) {
                        Message[] msgs2 = null;
                        if (this.doExpungeNotification && this.hasMessageCountListener) {
                            msgs2 = new Message[]{getMessageBySeqNumber(seqnum)};
                            if (msgs2[0] == null) {
                                msgs2 = null;
                            }
                        }
                        this.messageCache.expungeMessage(seqnum);
                        this.realTotal--;
                        if (msgs2 != null) {
                            notifyMessageRemovedListeners($assertionsDisabled, msgs2);
                        }
                    }
                } else if (ir.keyEquals("VANISHED")) {
                    if (ir.readAtomStringList() == null) {
                        UIDSet[] uidset = UIDSet.parseUIDSets(ir.readAtom());
                        this.realTotal = (int) (((long) this.realTotal) - UIDSet.size(uidset));
                        Message[] msgs3 = createMessagesForUIDs(UIDSet.toArray(uidset));
                        int length = msgs3.length;
                        for (int i2 = 0; i2 < length; i2++) {
                            Message m = msgs3[i2];
                            if (m.getMessageNumber() > 0) {
                                this.messageCache.expungeMessage(m.getMessageNumber());
                            }
                        }
                        if (this.doExpungeNotification && this.hasMessageCountListener) {
                            notifyMessageRemovedListeners(true, msgs3);
                        }
                    }
                } else if (ir.keyEquals("FETCH")) {
                    if ($assertionsDisabled || (ir instanceof FetchResponse)) {
                        Message msg = processFetchResponse((FetchResponse) ir);
                        if (msg != null) {
                            notifyMessageChangedListeners(1, msg);
                            return;
                        }
                        return;
                    }
                    throw new AssertionError("!ir instanceof FetchResponse");
                } else if (ir.keyEquals("RECENT")) {
                    this.recent = ir.getNumber();
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    private Message processFetchResponse(FetchResponse fr) {
        IMAPMessage msg = getMessageBySeqNumber(fr.getNumber());
        if (msg == null) {
            return msg;
        }
        boolean notify = $assertionsDisabled;
        UID uid = (UID) fr.getItem(UID.class);
        if (!(uid == null || msg.getUID() == uid.uid)) {
            msg.setUID(uid.uid);
            if (this.uidTable == null) {
                this.uidTable = new Hashtable<>();
            }
            this.uidTable.put(Long.valueOf(uid.uid), msg);
            notify = true;
        }
        MODSEQ modseq = (MODSEQ) fr.getItem(MODSEQ.class);
        if (!(modseq == null || msg._getModSeq() == modseq.modseq)) {
            msg.setModSeq(modseq.modseq);
            notify = true;
        }
        FLAGS flags = (FLAGS) fr.getItem(FLAGS.class);
        if (flags != null) {
            msg._setFlags(flags);
            notify = true;
        }
        msg.handleExtensionFetchItems(fr.getExtensionItems());
        if (!notify) {
            return null;
        }
        return msg;
    }

    /* access modifiers changed from: package-private */
    public void handleResponses(Response[] r) {
        for (int i = 0; i < r.length; i++) {
            if (r[i] != null) {
                handleResponse(r[i]);
            }
        }
    }

    /* access modifiers changed from: protected */
    public synchronized IMAPProtocol getStoreProtocol() throws ProtocolException {
        this.connectionPoolLogger.fine("getStoreProtocol() borrowing a connection");
        return ((IMAPStore) this.store).getFolderStoreProtocol();
    }

    /* access modifiers changed from: protected */
    public synchronized void throwClosedException(ConnectionException cex) throws FolderClosedException, StoreClosedException {
        if ((this.protocol == null || cex.getProtocol() != this.protocol) && (this.protocol != null || this.reallyClosed)) {
            throw new StoreClosedException(this.store, cex.getMessage());
        }
        throw new FolderClosedException(this, cex.getMessage());
    }

    /* access modifiers changed from: protected */
    public IMAPProtocol getProtocol() throws ProtocolException {
        if ($assertionsDisabled || Thread.holdsLock(this.messageCacheLock)) {
            waitIfIdle();
            if (this.protocol != null) {
                return this.protocol;
            }
            throw new ConnectionException("Connection closed");
        }
        throw new AssertionError();
    }

    public Object doCommand(ProtocolCommand cmd) throws MessagingException {
        try {
            return doProtocolCommand(cmd);
        } catch (ConnectionException cex) {
            throwClosedException(cex);
            return null;
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    public Object doOptionalCommand(String err, ProtocolCommand cmd) throws MessagingException {
        try {
            return doProtocolCommand(cmd);
        } catch (BadCommandException bex) {
            throw new MessagingException(err, bex);
        } catch (ConnectionException cex) {
            throwClosedException(cex);
            return null;
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    public Object doCommandIgnoreFailure(ProtocolCommand cmd) throws MessagingException {
        try {
            return doProtocolCommand(cmd);
        } catch (CommandFailedException e) {
            return null;
        } catch (ConnectionException cex) {
            throwClosedException(cex);
            return null;
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public synchronized Object doProtocolCommand(ProtocolCommand cmd) throws ProtocolException {
        Object doCommand;
        if (this.protocol != null) {
            synchronized (this.messageCacheLock) {
                doCommand = cmd.doCommand(getProtocol());
            }
        } else {
            IMAPProtocol p = null;
            try {
                p = getStoreProtocol();
                doCommand = cmd.doCommand(p);
                releaseStoreProtocol(p);
            } catch (Throwable th) {
                releaseStoreProtocol(p);
                throw th;
            }
        }
        return doCommand;
    }

    /* access modifiers changed from: protected */
    public synchronized void releaseStoreProtocol(IMAPProtocol p) {
        if (p != this.protocol) {
            ((IMAPStore) this.store).releaseFolderStoreProtocol(p);
        } else {
            this.logger.fine("releasing our protocol as store protocol?");
        }
    }

    /* access modifiers changed from: protected */
    public void releaseProtocol(boolean returnToPool) {
        if (this.protocol != null) {
            this.protocol.removeResponseHandler(this);
            if (returnToPool) {
                ((IMAPStore) this.store).releaseProtocol(this, this.protocol);
            } else {
                this.protocol.disconnect();
                ((IMAPStore) this.store).releaseProtocol(this, (IMAPProtocol) null);
            }
            this.protocol = null;
        }
    }

    /* access modifiers changed from: protected */
    public void keepConnectionAlive(boolean keepStoreAlive) throws ProtocolException {
        if (!$assertionsDisabled && !Thread.holdsLock(this.messageCacheLock)) {
            throw new AssertionError();
        } else if (this.protocol != null) {
            if (System.currentTimeMillis() - this.protocol.getTimestamp() > 1000) {
                waitIfIdle();
                if (this.protocol != null) {
                    this.protocol.noop();
                }
            }
            if (keepStoreAlive && ((IMAPStore) this.store).hasSeparateStoreConnection()) {
                try {
                    IMAPProtocol p = ((IMAPStore) this.store).getFolderStoreProtocol();
                    if (System.currentTimeMillis() - p.getTimestamp() > 1000) {
                        p.noop();
                    }
                    ((IMAPStore) this.store).releaseFolderStoreProtocol(p);
                } catch (Throwable th) {
                    Throwable th2 = th;
                    ((IMAPStore) this.store).releaseFolderStoreProtocol((IMAPProtocol) null);
                    throw th2;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public IMAPMessage getMessageBySeqNumber(int seqnum) {
        if (seqnum <= this.messageCache.size()) {
            return this.messageCache.getMessageBySeqnum(seqnum);
        }
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("ignoring message number " + seqnum + " outside range " + this.messageCache.size());
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public IMAPMessage[] getMessagesBySeqNumbers(int[] seqnums) {
        IMAPMessage[] msgs = new IMAPMessage[seqnums.length];
        int nulls = 0;
        for (int i = 0; i < seqnums.length; i++) {
            msgs[i] = getMessageBySeqNumber(seqnums[i]);
            if (msgs[i] == null) {
                nulls++;
            }
        }
        if (nulls <= 0) {
            return msgs;
        }
        IMAPMessage[] nmsgs = new IMAPMessage[(seqnums.length - nulls)];
        int j = 0;
        for (int i2 = 0; i2 < msgs.length; i2++) {
            if (msgs[i2] != null) {
                nmsgs[j] = msgs[i2];
                j++;
            }
        }
        return nmsgs;
    }

    private boolean isDirectory() {
        if ((this.type & 2) != 0) {
            return true;
        }
        return $assertionsDisabled;
    }
}
