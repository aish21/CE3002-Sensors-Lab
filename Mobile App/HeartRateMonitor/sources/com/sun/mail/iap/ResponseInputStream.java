package com.sun.mail.iap;

import com.sun.mail.util.ASCIIUtility;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResponseInputStream {
    private static final int incrementSlop = 16;
    private static final int maxIncrement = 262144;
    private static final int minIncrement = 256;
    private BufferedInputStream bin;

    public ResponseInputStream(InputStream in) {
        this.bin = new BufferedInputStream(in, 2048);
    }

    public ByteArray readResponse() throws IOException {
        return readResponse((ByteArray) null);
    }

    public ByteArray readResponse(ByteArray ba) throws IOException {
        int idx;
        int i;
        if (ba == null) {
            ba = new ByteArray(new byte[128], 0, 128);
        }
        byte[] buffer = ba.getBytes();
        int idx2 = 0;
        while (true) {
            int b = 0;
            boolean gotCRLF = false;
            idx = idx2;
            while (!gotCRLF) {
                b = this.bin.read();
                if (b == -1) {
                    break;
                }
                if (b == 10 && idx > 0 && buffer[idx - 1] == 13) {
                    gotCRLF = true;
                }
                if (idx >= buffer.length) {
                    int incr = buffer.length;
                    if (incr > 262144) {
                        incr = 262144;
                    }
                    ba.grow(incr);
                    buffer = ba.getBytes();
                }
                buffer[idx] = (byte) b;
                idx++;
            }
            if (b != -1) {
                if (idx < 5 || buffer[idx - 3] != 125) {
                    break;
                }
                int i2 = idx - 4;
                while (i2 >= 0 && buffer[i2] != 123) {
                    i2--;
                }
                if (i2 < 0) {
                    break;
                }
                try {
                    int count = ASCIIUtility.parseInt(buffer, i2 + 1, idx - 3);
                    if (count > 0) {
                        int avail = buffer.length - idx;
                        if (count + 16 > avail) {
                            if (256 > (count + 16) - avail) {
                                i = 256;
                            } else {
                                i = (count + 16) - avail;
                            }
                            ba.grow(i);
                            buffer = ba.getBytes();
                            idx2 = idx;
                        } else {
                            idx2 = idx;
                        }
                        while (count > 0) {
                            int actual = this.bin.read(buffer, idx2, count);
                            if (actual == -1) {
                                throw new IOException("Connection dropped by server?");
                            }
                            count -= actual;
                            idx2 += actual;
                        }
                        continue;
                    } else {
                        idx2 = idx;
                    }
                } catch (NumberFormatException e) {
                }
            } else {
                throw new IOException("Connection dropped by server?");
            }
        }
        ba.setCount(idx);
        return ba;
    }

    public int available() throws IOException {
        return this.bin.available();
    }
}
