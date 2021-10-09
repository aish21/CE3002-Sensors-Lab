package com.sun.mail.iap;

import com.sun.mail.util.ASCIIUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Response {
    private static String ASTRING_CHAR_DELIM = " (){%*\"\\";
    private static String ATOM_CHAR_DELIM = " (){%*\"\\]";
    public static final int BAD = 12;
    public static final int BYE = 16;
    public static final int CONTINUATION = 1;

    /* renamed from: NO */
    public static final int f268NO = 8;

    /* renamed from: OK */
    public static final int f269OK = 4;
    public static final int SYNTHETIC = 32;
    public static final int TAGGED = 2;
    public static final int TAG_MASK = 3;
    public static final int TYPE_MASK = 28;
    public static final int UNTAGGED = 3;
    private static final int increment = 100;
    protected byte[] buffer;

    /* renamed from: ex */
    protected Exception f270ex;
    protected int index;
    protected int pindex;
    protected int size;
    protected String tag;
    protected int type;
    protected boolean utf8;

    public Response(String s) {
        this(s, true);
    }

    public Response(String s, boolean supportsUtf8) {
        this.buffer = null;
        this.type = 0;
        this.tag = null;
        if (supportsUtf8) {
            this.buffer = s.getBytes(StandardCharsets.UTF_8);
        } else {
            this.buffer = s.getBytes(StandardCharsets.US_ASCII);
        }
        this.size = this.buffer.length;
        this.utf8 = supportsUtf8;
        parse();
    }

    public Response(Protocol p) throws IOException, ProtocolException {
        this.buffer = null;
        this.type = 0;
        this.tag = null;
        ByteArray response = p.getInputStream().readResponse(p.getResponseBuffer());
        this.buffer = response.getBytes();
        this.size = response.getCount() - 2;
        this.utf8 = p.supportsUtf8();
        parse();
    }

    public Response(Response r) {
        this.buffer = null;
        this.type = 0;
        this.tag = null;
        this.index = r.index;
        this.pindex = r.pindex;
        this.size = r.size;
        this.buffer = r.buffer;
        this.type = r.type;
        this.tag = r.tag;
        this.f270ex = r.f270ex;
        this.utf8 = r.utf8;
    }

    public static Response byeResponse(Exception ex) {
        Response r = new Response(("* BYE Jakarta Mail Exception: " + ex.toString()).replace(13, ' ').replace(10, ' '));
        r.type |= 32;
        r.f270ex = ex;
        return r;
    }

    public boolean supportsUtf8() {
        return this.utf8;
    }

    private void parse() {
        this.index = 0;
        if (this.size != 0) {
            if (this.buffer[this.index] == 43) {
                this.type |= 1;
                this.index++;
                return;
            }
            if (this.buffer[this.index] == 42) {
                this.type |= 3;
                this.index++;
            } else {
                this.type |= 2;
                this.tag = readAtom();
                if (this.tag == null) {
                    this.tag = "";
                }
            }
            int mark = this.index;
            String s = readAtom();
            if (s == null) {
                s = "";
            }
            if (s.equalsIgnoreCase("OK")) {
                this.type |= 4;
            } else if (s.equalsIgnoreCase("NO")) {
                this.type |= 8;
            } else if (s.equalsIgnoreCase("BAD")) {
                this.type |= 12;
            } else if (s.equalsIgnoreCase("BYE")) {
                this.type |= 16;
            } else {
                this.index = mark;
            }
            this.pindex = this.index;
        }
    }

    public void skipSpaces() {
        while (this.index < this.size && this.buffer[this.index] == 32) {
            this.index++;
        }
    }

    public boolean isNextNonSpace(char c) {
        skipSpaces();
        if (this.index >= this.size || this.buffer[this.index] != ((byte) c)) {
            return false;
        }
        this.index++;
        return true;
    }

    public void skipToken() {
        while (this.index < this.size && this.buffer[this.index] != 32) {
            this.index++;
        }
    }

    public void skip(int count) {
        this.index += count;
    }

    public byte peekByte() {
        if (this.index < this.size) {
            return this.buffer[this.index];
        }
        return 0;
    }

    public byte readByte() {
        if (this.index >= this.size) {
            return 0;
        }
        byte[] bArr = this.buffer;
        int i = this.index;
        this.index = i + 1;
        return bArr[i];
    }

    public String readAtom() {
        return readDelimString(ATOM_CHAR_DELIM);
    }

    private String readDelimString(String delim) {
        skipSpaces();
        if (this.index >= this.size) {
            return null;
        }
        int start = this.index;
        while (this.index < this.size && (b = this.buffer[this.index] & 255) >= 32 && delim.indexOf((char) b) < 0 && b != 127) {
            this.index++;
        }
        return toString(this.buffer, start, this.index);
    }

    public String readString(char delim) {
        skipSpaces();
        if (this.index >= this.size) {
            return null;
        }
        int start = this.index;
        while (this.index < this.size && this.buffer[this.index] != delim) {
            this.index++;
        }
        return toString(this.buffer, start, this.index);
    }

    public String[] readStringList() {
        return readStringList(false);
    }

    public String[] readAtomStringList() {
        return readStringList(true);
    }

    private String[] readStringList(boolean atom) {
        skipSpaces();
        if (this.buffer[this.index] != 40) {
            return null;
        }
        this.index++;
        List<String> result = new ArrayList<>();
        while (!isNextNonSpace(')')) {
            String s = atom ? readAtomString() : readString();
            if (s == null) {
                break;
            }
            result.add(s);
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public int readNumber() {
        skipSpaces();
        int start = this.index;
        while (this.index < this.size && Character.isDigit((char) this.buffer[this.index])) {
            this.index++;
        }
        if (this.index > start) {
            try {
                return ASCIIUtility.parseInt(this.buffer, start, this.index);
            } catch (NumberFormatException e) {
            }
        }
        return -1;
    }

    public long readLong() {
        skipSpaces();
        int start = this.index;
        while (this.index < this.size && Character.isDigit((char) this.buffer[this.index])) {
            this.index++;
        }
        if (this.index > start) {
            try {
                return ASCIIUtility.parseLong(this.buffer, start, this.index);
            } catch (NumberFormatException e) {
            }
        }
        return -1;
    }

    public String readString() {
        return (String) parseString(false, true);
    }

    public ByteArrayInputStream readBytes() {
        ByteArray ba = readByteArray();
        if (ba != null) {
            return ba.toByteArrayInputStream();
        }
        return null;
    }

    public ByteArray readByteArray() {
        if (!isContinuation()) {
            return (ByteArray) parseString(false, false);
        }
        skipSpaces();
        return new ByteArray(this.buffer, this.index, this.size - this.index);
    }

    public String readAtomString() {
        return (String) parseString(true, true);
    }

    private Object parseString(boolean parseAtoms, boolean returnString) {
        byte b;
        skipSpaces();
        byte b2 = this.buffer[this.index];
        if (b2 == 34) {
            this.index++;
            int start = this.index;
            int copyto = this.index;
            while (this.index < this.size && (b = this.buffer[this.index]) != 34) {
                if (b == 92) {
                    this.index++;
                }
                if (this.index != copyto) {
                    this.buffer[copyto] = this.buffer[this.index];
                }
                copyto++;
                this.index++;
            }
            if (this.index >= this.size) {
                return null;
            }
            this.index++;
            if (returnString) {
                return toString(this.buffer, start, copyto);
            }
            return new ByteArray(this.buffer, start, copyto - start);
        } else if (b2 == 123) {
            int start2 = this.index + 1;
            this.index = start2;
            while (this.buffer[this.index] != 125) {
                this.index++;
            }
            try {
                int count = ASCIIUtility.parseInt(this.buffer, start2, this.index);
                int start3 = this.index + 3;
                this.index = start3 + count;
                if (returnString) {
                    return toString(this.buffer, start3, start3 + count);
                }
                return new ByteArray(this.buffer, start3, count);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (parseAtoms) {
            int start4 = this.index;
            String readDelimString = readDelimString(ASTRING_CHAR_DELIM);
            if (!returnString) {
                return new ByteArray(this.buffer, start4, this.index);
            }
            return readDelimString;
        } else if (b2 != 78 && b2 != 110) {
            return null;
        } else {
            this.index += 3;
            return null;
        }
    }

    private String toString(byte[] buffer2, int start, int end) {
        if (this.utf8) {
            return new String(buffer2, start, end - start, StandardCharsets.UTF_8);
        }
        return ASCIIUtility.toString(buffer2, start, end);
    }

    public int getType() {
        return this.type;
    }

    public boolean isContinuation() {
        return (this.type & 3) == 1;
    }

    public boolean isTagged() {
        return (this.type & 3) == 2;
    }

    public boolean isUnTagged() {
        return (this.type & 3) == 3;
    }

    public boolean isOK() {
        return (this.type & 28) == 4;
    }

    public boolean isNO() {
        return (this.type & 28) == 8;
    }

    public boolean isBAD() {
        return (this.type & 28) == 12;
    }

    public boolean isBYE() {
        return (this.type & 28) == 16;
    }

    public boolean isSynthetic() {
        return (this.type & 32) == 32;
    }

    public String getTag() {
        return this.tag;
    }

    public String getRest() {
        skipSpaces();
        return toString(this.buffer, this.index, this.size);
    }

    public Exception getException() {
        return this.f270ex;
    }

    public void reset() {
        this.index = this.pindex;
    }

    public String toString() {
        return toString(this.buffer, 0, this.size);
    }
}
