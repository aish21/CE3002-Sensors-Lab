package com.sun.mail.util;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.mail.internet.MimePart;

public class MimeUtil {
    private static final Method cleanContentType;

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003e, code lost:
        cleanContentType = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0040, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        return;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0035 A[ExcHandler: NoSuchMethodException (e java.lang.NoSuchMethodException), Splitter:B:1:0x0003] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0039 A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:1:0x0003] */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003d A[ExcHandler:  FINALLY, Splitter:B:1:0x0003] */
    static {
        /*
            r3 = 0
            java.lang.String r4 = "mail.mime.contenttypehandler"
            java.lang.String r2 = java.lang.System.getProperty(r4)     // Catch:{ ClassNotFoundException -> 0x0031, NoSuchMethodException -> 0x0035, RuntimeException -> 0x0039, all -> 0x003d }
            if (r2 == 0) goto L_0x002e
            java.lang.ClassLoader r0 = getContextClassLoader()     // Catch:{ ClassNotFoundException -> 0x0031, NoSuchMethodException -> 0x0035, RuntimeException -> 0x0039, all -> 0x003d }
            r1 = 0
            if (r0 == 0) goto L_0x0015
            r4 = 0
            java.lang.Class r1 = java.lang.Class.forName(r2, r4, r0)     // Catch:{ ClassNotFoundException -> 0x0041, NoSuchMethodException -> 0x0035, RuntimeException -> 0x0039, all -> 0x003d }
        L_0x0015:
            if (r1 != 0) goto L_0x001b
            java.lang.Class r1 = java.lang.Class.forName(r2)     // Catch:{ ClassNotFoundException -> 0x0031, NoSuchMethodException -> 0x0035, RuntimeException -> 0x0039, all -> 0x003d }
        L_0x001b:
            java.lang.String r4 = "cleanContentType"
            r5 = 2
            java.lang.Class[] r5 = new java.lang.Class[r5]     // Catch:{ ClassNotFoundException -> 0x0031, NoSuchMethodException -> 0x0035, RuntimeException -> 0x0039, all -> 0x003d }
            r6 = 0
            java.lang.Class<javax.mail.internet.MimePart> r7 = javax.mail.internet.MimePart.class
            r5[r6] = r7     // Catch:{ ClassNotFoundException -> 0x0031, NoSuchMethodException -> 0x0035, RuntimeException -> 0x0039, all -> 0x003d }
            r6 = 1
            java.lang.Class<java.lang.String> r7 = java.lang.String.class
            r5[r6] = r7     // Catch:{ ClassNotFoundException -> 0x0031, NoSuchMethodException -> 0x0035, RuntimeException -> 0x0039, all -> 0x003d }
            java.lang.reflect.Method r3 = r1.getMethod(r4, r5)     // Catch:{ ClassNotFoundException -> 0x0031, NoSuchMethodException -> 0x0035, RuntimeException -> 0x0039, all -> 0x003d }
        L_0x002e:
            cleanContentType = r3
        L_0x0030:
            return
        L_0x0031:
            r4 = move-exception
            cleanContentType = r3
            goto L_0x0030
        L_0x0035:
            r4 = move-exception
            cleanContentType = r3
            goto L_0x0030
        L_0x0039:
            r4 = move-exception
            cleanContentType = r3
            goto L_0x0030
        L_0x003d:
            r4 = move-exception
            cleanContentType = r3
            throw r4
        L_0x0041:
            r4 = move-exception
            goto L_0x0015
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.MimeUtil.<clinit>():void");
    }

    private MimeUtil() {
    }

    public static String cleanContentType(MimePart mp, String contentType) {
        if (cleanContentType == null) {
            return contentType;
        }
        try {
            return (String) cleanContentType.invoke((Object) null, new Object[]{mp, contentType});
        } catch (Exception e) {
            return contentType;
        }
    }

    private static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                try {
                    return Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                    return null;
                }
            }
        });
    }
}
