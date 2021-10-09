package javax.mail;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public abstract class Multipart {
    protected String contentType = "multipart/mixed";
    protected Part parent;
    protected Vector<BodyPart> parts = new Vector<>();

    public abstract void writeTo(OutputStream outputStream) throws IOException, MessagingException;

    protected Multipart() {
    }

    /* access modifiers changed from: protected */
    public synchronized void setMultipartDataSource(MultipartDataSource mp) throws MessagingException {
        this.contentType = mp.getContentType();
        int count = mp.getCount();
        for (int i = 0; i < count; i++) {
            addBodyPart(mp.getBodyPart(i));
        }
    }

    public synchronized String getContentType() {
        return this.contentType;
    }

    public synchronized int getCount() throws MessagingException {
        int size;
        if (this.parts == null) {
            size = 0;
        } else {
            size = this.parts.size();
        }
        return size;
    }

    public synchronized BodyPart getBodyPart(int index) throws MessagingException {
        if (this.parts == null) {
            throw new IndexOutOfBoundsException("No such BodyPart");
        }
        return this.parts.elementAt(index);
    }

    public synchronized boolean removeBodyPart(BodyPart part) throws MessagingException {
        boolean ret;
        if (this.parts == null) {
            throw new MessagingException("No such body part");
        }
        ret = this.parts.removeElement(part);
        part.setParent((Multipart) null);
        return ret;
    }

    public synchronized void removeBodyPart(int index) throws MessagingException {
        if (this.parts == null) {
            throw new IndexOutOfBoundsException("No such BodyPart");
        }
        this.parts.removeElementAt(index);
        this.parts.elementAt(index).setParent((Multipart) null);
    }

    public synchronized void addBodyPart(BodyPart part) throws MessagingException {
        if (this.parts == null) {
            this.parts = new Vector<>();
        }
        this.parts.addElement(part);
        part.setParent(this);
    }

    public synchronized void addBodyPart(BodyPart part, int index) throws MessagingException {
        if (this.parts == null) {
            this.parts = new Vector<>();
        }
        this.parts.insertElementAt(part, index);
        part.setParent(this);
    }

    public synchronized Part getParent() {
        return this.parent;
    }

    public synchronized void setParent(Part parent2) {
        this.parent = parent2;
    }
}
