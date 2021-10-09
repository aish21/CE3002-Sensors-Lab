package com.sun.activation.registries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MailcapFile {
    private static boolean addReverse;
    private Map fallback_hash = new HashMap();
    private Map native_commands = new HashMap();
    private Map type_hash = new HashMap();

    static {
        addReverse = false;
        try {
            addReverse = Boolean.getBoolean("javax.activation.addreverse");
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x004b A[SYNTHETIC, Splitter:B:13:0x004b] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MailcapFile(java.lang.String r5) throws java.io.IOException {
        /*
            r4 = this;
            r4.<init>()
            java.util.HashMap r2 = new java.util.HashMap
            r2.<init>()
            r4.type_hash = r2
            java.util.HashMap r2 = new java.util.HashMap
            r2.<init>()
            r4.fallback_hash = r2
            java.util.HashMap r2 = new java.util.HashMap
            r2.<init>()
            r4.native_commands = r2
            boolean r2 = com.sun.activation.registries.LogSupport.isLoggable()
            if (r2 == 0) goto L_0x0034
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "new MailcapFile: file "
            java.lang.StringBuilder r2 = r2.append(r3)
            java.lang.StringBuilder r2 = r2.append(r5)
            java.lang.String r2 = r2.toString()
            com.sun.activation.registries.LogSupport.log(r2)
        L_0x0034:
            r0 = 0
            java.io.FileReader r1 = new java.io.FileReader     // Catch:{ all -> 0x0048 }
            r1.<init>(r5)     // Catch:{ all -> 0x0048 }
            java.io.BufferedReader r2 = new java.io.BufferedReader     // Catch:{ all -> 0x0053 }
            r2.<init>(r1)     // Catch:{ all -> 0x0053 }
            r4.parse(r2)     // Catch:{ all -> 0x0053 }
            if (r1 == 0) goto L_0x0047
            r1.close()     // Catch:{ IOException -> 0x004f }
        L_0x0047:
            return
        L_0x0048:
            r2 = move-exception
        L_0x0049:
            if (r0 == 0) goto L_0x004e
            r0.close()     // Catch:{ IOException -> 0x0051 }
        L_0x004e:
            throw r2
        L_0x004f:
            r2 = move-exception
            goto L_0x0047
        L_0x0051:
            r3 = move-exception
            goto L_0x004e
        L_0x0053:
            r2 = move-exception
            r0 = r1
            goto L_0x0049
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.activation.registries.MailcapFile.<init>(java.lang.String):void");
    }

    public MailcapFile(InputStream is) throws IOException {
        if (LogSupport.isLoggable()) {
            LogSupport.log("new MailcapFile: InputStream");
        }
        parse(new BufferedReader(new InputStreamReader(is, "iso-8859-1")));
    }

    public MailcapFile() {
        if (LogSupport.isLoggable()) {
            LogSupport.log("new MailcapFile: default");
        }
    }

    public Map getMailcapList(String mime_type) {
        Map search_result = (Map) this.type_hash.get(mime_type);
        int separator = mime_type.indexOf(47);
        if (mime_type.substring(separator + 1).equals("*")) {
            return search_result;
        }
        Map wildcard_result = (Map) this.type_hash.get(mime_type.substring(0, separator + 1) + "*");
        if (wildcard_result == null) {
            return search_result;
        }
        if (search_result != null) {
            return mergeResults(search_result, wildcard_result);
        }
        return wildcard_result;
    }

    public Map getMailcapFallbackList(String mime_type) {
        Map search_result = (Map) this.fallback_hash.get(mime_type);
        int separator = mime_type.indexOf(47);
        if (mime_type.substring(separator + 1).equals("*")) {
            return search_result;
        }
        Map wildcard_result = (Map) this.fallback_hash.get(mime_type.substring(0, separator + 1) + "*");
        if (wildcard_result == null) {
            return search_result;
        }
        if (search_result != null) {
            return mergeResults(search_result, wildcard_result);
        }
        return wildcard_result;
    }

    public String[] getMimeTypes() {
        Set types = new HashSet(this.type_hash.keySet());
        types.addAll(this.fallback_hash.keySet());
        types.addAll(this.native_commands.keySet());
        return (String[]) types.toArray(new String[types.size()]);
    }

    public String[] getNativeCommands(String mime_type) {
        List v = (List) this.native_commands.get(mime_type.toLowerCase(Locale.ENGLISH));
        if (v != null) {
            return (String[]) v.toArray(new String[v.size()]);
        }
        return null;
    }

    private Map mergeResults(Map first, Map second) {
        Map clonedHash = new HashMap(first);
        for (String verb : second.keySet()) {
            List cmdVector = (List) clonedHash.get(verb);
            if (cmdVector == null) {
                clonedHash.put(verb, second.get(verb));
            } else {
                ArrayList arrayList = new ArrayList(cmdVector);
                arrayList.addAll((List) second.get(verb));
                clonedHash.put(verb, arrayList);
                ArrayList arrayList2 = arrayList;
            }
        }
        return clonedHash;
    }

    public void appendToMailcap(String mail_cap) {
        if (LogSupport.isLoggable()) {
            LogSupport.log("appendToMailcap: " + mail_cap);
        }
        try {
            parse(new StringReader(mail_cap));
        } catch (IOException e) {
        }
    }

    private void parse(Reader reader) throws IOException {
        BufferedReader buf_reader = new BufferedReader(reader);
        String continued = null;
        while (true) {
            String line = buf_reader.readLine();
            if (line != null) {
                String line2 = line.trim();
                try {
                    if (line2.charAt(0) != '#') {
                        if (line2.charAt(line2.length() - 1) == '\\') {
                            if (continued != null) {
                                continued = continued + line2.substring(0, line2.length() - 1);
                            } else {
                                continued = line2.substring(0, line2.length() - 1);
                            }
                        } else if (continued != null) {
                            try {
                                parseLine(continued + line2);
                            } catch (MailcapParseException e) {
                            }
                            continued = null;
                        } else {
                            try {
                                parseLine(line2);
                            } catch (MailcapParseException e2) {
                            }
                        }
                    }
                } catch (StringIndexOutOfBoundsException e3) {
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void parseLine(String mailcapEntry) throws MailcapParseException, IOException {
        int currentToken;
        MailcapTokenizer mailcapTokenizer = new MailcapTokenizer(mailcapEntry);
        mailcapTokenizer.setIsAutoquoting(false);
        if (LogSupport.isLoggable()) {
            LogSupport.log("parse: " + mailcapEntry);
        }
        int currentToken2 = mailcapTokenizer.nextToken();
        if (currentToken2 != 2) {
            reportParseError(2, currentToken2, mailcapTokenizer.getCurrentTokenValue());
        }
        String primaryType = mailcapTokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
        String subType = "*";
        int currentToken3 = mailcapTokenizer.nextToken();
        if (!(currentToken3 == 47 || currentToken3 == 59)) {
            reportParseError(47, 59, currentToken3, mailcapTokenizer.getCurrentTokenValue());
        }
        if (currentToken3 == 47) {
            int currentToken4 = mailcapTokenizer.nextToken();
            if (currentToken4 != 2) {
                reportParseError(2, currentToken4, mailcapTokenizer.getCurrentTokenValue());
            }
            subType = mailcapTokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
            currentToken3 = mailcapTokenizer.nextToken();
        }
        String mimeType = primaryType + "/" + subType;
        if (LogSupport.isLoggable()) {
            LogSupport.log("  Type: " + mimeType);
        }
        Map commands = new LinkedHashMap();
        if (currentToken3 != 59) {
            reportParseError(59, currentToken3, mailcapTokenizer.getCurrentTokenValue());
        }
        mailcapTokenizer.setIsAutoquoting(true);
        int currentToken5 = mailcapTokenizer.nextToken();
        mailcapTokenizer.setIsAutoquoting(false);
        if (!(currentToken5 == 2 || currentToken5 == 59)) {
            reportParseError(2, 59, currentToken5, mailcapTokenizer.getCurrentTokenValue());
        }
        if (currentToken5 == 2) {
            List v = (List) this.native_commands.get(mimeType);
            if (v == null) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(mailcapEntry);
                this.native_commands.put(mimeType, arrayList);
            } else {
                v.add(mailcapEntry);
            }
        }
        if (currentToken5 != 59) {
            currentToken5 = mailcapTokenizer.nextToken();
        }
        if (currentToken5 == 59) {
            boolean isFallback = false;
            do {
                int currentToken6 = mailcapTokenizer.nextToken();
                if (currentToken6 != 2) {
                    reportParseError(2, currentToken6, mailcapTokenizer.getCurrentTokenValue());
                }
                String paramName = mailcapTokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
                currentToken = mailcapTokenizer.nextToken();
                if (!(currentToken == 61 || currentToken == 59 || currentToken == 5)) {
                    reportParseError(61, 59, 5, currentToken, mailcapTokenizer.getCurrentTokenValue());
                }
                if (currentToken == 61) {
                    mailcapTokenizer.setIsAutoquoting(true);
                    int currentToken7 = mailcapTokenizer.nextToken();
                    mailcapTokenizer.setIsAutoquoting(false);
                    if (currentToken7 != 2) {
                        reportParseError(2, currentToken7, mailcapTokenizer.getCurrentTokenValue());
                    }
                    String paramValue = mailcapTokenizer.getCurrentTokenValue();
                    if (paramName.startsWith("x-java-")) {
                        String commandName = paramName.substring(7);
                        if (!commandName.equals("fallback-entry") || !paramValue.equalsIgnoreCase("true")) {
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("    Command: " + commandName + ", Class: " + paramValue);
                            }
                            List classes = (List) commands.get(commandName);
                            if (classes == null) {
                                classes = new ArrayList();
                                commands.put(commandName, classes);
                            }
                            if (addReverse) {
                                classes.add(0, paramValue);
                            } else {
                                classes.add(paramValue);
                            }
                        } else {
                            isFallback = true;
                        }
                    }
                    currentToken = mailcapTokenizer.nextToken();
                }
            } while (currentToken == 59);
            Map masterHash = isFallback ? this.fallback_hash : this.type_hash;
            Map curcommands = (Map) masterHash.get(mimeType);
            if (curcommands == null) {
                masterHash.put(mimeType, commands);
                return;
            }
            if (LogSupport.isLoggable()) {
                LogSupport.log("Merging commands for type " + mimeType);
            }
            for (String cmdName : curcommands.keySet()) {
                List ccv = (List) curcommands.get(cmdName);
                List<String> cv = (List) commands.get(cmdName);
                if (cv != null) {
                    for (String clazz : cv) {
                        if (!ccv.contains(clazz)) {
                            if (addReverse) {
                                ccv.add(0, clazz);
                            } else {
                                ccv.add(clazz);
                            }
                        }
                    }
                }
            }
            for (String cmdName2 : commands.keySet()) {
                if (!curcommands.containsKey(cmdName2)) {
                    curcommands.put(cmdName2, (List) commands.get(cmdName2));
                }
            }
        } else if (currentToken5 != 5) {
            reportParseError(5, 59, currentToken5, mailcapTokenizer.getCurrentTokenValue());
        }
    }

    protected static void reportParseError(int expectedToken, int actualToken, String actualTokenValue) throws MailcapParseException {
        throw new MailcapParseException("Encountered a " + MailcapTokenizer.nameForToken(actualToken) + " token (" + actualTokenValue + ") while expecting a " + MailcapTokenizer.nameForToken(expectedToken) + " token.");
    }

    protected static void reportParseError(int expectedToken, int otherExpectedToken, int actualToken, String actualTokenValue) throws MailcapParseException {
        throw new MailcapParseException("Encountered a " + MailcapTokenizer.nameForToken(actualToken) + " token (" + actualTokenValue + ") while expecting a " + MailcapTokenizer.nameForToken(expectedToken) + " or a " + MailcapTokenizer.nameForToken(otherExpectedToken) + " token.");
    }

    protected static void reportParseError(int expectedToken, int otherExpectedToken, int anotherExpectedToken, int actualToken, String actualTokenValue) throws MailcapParseException {
        if (LogSupport.isLoggable()) {
            LogSupport.log("PARSE ERROR: Encountered a " + MailcapTokenizer.nameForToken(actualToken) + " token (" + actualTokenValue + ") while expecting a " + MailcapTokenizer.nameForToken(expectedToken) + ", a " + MailcapTokenizer.nameForToken(otherExpectedToken) + ", or a " + MailcapTokenizer.nameForToken(anotherExpectedToken) + " token.");
        }
        throw new MailcapParseException("Encountered a " + MailcapTokenizer.nameForToken(actualToken) + " token (" + actualTokenValue + ") while expecting a " + MailcapTokenizer.nameForToken(expectedToken) + ", a " + MailcapTokenizer.nameForToken(otherExpectedToken) + ", or a " + MailcapTokenizer.nameForToken(anotherExpectedToken) + " token.");
    }
}
