package com.sun.mail.imap.protocol;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* renamed from: com.sun.mail.imap.protocol.ID */
public class C0686ID {
    private Map<String, String> serverParams = null;

    public C0686ID(Response r) throws ProtocolException {
        r.skipSpaces();
        int c = r.peekByte();
        if (c != 78 && c != 110) {
            if (c != 40) {
                throw new ProtocolException("Missing '(' at start of ID");
            }
            this.serverParams = new HashMap();
            String[] v = r.readStringList();
            if (v != null) {
                int i = 0;
                while (i < v.length) {
                    String name = v[i];
                    if (name == null) {
                        throw new ProtocolException("ID field name null");
                    } else if (i + 1 >= v.length) {
                        throw new ProtocolException("ID field without value: " + name);
                    } else {
                        this.serverParams.put(name, v[i + 1]);
                        i += 2;
                    }
                }
            }
            this.serverParams = Collections.unmodifiableMap(this.serverParams);
        }
    }

    /* access modifiers changed from: package-private */
    public Map<String, String> getServerParams() {
        return this.serverParams;
    }

    static Argument getArgumentList(Map<String, String> clientParams) {
        Argument arg = new Argument();
        if (clientParams == null) {
            arg.writeAtom("NIL");
        } else {
            Argument list = new Argument();
            for (Map.Entry<String, String> e : clientParams.entrySet()) {
                list.writeNString(e.getKey());
                list.writeNString(e.getValue());
            }
            arg.writeArgument(list);
        }
        return arg;
    }
}
