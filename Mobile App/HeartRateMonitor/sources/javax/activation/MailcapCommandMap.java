package javax.activation;

import com.sun.activation.registries.LogSupport;
import com.sun.activation.registries.MailcapFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MailcapCommandMap extends CommandMap {
    private static final int PROG = 0;
    private static final String confDir;

    /* renamed from: DB */
    private MailcapFile[] f293DB;

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    static {
        /*
            r1 = 0
            javax.activation.MailcapCommandMap$1 r2 = new javax.activation.MailcapCommandMap$1     // Catch:{ Exception -> 0x0011 }
            r2.<init>()     // Catch:{ Exception -> 0x0011 }
            java.lang.Object r2 = java.security.AccessController.doPrivileged(r2)     // Catch:{ Exception -> 0x0011 }
            r0 = r2
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ Exception -> 0x0011 }
            r1 = r0
        L_0x000e:
            confDir = r1
            return
        L_0x0011:
            r2 = move-exception
            goto L_0x000e
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MailcapCommandMap.<clinit>():void");
    }

    public MailcapCommandMap() {
        MailcapFile mf;
        MailcapFile mf2;
        List dbv = new ArrayList(5);
        dbv.add((Object) null);
        LogSupport.log("MailcapCommandMap: load HOME");
        try {
            String user_home = System.getProperty("user.home");
            if (!(user_home == null || (mf2 = loadFile(user_home + File.separator + ".mailcap")) == null)) {
                dbv.add(mf2);
            }
        } catch (SecurityException e) {
        }
        LogSupport.log("MailcapCommandMap: load SYS");
        try {
            if (!(confDir == null || (mf = loadFile(confDir + "mailcap")) == null)) {
                dbv.add(mf);
            }
        } catch (SecurityException e2) {
        }
        LogSupport.log("MailcapCommandMap: load JAR");
        loadAllResources(dbv, "META-INF/mailcap");
        LogSupport.log("MailcapCommandMap: load DEF");
        MailcapFile mf3 = loadResource("/META-INF/mailcap.default");
        if (mf3 != null) {
            dbv.add(mf3);
        }
        this.f293DB = new MailcapFile[dbv.size()];
        this.f293DB = (MailcapFile[]) dbv.toArray(this.f293DB);
    }

    private MailcapFile loadResource(String name) {
        InputStream clis = null;
        try {
            clis = SecuritySupport.getResourceAsStream(getClass(), name);
            if (clis != null) {
                MailcapFile mf = new MailcapFile(clis);
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MailcapCommandMap: successfully loaded mailcap file: " + name);
                }
                if (clis == null) {
                    return mf;
                }
                try {
                    clis.close();
                    return mf;
                } catch (IOException e) {
                    return mf;
                }
            } else {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MailcapCommandMap: not loading mailcap file: " + name);
                }
                if (clis != null) {
                    try {
                        clis.close();
                    } catch (IOException e2) {
                    }
                }
                return null;
            }
        } catch (IOException e3) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: can't load " + name, e3);
            }
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e4) {
                }
            }
        } catch (SecurityException sex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: can't load " + name, sex);
            }
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e5) {
                }
            }
        } catch (Throwable th) {
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e6) {
                }
            }
            throw th;
        }
    }

    private void loadAllResources(List v, String name) {
        URL[] urls;
        boolean anyLoaded = false;
        try {
            ClassLoader cld = SecuritySupport.getContextClassLoader();
            if (cld == null) {
                cld = getClass().getClassLoader();
            }
            if (cld != null) {
                urls = SecuritySupport.getResources(cld, name);
            } else {
                urls = SecuritySupport.getSystemResources(name);
            }
            if (urls != null) {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MailcapCommandMap: getResources");
                }
                for (URL url : urls) {
                    InputStream clis = null;
                    if (LogSupport.isLoggable()) {
                        LogSupport.log("MailcapCommandMap: URL " + url);
                    }
                    try {
                        clis = SecuritySupport.openStream(url);
                        if (clis != null) {
                            v.add(new MailcapFile(clis));
                            anyLoaded = true;
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("MailcapCommandMap: successfully loaded mailcap file from URL: " + url);
                            }
                        } else if (LogSupport.isLoggable()) {
                            LogSupport.log("MailcapCommandMap: not loading mailcap file from URL: " + url);
                        }
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e) {
                            }
                        }
                    } catch (IOException ioex) {
                        if (LogSupport.isLoggable()) {
                            LogSupport.log("MailcapCommandMap: can't load " + url, ioex);
                        }
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e2) {
                            }
                        }
                    } catch (SecurityException sex) {
                        if (LogSupport.isLoggable()) {
                            LogSupport.log("MailcapCommandMap: can't load " + url, sex);
                        }
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e3) {
                            }
                        }
                    } catch (Throwable th) {
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                }
            }
        } catch (Exception ex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: can't load " + name, ex);
            }
        }
        if (!anyLoaded) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: !anyLoaded");
            }
            MailcapFile mf = loadResource("/" + name);
            if (mf != null) {
                v.add(mf);
            }
        }
    }

    private MailcapFile loadFile(String name) {
        try {
            return new MailcapFile(name);
        } catch (IOException e) {
            return null;
        }
    }

    public MailcapCommandMap(String fileName) throws IOException {
        this();
        if (LogSupport.isLoggable()) {
            LogSupport.log("MailcapCommandMap: load PROG from " + fileName);
        }
        if (this.f293DB[0] == null) {
            this.f293DB[0] = new MailcapFile(fileName);
        }
    }

    public MailcapCommandMap(InputStream is) {
        this();
        LogSupport.log("MailcapCommandMap: load PROG");
        if (this.f293DB[0] == null) {
            try {
                this.f293DB[0] = new MailcapFile(is);
            } catch (IOException e) {
            }
        }
    }

    public synchronized CommandInfo[] getPreferredCommands(String mimeType) {
        List cmdList;
        Map cmdMap;
        Map cmdMap2;
        cmdList = new ArrayList();
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.f293DB.length; i++) {
            if (!(this.f293DB[i] == null || (cmdMap2 = this.f293DB[i].getMailcapList(mimeType)) == null)) {
                appendPrefCmdsToList(cmdMap2, cmdList);
            }
        }
        for (int i2 = 0; i2 < this.f293DB.length; i2++) {
            if (!(this.f293DB[i2] == null || (cmdMap = this.f293DB[i2].getMailcapFallbackList(mimeType)) == null)) {
                appendPrefCmdsToList(cmdMap, cmdList);
            }
        }
        return (CommandInfo[]) cmdList.toArray(new CommandInfo[cmdList.size()]);
    }

    private void appendPrefCmdsToList(Map cmdHash, List cmdList) {
        for (String verb : cmdHash.keySet()) {
            if (!checkForVerb(cmdList, verb)) {
                cmdList.add(new CommandInfo(verb, (String) ((List) cmdHash.get(verb)).get(0)));
            }
        }
    }

    private boolean checkForVerb(List cmdList, String verb) {
        Iterator ee = cmdList.iterator();
        while (ee.hasNext()) {
            if (((CommandInfo) ee.next()).getCommandName().equals(verb)) {
                return true;
            }
        }
        return false;
    }

    public synchronized CommandInfo[] getAllCommands(String mimeType) {
        List cmdList;
        Map cmdMap;
        Map cmdMap2;
        cmdList = new ArrayList();
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.f293DB.length; i++) {
            if (!(this.f293DB[i] == null || (cmdMap2 = this.f293DB[i].getMailcapList(mimeType)) == null)) {
                appendCmdsToList(cmdMap2, cmdList);
            }
        }
        for (int i2 = 0; i2 < this.f293DB.length; i2++) {
            if (!(this.f293DB[i2] == null || (cmdMap = this.f293DB[i2].getMailcapFallbackList(mimeType)) == null)) {
                appendCmdsToList(cmdMap, cmdList);
            }
        }
        return (CommandInfo[]) cmdList.toArray(new CommandInfo[cmdList.size()]);
    }

    private void appendCmdsToList(Map typeHash, List cmdList) {
        for (String verb : typeHash.keySet()) {
            for (String cmd : (List) typeHash.get(verb)) {
                cmdList.add(new CommandInfo(verb, cmd));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003e, code lost:
        if (r2 >= r5.f293DB.length) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0044, code lost:
        if (r5.f293DB[r2] != null) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0046, code lost:
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0049, code lost:
        r1 = r5.f293DB[r2].getMailcapFallbackList(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0051, code lost:
        if (r1 == null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0053, code lost:
        r3 = (java.util.List) r1.get(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0059, code lost:
        if (r3 == null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x005b, code lost:
        r0 = (java.lang.String) r3.get(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0062, code lost:
        if (r0 == null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0064, code lost:
        r4 = new javax.activation.CommandInfo(r7, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006d, code lost:
        r4 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized javax.activation.CommandInfo getCommand(java.lang.String r6, java.lang.String r7) {
        /*
            r5 = this;
            monitor-enter(r5)
            if (r6 == 0) goto L_0x0009
            java.util.Locale r4 = java.util.Locale.ENGLISH     // Catch:{ all -> 0x006a }
            java.lang.String r6 = r6.toLowerCase(r4)     // Catch:{ all -> 0x006a }
        L_0x0009:
            r2 = 0
        L_0x000a:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f293DB     // Catch:{ all -> 0x006a }
            int r4 = r4.length     // Catch:{ all -> 0x006a }
            if (r2 >= r4) goto L_0x003a
            com.sun.activation.registries.MailcapFile[] r4 = r5.f293DB     // Catch:{ all -> 0x006a }
            r4 = r4[r2]     // Catch:{ all -> 0x006a }
            if (r4 != 0) goto L_0x0018
        L_0x0015:
            int r2 = r2 + 1
            goto L_0x000a
        L_0x0018:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f293DB     // Catch:{ all -> 0x006a }
            r4 = r4[r2]     // Catch:{ all -> 0x006a }
            java.util.Map r1 = r4.getMailcapList(r6)     // Catch:{ all -> 0x006a }
            if (r1 == 0) goto L_0x0015
            java.lang.Object r3 = r1.get(r7)     // Catch:{ all -> 0x006a }
            java.util.List r3 = (java.util.List) r3     // Catch:{ all -> 0x006a }
            if (r3 == 0) goto L_0x0015
            r4 = 0
            java.lang.Object r0 = r3.get(r4)     // Catch:{ all -> 0x006a }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ all -> 0x006a }
            if (r0 == 0) goto L_0x0015
            javax.activation.CommandInfo r4 = new javax.activation.CommandInfo     // Catch:{ all -> 0x006a }
            r4.<init>(r7, r0)     // Catch:{ all -> 0x006a }
        L_0x0038:
            monitor-exit(r5)
            return r4
        L_0x003a:
            r2 = 0
        L_0x003b:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f293DB     // Catch:{ all -> 0x006a }
            int r4 = r4.length     // Catch:{ all -> 0x006a }
            if (r2 >= r4) goto L_0x006d
            com.sun.activation.registries.MailcapFile[] r4 = r5.f293DB     // Catch:{ all -> 0x006a }
            r4 = r4[r2]     // Catch:{ all -> 0x006a }
            if (r4 != 0) goto L_0x0049
        L_0x0046:
            int r2 = r2 + 1
            goto L_0x003b
        L_0x0049:
            com.sun.activation.registries.MailcapFile[] r4 = r5.f293DB     // Catch:{ all -> 0x006a }
            r4 = r4[r2]     // Catch:{ all -> 0x006a }
            java.util.Map r1 = r4.getMailcapFallbackList(r6)     // Catch:{ all -> 0x006a }
            if (r1 == 0) goto L_0x0046
            java.lang.Object r3 = r1.get(r7)     // Catch:{ all -> 0x006a }
            java.util.List r3 = (java.util.List) r3     // Catch:{ all -> 0x006a }
            if (r3 == 0) goto L_0x0046
            r4 = 0
            java.lang.Object r0 = r3.get(r4)     // Catch:{ all -> 0x006a }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ all -> 0x006a }
            if (r0 == 0) goto L_0x0046
            javax.activation.CommandInfo r4 = new javax.activation.CommandInfo     // Catch:{ all -> 0x006a }
            r4.<init>(r7, r0)     // Catch:{ all -> 0x006a }
            goto L_0x0038
        L_0x006a:
            r4 = move-exception
            monitor-exit(r5)
            throw r4
        L_0x006d:
            r4 = 0
            goto L_0x0038
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MailcapCommandMap.getCommand(java.lang.String, java.lang.String):javax.activation.CommandInfo");
    }

    public synchronized void addMailcap(String mail_cap) {
        LogSupport.log("MailcapCommandMap: add to PROG");
        if (this.f293DB[0] == null) {
            this.f293DB[0] = new MailcapFile();
        }
        this.f293DB[0].appendToMailcap(mail_cap);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0073, code lost:
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0077, code lost:
        if (r2 >= r7.f293DB.length) goto L_0x00c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007d, code lost:
        if (r7.f293DB[r2] != null) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007f, code lost:
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0086, code lost:
        if (com.sun.activation.registries.LogSupport.isLoggable() == false) goto L_0x009e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0088, code lost:
        com.sun.activation.registries.LogSupport.log("  search fallback DB #" + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x009e, code lost:
        r0 = r7.f293DB[r2].getMailcapFallbackList(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a6, code lost:
        if (r0 == null) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a8, code lost:
        r4 = (java.util.List) r0.get("content-handler");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00b0, code lost:
        if (r4 == null) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00b2, code lost:
        r1 = getDataContentHandler((java.lang.String) r4.get(0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00bd, code lost:
        if (r1 == null) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c0, code lost:
        r1 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized javax.activation.DataContentHandler createDataContentHandler(java.lang.String r8) {
        /*
            r7 = this;
            monitor-enter(r7)
            boolean r5 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x00c2 }
            if (r5 == 0) goto L_0x001d
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c2 }
            r5.<init>()     // Catch:{ all -> 0x00c2 }
            java.lang.String r6 = "MailcapCommandMap: createDataContentHandler for "
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x00c2 }
            java.lang.StringBuilder r5 = r5.append(r8)     // Catch:{ all -> 0x00c2 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00c2 }
            com.sun.activation.registries.LogSupport.log(r5)     // Catch:{ all -> 0x00c2 }
        L_0x001d:
            if (r8 == 0) goto L_0x0025
            java.util.Locale r5 = java.util.Locale.ENGLISH     // Catch:{ all -> 0x00c2 }
            java.lang.String r8 = r8.toLowerCase(r5)     // Catch:{ all -> 0x00c2 }
        L_0x0025:
            r2 = 0
        L_0x0026:
            com.sun.activation.registries.MailcapFile[] r5 = r7.f293DB     // Catch:{ all -> 0x00c2 }
            int r5 = r5.length     // Catch:{ all -> 0x00c2 }
            if (r2 >= r5) goto L_0x0073
            com.sun.activation.registries.MailcapFile[] r5 = r7.f293DB     // Catch:{ all -> 0x00c2 }
            r5 = r5[r2]     // Catch:{ all -> 0x00c2 }
            if (r5 != 0) goto L_0x0034
        L_0x0031:
            int r2 = r2 + 1
            goto L_0x0026
        L_0x0034:
            boolean r5 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x00c2 }
            if (r5 == 0) goto L_0x0050
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c2 }
            r5.<init>()     // Catch:{ all -> 0x00c2 }
            java.lang.String r6 = "  search DB #"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x00c2 }
            java.lang.StringBuilder r5 = r5.append(r2)     // Catch:{ all -> 0x00c2 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00c2 }
            com.sun.activation.registries.LogSupport.log(r5)     // Catch:{ all -> 0x00c2 }
        L_0x0050:
            com.sun.activation.registries.MailcapFile[] r5 = r7.f293DB     // Catch:{ all -> 0x00c2 }
            r5 = r5[r2]     // Catch:{ all -> 0x00c2 }
            java.util.Map r0 = r5.getMailcapList(r8)     // Catch:{ all -> 0x00c2 }
            if (r0 == 0) goto L_0x0031
            java.lang.String r5 = "content-handler"
            java.lang.Object r4 = r0.get(r5)     // Catch:{ all -> 0x00c2 }
            java.util.List r4 = (java.util.List) r4     // Catch:{ all -> 0x00c2 }
            if (r4 == 0) goto L_0x0031
            r5 = 0
            java.lang.Object r3 = r4.get(r5)     // Catch:{ all -> 0x00c2 }
            java.lang.String r3 = (java.lang.String) r3     // Catch:{ all -> 0x00c2 }
            javax.activation.DataContentHandler r1 = r7.getDataContentHandler(r3)     // Catch:{ all -> 0x00c2 }
            if (r1 == 0) goto L_0x0031
        L_0x0071:
            monitor-exit(r7)
            return r1
        L_0x0073:
            r2 = 0
        L_0x0074:
            com.sun.activation.registries.MailcapFile[] r5 = r7.f293DB     // Catch:{ all -> 0x00c2 }
            int r5 = r5.length     // Catch:{ all -> 0x00c2 }
            if (r2 >= r5) goto L_0x00c0
            com.sun.activation.registries.MailcapFile[] r5 = r7.f293DB     // Catch:{ all -> 0x00c2 }
            r5 = r5[r2]     // Catch:{ all -> 0x00c2 }
            if (r5 != 0) goto L_0x0082
        L_0x007f:
            int r2 = r2 + 1
            goto L_0x0074
        L_0x0082:
            boolean r5 = com.sun.activation.registries.LogSupport.isLoggable()     // Catch:{ all -> 0x00c2 }
            if (r5 == 0) goto L_0x009e
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c2 }
            r5.<init>()     // Catch:{ all -> 0x00c2 }
            java.lang.String r6 = "  search fallback DB #"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x00c2 }
            java.lang.StringBuilder r5 = r5.append(r2)     // Catch:{ all -> 0x00c2 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00c2 }
            com.sun.activation.registries.LogSupport.log(r5)     // Catch:{ all -> 0x00c2 }
        L_0x009e:
            com.sun.activation.registries.MailcapFile[] r5 = r7.f293DB     // Catch:{ all -> 0x00c2 }
            r5 = r5[r2]     // Catch:{ all -> 0x00c2 }
            java.util.Map r0 = r5.getMailcapFallbackList(r8)     // Catch:{ all -> 0x00c2 }
            if (r0 == 0) goto L_0x007f
            java.lang.String r5 = "content-handler"
            java.lang.Object r4 = r0.get(r5)     // Catch:{ all -> 0x00c2 }
            java.util.List r4 = (java.util.List) r4     // Catch:{ all -> 0x00c2 }
            if (r4 == 0) goto L_0x007f
            r5 = 0
            java.lang.Object r3 = r4.get(r5)     // Catch:{ all -> 0x00c2 }
            java.lang.String r3 = (java.lang.String) r3     // Catch:{ all -> 0x00c2 }
            javax.activation.DataContentHandler r1 = r7.getDataContentHandler(r3)     // Catch:{ all -> 0x00c2 }
            if (r1 == 0) goto L_0x007f
            goto L_0x0071
        L_0x00c0:
            r1 = 0
            goto L_0x0071
        L_0x00c2:
            r5 = move-exception
            monitor-exit(r7)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MailcapCommandMap.createDataContentHandler(java.lang.String):javax.activation.DataContentHandler");
    }

    private DataContentHandler getDataContentHandler(String name) {
        Class cl;
        if (LogSupport.isLoggable()) {
            LogSupport.log("    got content-handler");
        }
        if (LogSupport.isLoggable()) {
            LogSupport.log("      class " + name);
        }
        try {
            ClassLoader cld = SecuritySupport.getContextClassLoader();
            if (cld == null) {
                cld = getClass().getClassLoader();
            }
            try {
                cl = cld.loadClass(name);
            } catch (Exception e) {
                cl = Class.forName(name);
            }
            if (cl != null) {
                return (DataContentHandler) cl.newInstance();
            }
        } catch (IllegalAccessException e2) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("Can't load DCH " + name, e2);
            }
        } catch (ClassNotFoundException e3) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("Can't load DCH " + name, e3);
            }
        } catch (InstantiationException e4) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("Can't load DCH " + name, e4);
            }
        }
        return null;
    }

    public synchronized String[] getMimeTypes() {
        List mtList;
        String[] ts;
        mtList = new ArrayList();
        for (int i = 0; i < this.f293DB.length; i++) {
            if (!(this.f293DB[i] == null || (ts = this.f293DB[i].getMimeTypes()) == null)) {
                for (int j = 0; j < ts.length; j++) {
                    if (!mtList.contains(ts[j])) {
                        mtList.add(ts[j]);
                    }
                }
            }
        }
        return (String[]) mtList.toArray(new String[mtList.size()]);
    }

    public synchronized String[] getNativeCommands(String mimeType) {
        List cmdList;
        String[] cmds;
        cmdList = new ArrayList();
        if (mimeType != null) {
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.f293DB.length; i++) {
            if (!(this.f293DB[i] == null || (cmds = this.f293DB[i].getNativeCommands(mimeType)) == null)) {
                for (int j = 0; j < cmds.length; j++) {
                    if (!cmdList.contains(cmds[j])) {
                        cmdList.add(cmds[j]);
                    }
                }
            }
        }
        return (String[]) cmdList.toArray(new String[cmdList.size()]);
    }
}
