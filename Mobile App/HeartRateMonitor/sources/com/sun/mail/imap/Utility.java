package com.sun.mail.imap;

import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.UIDSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.mail.Message;

public final class Utility {

    public interface Condition {
        boolean test(IMAPMessage iMAPMessage);
    }

    private Utility() {
    }

    public static MessageSet[] toMessageSet(Message[] msgs, Condition cond) {
        List<MessageSet> v = new ArrayList<>(1);
        int i = 0;
        while (i < msgs.length) {
            IMAPMessage msg = msgs[i];
            if (!msg.isExpunged()) {
                int current = msg.getSequenceNumber();
                if (cond == null || cond.test(msg)) {
                    MessageSet set = new MessageSet();
                    set.start = current;
                    while (true) {
                        i++;
                        if (i >= msgs.length) {
                            break;
                        }
                        IMAPMessage msg2 = msgs[i];
                        if (!msg2.isExpunged()) {
                            int next = msg2.getSequenceNumber();
                            if (cond == null || cond.test(msg2)) {
                                if (next != current + 1) {
                                    i--;
                                    break;
                                }
                                current = next;
                            }
                        }
                    }
                    set.end = current;
                    v.add(set);
                }
            }
            i++;
        }
        if (v.isEmpty()) {
            return null;
        }
        return (MessageSet[]) v.toArray(new MessageSet[v.size()]);
    }

    public static MessageSet[] toMessageSetSorted(Message[] msgs, Condition cond) {
        Message[] msgs2 = (Message[]) msgs.clone();
        Arrays.sort(msgs2, new Comparator<Message>() {
            public int compare(Message msg1, Message msg2) {
                return msg1.getMessageNumber() - msg2.getMessageNumber();
            }
        });
        return toMessageSet(msgs2, cond);
    }

    public static UIDSet[] toUIDSet(Message[] msgs) {
        List<UIDSet> v = new ArrayList<>(1);
        int i = 0;
        while (i < msgs.length) {
            IMAPMessage msg = msgs[i];
            if (!msg.isExpunged()) {
                long current = msg.getUID();
                UIDSet set = new UIDSet();
                set.start = current;
                while (true) {
                    i++;
                    if (i >= msgs.length) {
                        break;
                    }
                    IMAPMessage msg2 = msgs[i];
                    if (!msg2.isExpunged()) {
                        long next = msg2.getUID();
                        if (next != 1 + current) {
                            i--;
                            break;
                        }
                        current = next;
                    }
                }
                set.end = current;
                v.add(set);
            }
            i++;
        }
        if (v.isEmpty()) {
            return null;
        }
        return (UIDSet[]) v.toArray(new UIDSet[v.size()]);
    }

    public static UIDSet[] getResyncUIDSet(ResyncData rd) {
        return rd.getUIDSet();
    }
}
