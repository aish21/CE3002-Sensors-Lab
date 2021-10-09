package com.sun.mail.imap;

import java.util.ArrayList;
import java.util.List;

public class Rights implements Cloneable {
    private boolean[] rights = new boolean[128];

    public static final class Right {
        public static final Right ADMINISTER = getInstance('a');
        public static final Right CREATE = getInstance('c');
        public static final Right DELETE = getInstance('d');
        public static final Right INSERT = getInstance('i');
        public static final Right KEEP_SEEN = getInstance('s');
        public static final Right LOOKUP = getInstance('l');
        public static final Right POST = getInstance('p');
        public static final Right READ = getInstance('r');
        public static final Right WRITE = getInstance('w');
        private static Right[] cache = new Right[128];
        char right;

        private Right(char right2) {
            if (right2 >= 128) {
                throw new IllegalArgumentException("Right must be ASCII");
            }
            this.right = right2;
        }

        public static synchronized Right getInstance(char right2) {
            Right right3;
            synchronized (Right.class) {
                if (right2 >= 128) {
                    throw new IllegalArgumentException("Right must be ASCII");
                }
                if (cache[right2] == null) {
                    cache[right2] = new Right(right2);
                }
                right3 = cache[right2];
            }
            return right3;
        }

        public String toString() {
            return String.valueOf(this.right);
        }
    }

    public Rights() {
    }

    public Rights(Rights rights2) {
        System.arraycopy(rights2.rights, 0, this.rights, 0, this.rights.length);
    }

    public Rights(String rights2) {
        for (int i = 0; i < rights2.length(); i++) {
            add(Right.getInstance(rights2.charAt(i)));
        }
    }

    public Rights(Right right) {
        this.rights[right.right] = true;
    }

    public void add(Right right) {
        this.rights[right.right] = true;
    }

    public void add(Rights rights2) {
        for (int i = 0; i < rights2.rights.length; i++) {
            if (rights2.rights[i]) {
                this.rights[i] = true;
            }
        }
    }

    public void remove(Right right) {
        this.rights[right.right] = false;
    }

    public void remove(Rights rights2) {
        for (int i = 0; i < rights2.rights.length; i++) {
            if (rights2.rights[i]) {
                this.rights[i] = false;
            }
        }
    }

    public boolean contains(Right right) {
        return this.rights[right.right];
    }

    public boolean contains(Rights rights2) {
        for (int i = 0; i < rights2.rights.length; i++) {
            if (rights2.rights[i] && !this.rights[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Rights)) {
            return false;
        }
        Rights rights2 = (Rights) obj;
        for (int i = 0; i < rights2.rights.length; i++) {
            if (rights2.rights[i] != this.rights[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = 0;
        for (boolean z : this.rights) {
            if (z) {
                hash++;
            }
        }
        return hash;
    }

    public Right[] getRights() {
        List<Right> v = new ArrayList<>();
        for (int i = 0; i < this.rights.length; i++) {
            if (this.rights[i]) {
                v.add(Right.getInstance((char) i));
            }
        }
        return (Right[]) v.toArray(new Right[v.size()]);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.sun.mail.imap.Rights} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.Object clone() {
        /*
            r7 = this;
            r1 = 0
            java.lang.Object r2 = super.clone()     // Catch:{ CloneNotSupportedException -> 0x001c }
            r0 = r2
            com.sun.mail.imap.Rights r0 = (com.sun.mail.imap.Rights) r0     // Catch:{ CloneNotSupportedException -> 0x001c }
            r1 = r0
            r2 = 128(0x80, float:1.794E-43)
            boolean[] r2 = new boolean[r2]     // Catch:{ CloneNotSupportedException -> 0x001c }
            r1.rights = r2     // Catch:{ CloneNotSupportedException -> 0x001c }
            boolean[] r2 = r7.rights     // Catch:{ CloneNotSupportedException -> 0x001c }
            r3 = 0
            boolean[] r4 = r1.rights     // Catch:{ CloneNotSupportedException -> 0x001c }
            r5 = 0
            boolean[] r6 = r7.rights     // Catch:{ CloneNotSupportedException -> 0x001c }
            int r6 = r6.length     // Catch:{ CloneNotSupportedException -> 0x001c }
            java.lang.System.arraycopy(r2, r3, r4, r5, r6)     // Catch:{ CloneNotSupportedException -> 0x001c }
        L_0x001b:
            return r1
        L_0x001c:
            r2 = move-exception
            goto L_0x001b
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.Rights.clone():java.lang.Object");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.rights.length; i++) {
            if (this.rights[i]) {
                sb.append((char) i);
            }
        }
        return sb.toString();
    }
}
