package com.sun.mail.pop3;

import com.sun.mail.util.MailConnectException;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketConnectException;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class POP3Store extends Store {
    volatile boolean cacheWriteTo;
    private Map<String, String> capabilities;
    private int defaultPort;
    volatile boolean disableTop;
    volatile File fileCacheDir;
    volatile boolean finalizeCleanClose;
    volatile boolean forgetTopHeaders;
    private String host;
    private boolean isSSL;
    volatile boolean keepMessageContent;
    private MailLogger logger;
    volatile Constructor<?> messageConstructor;
    private String name;
    private String passwd;
    private Protocol port;
    private int portNum;
    private POP3Folder portOwner;
    private boolean requireStartTLS;
    volatile boolean rsetBeforeQuit;
    volatile boolean supportsUidl;
    volatile boolean useFileCache;
    private boolean useStartTLS;
    private String user;
    private boolean usingSSL;

    public POP3Store(Session session, URLName url) {
        this(session, url, "pop3", false);
    }

    public POP3Store(Session session, URLName url, String name2, boolean isSSL2) {
        super(session, url);
        Class<?> messageClass;
        this.name = "pop3";
        this.defaultPort = 110;
        this.isSSL = false;
        this.port = null;
        this.portOwner = null;
        this.host = null;
        this.portNum = -1;
        this.user = null;
        this.passwd = null;
        this.useStartTLS = false;
        this.requireStartTLS = false;
        this.usingSSL = false;
        this.messageConstructor = null;
        this.rsetBeforeQuit = false;
        this.disableTop = false;
        this.forgetTopHeaders = false;
        this.supportsUidl = true;
        this.cacheWriteTo = false;
        this.useFileCache = false;
        this.fileCacheDir = null;
        this.keepMessageContent = false;
        this.finalizeCleanClose = false;
        name2 = url != null ? url.getProtocol() : name2;
        this.name = name2;
        this.logger = new MailLogger(getClass(), "DEBUG POP3", session.getDebug(), session.getDebugOut());
        isSSL2 = !isSSL2 ? PropUtil.getBooleanProperty(session.getProperties(), "mail." + name2 + ".ssl.enable", false) : isSSL2;
        if (isSSL2) {
            this.defaultPort = 995;
        } else {
            this.defaultPort = 110;
        }
        this.isSSL = isSSL2;
        this.rsetBeforeQuit = getBoolProp("rsetbeforequit");
        this.disableTop = getBoolProp("disabletop");
        this.forgetTopHeaders = getBoolProp("forgettopheaders");
        this.cacheWriteTo = getBoolProp("cachewriteto");
        this.useFileCache = getBoolProp("filecache.enable");
        String dir = session.getProperty("mail." + name2 + ".filecache.dir");
        if (dir != null && this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("mail." + name2 + ".filecache.dir: " + dir);
        }
        if (dir != null) {
            this.fileCacheDir = new File(dir);
        }
        this.keepMessageContent = getBoolProp("keepmessagecontent");
        this.useStartTLS = getBoolProp("starttls.enable");
        this.requireStartTLS = getBoolProp("starttls.required");
        this.finalizeCleanClose = getBoolProp("finalizecleanclose");
        String s = session.getProperty("mail." + name2 + ".message.class");
        if (s != null) {
            this.logger.log(Level.CONFIG, "message class: {0}", (Object) s);
            try {
                try {
                    messageClass = Class.forName(s, false, getClass().getClassLoader());
                } catch (ClassNotFoundException e) {
                    messageClass = Class.forName(s);
                }
                this.messageConstructor = messageClass.getConstructor(new Class[]{Folder.class, Integer.TYPE});
            } catch (Exception ex) {
                this.logger.log(Level.CONFIG, "failed to load message class", (Throwable) ex);
            }
        }
    }

    private final synchronized boolean getBoolProp(String prop) {
        boolean val;
        String prop2 = "mail." + this.name + "." + prop;
        val = PropUtil.getBooleanProperty(this.session.getProperties(), prop2, false);
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(prop2 + ": " + val);
        }
        return val;
    }

    /* access modifiers changed from: package-private */
    public synchronized Session getSession() {
        return this.session;
    }

    /* access modifiers changed from: protected */
    public synchronized boolean protocolConnect(String host2, int portNum2, String user2, String passwd2) throws MessagingException {
        boolean z;
        if (host2 == null || passwd2 == null || user2 == null) {
            z = false;
        } else {
            if (portNum2 == -1) {
                portNum2 = PropUtil.getIntProperty(this.session.getProperties(), "mail." + this.name + ".port", -1);
            }
            if (portNum2 == -1) {
                portNum2 = this.defaultPort;
            }
            this.host = host2;
            this.portNum = portNum2;
            this.user = user2;
            this.passwd = passwd2;
            try {
                this.port = getPort((POP3Folder) null);
                z = true;
            } catch (EOFException eex) {
                throw new AuthenticationFailedException(eex.getMessage());
            } catch (SocketConnectException scex) {
                throw new MailConnectException(scex);
            } catch (IOException ioex) {
                throw new MessagingException("Connect failed", ioex);
            }
        }
        return z;
    }

    public synchronized boolean isConnected() {
        boolean z = false;
        synchronized (this) {
            if (super.isConnected()) {
                try {
                    if (this.port == null) {
                        this.port = getPort((POP3Folder) null);
                    } else if (!this.port.noop()) {
                        throw new IOException("NOOP failed");
                    }
                    z = true;
                } catch (IOException e) {
                    try {
                        super.close();
                    } catch (MessagingException e2) {
                    }
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public synchronized Protocol getPort(POP3Folder owner) throws IOException {
        Protocol p;
        if (this.port == null || this.portOwner != null) {
            p = new Protocol(this.host, this.portNum, this.logger, this.session.getProperties(), "mail." + this.name, this.isSSL);
            if (this.useStartTLS || this.requireStartTLS) {
                if (p.hasCapability("STLS")) {
                    if (p.stls()) {
                        p.setCapabilities(p.capa());
                    } else if (this.requireStartTLS) {
                        this.logger.fine("STLS required but failed");
                        throw cleanupAndThrow(p, new EOFException("STLS required but failed"));
                    }
                } else if (this.requireStartTLS) {
                    this.logger.fine("STLS required but not supported");
                    throw cleanupAndThrow(p, new EOFException("STLS required but not supported"));
                }
            }
            this.capabilities = p.getCapabilities();
            this.usingSSL = p.isSSL();
            if (!this.disableTop && this.capabilities != null && !this.capabilities.containsKey("TOP")) {
                this.disableTop = true;
                this.logger.fine("server doesn't support TOP, disabling it");
            }
            this.supportsUidl = this.capabilities == null || this.capabilities.containsKey("UIDL");
            try {
                if (!authenticate(p, this.user, this.passwd)) {
                    throw cleanupAndThrow(p, new EOFException("login failed"));
                }
                if (this.port == null && owner != null) {
                    this.port = p;
                    this.portOwner = owner;
                }
                if (this.portOwner == null) {
                    this.portOwner = owner;
                }
            } catch (EOFException ex) {
                throw cleanupAndThrow(p, ex);
            } catch (Exception ex2) {
                throw cleanupAndThrow(p, new EOFException(ex2.getMessage()));
            }
        } else {
            this.portOwner = owner;
            p = this.port;
        }
        return p;
    }

    private static IOException cleanupAndThrow(Protocol p, IOException ife) {
        try {
            p.quit();
        } catch (Throwable thr) {
            if (isRecoverable(thr)) {
                ife.addSuppressed(thr);
            } else {
                thr.addSuppressed(ife);
                if (thr instanceof Error) {
                    throw ((Error) thr);
                } else if (thr instanceof RuntimeException) {
                    throw ((RuntimeException) thr);
                } else {
                    throw new RuntimeException("unexpected exception", thr);
                }
            }
        }
        return ife;
    }

    private boolean authenticate(Protocol p, String user2, String passwd2) throws MessagingException {
        String mechs = this.session.getProperty("mail." + this.name + ".auth.mechanisms");
        boolean usingDefaultMechs = false;
        if (mechs == null) {
            mechs = p.getDefaultMechanisms();
            usingDefaultMechs = true;
        }
        String authzid = this.session.getProperty("mail." + this.name + ".sasl.authorizationid");
        if (authzid == null) {
            authzid = user2;
        }
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("Attempt to authenticate using mechanisms: " + mechs);
        }
        StringTokenizer st = new StringTokenizer(mechs);
        while (st.hasMoreTokens()) {
            String m = st.nextToken().toUpperCase(Locale.ENGLISH);
            if (!p.supportsMechanism(m)) {
                this.logger.log(Level.FINE, "no authenticator for mechanism {0}", (Object) m);
            } else if (!p.supportsAuthentication(m)) {
                this.logger.log(Level.FINE, "mechanism {0} not supported by server", (Object) m);
            } else {
                if (usingDefaultMechs) {
                    String dprop = "mail." + this.name + ".auth." + m.toLowerCase(Locale.ENGLISH) + ".disable";
                    if (PropUtil.getBooleanProperty(this.session.getProperties(), dprop, !p.isMechanismEnabled(m))) {
                        if (this.logger.isLoggable(Level.FINE)) {
                            this.logger.fine("mechanism " + m + " disabled by property: " + dprop);
                        }
                    }
                }
                this.logger.log(Level.FINE, "Using mechanism {0}", (Object) m);
                String msg = p.authenticate(m, this.host, authzid, user2, passwd2);
                if (msg == null) {
                    return true;
                }
                throw new AuthenticationFailedException(msg);
            }
        }
        throw new AuthenticationFailedException("No authentication mechanisms supported by both server and client");
    }

    private static boolean isRecoverable(Throwable t) {
        return (t instanceof Exception) || (t instanceof LinkageError);
    }

    /* access modifiers changed from: package-private */
    public synchronized void closePort(POP3Folder owner) {
        if (this.portOwner == owner) {
            this.port = null;
            this.portOwner = null;
        }
    }

    public synchronized void close() throws MessagingException {
        close(false);
    }

    /* access modifiers changed from: package-private */
    public synchronized void close(boolean force) throws MessagingException {
        try {
            if (this.port != null) {
                if (force) {
                    this.port.close();
                } else {
                    this.port.quit();
                }
            }
            this.port = null;
            super.close();
        } catch (IOException e) {
            this.port = null;
            super.close();
        } catch (Throwable th) {
            this.port = null;
            super.close();
            throw th;
        }
        return;
    }

    public Folder getDefaultFolder() throws MessagingException {
        checkConnected();
        return new DefaultFolder(this);
    }

    public Folder getFolder(String name2) throws MessagingException {
        checkConnected();
        return new POP3Folder(this, name2);
    }

    public Folder getFolder(URLName url) throws MessagingException {
        checkConnected();
        return new POP3Folder(this, url.getFile());
    }

    public Map<String, String> capabilities() throws MessagingException {
        Map<String, String> c;
        synchronized (this) {
            c = this.capabilities;
        }
        if (c != null) {
            return Collections.unmodifiableMap(c);
        }
        return Collections.emptyMap();
    }

    public synchronized boolean isSSL() {
        return this.usingSSL;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.port != null) {
                close(!this.finalizeCleanClose);
            }
        } finally {
            super.finalize();
        }
    }

    private void checkConnected() throws MessagingException {
        if (!super.isConnected()) {
            throw new MessagingException("Not connected");
        }
    }
}
