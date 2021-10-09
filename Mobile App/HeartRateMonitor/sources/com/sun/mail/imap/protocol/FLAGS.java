package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import gnu.bytecode.Access;
import javax.mail.Flags;

public class FLAGS extends Flags implements Item {
    static final char[] name = {Access.FIELD_CONTEXT, 'L', 'A', 'G', 'S'};
    private static final long serialVersionUID = 439049847053756670L;
    public int msgno;

    public FLAGS(IMAPResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        String[] flags = r.readSimpleList();
        if (flags != null) {
            for (String s : flags) {
                if (s.length() >= 2 && s.charAt(0) == '\\') {
                    switch (Character.toUpperCase(s.charAt(1))) {
                        case '*':
                            add(Flags.Flag.USER);
                            break;
                        case 'A':
                            add(Flags.Flag.ANSWERED);
                            break;
                        case 'D':
                            if (s.length() < 3) {
                                add(s);
                                break;
                            } else {
                                char c = s.charAt(2);
                                if (c != 'e' && c != 'E') {
                                    if (c != 'r' && c != 'R') {
                                        break;
                                    } else {
                                        add(Flags.Flag.DRAFT);
                                        break;
                                    }
                                } else {
                                    add(Flags.Flag.DELETED);
                                    break;
                                }
                            }
                            break;
                        case 'F':
                            add(Flags.Flag.FLAGGED);
                            break;
                        case 'R':
                            add(Flags.Flag.RECENT);
                            break;
                        case 'S':
                            add(Flags.Flag.SEEN);
                            break;
                        default:
                            add(s);
                            break;
                    }
                } else {
                    add(s);
                }
            }
        }
    }
}
