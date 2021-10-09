package com.sun.mail.imap;

import javax.mail.AuthenticationFailedException;

public class ReferralException extends AuthenticationFailedException {
    private static final long serialVersionUID = -3414063558596287683L;
    private String text;
    private String url;

    public ReferralException(String url2, String text2) {
        super("[REFERRAL " + url2 + "] " + text2);
        this.url = url2;
        this.text = text2;
    }

    public String getUrl() {
        return this.url;
    }

    public String getText() {
        return this.text;
    }
}
