package com.sun.mail.imap;

import java.util.Date;
import javax.mail.Message;
import javax.mail.search.SearchTerm;

public final class OlderTerm extends SearchTerm {
    private static final long serialVersionUID = 3951078948727995682L;
    private int interval;

    public OlderTerm(int interval2) {
        this.interval = interval2;
    }

    public int getInterval() {
        return this.interval;
    }

    public boolean match(Message msg) {
        try {
            Date d = msg.getReceivedDate();
            if (d != null && d.getTime() <= System.currentTimeMillis() - (((long) this.interval) * 1000)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean equals(Object obj) {
        if ((obj instanceof OlderTerm) && this.interval == ((OlderTerm) obj).interval) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.interval;
    }
}
