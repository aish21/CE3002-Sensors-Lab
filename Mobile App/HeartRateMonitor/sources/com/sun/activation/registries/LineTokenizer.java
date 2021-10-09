package com.sun.activation.registries;

import java.util.NoSuchElementException;
import java.util.Vector;

/* compiled from: MimeTypeFile */
class LineTokenizer {
    private static final String singles = "=";
    private int currentPosition = 0;
    private int maxPosition;
    private Vector stack = new Vector();
    private String str;

    public LineTokenizer(String str2) {
        this.str = str2;
        this.maxPosition = str2.length();
    }

    private void skipWhiteSpace() {
        while (this.currentPosition < this.maxPosition && Character.isWhitespace(this.str.charAt(this.currentPosition))) {
            this.currentPosition++;
        }
    }

    public boolean hasMoreTokens() {
        if (this.stack.size() > 0) {
            return true;
        }
        skipWhiteSpace();
        if (this.currentPosition >= this.maxPosition) {
            return false;
        }
        return true;
    }

    public String nextToken() {
        String s;
        int size = this.stack.size();
        if (size > 0) {
            String t = (String) this.stack.elementAt(size - 1);
            this.stack.removeElementAt(size - 1);
            return t;
        }
        skipWhiteSpace();
        if (this.currentPosition >= this.maxPosition) {
            throw new NoSuchElementException();
        }
        int start = this.currentPosition;
        char c = this.str.charAt(start);
        if (c == '\"') {
            this.currentPosition++;
            boolean filter = false;
            while (this.currentPosition < this.maxPosition) {
                String str2 = this.str;
                int i = this.currentPosition;
                this.currentPosition = i + 1;
                char c2 = str2.charAt(i);
                if (c2 == '\\') {
                    this.currentPosition++;
                    filter = true;
                } else if (c2 == '\"') {
                    if (filter) {
                        StringBuffer sb = new StringBuffer();
                        for (int i2 = start + 1; i2 < this.currentPosition - 1; i2++) {
                            char c3 = this.str.charAt(i2);
                            if (c3 != '\\') {
                                sb.append(c3);
                            }
                        }
                        s = sb.toString();
                    } else {
                        s = this.str.substring(start + 1, this.currentPosition - 1);
                    }
                    return s;
                }
            }
        } else if (singles.indexOf(c) >= 0) {
            this.currentPosition++;
        } else {
            while (this.currentPosition < this.maxPosition && singles.indexOf(this.str.charAt(this.currentPosition)) < 0 && !Character.isWhitespace(this.str.charAt(this.currentPosition))) {
                this.currentPosition++;
            }
        }
        return this.str.substring(start, this.currentPosition);
    }

    public void pushToken(String token) {
        this.stack.addElement(token);
    }
}
