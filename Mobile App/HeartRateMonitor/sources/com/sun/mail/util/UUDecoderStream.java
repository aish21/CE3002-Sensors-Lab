package com.sun.mail.util;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import kawa.Telnet;

public class UUDecoderStream extends FilterInputStream {
    private byte[] buffer = new byte[45];
    private int bufsize = 0;
    private boolean gotEnd = false;
    private boolean gotPrefix = false;
    private boolean ignoreErrors;
    private boolean ignoreMissingBeginEnd;
    private int index = 0;
    private LineInputStream lin;
    private int mode;
    private String name;
    private String readAhead;

    public UUDecoderStream(InputStream in) {
        super(in);
        this.lin = new LineInputStream(in);
        this.ignoreErrors = PropUtil.getBooleanSystemProperty("mail.mime.uudecode.ignoreerrors", false);
        this.ignoreMissingBeginEnd = PropUtil.getBooleanSystemProperty("mail.mime.uudecode.ignoremissingbeginend", false);
    }

    public UUDecoderStream(InputStream in, boolean ignoreErrors2, boolean ignoreMissingBeginEnd2) {
        super(in);
        this.lin = new LineInputStream(in);
        this.ignoreErrors = ignoreErrors2;
        this.ignoreMissingBeginEnd = ignoreMissingBeginEnd2;
    }

    public int read() throws IOException {
        if (this.index >= this.bufsize) {
            readPrefix();
            if (!decode()) {
                return -1;
            }
            this.index = 0;
        }
        byte[] bArr = this.buffer;
        int i = this.index;
        this.index = i + 1;
        return bArr[i] & Ev3Constants.Opcode.TST;
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        int i = 0;
        while (i < len) {
            int c = read();
            if (c != -1) {
                buf[off + i] = (byte) c;
                i++;
            } else if (i == 0) {
                return -1;
            } else {
                return i;
            }
        }
        return i;
    }

    public boolean markSupported() {
        return false;
    }

    public int available() throws IOException {
        return ((this.in.available() * 3) / 4) + (this.bufsize - this.index);
    }

    public String getName() throws IOException {
        readPrefix();
        return this.name;
    }

    public int getMode() throws IOException {
        readPrefix();
        return this.mode;
    }

    private void readPrefix() throws IOException {
        String line;
        if (!this.gotPrefix) {
            this.mode = 438;
            this.name = "encoder.buf";
            while (true) {
                line = this.lin.readLine();
                if (line == null) {
                    if (!this.ignoreMissingBeginEnd) {
                        throw new DecodingException("UUDecoder: Missing begin");
                    }
                    this.gotPrefix = true;
                    this.gotEnd = true;
                    return;
                } else if (line.regionMatches(false, 0, "begin", 0, 5)) {
                    try {
                        this.mode = Integer.parseInt(line.substring(6, 9));
                    } catch (NumberFormatException ex) {
                        if (!this.ignoreErrors) {
                            throw new DecodingException("UUDecoder: Error in mode: " + ex.toString());
                        }
                    }
                    if (line.length() > 10) {
                        this.name = line.substring(10);
                    } else if (!this.ignoreErrors) {
                        throw new DecodingException("UUDecoder: Missing name: " + line);
                    }
                    this.gotPrefix = true;
                    return;
                } else if (this.ignoreMissingBeginEnd && line.length() != 0) {
                    int need = ((((line.charAt(0) - 32) & 63) * 8) + 5) / 6;
                    if (need == 0 || line.length() >= need + 1) {
                        this.readAhead = line;
                        this.gotPrefix = true;
                    }
                }
            }
            this.readAhead = line;
            this.gotPrefix = true;
        }
    }

    private boolean decode() throws IOException {
        String line;
        if (this.gotEnd) {
            return false;
        }
        this.bufsize = 0;
        while (true) {
            if (this.readAhead != null) {
                line = this.readAhead;
                this.readAhead = null;
            } else {
                line = this.lin.readLine();
            }
            if (line == null) {
                if (!this.ignoreMissingBeginEnd) {
                    throw new DecodingException("UUDecoder: Missing end at EOF");
                }
                this.gotEnd = true;
                return false;
            } else if (line.equals("end")) {
                this.gotEnd = true;
                return false;
            } else if (line.length() != 0) {
                int count = line.charAt(0);
                if (count >= 32) {
                    int count2 = (count - 32) & 63;
                    if (count2 == 0) {
                        String line2 = this.lin.readLine();
                        if ((line2 == null || !line2.equals("end")) && !this.ignoreMissingBeginEnd) {
                            throw new DecodingException("UUDecoder: Missing End after count 0 line");
                        }
                        this.gotEnd = true;
                        return false;
                    }
                    if (line.length() >= (((count2 * 8) + 5) / 6) + 1) {
                        int i = 1;
                        while (this.bufsize < count2) {
                            int i2 = i + 1;
                            byte a = (byte) ((line.charAt(i) - ' ') & 63);
                            i = i2 + 1;
                            byte b = (byte) ((line.charAt(i2) - ' ') & 63);
                            byte[] bArr = this.buffer;
                            int i3 = this.bufsize;
                            this.bufsize = i3 + 1;
                            bArr[i3] = (byte) (((a << 2) & Telnet.WONT) | ((b >>> 4) & 3));
                            if (this.bufsize < count2) {
                                byte a2 = b;
                                b = (byte) ((line.charAt(i) - ' ') & 63);
                                byte[] bArr2 = this.buffer;
                                int i4 = this.bufsize;
                                this.bufsize = i4 + 1;
                                bArr2[i4] = (byte) (((a2 << 4) & 240) | ((b >>> 2) & 15));
                                i++;
                            }
                            if (this.bufsize < count2) {
                                byte a3 = b;
                                byte b2 = (byte) ((line.charAt(i) - ' ') & 63);
                                byte[] bArr3 = this.buffer;
                                int i5 = this.bufsize;
                                this.bufsize = i5 + 1;
                                bArr3[i5] = (byte) (((a3 << 6) & Ev3Constants.Opcode.FILE) | (b2 & Ev3Constants.Opcode.MOVEF_F));
                                i++;
                            }
                        }
                        return true;
                    } else if (!this.ignoreErrors) {
                        throw new DecodingException("UUDecoder: Short buffer error");
                    }
                } else if (!this.ignoreErrors) {
                    throw new DecodingException("UUDecoder: Buffer format error");
                }
            }
        }
    }
}
