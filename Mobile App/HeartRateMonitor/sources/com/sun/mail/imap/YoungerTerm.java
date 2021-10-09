package com.sun.mail.imap;

import java.util.Date;
import javax.mail.Message;
import javax.mail.search.SearchTerm;

public final class YoungerTerm extends SearchTerm {
    private static final long serialVersionUID = 1592714210688163496L;
    private int interval;

    public YoungerTerm(int interval2) {
        this.interval = interval2;
    }

    public int getInterval() {
        return this.interval;
    }

    public boolean match(Message msg) {
        try {
            Date d = msg.getReceivedDate();
            if (d != null && d.getTime() >= System.currentTimeMillis() - (((long) this.interval) * 1000)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean equals(Object obj) {
        if ((obj instanceof YoungerTerm) && this.interval == ((YoungerTerm) obj).interval) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.interval;
    }
}
