package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import java.util.ArrayList;
import java.util.List;

public class Namespaces {
    public Namespace[] otherUsers;
    public Namespace[] personal;
    public Namespace[] shared;

    public static class Namespace {
        public char delimiter;
        public String prefix;

        public Namespace(Response r) throws ProtocolException {
            if (!r.isNextNonSpace('(')) {
                throw new ProtocolException("Missing '(' at start of Namespace");
            }
            this.prefix = r.readString();
            if (!r.supportsUtf8()) {
                this.prefix = BASE64MailboxDecoder.decode(this.prefix);
            }
            r.skipSpaces();
            if (r.peekByte() == 34) {
                r.readByte();
                this.delimiter = (char) r.readByte();
                if (this.delimiter == '\\') {
                    this.delimiter = (char) r.readByte();
                }
                if (r.readByte() != 34) {
                    throw new ProtocolException("Missing '\"' at end of QUOTED_CHAR");
                }
            } else {
                String s = r.readAtom();
                if (s == null) {
                    throw new ProtocolException("Expected NIL, got null");
                } else if (!s.equalsIgnoreCase("NIL")) {
                    throw new ProtocolException("Expected NIL, got " + s);
                } else {
                    this.delimiter = 0;
                }
            }
            if (!r.isNextNonSpace(')')) {
                r.readString();
                r.skipSpaces();
                r.readStringList();
                if (!r.isNextNonSpace(')')) {
                    throw new ProtocolException("Missing ')' at end of Namespace");
                }
            }
        }
    }

    public Namespaces(Response r) throws ProtocolException {
        this.personal = getNamespaces(r);
        this.otherUsers = getNamespaces(r);
        this.shared = getNamespaces(r);
    }

    private Namespace[] getNamespaces(Response r) throws ProtocolException {
        if (r.isNextNonSpace('(')) {
            List<Namespace> v = new ArrayList<>();
            do {
                v.add(new Namespace(r));
            } while (!r.isNextNonSpace(')'));
            return (Namespace[]) v.toArray(new Namespace[v.size()]);
        }
        String s = r.readAtom();
        if (s == null) {
            throw new ProtocolException("Expected NIL, got null");
        } else if (s.equalsIgnoreCase("NIL")) {
            return null;
        } else {
            throw new ProtocolException("Expected NIL, got " + s);
        }
    }
}
