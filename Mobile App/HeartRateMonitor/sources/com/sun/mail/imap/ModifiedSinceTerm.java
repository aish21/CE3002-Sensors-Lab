package com.sun.mail.imap;

import javax.mail.Message;
import javax.mail.search.SearchTerm;

public final class ModifiedSinceTerm extends SearchTerm {
    private static final long serialVersionUID = 5151457469634727992L;
    private long modseq;

    public ModifiedSinceTerm(long modseq2) {
        this.modseq = modseq2;
    }

    public long getModSeq() {
        return this.modseq;
    }

    public boolean match(Message msg) {
        try {
            if (!(msg instanceof IMAPMessage)) {
                return false;
            }
            if (((IMAPMessage) msg).getModSeq() >= this.modseq) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean equals(Object obj) {
        if ((obj instanceof ModifiedSinceTerm) && this.modseq == ((ModifiedSinceTerm) obj).modseq) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (int) this.modseq;
    }
}
