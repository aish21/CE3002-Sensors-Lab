package com.sun.mail.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessageAware;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class message_rfc822 extends handler_base {
    private static ActivationDataFlavor[] ourDataFlavor = {new ActivationDataFlavor(Message.class, "message/rfc822", "Message")};

    /* access modifiers changed from: protected */
    public ActivationDataFlavor[] getDataFlavors() {
        return ourDataFlavor;
    }

    public Object getContent(DataSource ds) throws IOException {
        Session session;
        try {
            if (ds instanceof MessageAware) {
                session = ((MessageAware) ds).getMessageContext().getSession();
            } else {
                session = Session.getDefaultInstance(new Properties(), (Authenticator) null);
            }
            return new MimeMessage(session, ds.getInputStream());
        } catch (MessagingException me) {
            IOException ioex = new IOException("Exception creating MimeMessage in message/rfc822 DataContentHandler");
            ioex.initCause(me);
            throw ioex;
        }
    }

    public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
        if (!(obj instanceof Message)) {
            throw new IOException("\"" + getDataFlavors()[0].getMimeType() + "\" DataContentHandler requires Message object, was given object of type " + obj.getClass().toString() + "; obj.cl " + obj.getClass().getClassLoader() + ", Message.cl " + Message.class.getClassLoader());
        }
        try {
            ((Message) obj).writeTo(os);
        } catch (MessagingException me) {
            IOException ioex = new IOException("Exception writing message");
            ioex.initCause(me);
            throw ioex;
        }
    }
}
