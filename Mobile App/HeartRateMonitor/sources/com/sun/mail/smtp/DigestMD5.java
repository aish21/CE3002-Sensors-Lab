package com.sun.mail.smtp;

import androidx.appcompat.widget.ActivityChooserView;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import com.sun.mail.util.MailLogger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

public class DigestMD5 {
    private static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private String clientResponse;
    private MailLogger logger;
    private MessageDigest md5;
    private String uri;

    public DigestMD5(MailLogger logger2) {
        this.logger = logger2.getLogger(getClass(), "DEBUG DIGEST-MD5");
        logger2.config("DIGEST-MD5 Loaded");
    }

    public byte[] authClient(String host, String user, String passwd, String realm, String serverChallenge) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream b64os = new BASE64EncoderStream(bos, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
        try {
            SecureRandom random = new SecureRandom();
            this.md5 = MessageDigest.getInstance("MD5");
            StringBuilder result = new StringBuilder();
            this.uri = "smtp/" + host;
            byte[] bytes = new byte[32];
            this.logger.fine("Begin authentication ...");
            Map<String, String> map = tokenize(serverChallenge);
            if (realm == null) {
                String text = map.get("realm");
                if (text != null) {
                    realm = new StringTokenizer(text, ",").nextToken();
                } else {
                    realm = host;
                }
            }
            String nonce = map.get("nonce");
            String charset = map.get("charset");
            boolean utf8 = charset != null && charset.equalsIgnoreCase("utf-8");
            random.nextBytes(bytes);
            b64os.write(bytes);
            b64os.flush();
            String cnonce = bos.toString("iso-8859-1");
            bos.reset();
            if (utf8) {
                this.md5.update(this.md5.digest((user + ":" + realm + ":" + passwd).getBytes(StandardCharsets.UTF_8)));
            } else {
                this.md5.update(this.md5.digest(ASCIIUtility.getBytes(user + ":" + realm + ":" + passwd)));
            }
            this.md5.update(ASCIIUtility.getBytes(":" + nonce + ":" + cnonce));
            this.clientResponse = toHex(this.md5.digest()) + ":" + nonce + ":" + "00000001" + ":" + cnonce + ":" + "auth" + ":";
            this.md5.update(ASCIIUtility.getBytes("AUTHENTICATE:" + this.uri));
            this.md5.update(ASCIIUtility.getBytes(this.clientResponse + toHex(this.md5.digest())));
            result.append("username=\"" + user + "\"");
            result.append(",realm=\"" + realm + "\"");
            result.append(",qop=" + "auth");
            result.append(",nc=" + "00000001");
            result.append(",nonce=\"" + nonce + "\"");
            result.append(",cnonce=\"" + cnonce + "\"");
            result.append(",digest-uri=\"" + this.uri + "\"");
            if (utf8) {
                result.append(",charset=\"utf-8\"");
            }
            result.append(",response=" + toHex(this.md5.digest()));
            if (this.logger.isLoggable(Level.FINE)) {
                this.logger.fine("Response => " + result.toString());
            }
            b64os.write(ASCIIUtility.getBytes(result.toString()));
            b64os.flush();
            return bos.toByteArray();
        } catch (NoSuchAlgorithmException ex) {
            this.logger.log(Level.FINE, "NoSuchAlgorithmException", (Throwable) ex);
            throw new IOException(ex.toString());
        }
    }

    public boolean authServer(String serverResponse) throws IOException {
        Map<String, String> map = tokenize(serverResponse);
        this.md5.update(ASCIIUtility.getBytes(":" + this.uri));
        this.md5.update(ASCIIUtility.getBytes(this.clientResponse + toHex(this.md5.digest())));
        String text = toHex(this.md5.digest());
        if (text.equals(map.get("rspauth"))) {
            return true;
        }
        if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("Expected => rspauth=" + text);
        }
        return false;
    }

    private Map<String, String> tokenize(String serverResponse) throws IOException {
        Map<String, String> map = new HashMap<>();
        byte[] bytes = serverResponse.getBytes("iso-8859-1");
        String key = null;
        StreamTokenizer tokens = new StreamTokenizer(new InputStreamReader(new BASE64DecoderStream(new ByteArrayInputStream(bytes, 4, bytes.length - 4)), "iso-8859-1"));
        tokens.ordinaryChars(48, 57);
        tokens.wordChars(48, 57);
        while (true) {
            int ttype = tokens.nextToken();
            if (ttype != -1) {
                switch (ttype) {
                    case -3:
                        if (key == null) {
                            key = tokens.sval;
                            break;
                        }
                    case 34:
                        if (this.logger.isLoggable(Level.FINE)) {
                            this.logger.fine("Received => " + key + "='" + tokens.sval + "'");
                        }
                        if (map.containsKey(key)) {
                            map.put(key, map.get(key) + "," + tokens.sval);
                        } else {
                            map.put(key, tokens.sval);
                        }
                        key = null;
                        break;
                }
            } else {
                return map;
            }
        }
    }

    private static String toHex(byte[] bytes) {
        char[] result = new char[(bytes.length * 2)];
        int i = 0;
        for (byte b : bytes) {
            int temp = b & 255;
            int i2 = i + 1;
            result[i] = digits[temp >> 4];
            i = i2 + 1;
            result[i2] = digits[temp & 15];
        }
        return new String(result);
    }
}
