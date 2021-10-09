package javax.activation;

import com.sun.activation.registries.LogSupport;
import com.sun.activation.registries.MimeTypeFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

public class MimetypesFileTypeMap extends FileTypeMap {
    private static final int PROG = 0;
    private static final String confDir;
    private static final String defaultType = "application/octet-stream";

    /* renamed from: DB */
    private MimeTypeFile[] f294DB;

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    static {
        /*
            r1 = 0
            javax.activation.MimetypesFileTypeMap$1 r2 = new javax.activation.MimetypesFileTypeMap$1     // Catch:{ Exception -> 0x0011 }
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
        throw new UnsupportedOperationException("Method not decompiled: javax.activation.MimetypesFileTypeMap.<clinit>():void");
    }

    public MimetypesFileTypeMap() {
        MimeTypeFile mf;
        MimeTypeFile mf2;
        Vector dbv = new Vector(5);
        dbv.addElement((Object) null);
        LogSupport.log("MimetypesFileTypeMap: load HOME");
        try {
            String user_home = System.getProperty("user.home");
            if (!(user_home == null || (mf2 = loadFile(user_home + File.separator + ".mime.types")) == null)) {
                dbv.addElement(mf2);
            }
        } catch (SecurityException e) {
        }
        LogSupport.log("MimetypesFileTypeMap: load SYS");
        try {
            if (!(confDir == null || (mf = loadFile(confDir + "mime.types")) == null)) {
                dbv.addElement(mf);
            }
        } catch (SecurityException e2) {
        }
        LogSupport.log("MimetypesFileTypeMap: load JAR");
        loadAllResources(dbv, "META-INF/mime.types");
        LogSupport.log("MimetypesFileTypeMap: load DEF");
        MimeTypeFile mf3 = loadResource("/META-INF/mimetypes.default");
        if (mf3 != null) {
            dbv.addElement(mf3);
        }
        this.f294DB = new MimeTypeFile[dbv.size()];
        dbv.copyInto(this.f294DB);
    }

    private MimeTypeFile loadResource(String name) {
        InputStream clis = null;
        try {
            clis = SecuritySupport.getResourceAsStream(getClass(), name);
            if (clis != null) {
                MimeTypeFile mf = new MimeTypeFile(clis);
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MimetypesFileTypeMap: successfully loaded mime types file: " + name);
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
                    LogSupport.log("MimetypesFileTypeMap: not loading mime types file: " + name);
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
                LogSupport.log("MimetypesFileTypeMap: can't load " + name, e3);
            }
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e4) {
                }
            }
        } catch (SecurityException sex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MimetypesFileTypeMap: can't load " + name, sex);
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

    private void loadAllResources(Vector v, String name) {
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
                    LogSupport.log("MimetypesFileTypeMap: getResources");
                }
                for (URL url : urls) {
                    InputStream clis = null;
                    if (LogSupport.isLoggable()) {
                        LogSupport.log("MimetypesFileTypeMap: URL " + url);
                    }
                    try {
                        clis = SecuritySupport.openStream(url);
                        if (clis != null) {
                            v.addElement(new MimeTypeFile(clis));
                            anyLoaded = true;
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("MimetypesFileTypeMap: successfully loaded mime types from URL: " + url);
                            }
                        } else if (LogSupport.isLoggable()) {
                            LogSupport.log("MimetypesFileTypeMap: not loading mime types from URL: " + url);
                        }
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e) {
                            }
                        }
                    } catch (IOException ioex) {
                        if (LogSupport.isLoggable()) {
                            LogSupport.log("MimetypesFileTypeMap: can't load " + url, ioex);
                        }
                        if (clis != null) {
                            try {
                                clis.close();
                            } catch (IOException e2) {
                            }
                        }
                    } catch (SecurityException sex) {
                        if (LogSupport.isLoggable()) {
                            LogSupport.log("MimetypesFileTypeMap: can't load " + url, sex);
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
                LogSupport.log("MimetypesFileTypeMap: can't load " + name, ex);
            }
        }
        if (!anyLoaded) {
            LogSupport.log("MimetypesFileTypeMap: !anyLoaded");
            MimeTypeFile mf = loadResource("/" + name);
            if (mf != null) {
                v.addElement(mf);
            }
        }
    }

    private MimeTypeFile loadFile(String name) {
        try {
            return new MimeTypeFile(name);
        } catch (IOException e) {
            return null;
        }
    }

    public MimetypesFileTypeMap(String mimeTypeFileName) throws IOException {
        this();
        this.f294DB[0] = new MimeTypeFile(mimeTypeFileName);
    }

    public MimetypesFileTypeMap(InputStream is) {
        this();
        try {
            this.f294DB[0] = new MimeTypeFile(is);
        } catch (IOException e) {
        }
    }

    public synchronized void addMimeTypes(String mime_types) {
        if (this.f294DB[0] == null) {
            this.f294DB[0] = new MimeTypeFile();
        }
        this.f294DB[0].appendToRegistry(mime_types);
    }

    public String getContentType(File f) {
        return getContentType(f.getName());
    }

    public synchronized String getContentType(String filename) {
        String result;
        int dot_pos = filename.lastIndexOf(".");
        if (dot_pos >= 0) {
            String file_ext = filename.substring(dot_pos + 1);
            if (file_ext.length() != 0) {
                int i = 0;
                while (true) {
                    if (i >= this.f294DB.length) {
                        result = "application/octet-stream";
                        break;
                    } else if (this.f294DB[i] != null && (result = this.f294DB[i].getMIMETypeString(file_ext)) != null) {
                        break;
                    } else {
                        i++;
                    }
                }
            } else {
                result = "application/octet-stream";
            }
        } else {
            result = "application/octet-stream";
        }
        return result;
    }
}
