package com.sun.mail.imap.protocol;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import gnu.bytecode.Access;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import kawa.Telnet;

public class BASE64MailboxDecoder {
    static final char[] pem_array = {'A', 'B', Access.CLASS_CONTEXT, 'D', 'E', Access.FIELD_CONTEXT, 'G', 'H', Access.INNERCLASS_CONTEXT, 'J', 'K', 'L', Access.METHOD_CONTEXT, 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', ','};
    private static final byte[] pem_convert_array = new byte[256];

    public static String decode(String original) {
        int copyTo;
        if (original == null || original.length() == 0) {
            return original;
        }
        boolean changedString = false;
        int copyTo2 = 0;
        char[] chars = new char[original.length()];
        StringCharacterIterator iter = new StringCharacterIterator(original);
        char c = iter.first();
        while (true) {
            copyTo = copyTo2;
            if (c == 65535) {
                break;
            }
            if (c == '&') {
                changedString = true;
                copyTo2 = base64decode(chars, copyTo, iter);
            } else {
                copyTo2 = copyTo + 1;
                chars[copyTo] = c;
            }
            c = iter.next();
        }
        if (changedString) {
            return new String(chars, 0, copyTo);
        }
        return original;
    }

    protected static int base64decode(char[] buffer, int offset, CharacterIterator iter) {
        int leftover;
        boolean firsttime = true;
        int leftover2 = -1;
        while (true) {
            byte orig_0 = (byte) iter.next();
            if (orig_0 == -1) {
                return offset;
            }
            if (orig_0 != 45) {
                firsttime = false;
                byte orig_1 = (byte) iter.next();
                if (orig_1 == -1 || orig_1 == 45) {
                    return offset;
                }
                byte a = pem_convert_array[orig_0 & Ev3Constants.Opcode.TST];
                byte b = pem_convert_array[orig_1 & Ev3Constants.Opcode.TST];
                int current = (byte) (((a << 2) & Telnet.WONT) | ((b >>> 4) & 3));
                if (leftover2 != -1) {
                    buffer[offset] = (char) ((leftover2 << 8) | (current & 255));
                    leftover = -1;
                    offset++;
                } else {
                    leftover = current & 255;
                }
                byte orig_2 = (byte) iter.next();
                if (orig_2 != 61) {
                    if (orig_2 == -1 || orig_2 == 45) {
                        return offset;
                    }
                    byte a2 = b;
                    byte b2 = pem_convert_array[orig_2 & Ev3Constants.Opcode.TST];
                    int current2 = (byte) (((a2 << 4) & 240) | ((b2 >>> 2) & 15));
                    if (leftover2 != -1) {
                        buffer[offset] = (char) ((leftover2 << 8) | (current2 & 255));
                        leftover2 = -1;
                        offset++;
                    } else {
                        leftover2 = current2 & 255;
                    }
                    byte orig_3 = (byte) iter.next();
                    if (orig_3 == 61) {
                        continue;
                    } else if (orig_3 == -1 || orig_3 == 45) {
                        return offset;
                    } else {
                        int current3 = (byte) (((b2 << 6) & Ev3Constants.Opcode.FILE) | (pem_convert_array[orig_3 & Ev3Constants.Opcode.TST] & Ev3Constants.Opcode.MOVEF_F));
                        if (leftover2 != -1) {
                            buffer[offset] = (char) ((leftover2 << 8) | (current3 & 255));
                            leftover2 = -1;
                            offset++;
                        } else {
                            leftover2 = current3 & 255;
                        }
                    }
                }
            } else if (!firsttime) {
                return offset;
            } else {
                buffer[offset] = '&';
                return offset + 1;
            }
        }
    }

    static {
        for (int i = 0; i < 255; i++) {
            pem_convert_array[i] = -1;
        }
        for (int i2 = 0; i2 < pem_array.length; i2++) {
            pem_convert_array[pem_array[i2]] = (byte) i2;
        }
    }
}
