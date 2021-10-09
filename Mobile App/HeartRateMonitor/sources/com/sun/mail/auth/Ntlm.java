package com.sun.mail.auth;

import com.google.appinventor.components.runtime.util.Ev3Constants;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.MailLogger;
import gnu.bytecode.Access;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Ntlm {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final byte HIRESPONSERVERSION = 1;
    private static final int NTLMSSP_NEGOTIATE_128 = 536870912;
    private static final int NTLMSSP_NEGOTIATE_56 = Integer.MIN_VALUE;
    private static final int NTLMSSP_NEGOTIATE_ALWAYS_SIGN = 32768;
    private static final int NTLMSSP_NEGOTIATE_DATAGRAM = 64;
    private static final int NTLMSSP_NEGOTIATE_EXTENDED_SESSIONSECURITY = 524288;
    private static final int NTLMSSP_NEGOTIATE_IDENTIFY = 1048576;
    private static final int NTLMSSP_NEGOTIATE_KEY_EXCH = 1073741824;
    private static final int NTLMSSP_NEGOTIATE_LM_KEY = 128;
    private static final int NTLMSSP_NEGOTIATE_NTLM = 512;
    private static final int NTLMSSP_NEGOTIATE_OEM = 2;
    private static final int NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED = 4096;
    private static final int NTLMSSP_NEGOTIATE_OEM_WORKSTATION_SUPPLIED = 8192;
    private static final int NTLMSSP_NEGOTIATE_SEAL = 32;
    private static final int NTLMSSP_NEGOTIATE_SIGN = 16;
    private static final int NTLMSSP_NEGOTIATE_TARGET_INFO = 8388608;
    private static final int NTLMSSP_NEGOTIATE_UNICODE = 1;
    private static final int NTLMSSP_NEGOTIATE_VERSION = 33554432;
    private static final int NTLMSSP_REQUEST_NON_NT_SESSION_KEY = 4194304;
    private static final int NTLMSSP_REQUEST_TARGET = 4;
    private static final int NTLMSSP_TARGET_TYPE_DOMAIN = 65536;
    private static final int NTLMSSP_TARGET_TYPE_SERVER = 131072;
    private static final byte RESPONSERVERSION = 1;

    /* renamed from: Z4 */
    private static final byte[] f265Z4 = {0, 0, 0, 0};

    /* renamed from: Z6 */
    private static final byte[] f266Z6 = {0, 0, 0, 0, 0, 0};
    private static char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', Access.CLASS_CONTEXT, 'D', 'E', Access.FIELD_CONTEXT};
    private Cipher cipher;
    private SecretKeyFactory fac;
    private Mac hmac;
    private String hostname;
    private MailLogger logger;
    private MD4 md4;
    private String ntdomain;
    private String password;
    private byte[] type1;
    private byte[] type3;
    private String username;

    static {
        boolean z;
        if (!Ntlm.class.desiredAssertionStatus()) {
            z = true;
        } else {
            z = $assertionsDisabled;
        }
        $assertionsDisabled = z;
    }

    private void init0() {
        this.type1 = new byte[256];
        this.type3 = new byte[512];
        System.arraycopy(new byte[]{Ev3Constants.Opcode.CP_EQ32, Ev3Constants.Opcode.CP_LTEQ8, Ev3Constants.Opcode.CP_EQ8, Ev3Constants.Opcode.CP_EQ16, Ev3Constants.Opcode.CP_NEQF, Ev3Constants.Opcode.CP_NEQF, Ev3Constants.Opcode.CP_NEQ8, 0, 1}, 0, this.type1, 0, 9);
        System.arraycopy(new byte[]{Ev3Constants.Opcode.CP_EQ32, Ev3Constants.Opcode.CP_LTEQ8, Ev3Constants.Opcode.CP_EQ8, Ev3Constants.Opcode.CP_EQ16, Ev3Constants.Opcode.CP_NEQF, Ev3Constants.Opcode.CP_NEQF, Ev3Constants.Opcode.CP_NEQ8, 0, 3}, 0, this.type3, 0, 9);
        try {
            this.fac = SecretKeyFactory.getInstance("DES");
            this.cipher = Cipher.getInstance("DES/ECB/NoPadding");
            this.md4 = new MD4();
        } catch (NoSuchPaddingException e) {
            if (!$assertionsDisabled) {
                throw new AssertionError();
            }
        } catch (NoSuchAlgorithmException e2) {
            if (!$assertionsDisabled) {
                throw new AssertionError();
            }
        }
    }

    public Ntlm(String ntdomain2, String hostname2, String username2, String password2, MailLogger logger2) {
        int i = hostname2.indexOf(46);
        hostname2 = i != -1 ? hostname2.substring(0, i) : hostname2;
        int i2 = username2.indexOf(92);
        if (i2 != -1) {
            ntdomain2 = username2.substring(0, i2).toUpperCase(Locale.ENGLISH);
            username2 = username2.substring(i2 + 1);
        } else if (ntdomain2 == null) {
            ntdomain2 = "";
        }
        this.ntdomain = ntdomain2;
        this.hostname = hostname2;
        this.username = username2;
        this.password = password2;
        this.logger = logger2.getLogger(getClass(), "DEBUG NTLM");
        init0();
    }

    private void copybytes(byte[] dest, int destpos, String src, String enc) {
        try {
            byte[] x = src.getBytes(enc);
            System.arraycopy(x, 0, dest, destpos, x.length);
        } catch (UnsupportedEncodingException e) {
            if (!$assertionsDisabled) {
                throw new AssertionError();
            }
        }
    }

    public String generateType1Msg(int flags) {
        return generateType1Msg(flags, $assertionsDisabled);
    }

    public String generateType1Msg(int flags, boolean v2) {
        int dlen = this.ntdomain.length();
        int type1flags = 41475 | flags;
        if (dlen != 0) {
            type1flags |= 4096;
        }
        if (v2) {
            type1flags |= 524288;
        }
        writeInt(this.type1, 12, type1flags);
        this.type1[28] = 32;
        writeShort(this.type1, 16, dlen);
        writeShort(this.type1, 18, dlen);
        int hlen = this.hostname.length();
        writeShort(this.type1, 24, hlen);
        writeShort(this.type1, 26, hlen);
        copybytes(this.type1, 32, this.hostname, "iso-8859-1");
        copybytes(this.type1, hlen + 32, this.ntdomain, "iso-8859-1");
        writeInt(this.type1, 20, hlen + 32);
        byte[] msg = new byte[(hlen + 32 + dlen)];
        System.arraycopy(this.type1, 0, msg, 0, hlen + 32 + dlen);
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("type 1 message: " + toHex(msg));
        }
        try {
            return new String(BASE64EncoderStream.encode(msg), "iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            if ($assertionsDisabled) {
                return null;
            }
            throw new AssertionError();
        }
    }

    private byte[] makeDesKey(byte[] input, int off) {
        int[] in = new int[input.length];
        for (int i = 0; i < in.length; i++) {
            in[i] = input[i] < 0 ? input[i] + 256 : input[i];
        }
        return new byte[]{(byte) in[off + 0], (byte) (((in[off + 0] << 7) & 255) | (in[off + 1] >> 1)), (byte) (((in[off + 1] << 6) & 255) | (in[off + 2] >> 2)), (byte) (((in[off + 2] << 5) & 255) | (in[off + 3] >> 3)), (byte) (((in[off + 3] << 4) & 255) | (in[off + 4] >> 4)), (byte) (((in[off + 4] << 3) & 255) | (in[off + 5] >> 5)), (byte) (((in[off + 5] << 2) & 255) | (in[off + 6] >> 6)), (byte) ((in[off + 6] << 1) & 255)};
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004c, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] hmacMD5(byte[] r9, byte[] r10) {
        /*
            r8 = this;
            r4 = 16
            javax.crypto.Mac r5 = r8.hmac     // Catch:{ NoSuchAlgorithmException -> 0x002d }
            if (r5 != 0) goto L_0x000e
            java.lang.String r5 = "HmacMD5"
            javax.crypto.Mac r5 = javax.crypto.Mac.getInstance(r5)     // Catch:{ NoSuchAlgorithmException -> 0x002d }
            r8.hmac = r5     // Catch:{ NoSuchAlgorithmException -> 0x002d }
        L_0x000e:
            r5 = 16
            byte[] r2 = new byte[r5]     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
            r5 = 0
            r6 = 0
            int r7 = r9.length     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
            if (r7 <= r4) goto L_0x0034
        L_0x0017:
            java.lang.System.arraycopy(r9, r5, r2, r6, r4)     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
            javax.crypto.spec.SecretKeySpec r3 = new javax.crypto.spec.SecretKeySpec     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
            java.lang.String r4 = "HmacMD5"
            r3.<init>(r2, r4)     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
            javax.crypto.Mac r4 = r8.hmac     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
            r4.init(r3)     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
            javax.crypto.Mac r4 = r8.hmac     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
            byte[] r4 = r4.doFinal(r10)     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
        L_0x002c:
            return r4
        L_0x002d:
            r1 = move-exception
            java.lang.AssertionError r4 = new java.lang.AssertionError
            r4.<init>()
            throw r4
        L_0x0034:
            int r4 = r9.length     // Catch:{ InvalidKeyException -> 0x0036, RuntimeException -> 0x0041 }
            goto L_0x0017
        L_0x0036:
            r1 = move-exception
            boolean r4 = $assertionsDisabled
            if (r4 != 0) goto L_0x004c
            java.lang.AssertionError r4 = new java.lang.AssertionError
            r4.<init>()
            throw r4
        L_0x0041:
            r0 = move-exception
            boolean r4 = $assertionsDisabled
            if (r4 != 0) goto L_0x004c
            java.lang.AssertionError r4 = new java.lang.AssertionError
            r4.<init>()
            throw r4
        L_0x004c:
            r4 = 0
            goto L_0x002c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.auth.Ntlm.hmacMD5(byte[], byte[]):byte[]");
    }

    private byte[] calcLMHash() throws GeneralSecurityException {
        byte[] magic = {Ev3Constants.Opcode.CP_GTF, Ev3Constants.Opcode.CP_LTF, Ev3Constants.Opcode.CP_NEQF, Ev3Constants.Opcode.OR16, Ev3Constants.Opcode.f32JR, 35, Ev3Constants.Opcode.AND8, Ev3Constants.Opcode.AND16};
        byte[] pwb = null;
        try {
            pwb = this.password.toUpperCase(Locale.ENGLISH).getBytes("iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            if (!$assertionsDisabled) {
                throw new AssertionError();
            }
        }
        byte[] pwb1 = new byte[14];
        int len = this.password.length();
        if (len > 14) {
            len = 14;
        }
        System.arraycopy(pwb, 0, pwb1, 0, len);
        DESKeySpec dks1 = new DESKeySpec(makeDesKey(pwb1, 0));
        DESKeySpec dks2 = new DESKeySpec(makeDesKey(pwb1, 7));
        SecretKey key1 = this.fac.generateSecret(dks1);
        SecretKey key2 = this.fac.generateSecret(dks2);
        this.cipher.init(1, key1);
        byte[] out1 = this.cipher.doFinal(magic, 0, 8);
        this.cipher.init(1, key2);
        byte[] out2 = this.cipher.doFinal(magic, 0, 8);
        byte[] result = new byte[21];
        System.arraycopy(out1, 0, result, 0, 8);
        System.arraycopy(out2, 0, result, 8, 8);
        return result;
    }

    private byte[] calcNTHash() throws GeneralSecurityException {
        byte[] pw = null;
        try {
            pw = this.password.getBytes("UnicodeLittleUnmarked");
        } catch (UnsupportedEncodingException e) {
            if (!$assertionsDisabled) {
                throw new AssertionError();
            }
        }
        byte[] result = new byte[21];
        System.arraycopy(this.md4.digest(pw), 0, result, 0, 16);
        return result;
    }

    private byte[] calcResponse(byte[] key, byte[] text) throws GeneralSecurityException {
        if ($assertionsDisabled || key.length == 21) {
            DESKeySpec dks1 = new DESKeySpec(makeDesKey(key, 0));
            DESKeySpec dks2 = new DESKeySpec(makeDesKey(key, 7));
            DESKeySpec dks3 = new DESKeySpec(makeDesKey(key, 14));
            SecretKey key1 = this.fac.generateSecret(dks1);
            SecretKey key2 = this.fac.generateSecret(dks2);
            SecretKey key3 = this.fac.generateSecret(dks3);
            this.cipher.init(1, key1);
            byte[] out1 = this.cipher.doFinal(text, 0, 8);
            this.cipher.init(1, key2);
            byte[] out2 = this.cipher.doFinal(text, 0, 8);
            this.cipher.init(1, key3);
            byte[] out3 = this.cipher.doFinal(text, 0, 8);
            byte[] result = new byte[24];
            System.arraycopy(out1, 0, result, 0, 8);
            System.arraycopy(out2, 0, result, 8, 8);
            System.arraycopy(out3, 0, result, 16, 8);
            return result;
        }
        throw new AssertionError();
    }

    private byte[] calcV2Response(byte[] nthash, byte[] blob, byte[] challenge) throws GeneralSecurityException {
        byte[] txt = null;
        try {
            txt = (this.username.toUpperCase(Locale.ENGLISH) + this.ntdomain).getBytes("UnicodeLittleUnmarked");
        } catch (UnsupportedEncodingException e) {
            if (!$assertionsDisabled) {
                throw new AssertionError();
            }
        }
        byte[] ntlmv2hash = hmacMD5(nthash, txt);
        byte[] cb = new byte[(blob.length + 8)];
        System.arraycopy(challenge, 0, cb, 0, 8);
        System.arraycopy(blob, 0, cb, 8, blob.length);
        byte[] result = new byte[(blob.length + 16)];
        System.arraycopy(hmacMD5(ntlmv2hash, cb), 0, result, 0, 16);
        System.arraycopy(blob, 0, result, 16, blob.length);
        return result;
    }

    public String generateType3Msg(String type2msg) {
        byte[] lmresponse;
        byte[] ntresponse;
        byte[] type2 = null;
        try {
            type2 = BASE64DecoderStream.decode(type2msg.getBytes("us-ascii"));
        } catch (UnsupportedEncodingException e) {
            if ($assertionsDisabled) {
                return null;
            }
            throw new AssertionError();
        } catch (UnsupportedEncodingException e2) {
            if (!$assertionsDisabled) {
                throw new AssertionError();
            }
        } catch (GeneralSecurityException ex) {
            this.logger.log(Level.FINE, "GeneralSecurityException", (Throwable) ex);
            return "";
        }
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("type 2 message: " + toHex(type2));
        }
        byte[] challenge = new byte[8];
        System.arraycopy(type2, 24, challenge, 0, 8);
        int type3flags = 33281;
        int ulen = this.username.length() * 2;
        writeShort(this.type3, 36, ulen);
        writeShort(this.type3, 38, ulen);
        int dlen = this.ntdomain.length() * 2;
        writeShort(this.type3, 28, dlen);
        writeShort(this.type3, 30, dlen);
        int hlen = this.hostname.length() * 2;
        writeShort(this.type3, 44, hlen);
        writeShort(this.type3, 46, hlen);
        copybytes(this.type3, 64, this.ntdomain, "UnicodeLittleUnmarked");
        writeInt(this.type3, 32, 64);
        int l = 64 + dlen;
        copybytes(this.type3, l, this.username, "UnicodeLittleUnmarked");
        writeInt(this.type3, 40, l);
        int l2 = l + ulen;
        copybytes(this.type3, l2, this.hostname, "UnicodeLittleUnmarked");
        writeInt(this.type3, 48, l2);
        int l3 = l2 + hlen;
        int flags = readInt(type2, 20);
        if ((524288 & flags) != 0) {
            this.logger.fine("Using NTLMv2");
            type3flags = 33281 | 524288;
            byte[] nonce = new byte[8];
            new Random().nextBytes(nonce);
            byte[] nthash = calcNTHash();
            lmresponse = calcV2Response(nthash, nonce, challenge);
            byte[] targetInfo = new byte[0];
            if ((8388608 & flags) != 0) {
                int tlen = readShort(type2, 40);
                targetInfo = new byte[tlen];
                System.arraycopy(type2, readInt(type2, 44), targetInfo, 0, tlen);
            }
            byte[] blob = new byte[(targetInfo.length + 32)];
            blob[0] = 1;
            blob[1] = 1;
            System.arraycopy(f266Z6, 0, blob, 2, 6);
            long now = (System.currentTimeMillis() + 11644473600000L) * 10000;
            for (int i = 0; i < 8; i++) {
                blob[i + 8] = (byte) ((int) (255 & now));
                now >>= 8;
            }
            System.arraycopy(nonce, 0, blob, 16, 8);
            System.arraycopy(f265Z4, 0, blob, 24, 4);
            System.arraycopy(targetInfo, 0, blob, 28, targetInfo.length);
            System.arraycopy(f265Z4, 0, blob, targetInfo.length + 28, 4);
            ntresponse = calcV2Response(nthash, blob, challenge);
        } else {
            lmresponse = calcResponse(calcLMHash(), challenge);
            ntresponse = calcResponse(calcNTHash(), challenge);
        }
        System.arraycopy(lmresponse, 0, this.type3, l3, lmresponse.length);
        writeShort(this.type3, 12, lmresponse.length);
        writeShort(this.type3, 14, lmresponse.length);
        writeInt(this.type3, 16, l3);
        int l4 = l3 + 24;
        System.arraycopy(ntresponse, 0, this.type3, l4, ntresponse.length);
        writeShort(this.type3, 20, ntresponse.length);
        writeShort(this.type3, 22, ntresponse.length);
        writeInt(this.type3, 24, l4);
        int l5 = l4 + ntresponse.length;
        writeShort(this.type3, 56, l5);
        byte[] msg = new byte[l5];
        System.arraycopy(this.type3, 0, msg, 0, l5);
        writeInt(this.type3, 60, type3flags);
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("type 3 message: " + toHex(msg));
        }
        return new String(BASE64EncoderStream.encode(msg), "iso-8859-1");
    }

    private static int readShort(byte[] b, int off) {
        return (b[off] & Ev3Constants.Opcode.TST) | ((b[off + 1] & Ev3Constants.Opcode.TST) << 8);
    }

    private void writeShort(byte[] b, int off, int data) {
        b[off] = (byte) (data & 255);
        b[off + 1] = (byte) ((data >> 8) & 255);
    }

    private static int readInt(byte[] b, int off) {
        return (b[off] & Ev3Constants.Opcode.TST) | ((b[off + 1] & Ev3Constants.Opcode.TST) << 8) | ((b[off + 2] & Ev3Constants.Opcode.TST) << 16) | ((b[off + 3] & Ev3Constants.Opcode.TST) << 24);
    }

    private void writeInt(byte[] b, int off, int data) {
        b[off] = (byte) (data & 255);
        b[off + 1] = (byte) ((data >> 8) & 255);
        b[off + 2] = (byte) ((data >> 16) & 255);
        b[off + 3] = (byte) ((data >> 24) & 255);
    }

    private static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 3);
        for (int i = 0; i < b.length; i++) {
            sb.append(hex[(b[i] >> 4) & 15]).append(hex[b[i] & 15]).append(' ');
        }
        return sb.toString();
    }
}
