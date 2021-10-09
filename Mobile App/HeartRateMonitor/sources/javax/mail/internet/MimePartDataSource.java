package javax.mail.internet;

import com.google.appinventor.components.runtime.util.NanoHTTPD;
import com.sun.mail.util.FolderClosedIOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownServiceException;
import javax.activation.DataSource;
import javax.mail.FolderClosedException;
import javax.mail.MessageAware;
import javax.mail.MessageContext;
import javax.mail.MessagingException;

public class MimePartDataSource implements DataSource, MessageAware {
    private MessageContext context;
    protected MimePart part;

    public MimePartDataSource(MimePart part2) {
        this.part = part2;
    }

    public InputStream getInputStream() throws IOException {
        InputStream is;
        try {
            if (this.part instanceof MimeBodyPart) {
                is = ((MimeBodyPart) this.part).getContentStream();
            } else if (this.part instanceof MimeMessage) {
                is = ((MimeMessage) this.part).getContentStream();
            } else {
                throw new MessagingException("Unknown part");
            }
            String encoding = MimeBodyPart.restrictEncoding(this.part, this.part.getEncoding());
            if (encoding != null) {
                return MimeUtility.decode(is, encoding);
            }
            return is;
        } catch (FolderClosedException fex) {
            throw new FolderClosedIOException(fex.getFolder(), fex.getMessage());
        } catch (MessagingException mex) {
            IOException ioex = new IOException(mex.getMessage());
            ioex.initCause(mex);
            throw ioex;
        }
    }

    public OutputStream getOutputStream() throws IOException {
        throw new UnknownServiceException("Writing not supported");
    }

    public String getContentType() {
        try {
            return this.part.getContentType();
        } catch (MessagingException e) {
            return NanoHTTPD.MIME_DEFAULT_BINARY;
        }
    }

    public String getName() {
        try {
            if (this.part instanceof MimeBodyPart) {
                return ((MimeBodyPart) this.part).getFileName();
            }
        } catch (MessagingException e) {
        }
        return "";
    }

    public synchronized MessageContext getMessageContext() {
        if (this.context == null) {
            this.context = new MessageContext(this.part);
        }
        return this.context;
    }
}
