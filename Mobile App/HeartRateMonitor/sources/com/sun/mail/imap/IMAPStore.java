package com.sun.mail.imap;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.Namespaces;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import gnu.kawa.lispexpr.LispReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Quota;
import javax.mail.QuotaAwareStore;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.URLName;

public class IMAPStore extends Store implements QuotaAwareStore, ResponseHandler {
    static final /* synthetic */ boolean $assertionsDisabled = (!IMAPStore.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    public static final String ID_ADDRESS = "address";
    public static final String ID_ARGUMENTS = "arguments";
    public static final String ID_COMMAND = "command";
    public static final String ID_DATE = "date";
    public static final String ID_ENVIRONMENT = "environment";
    public static final String ID_NAME = "name";
    public static final String ID_OS = "os";
    public static final String ID_OS_VERSION = "os-version";
    public static final String ID_SUPPORT_URL = "support-url";
    public static final String ID_VENDOR = "vendor";
    public static final String ID_VERSION = "version";
    public static final int RESPONSE = 1000;
    private final int appendBufferSize;
    protected String authorizationID;
    private final int blksize;
    private boolean closeFoldersOnStoreFailure;
    private volatile boolean connectionFailed;
    private final Object connectionFailedLock;
    private boolean debugpassword;
    private boolean debugusername;
    protected final int defaultPort;
    private boolean enableCompress;
    private boolean enableImapEvents;
    private boolean enableResponseEvents;
    private boolean enableSASL;
    private boolean enableStartTLS;
    private boolean finalizeCleanClose;
    private volatile Constructor<?> folderConstructor;
    private volatile Constructor<?> folderConstructorLI;
    private volatile boolean forceClose;
    private boolean forcePasswordRefresh;
    private String guid;
    protected String host;
    private boolean ignoreSize;
    protected final boolean isSSL;
    protected MailLogger logger;
    private boolean messageCacheDebug;
    private final int minIdleTime;
    protected final String name;
    private Namespaces namespaces;
    private ResponseHandler nonStoreResponseHandler;
    protected String password;
    private boolean peek;
    private final ConnectionPool pool;
    private volatile int port;
    protected String proxyAuthUser;
    private boolean requireStartTLS;
    private String[] saslMechanisms;
    protected String saslRealm;
    private final int statusCacheTimeout;
    private boolean throwSearchException;
    protected String user;
    private boolean usingSSL;

    static class ConnectionPool {
        private static final int ABORTING = 2;
        private static final int IDLE = 1;
        private static final int RUNNING = 0;
        /* access modifiers changed from: private */
        public Vector<IMAPProtocol> authenticatedConnections = new Vector<>();
        /* access modifiers changed from: private */
        public final long clientTimeoutInterval;
        /* access modifiers changed from: private */
        public Vector<IMAPFolder> folders;
        /* access modifiers changed from: private */
        public IMAPProtocol idleProtocol;
        /* access modifiers changed from: private */
        public int idleState = 0;
        /* access modifiers changed from: private */
        public long lastTimePruned = System.currentTimeMillis();
        /* access modifiers changed from: private */
        public final MailLogger logger;
        /* access modifiers changed from: private */
        public final int poolSize;
        /* access modifiers changed from: private */
        public final long pruningInterval;
        /* access modifiers changed from: private */
        public final boolean separateStoreConnection;
        /* access modifiers changed from: private */
        public final long serverTimeoutInterval;
        /* access modifiers changed from: private */
        public boolean storeConnectionInUse = IMAPStore.$assertionsDisabled;

        ConnectionPool(String name, MailLogger plogger, Session session) {
            Properties props = session.getProperties();
            this.logger = plogger.getSubLogger("connectionpool", "DEBUG IMAP CP", PropUtil.getBooleanProperty(props, "mail." + name + ".connectionpool.debug", IMAPStore.$assertionsDisabled));
            int size = PropUtil.getIntProperty(props, "mail." + name + ".connectionpoolsize", -1);
            if (size > 0) {
                this.poolSize = size;
                if (this.logger.isLoggable(Level.CONFIG)) {
                    this.logger.config("mail.imap.connectionpoolsize: " + this.poolSize);
                }
            } else {
                this.poolSize = 1;
            }
            int connectionPoolTimeout = PropUtil.getIntProperty(props, "mail." + name + ".connectionpooltimeout", -1);
            if (connectionPoolTimeout > 0) {
                this.clientTimeoutInterval = (long) connectionPoolTimeout;
                if (this.logger.isLoggable(Level.CONFIG)) {
                    this.logger.config("mail.imap.connectionpooltimeout: " + this.clientTimeoutInterval);
                }
            } else {
                this.clientTimeoutInterval = 45000;
            }
            int serverTimeout = PropUtil.getIntProperty(props, "mail." + name + ".servertimeout", -1);
            if (serverTimeout > 0) {
                this.serverTimeoutInterval = (long) serverTimeout;
                if (this.logger.isLoggable(Level.CONFIG)) {
                    this.logger.config("mail.imap.servertimeout: " + this.serverTimeoutInterval);
                }
            } else {
                this.serverTimeoutInterval = 1800000;
            }
            int pruning = PropUtil.getIntProperty(props, "mail." + name + ".pruninginterval", -1);
            if (pruning > 0) {
                this.pruningInterval = (long) pruning;
                if (this.logger.isLoggable(Level.CONFIG)) {
                    this.logger.config("mail.imap.pruninginterval: " + this.pruningInterval);
                }
            } else {
                this.pruningInterval = 60000;
            }
            this.separateStoreConnection = PropUtil.getBooleanProperty(props, "mail." + name + ".separatestoreconnection", IMAPStore.$assertionsDisabled);
            if (this.separateStoreConnection) {
                this.logger.config("dedicate a store connection");
            }
        }
    }

    public IMAPStore(Session session, URLName url) {
        this(session, url, "imap", $assertionsDisabled);
    }

    protected IMAPStore(Session session, URLName url, String name2, boolean isSSL2) {
        super(session, url);
        Class<?> folderClass;
        String s;
        this.port = -1;
        this.enableStartTLS = $assertionsDisabled;
        this.requireStartTLS = $assertionsDisabled;
        this.usingSSL = $assertionsDisabled;
        this.enableSASL = $assertionsDisabled;
        this.forcePasswordRefresh = $assertionsDisabled;
        this.enableResponseEvents = $assertionsDisabled;
        this.enableImapEvents = $assertionsDisabled;
        this.throwSearchException = $assertionsDisabled;
        this.peek = $assertionsDisabled;
        this.closeFoldersOnStoreFailure = true;
        this.enableCompress = $assertionsDisabled;
        this.finalizeCleanClose = $assertionsDisabled;
        this.connectionFailed = $assertionsDisabled;
        this.forceClose = $assertionsDisabled;
        this.connectionFailedLock = new Object();
        this.folderConstructor = null;
        this.folderConstructorLI = null;
        this.nonStoreResponseHandler = new ResponseHandler() {
            public void handleResponse(Response r) {
                if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
                    IMAPStore.this.handleResponseCode(r);
                }
                if (r.isBYE()) {
                    IMAPStore.this.logger.fine("IMAPStore non-store connection dead");
                }
            }
        };
        Properties props = session.getProperties();
        name2 = url != null ? url.getProtocol() : name2;
        this.name = name2;
        isSSL2 = !isSSL2 ? PropUtil.getBooleanProperty(props, "mail." + name2 + ".ssl.enable", $assertionsDisabled) : isSSL2;
        if (isSSL2) {
            this.defaultPort = 993;
        } else {
            this.defaultPort = 143;
        }
        this.isSSL = isSSL2;
        this.debug = session.getDebug();
        this.debugusername = PropUtil.getBooleanProperty(props, "mail.debug.auth.username", true);
        this.debugpassword = PropUtil.getBooleanProperty(props, "mail.debug.auth.password", $assertionsDisabled);
        this.logger = new MailLogger(getClass(), "DEBUG " + name2.toUpperCase(Locale.ENGLISH), session.getDebug(), session.getDebugOut());
        if (!PropUtil.getBooleanProperty(props, "mail." + name2 + ".partialfetch", true)) {
            this.blksize = -1;
            this.logger.config("mail.imap.partialfetch: false");
        } else {
            this.blksize = PropUtil.getIntProperty(props, "mail." + name2 + ".fetchsize", 16384);
            if (this.logger.isLoggable(Level.CONFIG)) {
                this.logger.config("mail.imap.fetchsize: " + this.blksize);
            }
        }
        this.ignoreSize = PropUtil.getBooleanProperty(props, "mail." + name2 + ".ignorebodystructuresize", $assertionsDisabled);
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("mail.imap.ignorebodystructuresize: " + this.ignoreSize);
        }
        this.statusCacheTimeout = PropUtil.getIntProperty(props, "mail." + name2 + ".statuscachetimeout", 1000);
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("mail.imap.statuscachetimeout: " + this.statusCacheTimeout);
        }
        this.appendBufferSize = PropUtil.getIntProperty(props, "mail." + name2 + ".appendbuffersize", -1);
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("mail.imap.appendbuffersize: " + this.appendBufferSize);
        }
        this.minIdleTime = PropUtil.getIntProperty(props, "mail." + name2 + ".minidletime", 10);
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("mail.imap.minidletime: " + this.minIdleTime);
        }
        String s2 = session.getProperty("mail." + name2 + ".proxyauth.user");
        if (s2 != null) {
            this.proxyAuthUser = s2;
            if (this.logger.isLoggable(Level.CONFIG)) {
                this.logger.config("mail.imap.proxyauth.user: " + this.proxyAuthUser);
            }
        }
        this.enableStartTLS = PropUtil.getBooleanProperty(props, "mail." + name2 + ".starttls.enable", $assertionsDisabled);
        if (this.enableStartTLS) {
            this.logger.config("enable STARTTLS");
        }
        this.requireStartTLS = PropUtil.getBooleanProperty(props, "mail." + name2 + ".starttls.required", $assertionsDisabled);
        if (this.requireStartTLS) {
            this.logger.config("require STARTTLS");
        }
        this.enableSASL = PropUtil.getBooleanProperty(props, "mail." + name2 + ".sasl.enable", $assertionsDisabled);
        if (this.enableSASL) {
            this.logger.config("enable SASL");
        }
        if (this.enableSASL && (s = session.getProperty("mail." + name2 + ".sasl.mechanisms")) != null && s.length() > 0) {
            if (this.logger.isLoggable(Level.CONFIG)) {
                this.logger.config("SASL mechanisms allowed: " + s);
            }
            List<String> v = new ArrayList<>(5);
            StringTokenizer st = new StringTokenizer(s, " ,");
            while (st.hasMoreTokens()) {
                String m = st.nextToken();
                if (m.length() > 0) {
                    v.add(m);
                }
            }
            this.saslMechanisms = new String[v.size()];
            v.toArray(this.saslMechanisms);
        }
        String s3 = session.getProperty("mail." + name2 + ".sasl.authorizationid");
        if (s3 != null) {
            this.authorizationID = s3;
            this.logger.log(Level.CONFIG, "mail.imap.sasl.authorizationid: {0}", (Object) this.authorizationID);
        }
        String s4 = session.getProperty("mail." + name2 + ".sasl.realm");
        if (s4 != null) {
            this.saslRealm = s4;
            this.logger.log(Level.CONFIG, "mail.imap.sasl.realm: {0}", (Object) this.saslRealm);
        }
        this.forcePasswordRefresh = PropUtil.getBooleanProperty(props, "mail." + name2 + ".forcepasswordrefresh", $assertionsDisabled);
        if (this.forcePasswordRefresh) {
            this.logger.config("enable forcePasswordRefresh");
        }
        this.enableResponseEvents = PropUtil.getBooleanProperty(props, "mail." + name2 + ".enableresponseevents", $assertionsDisabled);
        if (this.enableResponseEvents) {
            this.logger.config("enable IMAP response events");
        }
        this.enableImapEvents = PropUtil.getBooleanProperty(props, "mail." + name2 + ".enableimapevents", $assertionsDisabled);
        if (this.enableImapEvents) {
            this.logger.config("enable IMAP IDLE events");
        }
        this.messageCacheDebug = PropUtil.getBooleanProperty(props, "mail." + name2 + ".messagecache.debug", $assertionsDisabled);
        this.guid = session.getProperty("mail." + name2 + ".yahoo.guid");
        if (this.guid != null) {
            this.logger.log(Level.CONFIG, "mail.imap.yahoo.guid: {0}", (Object) this.guid);
        }
        this.throwSearchException = PropUtil.getBooleanProperty(props, "mail." + name2 + ".throwsearchexception", $assertionsDisabled);
        if (this.throwSearchException) {
            this.logger.config("throw SearchException");
        }
        this.peek = PropUtil.getBooleanProperty(props, "mail." + name2 + ".peek", $assertionsDisabled);
        if (this.peek) {
            this.logger.config("peek");
        }
        this.closeFoldersOnStoreFailure = PropUtil.getBooleanProperty(props, "mail." + name2 + ".closefoldersonstorefailure", true);
        if (this.closeFoldersOnStoreFailure) {
            this.logger.config("closeFoldersOnStoreFailure");
        }
        this.enableCompress = PropUtil.getBooleanProperty(props, "mail." + name2 + ".compress.enable", $assertionsDisabled);
        if (this.enableCompress) {
            this.logger.config("enable COMPRESS");
        }
        this.finalizeCleanClose = PropUtil.getBooleanProperty(props, "mail." + name2 + ".finalizecleanclose", $assertionsDisabled);
        if (this.finalizeCleanClose) {
            this.logger.config("close connection cleanly in finalize");
        }
        String s5 = session.getProperty("mail." + name2 + ".folder.class");
        if (s5 != null) {
            this.logger.log(Level.CONFIG, "IMAP: folder class: {0}", (Object) s5);
            try {
                try {
                    folderClass = Class.forName(s5, $assertionsDisabled, getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    folderClass = Class.forName(s5);
                }
                this.folderConstructor = folderClass.getConstructor(new Class[]{String.class, Character.TYPE, IMAPStore.class, Boolean.class});
                this.folderConstructorLI = folderClass.getConstructor(new Class[]{ListInfo.class, IMAPStore.class});
            } catch (Exception ex) {
                this.logger.log(Level.CONFIG, "IMAP: failed to load folder class", (Throwable) ex);
            }
        }
        this.pool = new ConnectionPool(name2, this.logger, session);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:19:0x0057=Splitter:B:19:0x0057, B:5:0x0008=Splitter:B:5:0x0008} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean protocolConnect(java.lang.String r12, int r13, java.lang.String r14, java.lang.String r15) throws javax.mail.MessagingException {
        /*
            r11 = this;
            monitor-enter(r11)
            r5 = 0
            if (r12 == 0) goto L_0x0008
            if (r15 == 0) goto L_0x0008
            if (r14 != 0) goto L_0x0049
        L_0x0008:
            com.sun.mail.util.MailLogger r8 = r11.logger     // Catch:{ all -> 0x013c }
            java.util.logging.Level r9 = java.util.logging.Level.FINE     // Catch:{ all -> 0x013c }
            boolean r8 = r8.isLoggable(r9)     // Catch:{ all -> 0x013c }
            if (r8 == 0) goto L_0x0046
            com.sun.mail.util.MailLogger r8 = r11.logger     // Catch:{ all -> 0x013c }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x013c }
            r9.<init>()     // Catch:{ all -> 0x013c }
            java.lang.String r10 = "protocolConnect returning false, host="
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x013c }
            java.lang.StringBuilder r9 = r9.append(r12)     // Catch:{ all -> 0x013c }
            java.lang.String r10 = ", user="
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x013c }
            java.lang.String r10 = r11.traceUser(r14)     // Catch:{ all -> 0x013c }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x013c }
            java.lang.String r10 = ", password="
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x013c }
            java.lang.String r10 = r11.tracePassword(r15)     // Catch:{ all -> 0x013c }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x013c }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x013c }
            r8.fine(r9)     // Catch:{ all -> 0x013c }
        L_0x0046:
            r8 = 0
        L_0x0047:
            monitor-exit(r11)
            return r8
        L_0x0049:
            r8 = -1
            if (r13 == r8) goto L_0x0111
            r11.port = r13     // Catch:{ all -> 0x013c }
        L_0x004e:
            int r8 = r11.port     // Catch:{ all -> 0x013c }
            r9 = -1
            if (r8 != r9) goto L_0x0057
            int r8 = r11.defaultPort     // Catch:{ all -> 0x013c }
            r11.port = r8     // Catch:{ all -> 0x013c }
        L_0x0057:
            com.sun.mail.imap.IMAPStore$ConnectionPool r9 = r11.pool     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            monitor-enter(r9)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r11.pool     // Catch:{ all -> 0x013f }
            java.util.Vector r8 = r8.authenticatedConnections     // Catch:{ all -> 0x013f }
            boolean r4 = r8.isEmpty()     // Catch:{ all -> 0x013f }
            monitor-exit(r9)     // Catch:{ all -> 0x013f }
            if (r4 == 0) goto L_0x010e
            com.sun.mail.util.MailLogger r8 = r11.logger     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.util.logging.Level r9 = java.util.logging.Level.FINE     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            boolean r8 = r8.isLoggable(r9)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            if (r8 == 0) goto L_0x00a1
            com.sun.mail.util.MailLogger r8 = r11.logger     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r9.<init>()     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r10 = "trying to connect to host \""
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.StringBuilder r9 = r9.append(r12)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r10 = "\", port "
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            int r10 = r11.port     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r10 = ", isSSL "
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            boolean r10 = r11.isSSL     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r9 = r9.toString()     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r8.fine(r9)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
        L_0x00a1:
            int r8 = r11.port     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r11.newIMAPProtocol(r12, r8)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            com.sun.mail.util.MailLogger r8 = r11.logger     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.util.logging.Level r9 = java.util.logging.Level.FINE     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            boolean r8 = r8.isLoggable(r9)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            if (r8 == 0) goto L_0x00e5
            com.sun.mail.util.MailLogger r8 = r11.logger     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r9.<init>()     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r10 = "protocolConnect login, host="
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.StringBuilder r9 = r9.append(r12)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r10 = ", user="
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r10 = r11.traceUser(r14)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r10 = ", password="
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r10 = r11.tracePassword(r15)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            java.lang.String r9 = r9.toString()     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r8.fine(r9)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
        L_0x00e5:
            com.sun.mail.iap.ResponseHandler r8 = r11.nonStoreResponseHandler     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r5.addResponseHandler(r8)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r11.login(r5, r14, r15)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            com.sun.mail.iap.ResponseHandler r8 = r11.nonStoreResponseHandler     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r5.removeResponseHandler(r8)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r5.addResponseHandler(r11)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            boolean r8 = r5.isSSL()     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r11.usingSSL = r8     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r11.host = r12     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r11.user = r14     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            r11.password = r15     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            com.sun.mail.imap.IMAPStore$ConnectionPool r9 = r11.pool     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            monitor-enter(r9)     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r11.pool     // Catch:{ all -> 0x0157 }
            java.util.Vector r8 = r8.authenticatedConnections     // Catch:{ all -> 0x0157 }
            r8.addElement(r5)     // Catch:{ all -> 0x0157 }
            monitor-exit(r9)     // Catch:{ all -> 0x0157 }
        L_0x010e:
            r8 = 1
            goto L_0x0047
        L_0x0111:
            javax.mail.Session r8 = r11.session     // Catch:{ all -> 0x013c }
            java.util.Properties r8 = r8.getProperties()     // Catch:{ all -> 0x013c }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x013c }
            r9.<init>()     // Catch:{ all -> 0x013c }
            java.lang.String r10 = "mail."
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x013c }
            java.lang.String r10 = r11.name     // Catch:{ all -> 0x013c }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x013c }
            java.lang.String r10 = ".port"
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x013c }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x013c }
            int r10 = r11.port     // Catch:{ all -> 0x013c }
            int r8 = com.sun.mail.util.PropUtil.getIntProperty(r8, r9, r10)     // Catch:{ all -> 0x013c }
            r11.port = r8     // Catch:{ all -> 0x013c }
            goto L_0x004e
        L_0x013c:
            r8 = move-exception
            monitor-exit(r11)
            throw r8
        L_0x013f:
            r8 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x013f }
            throw r8     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
        L_0x0142:
            r1 = move-exception
            if (r5 == 0) goto L_0x0148
            r5.disconnect()     // Catch:{ all -> 0x013c }
        L_0x0148:
            r5 = 0
            com.sun.mail.imap.ReferralException r8 = new com.sun.mail.imap.ReferralException     // Catch:{ all -> 0x013c }
            java.lang.String r9 = r1.getUrl()     // Catch:{ all -> 0x013c }
            java.lang.String r10 = r1.getMessage()     // Catch:{ all -> 0x013c }
            r8.<init>(r9, r10)     // Catch:{ all -> 0x013c }
            throw r8     // Catch:{ all -> 0x013c }
        L_0x0157:
            r8 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0157 }
            throw r8     // Catch:{ IMAPReferralException -> 0x0142, CommandFailedException -> 0x015a, ProtocolException -> 0x0176, SocketConnectException -> 0x0187, IOException -> 0x018e }
        L_0x015a:
            r0 = move-exception
            if (r5 == 0) goto L_0x0160
            r5.disconnect()     // Catch:{ all -> 0x013c }
        L_0x0160:
            r5 = 0
            com.sun.mail.iap.Response r6 = r0.getResponse()     // Catch:{ all -> 0x013c }
            javax.mail.AuthenticationFailedException r9 = new javax.mail.AuthenticationFailedException     // Catch:{ all -> 0x013c }
            if (r6 == 0) goto L_0x0171
            java.lang.String r8 = r6.getRest()     // Catch:{ all -> 0x013c }
        L_0x016d:
            r9.<init>(r8)     // Catch:{ all -> 0x013c }
            throw r9     // Catch:{ all -> 0x013c }
        L_0x0171:
            java.lang.String r8 = r0.getMessage()     // Catch:{ all -> 0x013c }
            goto L_0x016d
        L_0x0176:
            r3 = move-exception
            if (r5 == 0) goto L_0x017c
            r5.disconnect()     // Catch:{ all -> 0x013c }
        L_0x017c:
            r5 = 0
            javax.mail.MessagingException r8 = new javax.mail.MessagingException     // Catch:{ all -> 0x013c }
            java.lang.String r9 = r3.getMessage()     // Catch:{ all -> 0x013c }
            r8.<init>(r9, r3)     // Catch:{ all -> 0x013c }
            throw r8     // Catch:{ all -> 0x013c }
        L_0x0187:
            r7 = move-exception
            com.sun.mail.util.MailConnectException r8 = new com.sun.mail.util.MailConnectException     // Catch:{ all -> 0x013c }
            r8.<init>(r7)     // Catch:{ all -> 0x013c }
            throw r8     // Catch:{ all -> 0x013c }
        L_0x018e:
            r2 = move-exception
            javax.mail.MessagingException r8 = new javax.mail.MessagingException     // Catch:{ all -> 0x013c }
            java.lang.String r9 = r2.getMessage()     // Catch:{ all -> 0x013c }
            r8.<init>(r9, r2)     // Catch:{ all -> 0x013c }
            throw r8     // Catch:{ all -> 0x013c }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.protocolConnect(java.lang.String, int, java.lang.String, java.lang.String):boolean");
    }

    /* access modifiers changed from: protected */
    public IMAPProtocol newIMAPProtocol(String host2, int port2) throws IOException, ProtocolException {
        return new IMAPProtocol(this.name, host2, port2, this.session.getProperties(), this.isSSL, this.logger);
    }

    private void login(IMAPProtocol p, String u, String pw) throws ProtocolException {
        String authzid;
        if ((this.enableStartTLS || this.requireStartTLS) && !p.isSSL()) {
            if (p.hasCapability("STARTTLS")) {
                p.startTLS();
                p.capability();
            } else if (this.requireStartTLS) {
                this.logger.fine("STARTTLS required but not supported by server");
                throw new ProtocolException("STARTTLS required but not supported by server");
            }
        }
        if (!p.isAuthenticated()) {
            preLogin(p);
            if (this.guid != null) {
                Map<String, String> gmap = new HashMap<>();
                gmap.put("GUID", this.guid);
                p.mo13040id(gmap);
            }
            p.getCapabilities().put("__PRELOGIN__", "");
            if (this.authorizationID != null) {
                authzid = this.authorizationID;
            } else if (this.proxyAuthUser != null) {
                authzid = this.proxyAuthUser;
            } else {
                authzid = null;
            }
            if (this.enableSASL) {
                try {
                    p.sasllogin(this.saslMechanisms, this.saslRealm, authzid, u, pw);
                    if (!p.isAuthenticated()) {
                        throw new CommandFailedException("SASL authentication failed");
                    }
                } catch (UnsupportedOperationException e) {
                }
            }
            if (!p.isAuthenticated()) {
                authenticate(p, authzid, u, pw);
            }
            if (this.proxyAuthUser != null) {
                p.proxyauth(this.proxyAuthUser);
            }
            if (p.hasCapability("__PRELOGIN__")) {
                try {
                    p.capability();
                } catch (ConnectionException cex) {
                    throw cex;
                } catch (ProtocolException e2) {
                }
            }
            if (this.enableCompress && p.hasCapability("COMPRESS=DEFLATE")) {
                p.compress();
            }
            if (p.hasCapability("UTF8=ACCEPT") || p.hasCapability("UTF8=ONLY")) {
                p.enable("UTF8=ACCEPT");
            }
        }
    }

    private void authenticate(IMAPProtocol p, String authzid, String user2, String password2) throws ProtocolException {
        String mechs = this.session.getProperty("mail." + this.name + ".auth.mechanisms");
        if (mechs == null) {
            mechs = "PLAIN LOGIN NTLM XOAUTH2";
        }
        StringTokenizer st = new StringTokenizer(mechs);
        while (st.hasMoreTokens()) {
            String m = st.nextToken().toUpperCase(Locale.ENGLISH);
            if (mechs == "PLAIN LOGIN NTLM XOAUTH2") {
                String dprop = "mail." + this.name + ".auth." + m.toLowerCase(Locale.ENGLISH) + ".disable";
                if (PropUtil.getBooleanProperty(this.session.getProperties(), dprop, m.equals("XOAUTH2"))) {
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine("mechanism " + m + " disabled by property: " + dprop);
                    }
                }
            }
            if (!p.hasCapability("AUTH=" + m) && (!m.equals("LOGIN") || !p.hasCapability("AUTH-LOGIN"))) {
                this.logger.log(Level.FINE, "mechanism {0} not supported by server", (Object) m);
            } else if (m.equals("PLAIN")) {
                p.authplain(authzid, user2, password2);
                return;
            } else if (m.equals("LOGIN")) {
                p.authlogin(user2, password2);
                return;
            } else if (m.equals("NTLM")) {
                p.authntlm(authzid, user2, password2);
                return;
            } else if (m.equals("XOAUTH2")) {
                p.authoauth2(user2, password2);
                return;
            } else {
                this.logger.log(Level.FINE, "no authenticator for mechanism {0}", (Object) m);
            }
        }
        if (!p.hasCapability("LOGINDISABLED")) {
            p.login(user2, password2);
            return;
        }
        throw new ProtocolException("No login methods supported!");
    }

    /* access modifiers changed from: protected */
    public void preLogin(IMAPProtocol p) throws ProtocolException {
    }

    public synchronized boolean isSSL() {
        return this.usingSSL;
    }

    public synchronized void setUsername(String user2) {
        this.user = user2;
    }

    public synchronized void setPassword(String password2) {
        this.password = password2;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v24, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.sun.mail.imap.protocol.IMAPProtocol} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sun.mail.imap.protocol.IMAPProtocol getProtocol(com.sun.mail.imap.IMAPFolder r15) throws javax.mail.MessagingException {
        /*
            r14 = this;
            r3 = 0
        L_0x0001:
            if (r3 != 0) goto L_0x014a
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r14.pool
            monitor-enter(r8)
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            java.util.Vector r7 = r7.authenticatedConnections     // Catch:{ all -> 0x0060 }
            boolean r7 = r7.isEmpty()     // Catch:{ all -> 0x0060 }
            if (r7 != 0) goto L_0x002f
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            java.util.Vector r7 = r7.authenticatedConnections     // Catch:{ all -> 0x0060 }
            int r7 = r7.size()     // Catch:{ all -> 0x0060 }
            r9 = 1
            if (r7 != r9) goto L_0x006b
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            boolean r7 = r7.separateStoreConnection     // Catch:{ all -> 0x0060 }
            if (r7 != 0) goto L_0x002f
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            boolean r7 = r7.storeConnectionInUse     // Catch:{ all -> 0x0060 }
            if (r7 == 0) goto L_0x006b
        L_0x002f:
            com.sun.mail.util.MailLogger r7 = r14.logger     // Catch:{ all -> 0x0060 }
            java.lang.String r9 = "no connections in the pool, creating a new one"
            r7.fine(r9)     // Catch:{ all -> 0x0060 }
            boolean r7 = r14.forcePasswordRefresh     // Catch:{ Exception -> 0x0063 }
            if (r7 == 0) goto L_0x003d
            r14.refreshPassword()     // Catch:{ Exception -> 0x0063 }
        L_0x003d:
            java.lang.String r7 = r14.host     // Catch:{ Exception -> 0x0063 }
            int r9 = r14.port     // Catch:{ Exception -> 0x0063 }
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r14.newIMAPProtocol(r7, r9)     // Catch:{ Exception -> 0x0063 }
            com.sun.mail.iap.ResponseHandler r7 = r14.nonStoreResponseHandler     // Catch:{ Exception -> 0x0063 }
            r3.addResponseHandler(r7)     // Catch:{ Exception -> 0x0063 }
            java.lang.String r7 = r14.user     // Catch:{ Exception -> 0x0063 }
            java.lang.String r9 = r14.password     // Catch:{ Exception -> 0x0063 }
            r14.login(r3, r7, r9)     // Catch:{ Exception -> 0x0063 }
            com.sun.mail.iap.ResponseHandler r7 = r14.nonStoreResponseHandler     // Catch:{ Exception -> 0x0063 }
            r3.removeResponseHandler(r7)     // Catch:{ Exception -> 0x0063 }
        L_0x0056:
            if (r3 != 0) goto L_0x010a
            javax.mail.MessagingException r7 = new javax.mail.MessagingException     // Catch:{ all -> 0x0060 }
            java.lang.String r9 = "connection failure"
            r7.<init>(r9)     // Catch:{ all -> 0x0060 }
            throw r7     // Catch:{ all -> 0x0060 }
        L_0x0060:
            r7 = move-exception
            monitor-exit(r8)     // Catch:{ all -> 0x0060 }
            throw r7
        L_0x0063:
            r2 = move-exception
            if (r3 == 0) goto L_0x0069
            r3.disconnect()     // Catch:{ Exception -> 0x0147 }
        L_0x0069:
            r3 = 0
            goto L_0x0056
        L_0x006b:
            com.sun.mail.util.MailLogger r7 = r14.logger     // Catch:{ all -> 0x0060 }
            java.util.logging.Level r9 = java.util.logging.Level.FINE     // Catch:{ all -> 0x0060 }
            boolean r7 = r7.isLoggable(r9)     // Catch:{ all -> 0x0060 }
            if (r7 == 0) goto L_0x0097
            com.sun.mail.util.MailLogger r7 = r14.logger     // Catch:{ all -> 0x0060 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x0060 }
            r9.<init>()     // Catch:{ all -> 0x0060 }
            java.lang.String r10 = "connection available -- size: "
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x0060 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r10 = r14.pool     // Catch:{ all -> 0x0060 }
            java.util.Vector r10 = r10.authenticatedConnections     // Catch:{ all -> 0x0060 }
            int r10 = r10.size()     // Catch:{ all -> 0x0060 }
            java.lang.StringBuilder r9 = r9.append(r10)     // Catch:{ all -> 0x0060 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0060 }
            r7.fine(r9)     // Catch:{ all -> 0x0060 }
        L_0x0097:
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            java.util.Vector r7 = r7.authenticatedConnections     // Catch:{ all -> 0x0060 }
            java.lang.Object r7 = r7.lastElement()     // Catch:{ all -> 0x0060 }
            r0 = r7
            com.sun.mail.imap.protocol.IMAPProtocol r0 = (com.sun.mail.imap.protocol.IMAPProtocol) r0     // Catch:{ all -> 0x0060 }
            r3 = r0
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            java.util.Vector r7 = r7.authenticatedConnections     // Catch:{ all -> 0x0060 }
            r7.removeElement(r3)     // Catch:{ all -> 0x0060 }
            long r10 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0060 }
            long r12 = r3.getTimestamp()     // Catch:{ all -> 0x0060 }
            long r4 = r10 - r12
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            long r10 = r7.serverTimeoutInterval     // Catch:{ all -> 0x0060 }
            int r7 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1))
            if (r7 <= 0) goto L_0x00d5
            r3.removeResponseHandler(r14)     // Catch:{ ProtocolException -> 0x012d }
            com.sun.mail.iap.ResponseHandler r7 = r14.nonStoreResponseHandler     // Catch:{ ProtocolException -> 0x012d }
            r3.addResponseHandler(r7)     // Catch:{ ProtocolException -> 0x012d }
            r3.noop()     // Catch:{ ProtocolException -> 0x012d }
            com.sun.mail.iap.ResponseHandler r7 = r14.nonStoreResponseHandler     // Catch:{ ProtocolException -> 0x012d }
            r3.removeResponseHandler(r7)     // Catch:{ ProtocolException -> 0x012d }
            r3.addResponseHandler(r14)     // Catch:{ ProtocolException -> 0x012d }
        L_0x00d5:
            java.lang.String r7 = r14.proxyAuthUser     // Catch:{ all -> 0x0060 }
            if (r7 == 0) goto L_0x0107
            java.lang.String r7 = r14.proxyAuthUser     // Catch:{ all -> 0x0060 }
            java.lang.String r9 = r3.getProxyAuthUser()     // Catch:{ all -> 0x0060 }
            boolean r7 = r7.equals(r9)     // Catch:{ all -> 0x0060 }
            if (r7 != 0) goto L_0x0107
            java.lang.String r7 = "X-UNAUTHENTICATE"
            boolean r7 = r3.hasCapability(r7)     // Catch:{ all -> 0x0060 }
            if (r7 == 0) goto L_0x0107
            r3.removeResponseHandler(r14)     // Catch:{ ProtocolException -> 0x013a }
            com.sun.mail.iap.ResponseHandler r7 = r14.nonStoreResponseHandler     // Catch:{ ProtocolException -> 0x013a }
            r3.addResponseHandler(r7)     // Catch:{ ProtocolException -> 0x013a }
            r3.unauthenticate()     // Catch:{ ProtocolException -> 0x013a }
            java.lang.String r7 = r14.user     // Catch:{ ProtocolException -> 0x013a }
            java.lang.String r9 = r14.password     // Catch:{ ProtocolException -> 0x013a }
            r14.login(r3, r7, r9)     // Catch:{ ProtocolException -> 0x013a }
            com.sun.mail.iap.ResponseHandler r7 = r14.nonStoreResponseHandler     // Catch:{ ProtocolException -> 0x013a }
            r3.removeResponseHandler(r7)     // Catch:{ ProtocolException -> 0x013a }
            r3.addResponseHandler(r14)     // Catch:{ ProtocolException -> 0x013a }
        L_0x0107:
            r3.removeResponseHandler(r14)     // Catch:{ all -> 0x0060 }
        L_0x010a:
            r14.timeoutConnections()     // Catch:{ all -> 0x0060 }
            if (r15 == 0) goto L_0x012a
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            java.util.Vector r7 = r7.folders     // Catch:{ all -> 0x0060 }
            if (r7 != 0) goto L_0x0121
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            java.util.Vector r9 = new java.util.Vector     // Catch:{ all -> 0x0060 }
            r9.<init>()     // Catch:{ all -> 0x0060 }
            java.util.Vector unused = r7.folders = r9     // Catch:{ all -> 0x0060 }
        L_0x0121:
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r14.pool     // Catch:{ all -> 0x0060 }
            java.util.Vector r7 = r7.folders     // Catch:{ all -> 0x0060 }
            r7.addElement(r15)     // Catch:{ all -> 0x0060 }
        L_0x012a:
            monitor-exit(r8)     // Catch:{ all -> 0x0060 }
            goto L_0x0001
        L_0x012d:
            r6 = move-exception
            com.sun.mail.iap.ResponseHandler r7 = r14.nonStoreResponseHandler     // Catch:{ RuntimeException -> 0x014d }
            r3.removeResponseHandler(r7)     // Catch:{ RuntimeException -> 0x014d }
            r3.disconnect()     // Catch:{ RuntimeException -> 0x014d }
        L_0x0136:
            r3 = 0
            monitor-exit(r8)     // Catch:{ all -> 0x0060 }
            goto L_0x0001
        L_0x013a:
            r6 = move-exception
            com.sun.mail.iap.ResponseHandler r7 = r14.nonStoreResponseHandler     // Catch:{ RuntimeException -> 0x014b }
            r3.removeResponseHandler(r7)     // Catch:{ RuntimeException -> 0x014b }
            r3.disconnect()     // Catch:{ RuntimeException -> 0x014b }
        L_0x0143:
            r3 = 0
            monitor-exit(r8)     // Catch:{ all -> 0x0060 }
            goto L_0x0001
        L_0x0147:
            r7 = move-exception
            goto L_0x0069
        L_0x014a:
            return r3
        L_0x014b:
            r7 = move-exception
            goto L_0x0143
        L_0x014d:
            r7 = move-exception
            goto L_0x0136
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.getProtocol(com.sun.mail.imap.IMAPFolder):com.sun.mail.imap.protocol.IMAPProtocol");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v9, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.sun.mail.imap.protocol.IMAPProtocol} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.sun.mail.imap.protocol.IMAPProtocol getStoreProtocol() throws com.sun.mail.iap.ProtocolException {
        /*
            r8 = this;
            r3 = 0
        L_0x0001:
            if (r3 != 0) goto L_0x00f5
            com.sun.mail.imap.IMAPStore$ConnectionPool r5 = r8.pool
            monitor-enter(r5)
            r8.waitIfIdle()     // Catch:{ all -> 0x0040 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ all -> 0x0040 }
            java.util.Vector r4 = r4.authenticatedConnections     // Catch:{ all -> 0x0040 }
            boolean r4 = r4.isEmpty()     // Catch:{ all -> 0x0040 }
            if (r4 == 0) goto L_0x006a
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ all -> 0x0040 }
            com.sun.mail.util.MailLogger r4 = r4.logger     // Catch:{ all -> 0x0040 }
            java.lang.String r6 = "getStoreProtocol() - no connections in the pool, creating a new one"
            r4.fine(r6)     // Catch:{ all -> 0x0040 }
            boolean r4 = r8.forcePasswordRefresh     // Catch:{ Exception -> 0x0043 }
            if (r4 == 0) goto L_0x0027
            r8.refreshPassword()     // Catch:{ Exception -> 0x0043 }
        L_0x0027:
            java.lang.String r4 = r8.host     // Catch:{ Exception -> 0x0043 }
            int r6 = r8.port     // Catch:{ Exception -> 0x0043 }
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r8.newIMAPProtocol(r4, r6)     // Catch:{ Exception -> 0x0043 }
            java.lang.String r4 = r8.user     // Catch:{ Exception -> 0x0043 }
            java.lang.String r6 = r8.password     // Catch:{ Exception -> 0x0043 }
            r8.login(r3, r4, r6)     // Catch:{ Exception -> 0x0043 }
        L_0x0036:
            if (r3 != 0) goto L_0x004b
            com.sun.mail.iap.ConnectionException r4 = new com.sun.mail.iap.ConnectionException     // Catch:{ all -> 0x0040 }
            java.lang.String r6 = "failed to create new store connection"
            r4.<init>(r6)     // Catch:{ all -> 0x0040 }
            throw r4     // Catch:{ all -> 0x0040 }
        L_0x0040:
            r4 = move-exception
            monitor-exit(r5)     // Catch:{ all -> 0x0040 }
            throw r4
        L_0x0043:
            r2 = move-exception
            if (r3 == 0) goto L_0x0049
            r3.logout()     // Catch:{ Exception -> 0x00f2 }
        L_0x0049:
            r3 = 0
            goto L_0x0036
        L_0x004b:
            r3.addResponseHandler(r8)     // Catch:{ all -> 0x0040 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ all -> 0x0040 }
            java.util.Vector r4 = r4.authenticatedConnections     // Catch:{ all -> 0x0040 }
            r4.addElement(r3)     // Catch:{ all -> 0x0040 }
        L_0x0057:
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ all -> 0x0040 }
            boolean r4 = r4.storeConnectionInUse     // Catch:{ all -> 0x0040 }
            if (r4 == 0) goto L_0x00df
            r3 = 0
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ InterruptedException -> 0x00cf }
            r4.wait()     // Catch:{ InterruptedException -> 0x00cf }
        L_0x0065:
            r8.timeoutConnections()     // Catch:{ all -> 0x0040 }
            monitor-exit(r5)     // Catch:{ all -> 0x0040 }
            goto L_0x0001
        L_0x006a:
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ all -> 0x0040 }
            com.sun.mail.util.MailLogger r4 = r4.logger     // Catch:{ all -> 0x0040 }
            java.util.logging.Level r6 = java.util.logging.Level.FINE     // Catch:{ all -> 0x0040 }
            boolean r4 = r4.isLoggable(r6)     // Catch:{ all -> 0x0040 }
            if (r4 == 0) goto L_0x009e
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ all -> 0x0040 }
            com.sun.mail.util.MailLogger r4 = r4.logger     // Catch:{ all -> 0x0040 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0040 }
            r6.<init>()     // Catch:{ all -> 0x0040 }
            java.lang.String r7 = "getStoreProtocol() - connection available -- size: "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0040 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r7 = r8.pool     // Catch:{ all -> 0x0040 }
            java.util.Vector r7 = r7.authenticatedConnections     // Catch:{ all -> 0x0040 }
            int r7 = r7.size()     // Catch:{ all -> 0x0040 }
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0040 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0040 }
            r4.fine(r6)     // Catch:{ all -> 0x0040 }
        L_0x009e:
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ all -> 0x0040 }
            java.util.Vector r4 = r4.authenticatedConnections     // Catch:{ all -> 0x0040 }
            java.lang.Object r4 = r4.firstElement()     // Catch:{ all -> 0x0040 }
            r0 = r4
            com.sun.mail.imap.protocol.IMAPProtocol r0 = (com.sun.mail.imap.protocol.IMAPProtocol) r0     // Catch:{ all -> 0x0040 }
            r3 = r0
            java.lang.String r4 = r8.proxyAuthUser     // Catch:{ all -> 0x0040 }
            if (r4 == 0) goto L_0x0057
            java.lang.String r4 = r8.proxyAuthUser     // Catch:{ all -> 0x0040 }
            java.lang.String r6 = r3.getProxyAuthUser()     // Catch:{ all -> 0x0040 }
            boolean r4 = r4.equals(r6)     // Catch:{ all -> 0x0040 }
            if (r4 != 0) goto L_0x0057
            java.lang.String r4 = "X-UNAUTHENTICATE"
            boolean r4 = r3.hasCapability(r4)     // Catch:{ all -> 0x0040 }
            if (r4 == 0) goto L_0x0057
            r3.unauthenticate()     // Catch:{ all -> 0x0040 }
            java.lang.String r4 = r8.user     // Catch:{ all -> 0x0040 }
            java.lang.String r6 = r8.password     // Catch:{ all -> 0x0040 }
            r8.login(r3, r4, r6)     // Catch:{ all -> 0x0040 }
            goto L_0x0057
        L_0x00cf:
            r1 = move-exception
            java.lang.Thread r4 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0040 }
            r4.interrupt()     // Catch:{ all -> 0x0040 }
            com.sun.mail.iap.ProtocolException r4 = new com.sun.mail.iap.ProtocolException     // Catch:{ all -> 0x0040 }
            java.lang.String r6 = "Interrupted getStoreProtocol"
            r4.<init>(r6, r1)     // Catch:{ all -> 0x0040 }
            throw r4     // Catch:{ all -> 0x0040 }
        L_0x00df:
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ all -> 0x0040 }
            r6 = 1
            boolean unused = r4.storeConnectionInUse = r6     // Catch:{ all -> 0x0040 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r8.pool     // Catch:{ all -> 0x0040 }
            com.sun.mail.util.MailLogger r4 = r4.logger     // Catch:{ all -> 0x0040 }
            java.lang.String r6 = "getStoreProtocol() -- storeConnectionInUse"
            r4.fine(r6)     // Catch:{ all -> 0x0040 }
            goto L_0x0065
        L_0x00f2:
            r4 = move-exception
            goto L_0x0049
        L_0x00f5:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.getStoreProtocol():com.sun.mail.imap.protocol.IMAPProtocol");
    }

    /* access modifiers changed from: package-private */
    public IMAPProtocol getFolderStoreProtocol() throws ProtocolException {
        IMAPProtocol p = getStoreProtocol();
        p.removeResponseHandler(this);
        p.addResponseHandler(this.nonStoreResponseHandler);
        return p;
    }

    private void refreshPassword() {
        InetAddress addr;
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("refresh password, user: " + traceUser(this.user));
        }
        try {
            addr = InetAddress.getByName(this.host);
        } catch (UnknownHostException e) {
            addr = null;
        }
        PasswordAuthentication pa = this.session.requestPasswordAuthentication(addr, this.port, this.name, (String) null, this.user);
        if (pa != null) {
            this.user = pa.getUserName();
            this.password = pa.getPassword();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean allowReadOnlySelect() {
        return PropUtil.getBooleanProperty(this.session.getProperties(), "mail." + this.name + ".allowreadonlyselect", $assertionsDisabled);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSeparateStoreConnection() {
        return this.pool.separateStoreConnection;
    }

    /* access modifiers changed from: package-private */
    public MailLogger getConnectionPoolLogger() {
        return this.pool.logger;
    }

    /* access modifiers changed from: package-private */
    public boolean getMessageCacheDebug() {
        return this.messageCacheDebug;
    }

    /* access modifiers changed from: package-private */
    public boolean isConnectionPoolFull() {
        boolean z;
        synchronized (this.pool) {
            if (this.pool.logger.isLoggable(Level.FINE)) {
                this.pool.logger.fine("connection pool current size: " + this.pool.authenticatedConnections.size() + "   pool size: " + this.pool.poolSize);
            }
            z = this.pool.authenticatedConnections.size() >= this.pool.poolSize ? true : $assertionsDisabled;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void releaseProtocol(IMAPFolder folder, IMAPProtocol protocol) {
        synchronized (this.pool) {
            if (protocol != null) {
                if (!isConnectionPoolFull()) {
                    protocol.addResponseHandler(this);
                    this.pool.authenticatedConnections.addElement(protocol);
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine("added an Authenticated connection -- size: " + this.pool.authenticatedConnections.size());
                    }
                } else {
                    this.logger.fine("pool is full, not adding an Authenticated connection");
                    try {
                        protocol.logout();
                    } catch (ProtocolException e) {
                    }
                }
            }
            if (this.pool.folders != null) {
                this.pool.folders.removeElement(folder);
            }
            timeoutConnections();
        }
    }

    private void releaseStoreProtocol(IMAPProtocol protocol) {
        boolean failed;
        if (protocol == null) {
            cleanup();
            return;
        }
        synchronized (this.connectionFailedLock) {
            failed = this.connectionFailed;
            this.connectionFailed = $assertionsDisabled;
        }
        synchronized (this.pool) {
            boolean unused = this.pool.storeConnectionInUse = $assertionsDisabled;
            this.pool.notifyAll();
            this.pool.logger.fine("releaseStoreProtocol()");
            timeoutConnections();
        }
        if (!$assertionsDisabled && Thread.holdsLock(this.pool)) {
            throw new AssertionError();
        } else if (failed) {
            cleanup();
        }
    }

    /* access modifiers changed from: package-private */
    public void releaseFolderStoreProtocol(IMAPProtocol protocol) {
        if (protocol != null) {
            protocol.removeResponseHandler(this.nonStoreResponseHandler);
            protocol.addResponseHandler(this);
            synchronized (this.pool) {
                boolean unused = this.pool.storeConnectionInUse = $assertionsDisabled;
                this.pool.notifyAll();
                this.pool.logger.fine("releaseFolderStoreProtocol()");
                timeoutConnections();
            }
        }
    }

    private void emptyConnectionPool(boolean force) {
        synchronized (this.pool) {
            for (int index = this.pool.authenticatedConnections.size() - 1; index >= 0; index--) {
                try {
                    IMAPProtocol p = (IMAPProtocol) this.pool.authenticatedConnections.elementAt(index);
                    p.removeResponseHandler(this);
                    if (force) {
                        p.disconnect();
                    } else {
                        p.logout();
                    }
                } catch (ProtocolException e) {
                }
            }
            this.pool.authenticatedConnections.removeAllElements();
        }
        this.pool.logger.fine("removed all authenticated connections from pool");
    }

    private void timeoutConnections() {
        synchronized (this.pool) {
            if (System.currentTimeMillis() - this.pool.lastTimePruned > this.pool.pruningInterval && this.pool.authenticatedConnections.size() > 1) {
                if (this.pool.logger.isLoggable(Level.FINE)) {
                    this.pool.logger.fine("checking for connections to prune: " + (System.currentTimeMillis() - this.pool.lastTimePruned));
                    this.pool.logger.fine("clientTimeoutInterval: " + this.pool.clientTimeoutInterval);
                }
                for (int index = this.pool.authenticatedConnections.size() - 1; index > 0; index--) {
                    IMAPProtocol p = (IMAPProtocol) this.pool.authenticatedConnections.elementAt(index);
                    if (this.pool.logger.isLoggable(Level.FINE)) {
                        this.pool.logger.fine("protocol last used: " + (System.currentTimeMillis() - p.getTimestamp()));
                    }
                    if (System.currentTimeMillis() - p.getTimestamp() > this.pool.clientTimeoutInterval) {
                        this.pool.logger.fine("authenticated connection timed out, logging out the connection");
                        p.removeResponseHandler(this);
                        this.pool.authenticatedConnections.removeElementAt(index);
                        try {
                            p.logout();
                        } catch (ProtocolException e) {
                        }
                    }
                }
                long unused = this.pool.lastTimePruned = System.currentTimeMillis();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getFetchBlockSize() {
        return this.blksize;
    }

    /* access modifiers changed from: package-private */
    public boolean ignoreBodyStructureSize() {
        return this.ignoreSize;
    }

    /* access modifiers changed from: package-private */
    public Session getSession() {
        return this.session;
    }

    /* access modifiers changed from: package-private */
    public int getStatusCacheTimeout() {
        return this.statusCacheTimeout;
    }

    /* access modifiers changed from: package-private */
    public int getAppendBufferSize() {
        return this.appendBufferSize;
    }

    /* access modifiers changed from: package-private */
    public int getMinIdleTime() {
        return this.minIdleTime;
    }

    /* access modifiers changed from: package-private */
    public boolean throwSearchException() {
        return this.throwSearchException;
    }

    /* access modifiers changed from: package-private */
    public boolean getPeek() {
        return this.peek;
    }

    public synchronized boolean hasCapability(String capability) throws MessagingException {
        boolean hasCapability;
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            hasCapability = p.hasCapability(capability);
            releaseStoreProtocol(p);
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        } catch (Throwable th) {
            releaseStoreProtocol(p);
            throw th;
        }
        return hasCapability;
    }

    public void setProxyAuthUser(String user2) {
        this.proxyAuthUser = user2;
    }

    public String getProxyAuthUser() {
        return this.proxyAuthUser;
    }

    public synchronized boolean isConnected() {
        boolean isConnected;
        if (!super.isConnected()) {
            isConnected = $assertionsDisabled;
        } else {
            IMAPProtocol p = null;
            try {
                p = getStoreProtocol();
                p.noop();
                releaseStoreProtocol(p);
            } catch (ProtocolException e) {
                releaseStoreProtocol(p);
            } catch (Throwable th) {
                releaseStoreProtocol(p);
                throw th;
            }
            isConnected = super.isConnected();
        }
        return isConnected;
    }

    public synchronized void close() throws MessagingException {
        cleanup();
        closeAllFolders(true);
        emptyConnectionPool(true);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (!this.finalizeCleanClose) {
            synchronized (this.connectionFailedLock) {
                this.connectionFailed = true;
                this.forceClose = true;
            }
            this.closeFoldersOnStoreFailure = true;
        }
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private synchronized void cleanup() {
        boolean force;
        if (!super.isConnected()) {
            this.logger.fine("IMAPStore cleanup, not connected");
        } else {
            synchronized (this.connectionFailedLock) {
                force = this.forceClose;
                this.forceClose = $assertionsDisabled;
                this.connectionFailed = $assertionsDisabled;
            }
            if (this.logger.isLoggable(Level.FINE)) {
                this.logger.fine("IMAPStore cleanup, force " + force);
            }
            if (!force || this.closeFoldersOnStoreFailure) {
                closeAllFolders(force);
            }
            emptyConnectionPool(force);
            try {
                super.close();
            } catch (MessagingException e) {
            }
            this.logger.fine("IMAPStore cleanup done");
        }
    }

    private void closeAllFolders(boolean force) {
        boolean done;
        List<IMAPFolder> foldersCopy = null;
        while (true) {
            synchronized (this.pool) {
                if (this.pool.folders != null) {
                    done = $assertionsDisabled;
                    foldersCopy = this.pool.folders;
                    Vector unused = this.pool.folders = null;
                } else {
                    done = true;
                }
            }
            if (!done) {
                int fsize = foldersCopy.size();
                for (int i = 0; i < fsize; i++) {
                    IMAPFolder f = foldersCopy.get(i);
                    if (force) {
                        try {
                            this.logger.fine("force folder to close");
                            f.forceClose();
                        } catch (IllegalStateException | MessagingException e) {
                        }
                    } else {
                        this.logger.fine("close folder");
                        f.close($assertionsDisabled);
                    }
                }
            } else {
                return;
            }
        }
    }

    public synchronized Folder getDefaultFolder() throws MessagingException {
        checkConnected();
        return new DefaultFolder(this);
    }

    public synchronized Folder getFolder(String name2) throws MessagingException {
        checkConnected();
        return newIMAPFolder(name2, LispReader.TOKEN_ESCAPE_CHAR);
    }

    public synchronized Folder getFolder(URLName url) throws MessagingException {
        checkConnected();
        return newIMAPFolder(url.getFile(), LispReader.TOKEN_ESCAPE_CHAR);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v8, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.sun.mail.imap.IMAPFolder} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sun.mail.imap.IMAPFolder newIMAPFolder(java.lang.String r8, char r9, java.lang.Boolean r10) {
        /*
            r7 = this;
            r2 = 0
            java.lang.reflect.Constructor<?> r4 = r7.folderConstructor
            if (r4 == 0) goto L_0x0022
            r4 = 4
            java.lang.Object[] r3 = new java.lang.Object[r4]     // Catch:{ Exception -> 0x002a }
            r4 = 0
            r3[r4] = r8     // Catch:{ Exception -> 0x002a }
            r4 = 1
            java.lang.Character r5 = java.lang.Character.valueOf(r9)     // Catch:{ Exception -> 0x002a }
            r3[r4] = r5     // Catch:{ Exception -> 0x002a }
            r4 = 2
            r3[r4] = r7     // Catch:{ Exception -> 0x002a }
            r4 = 3
            r3[r4] = r10     // Catch:{ Exception -> 0x002a }
            java.lang.reflect.Constructor<?> r4 = r7.folderConstructor     // Catch:{ Exception -> 0x002a }
            java.lang.Object r4 = r4.newInstance(r3)     // Catch:{ Exception -> 0x002a }
            r0 = r4
            com.sun.mail.imap.IMAPFolder r0 = (com.sun.mail.imap.IMAPFolder) r0     // Catch:{ Exception -> 0x002a }
            r2 = r0
        L_0x0022:
            if (r2 != 0) goto L_0x0029
            com.sun.mail.imap.IMAPFolder r2 = new com.sun.mail.imap.IMAPFolder
            r2.<init>(r8, r9, r7, r10)
        L_0x0029:
            return r2
        L_0x002a:
            r1 = move-exception
            com.sun.mail.util.MailLogger r4 = r7.logger
            java.util.logging.Level r5 = java.util.logging.Level.FINE
            java.lang.String r6 = "exception creating IMAPFolder class"
            r4.log((java.util.logging.Level) r5, (java.lang.String) r6, (java.lang.Throwable) r1)
            goto L_0x0022
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.newIMAPFolder(java.lang.String, char, java.lang.Boolean):com.sun.mail.imap.IMAPFolder");
    }

    /* access modifiers changed from: protected */
    public IMAPFolder newIMAPFolder(String fullName, char separator) {
        return newIMAPFolder(fullName, separator, (Boolean) null);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.sun.mail.imap.IMAPFolder} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sun.mail.imap.IMAPFolder newIMAPFolder(com.sun.mail.imap.protocol.ListInfo r8) {
        /*
            r7 = this;
            r2 = 0
            java.lang.reflect.Constructor<?> r4 = r7.folderConstructorLI
            if (r4 == 0) goto L_0x0018
            r4 = 2
            java.lang.Object[] r3 = new java.lang.Object[r4]     // Catch:{ Exception -> 0x0020 }
            r4 = 0
            r3[r4] = r8     // Catch:{ Exception -> 0x0020 }
            r4 = 1
            r3[r4] = r7     // Catch:{ Exception -> 0x0020 }
            java.lang.reflect.Constructor<?> r4 = r7.folderConstructorLI     // Catch:{ Exception -> 0x0020 }
            java.lang.Object r4 = r4.newInstance(r3)     // Catch:{ Exception -> 0x0020 }
            r0 = r4
            com.sun.mail.imap.IMAPFolder r0 = (com.sun.mail.imap.IMAPFolder) r0     // Catch:{ Exception -> 0x0020 }
            r2 = r0
        L_0x0018:
            if (r2 != 0) goto L_0x001f
            com.sun.mail.imap.IMAPFolder r2 = new com.sun.mail.imap.IMAPFolder
            r2.<init>(r8, r7)
        L_0x001f:
            return r2
        L_0x0020:
            r1 = move-exception
            com.sun.mail.util.MailLogger r4 = r7.logger
            java.util.logging.Level r5 = java.util.logging.Level.FINE
            java.lang.String r6 = "exception creating IMAPFolder class LI"
            r4.log((java.util.logging.Level) r5, (java.lang.String) r6, (java.lang.Throwable) r1)
            goto L_0x0018
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.newIMAPFolder(com.sun.mail.imap.protocol.ListInfo):com.sun.mail.imap.IMAPFolder");
    }

    public Folder[] getPersonalNamespaces() throws MessagingException {
        Namespaces ns = getNamespaces();
        if (ns == null || ns.personal == null) {
            return super.getPersonalNamespaces();
        }
        return namespaceToFolders(ns.personal, (String) null);
    }

    public Folder[] getUserNamespaces(String user2) throws MessagingException {
        Namespaces ns = getNamespaces();
        if (ns == null || ns.otherUsers == null) {
            return super.getUserNamespaces(user2);
        }
        return namespaceToFolders(ns.otherUsers, user2);
    }

    public Folder[] getSharedNamespaces() throws MessagingException {
        Namespaces ns = getNamespaces();
        if (ns == null || ns.shared == null) {
            return super.getSharedNamespaces();
        }
        return namespaceToFolders(ns.shared, (String) null);
    }

    private synchronized Namespaces getNamespaces() throws MessagingException {
        checkConnected();
        IMAPProtocol p = null;
        if (this.namespaces == null) {
            try {
                p = getStoreProtocol();
                this.namespaces = p.namespace();
                releaseStoreProtocol(p);
            } catch (BadCommandException e) {
                releaseStoreProtocol(p);
            } catch (ConnectionException cex) {
                throw new StoreClosedException(this, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            } catch (Throwable th) {
                releaseStoreProtocol(p);
                throw th;
            }
        }
        return this.namespaces;
    }

    private Folder[] namespaceToFolders(Namespaces.Namespace[] ns, String user2) {
        boolean z;
        Folder[] fa = new Folder[ns.length];
        for (int i = 0; i < fa.length; i++) {
            String name2 = ns[i].prefix;
            if (user2 == null) {
                int len = name2.length();
                if (len > 0 && name2.charAt(len - 1) == ns[i].delimiter) {
                    name2 = name2.substring(0, len - 1);
                }
            } else {
                name2 = name2 + user2;
            }
            char c = ns[i].delimiter;
            if (user2 == null) {
                z = true;
            } else {
                z = false;
            }
            fa[i] = newIMAPFolder(name2, c, Boolean.valueOf(z));
        }
        return fa;
    }

    public synchronized Quota[] getQuota(String root) throws MessagingException {
        Quota[] qa;
        checkConnected();
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            qa = p.getQuotaRoot(root);
            releaseStoreProtocol(p);
        } catch (BadCommandException bex) {
            throw new MessagingException("QUOTA not supported", bex);
        } catch (ConnectionException cex) {
            throw new StoreClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        } catch (Throwable th) {
            releaseStoreProtocol(p);
            throw th;
        }
        return qa;
    }

    public synchronized void setQuota(Quota quota) throws MessagingException {
        checkConnected();
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            p.setQuota(quota);
            releaseStoreProtocol(p);
        } catch (BadCommandException bex) {
            throw new MessagingException("QUOTA not supported", bex);
        } catch (ConnectionException cex) {
            throw new StoreClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        } catch (Throwable th) {
            releaseStoreProtocol(p);
            throw th;
        }
    }

    private void checkConnected() {
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (!super.isConnected()) {
            throw new IllegalStateException("Not connected");
        }
    }

    public void handleResponse(Response r) {
        if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
            handleResponseCode(r);
        }
        if (r.isBYE()) {
            this.logger.fine("IMAPStore connection dead");
            synchronized (this.connectionFailedLock) {
                this.connectionFailed = true;
                if (r.isSynthetic()) {
                    this.forceClose = true;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:105:?, code lost:
        java.lang.Thread.currentThread().interrupt();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002e, code lost:
        if (0 == 0) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0030, code lost:
        r9 = r12.pool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0032, code lost:
        monitor-enter(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        com.sun.mail.imap.IMAPStore.ConnectionPool.access$1002(r12.pool, 0);
        com.sun.mail.imap.IMAPStore.ConnectionPool.access$1102(r12.pool, (com.sun.mail.imap.protocol.IMAPProtocol) null);
        r12.pool.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0044, code lost:
        monitor-exit(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0045, code lost:
        releaseStoreProtocol(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:?, code lost:
        r7 = r5.readIdleResponse();
        r9 = r12.pool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x009d, code lost:
        monitor-enter(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x009e, code lost:
        if (r7 == null) goto L_0x00a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00a4, code lost:
        if (r5.processIdleResponse(r7) != false) goto L_0x00df;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00a6, code lost:
        com.sun.mail.imap.IMAPStore.ConnectionPool.access$1002(r12.pool, 0);
        com.sun.mail.imap.IMAPStore.ConnectionPool.access$1102(r12.pool, (com.sun.mail.imap.protocol.IMAPProtocol) null);
        r12.pool.notifyAll();
        r4 = $assertionsDisabled;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00b8, code lost:
        monitor-exit(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:?, code lost:
        r3 = getMinIdleTime();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00bd, code lost:
        if (r3 <= 0) goto L_0x00c3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:?, code lost:
        java.lang.Thread.sleep((long) r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:?, code lost:
        monitor-exit(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x00e2, code lost:
        if (r12.enableImapEvents == false) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x00e8, code lost:
        if (r7.isUnTagged() == false) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x00ea, code lost:
        notifyStoreListeners(1000, r7.toString());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void idle() throws javax.mail.MessagingException {
        /*
            r12 = this;
            r5 = 0
            boolean r8 = $assertionsDisabled
            if (r8 != 0) goto L_0x0013
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool
            boolean r8 = java.lang.Thread.holdsLock(r8)
            if (r8 == 0) goto L_0x0013
            java.lang.AssertionError r8 = new java.lang.AssertionError
            r8.<init>()
            throw r8
        L_0x0013:
            monitor-enter(r12)
            r12.checkConnected()     // Catch:{ all -> 0x0049 }
            monitor-exit(r12)     // Catch:{ all -> 0x0049 }
            r4 = 0
            com.sun.mail.imap.IMAPStore$ConnectionPool r9 = r12.pool     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            monitor-enter(r9)     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r12.getStoreProtocol()     // Catch:{ all -> 0x005c }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x005c }
            int r8 = r8.idleState     // Catch:{ all -> 0x005c }
            if (r8 == 0) goto L_0x0087
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ InterruptedException -> 0x004c }
            r8.wait()     // Catch:{ InterruptedException -> 0x004c }
            monitor-exit(r9)     // Catch:{ all -> 0x005c }
            if (r4 == 0) goto L_0x0045
            com.sun.mail.imap.IMAPStore$ConnectionPool r9 = r12.pool
            monitor-enter(r9)
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x0084 }
            r10 = 0
            int unused = r8.idleState = r10     // Catch:{ all -> 0x0084 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x0084 }
            r10 = 0
            com.sun.mail.imap.protocol.IMAPProtocol unused = r8.idleProtocol = r10     // Catch:{ all -> 0x0084 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x0084 }
            r8.notifyAll()     // Catch:{ all -> 0x0084 }
            monitor-exit(r9)     // Catch:{ all -> 0x0084 }
        L_0x0045:
            r12.releaseStoreProtocol(r5)
        L_0x0048:
            return
        L_0x0049:
            r8 = move-exception
            monitor-exit(r12)     // Catch:{ all -> 0x0049 }
            throw r8
        L_0x004c:
            r2 = move-exception
            java.lang.Thread r8 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x005c }
            r8.interrupt()     // Catch:{ all -> 0x005c }
            javax.mail.MessagingException r8 = new javax.mail.MessagingException     // Catch:{ all -> 0x005c }
            java.lang.String r10 = "idle interrupted"
            r8.<init>(r10, r2)     // Catch:{ all -> 0x005c }
            throw r8     // Catch:{ all -> 0x005c }
        L_0x005c:
            r8 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x005c }
            throw r8     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
        L_0x005f:
            r0 = move-exception
            javax.mail.MessagingException r8 = new javax.mail.MessagingException     // Catch:{ all -> 0x0068 }
            java.lang.String r9 = "IDLE not supported"
            r8.<init>(r9, r0)     // Catch:{ all -> 0x0068 }
            throw r8     // Catch:{ all -> 0x0068 }
        L_0x0068:
            r8 = move-exception
            if (r4 == 0) goto L_0x0080
            com.sun.mail.imap.IMAPStore$ConnectionPool r9 = r12.pool
            monitor-enter(r9)
            com.sun.mail.imap.IMAPStore$ConnectionPool r10 = r12.pool     // Catch:{ all -> 0x0119 }
            r11 = 0
            int unused = r10.idleState = r11     // Catch:{ all -> 0x0119 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r10 = r12.pool     // Catch:{ all -> 0x0119 }
            r11 = 0
            com.sun.mail.imap.protocol.IMAPProtocol unused = r10.idleProtocol = r11     // Catch:{ all -> 0x0119 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r10 = r12.pool     // Catch:{ all -> 0x0119 }
            r10.notifyAll()     // Catch:{ all -> 0x0119 }
            monitor-exit(r9)     // Catch:{ all -> 0x0119 }
        L_0x0080:
            r12.releaseStoreProtocol(r5)
            throw r8
        L_0x0084:
            r8 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0084 }
            throw r8
        L_0x0087:
            r5.idleStart()     // Catch:{ all -> 0x005c }
            r4 = 1
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x005c }
            r10 = 1
            int unused = r8.idleState = r10     // Catch:{ all -> 0x005c }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x005c }
            com.sun.mail.imap.protocol.IMAPProtocol unused = r8.idleProtocol = r5     // Catch:{ all -> 0x005c }
            monitor-exit(r9)     // Catch:{ all -> 0x005c }
        L_0x0097:
            com.sun.mail.iap.Response r7 = r5.readIdleResponse()     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r9 = r12.pool     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            monitor-enter(r9)     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            if (r7 == 0) goto L_0x00a6
            boolean r8 = r5.processIdleResponse(r7)     // Catch:{ all -> 0x00ff }
            if (r8 != 0) goto L_0x00df
        L_0x00a6:
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x00ff }
            r10 = 0
            int unused = r8.idleState = r10     // Catch:{ all -> 0x00ff }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x00ff }
            r10 = 0
            com.sun.mail.imap.protocol.IMAPProtocol unused = r8.idleProtocol = r10     // Catch:{ all -> 0x00ff }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x00ff }
            r8.notifyAll()     // Catch:{ all -> 0x00ff }
            r4 = 0
            monitor-exit(r9)     // Catch:{ all -> 0x00ff }
            int r3 = r12.getMinIdleTime()     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            if (r3 <= 0) goto L_0x00c3
            long r8 = (long) r3
            java.lang.Thread.sleep(r8)     // Catch:{ InterruptedException -> 0x010d }
        L_0x00c3:
            if (r4 == 0) goto L_0x00da
            com.sun.mail.imap.IMAPStore$ConnectionPool r9 = r12.pool
            monitor-enter(r9)
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x0116 }
            r10 = 0
            int unused = r8.idleState = r10     // Catch:{ all -> 0x0116 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x0116 }
            r10 = 0
            com.sun.mail.imap.protocol.IMAPProtocol unused = r8.idleProtocol = r10     // Catch:{ all -> 0x0116 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r8 = r12.pool     // Catch:{ all -> 0x0116 }
            r8.notifyAll()     // Catch:{ all -> 0x0116 }
            monitor-exit(r9)     // Catch:{ all -> 0x0116 }
        L_0x00da:
            r12.releaseStoreProtocol(r5)
            goto L_0x0048
        L_0x00df:
            monitor-exit(r9)     // Catch:{ all -> 0x00ff }
            boolean r8 = r12.enableImapEvents     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            if (r8 == 0) goto L_0x0097
            boolean r8 = r7.isUnTagged()     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            if (r8 == 0) goto L_0x0097
            r8 = 1000(0x3e8, float:1.401E-42)
            java.lang.String r9 = r7.toString()     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            r12.notifyStoreListeners(r8, r9)     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            goto L_0x0097
        L_0x00f4:
            r1 = move-exception
            javax.mail.StoreClosedException r8 = new javax.mail.StoreClosedException     // Catch:{ all -> 0x0068 }
            java.lang.String r9 = r1.getMessage()     // Catch:{ all -> 0x0068 }
            r8.<init>(r12, r9)     // Catch:{ all -> 0x0068 }
            throw r8     // Catch:{ all -> 0x0068 }
        L_0x00ff:
            r8 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x00ff }
            throw r8     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
        L_0x0102:
            r6 = move-exception
            javax.mail.MessagingException r8 = new javax.mail.MessagingException     // Catch:{ all -> 0x0068 }
            java.lang.String r9 = r6.getMessage()     // Catch:{ all -> 0x0068 }
            r8.<init>(r9, r6)     // Catch:{ all -> 0x0068 }
            throw r8     // Catch:{ all -> 0x0068 }
        L_0x010d:
            r2 = move-exception
            java.lang.Thread r8 = java.lang.Thread.currentThread()     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            r8.interrupt()     // Catch:{ BadCommandException -> 0x005f, ConnectionException -> 0x00f4, ProtocolException -> 0x0102 }
            goto L_0x00c3
        L_0x0116:
            r8 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0116 }
            throw r8
        L_0x0119:
            r8 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0119 }
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.idle():void");
    }

    private void waitIfIdle() throws ProtocolException {
        if ($assertionsDisabled || Thread.holdsLock(this.pool)) {
            while (this.pool.idleState != 0) {
                if (this.pool.idleState == 1) {
                    this.pool.idleProtocol.idleAbort();
                    int unused = this.pool.idleState = 2;
                }
                try {
                    this.pool.wait();
                } catch (InterruptedException ex) {
                    throw new ProtocolException("Interrupted waitIfIdle", ex);
                }
            }
            return;
        }
        throw new AssertionError();
    }

    /* renamed from: id */
    public synchronized Map<String, String> mo12896id(Map<String, String> clientParams) throws MessagingException {
        Map<String, String> serverParams;
        checkConnected();
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            serverParams = p.mo13040id(clientParams);
            releaseStoreProtocol(p);
        } catch (BadCommandException bex) {
            throw new MessagingException("ID not supported", bex);
        } catch (ConnectionException cex) {
            throw new StoreClosedException(this, cex.getMessage());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        } catch (Throwable th) {
            releaseStoreProtocol(p);
            throw th;
        }
        return serverParams;
    }

    /* access modifiers changed from: package-private */
    public void handleResponseCode(Response r) {
        if (this.enableResponseEvents) {
            notifyStoreListeners(1000, r.toString());
        }
        String s = r.getRest();
        boolean isAlert = $assertionsDisabled;
        if (s.startsWith("[")) {
            int i = s.indexOf(93);
            if (i > 0 && s.substring(0, i + 1).equalsIgnoreCase("[ALERT]")) {
                isAlert = true;
            }
            s = s.substring(i + 1).trim();
        }
        if (isAlert) {
            notifyStoreListeners(1, s);
        } else if (r.isUnTagged() && s.length() > 0) {
            notifyStoreListeners(2, s);
        }
    }

    private String traceUser(String user2) {
        return this.debugusername ? user2 : "<user name suppressed>";
    }

    private String tracePassword(String password2) {
        if (this.debugpassword) {
            return password2;
        }
        return password2 == null ? "<null>" : "<non-null>";
    }
}
