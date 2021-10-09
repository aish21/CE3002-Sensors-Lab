package com.sun.mail.pop3;

import androidx.appcompat.widget.ActivityChooserView;
import com.sun.mail.auth.Ntlm;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SharedByteArrayOutputStream;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.net.ssl.SSLSocket;

class Protocol {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final String CRLF = "\r\n";
    private static final int POP3_PORT = 110;
    private static final int SLOP = 128;
    private static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private String apopChallenge = null;
    private Map<String, Authenticator> authenticators = new HashMap();
    private Map<String, String> capabilities = null;
    private String defaultAuthenticationMechanisms;
    private String host;
    private BufferedReader input;
    private String localHostName;
    /* access modifiers changed from: private */
    public MailLogger logger;
    /* access modifiers changed from: private */
    public boolean noauthdebug = true;
    private PrintWriter output;
    private boolean pipelining;
    /* access modifiers changed from: private */
    public String prefix;
    /* access modifiers changed from: private */
    public Properties props;
    private Socket socket;
    private TraceInputStream traceInput;
    private MailLogger traceLogger;
    private TraceOutputStream traceOutput;
    private boolean traceSuspended;

    static {
        boolean z;
        if (!Protocol.class.desiredAssertionStatus()) {
            z = true;
        } else {
            z = $assertionsDisabled;
        }
        $assertionsDisabled = z;
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0181  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x01bc A[LOOP:0: B:39:0x01b9->B:41:0x01bc, LOOP_END] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    Protocol(java.lang.String r18, int r19, com.sun.mail.util.MailLogger r20, java.util.Properties r21, java.lang.String r22, boolean r23) throws java.io.IOException {
        /*
            r17 = this;
            r17.<init>()
            r14 = 0
            r0 = r17
            r0.apopChallenge = r14
            r14 = 0
            r0 = r17
            r0.capabilities = r14
            r14 = 1
            r0 = r17
            r0.noauthdebug = r14
            java.util.HashMap r14 = new java.util.HashMap
            r14.<init>()
            r0 = r17
            r0.authenticators = r14
            r0 = r18
            r1 = r17
            r1.host = r0
            r0 = r21
            r1 = r17
            r1.props = r0
            r0 = r22
            r1 = r17
            r1.prefix = r0
            r0 = r20
            r1 = r17
            r1.logger = r0
            java.lang.String r14 = "protocol"
            r15 = 0
            r0 = r20
            com.sun.mail.util.MailLogger r14 = r0.getSubLogger(r14, r15)
            r0 = r17
            r0.traceLogger = r14
            java.lang.String r14 = "mail.debug.auth"
            r15 = 0
            r0 = r21
            boolean r14 = com.sun.mail.util.PropUtil.getBooleanProperty(r0, r14, r15)
            if (r14 != 0) goto L_0x00fd
            r14 = 1
        L_0x004c:
            r0 = r17
            r0.noauthdebug = r14
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r0 = r22
            java.lang.StringBuilder r14 = r14.append(r0)
            java.lang.String r15 = ".apop.enable"
            java.lang.StringBuilder r14 = r14.append(r15)
            java.lang.String r14 = r14.toString()
            r0 = r17
            r1 = r21
            boolean r9 = r0.getBoolProp(r1, r14)
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r0 = r22
            java.lang.StringBuilder r14 = r14.append(r0)
            java.lang.String r15 = ".disablecapa"
            java.lang.StringBuilder r14 = r14.append(r15)
            java.lang.String r14 = r14.toString()
            r0 = r17
            r1 = r21
            boolean r8 = r0.getBoolProp(r1, r14)
            r14 = -1
            r0 = r19
            if (r0 != r14) goto L_0x0091
            r19 = 110(0x6e, float:1.54E-43)
        L_0x0091:
            java.util.logging.Level r14 = java.util.logging.Level.FINE     // Catch:{ IOException -> 0x0100 }
            r0 = r20
            boolean r14 = r0.isLoggable(r14)     // Catch:{ IOException -> 0x0100 }
            if (r14 == 0) goto L_0x00cd
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0100 }
            r14.<init>()     // Catch:{ IOException -> 0x0100 }
            java.lang.String r15 = "connecting to host \""
            java.lang.StringBuilder r14 = r14.append(r15)     // Catch:{ IOException -> 0x0100 }
            r0 = r18
            java.lang.StringBuilder r14 = r14.append(r0)     // Catch:{ IOException -> 0x0100 }
            java.lang.String r15 = "\", port "
            java.lang.StringBuilder r14 = r14.append(r15)     // Catch:{ IOException -> 0x0100 }
            r0 = r19
            java.lang.StringBuilder r14 = r14.append(r0)     // Catch:{ IOException -> 0x0100 }
            java.lang.String r15 = ", isSSL "
            java.lang.StringBuilder r14 = r14.append(r15)     // Catch:{ IOException -> 0x0100 }
            r0 = r23
            java.lang.StringBuilder r14 = r14.append(r0)     // Catch:{ IOException -> 0x0100 }
            java.lang.String r14 = r14.toString()     // Catch:{ IOException -> 0x0100 }
            r0 = r20
            r0.fine(r14)     // Catch:{ IOException -> 0x0100 }
        L_0x00cd:
            r0 = r18
            r1 = r19
            r2 = r21
            r3 = r22
            r4 = r23
            java.net.Socket r14 = com.sun.mail.util.SocketFetcher.getSocket(r0, r1, r2, r3, r4)     // Catch:{ IOException -> 0x0100 }
            r0 = r17
            r0.socket = r14     // Catch:{ IOException -> 0x0100 }
            r17.initStreams()     // Catch:{ IOException -> 0x0100 }
            r14 = 0
            r0 = r17
            com.sun.mail.pop3.Response r12 = r0.simpleCommand(r14)     // Catch:{ IOException -> 0x0100 }
            boolean r14 = r12.f282ok
            if (r14 != 0) goto L_0x010a
            r0 = r17
            java.net.Socket r14 = r0.socket
            java.io.IOException r15 = new java.io.IOException
            java.lang.String r16 = "Connect failed"
            r15.<init>(r16)
            java.io.IOException r14 = cleanupAndThrow(r14, r15)
            throw r14
        L_0x00fd:
            r14 = 0
            goto L_0x004c
        L_0x0100:
            r11 = move-exception
            r0 = r17
            java.net.Socket r14 = r0.socket
            java.io.IOException r14 = cleanupAndThrow(r14, r11)
            throw r14
        L_0x010a:
            if (r9 == 0) goto L_0x0143
            java.lang.String r14 = r12.data
            if (r14 == 0) goto L_0x0143
            java.lang.String r14 = r12.data
            r15 = 60
            int r7 = r14.indexOf(r15)
            java.lang.String r14 = r12.data
            r15 = 62
            int r6 = r14.indexOf(r15, r7)
            r14 = -1
            if (r7 == r14) goto L_0x0132
            r14 = -1
            if (r6 == r14) goto L_0x0132
            java.lang.String r14 = r12.data
            int r15 = r6 + 1
            java.lang.String r14 = r14.substring(r7, r15)
            r0 = r17
            r0.apopChallenge = r14
        L_0x0132:
            java.util.logging.Level r14 = java.util.logging.Level.FINE
            java.lang.String r15 = "APOP challenge: {0}"
            r0 = r17
            java.lang.String r0 = r0.apopChallenge
            r16 = r0
            r0 = r20
            r1 = r16
            r0.log((java.util.logging.Level) r14, (java.lang.String) r15, (java.lang.Object) r1)
        L_0x0143:
            if (r8 != 0) goto L_0x014e
            java.io.InputStream r14 = r17.capa()
            r0 = r17
            r0.setCapabilities(r14)
        L_0x014e:
            java.lang.String r14 = "PIPELINING"
            r0 = r17
            boolean r14 = r0.hasCapability(r14)
            if (r14 != 0) goto L_0x0176
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r0 = r22
            java.lang.StringBuilder r14 = r14.append(r0)
            java.lang.String r15 = ".pipelining"
            java.lang.StringBuilder r14 = r14.append(r15)
            java.lang.String r14 = r14.toString()
            r15 = 0
            r0 = r21
            boolean r14 = com.sun.mail.util.PropUtil.getBooleanProperty(r0, r14, r15)
            if (r14 == 0) goto L_0x01dd
        L_0x0176:
            r14 = 1
        L_0x0177:
            r0 = r17
            r0.pipelining = r14
            r0 = r17
            boolean r14 = r0.pipelining
            if (r14 == 0) goto L_0x0188
            java.lang.String r14 = "PIPELINING enabled"
            r0 = r20
            r0.config(r14)
        L_0x0188:
            r14 = 4
            com.sun.mail.pop3.Protocol$Authenticator[] r5 = new com.sun.mail.pop3.Protocol.Authenticator[r14]
            r14 = 0
            com.sun.mail.pop3.Protocol$LoginAuthenticator r15 = new com.sun.mail.pop3.Protocol$LoginAuthenticator
            r0 = r17
            r15.<init>()
            r5[r14] = r15
            r14 = 1
            com.sun.mail.pop3.Protocol$PlainAuthenticator r15 = new com.sun.mail.pop3.Protocol$PlainAuthenticator
            r0 = r17
            r15.<init>()
            r5[r14] = r15
            r14 = 2
            com.sun.mail.pop3.Protocol$NtlmAuthenticator r15 = new com.sun.mail.pop3.Protocol$NtlmAuthenticator
            r0 = r17
            r15.<init>()
            r5[r14] = r15
            r14 = 3
            com.sun.mail.pop3.Protocol$OAuth2Authenticator r15 = new com.sun.mail.pop3.Protocol$OAuth2Authenticator
            r0 = r17
            r15.<init>()
            r5[r14] = r15
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            r10 = 0
        L_0x01b9:
            int r14 = r5.length
            if (r10 >= r14) goto L_0x01df
            r0 = r17
            java.util.Map<java.lang.String, com.sun.mail.pop3.Protocol$Authenticator> r14 = r0.authenticators
            r15 = r5[r10]
            java.lang.String r15 = r15.getMechanism()
            r16 = r5[r10]
            r14.put(r15, r16)
            r14 = r5[r10]
            java.lang.String r14 = r14.getMechanism()
            java.lang.StringBuilder r14 = r13.append(r14)
            r15 = 32
            r14.append(r15)
            int r10 = r10 + 1
            goto L_0x01b9
        L_0x01dd:
            r14 = 0
            goto L_0x0177
        L_0x01df:
            java.lang.String r14 = r13.toString()
            r0 = r17
            r0.defaultAuthenticationMechanisms = r14
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.Protocol.<init>(java.lang.String, int, com.sun.mail.util.MailLogger, java.util.Properties, java.lang.String, boolean):void");
    }

    private static IOException cleanupAndThrow(Socket socket2, IOException ife) {
        try {
            socket2.close();
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

    private static boolean isRecoverable(Throwable t) {
        if ((t instanceof Exception) || (t instanceof LinkageError)) {
            return true;
        }
        return $assertionsDisabled;
    }

    /* access modifiers changed from: private */
    public final synchronized boolean getBoolProp(Properties props2, String prop) {
        boolean val;
        val = PropUtil.getBooleanProperty(props2, prop, $assertionsDisabled);
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config(prop + ": " + val);
        }
        return val;
    }

    private void initStreams() throws IOException {
        boolean quote = PropUtil.getBooleanProperty(this.props, "mail.debug.quote", $assertionsDisabled);
        this.traceInput = new TraceInputStream(this.socket.getInputStream(), this.traceLogger);
        this.traceInput.setQuote(quote);
        this.traceOutput = new TraceOutputStream(this.socket.getOutputStream(), this.traceLogger);
        this.traceOutput.setQuote(quote);
        this.input = new BufferedReader(new InputStreamReader(this.traceInput, "iso-8859-1"));
        this.output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.traceOutput, "iso-8859-1")));
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.socket != null) {
                quit();
            }
        } finally {
            super.finalize();
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void setCapabilities(InputStream in) {
        if (in == null) {
            this.capabilities = null;
        } else {
            this.capabilities = new HashMap(10);
            BufferedReader r = null;
            try {
                r = new BufferedReader(new InputStreamReader(in, "us-ascii"));
            } catch (IOException e) {
                try {
                    in.close();
                } catch (IOException e2) {
                }
            } catch (UnsupportedEncodingException e3) {
                if (!$assertionsDisabled) {
                    throw new AssertionError();
                }
            } catch (Throwable th) {
                try {
                    in.close();
                } catch (IOException e4) {
                }
                throw th;
            }
            while (true) {
                String s = r.readLine();
                if (s != null) {
                    String cap = s;
                    int i = cap.indexOf(32);
                    if (i > 0) {
                        cap = cap.substring(0, i);
                    }
                    this.capabilities.put(cap.toUpperCase(Locale.ENGLISH), s);
                } else {
                    try {
                        break;
                    } catch (IOException e5) {
                    }
                }
            }
            in.close();
        }
        return;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean hasCapability(String c) {
        return (this.capabilities == null || !this.capabilities.containsKey(c.toUpperCase(Locale.ENGLISH))) ? $assertionsDisabled : true;
    }

    /* access modifiers changed from: package-private */
    public synchronized Map<String, String> getCapabilities() {
        return this.capabilities;
    }

    /* access modifiers changed from: package-private */
    public boolean supportsMechanism(String mech) {
        return this.authenticators.containsKey(mech.toUpperCase(Locale.ENGLISH));
    }

    /* access modifiers changed from: package-private */
    public String getDefaultMechanisms() {
        return this.defaultAuthenticationMechanisms;
    }

    /* access modifiers changed from: package-private */
    public boolean isMechanismEnabled(String mech) {
        Authenticator a = this.authenticators.get(mech.toUpperCase(Locale.ENGLISH));
        if (a == null || !a.enabled()) {
            return $assertionsDisabled;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public synchronized String authenticate(String mech, String host2, String authzid, String user, String passwd) {
        String message;
        Authenticator a = this.authenticators.get(mech.toUpperCase(Locale.ENGLISH));
        if (a == null) {
            message = "No such authentication mechanism: " + mech;
        } else {
            try {
                if (!a.authenticate(host2, authzid, user, passwd)) {
                    message = "login failed";
                } else {
                    message = null;
                }
            } catch (IOException ex) {
                message = ex.getMessage();
            }
        }
        return message;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean supportsAuthentication(String auth) {
        boolean z = true;
        synchronized (this) {
            if (!$assertionsDisabled && !Thread.holdsLock(this)) {
                throw new AssertionError();
            } else if (!auth.equals("LOGIN")) {
                if (this.capabilities != null) {
                    String a = this.capabilities.get("SASL");
                    if (a != null) {
                        StringTokenizer st = new StringTokenizer(a);
                        while (true) {
                            if (st.hasMoreTokens()) {
                                if (st.nextToken().equalsIgnoreCase(auth)) {
                                    break;
                                }
                            } else {
                                z = false;
                                break;
                            }
                        }
                    } else {
                        z = false;
                    }
                } else {
                    z = false;
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:33:0x0073=Splitter:B:33:0x0073, B:65:0x00fc=Splitter:B:65:0x00fc} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.lang.String login(java.lang.String r10, java.lang.String r11) throws java.io.IOException {
        /*
            r9 = this;
            monitor-enter(r9)
            boolean r5 = r9.pipelining     // Catch:{ all -> 0x00d8 }
            if (r5 == 0) goto L_0x0078
            java.net.Socket r5 = r9.socket     // Catch:{ all -> 0x00d8 }
            boolean r5 = r5 instanceof javax.net.ssl.SSLSocket     // Catch:{ all -> 0x00d8 }
            if (r5 == 0) goto L_0x0078
            r0 = 1
        L_0x000c:
            boolean r5 = r9.noauthdebug     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x0020
            boolean r5 = r9.isTracing()     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x0020
            com.sun.mail.util.MailLogger r5 = r9.logger     // Catch:{ all -> 0x00d3 }
            java.lang.String r6 = "authentication command trace suppressed"
            r5.fine(r6)     // Catch:{ all -> 0x00d3 }
            r9.suspendTracing()     // Catch:{ all -> 0x00d3 }
        L_0x0020:
            r2 = 0
            java.lang.String r5 = r9.apopChallenge     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x0029
            java.lang.String r2 = r9.getDigest(r11)     // Catch:{ all -> 0x00d3 }
        L_0x0029:
            java.lang.String r5 = r9.apopChallenge     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x007a
            if (r2 == 0) goto L_0x007a
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d3 }
            r5.<init>()     // Catch:{ all -> 0x00d3 }
            java.lang.String r6 = "APOP "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x00d3 }
            java.lang.StringBuilder r5 = r5.append(r10)     // Catch:{ all -> 0x00d3 }
            java.lang.String r6 = " "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x00d3 }
            java.lang.StringBuilder r5 = r5.append(r2)     // Catch:{ all -> 0x00d3 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00d3 }
            com.sun.mail.pop3.Response r4 = r9.simpleCommand(r5)     // Catch:{ all -> 0x00d3 }
        L_0x0050:
            boolean r5 = r9.noauthdebug     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x0069
            boolean r5 = r9.isTracing()     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x0069
            com.sun.mail.util.MailLogger r6 = r9.logger     // Catch:{ all -> 0x00d3 }
            java.util.logging.Level r7 = java.util.logging.Level.FINE     // Catch:{ all -> 0x00d3 }
            java.lang.String r8 = "authentication command {0}"
            boolean r5 = r4.f282ok     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x011d
            java.lang.String r5 = "succeeded"
        L_0x0066:
            r6.log((java.util.logging.Level) r7, (java.lang.String) r8, (java.lang.Object) r5)     // Catch:{ all -> 0x00d3 }
        L_0x0069:
            boolean r5 = r4.f282ok     // Catch:{ all -> 0x00d3 }
            if (r5 != 0) goto L_0x0125
            java.lang.String r5 = r4.data     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x0121
            java.lang.String r5 = r4.data     // Catch:{ all -> 0x00d3 }
        L_0x0073:
            r9.resumeTracing()     // Catch:{ all -> 0x00d8 }
        L_0x0076:
            monitor-exit(r9)
            return r5
        L_0x0078:
            r0 = 0
            goto L_0x000c
        L_0x007a:
            if (r0 == 0) goto L_0x00db
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d3 }
            r5.<init>()     // Catch:{ all -> 0x00d3 }
            java.lang.String r6 = "USER "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x00d3 }
            java.lang.StringBuilder r5 = r5.append(r10)     // Catch:{ all -> 0x00d3 }
            java.lang.String r1 = r5.toString()     // Catch:{ all -> 0x00d3 }
            r9.batchCommandStart(r1)     // Catch:{ all -> 0x00d3 }
            r9.issueCommand(r1)     // Catch:{ all -> 0x00d3 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d3 }
            r5.<init>()     // Catch:{ all -> 0x00d3 }
            java.lang.String r6 = "PASS "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x00d3 }
            java.lang.StringBuilder r5 = r5.append(r11)     // Catch:{ all -> 0x00d3 }
            java.lang.String r1 = r5.toString()     // Catch:{ all -> 0x00d3 }
            r9.batchCommandContinue(r1)     // Catch:{ all -> 0x00d3 }
            r9.issueCommand(r1)     // Catch:{ all -> 0x00d3 }
            com.sun.mail.pop3.Response r4 = r9.readResponse()     // Catch:{ all -> 0x00d3 }
            boolean r5 = r4.f282ok     // Catch:{ all -> 0x00d3 }
            if (r5 != 0) goto L_0x00ca
            java.lang.String r5 = r4.data     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x00c7
            java.lang.String r3 = r4.data     // Catch:{ all -> 0x00d3 }
        L_0x00bc:
            r9.readResponse()     // Catch:{ all -> 0x00d3 }
            r9.batchCommandEnd()     // Catch:{ all -> 0x00d3 }
            r9.resumeTracing()     // Catch:{ all -> 0x00d8 }
            r5 = r3
            goto L_0x0076
        L_0x00c7:
            java.lang.String r3 = "USER command failed"
            goto L_0x00bc
        L_0x00ca:
            com.sun.mail.pop3.Response r4 = r9.readResponse()     // Catch:{ all -> 0x00d3 }
            r9.batchCommandEnd()     // Catch:{ all -> 0x00d3 }
            goto L_0x0050
        L_0x00d3:
            r5 = move-exception
            r9.resumeTracing()     // Catch:{ all -> 0x00d8 }
            throw r5     // Catch:{ all -> 0x00d8 }
        L_0x00d8:
            r5 = move-exception
            monitor-exit(r9)
            throw r5
        L_0x00db:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d3 }
            r5.<init>()     // Catch:{ all -> 0x00d3 }
            java.lang.String r6 = "USER "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x00d3 }
            java.lang.StringBuilder r5 = r5.append(r10)     // Catch:{ all -> 0x00d3 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00d3 }
            com.sun.mail.pop3.Response r4 = r9.simpleCommand(r5)     // Catch:{ all -> 0x00d3 }
            boolean r5 = r4.f282ok     // Catch:{ all -> 0x00d3 }
            if (r5 != 0) goto L_0x0104
            java.lang.String r5 = r4.data     // Catch:{ all -> 0x00d3 }
            if (r5 == 0) goto L_0x0101
            java.lang.String r5 = r4.data     // Catch:{ all -> 0x00d3 }
        L_0x00fc:
            r9.resumeTracing()     // Catch:{ all -> 0x00d8 }
            goto L_0x0076
        L_0x0101:
            java.lang.String r5 = "USER command failed"
            goto L_0x00fc
        L_0x0104:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00d3 }
            r5.<init>()     // Catch:{ all -> 0x00d3 }
            java.lang.String r6 = "PASS "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x00d3 }
            java.lang.StringBuilder r5 = r5.append(r11)     // Catch:{ all -> 0x00d3 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00d3 }
            com.sun.mail.pop3.Response r4 = r9.simpleCommand(r5)     // Catch:{ all -> 0x00d3 }
            goto L_0x0050
        L_0x011d:
            java.lang.String r5 = "failed"
            goto L_0x0066
        L_0x0121:
            java.lang.String r5 = "login failed"
            goto L_0x0073
        L_0x0125:
            r5 = 0
            r9.resumeTracing()     // Catch:{ all -> 0x00d8 }
            goto L_0x0076
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.Protocol.login(java.lang.String, java.lang.String):java.lang.String");
    }

    private String getDigest(String password) {
        try {
            return toHex(MessageDigest.getInstance("MD5").digest((this.apopChallenge + password).getBytes("iso-8859-1")));
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e2) {
            return null;
        }
    }

    private abstract class Authenticator {
        static final /* synthetic */ boolean $assertionsDisabled = (!Protocol.class.desiredAssertionStatus() ? true : Protocol.$assertionsDisabled);
        private final boolean enabled;
        private final String mech;
        protected Response resp;

        /* access modifiers changed from: package-private */
        public abstract void doAuth(String str, String str2, String str3, String str4) throws IOException;

        Authenticator(Protocol protocol, String mech2) {
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

        /* access modifiers changed from: protected */
        public void runAuthenticationCommand(String command, String ir) throws IOException {
            if (Protocol.this.logger.isLoggable(Level.FINE)) {
                Protocol.this.logger.fine(command + " using one line authentication format");
            }
            if (ir != null) {
                Protocol protocol = Protocol.this;
                StringBuilder append = new StringBuilder().append(command).append(" ");
                if (ir.length() == 0) {
                    ir = "=";
                }
                this.resp = protocol.simpleCommand(append.append(ir).toString());
                return;
            }
            this.resp = Protocol.this.simpleCommand(command);
        }

        /* access modifiers changed from: package-private */
        public boolean authenticate(String host, String authzid, String user, String passwd) throws IOException {
            try {
                String ir = getInitialResponse(host, authzid, user, passwd);
                if (Protocol.this.noauthdebug && Protocol.this.isTracing()) {
                    Protocol.this.logger.fine("AUTH " + this.mech + " command trace suppressed");
                    Protocol.this.suspendTracing();
                }
                runAuthenticationCommand("AUTH " + this.mech, ir);
                if (this.resp.cont) {
                    doAuth(host, authzid, user, passwd);
                }
                if (Protocol.this.noauthdebug && Protocol.this.isTracing()) {
                    Protocol.this.logger.fine("AUTH " + this.mech + " " + (this.resp.f282ok ? "succeeded" : "failed"));
                }
                Protocol.this.resumeTracing();
                if (this.resp.f282ok) {
                    return true;
                }
                Protocol.this.close();
                if (0 != 0) {
                    if (0 instanceof Error) {
                        throw ((Error) null);
                    } else if (0 instanceof Exception) {
                        EOFException ex = new EOFException(this.resp.data != null ? this.resp.data : "authentication failed");
                        ex.initCause((Throwable) null);
                        throw ex;
                    } else if (!$assertionsDisabled) {
                        throw new AssertionError("unknown Throwable");
                    }
                }
                throw new EOFException(this.resp.data != null ? this.resp.data : "authentication failed");
            } catch (IOException ex2) {
                Protocol.this.logger.log(Level.FINE, "AUTH " + this.mech + " failed", (Throwable) ex2);
                if (Protocol.this.noauthdebug && Protocol.this.isTracing()) {
                    Protocol.this.logger.fine("AUTH " + this.mech + " " + (this.resp.f282ok ? "succeeded" : "failed"));
                }
                Protocol.this.resumeTracing();
                if (this.resp.f282ok) {
                    return true;
                }
                Protocol.this.close();
                if (0 != 0) {
                    if (0 instanceof Error) {
                        throw ((Error) null);
                    } else if (0 instanceof Exception) {
                        EOFException ex3 = new EOFException(this.resp.data != null ? this.resp.data : "authentication failed");
                        ex3.initCause((Throwable) null);
                        throw ex3;
                    } else if (!$assertionsDisabled) {
                        throw new AssertionError("unknown Throwable");
                    }
                }
                throw new EOFException(this.resp.data != null ? this.resp.data : "authentication failed");
            } catch (Throwable th) {
                Throwable th2 = th;
                if (Protocol.this.noauthdebug && Protocol.this.isTracing()) {
                    Protocol.this.logger.fine("AUTH " + this.mech + " " + (this.resp.f282ok ? "succeeded" : "failed"));
                }
                Protocol.this.resumeTracing();
                if (!this.resp.f282ok) {
                    Protocol.this.close();
                    if (0 != 0) {
                        if (0 instanceof Error) {
                            throw ((Error) null);
                        } else if (0 instanceof Exception) {
                            EOFException ex4 = new EOFException(this.resp.data != null ? this.resp.data : "authentication failed");
                            ex4.initCause((Throwable) null);
                            throw ex4;
                        } else if (!$assertionsDisabled) {
                            throw new AssertionError("unknown Throwable");
                        }
                    }
                    throw new EOFException(this.resp.data != null ? this.resp.data : "authentication failed");
                }
                throw th2;
            }
        }

        /* access modifiers changed from: package-private */
        public String getInitialResponse(String host, String authzid, String user, String passwd) throws IOException {
            return null;
        }
    }

    private class LoginAuthenticator extends Authenticator {
        LoginAuthenticator() {
            super(Protocol.this, "LOGIN");
        }

        /* access modifiers changed from: package-private */
        public boolean authenticate(String host, String authzid, String user, String passwd) throws IOException {
            String msg = Protocol.this.login(user, passwd);
            if (msg == null) {
                return true;
            }
            throw new EOFException(msg);
        }

        /* access modifiers changed from: package-private */
        public void doAuth(String host, String authzid, String user, String passwd) throws IOException {
            throw new EOFException("LOGIN asked for more");
        }
    }

    private class PlainAuthenticator extends Authenticator {
        PlainAuthenticator() {
            super(Protocol.this, "PLAIN");
        }

        /* access modifiers changed from: package-private */
        public String getInitialResponse(String host, String authzid, String user, String passwd) throws IOException {
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
        public void doAuth(String host, String authzid, String user, String passwd) throws IOException {
            throw new EOFException("PLAIN asked for more");
        }
    }

    private class NtlmAuthenticator extends Authenticator {
        static final /* synthetic */ boolean $assertionsDisabled = (!Protocol.class.desiredAssertionStatus() ? true : Protocol.$assertionsDisabled);
        private Ntlm ntlm;

        NtlmAuthenticator() {
            super(Protocol.this, "NTLM");
        }

        /* access modifiers changed from: package-private */
        public String getInitialResponse(String host, String authzid, String user, String passwd) throws IOException {
            this.ntlm = new Ntlm(Protocol.this.props.getProperty(Protocol.this.prefix + ".auth.ntlm.domain"), Protocol.this.getLocalHost(), user, passwd, Protocol.this.logger);
            return this.ntlm.generateType1Msg(PropUtil.getIntProperty(Protocol.this.props, Protocol.this.prefix + ".auth.ntlm.flags", 0), PropUtil.getBooleanProperty(Protocol.this.props, Protocol.this.prefix + ".auth.ntlm.v2", true));
        }

        /* access modifiers changed from: package-private */
        public void doAuth(String host, String authzid, String user, String passwd) throws IOException {
            if ($assertionsDisabled || this.ntlm != null) {
                this.resp = Protocol.this.simpleCommand(this.ntlm.generateType3Msg(this.resp.data.substring(4).trim()));
                return;
            }
            throw new AssertionError();
        }
    }

    private class OAuth2Authenticator extends Authenticator {
        OAuth2Authenticator() {
            super("XOAUTH2", Protocol.$assertionsDisabled);
        }

        /* access modifiers changed from: package-private */
        public String getInitialResponse(String host, String authzid, String user, String passwd) throws IOException {
            return ASCIIUtility.toString(BASE64EncoderStream.encode(("user=" + user + "\u0001auth=Bearer " + passwd + "\u0001\u0001").getBytes(StandardCharsets.UTF_8)));
        }

        /* access modifiers changed from: protected */
        public void runAuthenticationCommand(String command, String ir) throws IOException {
            if (Boolean.valueOf(Protocol.this.getBoolProp(Protocol.this.props, Protocol.this.prefix + ".auth.xoauth2.two.line.authentication.format")).booleanValue()) {
                if (Protocol.this.logger.isLoggable(Level.FINE)) {
                    Protocol.this.logger.fine(command + " using two line authentication format");
                }
                Protocol protocol = Protocol.this;
                if (ir.length() == 0) {
                    ir = "=";
                }
                this.resp = protocol.twoLinesCommand(command, ir);
                return;
            }
            super.runAuthenticationCommand(command, ir);
        }

        /* access modifiers changed from: package-private */
        public void doAuth(String host, String authzid, String user, String passwd) throws IOException {
            String err = "";
            if (this.resp.data != null) {
                err = new String(BASE64DecoderStream.decode(this.resp.data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            }
            throw new EOFException("OAUTH2 authentication failed: " + err);
        }
    }

    /* access modifiers changed from: private */
    public synchronized String getLocalHost() {
        try {
            if (this.localHostName == null || this.localHostName.length() == 0) {
                InetAddress localHost = InetAddress.getLocalHost();
                this.localHostName = localHost.getCanonicalHostName();
                if (this.localHostName == null) {
                    this.localHostName = "[" + localHost.getHostAddress() + "]";
                }
            }
        } catch (UnknownHostException e) {
        }
        if ((this.localHostName == null || this.localHostName.length() <= 0) && this.socket != null && this.socket.isBound()) {
            InetAddress localHost2 = this.socket.getLocalAddress();
            this.localHostName = localHost2.getCanonicalHostName();
            if (this.localHostName == null) {
                this.localHostName = "[" + localHost2.getHostAddress() + "]";
            }
        }
        return this.localHostName;
    }

    private static String toHex(byte[] bytes) {
        char[] result = new char[(bytes.length * 2)];
        int i = 0;
        for (byte b : bytes) {
            int temp = b & 255;
            int i2 = i + 1;
            result[i] = digits[temp >> 4];
            i = i2 + 1;
            result[i2] = digits[temp & 15];
        }
        return new String(result);
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean quit() throws IOException {
        boolean ok;
        try {
            ok = simpleCommand("QUIT").f282ok;
            close();
        } catch (Throwable th) {
            close();
            throw th;
        }
        return ok;
    }

    /* access modifiers changed from: package-private */
    public void close() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
        } finally {
            this.socket = null;
            this.input = null;
            this.output = null;
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized Status stat() throws IOException {
        Status s;
        Response r = simpleCommand("STAT");
        s = new Status();
        if (!r.f282ok) {
            throw new IOException("STAT command failed: " + r.data);
        } else if (r.data != null) {
            try {
                StringTokenizer st = new StringTokenizer(r.data);
                s.total = Integer.parseInt(st.nextToken());
                s.size = Integer.parseInt(st.nextToken());
            } catch (RuntimeException e) {
            }
        }
        return s;
    }

    /* access modifiers changed from: package-private */
    public synchronized int list(int msg) throws IOException {
        int size;
        Response r = simpleCommand("LIST " + msg);
        size = -1;
        if (r.f282ok && r.data != null) {
            try {
                StringTokenizer st = new StringTokenizer(r.data);
                st.nextToken();
                size = Integer.parseInt(st.nextToken());
            } catch (RuntimeException e) {
            }
        }
        return size;
    }

    /* access modifiers changed from: package-private */
    public synchronized InputStream list() throws IOException {
        return multilineCommand("LIST", 128).bytes;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ce A[SYNTHETIC, Splitter:B:37:0x00ce] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x000c  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:17:0x0061=Splitter:B:17:0x0061, B:51:0x011a=Splitter:B:51:0x011a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.io.InputStream retr(int r10, int r11) throws java.io.IOException {
        /*
            r9 = this;
            r8 = 1073741824(0x40000000, float:2.0)
            monitor-enter(r9)
            if (r11 != 0) goto L_0x00a6
            boolean r6 = r9.pipelining     // Catch:{ all -> 0x0125 }
            if (r6 == 0) goto L_0x00a6
            r0 = 1
        L_0x000a:
            if (r0 == 0) goto L_0x00ce
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0125 }
            r6.<init>()     // Catch:{ all -> 0x0125 }
            java.lang.String r7 = "LIST "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0125 }
            java.lang.StringBuilder r6 = r6.append(r10)     // Catch:{ all -> 0x0125 }
            java.lang.String r1 = r6.toString()     // Catch:{ all -> 0x0125 }
            r9.batchCommandStart(r1)     // Catch:{ all -> 0x0125 }
            r9.issueCommand(r1)     // Catch:{ all -> 0x0125 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0125 }
            r6.<init>()     // Catch:{ all -> 0x0125 }
            java.lang.String r7 = "RETR "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0125 }
            java.lang.StringBuilder r6 = r6.append(r10)     // Catch:{ all -> 0x0125 }
            java.lang.String r1 = r6.toString()     // Catch:{ all -> 0x0125 }
            r9.batchCommandContinue(r1)     // Catch:{ all -> 0x0125 }
            r9.issueCommand(r1)     // Catch:{ all -> 0x0125 }
            com.sun.mail.pop3.Response r3 = r9.readResponse()     // Catch:{ all -> 0x0125 }
            boolean r6 = r3.f282ok     // Catch:{ all -> 0x0125 }
            if (r6 == 0) goto L_0x0061
            java.lang.String r6 = r3.data     // Catch:{ all -> 0x0125 }
            if (r6 == 0) goto L_0x0061
            java.util.StringTokenizer r5 = new java.util.StringTokenizer     // Catch:{ RuntimeException -> 0x014f }
            java.lang.String r6 = r3.data     // Catch:{ RuntimeException -> 0x014f }
            r5.<init>(r6)     // Catch:{ RuntimeException -> 0x014f }
            r5.nextToken()     // Catch:{ RuntimeException -> 0x014f }
            java.lang.String r6 = r5.nextToken()     // Catch:{ RuntimeException -> 0x014f }
            int r11 = java.lang.Integer.parseInt(r6)     // Catch:{ RuntimeException -> 0x014f }
            if (r11 > r8) goto L_0x0060
            if (r11 >= 0) goto L_0x00a9
        L_0x0060:
            r11 = 0
        L_0x0061:
            com.sun.mail.pop3.Response r3 = r9.readResponse()     // Catch:{ all -> 0x0125 }
            boolean r6 = r3.f282ok     // Catch:{ all -> 0x0125 }
            if (r6 == 0) goto L_0x0071
            int r6 = r11 + 128
            java.io.InputStream r6 = r9.readMultilineResponse(r6)     // Catch:{ all -> 0x0125 }
            r3.bytes = r6     // Catch:{ all -> 0x0125 }
        L_0x0071:
            r9.batchCommandEnd()     // Catch:{ all -> 0x0125 }
        L_0x0074:
            boolean r6 = r3.f282ok     // Catch:{ all -> 0x0125 }
            if (r6 == 0) goto L_0x00a2
            if (r11 <= 0) goto L_0x00a2
            com.sun.mail.util.MailLogger r6 = r9.logger     // Catch:{ all -> 0x0125 }
            java.util.logging.Level r7 = java.util.logging.Level.FINE     // Catch:{ all -> 0x0125 }
            boolean r6 = r6.isLoggable(r7)     // Catch:{ all -> 0x0125 }
            if (r6 == 0) goto L_0x00a2
            com.sun.mail.util.MailLogger r6 = r9.logger     // Catch:{ all -> 0x0125 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x0125 }
            r7.<init>()     // Catch:{ all -> 0x0125 }
            java.lang.String r8 = "got message size "
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ all -> 0x0125 }
            java.io.InputStream r8 = r3.bytes     // Catch:{ all -> 0x0125 }
            int r8 = r8.available()     // Catch:{ all -> 0x0125 }
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ all -> 0x0125 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x0125 }
            r6.fine(r7)     // Catch:{ all -> 0x0125 }
        L_0x00a2:
            java.io.InputStream r6 = r3.bytes     // Catch:{ all -> 0x0125 }
        L_0x00a4:
            monitor-exit(r9)
            return r6
        L_0x00a6:
            r0 = 0
            goto L_0x000a
        L_0x00a9:
            com.sun.mail.util.MailLogger r6 = r9.logger     // Catch:{ RuntimeException -> 0x014f }
            java.util.logging.Level r7 = java.util.logging.Level.FINE     // Catch:{ RuntimeException -> 0x014f }
            boolean r6 = r6.isLoggable(r7)     // Catch:{ RuntimeException -> 0x014f }
            if (r6 == 0) goto L_0x00cb
            com.sun.mail.util.MailLogger r6 = r9.logger     // Catch:{ RuntimeException -> 0x014f }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x014f }
            r7.<init>()     // Catch:{ RuntimeException -> 0x014f }
            java.lang.String r8 = "pipeline message size "
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ RuntimeException -> 0x014f }
            java.lang.StringBuilder r7 = r7.append(r11)     // Catch:{ RuntimeException -> 0x014f }
            java.lang.String r7 = r7.toString()     // Catch:{ RuntimeException -> 0x014f }
            r6.fine(r7)     // Catch:{ RuntimeException -> 0x014f }
        L_0x00cb:
            int r11 = r11 + 128
            goto L_0x0061
        L_0x00ce:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0125 }
            r6.<init>()     // Catch:{ all -> 0x0125 }
            java.lang.String r7 = "RETR "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0125 }
            java.lang.StringBuilder r6 = r6.append(r10)     // Catch:{ all -> 0x0125 }
            java.lang.String r1 = r6.toString()     // Catch:{ all -> 0x0125 }
            r9.multilineCommandStart(r1)     // Catch:{ all -> 0x0125 }
            r9.issueCommand(r1)     // Catch:{ all -> 0x0125 }
            com.sun.mail.pop3.Response r3 = r9.readResponse()     // Catch:{ all -> 0x0125 }
            boolean r6 = r3.f282ok     // Catch:{ all -> 0x0125 }
            if (r6 != 0) goto L_0x00f4
            r9.multilineCommandEnd()     // Catch:{ all -> 0x0125 }
            r6 = 0
            goto L_0x00a4
        L_0x00f4:
            if (r11 > 0) goto L_0x011a
            java.lang.String r6 = r3.data     // Catch:{ all -> 0x0125 }
            if (r6 == 0) goto L_0x011a
            java.util.StringTokenizer r5 = new java.util.StringTokenizer     // Catch:{ RuntimeException -> 0x014d }
            java.lang.String r6 = r3.data     // Catch:{ RuntimeException -> 0x014d }
            r5.<init>(r6)     // Catch:{ RuntimeException -> 0x014d }
            java.lang.String r4 = r5.nextToken()     // Catch:{ RuntimeException -> 0x014d }
            java.lang.String r2 = r5.nextToken()     // Catch:{ RuntimeException -> 0x014d }
            java.lang.String r6 = "octets"
            boolean r6 = r2.equals(r6)     // Catch:{ RuntimeException -> 0x014d }
            if (r6 == 0) goto L_0x011a
            int r11 = java.lang.Integer.parseInt(r4)     // Catch:{ RuntimeException -> 0x014d }
            if (r11 > r8) goto L_0x0119
            if (r11 >= 0) goto L_0x0128
        L_0x0119:
            r11 = 0
        L_0x011a:
            java.io.InputStream r6 = r9.readMultilineResponse(r11)     // Catch:{ all -> 0x0125 }
            r3.bytes = r6     // Catch:{ all -> 0x0125 }
            r9.multilineCommandEnd()     // Catch:{ all -> 0x0125 }
            goto L_0x0074
        L_0x0125:
            r6 = move-exception
            monitor-exit(r9)
            throw r6
        L_0x0128:
            com.sun.mail.util.MailLogger r6 = r9.logger     // Catch:{ RuntimeException -> 0x014d }
            java.util.logging.Level r7 = java.util.logging.Level.FINE     // Catch:{ RuntimeException -> 0x014d }
            boolean r6 = r6.isLoggable(r7)     // Catch:{ RuntimeException -> 0x014d }
            if (r6 == 0) goto L_0x014a
            com.sun.mail.util.MailLogger r6 = r9.logger     // Catch:{ RuntimeException -> 0x014d }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ RuntimeException -> 0x014d }
            r7.<init>()     // Catch:{ RuntimeException -> 0x014d }
            java.lang.String r8 = "guessing message size: "
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ RuntimeException -> 0x014d }
            java.lang.StringBuilder r7 = r7.append(r11)     // Catch:{ RuntimeException -> 0x014d }
            java.lang.String r7 = r7.toString()     // Catch:{ RuntimeException -> 0x014d }
            r6.fine(r7)     // Catch:{ RuntimeException -> 0x014d }
        L_0x014a:
            int r11 = r11 + 128
            goto L_0x011a
        L_0x014d:
            r6 = move-exception
            goto L_0x011a
        L_0x014f:
            r6 = move-exception
            goto L_0x0061
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.Protocol.retr(int, int):java.io.InputStream");
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean retr(int msg, OutputStream os) throws IOException {
        int b;
        boolean z;
        String cmd = "RETR " + msg;
        multilineCommandStart(cmd);
        issueCommand(cmd);
        if (!readResponse().f282ok) {
            multilineCommandEnd();
            z = $assertionsDisabled;
        } else {
            Throwable terr = null;
            int lastb = 10;
            while (true) {
                try {
                    b = this.input.read();
                    if (b >= 0) {
                        if (lastb == 10 && b == 46 && (b = this.input.read()) == 13) {
                            b = this.input.read();
                            break;
                        }
                        if (terr == null) {
                            try {
                                os.write(b);
                            } catch (IOException ex) {
                                this.logger.log(Level.FINE, "exception while streaming", ex);
                                terr = ex;
                            } catch (RuntimeException ex2) {
                                this.logger.log(Level.FINE, "exception while streaming", ex2);
                                terr = ex2;
                            }
                        }
                        lastb = b;
                    } else {
                        break;
                    }
                } catch (InterruptedIOException iioex) {
                    try {
                        this.socket.close();
                    } catch (IOException e) {
                    }
                    throw iioex;
                }
            }
            if (b < 0) {
                throw new EOFException("EOF on socket");
            }
            if (terr != null) {
                if (terr instanceof IOException) {
                    throw ((IOException) terr);
                } else if (terr instanceof RuntimeException) {
                    throw ((RuntimeException) terr);
                } else if (!$assertionsDisabled) {
                    throw new AssertionError();
                }
            }
            multilineCommandEnd();
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public synchronized InputStream top(int msg, int n) throws IOException {
        return multilineCommand("TOP " + msg + " " + n, 0).bytes;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean dele(int msg) throws IOException {
        return simpleCommand("DELE " + msg).f282ok;
    }

    /* access modifiers changed from: package-private */
    public synchronized String uidl(int msg) throws IOException {
        String str = null;
        synchronized (this) {
            Response r = simpleCommand("UIDL " + msg);
            if (r.f282ok) {
                int i = r.data.indexOf(32);
                if (i > 0) {
                    str = r.data.substring(i + 1);
                }
            }
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean uidl(String[] uids) throws IOException {
        int n;
        boolean z = $assertionsDisabled;
        synchronized (this) {
            Response r = multilineCommand("UIDL", uids.length * 15);
            if (r.f282ok) {
                LineInputStream lis = new LineInputStream(r.bytes);
                while (true) {
                    String line = lis.readLine();
                    if (line != null) {
                        int i = line.indexOf(32);
                        if (i >= 1 && i < line.length() && (n = Integer.parseInt(line.substring(0, i))) > 0 && n <= uids.length) {
                            uids[n - 1] = line.substring(i + 1);
                        }
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                        }
                    }
                }
                r.bytes.close();
                z = true;
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean noop() throws IOException {
        return simpleCommand("NOOP").f282ok;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean rset() throws IOException {
        return simpleCommand("RSET").f282ok;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean stls() throws IOException {
        boolean z;
        if (this.socket instanceof SSLSocket) {
            z = true;
        } else {
            Response r = simpleCommand("STLS");
            if (r.f282ok) {
                try {
                    this.socket = SocketFetcher.startTLS(this.socket, this.host, this.props, this.prefix);
                    initStreams();
                } catch (IOException ioex) {
                    this.socket.close();
                    this.socket = null;
                    this.input = null;
                    this.output = null;
                    IOException sioex = new IOException("Could not convert socket to TLS");
                    sioex.initCause(ioex);
                    throw sioex;
                } catch (Throwable th) {
                    this.socket = null;
                    this.input = null;
                    this.output = null;
                    throw th;
                }
            }
            z = r.f282ok;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean isSSL() {
        return this.socket instanceof SSLSocket;
    }

    /* access modifiers changed from: package-private */
    public synchronized InputStream capa() throws IOException {
        InputStream inputStream;
        Response r = multilineCommand("CAPA", 128);
        if (!r.f282ok) {
            inputStream = null;
        } else {
            inputStream = r.bytes;
        }
        return inputStream;
    }

    /* access modifiers changed from: private */
    public Response simpleCommand(String cmd) throws IOException {
        simpleCommandStart(cmd);
        issueCommand(cmd);
        Response r = readResponse();
        simpleCommandEnd();
        return r;
    }

    /* access modifiers changed from: private */
    public Response twoLinesCommand(String firstCommand, String secondCommand) throws IOException {
        String cmd = firstCommand + " " + secondCommand;
        batchCommandStart(cmd);
        simpleCommand(firstCommand);
        batchCommandContinue(cmd);
        Response r = simpleCommand(secondCommand);
        batchCommandEnd();
        return r;
    }

    private void issueCommand(String cmd) throws IOException {
        if (this.socket == null) {
            throw new IOException("Folder is closed");
        } else if (cmd != null) {
            this.output.print(cmd + CRLF);
            this.output.flush();
        }
    }

    private Response readResponse() throws IOException {
        try {
            String line = this.input.readLine();
            if (line == null) {
                this.traceLogger.finest("<EOF>");
                throw new EOFException("EOF on socket");
            }
            Response r = new Response();
            if (line.startsWith("+OK")) {
                r.f282ok = true;
            } else if (line.startsWith("+ ")) {
                r.f282ok = true;
                r.cont = true;
            } else if (line.startsWith("-ERR")) {
                r.f282ok = $assertionsDisabled;
            } else {
                throw new IOException("Unexpected response: " + line);
            }
            int i = line.indexOf(32);
            if (i >= 0) {
                r.data = line.substring(i + 1);
            }
            return r;
        } catch (InterruptedIOException iioex) {
            try {
                this.socket.close();
            } catch (IOException e) {
            }
            throw new EOFException(iioex.getMessage());
        } catch (SocketException ex) {
            try {
                this.socket.close();
            } catch (IOException e2) {
            }
            throw new EOFException(ex.getMessage());
        }
    }

    private Response multilineCommand(String cmd, int size) throws IOException {
        multilineCommandStart(cmd);
        issueCommand(cmd);
        Response r = readResponse();
        if (!r.f282ok) {
            multilineCommandEnd();
        } else {
            r.bytes = readMultilineResponse(size);
            multilineCommandEnd();
        }
        return r;
    }

    private InputStream readMultilineResponse(int size) throws IOException {
        int b;
        SharedByteArrayOutputStream buf = new SharedByteArrayOutputStream(size);
        int lastb = 10;
        while (true) {
            b = this.input.read();
            if (b >= 0) {
                if (lastb == 10 && b == 46 && (b = this.input.read()) == 13) {
                    b = this.input.read();
                    break;
                }
                try {
                    buf.write(b);
                    lastb = b;
                } catch (InterruptedIOException iioex) {
                    try {
                        this.socket.close();
                    } catch (IOException e) {
                    }
                    throw iioex;
                }
            } else {
                break;
            }
        }
        if (b >= 0) {
            return buf.toStream();
        }
        throw new EOFException("EOF on socket");
    }

    /* access modifiers changed from: protected */
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

    private void simpleCommandStart(String command) {
    }

    private void simpleCommandEnd() {
    }

    private void multilineCommandStart(String command) {
    }

    private void multilineCommandEnd() {
    }

    private void batchCommandStart(String command) {
    }

    private void batchCommandContinue(String command) {
    }

    private void batchCommandEnd() {
    }
}
