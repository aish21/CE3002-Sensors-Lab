package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ProtocolException;

public class IMAPReferralException extends ProtocolException {
    private static final long serialVersionUID = 2578770669364251968L;
    private String url;

    public IMAPReferralException(String s, String url2) {
        super(s);
        this.url = url2;
    }

    public String getUrl() {
        return this.url;
    }
}
