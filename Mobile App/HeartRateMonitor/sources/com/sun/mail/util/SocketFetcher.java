package com.sun.mail.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SocketFetcher {
    private static MailLogger logger = new MailLogger(SocketFetcher.class, "socket", "DEBUG SocketFetcher", PropUtil.getBooleanSystemProperty("mail.socket.debug", false), System.out);

    private SocketFetcher() {
    }

    public static Socket getSocket(String host, int port, Properties props, String prefix, boolean useSSL) throws IOException {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("getSocket, host " + host + ", port " + port + ", prefix " + prefix + ", useSSL " + useSSL);
        }
        if (prefix == null) {
            prefix = "socket";
        }
        if (props == null) {
            props = new Properties();
        }
        int cto = PropUtil.getIntProperty(props, prefix + ".connectiontimeout", -1);
        Socket socket = null;
        String localaddrstr = props.getProperty(prefix + ".localaddress", (String) null);
        InetAddress localaddr = null;
        if (localaddrstr != null) {
            localaddr = InetAddress.getByName(localaddrstr);
        }
        int localport = PropUtil.getIntProperty(props, prefix + ".localport", 0);
        boolean fb = PropUtil.getBooleanProperty(props, prefix + ".socketFactory.fallback", true);
        int to = PropUtil.getIntProperty(props, prefix + ".timeout", -1);
        SocketFactory sf = null;
        String sfPortName = null;
        if (useSSL) {
            try {
                Object sfo = props.get(prefix + ".ssl.socketFactory");
                if (sfo instanceof SocketFactory) {
                    sf = (SocketFactory) sfo;
                    String sfErr = "SSL socket factory instance " + sf;
                }
                if (sf == null) {
                    String sfClass = props.getProperty(prefix + ".ssl.socketFactory.class");
                    sf = getSocketFactory(sfClass);
                    String sfErr2 = "SSL socket factory class " + sfClass;
                }
                sfPortName = ".ssl.socketFactory.port";
            } catch (SocketTimeoutException sex) {
                throw sex;
            } catch (Exception e) {
                ex = e;
                if (!fb) {
                    if (ex instanceof InvocationTargetException) {
                        Throwable t = ((InvocationTargetException) ex).getTargetException();
                        if (t instanceof Exception) {
                            ex = (Exception) t;
                        }
                    }
                    if (ex instanceof IOException) {
                        throw ((IOException) ex);
                    }
                    throw new SocketConnectException("Using " + "unknown socket factory", ex, host, -1, cto);
                }
            }
        }
        if (sf == null) {
            Object sfo2 = props.get(prefix + ".socketFactory");
            if (sfo2 instanceof SocketFactory) {
                sf = (SocketFactory) sfo2;
                String sfErr3 = "socket factory instance " + sf;
            }
            if (sf == null) {
                String sfClass2 = props.getProperty(prefix + ".socketFactory.class");
                sf = getSocketFactory(sfClass2);
                String sfErr4 = "socket factory class " + sfClass2;
            }
            sfPortName = ".socketFactory.port";
        }
        if (sf != null) {
            int sfPort = PropUtil.getIntProperty(props, prefix + sfPortName, -1);
            if (sfPort == -1) {
                sfPort = port;
            }
            socket = createSocket(localaddr, localport, host, sfPort, cto, to, props, prefix, sf, useSSL);
        }
        if (socket == null) {
            return createSocket(localaddr, localport, host, port, cto, to, props, prefix, (SocketFactory) null, useSSL);
        }
        if (to < 0) {
            return socket;
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("set socket read timeout " + to);
        }
        socket.setSoTimeout(to);
        return socket;
    }

    public static Socket getSocket(String host, int port, Properties props, String prefix) throws IOException {
        return getSocket(host, port, props, prefix, false);
    }

    private static Socket createSocket(InetAddress localaddr, int localport, String host, int port, int cto, int to, Properties props, String prefix, SocketFactory sf, boolean useSSL) throws IOException {
        Socket socket;
        Socket socket2;
        SSLSocketFactory ssf;
        Socket socket3 = null;
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("create socket: prefix " + prefix + ", localaddr " + localaddr + ", localport " + localport + ", host " + host + ", port " + port + ", connection timeout " + cto + ", timeout " + to + ", socket factory " + sf + ", useSSL " + useSSL);
        }
        String proxyHost = props.getProperty(prefix + ".proxy.host", (String) null);
        String proxyUser = props.getProperty(prefix + ".proxy.user", (String) null);
        String proxyPassword = props.getProperty(prefix + ".proxy.password", (String) null);
        int proxyPort = 80;
        String socksHost = null;
        int socksPort = 1080;
        String err = null;
        if (proxyHost != null) {
            int i = proxyHost.indexOf(58);
            if (i >= 0) {
                try {
                    proxyPort = Integer.parseInt(proxyHost.substring(i + 1));
                } catch (NumberFormatException e) {
                }
                proxyHost = proxyHost.substring(0, i);
            }
            proxyPort = PropUtil.getIntProperty(props, prefix + ".proxy.port", proxyPort);
            err = "Using web proxy host, port: " + proxyHost + ", " + proxyPort;
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("web proxy host " + proxyHost + ", port " + proxyPort);
                if (proxyUser != null) {
                    logger.finer("web proxy user " + proxyUser + ", password " + (proxyPassword == null ? "<null>" : "<non-null>"));
                }
            }
        } else {
            socksHost = props.getProperty(prefix + ".socks.host", (String) null);
            if (socksHost != null) {
                int i2 = socksHost.indexOf(58);
                if (i2 >= 0) {
                    try {
                        socksPort = Integer.parseInt(socksHost.substring(i2 + 1));
                    } catch (NumberFormatException e2) {
                    }
                    socksHost = socksHost.substring(0, i2);
                }
                socksPort = PropUtil.getIntProperty(props, prefix + ".socks.port", socksPort);
                err = "Using SOCKS host, port: " + socksHost + ", " + socksPort;
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("socks host " + socksHost + ", port " + socksPort);
                }
            }
        }
        if (sf != null && !(sf instanceof SSLSocketFactory)) {
            socket3 = sf.createSocket();
        }
        if (socket3 != null) {
            socket = socket3;
        } else if (socksHost != null) {
            socket = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksHost, socksPort)));
        } else {
            if (PropUtil.getBooleanProperty(props, prefix + ".usesocketchannels", false)) {
                logger.finer("using SocketChannels");
                socket = SocketChannel.open().socket();
            } else {
                socket = new Socket();
            }
        }
        if (to >= 0) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("set socket read timeout " + to);
            }
            socket.setSoTimeout(to);
        }
        int writeTimeout = PropUtil.getIntProperty(props, prefix + ".writetimeout", -1);
        if (writeTimeout != -1) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("set socket write timeout " + writeTimeout);
            }
            socket2 = new WriteTimeoutSocket(socket, writeTimeout);
        } else {
            socket2 = socket;
        }
        if (localaddr != null) {
            socket2.bind(new InetSocketAddress(localaddr, localport));
        }
        try {
            logger.finest("connecting...");
            if (proxyHost != null) {
                proxyConnect(socket2, proxyHost, proxyPort, proxyUser, proxyPassword, host, port, cto);
            } else if (cto >= 0) {
                socket2.connect(new InetSocketAddress(host, port), cto);
            } else {
                socket2.connect(new InetSocketAddress(host, port));
            }
            logger.finest("success!");
            if ((useSSL || (sf instanceof SSLSocketFactory)) && !(socket2 instanceof SSLSocket)) {
                String trusted = props.getProperty(prefix + ".ssl.trust");
                if (trusted != null) {
                    try {
                        MailSSLSocketFactory msf = new MailSSLSocketFactory();
                        if (trusted.equals("*")) {
                            msf.setTrustAllHosts(true);
                        } else {
                            msf.setTrustedHosts(trusted.split("\\s+"));
                        }
                        ssf = msf;
                    } catch (GeneralSecurityException gex) {
                        IOException iOException = new IOException("Can't create MailSSLSocketFactory");
                        iOException.initCause(gex);
                        throw iOException;
                    }
                } else if (sf instanceof SSLSocketFactory) {
                    ssf = (SSLSocketFactory) sf;
                } else {
                    ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                }
                socket2 = ssf.createSocket(socket2, host, port, true);
                sf = ssf;
            }
            configureSSLSocket(socket2, host, props, prefix, sf);
            return socket2;
        } catch (IOException ex) {
            logger.log(Level.FINEST, "connection failed", (Throwable) ex);
            throw new SocketConnectException(err, ex, host, port, cto);
        }
    }

    private static SocketFactory getSocketFactory(String sfClass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (sfClass == null || sfClass.length() == 0) {
            return null;
        }
        ClassLoader cl = getContextClassLoader();
        Class<?> clsSockFact = null;
        if (cl != null) {
            try {
                clsSockFact = Class.forName(sfClass, false, cl);
            } catch (ClassNotFoundException e) {
            }
        }
        if (clsSockFact == null) {
            clsSockFact = Class.forName(sfClass);
        }
        return (SocketFactory) clsSockFact.getMethod("getDefault", new Class[0]).invoke(new Object(), new Object[0]);
    }

    @Deprecated
    public static Socket startTLS(Socket socket) throws IOException {
        return startTLS(socket, new Properties(), "socket");
    }

    @Deprecated
    public static Socket startTLS(Socket socket, Properties props, String prefix) throws IOException {
        return startTLS(socket, socket.getInetAddress().getHostName(), props, prefix);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v0, resolved type: com.sun.mail.util.MailSSLSocketFactory} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v1, resolved type: com.sun.mail.util.MailSSLSocketFactory} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v2, resolved type: com.sun.mail.util.MailSSLSocketFactory} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v3, resolved type: com.sun.mail.util.MailSSLSocketFactory} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v5, resolved type: com.sun.mail.util.MailSSLSocketFactory} */
    /* JADX WARNING: type inference failed for: r14v0, types: [java.lang.Throwable] */
    /* JADX WARNING: type inference failed for: r16v27, types: [javax.net.SocketFactory] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.net.Socket startTLS(java.net.Socket r19, java.lang.String r20, java.util.Properties r21, java.lang.String r22) throws java.io.IOException {
        /*
            int r8 = r19.getPort()
            com.sun.mail.util.MailLogger r16 = logger
            java.util.logging.Level r17 = java.util.logging.Level.FINER
            boolean r16 = r16.isLoggable(r17)
            if (r16 == 0) goto L_0x0036
            com.sun.mail.util.MailLogger r16 = logger
            java.lang.StringBuilder r17 = new java.lang.StringBuilder
            r17.<init>()
            java.lang.String r18 = "startTLS host "
            java.lang.StringBuilder r17 = r17.append(r18)
            r0 = r17
            r1 = r20
            java.lang.StringBuilder r17 = r0.append(r1)
            java.lang.String r18 = ", port "
            java.lang.StringBuilder r17 = r17.append(r18)
            r0 = r17
            java.lang.StringBuilder r17 = r0.append(r8)
            java.lang.String r17 = r17.toString()
            r16.finer(r17)
        L_0x0036:
            java.lang.String r11 = "unknown socket factory"
            r13 = 0
            r9 = 0
            java.lang.StringBuilder r16 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01b0 }
            r16.<init>()     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            r1 = r22
            java.lang.StringBuilder r16 = r0.append(r1)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r17 = ".ssl.socketFactory"
            java.lang.StringBuilder r16 = r16.append(r17)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r16 = r16.toString()     // Catch:{ Exception -> 0x01b0 }
            r0 = r21
            r1 = r16
            java.lang.Object r12 = r0.get(r1)     // Catch:{ Exception -> 0x01b0 }
            boolean r0 = r12 instanceof javax.net.SocketFactory     // Catch:{ Exception -> 0x01b0 }
            r16 = r0
            if (r16 == 0) goto L_0x0078
            r0 = r12
            javax.net.SocketFactory r0 = (javax.net.SocketFactory) r0     // Catch:{ Exception -> 0x01b0 }
            r9 = r0
            java.lang.StringBuilder r16 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01b0 }
            r16.<init>()     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r17 = "SSL socket factory instance "
            java.lang.StringBuilder r16 = r16.append(r17)     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            java.lang.StringBuilder r16 = r0.append(r9)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r11 = r16.toString()     // Catch:{ Exception -> 0x01b0 }
        L_0x0078:
            if (r9 != 0) goto L_0x00b2
            java.lang.StringBuilder r16 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01b0 }
            r16.<init>()     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            r1 = r22
            java.lang.StringBuilder r16 = r0.append(r1)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r17 = ".ssl.socketFactory.class"
            java.lang.StringBuilder r16 = r16.append(r17)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r16 = r16.toString()     // Catch:{ Exception -> 0x01b0 }
            r0 = r21
            r1 = r16
            java.lang.String r10 = r0.getProperty(r1)     // Catch:{ Exception -> 0x01b0 }
            javax.net.SocketFactory r9 = getSocketFactory(r10)     // Catch:{ Exception -> 0x01b0 }
            java.lang.StringBuilder r16 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01b0 }
            r16.<init>()     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r17 = "SSL socket factory class "
            java.lang.StringBuilder r16 = r16.append(r17)     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            java.lang.StringBuilder r16 = r0.append(r10)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r11 = r16.toString()     // Catch:{ Exception -> 0x01b0 }
        L_0x00b2:
            if (r9 == 0) goto L_0x00be
            boolean r0 = r9 instanceof javax.net.ssl.SSLSocketFactory     // Catch:{ Exception -> 0x01b0 }
            r16 = r0
            if (r16 == 0) goto L_0x00be
            r0 = r9
            javax.net.ssl.SSLSocketFactory r0 = (javax.net.ssl.SSLSocketFactory) r0     // Catch:{ Exception -> 0x01b0 }
            r13 = r0
        L_0x00be:
            if (r13 != 0) goto L_0x0144
            java.lang.StringBuilder r16 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01b0 }
            r16.<init>()     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            r1 = r22
            java.lang.StringBuilder r16 = r0.append(r1)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r17 = ".socketFactory"
            java.lang.StringBuilder r16 = r16.append(r17)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r16 = r16.toString()     // Catch:{ Exception -> 0x01b0 }
            r0 = r21
            r1 = r16
            java.lang.Object r12 = r0.get(r1)     // Catch:{ Exception -> 0x01b0 }
            boolean r0 = r12 instanceof javax.net.SocketFactory     // Catch:{ Exception -> 0x01b0 }
            r16 = r0
            if (r16 == 0) goto L_0x00fe
            r0 = r12
            javax.net.SocketFactory r0 = (javax.net.SocketFactory) r0     // Catch:{ Exception -> 0x01b0 }
            r9 = r0
            java.lang.StringBuilder r16 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01b0 }
            r16.<init>()     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r17 = "socket factory instance "
            java.lang.StringBuilder r16 = r16.append(r17)     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            java.lang.StringBuilder r16 = r0.append(r9)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r11 = r16.toString()     // Catch:{ Exception -> 0x01b0 }
        L_0x00fe:
            if (r9 != 0) goto L_0x0138
            java.lang.StringBuilder r16 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01b0 }
            r16.<init>()     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            r1 = r22
            java.lang.StringBuilder r16 = r0.append(r1)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r17 = ".socketFactory.class"
            java.lang.StringBuilder r16 = r16.append(r17)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r16 = r16.toString()     // Catch:{ Exception -> 0x01b0 }
            r0 = r21
            r1 = r16
            java.lang.String r10 = r0.getProperty(r1)     // Catch:{ Exception -> 0x01b0 }
            javax.net.SocketFactory r9 = getSocketFactory(r10)     // Catch:{ Exception -> 0x01b0 }
            java.lang.StringBuilder r16 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01b0 }
            r16.<init>()     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r17 = "socket factory class "
            java.lang.StringBuilder r16 = r16.append(r17)     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            java.lang.StringBuilder r16 = r0.append(r10)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r11 = r16.toString()     // Catch:{ Exception -> 0x01b0 }
        L_0x0138:
            if (r9 == 0) goto L_0x0144
            boolean r0 = r9 instanceof javax.net.ssl.SSLSocketFactory     // Catch:{ Exception -> 0x01b0 }
            r16 = r0
            if (r16 == 0) goto L_0x0144
            r0 = r9
            javax.net.ssl.SSLSocketFactory r0 = (javax.net.ssl.SSLSocketFactory) r0     // Catch:{ Exception -> 0x01b0 }
            r13 = r0
        L_0x0144:
            if (r13 != 0) goto L_0x017e
            java.lang.StringBuilder r16 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x01b0 }
            r16.<init>()     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            r1 = r22
            java.lang.StringBuilder r16 = r0.append(r1)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r17 = ".ssl.trust"
            java.lang.StringBuilder r16 = r16.append(r17)     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r16 = r16.toString()     // Catch:{ Exception -> 0x01b0 }
            r0 = r21
            r1 = r16
            java.lang.String r15 = r0.getProperty(r1)     // Catch:{ Exception -> 0x01b0 }
            if (r15 == 0) goto L_0x01d1
            com.sun.mail.util.MailSSLSocketFactory r7 = new com.sun.mail.util.MailSSLSocketFactory     // Catch:{ GeneralSecurityException -> 0x01a2 }
            r7.<init>()     // Catch:{ GeneralSecurityException -> 0x01a2 }
            java.lang.String r16 = "*"
            boolean r16 = r15.equals(r16)     // Catch:{ GeneralSecurityException -> 0x01a2 }
            if (r16 == 0) goto L_0x0196
            r16 = 1
            r0 = r16
            r7.setTrustAllHosts(r0)     // Catch:{ GeneralSecurityException -> 0x01a2 }
        L_0x017b:
            r13 = r7
            java.lang.String r11 = "mail SSL socket factory"
        L_0x017e:
            r16 = 1
            r0 = r19
            r1 = r20
            r2 = r16
            java.net.Socket r19 = r13.createSocket(r0, r1, r8, r2)     // Catch:{ Exception -> 0x01b0 }
            r0 = r19
            r1 = r20
            r2 = r21
            r3 = r22
            configureSSLSocket(r0, r1, r2, r3, r13)     // Catch:{ Exception -> 0x01b0 }
            return r19
        L_0x0196:
            java.lang.String r16 = "\\s+"
            java.lang.String[] r16 = r15.split(r16)     // Catch:{ GeneralSecurityException -> 0x01a2 }
            r0 = r16
            r7.setTrustedHosts(r0)     // Catch:{ GeneralSecurityException -> 0x01a2 }
            goto L_0x017b
        L_0x01a2:
            r5 = move-exception
            java.io.IOException r6 = new java.io.IOException     // Catch:{ Exception -> 0x01b0 }
            java.lang.String r16 = "Can't create MailSSLSocketFactory"
            r0 = r16
            r6.<init>(r0)     // Catch:{ Exception -> 0x01b0 }
            r6.initCause(r5)     // Catch:{ Exception -> 0x01b0 }
            throw r6     // Catch:{ Exception -> 0x01b0 }
        L_0x01b0:
            r4 = move-exception
            boolean r0 = r4 instanceof java.lang.reflect.InvocationTargetException
            r16 = r0
            if (r16 == 0) goto L_0x01c8
            r16 = r4
            java.lang.reflect.InvocationTargetException r16 = (java.lang.reflect.InvocationTargetException) r16
            java.lang.Throwable r14 = r16.getTargetException()
            boolean r0 = r14 instanceof java.lang.Exception
            r16 = r0
            if (r16 == 0) goto L_0x01c8
            r4 = r14
            java.lang.Exception r4 = (java.lang.Exception) r4
        L_0x01c8:
            boolean r0 = r4 instanceof java.io.IOException
            r16 = r0
            if (r16 == 0) goto L_0x01dd
            java.io.IOException r4 = (java.io.IOException) r4
            throw r4
        L_0x01d1:
            javax.net.SocketFactory r16 = javax.net.ssl.SSLSocketFactory.getDefault()     // Catch:{ Exception -> 0x01b0 }
            r0 = r16
            javax.net.ssl.SSLSocketFactory r0 = (javax.net.ssl.SSLSocketFactory) r0     // Catch:{ Exception -> 0x01b0 }
            r13 = r0
            java.lang.String r11 = "default SSL socket factory"
            goto L_0x017e
        L_0x01dd:
            java.io.IOException r6 = new java.io.IOException
            java.lang.StringBuilder r16 = new java.lang.StringBuilder
            r16.<init>()
            java.lang.String r17 = "Exception in startTLS using "
            java.lang.StringBuilder r16 = r16.append(r17)
            r0 = r16
            java.lang.StringBuilder r16 = r0.append(r11)
            java.lang.String r17 = ": host, port: "
            java.lang.StringBuilder r16 = r16.append(r17)
            r0 = r16
            r1 = r20
            java.lang.StringBuilder r16 = r0.append(r1)
            java.lang.String r17 = ", "
            java.lang.StringBuilder r16 = r16.append(r17)
            r0 = r16
            java.lang.StringBuilder r16 = r0.append(r8)
            java.lang.String r17 = "; Exception: "
            java.lang.StringBuilder r16 = r16.append(r17)
            r0 = r16
            java.lang.StringBuilder r16 = r0.append(r4)
            java.lang.String r16 = r16.toString()
            r0 = r16
            r6.<init>(r0)
            r6.initCause(r4)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.SocketFetcher.startTLS(java.net.Socket, java.lang.String, java.util.Properties, java.lang.String):java.net.Socket");
    }

    private static void configureSSLSocket(Socket socket, String host, Properties props, String prefix, SocketFactory sf) throws IOException {
        if (socket instanceof SSLSocket) {
            SSLSocket sslsocket = (SSLSocket) socket;
            String protocols = props.getProperty(prefix + ".ssl.protocols", (String) null);
            if (protocols != null) {
                sslsocket.setEnabledProtocols(stringArray(protocols));
            } else {
                String[] prots = sslsocket.getEnabledProtocols();
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("SSL enabled protocols before " + Arrays.asList(prots));
                }
                List<String> eprots = new ArrayList<>();
                for (int i = 0; i < prots.length; i++) {
                    if (prots[i] != null && !prots[i].startsWith("SSL")) {
                        eprots.add(prots[i]);
                    }
                }
                sslsocket.setEnabledProtocols((String[]) eprots.toArray(new String[eprots.size()]));
            }
            String ciphers = props.getProperty(prefix + ".ssl.ciphersuites", (String) null);
            if (ciphers != null) {
                sslsocket.setEnabledCipherSuites(stringArray(ciphers));
            }
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("SSL enabled protocols after " + Arrays.asList(sslsocket.getEnabledProtocols()));
                logger.finer("SSL enabled ciphers after " + Arrays.asList(sslsocket.getEnabledCipherSuites()));
            }
            sslsocket.startHandshake();
            if (PropUtil.getBooleanProperty(props, prefix + ".ssl.checkserveridentity", false)) {
                checkServerIdentity(host, sslsocket);
            }
            if ((sf instanceof MailSSLSocketFactory) && !((MailSSLSocketFactory) sf).isServerTrusted(host, sslsocket)) {
                throw cleanupAndThrow(sslsocket, new IOException("Server is not trusted: " + host));
            }
        }
    }

    private static IOException cleanupAndThrow(Socket socket, IOException ife) {
        try {
            socket.close();
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
        return (t instanceof Exception) || (t instanceof LinkageError);
    }

    private static void checkServerIdentity(String server, SSLSocket sslSocket) throws IOException {
        try {
            Certificate[] certChain = sslSocket.getSession().getPeerCertificates();
            if (certChain == null || certChain.length <= 0 || !(certChain[0] instanceof X509Certificate) || !matchCert(server, (X509Certificate) certChain[0])) {
                sslSocket.close();
                throw new IOException("Can't verify identity of server: " + server);
            }
        } catch (SSLPeerUnverifiedException e) {
            sslSocket.close();
            IOException ioex = new IOException("Can't verify identity of server: " + server);
            ioex.initCause(e);
            throw ioex;
        }
    }

    private static boolean matchCert(String server, X509Certificate cert) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("matchCert server " + server + ", cert " + cert);
        }
        try {
            Class<?> hnc = Class.forName("sun.security.util.HostnameChecker");
            Object hostnameChecker = hnc.getMethod("getInstance", new Class[]{Byte.TYPE}).invoke(new Object(), new Object[]{(byte) 2});
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("using sun.security.util.HostnameChecker");
            }
            try {
                hnc.getMethod("match", new Class[]{String.class, X509Certificate.class}).invoke(hostnameChecker, new Object[]{server, cert});
                return true;
            } catch (InvocationTargetException cex) {
                logger.log(Level.FINER, "HostnameChecker FAIL", (Throwable) cex);
                return false;
            }
        } catch (Exception ex) {
            logger.log(Level.FINER, "NO sun.security.util.HostnameChecker", (Throwable) ex);
            try {
                Collection<List<?>> names = cert.getSubjectAlternativeNames();
                if (names != null) {
                    boolean foundName = false;
                    for (List<?> nameEnt : names) {
                        if (((Integer) nameEnt.get(0)).intValue() == 2) {
                            foundName = true;
                            String name = (String) nameEnt.get(1);
                            if (logger.isLoggable(Level.FINER)) {
                                logger.finer("found name: " + name);
                            }
                            if (matchServer(server, name)) {
                                return true;
                            }
                        }
                    }
                    if (foundName) {
                        return false;
                    }
                }
            } catch (CertificateParsingException e) {
            }
            Matcher m = Pattern.compile("CN=([^,]*)").matcher(cert.getSubjectX500Principal().getName());
            if (!m.find() || !matchServer(server, m.group(1).trim())) {
                return false;
            }
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0061, code lost:
        if (r7.regionMatches(true, r2, r3, 0, r3.length()) != false) goto L_0x0063;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean matchServer(java.lang.String r7, java.lang.String r8) {
        /*
            r1 = 1
            r4 = 0
            com.sun.mail.util.MailLogger r0 = logger
            java.util.logging.Level r5 = java.util.logging.Level.FINER
            boolean r0 = r0.isLoggable(r5)
            if (r0 == 0) goto L_0x002e
            com.sun.mail.util.MailLogger r0 = logger
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "match server "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.StringBuilder r5 = r5.append(r7)
            java.lang.String r6 = " with "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.StringBuilder r5 = r5.append(r8)
            java.lang.String r5 = r5.toString()
            r0.finer(r5)
        L_0x002e:
            java.lang.String r0 = "*."
            boolean r0 = r8.startsWith(r0)
            if (r0 == 0) goto L_0x0067
            r0 = 2
            java.lang.String r3 = r8.substring(r0)
            int r0 = r3.length()
            if (r0 != 0) goto L_0x0042
        L_0x0041:
            return r4
        L_0x0042:
            int r0 = r7.length()
            int r5 = r3.length()
            int r2 = r0 - r5
            if (r2 < r1) goto L_0x0041
            int r0 = r2 + -1
            char r0 = r7.charAt(r0)
            r5 = 46
            if (r0 != r5) goto L_0x0065
            int r5 = r3.length()
            r0 = r7
            boolean r0 = r0.regionMatches(r1, r2, r3, r4, r5)
            if (r0 == 0) goto L_0x0065
        L_0x0063:
            r4 = r1
            goto L_0x0041
        L_0x0065:
            r1 = r4
            goto L_0x0063
        L_0x0067:
            boolean r4 = r7.equalsIgnoreCase(r8)
            goto L_0x0041
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.SocketFetcher.matchServer(java.lang.String, java.lang.String):boolean");
    }

    private static void proxyConnect(Socket socket, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String host, int port, int cto) throws IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("connecting through proxy " + proxyHost + ":" + proxyPort + " to " + host + ":" + port);
        }
        if (cto >= 0) {
            socket.connect(new InetSocketAddress(proxyHost, proxyPort), cto);
        } else {
            socket.connect(new InetSocketAddress(proxyHost, proxyPort));
        }
        PrintStream os = new PrintStream(socket.getOutputStream(), false, StandardCharsets.UTF_8.name());
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("CONNECT ").append(host).append(":").append(port).append(" HTTP/1.1\r\n");
        requestBuilder.append("Host: ").append(host).append(":").append(port).append("\r\n");
        if (!(proxyUser == null || proxyPassword == null)) {
            requestBuilder.append("Proxy-Authorization: Basic ").append(new String(BASE64EncoderStream.encode((proxyUser + ':' + proxyPassword).getBytes(StandardCharsets.UTF_8)), StandardCharsets.US_ASCII)).append("\r\n");
        }
        requestBuilder.append("Proxy-Connection: keep-alive\r\n\r\n");
        os.print(requestBuilder.toString());
        os.flush();
        BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        boolean first = true;
        while (true) {
            String line = r.readLine();
            if (line != null && line.length() != 0) {
                logger.finest(line);
                if (first) {
                    StringTokenizer st = new StringTokenizer(line);
                    String nextToken = st.nextToken();
                    if (!st.nextToken().equals("200")) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                        }
                        ConnectException ex = new ConnectException("connection through proxy " + proxyHost + ":" + proxyPort + " to " + host + ":" + port + " failed: " + line);
                        logger.log(Level.FINE, "connect failed", (Throwable) ex);
                        throw ex;
                    }
                    first = false;
                }
            } else {
                return;
            }
        }
    }

    private static String[] stringArray(String s) {
        StringTokenizer st = new StringTokenizer(s);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

    private static ClassLoader getContextClassLoader() {
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
}
