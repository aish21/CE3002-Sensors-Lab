package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Response;
import com.sun.mail.util.PropUtil;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;

public class ENVELOPE implements Item {
    private static final MailDateFormat mailDateFormat = new MailDateFormat();
    static final char[] name = {'E', 'N', 'V', 'E', 'L', 'O', 'P', 'E'};
    private static final boolean parseDebug = PropUtil.getBooleanSystemProperty("mail.imap.parse.debug", false);
    public InternetAddress[] bcc;

    /* renamed from: cc */
    public InternetAddress[] f277cc;
    public Date date = null;
    public InternetAddress[] from;
    public String inReplyTo;
    public String messageId;
    public int msgno;
    public InternetAddress[] replyTo;
    public InternetAddress[] sender;
    public String subject;

    /* renamed from: to */
    public InternetAddress[] f278to;

    public ENVELOPE(FetchResponse r) throws ParsingException {
        if (parseDebug) {
            System.out.println("parse ENVELOPE");
        }
        this.msgno = r.getNumber();
        r.skipSpaces();
        if (r.readByte() != 40) {
            throw new ParsingException("ENVELOPE parse error");
        }
        String s = r.readString();
        if (s != null) {
            try {
                synchronized (mailDateFormat) {
                    this.date = mailDateFormat.parse(s);
                }
            } catch (ParseException e) {
            }
        }
        if (parseDebug) {
            System.out.println("  Date: " + this.date);
        }
        this.subject = r.readString();
        if (parseDebug) {
            System.out.println("  Subject: " + this.subject);
        }
        if (parseDebug) {
            System.out.println("  From addresses:");
        }
        this.from = parseAddressList(r);
        if (parseDebug) {
            System.out.println("  Sender addresses:");
        }
        this.sender = parseAddressList(r);
        if (parseDebug) {
            System.out.println("  Reply-To addresses:");
        }
        this.replyTo = parseAddressList(r);
        if (parseDebug) {
            System.out.println("  To addresses:");
        }
        this.f278to = parseAddressList(r);
        if (parseDebug) {
            System.out.println("  Cc addresses:");
        }
        this.f277cc = parseAddressList(r);
        if (parseDebug) {
            System.out.println("  Bcc addresses:");
        }
        this.bcc = parseAddressList(r);
        this.inReplyTo = r.readString();
        if (parseDebug) {
            System.out.println("  In-Reply-To: " + this.inReplyTo);
        }
        this.messageId = r.readString();
        if (parseDebug) {
            System.out.println("  Message-ID: " + this.messageId);
        }
        if (!r.isNextNonSpace(')')) {
            throw new ParsingException("ENVELOPE parse error");
        }
    }

    private InternetAddress[] parseAddressList(Response r) throws ParsingException {
        r.skipSpaces();
        byte b = r.readByte();
        if (b == 40) {
            if (r.isNextNonSpace(')')) {
                return null;
            }
            List<InternetAddress> v = new ArrayList<>();
            do {
                IMAPAddress a = new IMAPAddress(r);
                if (parseDebug) {
                    System.out.println("    Address: " + a);
                }
                if (!a.isEndOfGroup()) {
                    v.add(a);
                }
            } while (!r.isNextNonSpace(')'));
            return (InternetAddress[]) v.toArray(new InternetAddress[v.size()]);
        } else if (b == 78 || b == 110) {
            r.skip(2);
            return null;
        } else {
            throw new ParsingException("ADDRESS parse error");
        }
    }
}
