package com.sun.mail.smtp;

import androidx.appcompat.widget.ActivityChooserView;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.Ev3Constants;
import com.sun.mail.auth.Ntlm;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.MailConnectException;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketConnectException;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import gnu.bytecode.Access;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;
import javax.net.ssl.SSLSocket;

public class SMTPTransport extends Transport {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final byte[] CRLF = {13, 10};
    private static final String UNKNOWN = "UNKNOWN";
    private static final String[] UNKNOWN_SA = new String[0];
    private static char[] hexchar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', Access.CLASS_CONTEXT, 'D', 'E', Access.FIELD_CONTEXT};
    private static final String[] ignoreList = {"Bcc", "Content-Length"};
    private Address[] addresses;
    private boolean allowutf8;
    private Map<String, Authenticator> authenticators;
    private String authorizationID;
    private int chunkSize;
    private SMTPOutputStream dataStream;
    private boolean debugpassword;
    private boolean debugusername;
    private String defaultAuthenticationMechanisms;
    private int defaultPort;
    private boolean enableSASL;
    private MessagingException exception;
    private Hashtable<String, String> extMap;
    private String host;
    private Address[] invalidAddr;
    private boolean isSSL;
    private int lastReturnCode;
    /* access modifiers changed from: private */
    public String lastServerResponse;
    private LineInputStream lineInputStream;
    private String localHostName;
    /* access modifiers changed from: private */
    public MailLogger logger;
    private MimeMessage message;
    /* access modifiers changed from: private */
    public String name;
    /* access modifiers changed from: private */
    public boolean noauthdebug;
    private boolean noopStrict;
    private boolean notificationDone;
    private String ntlmDomain;
    private boolean quitOnSessionReject;
    private boolean quitWait;
    private boolean reportSuccess;
    private boolean requireStartTLS;
    private SaslAuthenticator saslAuthenticator;
    private String[] saslMechanisms;
    private String saslRealm;
    private boolean sendPartiallyFailed;
    private BufferedInputStream serverInput;
    private OutputStream serverOutput;
    private Socket serverSocket;
    private TraceInputStream traceInput;
    private MailLogger traceLogger;
    private TraceOutputStream traceOutput;
    private boolean useCanonicalHostName;
    private boolean useRset;
    private boolean useStartTLS;
    private Address[] validSentAddr;
    private Address[] validUnsentAddr;

    static {
        boolean z;
        if (!SMTPTransport.class.desiredAssertionStatus()) {
            z = true;
        } else {
            z = false;
        }
        $assertionsDisabled = z;
    }

    public SMTPTransport(Session session, URLName urlname) {
        this(session, urlname, "smtp", $assertionsDisabled);
    }

    protected SMTPTransport(Session session, URLName urlname, String name2, boolean isSSL2) {
        super(session, urlname);
        this.name = "smtp";
        this.defaultPort = 25;
        this.isSSL = $assertionsDisabled;
        this.sendPartiallyFailed = $assertionsDisabled;
        this.authenticators = new HashMap();
        this.quitWait = $assertionsDisabled;
        this.quitOnSessionReject = $assertionsDisabled;
        this.saslRealm = UNKNOWN;
        this.authorizationID = UNKNOWN;
        this.enableSASL = $assertionsDisabled;
        this.useCanonicalHostName = $assertionsDisabled;
        this.saslMechanisms = UNKNOWN_SA;
        this.ntlmDomain = UNKNOWN;
        this.noopStrict = true;
        this.noauthdebug = true;
        Properties props = session.getProperties();
        this.logger = new MailLogger(getClass(), "DEBUG SMTP", session.getDebug(), session.getDebugOut());
        this.traceLogger = this.logger.getSubLogger("protocol", (String) null);
        this.noauthdebug = !PropUtil.getBooleanProperty(props, "mail.debug.auth", $assertionsDisabled) ? true : $assertionsDisabled;
        this.debugusername = PropUtil.getBooleanProperty(props, "mail.debug.auth.username", true);
        this.debugpassword = PropUtil.getBooleanProperty(props, "mail.debug.auth.password", $assertionsDisabled);
        name2 = urlname != null ? urlname.getProtocol() : name2;
        this.name = name2;
        isSSL2 = !isSSL2 ? PropUtil.getBooleanProperty(props, "mail." + name2 + ".ssl.enable", $assertionsDisabled) : isSSL2;
        if (isSSL2) {
            this.defaultPort = 465;
        } else {
            this.defaultPort = 25;
        }
        this.isSSL = isSSL2;
        this.quitWait = PropUtil.getBooleanProperty(props, "mail." + name2 + ".quitwait", true);
        this.quitOnSessionReject = PropUtil.getBooleanProperty(props, "mail." + name2 + ".quitonsessionreject", $assertionsDisabled);
        this.reportSuccess = PropUtil.getBooleanProperty(props, "mail." + name2 + ".reportsuccess", $assertionsDisabled);
        this.useStartTLS = PropUtil.getBooleanProperty(props, "mail." + name2 + ".starttls.enable", $assertionsDisabled);
        this.requireStartTLS = PropUtil.getBooleanProperty(props, "mail." + name2 + ".starttls.required", $assertionsDisabled);
        this.useRset = PropUtil.getBooleanProperty(props, "mail." + name2 + ".userset", $assertionsDisabled);
        this.noopStrict = PropUtil.getBooleanProperty(props, "mail." + name2 + ".noop.strict", true);
        this.enableSASL = PropUtil.getBooleanProperty(props, "mail." + name2 + ".sasl.enable", $assertionsDisabled);
        if (this.enableSASL) {
            this.logger.config("enable SASL");
        }
        this.useCanonicalHostName = PropUtil.getBooleanProperty(props, "mail." + name2 + ".sasl.usecanonicalhostname", $assertionsDisabled);
        if (this.useCanonicalHostName) {
            this.logger.config("use canonical host name");
        }
        this.allowutf8 = PropUtil.getBooleanProperty(props, "mail.mime.allowutf8", $assertionsDisabled);
        if (this.allowutf8) {
            this.logger.config("allow UTF-8");
        }
        this.chunkSize = PropUtil.getIntProperty(props, "mail." + name2 + ".chunksize", -1);
        if (this.chunkSize > 0 && this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("chunk size " + this.chunkSize);
        }
        Authenticator[] a = {new LoginAuthenticator(), new PlainAuthenticator(), new DigestMD5Authenticator(), new NtlmAuthenticator(), new OAuth2Authenticator()};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            this.authenticators.put(a[i].getMechanism(), a[i]);
            sb.append(a[i].getMechanism()).append(' ');
        }
        this.defaultAuthenticationMechanisms = sb.toString();
    }

    public synchronized String getLocalHost() {
        if (this.localHostName == null || this.localHostName.length() <= 0) {
            this.localHostName = this.session.getProperty("mail." + this.name + ".localhost");
        }
        if (this.localHostName == null || this.localHostName.length() <= 0) {
            this.localHostName = this.session.getProperty("mail." + this.name + ".localaddress");
        }
        try {
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                InetAddress localHost = InetAddress.getLocalHost();
                this.localHostName = localHost.getCanonicalHostName();
                if (this.localHostName == null) {
                    this.localHostName = "[" + localHost.getHostAddress() + "]";
                }
            }
        } catch (UnknownHostException e) {
        }
        if ((this.localHostName == null || this.localHostName.length() <= 0) && this.serverSocket != null && this.serverSocket.isBound()) {
            InetAddress localHost2 = this.serverSocket.getLocalAddress();
            this.localHostName = localHost2.getCanonicalHostName();
            if (this.localHostName == null) {
                this.localHostName = "[" + localHost2.getHostAddress() + "]";
            }
        }
        return this.localHostName;
    }

    public synchronized void setLocalHost(String localhost) {
        this.localHostName = localhost;
    }

    public synchronized void connect(Socket socket) throws MessagingException {
        this.serverSocket = socket;
        super.connect();
    }

    public synchronized String getAuthorizationId() {
        if (this.authorizationID == UNKNOWN) {
            this.authorizationID = this.session.getProperty("mail." + this.name + ".sasl.authorizationid");
        }
        return this.authorizationID;
    }

    public synchronized void setAuthorizationID(String authzid) {
        this.authorizationID = authzid;
    }

    public synchronized boolean getSASLEnabled() {
        return this.enableSASL;
    }

    public synchronized void setSASLEnabled(boolean enableSASL2) {
        this.enableSASL = enableSASL2;
    }

    public synchronized String getSASLRealm() {
        if (this.saslRealm == UNKNOWN) {
            this.saslRealm = this.session.getProperty("mail." + this.name + ".sasl.realm");
            if (this.saslRealm == null) {
                this.saslRealm = this.session.getProperty("mail." + this.name + ".saslrealm");
            }
        }
        return this.saslRealm;
    }

    public synchronized void setSASLRealm(String saslRealm2) {
        this.saslRealm = saslRealm2;
    }

    public synchronized boolean getUseCanonicalHostName() {
        return this.useCanonicalHostName;
    }

    public synchronized void setUseCanonicalHostName(boolean useCanonicalHostName2) {
        this.useCanonicalHostName = useCanonicalHostName2;
    }

    public synchronized String[] getSASLMechanisms() {
        String[] strArr;
        if (this.saslMechanisms == UNKNOWN_SA) {
            List<String> v = new ArrayList<>(5);
            String s = this.session.getProperty("mail." + this.name + ".sasl.mechanisms");
            if (s != null && s.length() > 0) {
                if (this.logger.isLoggable(Level.FINE)) {
                    this.logger.fine("SASL mechanisms allowed: " + s);
                }
                StringTokenizer st = new StringTokenizer(s, " ,");
                while (st.hasMoreTokens()) {
                    String m = st.nextToken();
                    if (m.length() > 0) {
                        v.add(m);
                    }
                }
            }
            this.saslMechanisms = new String[v.size()];
            v.toArray(this.saslMechanisms);
        }
        if (this.saslMechanisms == null) {
            strArr = null;
        } else {
            strArr = (String[]) this.saslMechanisms.clone();
        }
        return strArr;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.String[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setSASLMechanisms(java.lang.String[] r3) {
        /*
            r2 = this;
            monitor-enter(r2)
            if (r3 == 0) goto L_0x000b
            java.lang.Object r1 = r3.clone()     // Catch:{ all -> 0x000f }
            r0 = r1
            java.lang.String[] r0 = (java.lang.String[]) r0     // Catch:{ all -> 0x000f }
            r3 = r0
        L_0x000b:
            r2.saslMechanisms = r3     // Catch:{ all -> 0x000f }
            monitor-exit(r2)
            return
        L_0x000f:
            r1 = move-exception
            monitor-exit(r2)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.setSASLMechanisms(java.lang.String[]):void");
    }

    public synchronized String getNTLMDomain() {
        if (this.ntlmDomain == UNKNOWN) {
            this.ntlmDomain = this.session.getProperty("mail." + this.name + ".auth.ntlm.domain");
        }
        return this.ntlmDomain;
    }

    public synchronized void setNTLMDomain(String ntlmDomain2) {
        this.ntlmDomain = ntlmDomain2;
    }

    public synchronized boolean getReportSuccess() {
        return this.reportSuccess;
    }

    public synchronized void setReportSuccess(boolean reportSuccess2) {
        this.reportSuccess = reportSuccess2;
    }

    public synchronized boolean getStartTLS() {
        return this.useStartTLS;
    }

    public synchronized void setStartTLS(boolean useStartTLS2) {
        this.useStartTLS = useStartTLS2;
    }

    public synchronized boolean getRequireStartTLS() {
        return this.requireStartTLS;
    }

    public synchronized void setRequireStartTLS(boolean requireStartTLS2) {
        this.requireStartTLS = requireStartTLS2;
    }

    public synchronized boolean isSSL() {
        return this.serverSocket instanceof SSLSocket;
    }

    public synchronized boolean getUseRset() {
        return this.useRset;
    }

    public synchronized void setUseRset(boolean useRset2) {
        this.useRset = useRset2;
    }

    public synchronized boolean getNoopStrict() {
        return this.noopStrict;
    }

    public synchronized void setNoopStrict(boolean noopStrict2) {
        this.noopStrict = noopStrict2;
    }

    public synchronized String getLastServerResponse() {
        return this.lastServerResponse;
    }

    public synchronized int getLastReturnCode() {
        return this.lastReturnCode;
    }

    /* access modifiers changed from: protected */
    public synchronized boolean protocolConnect(String host2, int port, String user, String password) throws MessagingException {
        boolean connected = $assertionsDisabled;
        synchronized (this) {
            Properties props = this.session.getProperties();
            boolean useAuth = PropUtil.getBooleanProperty(props, "mail." + this.name + ".auth", $assertionsDisabled);
            if (!useAuth || !(user == null || password == null)) {
                boolean useEhlo = PropUtil.getBooleanProperty(props, "mail." + this.name + ".ehlo", true);
                if (this.logger.isLoggable(Level.FINE)) {
                    this.logger.fine("useEhlo " + useEhlo + ", useAuth " + useAuth);
                }
                if (port == -1) {
                    port = PropUtil.getIntProperty(props, "mail." + this.name + ".port", -1);
                }
                if (port == -1) {
                    port = this.defaultPort;
                }
                if (host2 == null || host2.length() == 0) {
                    host2 = "localhost";
                }
                try {
                    if (this.serverSocket != null) {
                        openServer();
                    } else {
                        openServer(host2, port);
                    }
                    boolean succeed = $assertionsDisabled;
                    if (useEhlo) {
                        succeed = ehlo(getLocalHost());
                    }
                    if (!succeed) {
                        helo(getLocalHost());
                    }
                    if (this.useStartTLS || this.requireStartTLS) {
                        if (this.serverSocket instanceof SSLSocket) {
                            this.logger.fine("STARTTLS requested but already using SSL");
                        } else if (supportsExtension("STARTTLS")) {
                            startTLS();
                            ehlo(getLocalHost());
                        } else if (this.requireStartTLS) {
                            this.logger.fine("STARTTLS required but not supported");
                            throw new MessagingException("STARTTLS is required but host does not support STARTTLS");
                        }
                    }
                    if (this.allowutf8 && !supportsExtension("SMTPUTF8")) {
                        this.logger.log(Level.INFO, "mail.mime.allowutf8 set but server doesn't advertise SMTPUTF8 support");
                    }
                    if ((useAuth || !(user == null || password == null)) && (supportsExtension("AUTH") || supportsExtension("AUTH=LOGIN"))) {
                        if (this.logger.isLoggable(Level.FINE)) {
                            this.logger.fine("protocolConnect login, host=" + host2 + ", user=" + traceUser(user) + ", password=" + tracePassword(password));
                        }
                        connected = authenticate(user, password);
                        if (!connected) {
                            try {
                                closeConnection();
                            } catch (MessagingException e) {
                            }
                        }
                    } else {
                        if (1 == 0) {
                            try {
                                closeConnection();
                            } catch (MessagingException e2) {
                            }
                        }
                        connected = true;
                    }
                } catch (Throwable th) {
                    if (0 == 0) {
                        try {
                            closeConnection();
                        } catch (MessagingException e3) {
                        }
                    }
                    throw th;
                }
            } else if (this.logger.isLoggable(Level.FINE)) {
                this.logger.fine("need username and password for authentication");
                this.logger.fine("protocolConnect returning false, host=" + host2 + ", user=" + traceUser(user) + ", password=" + tracePassword(password));
            }
        }
        return connected;
    }

    private boolean authenticate(String user, String passwd) throws MessagingException {
        String mechs = this.session.getProperty("mail." + this.name + ".auth.mechanisms");
        if (mechs == null) {
            mechs = this.defaultAuthenticationMechanisms;
        }
        String authzid = getAuthorizationId();
        if (authzid == null) {
            authzid = user;
        }
        if (this.enableSASL) {
            this.logger.fine("Authenticate with SASL");
            try {
                if (sasllogin(getSASLMechanisms(), getSASLRealm(), authzid, user, passwd)) {
                    return true;
                }
                this.logger.fine("SASL authentication failed");
                return $assertionsDisabled;
            } catch (UnsupportedOperationException ex) {
                this.logger.log(Level.FINE, "SASL support failed", (Throwable) ex);
            }
        }
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("Attempt to authenticate using mechanisms: " + mechs);
        }
        StringTokenizer st = new StringTokenizer(mechs);
        while (st.hasMoreTokens()) {
            String m = st.nextToken().toUpperCase(Locale.ENGLISH);
            Authenticator a = this.authenticators.get(m);
            if (a == null) {
                this.logger.log(Level.FINE, "no authenticator for mechanism {0}", (Object) m);
            } else if (!supportsAuthentication(m)) {
                this.logger.log(Level.FINE, "mechanism {0} not supported by server", (Object) m);
            } else {
                if (mechs == this.defaultAuthenticationMechanisms) {
                    String dprop = "mail." + this.name + ".auth." + m.toLowerCase(Locale.ENGLISH) + ".disable";
                    if (PropUtil.getBooleanProperty(this.session.getProperties(), dprop, !a.enabled() ? true : $assertionsDisabled)) {
                        if (this.logger.isLoggable(Level.FINE)) {
                            this.logger.fine("mechanism " + m + " disabled by property: " + dprop);
                        }
                    }
                }
                this.logger.log(Level.FINE, "Using mechanism {0}", (Object) m);
                return a.authenticate(this.host, authzid, user, passwd);
            }
        }
        throw new AuthenticationFailedException("No authentication mechanisms supported by both server and client");
    }

    private abstract class Authenticator {
        static final /* synthetic */ boolean $assertionsDisabled = (!SMTPTransport.class.desiredAssertionStatus() ? true : SMTPTransport.$assertionsDisabled);
        private final boolean enabled;
        private final String mech;
        protected int resp;

        /* access modifiers changed from: package-private */
        public abstract void doAuth(String str, String str2, String str3, String str4) throws MessagingException, IOException;

        Authenticator(SMTPTransport sMTPTransport, String mech2) {
            this(mech2, true);
        }

        Authenticator(String mech2, boolean enabled2) {
            this.mech = mech2.toUpperCase(Locale.ENGLISH);
            this.enabled = enabled2;
        }

        /* access modifiers changed from: package-private */
        public String getMechanism() {
            return this.mech;
        }

        /* access modifiers changed from: package-private */
        public boolean enabled() {
            return this.enabled;
        }

        /* access modifiers changed from: package-private */
        public boolean authenticate(String host, String authzid, String user, String passwd) throws MessagingException {
            String str;
            try {
                String ir = getInitialResponse(host, authzid, user, passwd);
                if (SMTPTransport.this.noauthdebug && SMTPTransport.this.isTracing()) {
                    SMTPTransport.this.logger.fine("AUTH " + this.mech + " command trace suppressed");
                    SMTPTransport.this.suspendTracing();
                }
                if (ir != null) {
                    SMTPTransport sMTPTransport = SMTPTransport.this;
                    StringBuilder append = new StringBuilder().append("AUTH ").append(this.mech).append(" ");
                    if (ir.length() == 0) {
                        str = "=";
                    } else {
                        str = ir;
                    }
                    this.resp = sMTPTransport.simpleCommand(append.append(str).toString());
                } else {
                    this.resp = SMTPTransport.this.simpleCommand("AUTH " + this.mech);
                }
                if (this.resp == 530) {
                    SMTPTransport.this.startTLS();
                    if (ir != null) {
                        this.resp = SMTPTransport.this.simpleCommand("AUTH " + this.mech + " " + ir);
                    } else {
                        this.resp = SMTPTransport.this.simpleCommand("AUTH " + this.mech);
                    }
                }
                if (this.resp == 334) {
                    doAuth(host, authzid, user, passwd);
                }
                if (SMTPTransport.this.noauthdebug && SMTPTransport.this.isTracing()) {
                    SMTPTransport.this.logger.fine("AUTH " + this.mech + " " + (this.resp == 235 ? "succeeded" : "failed"));
                }
                SMTPTransport.this.resumeTracing();
                if (this.resp == 235) {
                    return true;
                }
                SMTPTransport.this.closeConnection();
                if (0 != 0) {
                    if (0 instanceof Error) {
                        throw ((Error) null);
                    } else if (0 instanceof Exception) {
                        throw new AuthenticationFailedException(SMTPTransport.this.getLastServerResponse(), (Exception) null);
                    } else if (!$assertionsDisabled) {
                        throw new AssertionError("unknown Throwable");
                    }
                }
                throw new AuthenticationFailedException(SMTPTransport.this.getLastServerResponse());
            } catch (IOException ex) {
                SMTPTransport.this.logger.log(Level.FINE, "AUTH " + this.mech + " failed", (Throwable) ex);
                if (SMTPTransport.this.noauthdebug && SMTPTransport.this.isTracing()) {
                    SMTPTransport.this.logger.fine("AUTH " + this.mech + " " + (this.resp == 235 ? "succeeded" : "failed"));
                }
                SMTPTransport.this.resumeTracing();
                if (this.resp == 235) {
                    return true;
                }
                SMTPTransport.this.closeConnection();
                if (0 != 0) {
                    if (0 instanceof Error) {
                        throw ((Error) null);
                    } else if (0 instanceof Exception) {
                        throw new AuthenticationFailedException(SMTPTransport.this.getLastServerResponse(), (Exception) null);
                    } else if (!$assertionsDisabled) {
                        throw new AssertionError("unknown Throwable");
                    }
                }
                throw new AuthenticationFailedException(SMTPTransport.this.getLastServerResponse());
            } catch (Throwable th) {
                Throwable th2 = th;
                if (SMTPTransport.this.noauthdebug && SMTPTransport.this.isTracing()) {
                    SMTPTransport.this.logger.fine("AUTH " + this.mech + " " + (this.resp == 235 ? "succeeded" : "failed"));
                }
                SMTPTransport.this.resumeTracing();
                if (this.resp != 235) {
                    SMTPTransport.this.closeConnection();
                    if (0 != 0) {
                        if (0 instanceof Error) {
                            throw ((Error) null);
                        } else if (0 instanceof Exception) {
                            throw new AuthenticationFailedException(SMTPTransport.this.getLastServerResponse(), (Exception) null);
                        } else if (!$assertionsDisabled) {
                            throw new AssertionError("unknown Throwable");
                        }
                    }
                    throw new AuthenticationFailedException(SMTPTransport.this.getLastServerResponse());
                }
                throw th2;
            }
        }

        /* access modifiers changed from: package-private */
        public String getInitialResponse(String host, String authzid, String user, String passwd) throws MessagingException, IOException {
            return null;
        }
    }

    private class LoginAuthenticator extends Authenticator {
        LoginAuthenticator() {
            super(SMTPTransport.this, "LOGIN");
        }

        /* access modifiers changed from: package-private */
        public void doAuth(String host, String authzid, String user, String passwd) throws MessagingException, IOException {
            this.resp = SMTPTransport.this.simpleCommand(BASE64EncoderStream.encode(user.getBytes(StandardCharsets.UTF_8)));
            if (this.resp == 334) {
                this.resp = SMTPTransport.this.simpleCommand(BASE64EncoderStream.encode(passwd.getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    private class PlainAuthenticator extends Authenticator {
        PlainAuthenticator() {
            super(SMTPTransport.this, "PLAIN");
        }

        /* access modifiers changed from: package-private */
        public String getInitialResponse(String host, String authzid, String user, String passwd) throws MessagingException, IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStream b64os = new BASE64EncoderStream(bos, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
            if (authzid != null) {
                b64os.write(authzid.getBytes(StandardCharsets.UTF_8));
            }
            b64os.write(0);
            b64os.write(user.getBytes(StandardCharsets.UTF_8));
            b64os.write(0);
            b64os.write(passwd.getBytes(StandardCharsets.UTF_8));
            b64os.flush();
            return ASCIIUtility.toString(bos.toByteArray());
        }

        /* access modifiers changed from: package-private */
        public void doAuth(String host, String authzid, String user, String passwd) throws MessagingException, IOException {
            throw new AuthenticationFailedException("PLAIN asked for more");
        }
    }

    private class DigestMD5Authenticator extends Authenticator {
        static final /* synthetic */ boolean $assertionsDisabled = (!SMTPTransport.class.desiredAssertionStatus() ? true : SMTPTransport.$assertionsDisabled);
        private DigestMD5 md5support;

        DigestMD5Authenticator() {
            super(SMTPTransport.this, "DIGEST-MD5");
        }

        private synchronized DigestMD5 getMD5() {
            if (this.md5support == null) {
                this.md5support = new DigestMD5(SMTPTransport.this.logger);
            }
            return this.md5support;
        }

        /* access modifiers changed from: package-private */
        public void doAuth(String host, String authzid, String user, String passwd) throws MessagingException, IOException {
            DigestMD5 md5 = getMD5();
            if ($assertionsDisabled || md5 != null) {
                this.resp = SMTPTransport.this.simpleCommand(md5.authClient(host, user, passwd, SMTPTransport.this.getSASLRealm(), SMTPTransport.this.getLastServerResponse()));
                if (this.resp != 334) {
                    return;
                }
                if (!md5.authServer(SMTPTransport.this.getLastServerResponse())) {
                    this.resp = -1;
                } else {
                    this.resp = SMTPTransport.this.simpleCommand(new byte[0]);
                }
            } else {
                throw new AssertionError();
            }
        }
    }

    private class NtlmAuthenticator extends Authenticator {
        static final /* synthetic */ boolean $assertionsDisabled = (!SMTPTransport.class.desiredAssertionStatus() ? true : SMTPTransport.$assertionsDisabled);
        private Ntlm ntlm;

        NtlmAuthenticator() {
            super(SMTPTransport.this, "NTLM");
        }

        /* access modifiers changed from: package-private */
        public String getInitialResponse(String host, String authzid, String user, String passwd) throws MessagingException, IOException {
            this.ntlm = new Ntlm(SMTPTransport.this.getNTLMDomain(), SMTPTransport.this.getLocalHost(), user, passwd, SMTPTransport.this.logger);
            return this.ntlm.generateType1Msg(PropUtil.getIntProperty(SMTPTransport.this.session.getProperties(), "mail." + SMTPTransport.this.name + ".auth.ntlm.flags", 0), PropUtil.getBooleanProperty(SMTPTransport.this.session.getProperties(), "mail." + SMTPTransport.this.name + ".auth.ntlm.v2", true));
        }

        /* access modifiers changed from: package-private */
        public void doAuth(String host, String authzid, String user, String passwd) throws MessagingException, IOException {
            if ($assertionsDisabled || this.ntlm != null) {
                this.resp = SMTPTransport.this.simpleCommand(this.ntlm.generateType3Msg(SMTPTransport.this.getLastServerResponse().substring(4).trim()));
                return;
            }
            throw new AssertionError();
        }
    }

    private class OAuth2Authenticator extends Authenticator {
        OAuth2Authenticator() {
            super("XOAUTH2", SMTPTransport.$assertionsDisabled);
        }

        /* access modifiers changed from: package-private */
        public String getInitialResponse(String host, String authzid, String user, String passwd) throws MessagingException, IOException {
            return ASCIIUtility.toString(BASE64EncoderStream.encode(("user=" + user + "\u0001auth=Bearer " + passwd + "\u0001\u0001").getBytes(StandardCharsets.UTF_8)));
        }

        /* access modifiers changed from: package-private */
        public void doAuth(String host, String authzid, String user, String passwd) throws MessagingException, IOException {
            throw new AuthenticationFailedException("OAUTH2 asked for more");
        }
    }

    private boolean sasllogin(String[] allowed, String realm, String authzid, String u, String p) throws MessagingException {
        String serviceHost;
        List<String> v;
        String a;
        if (this.useCanonicalHostName) {
            serviceHost = this.serverSocket.getInetAddress().getCanonicalHostName();
        } else {
            serviceHost = this.host;
        }
        if (this.saslAuthenticator == null) {
            try {
                this.saslAuthenticator = (SaslAuthenticator) Class.forName("com.sun.mail.smtp.SMTPSaslAuthenticator").getConstructor(new Class[]{SMTPTransport.class, String.class, Properties.class, MailLogger.class, String.class}).newInstance(new Object[]{this, this.name, this.session.getProperties(), this.logger, serviceHost});
            } catch (Exception ex) {
                this.logger.log(Level.FINE, "Can't load SASL authenticator", (Throwable) ex);
                return $assertionsDisabled;
            }
        }
        if (allowed == null || allowed.length <= 0) {
            v = new ArrayList<>();
            if (!(this.extMap == null || (a = this.extMap.get("AUTH")) == null)) {
                StringTokenizer st = new StringTokenizer(a);
                while (st.hasMoreTokens()) {
                    v.add(st.nextToken());
                }
            }
        } else {
            v = new ArrayList<>(allowed.length);
            for (int i = 0; i < allowed.length; i++) {
                if (supportsAuthentication(allowed[i])) {
                    v.add(allowed[i]);
                }
            }
        }
        String[] mechs = (String[]) v.toArray(new String[v.size()]);
        try {
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("SASL AUTH command trace suppressed");
                suspendTracing();
            }
            return this.saslAuthenticator.authenticate(mechs, realm, authzid, u, p);
        } finally {
            resumeTracing();
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:73:0x0181=Splitter:B:73:0x0181, B:58:0x013c=Splitter:B:58:0x013c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void sendMessage(javax.mail.Message r14, javax.mail.Address[] r15) throws javax.mail.MessagingException, javax.mail.SendFailedException {
        /*
            r13 = this;
            monitor-enter(r13)
            if (r14 == 0) goto L_0x0023
            java.lang.String r1 = r14.getSubject()     // Catch:{ all -> 0x0020 }
        L_0x0007:
            r13.sendMessageStart(r1)     // Catch:{ all -> 0x0020 }
            r13.checkConnected()     // Catch:{ all -> 0x0020 }
            boolean r1 = r14 instanceof javax.mail.internet.MimeMessage     // Catch:{ all -> 0x0020 }
            if (r1 != 0) goto L_0x0026
            com.sun.mail.util.MailLogger r1 = r13.logger     // Catch:{ all -> 0x0020 }
            java.lang.String r2 = "Can only send RFC822 msgs"
            r1.fine(r2)     // Catch:{ all -> 0x0020 }
            javax.mail.MessagingException r1 = new javax.mail.MessagingException     // Catch:{ all -> 0x0020 }
            java.lang.String r2 = "SMTP can only send RFC822 messages"
            r1.<init>(r2)     // Catch:{ all -> 0x0020 }
            throw r1     // Catch:{ all -> 0x0020 }
        L_0x0020:
            r1 = move-exception
            monitor-exit(r13)
            throw r1
        L_0x0023:
            java.lang.String r1 = ""
            goto L_0x0007
        L_0x0026:
            if (r15 == 0) goto L_0x002b
            int r1 = r15.length     // Catch:{ all -> 0x0020 }
            if (r1 != 0) goto L_0x0033
        L_0x002b:
            javax.mail.SendFailedException r1 = new javax.mail.SendFailedException     // Catch:{ all -> 0x0020 }
            java.lang.String r2 = "No recipient addresses"
            r1.<init>(r2)     // Catch:{ all -> 0x0020 }
            throw r1     // Catch:{ all -> 0x0020 }
        L_0x0033:
            r10 = 0
        L_0x0034:
            int r1 = r15.length     // Catch:{ all -> 0x0020 }
            if (r10 >= r1) goto L_0x005b
            r1 = r15[r10]     // Catch:{ all -> 0x0020 }
            boolean r1 = r1 instanceof javax.mail.internet.InternetAddress     // Catch:{ all -> 0x0020 }
            if (r1 != 0) goto L_0x0058
            javax.mail.MessagingException r1 = new javax.mail.MessagingException     // Catch:{ all -> 0x0020 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0020 }
            r2.<init>()     // Catch:{ all -> 0x0020 }
            r3 = r15[r10]     // Catch:{ all -> 0x0020 }
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x0020 }
            java.lang.String r3 = " is not an InternetAddress"
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x0020 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0020 }
            r1.<init>(r2)     // Catch:{ all -> 0x0020 }
            throw r1     // Catch:{ all -> 0x0020 }
        L_0x0058:
            int r10 = r10 + 1
            goto L_0x0034
        L_0x005b:
            r0 = r14
            javax.mail.internet.MimeMessage r0 = (javax.mail.internet.MimeMessage) r0     // Catch:{ all -> 0x0020 }
            r1 = r0
            r13.message = r1     // Catch:{ all -> 0x0020 }
            r13.addresses = r15     // Catch:{ all -> 0x0020 }
            r13.validUnsentAddr = r15     // Catch:{ all -> 0x0020 }
            r13.expandGroups()     // Catch:{ all -> 0x0020 }
            r12 = 0
            boolean r1 = r14 instanceof com.sun.mail.smtp.SMTPMessage     // Catch:{ all -> 0x0020 }
            if (r1 == 0) goto L_0x0073
            com.sun.mail.smtp.SMTPMessage r14 = (com.sun.mail.smtp.SMTPMessage) r14     // Catch:{ all -> 0x0020 }
            boolean r12 = r14.getAllow8bitMIME()     // Catch:{ all -> 0x0020 }
        L_0x0073:
            if (r12 != 0) goto L_0x009b
            javax.mail.Session r1 = r13.session     // Catch:{ all -> 0x0020 }
            java.util.Properties r1 = r1.getProperties()     // Catch:{ all -> 0x0020 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0020 }
            r2.<init>()     // Catch:{ all -> 0x0020 }
            java.lang.String r3 = "mail."
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x0020 }
            java.lang.String r3 = r13.name     // Catch:{ all -> 0x0020 }
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x0020 }
            java.lang.String r3 = ".allow8bitmime"
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x0020 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0020 }
            r3 = 0
            boolean r12 = com.sun.mail.util.PropUtil.getBooleanProperty(r1, r2, r3)     // Catch:{ all -> 0x0020 }
        L_0x009b:
            com.sun.mail.util.MailLogger r1 = r13.logger     // Catch:{ all -> 0x0020 }
            java.util.logging.Level r2 = java.util.logging.Level.FINE     // Catch:{ all -> 0x0020 }
            boolean r1 = r1.isLoggable(r2)     // Catch:{ all -> 0x0020 }
            if (r1 == 0) goto L_0x00bd
            com.sun.mail.util.MailLogger r1 = r13.logger     // Catch:{ all -> 0x0020 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0020 }
            r2.<init>()     // Catch:{ all -> 0x0020 }
            java.lang.String r3 = "use8bit "
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x0020 }
            java.lang.StringBuilder r2 = r2.append(r12)     // Catch:{ all -> 0x0020 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0020 }
            r1.fine(r2)     // Catch:{ all -> 0x0020 }
        L_0x00bd:
            if (r12 == 0) goto L_0x00d4
            java.lang.String r1 = "8BITMIME"
            boolean r1 = r13.supportsExtension(r1)     // Catch:{ all -> 0x0020 }
            if (r1 == 0) goto L_0x00d4
            javax.mail.internet.MimeMessage r1 = r13.message     // Catch:{ all -> 0x0020 }
            boolean r1 = r13.convertTo8Bit(r1)     // Catch:{ all -> 0x0020 }
            if (r1 == 0) goto L_0x00d4
            javax.mail.internet.MimeMessage r1 = r13.message     // Catch:{ MessagingException -> 0x01cd }
            r1.saveChanges()     // Catch:{ MessagingException -> 0x01cd }
        L_0x00d4:
            r13.mailFrom()     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r13.rcptTo()     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            int r1 = r13.chunkSize     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            if (r1 <= 0) goto L_0x0165
            java.lang.String r1 = "CHUNKING"
            boolean r1 = r13.supportsExtension(r1)     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            if (r1 == 0) goto L_0x0165
            javax.mail.internet.MimeMessage r1 = r13.message     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            java.io.OutputStream r2 = r13.bdat()     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            java.lang.String[] r3 = ignoreList     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r1.writeTo(r2, r3)     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r13.finishBdat()     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
        L_0x00f4:
            boolean r1 = r13.sendPartiallyFailed     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            if (r1 == 0) goto L_0x0199
            com.sun.mail.util.MailLogger r1 = r13.logger     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            java.lang.String r2 = "Sending partially failed because of invalid destination addresses"
            r1.fine(r2)     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r2 = 3
            javax.mail.Address[] r3 = r13.validSentAddr     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.Address[] r4 = r13.validUnsentAddr     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.Address[] r5 = r13.invalidAddr     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.internet.MimeMessage r6 = r13.message     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r1 = r13
            r1.notifyTransportListeners(r2, r3, r4, r5, r6)     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            com.sun.mail.smtp.SMTPSendFailedException r1 = new com.sun.mail.smtp.SMTPSendFailedException     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            java.lang.String r2 = "."
            int r3 = r13.lastReturnCode     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            java.lang.String r4 = r13.lastServerResponse     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.MessagingException r5 = r13.exception     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.Address[] r6 = r13.validSentAddr     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.Address[] r7 = r13.validUnsentAddr     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.Address[] r8 = r13.invalidAddr     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r1.<init>(r2, r3, r4, r5, r6, r7, r8)     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            throw r1     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
        L_0x0120:
            r11 = move-exception
            com.sun.mail.util.MailLogger r1 = r13.logger     // Catch:{ all -> 0x014d }
            java.util.logging.Level r2 = java.util.logging.Level.FINE     // Catch:{ all -> 0x014d }
            java.lang.String r3 = "MessagingException while sending"
            r1.log((java.util.logging.Level) r2, (java.lang.String) r3, (java.lang.Throwable) r11)     // Catch:{ all -> 0x014d }
            java.lang.Exception r1 = r11.getNextException()     // Catch:{ all -> 0x014d }
            boolean r1 = r1 instanceof java.io.IOException     // Catch:{ all -> 0x014d }
            if (r1 == 0) goto L_0x013c
            com.sun.mail.util.MailLogger r1 = r13.logger     // Catch:{ all -> 0x014d }
            java.lang.String r2 = "nested IOException, closing"
            r1.fine(r2)     // Catch:{ all -> 0x014d }
            r13.closeConnection()     // Catch:{ MessagingException -> 0x01c8 }
        L_0x013c:
            r13.addressesFailed()     // Catch:{ all -> 0x014d }
            r2 = 2
            javax.mail.Address[] r3 = r13.validSentAddr     // Catch:{ all -> 0x014d }
            javax.mail.Address[] r4 = r13.validUnsentAddr     // Catch:{ all -> 0x014d }
            javax.mail.Address[] r5 = r13.invalidAddr     // Catch:{ all -> 0x014d }
            javax.mail.internet.MimeMessage r6 = r13.message     // Catch:{ all -> 0x014d }
            r1 = r13
            r1.notifyTransportListeners(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x014d }
            throw r11     // Catch:{ all -> 0x014d }
        L_0x014d:
            r1 = move-exception
            r2 = 0
            r13.invalidAddr = r2     // Catch:{ all -> 0x0020 }
            r13.validUnsentAddr = r2     // Catch:{ all -> 0x0020 }
            r13.validSentAddr = r2     // Catch:{ all -> 0x0020 }
            r2 = 0
            r13.addresses = r2     // Catch:{ all -> 0x0020 }
            r2 = 0
            r13.message = r2     // Catch:{ all -> 0x0020 }
            r2 = 0
            r13.exception = r2     // Catch:{ all -> 0x0020 }
            r2 = 0
            r13.sendPartiallyFailed = r2     // Catch:{ all -> 0x0020 }
            r2 = 0
            r13.notificationDone = r2     // Catch:{ all -> 0x0020 }
            throw r1     // Catch:{ all -> 0x0020 }
        L_0x0165:
            javax.mail.internet.MimeMessage r1 = r13.message     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            java.io.OutputStream r2 = r13.data()     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            java.lang.String[] r3 = ignoreList     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r1.writeTo(r2, r3)     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r13.finishData()     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            goto L_0x00f4
        L_0x0174:
            r9 = move-exception
            com.sun.mail.util.MailLogger r1 = r13.logger     // Catch:{ all -> 0x014d }
            java.util.logging.Level r2 = java.util.logging.Level.FINE     // Catch:{ all -> 0x014d }
            java.lang.String r3 = "IOException while sending, closing"
            r1.log((java.util.logging.Level) r2, (java.lang.String) r3, (java.lang.Throwable) r9)     // Catch:{ all -> 0x014d }
            r13.closeConnection()     // Catch:{ MessagingException -> 0x01cb }
        L_0x0181:
            r13.addressesFailed()     // Catch:{ all -> 0x014d }
            r2 = 2
            javax.mail.Address[] r3 = r13.validSentAddr     // Catch:{ all -> 0x014d }
            javax.mail.Address[] r4 = r13.validUnsentAddr     // Catch:{ all -> 0x014d }
            javax.mail.Address[] r5 = r13.invalidAddr     // Catch:{ all -> 0x014d }
            javax.mail.internet.MimeMessage r6 = r13.message     // Catch:{ all -> 0x014d }
            r1 = r13
            r1.notifyTransportListeners(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x014d }
            javax.mail.MessagingException r1 = new javax.mail.MessagingException     // Catch:{ all -> 0x014d }
            java.lang.String r2 = "IOException while sending message"
            r1.<init>(r2, r9)     // Catch:{ all -> 0x014d }
            throw r1     // Catch:{ all -> 0x014d }
        L_0x0199:
            com.sun.mail.util.MailLogger r1 = r13.logger     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            java.lang.String r2 = "message successfully delivered to mail server"
            r1.fine(r2)     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r2 = 1
            javax.mail.Address[] r3 = r13.validSentAddr     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.Address[] r4 = r13.validUnsentAddr     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.Address[] r5 = r13.invalidAddr     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            javax.mail.internet.MimeMessage r6 = r13.message     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r1 = r13
            r1.notifyTransportListeners(r2, r3, r4, r5, r6)     // Catch:{ MessagingException -> 0x0120, IOException -> 0x0174 }
            r1 = 0
            r13.invalidAddr = r1     // Catch:{ all -> 0x0020 }
            r13.validUnsentAddr = r1     // Catch:{ all -> 0x0020 }
            r13.validSentAddr = r1     // Catch:{ all -> 0x0020 }
            r1 = 0
            r13.addresses = r1     // Catch:{ all -> 0x0020 }
            r1 = 0
            r13.message = r1     // Catch:{ all -> 0x0020 }
            r1 = 0
            r13.exception = r1     // Catch:{ all -> 0x0020 }
            r1 = 0
            r13.sendPartiallyFailed = r1     // Catch:{ all -> 0x0020 }
            r1 = 0
            r13.notificationDone = r1     // Catch:{ all -> 0x0020 }
            r13.sendMessageEnd()     // Catch:{ all -> 0x0020 }
            monitor-exit(r13)
            return
        L_0x01c8:
            r1 = move-exception
            goto L_0x013c
        L_0x01cb:
            r1 = move-exception
            goto L_0x0181
        L_0x01cd:
            r1 = move-exception
            goto L_0x00d4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.sendMessage(javax.mail.Message, javax.mail.Address[]):void");
    }

    private void addressesFailed() {
        if (this.validSentAddr == null) {
            return;
        }
        if (this.validUnsentAddr != null) {
            Address[] newa = new Address[(this.validSentAddr.length + this.validUnsentAddr.length)];
            System.arraycopy(this.validSentAddr, 0, newa, 0, this.validSentAddr.length);
            System.arraycopy(this.validUnsentAddr, 0, newa, this.validSentAddr.length, this.validUnsentAddr.length);
            this.validSentAddr = null;
            this.validUnsentAddr = newa;
            return;
        }
        this.validUnsentAddr = this.validSentAddr;
        this.validSentAddr = null;
    }

    public synchronized void close() throws MessagingException {
        int resp;
        if (super.isConnected()) {
            try {
                if (this.serverSocket != null) {
                    sendCommand("QUIT");
                    if (this.quitWait && (resp = readServerResponse()) != 221 && resp != -1 && this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine("QUIT failed with " + resp);
                    }
                }
            } finally {
                closeConnection();
            }
        }
    }

    /* access modifiers changed from: private */
    public void closeConnection() throws MessagingException {
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
            this.serverSocket = null;
            this.serverOutput = null;
            this.serverInput = null;
            this.lineInputStream = null;
            if (super.isConnected()) {
                super.close();
            }
        } catch (IOException ioex) {
            throw new MessagingException("Server Close Failed", ioex);
        } catch (Throwable th) {
            this.serverSocket = null;
            this.serverOutput = null;
            this.serverInput = null;
            this.lineInputStream = null;
            if (super.isConnected()) {
                super.close();
            }
            throw th;
        }
    }

    public synchronized boolean isConnected() {
        boolean z = $assertionsDisabled;
        synchronized (this) {
            if (super.isConnected()) {
                try {
                    if (this.useRset) {
                        sendCommand("RSET");
                    } else {
                        sendCommand("NOOP");
                    }
                    int resp = readServerResponse();
                    if (resp < 0 || (!this.noopStrict ? resp == 421 : resp != 250)) {
                        try {
                            closeConnection();
                        } catch (MessagingException e) {
                        }
                    } else {
                        z = true;
                    }
                } catch (Exception e2) {
                    try {
                        closeConnection();
                    } catch (MessagingException e3) {
                    }
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void notifyTransportListeners(int type, Address[] validSent, Address[] validUnsent, Address[] invalid, Message msg) {
        if (!this.notificationDone) {
            super.notifyTransportListeners(type, validSent, validUnsent, invalid, msg);
            this.notificationDone = true;
        }
    }

    private void expandGroups() {
        List<Address> groups = null;
        for (int i = 0; i < this.addresses.length; i++) {
            InternetAddress a = (InternetAddress) this.addresses[i];
            if (a.isGroup()) {
                if (groups == null) {
                    groups = new ArrayList<>();
                    for (int k = 0; k < i; k++) {
                        groups.add(this.addresses[k]);
                    }
                }
                try {
                    InternetAddress[] ia = a.getGroup(true);
                    if (ia != null) {
                        for (InternetAddress add : ia) {
                            groups.add(add);
                        }
                    } else {
                        groups.add(a);
                    }
                } catch (ParseException e) {
                    groups.add(a);
                }
            } else if (groups != null) {
                groups.add(a);
            }
        }
        if (groups != null) {
            InternetAddress[] newa = new InternetAddress[groups.size()];
            groups.toArray(newa);
            this.addresses = newa;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0044, code lost:
        if (r4 != null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        throw r6;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0075 A[ExcHandler: MessagingException (e javax.mail.MessagingException), PHI: r0 
      PHI: (r0v1 'changed' boolean) = (r0v0 'changed' boolean), (r0v0 'changed' boolean), (r0v0 'changed' boolean), (r0v0 'changed' boolean), (r0v6 'changed' boolean), (r0v6 'changed' boolean) binds: [B:1:0x0003, B:24:0x0049, B:22:0x0046, B:23:?, B:17:0x003f, B:18:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x0003] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean convertTo8Bit(javax.mail.internet.MimePart r9) {
        /*
            r8 = this;
            r0 = 0
            java.lang.String r6 = "text/*"
            boolean r6 = r9.isMimeType(r6)     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            if (r6 == 0) goto L_0x004c
            java.lang.String r2 = r9.getEncoding()     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            if (r2 == 0) goto L_0x0042
            java.lang.String r6 = "quoted-printable"
            boolean r6 = r2.equalsIgnoreCase(r6)     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            if (r6 != 0) goto L_0x001f
            java.lang.String r6 = "base64"
            boolean r6 = r2.equalsIgnoreCase(r6)     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            if (r6 == 0) goto L_0x0042
        L_0x001f:
            r4 = 0
            java.io.InputStream r4 = r9.getInputStream()     // Catch:{ all -> 0x0043 }
            boolean r6 = r8.is8Bit(r4)     // Catch:{ all -> 0x0043 }
            if (r6 == 0) goto L_0x003d
            java.lang.Object r6 = r9.getContent()     // Catch:{ all -> 0x0043 }
            java.lang.String r7 = r9.getContentType()     // Catch:{ all -> 0x0043 }
            r9.setContent(r6, r7)     // Catch:{ all -> 0x0043 }
            java.lang.String r6 = "Content-Transfer-Encoding"
            java.lang.String r7 = "8bit"
            r9.setHeader(r6, r7)     // Catch:{ all -> 0x0043 }
            r0 = 1
        L_0x003d:
            if (r4 == 0) goto L_0x0042
            r4.close()     // Catch:{ IOException -> 0x0071, MessagingException -> 0x0075 }
        L_0x0042:
            return r0
        L_0x0043:
            r6 = move-exception
            if (r4 == 0) goto L_0x0049
            r4.close()     // Catch:{ IOException -> 0x0073, MessagingException -> 0x0075 }
        L_0x0049:
            throw r6     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
        L_0x004a:
            r6 = move-exception
            goto L_0x0042
        L_0x004c:
            java.lang.String r6 = "multipart/*"
            boolean r6 = r9.isMimeType(r6)     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            if (r6 == 0) goto L_0x0042
            java.lang.Object r5 = r9.getContent()     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            javax.mail.internet.MimeMultipart r5 = (javax.mail.internet.MimeMultipart) r5     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            int r1 = r5.getCount()     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            r3 = 0
        L_0x005f:
            if (r3 >= r1) goto L_0x0042
            javax.mail.BodyPart r6 = r5.getBodyPart((int) r3)     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            javax.mail.internet.MimePart r6 = (javax.mail.internet.MimePart) r6     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            boolean r6 = r8.convertTo8Bit(r6)     // Catch:{ IOException -> 0x004a, MessagingException -> 0x0075 }
            if (r6 == 0) goto L_0x006e
            r0 = 1
        L_0x006e:
            int r3 = r3 + 1
            goto L_0x005f
        L_0x0071:
            r6 = move-exception
            goto L_0x0042
        L_0x0073:
            r7 = move-exception
            goto L_0x0049
        L_0x0075:
            r6 = move-exception
            goto L_0x0042
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.convertTo8Bit(javax.mail.internet.MimePart):boolean");
    }

    private boolean is8Bit(InputStream is) {
        int linelen = 0;
        boolean need8bit = $assertionsDisabled;
        while (true) {
            try {
                int b = is.read();
                if (b >= 0) {
                    int b2 = b & 255;
                    if (b2 == 13 || b2 == 10) {
                        linelen = 0;
                    } else if (b2 == 0) {
                        return $assertionsDisabled;
                    } else {
                        linelen++;
                        if (linelen > 998) {
                            return $assertionsDisabled;
                        }
                    }
                    if (b2 > 127) {
                        need8bit = true;
                    }
                } else if (!need8bit) {
                    return need8bit;
                } else {
                    this.logger.fine("found an 8bit part");
                    return need8bit;
                }
            } catch (IOException e) {
                return $assertionsDisabled;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            closeConnection();
        } catch (MessagingException e) {
        } finally {
            super.finalize();
        }
    }

    /* access modifiers changed from: protected */
    public void helo(String domain) throws MessagingException {
        if (domain != null) {
            issueCommand("HELO " + domain, 250);
        } else {
            issueCommand("HELO", 250);
        }
    }

    /* access modifiers changed from: protected */
    public boolean ehlo(String domain) throws MessagingException {
        String cmd;
        if (domain != null) {
            cmd = "EHLO " + domain;
        } else {
            cmd = "EHLO";
        }
        sendCommand(cmd);
        int resp = readServerResponse();
        if (resp == 250) {
            BufferedReader rd = new BufferedReader(new StringReader(this.lastServerResponse));
            this.extMap = new Hashtable<>();
            boolean first = true;
            while (true) {
                try {
                    String line = rd.readLine();
                    if (line == null) {
                        break;
                    } else if (first) {
                        first = $assertionsDisabled;
                    } else if (line.length() >= 5) {
                        String line2 = line.substring(4);
                        int i = line2.indexOf(32);
                        String arg = "";
                        if (i > 0) {
                            arg = line2.substring(i + 1);
                            line2 = line2.substring(0, i);
                        }
                        if (this.logger.isLoggable(Level.FINE)) {
                            this.logger.fine("Found extension \"" + line2 + "\", arg \"" + arg + "\"");
                        }
                        this.extMap.put(line2.toUpperCase(Locale.ENGLISH), arg);
                    }
                } catch (IOException e) {
                }
            }
        }
        if (resp == 250) {
            return true;
        }
        return $assertionsDisabled;
    }

    /* access modifiers changed from: protected */
    public void mailFrom() throws MessagingException {
        boolean z;
        Address me;
        Address[] fa;
        String from = null;
        if (this.message instanceof SMTPMessage) {
            from = ((SMTPMessage) this.message).getEnvelopeFrom();
        }
        if (from == null || from.length() <= 0) {
            from = this.session.getProperty("mail." + this.name + ".from");
        }
        if (from == null || from.length() <= 0) {
            if (this.message == null || (fa = this.message.getFrom()) == null || fa.length <= 0) {
                me = InternetAddress.getLocalAddress(this.session);
            } else {
                me = fa[0];
            }
            if (me != null) {
                from = ((InternetAddress) me).getAddress();
            } else {
                throw new MessagingException("can't determine local email address");
            }
        }
        String cmd = "MAIL FROM:" + normalizeAddress(from);
        if (this.allowutf8 && supportsExtension("SMTPUTF8")) {
            cmd = cmd + " SMTPUTF8";
        }
        if (supportsExtension("DSN")) {
            String ret = null;
            if (this.message instanceof SMTPMessage) {
                ret = ((SMTPMessage) this.message).getDSNRet();
            }
            if (ret == null) {
                ret = this.session.getProperty("mail." + this.name + ".dsn.ret");
            }
            if (ret != null) {
                cmd = cmd + " RET=" + ret;
            }
        }
        if (supportsExtension("AUTH")) {
            String submitter = null;
            if (this.message instanceof SMTPMessage) {
                submitter = ((SMTPMessage) this.message).getSubmitter();
            }
            if (submitter == null) {
                submitter = this.session.getProperty("mail." + this.name + ".submitter");
            }
            if (submitter != null) {
                try {
                    if (!this.allowutf8 || !supportsExtension("SMTPUTF8")) {
                        z = false;
                    } else {
                        z = true;
                    }
                    cmd = cmd + " AUTH=" + xtext(submitter, z);
                } catch (IllegalArgumentException ex) {
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.log(Level.FINE, "ignoring invalid submitter: " + submitter, (Throwable) ex);
                    }
                }
            }
        }
        String ext = null;
        if (this.message instanceof SMTPMessage) {
            ext = ((SMTPMessage) this.message).getMailExtension();
        }
        if (ext == null) {
            ext = this.session.getProperty("mail." + this.name + ".mailextension");
        }
        if (ext != null && ext.length() > 0) {
            cmd = cmd + " " + ext;
        }
        try {
            issueSendCommand(cmd, 250);
        } catch (SMTPSendFailedException ex2) {
            int retCode = ex2.getReturnCode();
            switch (retCode) {
                case ErrorMessages.ERROR_BLUETOOTH_NOT_AVAILABLE:
                case ErrorMessages.ERROR_BLUETOOTH_INVALID_ADDRESS:
                case 550:
                case 551:
                case 553:
                    try {
                        ex2.setNextException(new SMTPSenderFailedException(new InternetAddress(from), cmd, retCode, ex2.getMessage()));
                        break;
                    } catch (AddressException e) {
                        break;
                    }
            }
            throw ex2;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r24v0, resolved type: com.sun.mail.smtp.SMTPAddressFailedException} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r24v1, resolved type: com.sun.mail.smtp.SMTPAddressFailedException} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r24v2, resolved type: com.sun.mail.smtp.SMTPAddressFailedException} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r24v3, resolved type: com.sun.mail.smtp.SMTPAddressFailedException} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v87, resolved type: com.sun.mail.smtp.SMTPAddressSucceededException} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r24v4, resolved type: com.sun.mail.smtp.SMTPAddressFailedException} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r24v5, resolved type: com.sun.mail.smtp.SMTPAddressFailedException} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r24v6, resolved type: com.sun.mail.smtp.SMTPAddressFailedException} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void rcptTo() throws javax.mail.MessagingException {
        /*
            r32 = this;
            java.util.ArrayList r30 = new java.util.ArrayList
            r30.<init>()
            java.util.ArrayList r31 = new java.util.ArrayList
            r31.<init>()
            java.util.ArrayList r18 = new java.util.ArrayList
            r18.<init>()
            r26 = -1
            r24 = 0
            r27 = 0
            r29 = 0
            r3 = 0
            r0 = r32
            r0.invalidAddr = r3
            r0 = r32
            r0.validUnsentAddr = r3
            r0 = r32
            r0.validSentAddr = r3
            r28 = 0
            r0 = r32
            javax.mail.internet.MimeMessage r3 = r0.message
            boolean r3 = r3 instanceof com.sun.mail.smtp.SMTPMessage
            if (r3 == 0) goto L_0x0038
            r0 = r32
            javax.mail.internet.MimeMessage r3 = r0.message
            com.sun.mail.smtp.SMTPMessage r3 = (com.sun.mail.smtp.SMTPMessage) r3
            boolean r28 = r3.getSendPartial()
        L_0x0038:
            if (r28 != 0) goto L_0x0064
            r0 = r32
            javax.mail.Session r3 = r0.session
            java.util.Properties r3 = r3.getProperties()
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "mail."
            java.lang.StringBuilder r4 = r4.append(r5)
            r0 = r32
            java.lang.String r5 = r0.name
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r5 = ".sendpartial"
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r5 = 0
            boolean r28 = com.sun.mail.util.PropUtil.getBooleanProperty(r3, r4, r5)
        L_0x0064:
            if (r28 == 0) goto L_0x006f
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.lang.String r4 = "sendPartial set"
            r3.fine(r4)
        L_0x006f:
            r12 = 0
            r25 = 0
            java.lang.String r3 = "DSN"
            r0 = r32
            boolean r3 = r0.supportsExtension(r3)
            if (r3 == 0) goto L_0x00b8
            r0 = r32
            javax.mail.internet.MimeMessage r3 = r0.message
            boolean r3 = r3 instanceof com.sun.mail.smtp.SMTPMessage
            if (r3 == 0) goto L_0x008e
            r0 = r32
            javax.mail.internet.MimeMessage r3 = r0.message
            com.sun.mail.smtp.SMTPMessage r3 = (com.sun.mail.smtp.SMTPMessage) r3
            java.lang.String r25 = r3.getDSNNotify()
        L_0x008e:
            if (r25 != 0) goto L_0x00b5
            r0 = r32
            javax.mail.Session r3 = r0.session
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "mail."
            java.lang.StringBuilder r4 = r4.append(r5)
            r0 = r32
            java.lang.String r5 = r0.name
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r5 = ".dsn.notify"
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            java.lang.String r25 = r3.getProperty(r4)
        L_0x00b5:
            if (r25 == 0) goto L_0x00b8
            r12 = 1
        L_0x00b8:
            r15 = 0
        L_0x00b9:
            r0 = r32
            javax.mail.Address[] r3 = r0.addresses
            int r3 = r3.length
            if (r15 >= r3) goto L_0x022f
            r29 = 0
            r0 = r32
            javax.mail.Address[] r3 = r0.addresses
            r17 = r3[r15]
            javax.mail.internet.InternetAddress r17 = (javax.mail.internet.InternetAddress) r17
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "RCPT TO:"
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r4 = r17.getAddress()
            r0 = r32
            java.lang.String r4 = r0.normalizeAddress(r4)
            java.lang.StringBuilder r3 = r3.append(r4)
            java.lang.String r11 = r3.toString()
            if (r12 == 0) goto L_0x0102
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.StringBuilder r3 = r3.append(r11)
            java.lang.String r4 = " NOTIFY="
            java.lang.StringBuilder r3 = r3.append(r4)
            r0 = r25
            java.lang.StringBuilder r3 = r3.append(r0)
            java.lang.String r11 = r3.toString()
        L_0x0102:
            r0 = r32
            r0.sendCommand((java.lang.String) r11)
            int r26 = r32.readServerResponse()
            switch(r26) {
                case 250: goto L_0x013c;
                case 251: goto L_0x013c;
                case 450: goto L_0x018c;
                case 451: goto L_0x018c;
                case 452: goto L_0x018c;
                case 501: goto L_0x0165;
                case 503: goto L_0x0165;
                case 550: goto L_0x0165;
                case 551: goto L_0x0165;
                case 552: goto L_0x018c;
                case 553: goto L_0x0165;
                default: goto L_0x010e;
            }
        L_0x010e:
            r3 = 400(0x190, float:5.6E-43)
            r0 = r26
            if (r0 < r3) goto L_0x01b3
            r3 = 499(0x1f3, float:6.99E-43)
            r0 = r26
            if (r0 > r3) goto L_0x01b3
            r0 = r31
            r1 = r17
            r0.add(r1)
        L_0x0121:
            if (r28 != 0) goto L_0x0125
            r27 = 1
        L_0x0125:
            com.sun.mail.smtp.SMTPAddressFailedException r29 = new com.sun.mail.smtp.SMTPAddressFailedException
            r0 = r32
            java.lang.String r3 = r0.lastServerResponse
            r0 = r29
            r1 = r17
            r2 = r26
            r0.<init>(r1, r11, r2, r3)
            if (r24 != 0) goto L_0x0226
            r24 = r29
        L_0x0138:
            int r15 = r15 + 1
            goto L_0x00b9
        L_0x013c:
            r0 = r30
            r1 = r17
            r0.add(r1)
            r0 = r32
            boolean r3 = r0.reportSuccess
            if (r3 == 0) goto L_0x0138
            com.sun.mail.smtp.SMTPAddressSucceededException r29 = new com.sun.mail.smtp.SMTPAddressSucceededException
            r0 = r32
            java.lang.String r3 = r0.lastServerResponse
            r0 = r29
            r1 = r17
            r2 = r26
            r0.<init>(r1, r11, r2, r3)
            if (r24 != 0) goto L_0x015d
            r24 = r29
            goto L_0x0138
        L_0x015d:
            r0 = r24
            r1 = r29
            r0.setNextException(r1)
            goto L_0x0138
        L_0x0165:
            if (r28 != 0) goto L_0x0169
            r27 = 1
        L_0x0169:
            r0 = r18
            r1 = r17
            r0.add(r1)
            com.sun.mail.smtp.SMTPAddressFailedException r29 = new com.sun.mail.smtp.SMTPAddressFailedException
            r0 = r32
            java.lang.String r3 = r0.lastServerResponse
            r0 = r29
            r1 = r17
            r2 = r26
            r0.<init>(r1, r11, r2, r3)
            if (r24 != 0) goto L_0x0184
            r24 = r29
            goto L_0x0138
        L_0x0184:
            r0 = r24
            r1 = r29
            r0.setNextException(r1)
            goto L_0x0138
        L_0x018c:
            if (r28 != 0) goto L_0x0190
            r27 = 1
        L_0x0190:
            r0 = r31
            r1 = r17
            r0.add(r1)
            com.sun.mail.smtp.SMTPAddressFailedException r29 = new com.sun.mail.smtp.SMTPAddressFailedException
            r0 = r32
            java.lang.String r3 = r0.lastServerResponse
            r0 = r29
            r1 = r17
            r2 = r26
            r0.<init>(r1, r11, r2, r3)
            if (r24 != 0) goto L_0x01ab
            r24 = r29
            goto L_0x0138
        L_0x01ab:
            r0 = r24
            r1 = r29
            r0.setNextException(r1)
            goto L_0x0138
        L_0x01b3:
            r3 = 500(0x1f4, float:7.0E-43)
            r0 = r26
            if (r0 < r3) goto L_0x01c8
            r3 = 599(0x257, float:8.4E-43)
            r0 = r26
            if (r0 > r3) goto L_0x01c8
            r0 = r18
            r1 = r17
            r0.add(r1)
            goto L_0x0121
        L_0x01c8:
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.util.logging.Level r4 = java.util.logging.Level.FINE
            boolean r3 = r3.isLoggable(r4)
            if (r3 == 0) goto L_0x01fe
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "got response code "
            java.lang.StringBuilder r4 = r4.append(r5)
            r0 = r26
            java.lang.StringBuilder r4 = r4.append(r0)
            java.lang.String r5 = ", with response: "
            java.lang.StringBuilder r4 = r4.append(r5)
            r0 = r32
            java.lang.String r5 = r0.lastServerResponse
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.fine(r4)
        L_0x01fe:
            r0 = r32
            java.lang.String r10 = r0.lastServerResponse
            r0 = r32
            int r9 = r0.lastReturnCode
            r0 = r32
            java.net.Socket r3 = r0.serverSocket
            if (r3 == 0) goto L_0x0214
            java.lang.String r3 = "RSET"
            r4 = -1
            r0 = r32
            r0.issueCommand(r3, r4)
        L_0x0214:
            r0 = r32
            r0.lastServerResponse = r10
            r0 = r32
            r0.lastReturnCode = r9
            com.sun.mail.smtp.SMTPAddressFailedException r3 = new com.sun.mail.smtp.SMTPAddressFailedException
            r0 = r17
            r1 = r26
            r3.<init>(r0, r11, r1, r10)
            throw r3
        L_0x0226:
            r0 = r24
            r1 = r29
            r0.setNextException(r1)
            goto L_0x0138
        L_0x022f:
            if (r28 == 0) goto L_0x0239
            int r3 = r30.size()
            if (r3 != 0) goto L_0x0239
            r27 = 1
        L_0x0239:
            if (r27 == 0) goto L_0x02a0
            int r3 = r18.size()
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r32
            r0.invalidAddr = r3
            r0 = r32
            javax.mail.Address[] r3 = r0.invalidAddr
            r0 = r18
            r0.toArray(r3)
            int r3 = r30.size()
            int r4 = r31.size()
            int r3 = r3 + r4
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r32
            r0.validUnsentAddr = r3
            r15 = 0
            r19 = 0
        L_0x0260:
            int r3 = r30.size()
            r0 = r19
            if (r0 >= r3) goto L_0x027f
            r0 = r32
            javax.mail.Address[] r4 = r0.validUnsentAddr
            int r16 = r15 + 1
            r0 = r30
            r1 = r19
            java.lang.Object r3 = r0.get(r1)
            javax.mail.Address r3 = (javax.mail.Address) r3
            r4[r15] = r3
            int r19 = r19 + 1
            r15 = r16
            goto L_0x0260
        L_0x027f:
            r19 = 0
        L_0x0281:
            int r3 = r31.size()
            r0 = r19
            if (r0 >= r3) goto L_0x02f8
            r0 = r32
            javax.mail.Address[] r4 = r0.validUnsentAddr
            int r16 = r15 + 1
            r0 = r31
            r1 = r19
            java.lang.Object r3 = r0.get(r1)
            javax.mail.Address r3 = (javax.mail.Address) r3
            r4[r15] = r3
            int r19 = r19 + 1
            r15 = r16
            goto L_0x0281
        L_0x02a0:
            r0 = r32
            boolean r3 = r0.reportSuccess
            if (r3 != 0) goto L_0x02b4
            if (r28 == 0) goto L_0x0348
            int r3 = r18.size()
            if (r3 > 0) goto L_0x02b4
            int r3 = r31.size()
            if (r3 <= 0) goto L_0x0348
        L_0x02b4:
            r3 = 1
            r0 = r32
            r0.sendPartiallyFailed = r3
            r0 = r24
            r1 = r32
            r1.exception = r0
            int r3 = r18.size()
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r32
            r0.invalidAddr = r3
            r0 = r32
            javax.mail.Address[] r3 = r0.invalidAddr
            r0 = r18
            r0.toArray(r3)
            int r3 = r31.size()
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r32
            r0.validUnsentAddr = r3
            r0 = r32
            javax.mail.Address[] r3 = r0.validUnsentAddr
            r0 = r31
            r0.toArray(r3)
            int r3 = r30.size()
            javax.mail.Address[] r3 = new javax.mail.Address[r3]
            r0 = r32
            r0.validSentAddr = r3
            r0 = r32
            javax.mail.Address[] r3 = r0.validSentAddr
            r0 = r30
            r0.toArray(r3)
        L_0x02f8:
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.util.logging.Level r4 = java.util.logging.Level.FINE
            boolean r3 = r3.isLoggable(r4)
            if (r3 == 0) goto L_0x03d9
            r0 = r32
            javax.mail.Address[] r3 = r0.validSentAddr
            if (r3 == 0) goto L_0x0351
            r0 = r32
            javax.mail.Address[] r3 = r0.validSentAddr
            int r3 = r3.length
            if (r3 <= 0) goto L_0x0351
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.lang.String r4 = "Verified Addresses"
            r3.fine(r4)
            r21 = 0
        L_0x031c:
            r0 = r32
            javax.mail.Address[] r3 = r0.validSentAddr
            int r3 = r3.length
            r0 = r21
            if (r0 >= r3) goto L_0x0351
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "  "
            java.lang.StringBuilder r4 = r4.append(r5)
            r0 = r32
            javax.mail.Address[] r5 = r0.validSentAddr
            r5 = r5[r21]
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.fine(r4)
            int r21 = r21 + 1
            goto L_0x031c
        L_0x0348:
            r0 = r32
            javax.mail.Address[] r3 = r0.addresses
            r0 = r32
            r0.validSentAddr = r3
            goto L_0x02f8
        L_0x0351:
            r0 = r32
            javax.mail.Address[] r3 = r0.validUnsentAddr
            if (r3 == 0) goto L_0x0395
            r0 = r32
            javax.mail.Address[] r3 = r0.validUnsentAddr
            int r3 = r3.length
            if (r3 <= 0) goto L_0x0395
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.lang.String r4 = "Valid Unsent Addresses"
            r3.fine(r4)
            r19 = 0
        L_0x0369:
            r0 = r32
            javax.mail.Address[] r3 = r0.validUnsentAddr
            int r3 = r3.length
            r0 = r19
            if (r0 >= r3) goto L_0x0395
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "  "
            java.lang.StringBuilder r4 = r4.append(r5)
            r0 = r32
            javax.mail.Address[] r5 = r0.validUnsentAddr
            r5 = r5[r19]
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.fine(r4)
            int r19 = r19 + 1
            goto L_0x0369
        L_0x0395:
            r0 = r32
            javax.mail.Address[] r3 = r0.invalidAddr
            if (r3 == 0) goto L_0x03d9
            r0 = r32
            javax.mail.Address[] r3 = r0.invalidAddr
            int r3 = r3.length
            if (r3 <= 0) goto L_0x03d9
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.lang.String r4 = "Invalid Addresses"
            r3.fine(r4)
            r20 = 0
        L_0x03ad:
            r0 = r32
            javax.mail.Address[] r3 = r0.invalidAddr
            int r3 = r3.length
            r0 = r20
            if (r0 >= r3) goto L_0x03d9
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "  "
            java.lang.StringBuilder r4 = r4.append(r5)
            r0 = r32
            javax.mail.Address[] r5 = r0.invalidAddr
            r5 = r5[r20]
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.fine(r4)
            int r20 = r20 + 1
            goto L_0x03ad
        L_0x03d9:
            if (r27 == 0) goto L_0x0462
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger
            java.lang.String r4 = "Sending failed because of invalid destination addresses"
            r3.fine(r4)
            r4 = 2
            r0 = r32
            javax.mail.Address[] r5 = r0.validSentAddr
            r0 = r32
            javax.mail.Address[] r6 = r0.validUnsentAddr
            r0 = r32
            javax.mail.Address[] r7 = r0.invalidAddr
            r0 = r32
            javax.mail.internet.MimeMessage r8 = r0.message
            r3 = r32
            r3.notifyTransportListeners(r4, r5, r6, r7, r8)
            r0 = r32
            java.lang.String r0 = r0.lastServerResponse
            r23 = r0
            r0 = r32
            int r0 = r0.lastReturnCode
            r22 = r0
            r0 = r32
            java.net.Socket r3 = r0.serverSocket     // Catch:{ MessagingException -> 0x0436 }
            if (r3 == 0) goto L_0x0414
            java.lang.String r3 = "RSET"
            r4 = -1
            r0 = r32
            r0.issueCommand(r3, r4)     // Catch:{ MessagingException -> 0x0436 }
        L_0x0414:
            r0 = r23
            r1 = r32
            r1.lastServerResponse = r0
            r0 = r22
            r1 = r32
            r1.lastReturnCode = r0
        L_0x0420:
            javax.mail.SendFailedException r3 = new javax.mail.SendFailedException
            java.lang.String r4 = "Invalid Addresses"
            r0 = r32
            javax.mail.Address[] r6 = r0.validSentAddr
            r0 = r32
            javax.mail.Address[] r7 = r0.validUnsentAddr
            r0 = r32
            javax.mail.Address[] r8 = r0.invalidAddr
            r5 = r24
            r3.<init>(r4, r5, r6, r7, r8)
            throw r3
        L_0x0436:
            r13 = move-exception
            r32.close()     // Catch:{ MessagingException -> 0x0447 }
        L_0x043a:
            r0 = r23
            r1 = r32
            r1.lastServerResponse = r0
            r0 = r22
            r1 = r32
            r1.lastReturnCode = r0
            goto L_0x0420
        L_0x0447:
            r14 = move-exception
            r0 = r32
            com.sun.mail.util.MailLogger r3 = r0.logger     // Catch:{ all -> 0x0454 }
            java.util.logging.Level r4 = java.util.logging.Level.FINE     // Catch:{ all -> 0x0454 }
            java.lang.String r5 = "close failed"
            r3.log((java.util.logging.Level) r4, (java.lang.String) r5, (java.lang.Throwable) r14)     // Catch:{ all -> 0x0454 }
            goto L_0x043a
        L_0x0454:
            r3 = move-exception
            r0 = r23
            r1 = r32
            r1.lastServerResponse = r0
            r0 = r22
            r1 = r32
            r1.lastReturnCode = r0
            throw r3
        L_0x0462:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.rcptTo():void");
    }

    /* access modifiers changed from: protected */
    public OutputStream data() throws MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            issueSendCommand("DATA", 354);
            this.dataStream = new SMTPOutputStream(this.serverOutput);
            return this.dataStream;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public void finishData() throws IOException, MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            this.dataStream.ensureAtBOL();
            issueSendCommand(".", 250);
            return;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public OutputStream bdat() throws MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            this.dataStream = new BDATOutputStream(this.serverOutput, this.chunkSize);
            return this.dataStream;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public void finishBdat() throws IOException, MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            this.dataStream.ensureAtBOL();
            this.dataStream.close();
            return;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public void startTLS() throws MessagingException {
        issueCommand("STARTTLS", 220);
        try {
            this.serverSocket = SocketFetcher.startTLS(this.serverSocket, this.host, this.session.getProperties(), "mail." + this.name);
            initStreams();
        } catch (IOException ioex) {
            closeConnection();
            throw new MessagingException("Could not convert socket to TLS", ioex);
        }
    }

    private void openServer(String host2, int port) throws MessagingException {
        int resp;
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("trying to connect to host \"" + host2 + "\", port " + port + ", isSSL " + this.isSSL);
        }
        try {
            this.serverSocket = SocketFetcher.getSocket(host2, port, this.session.getProperties(), "mail." + this.name, this.isSSL);
            port = this.serverSocket.getPort();
            this.host = host2;
            initStreams();
            if (readServerResponse() != 220) {
                String failResponse = this.lastServerResponse;
                try {
                    if (this.quitOnSessionReject) {
                        sendCommand("QUIT");
                        if (this.quitWait && (resp = readServerResponse()) != 221 && resp != -1 && this.logger.isLoggable(Level.FINE)) {
                            this.logger.fine("QUIT failed with " + resp);
                        }
                    }
                    this.serverSocket.close();
                    this.serverSocket = null;
                    this.serverOutput = null;
                    this.serverInput = null;
                    this.lineInputStream = null;
                } catch (Exception e) {
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.log(Level.FINE, "QUIT failed", (Throwable) e);
                    }
                    this.serverSocket.close();
                    this.serverSocket = null;
                    this.serverOutput = null;
                    this.serverInput = null;
                    this.lineInputStream = null;
                } catch (Throwable th) {
                    this.serverSocket.close();
                    this.serverSocket = null;
                    this.serverOutput = null;
                    this.serverInput = null;
                    this.lineInputStream = null;
                    throw th;
                }
                if (this.logger.isLoggable(Level.FINE)) {
                    this.logger.fine("got bad greeting from host \"" + host2 + "\", port: " + port + ", response: " + failResponse);
                }
                throw new MessagingException("Got bad greeting from SMTP host: " + host2 + ", port: " + port + ", response: " + failResponse);
            } else if (this.logger.isLoggable(Level.FINE)) {
                this.logger.fine("connected to host \"" + host2 + "\", port: " + port);
            }
        } catch (UnknownHostException uhex) {
            throw new MessagingException("Unknown SMTP host: " + host2, uhex);
        } catch (SocketConnectException scex) {
            throw new MailConnectException(scex);
        } catch (IOException ioe) {
            throw new MessagingException("Could not connect to SMTP host: " + host2 + ", port: " + port, ioe);
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:20:0x008b=Splitter:B:20:0x008b, B:35:0x013d=Splitter:B:35:0x013d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void openServer() throws javax.mail.MessagingException {
        /*
            r8 = this;
            r2 = -1
            java.lang.String r5 = "UNKNOWN"
            r8.host = r5
            java.net.Socket r5 = r8.serverSocket     // Catch:{ IOException -> 0x0103 }
            int r2 = r5.getPort()     // Catch:{ IOException -> 0x0103 }
            java.net.Socket r5 = r8.serverSocket     // Catch:{ IOException -> 0x0103 }
            java.net.InetAddress r5 = r5.getInetAddress()     // Catch:{ IOException -> 0x0103 }
            java.lang.String r5 = r5.getHostName()     // Catch:{ IOException -> 0x0103 }
            r8.host = r5     // Catch:{ IOException -> 0x0103 }
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ IOException -> 0x0103 }
            java.util.logging.Level r6 = java.util.logging.Level.FINE     // Catch:{ IOException -> 0x0103 }
            boolean r5 = r5.isLoggable(r6)     // Catch:{ IOException -> 0x0103 }
            if (r5 == 0) goto L_0x0045
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0103 }
            r6.<init>()     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = "starting protocol to host \""
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = r8.host     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = "\", port "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r2)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r6 = r6.toString()     // Catch:{ IOException -> 0x0103 }
            r5.fine(r6)     // Catch:{ IOException -> 0x0103 }
        L_0x0045:
            r8.initStreams()     // Catch:{ IOException -> 0x0103 }
            r3 = -1
            int r3 = r8.readServerResponse()     // Catch:{ IOException -> 0x0103 }
            r5 = 220(0xdc, float:3.08E-43)
            if (r3 == r5) goto L_0x0163
            boolean r5 = r8.quitOnSessionReject     // Catch:{ Exception -> 0x0129 }
            if (r5 == 0) goto L_0x008b
            java.lang.String r5 = "QUIT"
            r8.sendCommand((java.lang.String) r5)     // Catch:{ Exception -> 0x0129 }
            boolean r5 = r8.quitWait     // Catch:{ Exception -> 0x0129 }
            if (r5 == 0) goto L_0x008b
            int r4 = r8.readServerResponse()     // Catch:{ Exception -> 0x0129 }
            r5 = 221(0xdd, float:3.1E-43)
            if (r4 == r5) goto L_0x008b
            r5 = -1
            if (r4 == r5) goto L_0x008b
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ Exception -> 0x0129 }
            java.util.logging.Level r6 = java.util.logging.Level.FINE     // Catch:{ Exception -> 0x0129 }
            boolean r5 = r5.isLoggable(r6)     // Catch:{ Exception -> 0x0129 }
            if (r5 == 0) goto L_0x008b
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ Exception -> 0x0129 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0129 }
            r6.<init>()     // Catch:{ Exception -> 0x0129 }
            java.lang.String r7 = "QUIT failed with "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ Exception -> 0x0129 }
            java.lang.StringBuilder r6 = r6.append(r4)     // Catch:{ Exception -> 0x0129 }
            java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x0129 }
            r5.fine(r6)     // Catch:{ Exception -> 0x0129 }
        L_0x008b:
            java.net.Socket r5 = r8.serverSocket     // Catch:{ IOException -> 0x0103 }
            r5.close()     // Catch:{ IOException -> 0x0103 }
            r5 = 0
            r8.serverSocket = r5     // Catch:{ IOException -> 0x0103 }
            r5 = 0
            r8.serverOutput = r5     // Catch:{ IOException -> 0x0103 }
            r5 = 0
            r8.serverInput = r5     // Catch:{ IOException -> 0x0103 }
            r5 = 0
            r8.lineInputStream = r5     // Catch:{ IOException -> 0x0103 }
        L_0x009c:
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ IOException -> 0x0103 }
            java.util.logging.Level r6 = java.util.logging.Level.FINE     // Catch:{ IOException -> 0x0103 }
            boolean r5 = r5.isLoggable(r6)     // Catch:{ IOException -> 0x0103 }
            if (r5 == 0) goto L_0x00d4
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0103 }
            r6.<init>()     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = "got bad greeting from host \""
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = r8.host     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = "\", port: "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r2)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = ", response: "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r3)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r6 = r6.toString()     // Catch:{ IOException -> 0x0103 }
            r5.fine(r6)     // Catch:{ IOException -> 0x0103 }
        L_0x00d4:
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0103 }
            r6.<init>()     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = "Got bad greeting from SMTP host: "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = r8.host     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = ", port: "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r2)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = ", response: "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r3)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r6 = r6.toString()     // Catch:{ IOException -> 0x0103 }
            r5.<init>(r6)     // Catch:{ IOException -> 0x0103 }
            throw r5     // Catch:{ IOException -> 0x0103 }
        L_0x0103:
            r1 = move-exception
            javax.mail.MessagingException r5 = new javax.mail.MessagingException
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "Could not start protocol to SMTP host: "
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r7 = r8.host
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r7 = ", port: "
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.StringBuilder r6 = r6.append(r2)
            java.lang.String r6 = r6.toString()
            r5.<init>(r6, r1)
            throw r5
        L_0x0129:
            r0 = move-exception
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ all -> 0x0150 }
            java.util.logging.Level r6 = java.util.logging.Level.FINE     // Catch:{ all -> 0x0150 }
            boolean r5 = r5.isLoggable(r6)     // Catch:{ all -> 0x0150 }
            if (r5 == 0) goto L_0x013d
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ all -> 0x0150 }
            java.util.logging.Level r6 = java.util.logging.Level.FINE     // Catch:{ all -> 0x0150 }
            java.lang.String r7 = "QUIT failed"
            r5.log((java.util.logging.Level) r6, (java.lang.String) r7, (java.lang.Throwable) r0)     // Catch:{ all -> 0x0150 }
        L_0x013d:
            java.net.Socket r5 = r8.serverSocket     // Catch:{ IOException -> 0x0103 }
            r5.close()     // Catch:{ IOException -> 0x0103 }
            r5 = 0
            r8.serverSocket = r5     // Catch:{ IOException -> 0x0103 }
            r5 = 0
            r8.serverOutput = r5     // Catch:{ IOException -> 0x0103 }
            r5 = 0
            r8.serverInput = r5     // Catch:{ IOException -> 0x0103 }
            r5 = 0
            r8.lineInputStream = r5     // Catch:{ IOException -> 0x0103 }
            goto L_0x009c
        L_0x0150:
            r5 = move-exception
            java.net.Socket r6 = r8.serverSocket     // Catch:{ IOException -> 0x0103 }
            r6.close()     // Catch:{ IOException -> 0x0103 }
            r6 = 0
            r8.serverSocket = r6     // Catch:{ IOException -> 0x0103 }
            r6 = 0
            r8.serverOutput = r6     // Catch:{ IOException -> 0x0103 }
            r6 = 0
            r8.serverInput = r6     // Catch:{ IOException -> 0x0103 }
            r6 = 0
            r8.lineInputStream = r6     // Catch:{ IOException -> 0x0103 }
            throw r5     // Catch:{ IOException -> 0x0103 }
        L_0x0163:
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ IOException -> 0x0103 }
            java.util.logging.Level r6 = java.util.logging.Level.FINE     // Catch:{ IOException -> 0x0103 }
            boolean r5 = r5.isLoggable(r6)     // Catch:{ IOException -> 0x0103 }
            if (r5 == 0) goto L_0x0191
            com.sun.mail.util.MailLogger r5 = r8.logger     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0103 }
            r6.<init>()     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = "protocol started to host \""
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = r8.host     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r7 = "\", port: "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ IOException -> 0x0103 }
            java.lang.StringBuilder r6 = r6.append(r2)     // Catch:{ IOException -> 0x0103 }
            java.lang.String r6 = r6.toString()     // Catch:{ IOException -> 0x0103 }
            r5.fine(r6)     // Catch:{ IOException -> 0x0103 }
        L_0x0191:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.openServer():void");
    }

    private void initStreams() throws IOException {
        boolean quote = PropUtil.getBooleanProperty(this.session.getProperties(), "mail.debug.quote", $assertionsDisabled);
        this.traceInput = new TraceInputStream(this.serverSocket.getInputStream(), this.traceLogger);
        this.traceInput.setQuote(quote);
        this.traceOutput = new TraceOutputStream(this.serverSocket.getOutputStream(), this.traceLogger);
        this.traceOutput.setQuote(quote);
        this.serverOutput = new BufferedOutputStream(this.traceOutput);
        this.serverInput = new BufferedInputStream(this.traceInput);
        this.lineInputStream = new LineInputStream(this.serverInput);
    }

    /* access modifiers changed from: private */
    public boolean isTracing() {
        return this.traceLogger.isLoggable(Level.FINEST);
    }

    /* access modifiers changed from: private */
    public void suspendTracing() {
        if (this.traceLogger.isLoggable(Level.FINEST)) {
            this.traceInput.setTrace($assertionsDisabled);
            this.traceOutput.setTrace($assertionsDisabled);
        }
    }

    /* access modifiers changed from: private */
    public void resumeTracing() {
        if (this.traceLogger.isLoggable(Level.FINEST)) {
            this.traceInput.setTrace(true);
            this.traceOutput.setTrace(true);
        }
    }

    public synchronized void issueCommand(String cmd, int expect) throws MessagingException {
        sendCommand(cmd);
        int resp = readServerResponse();
        if (expect != -1 && resp != expect) {
            throw new MessagingException(this.lastServerResponse);
        }
    }

    private void issueSendCommand(String cmd, int expect) throws MessagingException {
        int vul;
        sendCommand(cmd);
        int ret = readServerResponse();
        if (ret != expect) {
            int vsl = this.validSentAddr == null ? 0 : this.validSentAddr.length;
            if (this.validUnsentAddr == null) {
                vul = 0;
            } else {
                vul = this.validUnsentAddr.length;
            }
            Address[] valid = new Address[(vsl + vul)];
            if (vsl > 0) {
                System.arraycopy(this.validSentAddr, 0, valid, 0, vsl);
            }
            if (vul > 0) {
                System.arraycopy(this.validUnsentAddr, 0, valid, vsl, vul);
            }
            this.validSentAddr = null;
            this.validUnsentAddr = valid;
            if (this.logger.isLoggable(Level.FINE)) {
                this.logger.fine("got response code " + ret + ", with response: " + this.lastServerResponse);
            }
            String _lsr = this.lastServerResponse;
            int _lrc = this.lastReturnCode;
            if (this.serverSocket != null) {
                issueCommand("RSET", -1);
            }
            this.lastServerResponse = _lsr;
            this.lastReturnCode = _lrc;
            throw new SMTPSendFailedException(cmd, ret, this.lastServerResponse, this.exception, this.validSentAddr, this.validUnsentAddr, this.invalidAddr);
        }
    }

    public synchronized int simpleCommand(String cmd) throws MessagingException {
        sendCommand(cmd);
        return readServerResponse();
    }

    /* access modifiers changed from: protected */
    public int simpleCommand(byte[] cmd) throws MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            sendCommand(cmd);
            return readServerResponse();
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public void sendCommand(String cmd) throws MessagingException {
        sendCommand(toBytes(cmd));
    }

    private void sendCommand(byte[] cmdBytes) throws MessagingException {
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            try {
                this.serverOutput.write(cmdBytes);
                this.serverOutput.write(CRLF);
                this.serverOutput.flush();
            } catch (IOException ex) {
                throw new MessagingException("Can't send command to SMTP host", ex);
            }
        } else {
            throw new AssertionError();
        }
    }

    /* access modifiers changed from: protected */
    public int readServerResponse() throws MessagingException {
        String line;
        int returnCode;
        if ($assertionsDisabled || Thread.holdsLock(this)) {
            StringBuilder buf = new StringBuilder(100);
            do {
                try {
                    line = this.lineInputStream.readLine();
                    if (line == null) {
                        String serverResponse = buf.toString();
                        if (serverResponse.length() == 0) {
                            serverResponse = "[EOF]";
                        }
                        this.lastServerResponse = serverResponse;
                        this.lastReturnCode = -1;
                        this.logger.log(Level.FINE, "EOF: {0}", (Object) serverResponse);
                        return -1;
                    }
                    buf.append(line);
                    buf.append("\n");
                } catch (IOException ioex) {
                    this.logger.log(Level.FINE, "exception reading response", (Throwable) ioex);
                    this.lastServerResponse = "";
                    this.lastReturnCode = 0;
                    throw new MessagingException("Exception reading response", ioex);
                }
            } while (isNotLastLine(line));
            String serverResponse2 = buf.toString();
            if (serverResponse2.length() >= 3) {
                try {
                    returnCode = Integer.parseInt(serverResponse2.substring(0, 3));
                } catch (NumberFormatException e) {
                    try {
                        close();
                    } catch (MessagingException mex) {
                        this.logger.log(Level.FINE, "close failed", (Throwable) mex);
                    }
                    returnCode = -1;
                } catch (StringIndexOutOfBoundsException e2) {
                    try {
                        close();
                    } catch (MessagingException mex2) {
                        this.logger.log(Level.FINE, "close failed", (Throwable) mex2);
                    }
                    returnCode = -1;
                }
            } else {
                returnCode = -1;
            }
            if (returnCode == -1) {
                this.logger.log(Level.FINE, "bad server response: {0}", (Object) serverResponse2);
            }
            this.lastServerResponse = serverResponse2;
            this.lastReturnCode = returnCode;
            return returnCode;
        }
        throw new AssertionError();
    }

    /* access modifiers changed from: protected */
    public void checkConnected() {
        if (!super.isConnected()) {
            throw new IllegalStateException("Not connected");
        }
    }

    private boolean isNotLastLine(String line) {
        if (line == null || line.length() < 4 || line.charAt(3) != '-') {
            return $assertionsDisabled;
        }
        return true;
    }

    private String normalizeAddress(String addr) {
        if (addr.startsWith("<") || addr.endsWith(">")) {
            return addr;
        }
        return "<" + addr + ">";
    }

    public boolean supportsExtension(String ext) {
        if (this.extMap == null || this.extMap.get(ext.toUpperCase(Locale.ENGLISH)) == null) {
            return $assertionsDisabled;
        }
        return true;
    }

    public String getExtensionParameter(String ext) {
        if (this.extMap == null) {
            return null;
        }
        return this.extMap.get(ext.toUpperCase(Locale.ENGLISH));
    }

    /* access modifiers changed from: protected */
    public boolean supportsAuthentication(String auth) {
        String a;
        if (!$assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.extMap == null || (a = this.extMap.get("AUTH")) == null) {
            return $assertionsDisabled;
        } else {
            StringTokenizer st = new StringTokenizer(a);
            while (st.hasMoreTokens()) {
                if (st.nextToken().equalsIgnoreCase(auth)) {
                    return true;
                }
            }
            if (!auth.equalsIgnoreCase("LOGIN") || !supportsExtension("AUTH=LOGIN")) {
                return $assertionsDisabled;
            }
            this.logger.fine("use AUTH=LOGIN hack");
            return true;
        }
    }

    protected static String xtext(String s) {
        return xtext(s, $assertionsDisabled);
    }

    protected static String xtext(String s, boolean utf8) {
        byte[] bytes;
        StringBuilder sb = null;
        if (utf8) {
            bytes = s.getBytes(StandardCharsets.UTF_8);
        } else {
            bytes = ASCIIUtility.getBytes(s);
        }
        int i = 0;
        while (i < bytes.length) {
            char c = (char) (bytes[i] & Ev3Constants.Opcode.TST);
            if (utf8 || c < 128) {
                if (c < '!' || c > '~' || c == '+' || c == '=') {
                    if (sb == null) {
                        sb = new StringBuilder(s.length() + 4);
                        sb.append(s.substring(0, i));
                    }
                    sb.append('+');
                    sb.append(hexchar[(c & 240) >> 4]);
                    sb.append(hexchar[c & 15]);
                } else if (sb != null) {
                    sb.append(c);
                }
                i++;
            } else {
                throw new IllegalArgumentException("Non-ASCII character in SMTP submitter: " + s);
            }
        }
        return sb != null ? sb.toString() : s;
    }

    private String traceUser(String user) {
        return this.debugusername ? user : "<user name suppressed>";
    }

    private String tracePassword(String password) {
        if (this.debugpassword) {
            return password;
        }
        return password == null ? "<null>" : "<non-null>";
    }

    private byte[] toBytes(String s) {
        if (this.allowutf8) {
            return s.getBytes(StandardCharsets.UTF_8);
        }
        return ASCIIUtility.getBytes(s);
    }

    private void sendMessageStart(String subject) {
    }

    private void sendMessageEnd() {
    }

    private class BDATOutputStream extends SMTPOutputStream {
        public BDATOutputStream(OutputStream out, int size) {
            super(new ChunkedOutputStream(out, size));
        }

        public void close() throws IOException {
            this.out.close();
        }
    }

    private class ChunkedOutputStream extends OutputStream {
        private final byte[] buf;
        private int count = 0;
        private final OutputStream out;

        public ChunkedOutputStream(OutputStream out2, int size) {
            this.out = out2;
            this.buf = new byte[size];
        }

        public void write(int b) throws IOException {
            byte[] bArr = this.buf;
            int i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) b;
            if (this.count >= this.buf.length) {
                flush();
            }
        }

        public void write(byte[] b, int off, int len) throws IOException {
            while (len > 0) {
                int size = Math.min(this.buf.length - this.count, len);
                if (size == this.buf.length) {
                    bdat(b, off, size, SMTPTransport.$assertionsDisabled);
                } else {
                    System.arraycopy(b, off, this.buf, this.count, size);
                    this.count += size;
                }
                off += size;
                len -= size;
                if (this.count >= this.buf.length) {
                    flush();
                }
            }
        }

        public void flush() throws IOException {
            bdat(this.buf, 0, this.count, SMTPTransport.$assertionsDisabled);
            this.count = 0;
        }

        public void close() throws IOException {
            bdat(this.buf, 0, this.count, true);
            this.count = 0;
        }

        private void bdat(byte[] b, int off, int len, boolean last) throws IOException {
            if (len > 0 || last) {
                if (last) {
                    try {
                        SMTPTransport.this.sendCommand("BDAT " + len + " LAST");
                    } catch (MessagingException mex) {
                        throw new IOException("BDAT write exception", mex);
                    }
                } else {
                    SMTPTransport.this.sendCommand("BDAT " + len);
                }
                this.out.write(b, off, len);
                this.out.flush();
                if (SMTPTransport.this.readServerResponse() != 250) {
                    throw new IOException(SMTPTransport.this.lastServerResponse);
                }
            }
        }
    }
}
