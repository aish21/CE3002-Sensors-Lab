package javax.mail;

public class Provider {
    private String className;
    private String protocol;
    private Type type;
    private String vendor;
    private String version;

    public static class Type {
        public static final Type STORE = new Type("STORE");
        public static final Type TRANSPORT = new Type("TRANSPORT");
        private String type;

        private Type(String type2) {
            this.type = type2;
        }

        public String toString() {
            return this.type;
        }
    }

    public Provider(Type type2, String protocol2, String classname, String vendor2, String version2) {
        this.type = type2;
        this.protocol = protocol2;
        this.className = classname;
        this.vendor = vendor2;
        this.version = version2;
    }

    public Type getType() {
        return this.type;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getClassName() {
        return this.className;
    }

    public String getVendor() {
        return this.vendor;
    }

    public String getVersion() {
        return this.version;
    }

    public String toString() {
        String s = "javax.mail.Provider[" + this.type + "," + this.protocol + "," + this.className;
        if (this.vendor != null) {
            s = s + "," + this.vendor;
        }
        if (this.version != null) {
            s = s + "," + this.version;
        }
        return s + "]";
    }
}
