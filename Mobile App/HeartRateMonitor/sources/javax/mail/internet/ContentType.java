package javax.mail.internet;

import javax.mail.internet.HeaderTokenizer;

public class ContentType {
    private ParameterList list;
    private String primaryType;
    private String subType;

    public ContentType() {
    }

    public ContentType(String primaryType2, String subType2, ParameterList list2) {
        this.primaryType = primaryType2;
        this.subType = subType2;
        this.list = list2;
    }

    public ContentType(String s) throws ParseException {
        HeaderTokenizer h = new HeaderTokenizer(s, HeaderTokenizer.MIME);
        HeaderTokenizer.Token tk = h.next();
        if (tk.getType() != -1) {
            throw new ParseException("In Content-Type string <" + s + ">, expected MIME type, got " + tk.getValue());
        }
        this.primaryType = tk.getValue();
        HeaderTokenizer.Token tk2 = h.next();
        if (((char) tk2.getType()) != '/') {
            throw new ParseException("In Content-Type string <" + s + ">, expected '/', got " + tk2.getValue());
        }
        HeaderTokenizer.Token tk3 = h.next();
        if (tk3.getType() != -1) {
            throw new ParseException("In Content-Type string <" + s + ">, expected MIME subtype, got " + tk3.getValue());
        }
        this.subType = tk3.getValue();
        String rem = h.getRemainder();
        if (rem != null) {
            this.list = new ParameterList(rem);
        }
    }

    public String getPrimaryType() {
        return this.primaryType;
    }

    public String getSubType() {
        return this.subType;
    }

    public String getBaseType() {
        if (this.primaryType == null || this.subType == null) {
            return "";
        }
        return this.primaryType + '/' + this.subType;
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

    public void setPrimaryType(String primaryType2) {
        this.primaryType = primaryType2;
    }

    public void setSubType(String subType2) {
        this.subType = subType2;
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
        if (this.primaryType == null || this.subType == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.primaryType).append('/').append(this.subType);
        if (this.list != null) {
            sb.append(this.list.toString(sb.length() + 14));
        }
        return sb.toString();
    }

    public boolean match(ContentType cType) {
        if ((this.primaryType != null || cType.getPrimaryType() != null) && (this.primaryType == null || !this.primaryType.equalsIgnoreCase(cType.getPrimaryType()))) {
            return false;
        }
        String sType = cType.getSubType();
        if ((this.subType != null && this.subType.startsWith("*")) || (sType != null && sType.startsWith("*"))) {
            return true;
        }
        if ((this.subType != null || sType != null) && (this.subType == null || !this.subType.equalsIgnoreCase(sType))) {
            return false;
        }
        return true;
    }

    public boolean match(String s) {
        try {
            return match(new ContentType(s));
        } catch (ParseException e) {
            return false;
        }
    }
}
