package javax.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;

public abstract class Transport extends Service {
    private volatile Vector<TransportListener> transportListeners = null;

    public abstract void sendMessage(Message message, Address[] addressArr) throws MessagingException;

    public Transport(Session session, URLName urlname) {
        super(session, urlname);
    }

    public static void send(Message msg) throws MessagingException {
        msg.saveChanges();
        send0(msg, msg.getAllRecipients(), (String) null, (String) null);
    }

    public static void send(Message msg, Address[] addresses) throws MessagingException {
        msg.saveChanges();
        send0(msg, addresses, (String) null, (String) null);
    }

    public static void send(Message msg, String user, String password) throws MessagingException {
        msg.saveChanges();
        send0(msg, msg.getAllRecipients(), user, password);
    }

    public static void send(Message msg, Address[] addresses, String user, String password) throws MessagingException {
        msg.saveChanges();
        send0(msg, addresses, user, password);
    }

    private static void send0(Message msg, Address[] addresses, String user, String password) throws MessagingException {
        Session s;
        if (addresses == null || addresses.length == 0) {
            throw new SendFailedException("No recipient addresses");
        }
        HashMap hashMap = new HashMap();
        List<Address> invalid = new ArrayList<>();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < addresses.length; i++) {
            if (hashMap.containsKey(addresses[i].getType())) {
                ((List) hashMap.get(addresses[i].getType())).add(addresses[i]);
            } else {
                ArrayList arrayList3 = new ArrayList();
                arrayList3.add(addresses[i]);
                hashMap.put(addresses[i].getType(), arrayList3);
            }
        }
        int dsize = hashMap.size();
        if (dsize == 0) {
            throw new SendFailedException("No recipient addresses");
        }
        if (msg.session != null) {
            s = msg.session;
        } else {
            s = Session.getDefaultInstance(System.getProperties(), (Authenticator) null);
        }
        if (dsize == 1) {
            Transport transport = s.getTransport(addresses[0]);
            if (user != null) {
                try {
                    transport.connect(user, password);
                } catch (Throwable th) {
                    transport.close();
                    throw th;
                }
            } else {
                transport.connect();
            }
            transport.sendMessage(msg, addresses);
            transport.close();
            return;
        }
        MessagingException chainedEx = null;
        boolean sendFailed = false;
        for (List<Address> v : hashMap.values()) {
            Address[] protaddresses = new Address[v.size()];
            v.toArray(protaddresses);
            Transport transport2 = s.getTransport(protaddresses[0]);
            if (transport2 == null) {
                for (Address add : protaddresses) {
                    invalid.add(add);
                }
            } else {
                try {
                    transport2.connect();
                    transport2.sendMessage(msg, protaddresses);
                } catch (SendFailedException sex) {
                    sendFailed = true;
                    if (chainedEx == null) {
                        chainedEx = sex;
                    } else {
                        chainedEx.setNextException(sex);
                    }
                    Address[] a = sex.getInvalidAddresses();
                    if (a != null) {
                        for (Address add2 : a) {
                            invalid.add(add2);
                        }
                    }
                    Address[] a2 = sex.getValidSentAddresses();
                    if (a2 != null) {
                        for (Address add3 : a2) {
                            arrayList.add(add3);
                        }
                    }
                    Address[] c = sex.getValidUnsentAddresses();
                    if (c != null) {
                        for (Address add4 : c) {
                            arrayList2.add(add4);
                        }
                    }
                } catch (MessagingException mex) {
                    sendFailed = true;
                    if (chainedEx == null) {
                        chainedEx = mex;
                    } else {
                        chainedEx.setNextException(mex);
                    }
                } finally {
                    transport2.close();
                }
            }
        }
        if (sendFailed || invalid.size() != 0 || arrayList2.size() != 0) {
            Address[] a3 = null;
            Address[] b = null;
            Address[] c2 = null;
            if (arrayList.size() > 0) {
                a3 = new Address[arrayList.size()];
                arrayList.toArray(a3);
            }
            if (arrayList2.size() > 0) {
                b = new Address[arrayList2.size()];
                arrayList2.toArray(b);
            }
            if (invalid.size() > 0) {
                c2 = new Address[invalid.size()];
                invalid.toArray(c2);
            }
            throw new SendFailedException("Sending failed", chainedEx, a3, b, c2);
        }
    }

    public synchronized void addTransportListener(TransportListener l) {
        if (this.transportListeners == null) {
            this.transportListeners = new Vector<>();
        }
        this.transportListeners.addElement(l);
    }

    public synchronized void removeTransportListener(TransportListener l) {
        if (this.transportListeners != null) {
            this.transportListeners.removeElement(l);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyTransportListeners(int type, Address[] validSent, Address[] validUnsent, Address[] invalid, Message msg) {
        if (this.transportListeners != null) {
            queueEvent(new TransportEvent(this, type, validSent, validUnsent, invalid, msg), this.transportListeners);
        }
    }
}
