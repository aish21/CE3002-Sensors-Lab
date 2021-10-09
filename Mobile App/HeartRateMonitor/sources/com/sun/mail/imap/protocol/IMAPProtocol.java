package com.sun.mail.imap.protocol;

import androidx.appcompat.widget.ActivityChooserView;
import com.google.appinventor.components.runtime.util.Ev3Constants;
import com.sun.mail.auth.Ntlm;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.Literal;
import com.sun.mail.iap.LiteralException;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.CopyUID;
import com.sun.mail.imap.ResyncData;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.SortTerm;
import com.sun.mail.imap.Utility;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import javax.mail.Flags;
import javax.mail.Quota;
import javax.mail.internet.MimeUtility;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

public class IMAPProtocol extends Protocol {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final byte[] CRLF = {13, 10};
    private static final byte[] DONE = {Ev3Constants.Opcode.CP_LT8, Ev3Constants.Opcode.CP_EQF, Ev3Constants.Opcode.CP_EQ32, Ev3Constants.Opcode.CP_LT16, 13, 10};
    private static final FetchItem[] fetchItems = new FetchItem[0];
    private boolean authenticated;
    private List<String> authmechs;

    /* renamed from: ba */
    private ByteArray f279ba;
    private Map<String, String> capabilities;
    private boolean connected;
    protected Set<String> enabled;
    private volatile String idleTag;
    private String name;
    private boolean noauthdebug;
    private String proxyAuthUser;
    private boolean referralException;
    private boolean rev1;
    private SaslAuthenticator saslAuthenticator;
    protected String[] searchCharsets;
    protected SearchSequence searchSequence;
    private boolean utf8;

    static {
        boolean z;
        if (!IMAPProtocol.class.desiredAssertionStatus()) {
            z = true;
        } else {
            z = false;
        }
        $assertionsDisabled = z;
    }

    public IMAPProtocol(String name2, String host, int port, Properties props, boolean isSSL, MailLogger logger) throws IOException, ProtocolException {
        super(host, port, props, "mail." + name2, isSSL, logger);
        this.connected = false;
        this.rev1 = false;
        this.noauthdebug = true;
        try {
            this.name = name2;
            this.noauthdebug = !PropUtil.getBooleanProperty(props, "mail.debug.auth", false);
            this.referralException = PropUtil.getBooleanProperty(props, this.prefix + ".referralexception", false);
            if (this.capabilities == null) {
                capability();
            }
            if (hasCapability("IMAP4rev1")) {
                this.rev1 = true;
            }
            this.searchCharsets = new String[2];
            this.searchCharsets[0] = "UTF-8";
            this.searchCharsets[1] = MimeUtility.mimeCharset(MimeUtility.getDefaultJavaCharset());
            this.connected = true;
        } finally {
            if (!this.connected) {
                disconnect();
            }
        }
    }

    public IMAPProtocol(InputStream in, PrintStream out, Properties props, boolean debug) throws IOException {
        super(in, out, props, debug);
        this.connected = false;
        this.rev1 = false;
        this.noauthdebug = true;
        this.name = "imap";
        this.noauthdebug = !PropUtil.getBooleanProperty(props, "mail.debug.auth", false);
        if (this.capabilities == null) {
            this.capabilities = new HashMap();
        }
        this.searchCharsets = new String[2];
        this.searchCharsets[0] = "UTF-8";
        this.searchCharsets[1] = MimeUtility.mimeCharset(MimeUtility.getDefaultJavaCharset());
        this.connected = true;
    }

    public FetchItem[] getFetchItems() {
        return fetchItems;
    }

    public void capability() throws ProtocolException {
        Response[] r = command("CAPABILITY", (Argument) null);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            handleCapabilityResponse(r);
        }
        handleResult(response);
    }

    public void handleCapabilityResponse(Response[] r) {
        boolean first = true;
        int len = r.length;
        for (int i = 0; i < len; i++) {
            if (r[i] instanceof IMAPResponse) {
                IMAPResponse ir = r[i];
                if (ir.keyEquals("CAPABILITY")) {
                    if (first) {
                        this.capabilities = new HashMap(10);
                        this.authmechs = new ArrayList(5);
                        first = false;
                    }
                    parseCapabilities(ir);
                }
            }
        }
    }

    /*  JADX ERROR: StackOverflow in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    protected void setCapabilities(com.sun.mail.iap.Response r5) {
        /*
            r4 = this;
        L_0x0000:
            byte r0 = r5.readByte()
            if (r0 <= 0) goto L_0x000a
            r2 = 91
            if (r0 != r2) goto L_0x0000
        L_0x000a:
            if (r0 != 0) goto L_0x000d
        L_0x000c:
            return
        L_0x000d:
            java.lang.String r1 = r5.readAtom()
            java.lang.String r2 = "CAPABILITY"
            boolean r2 = r1.equalsIgnoreCase(r2)
            if (r2 == 0) goto L_0x000c
            java.util.HashMap r2 = new java.util.HashMap
            r3 = 10
            r2.<init>(r3)
            r4.capabilities = r2
            java.util.ArrayList r2 = new java.util.ArrayList
            r3 = 5
            r2.<init>(r3)
            r4.authmechs = r2
            r4.parseCapabilities(r5)
            goto L_0x000c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.protocol.IMAPProtocol.setCapabilities(com.sun.mail.iap.Response):void");
    }

    /* access modifiers changed from: protected */
    public void parseCapabilities(Response r) {
        while (true) {
            String s = r.readAtom();
            if (s == null) {
                return;
            }
            if (s.length() != 0) {
                this.capabilities.put(s.toUpperCase(Locale.ENGLISH), s);
                if (s.regionMatches(true, 0, "AUTH=", 0, 5)) {
                    this.authmechs.add(s.substring(5));
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine("AUTH: " + s.substring(5));
                    }
                }
            } else if (r.peekByte() != 93) {
                r.skipToken();
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void processGreeting(Response r) throws ProtocolException {
        if (r.isBYE()) {
            checkReferral(r);
            throw new ConnectionException(this, r);
        } else if (r.isOK()) {
            this.referralException = PropUtil.getBooleanProperty(this.props, this.prefix + ".referralexception", false);
            if (this.referralException) {
                checkReferral(r);
            }
            setCapabilities(r);
        } else if (!$assertionsDisabled && !(r instanceof IMAPResponse)) {
            throw new AssertionError();
        } else if (((IMAPResponse) r).keyEquals("PREAUTH")) {
            this.authenticated = true;
            setCapabilities(r);
        } else {
            disconnect();
            throw new ConnectionException(this, r);
        }
    }

    private void checkReferral(Response r) throws IMAPReferralException {
        int i;
        String url;
        String msg;
        String s = r.getRest();
        if (s.startsWith("[") && (i = s.indexOf(32)) > 0 && s.substring(1, i).equalsIgnoreCase("REFERRAL")) {
            int j = s.indexOf(93);
            if (j > 0) {
                url = s.substring(i + 1, j);
                msg = s.substring(j + 1).trim();
            } else {
                url = s.substring(i + 1);
                msg = "";
            }
            if (r.isBYE()) {
                disconnect();
            }
            throw new IMAPReferralException(msg, url);
        }
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public boolean isREV1() {
        return this.rev1;
    }

    /* access modifiers changed from: protected */
    public boolean supportsNonSyncLiterals() {
        return hasCapability("LITERAL+");
    }

    public Response readResponse() throws IOException, ProtocolException {
        IMAPResponse r = new IMAPResponse((Protocol) this);
        if (r.keyEquals("FETCH")) {
            return new FetchResponse(r, getFetchItems());
        }
        return r;
    }

    public boolean hasCapability(String c) {
        if (!c.endsWith("*")) {
            return this.capabilities.containsKey(c.toUpperCase(Locale.ENGLISH));
        }
        String c2 = c.substring(0, c.length() - 1).toUpperCase(Locale.ENGLISH);
        for (String startsWith : this.capabilities.keySet()) {
            if (startsWith.startsWith(c2)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, String> getCapabilities() {
        return this.capabilities;
    }

    public boolean supportsUtf8() {
        return this.utf8;
    }

    public void disconnect() {
        super.disconnect();
        this.authenticated = false;
    }

    public void noop() throws ProtocolException {
        this.logger.fine("IMAPProtocol noop");
        simpleCommand("NOOP", (Argument) null);
    }

    public void logout() throws ProtocolException {
        try {
            Response[] r = command("LOGOUT", (Argument) null);
            this.authenticated = false;
            notifyResponseHandlers(r);
        } finally {
            disconnect();
        }
    }

    /* JADX INFO: finally extract failed */
    public void login(String u, String p) throws ProtocolException {
        Argument args = new Argument();
        args.writeString(u);
        args.writeString(p);
        try {
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("LOGIN command trace suppressed");
                suspendTracing();
            }
            Response[] r = command("LOGIN", args);
            resumeTracing();
            handleCapabilityResponse(r);
            notifyResponseHandlers(r);
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("LOGIN command result: " + r[r.length - 1]);
            }
            handleLoginResult(r[r.length - 1]);
            setCapabilities(r[r.length - 1]);
            this.authenticated = true;
        } catch (Throwable th) {
            resumeTracing();
            throw th;
        }
    }

    public synchronized void authlogin(String u, String p) throws ProtocolException {
        Response r;
        String s;
        List<Response> v = new ArrayList<>();
        String tag = null;
        Response r2 = null;
        boolean done = false;
        if (this.noauthdebug && isTracing()) {
            this.logger.fine("AUTHENTICATE LOGIN command trace suppressed");
            suspendTracing();
        }
        try {
            tag = writeCommand("AUTHENTICATE LOGIN", (Argument) null);
        } catch (Exception ex) {
            r2 = Response.byeResponse(ex);
            done = true;
        }
        try {
            OutputStream os = getOutputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStream b64os = new BASE64EncoderStream(bos, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
            boolean first = true;
            while (!done) {
                try {
                    r = readResponse();
                    if (r.isContinuation()) {
                        if (first) {
                            s = u;
                            first = false;
                        } else {
                            s = p;
                        }
                        b64os.write(s.getBytes(StandardCharsets.UTF_8));
                        b64os.flush();
                        bos.write(CRLF);
                        os.write(bos.toByteArray());
                        os.flush();
                        bos.reset();
                    } else if (r.isTagged() && r.getTag().equals(tag)) {
                        done = true;
                    } else if (r.isBYE()) {
                        done = true;
                    }
                } catch (Exception ioex) {
                    r = Response.byeResponse(ioex);
                    done = true;
                }
                v.add(r);
            }
            resumeTracing();
            Response[] responses = (Response[]) v.toArray(new Response[v.size()]);
            handleCapabilityResponse(responses);
            notifyResponseHandlers(responses);
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("AUTHENTICATE LOGIN command result: " + r);
            }
            handleLoginResult(r);
            setCapabilities(r);
            this.authenticated = true;
        } catch (Throwable th) {
            resumeTracing();
            throw th;
        }
    }

    public synchronized void authplain(String authzid, String u, String p) throws ProtocolException {
        Response r;
        String str;
        List<Response> v = new ArrayList<>();
        String tag = null;
        Response r2 = null;
        boolean done = false;
        if (this.noauthdebug && isTracing()) {
            this.logger.fine("AUTHENTICATE PLAIN command trace suppressed");
            suspendTracing();
        }
        try {
            tag = writeCommand("AUTHENTICATE PLAIN", (Argument) null);
        } catch (Exception ex) {
            r2 = Response.byeResponse(ex);
            done = true;
        }
        try {
            OutputStream os = getOutputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStream b64os = new BASE64EncoderStream(bos, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
            while (!done) {
                try {
                    r = readResponse();
                    if (r.isContinuation()) {
                        StringBuilder sb = new StringBuilder();
                        if (authzid == null) {
                            str = "";
                        } else {
                            str = authzid;
                        }
                        b64os.write(sb.append(str).append("\u0000").append(u).append("\u0000").append(p).toString().getBytes(StandardCharsets.UTF_8));
                        b64os.flush();
                        bos.write(CRLF);
                        os.write(bos.toByteArray());
                        os.flush();
                        bos.reset();
                    } else if (r.isTagged() && r.getTag().equals(tag)) {
                        done = true;
                    } else if (r.isBYE()) {
                        done = true;
                    }
                } catch (Exception ioex) {
                    r = Response.byeResponse(ioex);
                    done = true;
                }
                v.add(r);
            }
            resumeTracing();
            Response[] responses = (Response[]) v.toArray(new Response[v.size()]);
            handleCapabilityResponse(responses);
            notifyResponseHandlers(responses);
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("AUTHENTICATE PLAIN command result: " + r);
            }
            handleLoginResult(r);
            setCapabilities(r);
            this.authenticated = true;
        } catch (Throwable th) {
            resumeTracing();
            throw th;
        }
    }

    public synchronized void authntlm(String authzid, String u, String p) throws ProtocolException {
        Response r;
        String s;
        ArrayList arrayList = new ArrayList();
        String tag = null;
        Response r2 = null;
        boolean done = false;
        int flags = PropUtil.getIntProperty(this.props, "mail." + this.name + ".auth.ntlm.flags", 0);
        boolean v2 = PropUtil.getBooleanProperty(this.props, "mail." + this.name + ".auth.ntlm.v2", true);
        Ntlm ntlm = new Ntlm(this.props.getProperty("mail." + this.name + ".auth.ntlm.domain", ""), getLocalHost(), u, p, this.logger);
        try {
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("AUTHENTICATE NTLM command trace suppressed");
                suspendTracing();
            }
            try {
                tag = writeCommand("AUTHENTICATE NTLM", (Argument) null);
            } catch (Exception ex) {
                r2 = Response.byeResponse(ex);
                done = true;
            }
            OutputStream os = getOutputStream();
            boolean first = true;
            while (!done) {
                try {
                    r = readResponse();
                    if (r.isContinuation()) {
                        if (first) {
                            s = ntlm.generateType1Msg(flags, v2);
                            first = false;
                        } else {
                            s = ntlm.generateType3Msg(r.getRest());
                        }
                        os.write(s.getBytes(StandardCharsets.UTF_8));
                        os.write(CRLF);
                        os.flush();
                    } else if (r.isTagged() && r.getTag().equals(tag)) {
                        done = true;
                    } else if (r.isBYE()) {
                        done = true;
                    }
                } catch (Exception ioex) {
                    r = Response.byeResponse(ioex);
                    done = true;
                }
                arrayList.add(r);
            }
            resumeTracing();
            Response[] responses = (Response[]) arrayList.toArray(new Response[arrayList.size()]);
            handleCapabilityResponse(responses);
            notifyResponseHandlers(responses);
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("AUTHENTICATE NTLM command result: " + r);
            }
            handleLoginResult(r);
            setCapabilities(r);
            this.authenticated = true;
        } catch (Throwable th) {
            resumeTracing();
            throw th;
        }
    }

    public synchronized void authoauth2(String u, String p) throws ProtocolException {
        Response r;
        List<Response> v = new ArrayList<>();
        String tag = null;
        Response r2 = null;
        boolean done = false;
        if (this.noauthdebug && isTracing()) {
            this.logger.fine("AUTHENTICATE XOAUTH2 command trace suppressed");
            suspendTracing();
        }
        try {
            Argument args = new Argument();
            args.writeAtom("XOAUTH2");
            if (hasCapability("SASL-IR")) {
                byte[] ba = BASE64EncoderStream.encode(("user=" + u + "\u0001auth=Bearer " + p + "\u0001\u0001").getBytes(StandardCharsets.UTF_8));
                args.writeAtom(ASCIIUtility.toString(ba, 0, ba.length));
            }
            tag = writeCommand("AUTHENTICATE", args);
        } catch (Exception ex) {
            r2 = Response.byeResponse(ex);
            done = true;
        }
        try {
            OutputStream os = getOutputStream();
            while (!done) {
                try {
                    r = readResponse();
                    if (r.isContinuation()) {
                        os.write(BASE64EncoderStream.encode(("user=" + u + "\u0001auth=Bearer " + p + "\u0001\u0001").getBytes(StandardCharsets.UTF_8)));
                        os.write(CRLF);
                        os.flush();
                    } else if (r.isTagged() && r.getTag().equals(tag)) {
                        done = true;
                    } else if (r.isBYE()) {
                        done = true;
                    }
                } catch (Exception ioex) {
                    r = Response.byeResponse(ioex);
                    done = true;
                }
                v.add(r);
            }
            resumeTracing();
            Response[] responses = (Response[]) v.toArray(new Response[v.size()]);
            handleCapabilityResponse(responses);
            notifyResponseHandlers(responses);
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("AUTHENTICATE XOAUTH2 command result: " + r);
            }
            handleLoginResult(r);
            setCapabilities(r);
            this.authenticated = true;
        } catch (Throwable th) {
            resumeTracing();
            throw th;
        }
    }

    public void sasllogin(String[] allowed, String realm, String authzid, String u, String p) throws ProtocolException {
        String serviceHost;
        List<String> v;
        if (PropUtil.getBooleanProperty(this.props, "mail." + this.name + ".sasl.usecanonicalhostname", false)) {
            serviceHost = getInetAddress().getCanonicalHostName();
        } else {
            serviceHost = this.host;
        }
        if (this.saslAuthenticator == null) {
            try {
                this.saslAuthenticator = (SaslAuthenticator) Class.forName("com.sun.mail.imap.protocol.IMAPSaslAuthenticator").getConstructor(new Class[]{IMAPProtocol.class, String.class, Properties.class, MailLogger.class, String.class}).newInstance(new Object[]{this, this.name, this.props, this.logger, serviceHost});
            } catch (Exception ex) {
                this.logger.log(Level.FINE, "Can't load SASL authenticator", (Throwable) ex);
                return;
            }
        }
        if (allowed == null || allowed.length <= 0) {
            v = this.authmechs;
        } else {
            v = new ArrayList<>(allowed.length);
            for (int i = 0; i < allowed.length; i++) {
                if (this.authmechs.contains(allowed[i])) {
                    v.add(allowed[i]);
                }
            }
        }
        String[] mechs = (String[]) v.toArray(new String[v.size()]);
        try {
            if (this.noauthdebug && isTracing()) {
                this.logger.fine("SASL authentication command trace suppressed");
                suspendTracing();
            }
            if (this.saslAuthenticator.authenticate(mechs, realm, authzid, u, p)) {
                if (this.noauthdebug && isTracing()) {
                    this.logger.fine("SASL authentication succeeded");
                }
                this.authenticated = true;
            } else if (this.noauthdebug && isTracing()) {
                this.logger.fine("SASL authentication failed");
            }
        } finally {
            resumeTracing();
        }
    }

    /* access modifiers changed from: package-private */
    public OutputStream getIMAPOutputStream() {
        return getOutputStream();
    }

    /* access modifiers changed from: protected */
    public void handleLoginResult(Response r) throws ProtocolException {
        if (hasCapability("LOGIN-REFERRALS") && (!r.isOK() || this.referralException)) {
            checkReferral(r);
        }
        handleResult(r);
    }

    public void proxyauth(String u) throws ProtocolException {
        Argument args = new Argument();
        args.writeString(u);
        simpleCommand("PROXYAUTH", args);
        this.proxyAuthUser = u;
    }

    public String getProxyAuthUser() {
        return this.proxyAuthUser;
    }

    public void unauthenticate() throws ProtocolException {
        if (!hasCapability("X-UNAUTHENTICATE")) {
            throw new BadCommandException("UNAUTHENTICATE not supported");
        }
        simpleCommand("UNAUTHENTICATE", (Argument) null);
        this.authenticated = false;
    }

    @Deprecated
    /* renamed from: id */
    public void mo13041id(String guid) throws ProtocolException {
        Map<String, String> gmap = new HashMap<>();
        gmap.put("GUID", guid);
        mo13040id(gmap);
    }

    public void startTLS() throws ProtocolException {
        try {
            super.startTLS("STARTTLS");
        } catch (ProtocolException pex) {
            this.logger.log(Level.FINE, "STARTTLS ProtocolException", (Throwable) pex);
            throw pex;
        } catch (Exception ex) {
            this.logger.log(Level.FINE, "STARTTLS Exception", (Throwable) ex);
            notifyResponseHandlers(new Response[]{Response.byeResponse(ex)});
            disconnect();
            throw new ProtocolException("STARTTLS failure", ex);
        }
    }

    public void compress() throws ProtocolException {
        try {
            super.startCompression("COMPRESS DEFLATE");
        } catch (ProtocolException pex) {
            this.logger.log(Level.FINE, "COMPRESS ProtocolException", (Throwable) pex);
            throw pex;
        } catch (Exception ex) {
            this.logger.log(Level.FINE, "COMPRESS Exception", (Throwable) ex);
            notifyResponseHandlers(new Response[]{Response.byeResponse(ex)});
            disconnect();
            throw new ProtocolException("COMPRESS failure", ex);
        }
    }

    /* access modifiers changed from: protected */
    public void writeMailboxName(Argument args, String name2) {
        if (this.utf8) {
            args.writeString(name2, StandardCharsets.UTF_8);
        } else {
            args.writeString(BASE64MailboxEncoder.encode(name2));
        }
    }

    public MailboxInfo select(String mbox) throws ProtocolException {
        return select(mbox, (ResyncData) null);
    }

    public MailboxInfo select(String mbox, ResyncData rd) throws ProtocolException {
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        if (rd != null) {
            if (rd == ResyncData.CONDSTORE) {
                if (!hasCapability("CONDSTORE")) {
                    throw new BadCommandException("CONDSTORE not supported");
                }
                args.writeArgument(new Argument().writeAtom("CONDSTORE"));
            } else if (!hasCapability("QRESYNC")) {
                throw new BadCommandException("QRESYNC not supported");
            } else {
                args.writeArgument(resyncArgs(rd));
            }
        }
        Response[] r = command("SELECT", args);
        MailboxInfo minfo = new MailboxInfo(r);
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            if (response.toString().indexOf("READ-ONLY") != -1) {
                minfo.mode = 1;
            } else {
                minfo.mode = 2;
            }
        }
        handleResult(response);
        return minfo;
    }

    public MailboxInfo examine(String mbox) throws ProtocolException {
        return examine(mbox, (ResyncData) null);
    }

    public MailboxInfo examine(String mbox, ResyncData rd) throws ProtocolException {
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        if (rd != null) {
            if (rd == ResyncData.CONDSTORE) {
                if (!hasCapability("CONDSTORE")) {
                    throw new BadCommandException("CONDSTORE not supported");
                }
                args.writeArgument(new Argument().writeAtom("CONDSTORE"));
            } else if (!hasCapability("QRESYNC")) {
                throw new BadCommandException("QRESYNC not supported");
            } else {
                args.writeArgument(resyncArgs(rd));
            }
        }
        Response[] r = command("EXAMINE", args);
        MailboxInfo minfo = new MailboxInfo(r);
        minfo.mode = 1;
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        return minfo;
    }

    private static Argument resyncArgs(ResyncData rd) {
        Argument cmd = new Argument();
        cmd.writeAtom("QRESYNC");
        Argument args = new Argument();
        args.writeNumber(rd.getUIDValidity());
        args.writeNumber(rd.getModSeq());
        UIDSet[] uids = Utility.getResyncUIDSet(rd);
        if (uids != null) {
            args.writeString(UIDSet.toString(uids));
        }
        cmd.writeArgument(args);
        return cmd;
    }

    public void enable(String cap) throws ProtocolException {
        if (!hasCapability("ENABLE")) {
            throw new BadCommandException("ENABLE not supported");
        }
        Argument args = new Argument();
        args.writeAtom(cap);
        simpleCommand("ENABLE", args);
        if (this.enabled == null) {
            this.enabled = new HashSet();
        }
        this.enabled.add(cap.toUpperCase(Locale.ENGLISH));
        this.utf8 = isEnabled("UTF8=ACCEPT");
    }

    public boolean isEnabled(String cap) {
        if (this.enabled == null) {
            return false;
        }
        return this.enabled.contains(cap.toUpperCase(Locale.ENGLISH));
    }

    public void unselect() throws ProtocolException {
        if (!hasCapability("UNSELECT")) {
            throw new BadCommandException("UNSELECT not supported");
        }
        simpleCommand("UNSELECT", (Argument) null);
    }

    public Status status(String mbox, String[] items) throws ProtocolException {
        if (isREV1() || hasCapability("IMAP4SUNVERSION")) {
            Argument args = new Argument();
            writeMailboxName(args, mbox);
            Argument itemArgs = new Argument();
            if (items == null) {
                items = Status.standardItems;
            }
            for (String writeAtom : items) {
                itemArgs.writeAtom(writeAtom);
            }
            args.writeArgument(itemArgs);
            Response[] r = command("STATUS", args);
            Status status = null;
            Response response = r[r.length - 1];
            if (response.isOK()) {
                int len = r.length;
                for (int i = 0; i < len; i++) {
                    if (r[i] instanceof IMAPResponse) {
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("STATUS")) {
                            if (status == null) {
                                status = new Status(ir);
                            } else {
                                Status.add(status, new Status(ir));
                            }
                            r[i] = null;
                        }
                    }
                }
            }
            notifyResponseHandlers(r);
            handleResult(response);
            return status;
        }
        throw new BadCommandException("STATUS not supported");
    }

    public void create(String mbox) throws ProtocolException {
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        simpleCommand("CREATE", args);
    }

    public void delete(String mbox) throws ProtocolException {
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        simpleCommand("DELETE", args);
    }

    public void rename(String o, String n) throws ProtocolException {
        Argument args = new Argument();
        writeMailboxName(args, o);
        writeMailboxName(args, n);
        simpleCommand("RENAME", args);
    }

    public void subscribe(String mbox) throws ProtocolException {
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        simpleCommand("SUBSCRIBE", args);
    }

    public void unsubscribe(String mbox) throws ProtocolException {
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        simpleCommand("UNSUBSCRIBE", args);
    }

    public ListInfo[] list(String ref, String pattern) throws ProtocolException {
        return doList("LIST", ref, pattern);
    }

    public ListInfo[] lsub(String ref, String pattern) throws ProtocolException {
        return doList("LSUB", ref, pattern);
    }

    /* access modifiers changed from: protected */
    public ListInfo[] doList(String cmd, String ref, String pat) throws ProtocolException {
        Argument args = new Argument();
        writeMailboxName(args, ref);
        writeMailboxName(args, pat);
        Response[] r = command(cmd, args);
        ListInfo[] linfo = null;
        Response response = r[r.length - 1];
        if (response.isOK()) {
            List<ListInfo> v = new ArrayList<>(1);
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals(cmd)) {
                        v.add(new ListInfo(ir));
                        r[i] = null;
                    }
                }
            }
            if (v.size() > 0) {
                linfo = (ListInfo[]) v.toArray(new ListInfo[v.size()]);
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return linfo;
    }

    public void append(String mbox, Flags f, Date d, Literal data) throws ProtocolException {
        appenduid(mbox, f, d, data, false);
    }

    public AppendUID appenduid(String mbox, Flags f, Date d, Literal data) throws ProtocolException {
        return appenduid(mbox, f, d, data, true);
    }

    public AppendUID appenduid(String mbox, Flags f, Date d, Literal data, boolean uid) throws ProtocolException {
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        if (f != null) {
            if (f.contains(Flags.Flag.RECENT)) {
                Flags f2 = new Flags(f);
                f2.remove(Flags.Flag.RECENT);
                f = f2;
            }
            args.writeAtom(createFlagList(f));
        }
        if (d != null) {
            args.writeString(INTERNALDATE.format(d));
        }
        args.writeBytes(data);
        Response[] r = command("APPEND", args);
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        if (uid) {
            return getAppendUID(r[r.length - 1]);
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0020  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.sun.mail.imap.AppendUID getAppendUID(com.sun.mail.iap.Response r9) {
        /*
            r8 = this;
            r6 = 0
            boolean r7 = r9.isOK()
            if (r7 != 0) goto L_0x0008
        L_0x0007:
            return r6
        L_0x0008:
            byte r0 = r9.readByte()
            if (r0 <= 0) goto L_0x0012
            r7 = 91
            if (r0 != r7) goto L_0x0008
        L_0x0012:
            if (r0 == 0) goto L_0x0007
            java.lang.String r1 = r9.readAtom()
            java.lang.String r7 = "APPENDUID"
            boolean r7 = r1.equalsIgnoreCase(r7)
            if (r7 == 0) goto L_0x0007
            long r4 = r9.readLong()
            long r2 = r9.readLong()
            com.sun.mail.imap.AppendUID r6 = new com.sun.mail.imap.AppendUID
            r6.<init>(r4, r2)
            goto L_0x0007
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.protocol.IMAPProtocol.getAppendUID(com.sun.mail.iap.Response):com.sun.mail.imap.AppendUID");
    }

    public void check() throws ProtocolException {
        simpleCommand("CHECK", (Argument) null);
    }

    public void close() throws ProtocolException {
        simpleCommand("CLOSE", (Argument) null);
    }

    public void expunge() throws ProtocolException {
        simpleCommand("EXPUNGE", (Argument) null);
    }

    public void uidexpunge(UIDSet[] set) throws ProtocolException {
        if (!hasCapability("UIDPLUS")) {
            throw new BadCommandException("UID EXPUNGE not supported");
        }
        simpleCommand("UID EXPUNGE " + UIDSet.toString(set), (Argument) null);
    }

    public BODYSTRUCTURE fetchBodyStructure(int msgno) throws ProtocolException {
        Response[] r = fetch(msgno, "BODYSTRUCTURE");
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (BODYSTRUCTURE) FetchResponse.getItem(r, msgno, BODYSTRUCTURE.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public BODY peekBody(int msgno, String section) throws ProtocolException {
        return fetchBody(msgno, section, true);
    }

    public BODY fetchBody(int msgno, String section) throws ProtocolException {
        return fetchBody(msgno, section, false);
    }

    /* access modifiers changed from: protected */
    public BODY fetchBody(int msgno, String section, boolean peek) throws ProtocolException {
        if (section == null) {
            section = "";
        }
        return fetchSectionBody(msgno, section, (peek ? "BODY.PEEK[" : "BODY[") + section + "]");
    }

    public BODY peekBody(int msgno, String section, int start, int size) throws ProtocolException {
        return fetchBody(msgno, section, start, size, true, (ByteArray) null);
    }

    public BODY fetchBody(int msgno, String section, int start, int size) throws ProtocolException {
        return fetchBody(msgno, section, start, size, false, (ByteArray) null);
    }

    public BODY peekBody(int msgno, String section, int start, int size, ByteArray ba) throws ProtocolException {
        return fetchBody(msgno, section, start, size, true, ba);
    }

    public BODY fetchBody(int msgno, String section, int start, int size, ByteArray ba) throws ProtocolException {
        return fetchBody(msgno, section, start, size, false, ba);
    }

    /* access modifiers changed from: protected */
    public BODY fetchBody(int msgno, String section, int start, int size, boolean peek, ByteArray ba) throws ProtocolException {
        this.f279ba = ba;
        if (section == null) {
            section = "";
        }
        return fetchSectionBody(msgno, section, (peek ? "BODY.PEEK[" : "BODY[") + section + "]<" + String.valueOf(start) + "." + String.valueOf(size) + ">");
    }

    /* access modifiers changed from: protected */
    public BODY fetchSectionBody(int msgno, String section, String body) throws ProtocolException {
        Response[] r = fetch(msgno, body);
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            List<BODY> bl = FetchResponse.getItems(r, msgno, BODY.class);
            if (bl.size() == 1) {
                return bl.get(0);
            }
            if (this.logger.isLoggable(Level.FINEST)) {
                this.logger.finest("got " + bl.size() + " BODY responses for section " + section);
            }
            for (BODY br : bl) {
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.finest("got BODY section " + br.getSection());
                }
                if (br.getSection().equalsIgnoreCase(section)) {
                    return br;
                }
            }
            return null;
        } else if (response.isNO()) {
            return null;
        } else {
            handleResult(response);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public ByteArray getResponseBuffer() {
        ByteArray ret = this.f279ba;
        this.f279ba = null;
        return ret;
    }

    public RFC822DATA fetchRFC822(int msgno, String what) throws ProtocolException {
        Response[] r = fetch(msgno, what == null ? "RFC822" : "RFC822." + what);
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (RFC822DATA) FetchResponse.getItem(r, msgno, RFC822DATA.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public Flags fetchFlags(int msgno) throws ProtocolException {
        Flags flags = null;
        Response[] r = fetch(msgno, "FLAGS");
        int i = 0;
        int len = r.length;
        while (true) {
            if (i < len) {
                if (r[i] != null && (r[i] instanceof FetchResponse) && ((FetchResponse) r[i]).getNumber() == msgno && (flags = (Flags) ((FetchResponse) r[i]).getItem(FLAGS.class)) != null) {
                    r[i] = null;
                    break;
                }
                i++;
            } else {
                break;
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        return flags;
    }

    public UID fetchUID(int msgno) throws ProtocolException {
        Response[] r = fetch(msgno, "UID");
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (UID) FetchResponse.getItem(r, msgno, UID.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public MODSEQ fetchMODSEQ(int msgno) throws ProtocolException {
        Response[] r = fetch(msgno, "MODSEQ");
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (MODSEQ) FetchResponse.getItem(r, msgno, MODSEQ.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public void fetchSequenceNumber(long uid) throws ProtocolException {
        Response[] r = fetch(String.valueOf(uid), "UID", true);
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
    }

    public long[] fetchSequenceNumbers(long start, long end) throws ProtocolException {
        String valueOf;
        UID u;
        StringBuilder append = new StringBuilder().append(String.valueOf(start)).append(":");
        if (end == -1) {
            valueOf = "*";
        } else {
            valueOf = String.valueOf(end);
        }
        Response[] r = fetch(append.append(valueOf).toString(), "UID", true);
        List<UID> v = new ArrayList<>();
        int len = r.length;
        for (int i = 0; i < len; i++) {
            if (!(r[i] == null || !(r[i] instanceof FetchResponse) || (u = (UID) ((FetchResponse) r[i]).getItem(UID.class)) == null)) {
                v.add(u);
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        long[] lv = new long[v.size()];
        for (int i2 = 0; i2 < v.size(); i2++) {
            lv[i2] = v.get(i2).uid;
        }
        return lv;
    }

    public void fetchSequenceNumbers(long[] uids) throws ProtocolException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < uids.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(String.valueOf(uids[i]));
        }
        Response[] r = fetch(sb.toString(), "UID", true);
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
    }

    public int[] uidfetchChangedSince(long start, long end, long modseq) throws ProtocolException {
        String valueOf;
        StringBuilder append = new StringBuilder().append(String.valueOf(start)).append(":");
        if (end == -1) {
            valueOf = "*";
        } else {
            valueOf = String.valueOf(end);
        }
        Response[] r = command("UID FETCH " + append.append(valueOf).toString() + " (FLAGS) (CHANGEDSINCE " + String.valueOf(modseq) + ")", (Argument) null);
        List<Integer> v = new ArrayList<>();
        int len = r.length;
        for (int i = 0; i < len; i++) {
            if (r[i] != null && (r[i] instanceof FetchResponse)) {
                v.add(Integer.valueOf(((FetchResponse) r[i]).getNumber()));
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        int vsize = v.size();
        int[] matches = new int[vsize];
        for (int i2 = 0; i2 < vsize; i2++) {
            matches[i2] = v.get(i2).intValue();
        }
        return matches;
    }

    public Response[] fetch(MessageSet[] msgsets, String what) throws ProtocolException {
        return fetch(MessageSet.toString(msgsets), what, false);
    }

    public Response[] fetch(int start, int end, String what) throws ProtocolException {
        return fetch(String.valueOf(start) + ":" + String.valueOf(end), what, false);
    }

    public Response[] fetch(int msg, String what) throws ProtocolException {
        return fetch(String.valueOf(msg), what, false);
    }

    private Response[] fetch(String msgSequence, String what, boolean uid) throws ProtocolException {
        if (uid) {
            return command("UID FETCH " + msgSequence + " (" + what + ")", (Argument) null);
        }
        return command("FETCH " + msgSequence + " (" + what + ")", (Argument) null);
    }

    public void copy(MessageSet[] msgsets, String mbox) throws ProtocolException {
        copyuid(MessageSet.toString(msgsets), mbox, false);
    }

    public void copy(int start, int end, String mbox) throws ProtocolException {
        copyuid(String.valueOf(start) + ":" + String.valueOf(end), mbox, false);
    }

    public CopyUID copyuid(MessageSet[] msgsets, String mbox) throws ProtocolException {
        return copyuid(MessageSet.toString(msgsets), mbox, true);
    }

    public CopyUID copyuid(int start, int end, String mbox) throws ProtocolException {
        return copyuid(String.valueOf(start) + ":" + String.valueOf(end), mbox, true);
    }

    private CopyUID copyuid(String msgSequence, String mbox, boolean uid) throws ProtocolException {
        if (!uid || hasCapability("UIDPLUS")) {
            Argument args = new Argument();
            args.writeAtom(msgSequence);
            writeMailboxName(args, mbox);
            Response[] r = command("COPY", args);
            notifyResponseHandlers(r);
            handleResult(r[r.length - 1]);
            if (uid) {
                return getCopyUID(r);
            }
            return null;
        }
        throw new BadCommandException("UIDPLUS not supported");
    }

    public void move(MessageSet[] msgsets, String mbox) throws ProtocolException {
        moveuid(MessageSet.toString(msgsets), mbox, false);
    }

    public void move(int start, int end, String mbox) throws ProtocolException {
        moveuid(String.valueOf(start) + ":" + String.valueOf(end), mbox, false);
    }

    public CopyUID moveuid(MessageSet[] msgsets, String mbox) throws ProtocolException {
        return moveuid(MessageSet.toString(msgsets), mbox, true);
    }

    public CopyUID moveuid(int start, int end, String mbox) throws ProtocolException {
        return moveuid(String.valueOf(start) + ":" + String.valueOf(end), mbox, true);
    }

    private CopyUID moveuid(String msgSequence, String mbox, boolean uid) throws ProtocolException {
        if (!hasCapability("MOVE")) {
            throw new BadCommandException("MOVE not supported");
        } else if (!uid || hasCapability("UIDPLUS")) {
            Argument args = new Argument();
            args.writeAtom(msgSequence);
            writeMailboxName(args, mbox);
            Response[] r = command("MOVE", args);
            notifyResponseHandlers(r);
            handleResult(r[r.length - 1]);
            if (uid) {
                return getCopyUID(r);
            }
            return null;
        } else {
            throw new BadCommandException("UIDPLUS not supported");
        }
    }

    /* access modifiers changed from: protected */
    public CopyUID getCopyUID(Response[] rr) {
        byte b;
        for (int i = rr.length - 1; i >= 0; i--) {
            Response r = rr[i];
            if (r != null && r.isOK()) {
                do {
                    b = r.readByte();
                    if (b <= 0 || b == 91) {
                        if (b != 0 && r.readAtom().equalsIgnoreCase("COPYUID")) {
                            return new CopyUID(r.readLong(), UIDSet.parseUIDSets(r.readAtom()), UIDSet.parseUIDSets(r.readAtom()));
                        }
                    }
                    b = r.readByte();
                    break;
                } while (b == 91);
                return new CopyUID(r.readLong(), UIDSet.parseUIDSets(r.readAtom()), UIDSet.parseUIDSets(r.readAtom()));
            }
        }
        return null;
    }

    public void storeFlags(MessageSet[] msgsets, Flags flags, boolean set) throws ProtocolException {
        storeFlags(MessageSet.toString(msgsets), flags, set);
    }

    public void storeFlags(int start, int end, Flags flags, boolean set) throws ProtocolException {
        storeFlags(String.valueOf(start) + ":" + String.valueOf(end), flags, set);
    }

    public void storeFlags(int msg, Flags flags, boolean set) throws ProtocolException {
        storeFlags(String.valueOf(msg), flags, set);
    }

    private void storeFlags(String msgset, Flags flags, boolean set) throws ProtocolException {
        Response[] r;
        if (set) {
            r = command("STORE " + msgset + " +FLAGS " + createFlagList(flags), (Argument) null);
        } else {
            r = command("STORE " + msgset + " -FLAGS " + createFlagList(flags), (Argument) null);
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
    }

    /* access modifiers changed from: protected */
    public String createFlagList(Flags flags) {
        String s;
        StringBuilder sb = new StringBuilder("(");
        Flags.Flag[] sf = flags.getSystemFlags();
        boolean first = true;
        for (Flags.Flag f : sf) {
            if (f == Flags.Flag.ANSWERED) {
                s = "\\Answered";
            } else if (f == Flags.Flag.DELETED) {
                s = "\\Deleted";
            } else if (f == Flags.Flag.DRAFT) {
                s = "\\Draft";
            } else if (f == Flags.Flag.FLAGGED) {
                s = "\\Flagged";
            } else if (f == Flags.Flag.RECENT) {
                s = "\\Recent";
            } else if (f == Flags.Flag.SEEN) {
                s = "\\Seen";
            }
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(s);
        }
        String[] uf = flags.getUserFlags();
        for (String append : uf) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(append);
        }
        sb.append(")");
        return sb.toString();
    }

    public int[] search(MessageSet[] msgsets, SearchTerm term) throws ProtocolException, SearchException {
        return search(MessageSet.toString(msgsets), term);
    }

    public int[] search(SearchTerm term) throws ProtocolException, SearchException {
        return search("ALL", term);
    }

    private int[] search(String msgSequence, SearchTerm term) throws ProtocolException, SearchException {
        if (supportsUtf8() || SearchSequence.isAscii(term)) {
            try {
                return issueSearch(msgSequence, term, (String) null);
            } catch (IOException e) {
            }
        }
        for (int i = 0; i < this.searchCharsets.length; i++) {
            if (this.searchCharsets[i] != null) {
                try {
                    return issueSearch(msgSequence, term, this.searchCharsets[i]);
                } catch (CommandFailedException e2) {
                    this.searchCharsets[i] = null;
                } catch (IOException e3) {
                } catch (ProtocolException pex) {
                    throw pex;
                } catch (SearchException sex) {
                    throw sex;
                }
            }
        }
        throw new SearchException("Search failed");
    }

    private int[] issueSearch(String msgSequence, SearchTerm term, String charset) throws ProtocolException, SearchException, IOException {
        String javaCharset;
        Response[] r;
        SearchSequence searchSequence2 = getSearchSequence();
        if (charset == null) {
            javaCharset = null;
        } else {
            javaCharset = MimeUtility.javaCharset(charset);
        }
        Argument args = searchSequence2.generateSequence(term, javaCharset);
        args.writeAtom(msgSequence);
        if (charset == null) {
            r = command("SEARCH", args);
        } else {
            r = command("SEARCH CHARSET " + charset, args);
        }
        Response response = r[r.length - 1];
        int[] matches = null;
        if (response.isOK()) {
            List<Integer> v = new ArrayList<>();
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("SEARCH")) {
                        while (true) {
                            int num = ir.readNumber();
                            if (num == -1) {
                                break;
                            }
                            v.add(Integer.valueOf(num));
                        }
                        r[i] = null;
                    }
                }
            }
            int vsize = v.size();
            matches = new int[vsize];
            for (int i2 = 0; i2 < vsize; i2++) {
                matches[i2] = v.get(i2).intValue();
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return matches;
    }

    /* access modifiers changed from: protected */
    public SearchSequence getSearchSequence() {
        if (this.searchSequence == null) {
            this.searchSequence = new SearchSequence(this);
        }
        return this.searchSequence;
    }

    public int[] sort(SortTerm[] term, SearchTerm sterm) throws ProtocolException, SearchException {
        if (!hasCapability("SORT*")) {
            throw new BadCommandException("SORT not supported");
        } else if (term == null || term.length == 0) {
            throw new BadCommandException("Must have at least one sort term");
        } else {
            Argument args = new Argument();
            Argument sargs = new Argument();
            for (SortTerm sortTerm : term) {
                sargs.writeAtom(sortTerm.toString());
            }
            args.writeArgument(sargs);
            args.writeAtom("UTF-8");
            if (sterm != null) {
                try {
                    args.append(getSearchSequence().generateSequence(sterm, "UTF-8"));
                } catch (IOException ioex) {
                    throw new SearchException(ioex.toString());
                }
            } else {
                args.writeAtom("ALL");
            }
            Response[] r = command("SORT", args);
            Response response = r[r.length - 1];
            int[] matches = null;
            if (response.isOK()) {
                List<Integer> v = new ArrayList<>();
                int len = r.length;
                for (int i = 0; i < len; i++) {
                    if (r[i] instanceof IMAPResponse) {
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("SORT")) {
                            while (true) {
                                int num = ir.readNumber();
                                if (num == -1) {
                                    break;
                                }
                                v.add(Integer.valueOf(num));
                            }
                            r[i] = null;
                        }
                    }
                }
                int vsize = v.size();
                matches = new int[vsize];
                for (int i2 = 0; i2 < vsize; i2++) {
                    matches[i2] = v.get(i2).intValue();
                }
            }
            notifyResponseHandlers(r);
            handleResult(response);
            return matches;
        }
    }

    public Namespaces namespace() throws ProtocolException {
        if (!hasCapability("NAMESPACE")) {
            throw new BadCommandException("NAMESPACE not supported");
        }
        Response[] r = command("NAMESPACE", (Argument) null);
        Namespaces namespace = null;
        Response response = r[r.length - 1];
        if (response.isOK()) {
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("NAMESPACE")) {
                        if (namespace == null) {
                            namespace = new Namespaces(ir);
                        }
                        r[i] = null;
                    }
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return namespace;
    }

    public Quota[] getQuotaRoot(String mbox) throws ProtocolException {
        if (!hasCapability("QUOTA")) {
            throw new BadCommandException("GETQUOTAROOT not supported");
        }
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        Response[] r = command("GETQUOTAROOT", args);
        Response response = r[r.length - 1];
        Map<String, Quota> tab = new HashMap<>();
        if (response.isOK()) {
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("QUOTAROOT")) {
                        ir.readAtomString();
                        while (true) {
                            String root = ir.readAtomString();
                            if (root == null || root.length() <= 0) {
                                r[i] = null;
                            } else {
                                tab.put(root, new Quota(root));
                            }
                        }
                        r[i] = null;
                    } else if (ir.keyEquals("QUOTA")) {
                        Quota quota = parseQuota(ir);
                        Quota q = tab.get(quota.quotaRoot);
                        if (!(q == null || q.resources == null)) {
                            Quota.Resource[] newr = new Quota.Resource[(q.resources.length + quota.resources.length)];
                            System.arraycopy(q.resources, 0, newr, 0, q.resources.length);
                            System.arraycopy(quota.resources, 0, newr, q.resources.length, quota.resources.length);
                            quota.resources = newr;
                        }
                        tab.put(quota.quotaRoot, quota);
                        r[i] = null;
                    }
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return (Quota[]) tab.values().toArray(new Quota[tab.size()]);
    }

    public Quota[] getQuota(String root) throws ProtocolException {
        if (!hasCapability("QUOTA")) {
            throw new BadCommandException("QUOTA not supported");
        }
        Argument args = new Argument();
        args.writeString(root);
        Response[] r = command("GETQUOTA", args);
        List<Quota> v = new ArrayList<>();
        Response response = r[r.length - 1];
        if (response.isOK()) {
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("QUOTA")) {
                        v.add(parseQuota(ir));
                        r[i] = null;
                    }
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return (Quota[]) v.toArray(new Quota[v.size()]);
    }

    public void setQuota(Quota quota) throws ProtocolException {
        if (!hasCapability("QUOTA")) {
            throw new BadCommandException("QUOTA not supported");
        }
        Argument args = new Argument();
        args.writeString(quota.quotaRoot);
        Argument qargs = new Argument();
        if (quota.resources != null) {
            for (int i = 0; i < quota.resources.length; i++) {
                qargs.writeAtom(quota.resources[i].name);
                qargs.writeNumber(quota.resources[i].limit);
            }
        }
        args.writeArgument(qargs);
        Response[] r = command("SETQUOTA", args);
        Response response = r[r.length - 1];
        notifyResponseHandlers(r);
        handleResult(response);
    }

    private Quota parseQuota(Response r) throws ParsingException {
        Quota q = new Quota(r.readAtomString());
        r.skipSpaces();
        if (r.readByte() != 40) {
            throw new ParsingException("parse error in QUOTA");
        }
        List<Quota.Resource> v = new ArrayList<>();
        while (!r.isNextNonSpace(')')) {
            String name2 = r.readAtom();
            if (name2 != null) {
                v.add(new Quota.Resource(name2, r.readLong(), r.readLong()));
            }
        }
        q.resources = (Quota.Resource[]) v.toArray(new Quota.Resource[v.size()]);
        return q;
    }

    public void setACL(String mbox, char modifier, ACL acl) throws ProtocolException {
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        args.writeString(acl.getName());
        String rights = acl.getRights().toString();
        if (modifier == '+' || modifier == '-') {
            rights = modifier + rights;
        }
        args.writeString(rights);
        Response[] r = command("SETACL", args);
        Response response = r[r.length - 1];
        notifyResponseHandlers(r);
        handleResult(response);
    }

    public void deleteACL(String mbox, String user) throws ProtocolException {
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        args.writeString(user);
        Response[] r = command("DELETEACL", args);
        Response response = r[r.length - 1];
        notifyResponseHandlers(r);
        handleResult(response);
    }

    public ACL[] getACL(String mbox) throws ProtocolException {
        String rights;
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        Response[] r = command("GETACL", args);
        Response response = r[r.length - 1];
        List<ACL> v = new ArrayList<>();
        if (response.isOK()) {
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("ACL")) {
                        ir.readAtomString();
                        while (true) {
                            String name2 = ir.readAtomString();
                            if (name2 == null || (rights = ir.readAtomString()) == null) {
                                r[i] = null;
                            } else {
                                v.add(new ACL(name2, new Rights(rights)));
                            }
                        }
                        r[i] = null;
                    }
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return (ACL[]) v.toArray(new ACL[v.size()]);
    }

    public Rights[] listRights(String mbox, String user) throws ProtocolException {
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        args.writeString(user);
        Response[] r = command("LISTRIGHTS", args);
        Response response = r[r.length - 1];
        List<Rights> v = new ArrayList<>();
        if (response.isOK()) {
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("LISTRIGHTS")) {
                        ir.readAtomString();
                        ir.readAtomString();
                        while (true) {
                            String rights = ir.readAtomString();
                            if (rights == null) {
                                break;
                            }
                            v.add(new Rights(rights));
                        }
                        r[i] = null;
                    }
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return (Rights[]) v.toArray(new Rights[v.size()]);
    }

    public Rights myRights(String mbox) throws ProtocolException {
        if (!hasCapability("ACL")) {
            throw new BadCommandException("ACL not supported");
        }
        Argument args = new Argument();
        writeMailboxName(args, mbox);
        Response[] r = command("MYRIGHTS", args);
        Response response = r[r.length - 1];
        Rights rights = null;
        if (response.isOK()) {
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("MYRIGHTS")) {
                        ir.readAtomString();
                        String rs = ir.readAtomString();
                        if (rights == null) {
                            rights = new Rights(rs);
                        }
                        r[i] = null;
                    }
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return rights;
    }

    public synchronized void idleStart() throws ProtocolException {
        Response r;
        if (!hasCapability("IDLE")) {
            throw new BadCommandException("IDLE not supported");
        }
        List<Response> v = new ArrayList<>();
        boolean done = false;
        try {
            this.idleTag = writeCommand("IDLE", (Argument) null);
        } catch (LiteralException lex) {
            v.add(lex.getResponse());
            done = true;
        } catch (Exception ex) {
            v.add(Response.byeResponse(ex));
            done = true;
        }
        while (!done) {
            try {
                r = readResponse();
            } catch (IOException ioex) {
                r = Response.byeResponse(ioex);
            } catch (ProtocolException e) {
            }
            v.add(r);
            if (r.isContinuation() || r.isBYE()) {
                done = true;
            }
        }
        Response[] responses = (Response[]) v.toArray(new Response[v.size()]);
        Response r2 = responses[responses.length - 1];
        notifyResponseHandlers(responses);
        if (!r2.isContinuation()) {
            handleResult(r2);
        }
    }

    public synchronized Response readIdleResponse() {
        Response r;
        if (this.idleTag == null) {
            r = null;
        } else {
            try {
                r = readResponse();
            } catch (IOException ioex) {
                r = Response.byeResponse(ioex);
            } catch (ProtocolException pex) {
                r = Response.byeResponse(pex);
            }
        }
        return r;
    }

    public boolean processIdleResponse(Response r) throws ProtocolException {
        boolean done = false;
        notifyResponseHandlers(new Response[]{r});
        if (r.isBYE()) {
            done = true;
        }
        if (r.isTagged() && r.getTag().equals(this.idleTag)) {
            done = true;
        }
        if (done) {
            this.idleTag = null;
        }
        handleResult(r);
        if (!done) {
            return true;
        }
        return false;
    }

    public void idleAbort() {
        OutputStream os = getOutputStream();
        try {
            os.write(DONE);
            os.flush();
        } catch (Exception ex) {
            this.logger.log(Level.FINEST, "Exception aborting IDLE", (Throwable) ex);
        }
    }

    /* renamed from: id */
    public Map<String, String> mo13040id(Map<String, String> clientParams) throws ProtocolException {
        if (!hasCapability("ID")) {
            throw new BadCommandException("ID not supported");
        }
        Response[] r = command("ID", C0686ID.getArgumentList(clientParams));
        C0686ID id = null;
        Response response = r[r.length - 1];
        if (response.isOK()) {
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("ID")) {
                        if (id == null) {
                            id = new C0686ID(ir);
                        }
                        r[i] = null;
                    }
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        if (id == null) {
            return null;
        }
        return id.getServerParams();
    }
}
