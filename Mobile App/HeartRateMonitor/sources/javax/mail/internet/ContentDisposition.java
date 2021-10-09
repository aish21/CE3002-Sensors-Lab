package javax.mail.internet;

import com.sun.mail.util.PropUtil;
import javax.mail.internet.HeaderTokenizer;

public class ContentDisposition {
    private static final boolean contentDispositionStrict = PropUtil.getBooleanSystemProperty("mail.mime.contentdisposition.strict", true);
    private String disposition;
    private ParameterList list;

    public ContentDisposition() {
    }

    public ContentDisposition(String disposition2, ParameterList list2) {
        this.disposition = disposition2;
        this.list = list2;
    }

    public ContentDisposition(String s) throws ParseException {
        HeaderTokenizer h = new HeaderTokenizer(s, HeaderTokenizer.MIME);
        HeaderTokenizer.Token tk = h.next();
        if (tk.getType() == -1) {
            this.disposition = tk.getValue();
        } else if (contentDispositionStrict) {
            throw new ParseException("Expected disposition, got " + tk.getValue());
        }
        String rem = h.getRemainder();
        if (rem != null) {
            try {
                this.list = new ParameterList(rem);
            } catch (ParseException px) {
                if (contentDispositionStrict) {
                    throw px;
                }
            }
        }
    }

    public String getDisposition() {
        return this.disposition;
    }

    public String getParameter(String name) {
        if (this.list == null) {
            return null;
        }
        return this.list.get(name);
    }

    public ParameterList getParameterList() {
        return this.list;
    }

    public void setDisposition(String disposition2) {
        this.disposition = disposition2;
    }

    public void setParameter(String name, String value) {
        if (this.list == null) {
            this.list = new ParameterList();
        }
        this.list.set(name, value);
    }

    public void setParameterList(ParameterList list2) {
        this.list = list2;
    }

    public String toString() {
        if (this.disposition == null) {
            return "";
        }
        if (this.list == null) {
            return this.disposition;
        }
        StringBuilder sb = new StringBuilder(this.disposition);
        sb.append(this.list.toString(sb.length() + 21));
        return sb.toString();
    }
}
