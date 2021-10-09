package javax.mail;

import androidx.core.app.NotificationCompat;
import com.sun.mail.util.DefaultProvider;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.MailLogger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import javax.mail.Provider;

public final class Session {
    private static final String confDir;
    private static Session defaultSession = null;
    /* access modifiers changed from: private */
    public final Properties addressMap = new Properties();
    private final Hashtable<URLName, PasswordAuthentication> authTable = new Hashtable<>();
    private final Authenticator authenticator;
    private boolean debug = false;
    private MailLogger logger;
    private PrintStream out;
    private final Properties props;
    private final List<Provider> providers = new ArrayList();
    private final Map<String, Provider> providersByClassName = new HashMap();
    private final Map<String, Provider> providersByProtocol = new HashMap();

    /* renamed from: q */
    private final EventQueue f300q;

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    static {
        /*
            r2 = 0
            defaultSession = r2
            r1 = 0
            javax.mail.Session$1 r2 = new javax.mail.Session$1     // Catch:{ Exception -> 0x0014 }
            r2.<init>()     // Catch:{ Exception -> 0x0014 }
            java.lang.Object r2 = java.security.AccessController.doPrivileged(r2)     // Catch:{ Exception -> 0x0014 }
            r0 = r2
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ Exception -> 0x0014 }
            r1 = r0
        L_0x0011:
            confDir = r1
            return
        L_0x0014:
            r2 = move-exception
            goto L_0x0011
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.Session.<clinit>():void");
    }

    private Session(Properties props2, Authenticator authenticator2) {
        Class<?> cl;
        this.props = props2;
        this.authenticator = authenticator2;
        if (Boolean.valueOf(props2.getProperty("mail.debug")).booleanValue()) {
            this.debug = true;
        }
        initLogger();
        this.logger.log(Level.CONFIG, "Jakarta Mail version {0}", (Object) Version.version);
        if (authenticator2 != null) {
            cl = authenticator2.getClass();
        } else {
            cl = getClass();
        }
        loadProviders(cl);
        loadAddressMap(cl);
        this.f300q = new EventQueue((Executor) props2.get("mail.event.executor"));
    }

    private final synchronized void initLogger() {
        this.logger = new MailLogger(getClass(), "DEBUG", this.debug, getDebugOut());
    }

    public static Session getInstance(Properties props2, Authenticator authenticator2) {
        return new Session(props2, authenticator2);
    }

    public static Session getInstance(Properties props2) {
        return new Session(props2, (Authenticator) null);
    }

    public static synchronized Session getDefaultInstance(Properties props2, Authenticator authenticator2) {
        Session session;
        synchronized (Session.class) {
            if (defaultSession == null) {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    security.checkSetFactory();
                }
                defaultSession = new Session(props2, authenticator2);
            } else if (defaultSession.authenticator != authenticator2 && (defaultSession.authenticator == null || authenticator2 == null || defaultSession.authenticator.getClass().getClassLoader() != authenticator2.getClass().getClassLoader())) {
                throw new SecurityException("Access to default session denied");
            }
            session = defaultSession;
        }
        return session;
    }

    public static Session getDefaultInstance(Properties props2) {
        return getDefaultInstance(props2, (Authenticator) null);
    }

    public synchronized void setDebug(boolean debug2) {
        this.debug = debug2;
        initLogger();
        this.logger.log(Level.CONFIG, "setDebug: Jakarta Mail version {0}", (Object) Version.version);
    }

    public synchronized boolean getDebug() {
        return this.debug;
    }

    public synchronized void setDebugOut(PrintStream out2) {
        this.out = out2;
        initLogger();
    }

    public synchronized PrintStream getDebugOut() {
        PrintStream printStream;
        if (this.out == null) {
            printStream = System.out;
        } else {
            printStream = this.out;
        }
        return printStream;
    }

    public synchronized Provider[] getProviders() {
        Provider[] _providers;
        _providers = new Provider[this.providers.size()];
        this.providers.toArray(_providers);
        return _providers;
    }

    public synchronized Provider getProvider(String protocol) throws NoSuchProviderException {
        Provider _provider;
        if (protocol != null) {
            if (protocol.length() > 0) {
                Provider _provider2 = null;
                String _className = this.props.getProperty("mail." + protocol + ".class");
                if (_className != null) {
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine("mail." + protocol + ".class property exists and points to " + _className);
                    }
                    _provider2 = this.providersByClassName.get(_className);
                }
                if (_provider2 != null) {
                    _provider = _provider2;
                } else {
                    Provider _provider3 = this.providersByProtocol.get(protocol);
                    if (_provider3 == null) {
                        throw new NoSuchProviderException("No provider for " + protocol);
                    }
                    if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine("getProvider() returning " + _provider3.toString());
                    }
                    _provider = _provider3;
                }
            }
        }
        throw new NoSuchProviderException("Invalid protocol: null");
        return _provider;
    }

    public synchronized void setProvider(Provider provider) throws NoSuchProviderException {
        if (provider == null) {
            throw new NoSuchProviderException("Can't set null provider");
        }
        this.providersByProtocol.put(provider.getProtocol(), provider);
        this.providersByClassName.put(provider.getClassName(), provider);
        this.props.put("mail." + provider.getProtocol() + ".class", provider.getClassName());
    }

    public Store getStore() throws NoSuchProviderException {
        return getStore(getProperty("mail.store.protocol"));
    }

    public Store getStore(String protocol) throws NoSuchProviderException {
        return getStore(new URLName(protocol, (String) null, -1, (String) null, (String) null, (String) null));
    }

    public Store getStore(URLName url) throws NoSuchProviderException {
        return getStore(getProvider(url.getProtocol()), url);
    }

    public Store getStore(Provider provider) throws NoSuchProviderException {
        return getStore(provider, (URLName) null);
    }

    private Store getStore(Provider provider, URLName url) throws NoSuchProviderException {
        if (provider != null && provider.getType() == Provider.Type.STORE) {
            return (Store) getService(provider, url, Store.class);
        }
        throw new NoSuchProviderException("invalid provider");
    }

    public Folder getFolder(URLName url) throws MessagingException {
        Store store = getStore(url);
        store.connect();
        return store.getFolder(url);
    }

    public Transport getTransport() throws NoSuchProviderException {
        String prot = getProperty("mail.transport.protocol");
        if (prot != null) {
            return getTransport(prot);
        }
        String prot2 = (String) this.addressMap.get("rfc822");
        if (prot2 != null) {
            return getTransport(prot2);
        }
        return getTransport("smtp");
    }

    public Transport getTransport(String protocol) throws NoSuchProviderException {
        return getTransport(new URLName(protocol, (String) null, -1, (String) null, (String) null, (String) null));
    }

    public Transport getTransport(URLName url) throws NoSuchProviderException {
        return getTransport(getProvider(url.getProtocol()), url);
    }

    public Transport getTransport(Provider provider) throws NoSuchProviderException {
        return getTransport(provider, (URLName) null);
    }

    public Transport getTransport(Address address) throws NoSuchProviderException {
        String transportProtocol = getProperty("mail.transport.protocol." + address.getType());
        if (transportProtocol != null) {
            return getTransport(transportProtocol);
        }
        String transportProtocol2 = (String) this.addressMap.get(address.getType());
        if (transportProtocol2 != null) {
            return getTransport(transportProtocol2);
        }
        throw new NoSuchProviderException("No provider for Address type: " + address.getType());
    }

    private Transport getTransport(Provider provider, URLName url) throws NoSuchProviderException {
        if (provider != null && provider.getType() == Provider.Type.TRANSPORT) {
            return (Transport) getService(provider, url, Transport.class);
        }
        throw new NoSuchProviderException("invalid provider");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004b, code lost:
        if (r21.isAssignableFrom(r17) == false) goto L_0x004d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private <T extends javax.mail.Service> T getService(javax.mail.Provider r19, javax.mail.URLName r20, java.lang.Class<T> r21) throws javax.mail.NoSuchProviderException {
        /*
            r18 = this;
            if (r19 != 0) goto L_0x000a
            javax.mail.NoSuchProviderException r2 = new javax.mail.NoSuchProviderException
            java.lang.String r3 = "null"
            r2.<init>(r3)
            throw r2
        L_0x000a:
            if (r20 != 0) goto L_0x001c
            javax.mail.URLName r20 = new javax.mail.URLName
            java.lang.String r3 = r19.getProtocol()
            r4 = 0
            r5 = -1
            r6 = 0
            r7 = 0
            r8 = 0
            r2 = r20
            r2.<init>(r3, r4, r5, r6, r7, r8)
        L_0x001c:
            r16 = 0
            r0 = r18
            javax.mail.Authenticator r2 = r0.authenticator
            if (r2 == 0) goto L_0x00d3
            r0 = r18
            javax.mail.Authenticator r2 = r0.authenticator
            java.lang.Class r2 = r2.getClass()
            java.lang.ClassLoader r11 = r2.getClassLoader()
        L_0x0030:
            r17 = 0
            java.lang.ClassLoader r10 = getContextClassLoader()     // Catch:{ Exception -> 0x0085 }
            if (r10 == 0) goto L_0x0041
            java.lang.String r2 = r19.getClassName()     // Catch:{ ClassNotFoundException -> 0x011e }
            r3 = 0
            java.lang.Class r17 = java.lang.Class.forName(r2, r3, r10)     // Catch:{ ClassNotFoundException -> 0x011e }
        L_0x0041:
            if (r17 == 0) goto L_0x004d
            r0 = r21
            r1 = r17
            boolean r2 = r0.isAssignableFrom(r1)     // Catch:{ Exception -> 0x0085 }
            if (r2 != 0) goto L_0x0056
        L_0x004d:
            java.lang.String r2 = r19.getClassName()     // Catch:{ Exception -> 0x0085 }
            r3 = 0
            java.lang.Class r17 = java.lang.Class.forName(r2, r3, r11)     // Catch:{ Exception -> 0x0085 }
        L_0x0056:
            r0 = r21
            r1 = r17
            boolean r2 = r0.isAssignableFrom(r1)     // Catch:{ Exception -> 0x0085 }
            if (r2 != 0) goto L_0x00dd
            java.lang.ClassCastException r2 = new java.lang.ClassCastException     // Catch:{ Exception -> 0x0085 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0085 }
            r3.<init>()     // Catch:{ Exception -> 0x0085 }
            java.lang.String r4 = r21.getName()     // Catch:{ Exception -> 0x0085 }
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0085 }
            java.lang.String r4 = " "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0085 }
            java.lang.String r4 = r17.getName()     // Catch:{ Exception -> 0x0085 }
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x0085 }
            java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0085 }
            r2.<init>(r3)     // Catch:{ Exception -> 0x0085 }
            throw r2     // Catch:{ Exception -> 0x0085 }
        L_0x0085:
            r14 = move-exception
            java.lang.String r2 = r19.getClassName()     // Catch:{ Exception -> 0x00bd }
            java.lang.Class r17 = java.lang.Class.forName(r2)     // Catch:{ Exception -> 0x00bd }
            r0 = r21
            r1 = r17
            boolean r2 = r0.isAssignableFrom(r1)     // Catch:{ Exception -> 0x00bd }
            if (r2 != 0) goto L_0x00dd
            java.lang.ClassCastException r2 = new java.lang.ClassCastException     // Catch:{ Exception -> 0x00bd }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00bd }
            r3.<init>()     // Catch:{ Exception -> 0x00bd }
            java.lang.String r4 = r21.getName()     // Catch:{ Exception -> 0x00bd }
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x00bd }
            java.lang.String r4 = " "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x00bd }
            java.lang.String r4 = r17.getName()     // Catch:{ Exception -> 0x00bd }
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ Exception -> 0x00bd }
            java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x00bd }
            r2.<init>(r3)     // Catch:{ Exception -> 0x00bd }
            throw r2     // Catch:{ Exception -> 0x00bd }
        L_0x00bd:
            r13 = move-exception
            r0 = r18
            com.sun.mail.util.MailLogger r2 = r0.logger
            java.util.logging.Level r3 = java.util.logging.Level.FINE
            java.lang.String r4 = "Exception loading provider"
            r2.log((java.util.logging.Level) r3, (java.lang.String) r4, (java.lang.Throwable) r13)
            javax.mail.NoSuchProviderException r2 = new javax.mail.NoSuchProviderException
            java.lang.String r3 = r19.getProtocol()
            r2.<init>(r3)
            throw r2
        L_0x00d3:
            java.lang.Class r2 = r18.getClass()
            java.lang.ClassLoader r11 = r2.getClassLoader()
            goto L_0x0030
        L_0x00dd:
            r2 = 2
            java.lang.Class[] r9 = new java.lang.Class[r2]     // Catch:{ Exception -> 0x0108 }
            r2 = 0
            java.lang.Class<javax.mail.Session> r3 = javax.mail.Session.class
            r9[r2] = r3     // Catch:{ Exception -> 0x0108 }
            r2 = 1
            java.lang.Class<javax.mail.URLName> r3 = javax.mail.URLName.class
            r9[r2] = r3     // Catch:{ Exception -> 0x0108 }
            r0 = r17
            java.lang.reflect.Constructor r12 = r0.getConstructor(r9)     // Catch:{ Exception -> 0x0108 }
            r2 = 2
            java.lang.Object[] r15 = new java.lang.Object[r2]     // Catch:{ Exception -> 0x0108 }
            r2 = 0
            r15[r2] = r18     // Catch:{ Exception -> 0x0108 }
            r2 = 1
            r15[r2] = r20     // Catch:{ Exception -> 0x0108 }
            java.lang.Object r16 = r12.newInstance(r15)     // Catch:{ Exception -> 0x0108 }
            r0 = r21
            r1 = r16
            java.lang.Object r2 = r0.cast(r1)
            javax.mail.Service r2 = (javax.mail.Service) r2
            return r2
        L_0x0108:
            r13 = move-exception
            r0 = r18
            com.sun.mail.util.MailLogger r2 = r0.logger
            java.util.logging.Level r3 = java.util.logging.Level.FINE
            java.lang.String r4 = "Exception loading provider"
            r2.log((java.util.logging.Level) r3, (java.lang.String) r4, (java.lang.Throwable) r13)
            javax.mail.NoSuchProviderException r2 = new javax.mail.NoSuchProviderException
            java.lang.String r3 = r19.getProtocol()
            r2.<init>(r3)
            throw r2
        L_0x011e:
            r2 = move-exception
            goto L_0x0041
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.Session.getService(javax.mail.Provider, javax.mail.URLName, java.lang.Class):javax.mail.Service");
    }

    public void setPasswordAuthentication(URLName url, PasswordAuthentication pw) {
        if (pw == null) {
            this.authTable.remove(url);
        } else {
            this.authTable.put(url, pw);
        }
    }

    public PasswordAuthentication getPasswordAuthentication(URLName url) {
        return this.authTable.get(url);
    }

    public PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol, String prompt, String defaultUserName) {
        if (this.authenticator != null) {
            return this.authenticator.requestPasswordAuthentication(addr, port, protocol, prompt, defaultUserName);
        }
        return null;
    }

    public Properties getProperties() {
        return this.props;
    }

    public String getProperty(String name) {
        return this.props.getProperty(name);
    }

    private void loadProviders(Class<?> cl) {
        StreamLoader loader = new StreamLoader() {
            public void load(InputStream is) throws IOException {
                Session.this.loadProvidersFromStream(is);
            }
        };
        try {
            if (confDir != null) {
                loadFile(confDir + "javamail.providers", loader);
            }
        } catch (SecurityException e) {
        }
        Iterator<Provider> it = ServiceLoader.load(Provider.class).iterator();
        while (it.hasNext()) {
            Provider p = it.next();
            if (!p.getClass().isAnnotationPresent(DefaultProvider.class)) {
                addProvider(p);
            }
        }
        loadAllResources("META-INF/javamail.providers", cl, loader);
        loadResource("/META-INF/javamail.default.providers", cl, loader, false);
        Iterator<Provider> it2 = ServiceLoader.load(Provider.class).iterator();
        while (it2.hasNext()) {
            Provider p2 = it2.next();
            if (p2.getClass().isAnnotationPresent(DefaultProvider.class)) {
                addProvider(p2);
            }
        }
        if (this.providers.size() == 0) {
            this.logger.config("failed to load any providers, using defaults");
            addProvider(new Provider(Provider.Type.STORE, "imap", "com.sun.mail.imap.IMAPStore", "Oracle", Version.version));
            addProvider(new Provider(Provider.Type.STORE, "imaps", "com.sun.mail.imap.IMAPSSLStore", "Oracle", Version.version));
            addProvider(new Provider(Provider.Type.STORE, "pop3", "com.sun.mail.pop3.POP3Store", "Oracle", Version.version));
            addProvider(new Provider(Provider.Type.STORE, "pop3s", "com.sun.mail.pop3.POP3SSLStore", "Oracle", Version.version));
            addProvider(new Provider(Provider.Type.TRANSPORT, "smtp", "com.sun.mail.smtp.SMTPTransport", "Oracle", Version.version));
            addProvider(new Provider(Provider.Type.TRANSPORT, "smtps", "com.sun.mail.smtp.SMTPSSLTransport", "Oracle", Version.version));
        }
        if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("Tables of loaded providers");
            this.logger.config("Providers Listed By Class Name: " + this.providersByClassName.toString());
            this.logger.config("Providers Listed By Protocol: " + this.providersByProtocol.toString());
        }
    }

    /* access modifiers changed from: private */
    public void loadProvidersFromStream(InputStream is) throws IOException {
        if (is != null) {
            LineInputStream lis = new LineInputStream(is);
            while (true) {
                String currLine = lis.readLine();
                if (currLine == null) {
                    return;
                }
                if (!currLine.startsWith("#") && currLine.trim().length() != 0) {
                    Provider.Type type = null;
                    String protocol = null;
                    String className = null;
                    String vendor = null;
                    String version = null;
                    StringTokenizer tuples = new StringTokenizer(currLine, ";");
                    while (tuples.hasMoreTokens()) {
                        String currTuple = tuples.nextToken().trim();
                        int sep = currTuple.indexOf("=");
                        if (currTuple.startsWith("protocol=")) {
                            protocol = currTuple.substring(sep + 1);
                        } else if (currTuple.startsWith("type=")) {
                            String strType = currTuple.substring(sep + 1);
                            if (strType.equalsIgnoreCase("store")) {
                                type = Provider.Type.STORE;
                            } else if (strType.equalsIgnoreCase(NotificationCompat.CATEGORY_TRANSPORT)) {
                                type = Provider.Type.TRANSPORT;
                            }
                        } else if (currTuple.startsWith("class=")) {
                            className = currTuple.substring(sep + 1);
                        } else if (currTuple.startsWith("vendor=")) {
                            vendor = currTuple.substring(sep + 1);
                        } else if (currTuple.startsWith("version=")) {
                            version = currTuple.substring(sep + 1);
                        }
                    }
                    if (type == null || protocol == null || className == null || protocol.length() <= 0 || className.length() <= 0) {
                        this.logger.log(Level.CONFIG, "Bad provider entry: {0}", (Object) currLine);
                    } else {
                        addProvider(new Provider(type, protocol, className, vendor, version));
                    }
                }
            }
        }
    }

    public synchronized void addProvider(Provider provider) {
        this.providers.add(provider);
        this.providersByClassName.put(provider.getClassName(), provider);
        if (!this.providersByProtocol.containsKey(provider.getProtocol())) {
            this.providersByProtocol.put(provider.getProtocol(), provider);
        }
    }

    private void loadAddressMap(Class<?> cl) {
        StreamLoader loader = new StreamLoader() {
            public void load(InputStream is) throws IOException {
                Session.this.addressMap.load(is);
            }
        };
        loadResource("/META-INF/javamail.default.address.map", cl, loader, true);
        loadAllResources("META-INF/javamail.address.map", cl, loader);
        try {
            if (confDir != null) {
                loadFile(confDir + "javamail.address.map", loader);
            }
        } catch (SecurityException e) {
        }
        if (this.addressMap.isEmpty()) {
            this.logger.config("failed to load address map, using defaults");
            this.addressMap.put("rfc822", "smtp");
        }
    }

    public synchronized void setProtocolForAddress(String addresstype, String protocol) {
        if (protocol == null) {
            this.addressMap.remove(addresstype);
        } else {
            this.addressMap.put(addresstype, protocol);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0024 A[SYNTHETIC, Splitter:B:13:0x0024] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0035 A[Catch:{ all -> 0x0084 }] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0051 A[SYNTHETIC, Splitter:B:22:0x0051] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0062 A[Catch:{ all -> 0x0084 }] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x007e A[SYNTHETIC, Splitter:B:31:0x007e] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0087 A[SYNTHETIC, Splitter:B:36:0x0087] */
    /* JADX WARNING: Removed duplicated region for block: B:50:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:51:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:26:0x0058=Splitter:B:26:0x0058, B:17:0x002b=Splitter:B:17:0x002b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadFile(java.lang.String r9, javax.mail.StreamLoader r10) {
        /*
            r8 = this;
            r0 = 0
            java.io.BufferedInputStream r1 = new java.io.BufferedInputStream     // Catch:{ FileNotFoundException -> 0x0021, IOException -> 0x002a, SecurityException -> 0x0057 }
            java.io.FileInputStream r4 = new java.io.FileInputStream     // Catch:{ FileNotFoundException -> 0x0021, IOException -> 0x002a, SecurityException -> 0x0057 }
            r4.<init>(r9)     // Catch:{ FileNotFoundException -> 0x0021, IOException -> 0x002a, SecurityException -> 0x0057 }
            r1.<init>(r4)     // Catch:{ FileNotFoundException -> 0x0021, IOException -> 0x002a, SecurityException -> 0x0057 }
            r10.load(r1)     // Catch:{ FileNotFoundException -> 0x0096, IOException -> 0x0093, SecurityException -> 0x0090, all -> 0x008d }
            com.sun.mail.util.MailLogger r4 = r8.logger     // Catch:{ FileNotFoundException -> 0x0096, IOException -> 0x0093, SecurityException -> 0x0090, all -> 0x008d }
            java.util.logging.Level r5 = java.util.logging.Level.CONFIG     // Catch:{ FileNotFoundException -> 0x0096, IOException -> 0x0093, SecurityException -> 0x0090, all -> 0x008d }
            java.lang.String r6 = "successfully loaded file: {0}"
            r4.log((java.util.logging.Level) r5, (java.lang.String) r6, (java.lang.Object) r9)     // Catch:{ FileNotFoundException -> 0x0096, IOException -> 0x0093, SecurityException -> 0x0090, all -> 0x008d }
            if (r1 == 0) goto L_0x001c
            r1.close()     // Catch:{ IOException -> 0x001e }
        L_0x001c:
            r0 = r1
        L_0x001d:
            return
        L_0x001e:
            r4 = move-exception
            r0 = r1
            goto L_0x001d
        L_0x0021:
            r4 = move-exception
        L_0x0022:
            if (r0 == 0) goto L_0x001d
            r0.close()     // Catch:{ IOException -> 0x0028 }
            goto L_0x001d
        L_0x0028:
            r4 = move-exception
            goto L_0x001d
        L_0x002a:
            r2 = move-exception
        L_0x002b:
            com.sun.mail.util.MailLogger r4 = r8.logger     // Catch:{ all -> 0x0084 }
            java.util.logging.Level r5 = java.util.logging.Level.CONFIG     // Catch:{ all -> 0x0084 }
            boolean r4 = r4.isLoggable(r5)     // Catch:{ all -> 0x0084 }
            if (r4 == 0) goto L_0x004f
            com.sun.mail.util.MailLogger r4 = r8.logger     // Catch:{ all -> 0x0084 }
            java.util.logging.Level r5 = java.util.logging.Level.CONFIG     // Catch:{ all -> 0x0084 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0084 }
            r6.<init>()     // Catch:{ all -> 0x0084 }
            java.lang.String r7 = "not loading file: "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0084 }
            java.lang.StringBuilder r6 = r6.append(r9)     // Catch:{ all -> 0x0084 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0084 }
            r4.log((java.util.logging.Level) r5, (java.lang.String) r6, (java.lang.Throwable) r2)     // Catch:{ all -> 0x0084 }
        L_0x004f:
            if (r0 == 0) goto L_0x001d
            r0.close()     // Catch:{ IOException -> 0x0055 }
            goto L_0x001d
        L_0x0055:
            r4 = move-exception
            goto L_0x001d
        L_0x0057:
            r3 = move-exception
        L_0x0058:
            com.sun.mail.util.MailLogger r4 = r8.logger     // Catch:{ all -> 0x0084 }
            java.util.logging.Level r5 = java.util.logging.Level.CONFIG     // Catch:{ all -> 0x0084 }
            boolean r4 = r4.isLoggable(r5)     // Catch:{ all -> 0x0084 }
            if (r4 == 0) goto L_0x007c
            com.sun.mail.util.MailLogger r4 = r8.logger     // Catch:{ all -> 0x0084 }
            java.util.logging.Level r5 = java.util.logging.Level.CONFIG     // Catch:{ all -> 0x0084 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0084 }
            r6.<init>()     // Catch:{ all -> 0x0084 }
            java.lang.String r7 = "not loading file: "
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0084 }
            java.lang.StringBuilder r6 = r6.append(r9)     // Catch:{ all -> 0x0084 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0084 }
            r4.log((java.util.logging.Level) r5, (java.lang.String) r6, (java.lang.Throwable) r3)     // Catch:{ all -> 0x0084 }
        L_0x007c:
            if (r0 == 0) goto L_0x001d
            r0.close()     // Catch:{ IOException -> 0x0082 }
            goto L_0x001d
        L_0x0082:
            r4 = move-exception
            goto L_0x001d
        L_0x0084:
            r4 = move-exception
        L_0x0085:
            if (r0 == 0) goto L_0x008a
            r0.close()     // Catch:{ IOException -> 0x008b }
        L_0x008a:
            throw r4
        L_0x008b:
            r5 = move-exception
            goto L_0x008a
        L_0x008d:
            r4 = move-exception
            r0 = r1
            goto L_0x0085
        L_0x0090:
            r3 = move-exception
            r0 = r1
            goto L_0x0058
        L_0x0093:
            r2 = move-exception
            r0 = r1
            goto L_0x002b
        L_0x0096:
            r4 = move-exception
            r0 = r1
            goto L_0x0022
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.Session.loadFile(java.lang.String, javax.mail.StreamLoader):void");
    }

    private void loadResource(String name, Class<?> cl, StreamLoader loader, boolean expected) {
        InputStream clis = null;
        try {
            clis = getResourceAsStream(cl, name);
            if (clis != null) {
                loader.load(clis);
                this.logger.log(Level.CONFIG, "successfully loaded resource: {0}", (Object) name);
            } else if (expected) {
                this.logger.log(Level.WARNING, "expected resource not found: {0}", (Object) name);
            }
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e2) {
            this.logger.log(Level.CONFIG, "Exception loading resource", (Throwable) e2);
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e3) {
                }
            }
        } catch (SecurityException sex) {
            this.logger.log(Level.CONFIG, "Exception loading resource", (Throwable) sex);
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e4) {
                }
            }
        } catch (Throwable th) {
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
    }

    private void loadAllResources(String name, Class<?> cl, StreamLoader loader) {
        URL[] urls;
        boolean anyLoaded = false;
        try {
            ClassLoader cld = getContextClassLoader();
            if (cld == null) {
                cld = cl.getClassLoader();
            }
            if (cld != null) {
                urls = getResources(cld, name);
            } else {
                urls = getSystemResources(name);
            }
            if (urls != null) {
                for (URL url : urls) {
                    InputStream clis = null;
                    this.logger.log(Level.CONFIG, "URL {0}", (Object) url);
                    try {
                        clis = openStream(url);
                        if (clis != null) {
                            loader.load(clis);
                            anyLoaded = true;
                            this.logger.log(Level.CONFIG, "successfully loaded resource: {0}", (Object) url);
                        } else {
                            this.logger.log(Level.CONFIG, "not loading resource: {0}", (Object) url);
                        }
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e) {
                            }
                        }
                    } catch (FileNotFoundException e2) {
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e3) {
                            }
                        }
                    } catch (IOException ioex) {
                        this.logger.log(Level.CONFIG, "Exception loading resource", (Throwable) ioex);
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e4) {
                            }
                        }
                    } catch (SecurityException sex) {
                        this.logger.log(Level.CONFIG, "Exception loading resource", (Throwable) sex);
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e5) {
                            }
                        }
                    } catch (Throwable th) {
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e6) {
                            }
                        }
                        throw th;
                    }
                }
            }
        } catch (Exception ex) {
            this.logger.log(Level.CONFIG, "Exception loading resource", (Throwable) ex);
        }
        if (!anyLoaded) {
            loadResource("/" + name, cl, loader, false);
        }
    }

    static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                try {
                    return Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                    return null;
                }
            }
        });
    }

    private static InputStream getResourceAsStream(final Class<?> c, final String name) throws IOException {
        try {
            return (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                public InputStream run() throws IOException {
                    try {
                        return c.getResourceAsStream(name);
                    } catch (RuntimeException e) {
                        IOException ioex = new IOException("ClassLoader.getResourceAsStream failed");
                        ioex.initCause(e);
                        throw ioex;
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((IOException) e.getException());
        }
    }

    private static URL[] getResources(final ClassLoader cl, final String name) {
        return (URL[]) AccessController.doPrivileged(new PrivilegedAction<URL[]>() {
            public URL[] run() {
                try {
                    List<URL> v = Collections.list(cl.getResources(name));
                    if (v.isEmpty()) {
                        return null;
                    }
                    URL[] ret = new URL[v.size()];
                    v.toArray(ret);
                    return ret;
                } catch (IOException | SecurityException e) {
                    return null;
                }
            }
        });
    }

    private static URL[] getSystemResources(final String name) {
        return (URL[]) AccessController.doPrivileged(new PrivilegedAction<URL[]>() {
            public URL[] run() {
                try {
                    List<URL> v = Collections.list(ClassLoader.getSystemResources(name));
                    if (v.isEmpty()) {
                        return null;
                    }
                    URL[] ret = new URL[v.size()];
                    v.toArray(ret);
                    return ret;
                } catch (IOException | SecurityException e) {
                    return null;
                }
            }
        });
    }

    private static InputStream openStream(final URL url) throws IOException {
        try {
            return (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                public InputStream run() throws IOException {
                    return url.openStream();
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((IOException) e.getException());
        }
    }

    /* access modifiers changed from: package-private */
    public EventQueue getEventQueue() {
        return this.f300q;
    }
}
