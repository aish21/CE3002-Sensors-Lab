package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Response;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Status {
    static final String[] standardItems = {"MESSAGES", "RECENT", "UNSEEN", "UIDNEXT", "UIDVALIDITY"};
    public long highestmodseq = -1;
    public Map<String, Long> items;
    public String mbox = null;
    public int recent = -1;
    public int total = -1;
    public long uidnext = -1;
    public long uidvalidity = -1;
    public int unseen = -1;

    public Status(Response r) throws ParsingException {
        this.mbox = r.readAtomString();
        if (!r.supportsUtf8()) {
            this.mbox = BASE64MailboxDecoder.decode(this.mbox);
        }
        StringBuilder buffer = new StringBuilder();
        boolean onlySpaces = true;
        while (r.peekByte() != 40 && r.peekByte() != 0) {
            char next = (char) r.readByte();
            buffer.append(next);
            if (next != ' ') {
                onlySpaces = false;
            }
        }
        if (!onlySpaces) {
            this.mbox = (this.mbox + buffer).trim();
        }
        if (r.readByte() != 40) {
            throw new ParsingException("parse error in STATUS");
        }
        do {
            String attr = r.readAtom();
            if (attr == null) {
                throw new ParsingException("parse error in STATUS");
            } else if (attr.equalsIgnoreCase("MESSAGES")) {
                this.total = r.readNumber();
            } else if (attr.equalsIgnoreCase("RECENT")) {
                this.recent = r.readNumber();
            } else if (attr.equalsIgnoreCase("UIDNEXT")) {
                this.uidnext = r.readLong();
            } else if (attr.equalsIgnoreCase("UIDVALIDITY")) {
                this.uidvalidity = r.readLong();
            } else if (attr.equalsIgnoreCase("UNSEEN")) {
                this.unseen = r.readNumber();
            } else if (attr.equalsIgnoreCase("HIGHESTMODSEQ")) {
                this.highestmodseq = r.readLong();
            } else {
                if (this.items == null) {
                    this.items = new HashMap();
                }
                this.items.put(attr.toUpperCase(Locale.ENGLISH), Long.valueOf(r.readLong()));
            }
        } while (!r.isNextNonSpace(')'));
    }

    public long getItem(String item) {
        Long v;
        String item2 = item.toUpperCase(Locale.ENGLISH);
        if (this.items != null && (v = this.items.get(item2)) != null) {
            return v.longValue();
        }
        if (item2.equals("MESSAGES")) {
            return (long) this.total;
        }
        if (item2.equals("RECENT")) {
            return (long) this.recent;
        }
        if (item2.equals("UIDNEXT")) {
            return this.uidnext;
        }
        if (item2.equals("UIDVALIDITY")) {
            return this.uidvalidity;
        }
        if (item2.equals("UNSEEN")) {
            return (long) this.unseen;
        }
        if (item2.equals("HIGHESTMODSEQ")) {
            return this.highestmodseq;
        }
        return -1;
    }

    public static void add(Status s1, Status s2) {
        if (s2.total != -1) {
            s1.total = s2.total;
        }
        if (s2.recent != -1) {
            s1.recent = s2.recent;
        }
        if (s2.uidnext != -1) {
            s1.uidnext = s2.uidnext;
        }
        if (s2.uidvalidity != -1) {
            s1.uidvalidity = s2.uidvalidity;
        }
        if (s2.unseen != -1) {
            s1.unseen = s2.unseen;
        }
        if (s2.highestmodseq != -1) {
            s1.highestmodseq = s2.highestmodseq;
        }
        if (s1.items == null) {
            s1.items = s2.items;
        } else if (s2.items != null) {
            s1.items.putAll(s2.items);
        }
    }
}
