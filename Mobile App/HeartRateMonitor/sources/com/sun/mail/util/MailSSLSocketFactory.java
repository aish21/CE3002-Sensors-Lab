package com.sun.mail.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MailSSLSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory adapteeFactory;
    private KeyManager[] keyManagers;
    private SecureRandom secureRandom;
    private SSLContext sslcontext;
    private boolean trustAllHosts;
    private TrustManager[] trustManagers;
    private String[] trustedHosts;

    public MailSSLSocketFactory() throws GeneralSecurityException {
        this("TLS");
    }

    public MailSSLSocketFactory(String protocol) throws GeneralSecurityException {
        this.trustedHosts = null;
        this.adapteeFactory = null;
        this.trustAllHosts = false;
        this.sslcontext = SSLContext.getInstance(protocol);
        this.keyManagers = null;
        this.trustManagers = new TrustManager[]{new MailTrustManager()};
        this.secureRandom = null;
        newAdapteeFactory();
    }

    private synchronized void newAdapteeFactory() throws KeyManagementException {
        this.sslcontext.init(this.keyManagers, this.trustManagers, this.secureRandom);
        this.adapteeFactory = this.sslcontext.getSocketFactory();
    }

    public synchronized KeyManager[] getKeyManagers() {
        return (KeyManager[]) this.keyManagers.clone();
    }

    public synchronized void setKeyManagers(KeyManager... keyManagers2) throws GeneralSecurityException {
        this.keyManagers = (KeyManager[]) keyManagers2.clone();
        newAdapteeFactory();
    }

    public synchronized SecureRandom getSecureRandom() {
        return this.secureRandom;
    }

    public synchronized void setSecureRandom(SecureRandom secureRandom2) throws GeneralSecurityException {
        this.secureRandom = secureRandom2;
        newAdapteeFactory();
    }

    public synchronized TrustManager[] getTrustManagers() {
        return this.trustManagers;
    }

    public synchronized void setTrustManagers(TrustManager... trustManagers2) throws GeneralSecurityException {
        this.trustManagers = trustManagers2;
        newAdapteeFactory();
    }

    public synchronized boolean isTrustAllHosts() {
        return this.trustAllHosts;
    }

    public synchronized void setTrustAllHosts(boolean trustAllHosts2) {
        this.trustAllHosts = trustAllHosts2;
    }

    public synchronized String[] getTrustedHosts() {
        String[] strArr;
        if (this.trustedHosts == null) {
            strArr = null;
        } else {
            strArr = (String[]) this.trustedHosts.clone();
        }
        return strArr;
    }

    public synchronized void setTrustedHosts(String... trustedHosts2) {
        if (trustedHosts2 == null) {
            this.trustedHosts = null;
        } else {
            this.trustedHosts = (String[]) trustedHosts2.clone();
        }
    }

    public synchronized boolean isServerTrusted(String server, SSLSocket sslSocket) {
        boolean z = true;
        synchronized (this) {
            if (!this.trustAllHosts) {
                if (this.trustedHosts != null) {
                    z = Arrays.asList(this.trustedHosts).contains(server);
                }
            }
        }
        return z;
    }

    public synchronized Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException {
        return this.adapteeFactory.createSocket(socket, s, i, flag);
    }

    public synchronized String[] getDefaultCipherSuites() {
        return this.adapteeFactory.getDefaultCipherSuites();
    }

    public synchronized String[] getSupportedCipherSuites() {
        return this.adapteeFactory.getSupportedCipherSuites();
    }

    public synchronized Socket createSocket() throws IOException {
        return this.adapteeFactory.createSocket();
    }

    public synchronized Socket createSocket(InetAddress inetaddress, int i, InetAddress inetaddress1, int j) throws IOException {
        return this.adapteeFactory.createSocket(inetaddress, i, inetaddress1, j);
    }

    public synchronized Socket createSocket(InetAddress inetaddress, int i) throws IOException {
        return this.adapteeFactory.createSocket(inetaddress, i);
    }

    public synchronized Socket createSocket(String s, int i, InetAddress inetaddress, int j) throws IOException, UnknownHostException {
        return this.adapteeFactory.createSocket(s, i, inetaddress, j);
    }

    public synchronized Socket createSocket(String s, int i) throws IOException, UnknownHostException {
        return this.adapteeFactory.createSocket(s, i);
    }

    private class MailTrustManager implements X509TrustManager {
        private X509TrustManager adapteeTrustManager;

        private MailTrustManager() throws GeneralSecurityException {
            this.adapteeTrustManager = null;
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init((KeyStore) null);
            this.adapteeTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            if (!MailSSLSocketFactory.this.isTrustAllHosts() && MailSSLSocketFactory.this.getTrustedHosts() == null) {
                this.adapteeTrustManager.checkClientTrusted(certs, authType);
            }
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            if (!MailSSLSocketFactory.this.isTrustAllHosts() && MailSSLSocketFactory.this.getTrustedHosts() == null) {
                this.adapteeTrustManager.checkServerTrusted(certs, authType);
            }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return this.adapteeTrustManager.getAcceptedIssuers();
        }
    }
}
