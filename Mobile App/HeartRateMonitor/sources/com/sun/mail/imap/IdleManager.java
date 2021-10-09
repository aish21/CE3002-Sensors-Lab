package com.sun.mail.imap;

import com.sun.mail.util.MailLogger;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;

public class IdleManager {
    private volatile boolean die = false;

    /* renamed from: es */
    private Executor f273es;
    /* access modifiers changed from: private */
    public MailLogger logger;
    /* access modifiers changed from: private */
    public volatile boolean running;
    private Selector selector;
    private Queue<IMAPFolder> toAbort = new ConcurrentLinkedQueue();
    private Queue<IMAPFolder> toWatch = new ConcurrentLinkedQueue();

    public IdleManager(Session session, Executor es) throws IOException {
        this.f273es = es;
        this.logger = new MailLogger(getClass(), "DEBUG IMAP", session.getDebug(), session.getDebugOut());
        this.selector = Selector.open();
        es.execute(new Runnable() {
            public void run() {
                String str;
                IdleManager.this.logger.fine("IdleManager select starting");
                try {
                    boolean unused = IdleManager.this.running = true;
                    IdleManager.this.select();
                } finally {
                    boolean unused2 = IdleManager.this.running = false;
                    str = "IdleManager select terminating";
                    IdleManager.this.logger.fine(str);
                }
            }
        });
    }

    public boolean isRunning() {
        return this.running;
    }

    public void watch(Folder folder) throws MessagingException {
        if (this.die) {
            throw new MessagingException("IdleManager is not running");
        } else if (!(folder instanceof IMAPFolder)) {
            throw new MessagingException("Can only watch IMAP folders");
        } else {
            IMAPFolder ifolder = (IMAPFolder) folder;
            if (ifolder.getChannel() != null) {
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.log(Level.FINEST, "IdleManager watching {0}", (Object) folderName(ifolder));
                }
                int tries = 0;
                while (!ifolder.startIdle(this)) {
                    if (this.logger.isLoggable(Level.FINEST)) {
                        this.logger.log(Level.FINEST, "IdleManager.watch startIdle failed for {0}", (Object) folderName(ifolder));
                    }
                    tries++;
                }
                if (this.logger.isLoggable(Level.FINEST)) {
                    if (tries > 0) {
                        this.logger.log(Level.FINEST, "IdleManager.watch startIdle succeeded for {0} after " + tries + " tries", (Object) folderName(ifolder));
                    } else {
                        this.logger.log(Level.FINEST, "IdleManager.watch startIdle succeeded for {0}", (Object) folderName(ifolder));
                    }
                }
                synchronized (this) {
                    this.toWatch.add(ifolder);
                    this.selector.wakeup();
                }
            } else if (folder.isOpen()) {
                throw new MessagingException("Folder is not using SocketChannels");
            } else {
                throw new MessagingException("Folder is not open");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void requestAbort(IMAPFolder folder) {
        this.toAbort.add(folder);
        this.selector.wakeup();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0008 A[Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1, all -> 0x0100 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void select() {
        /*
            r8 = this;
            r7 = 1
            r3 = 0
            r8.die = r3
        L_0x0004:
            boolean r3 = r8.die     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            if (r3 != 0) goto L_0x003d
            r8.watchAll()     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            com.sun.mail.util.MailLogger r3 = r8.logger     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            java.lang.String r4 = "IdleManager waiting..."
            r3.finest(r4)     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            java.nio.channels.Selector r3 = r8.selector     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            int r2 = r3.select()     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            com.sun.mail.util.MailLogger r3 = r8.logger     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            java.util.logging.Level r4 = java.util.logging.Level.FINEST     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            boolean r3 = r3.isLoggable(r4)     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            if (r3 == 0) goto L_0x002f
            com.sun.mail.util.MailLogger r3 = r8.logger     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            java.util.logging.Level r4 = java.util.logging.Level.FINEST     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            java.lang.String r5 = "IdleManager selected {0} channels"
            java.lang.Integer r6 = java.lang.Integer.valueOf(r2)     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            r3.log((java.util.logging.Level) r4, (java.lang.String) r5, (java.lang.Object) r6)     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
        L_0x002f:
            boolean r3 = r8.die     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            if (r3 != 0) goto L_0x003d
            java.lang.Thread r3 = java.lang.Thread.currentThread()     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            boolean r3 = r3.isInterrupted()     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            if (r3 == 0) goto L_0x0056
        L_0x003d:
            r8.die = r7
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.lang.String r4 = "IdleManager unwatchAll"
            r3.finest(r4)
            r8.unwatchAll()     // Catch:{ IOException -> 0x006a }
            java.nio.channels.Selector r3 = r8.selector     // Catch:{ IOException -> 0x006a }
            r3.close()     // Catch:{ IOException -> 0x006a }
        L_0x004e:
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.lang.String r4 = "IdleManager exiting"
            r3.fine(r4)
        L_0x0055:
            return
        L_0x0056:
            r8.processKeys()     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            java.nio.channels.Selector r3 = r8.selector     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            int r3 = r3.selectNow()     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            if (r3 > 0) goto L_0x0056
            java.util.Queue<com.sun.mail.imap.IMAPFolder> r3 = r8.toAbort     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            boolean r3 = r3.isEmpty()     // Catch:{ InterruptedIOException -> 0x0075, IOException -> 0x00a3, Exception -> 0x00d1 }
            if (r3 == 0) goto L_0x0056
            goto L_0x0004
        L_0x006a:
            r1 = move-exception
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.util.logging.Level r4 = java.util.logging.Level.FINEST
            java.lang.String r5 = "IdleManager unwatch exception"
            r3.log((java.util.logging.Level) r4, (java.lang.String) r5, (java.lang.Throwable) r1)
            goto L_0x004e
        L_0x0075:
            r0 = move-exception
            com.sun.mail.util.MailLogger r3 = r8.logger     // Catch:{ all -> 0x0100 }
            java.util.logging.Level r4 = java.util.logging.Level.FINEST     // Catch:{ all -> 0x0100 }
            java.lang.String r5 = "IdleManager interrupted"
            r3.log((java.util.logging.Level) r4, (java.lang.String) r5, (java.lang.Throwable) r0)     // Catch:{ all -> 0x0100 }
            r8.die = r7
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.lang.String r4 = "IdleManager unwatchAll"
            r3.finest(r4)
            r8.unwatchAll()     // Catch:{ IOException -> 0x0098 }
            java.nio.channels.Selector r3 = r8.selector     // Catch:{ IOException -> 0x0098 }
            r3.close()     // Catch:{ IOException -> 0x0098 }
        L_0x0090:
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.lang.String r4 = "IdleManager exiting"
            r3.fine(r4)
            goto L_0x0055
        L_0x0098:
            r1 = move-exception
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.util.logging.Level r4 = java.util.logging.Level.FINEST
            java.lang.String r5 = "IdleManager unwatch exception"
            r3.log((java.util.logging.Level) r4, (java.lang.String) r5, (java.lang.Throwable) r1)
            goto L_0x0090
        L_0x00a3:
            r0 = move-exception
            com.sun.mail.util.MailLogger r3 = r8.logger     // Catch:{ all -> 0x0100 }
            java.util.logging.Level r4 = java.util.logging.Level.FINEST     // Catch:{ all -> 0x0100 }
            java.lang.String r5 = "IdleManager got I/O exception"
            r3.log((java.util.logging.Level) r4, (java.lang.String) r5, (java.lang.Throwable) r0)     // Catch:{ all -> 0x0100 }
            r8.die = r7
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.lang.String r4 = "IdleManager unwatchAll"
            r3.finest(r4)
            r8.unwatchAll()     // Catch:{ IOException -> 0x00c6 }
            java.nio.channels.Selector r3 = r8.selector     // Catch:{ IOException -> 0x00c6 }
            r3.close()     // Catch:{ IOException -> 0x00c6 }
        L_0x00be:
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.lang.String r4 = "IdleManager exiting"
            r3.fine(r4)
            goto L_0x0055
        L_0x00c6:
            r1 = move-exception
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.util.logging.Level r4 = java.util.logging.Level.FINEST
            java.lang.String r5 = "IdleManager unwatch exception"
            r3.log((java.util.logging.Level) r4, (java.lang.String) r5, (java.lang.Throwable) r1)
            goto L_0x00be
        L_0x00d1:
            r0 = move-exception
            com.sun.mail.util.MailLogger r3 = r8.logger     // Catch:{ all -> 0x0100 }
            java.util.logging.Level r4 = java.util.logging.Level.FINEST     // Catch:{ all -> 0x0100 }
            java.lang.String r5 = "IdleManager got exception"
            r3.log((java.util.logging.Level) r4, (java.lang.String) r5, (java.lang.Throwable) r0)     // Catch:{ all -> 0x0100 }
            r8.die = r7
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.lang.String r4 = "IdleManager unwatchAll"
            r3.finest(r4)
            r8.unwatchAll()     // Catch:{ IOException -> 0x00f5 }
            java.nio.channels.Selector r3 = r8.selector     // Catch:{ IOException -> 0x00f5 }
            r3.close()     // Catch:{ IOException -> 0x00f5 }
        L_0x00ec:
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.lang.String r4 = "IdleManager exiting"
            r3.fine(r4)
            goto L_0x0055
        L_0x00f5:
            r1 = move-exception
            com.sun.mail.util.MailLogger r3 = r8.logger
            java.util.logging.Level r4 = java.util.logging.Level.FINEST
            java.lang.String r5 = "IdleManager unwatch exception"
            r3.log((java.util.logging.Level) r4, (java.lang.String) r5, (java.lang.Throwable) r1)
            goto L_0x00ec
        L_0x0100:
            r3 = move-exception
            r8.die = r7
            com.sun.mail.util.MailLogger r4 = r8.logger
            java.lang.String r5 = "IdleManager unwatchAll"
            r4.finest(r5)
            r8.unwatchAll()     // Catch:{ IOException -> 0x011a }
            java.nio.channels.Selector r4 = r8.selector     // Catch:{ IOException -> 0x011a }
            r4.close()     // Catch:{ IOException -> 0x011a }
        L_0x0112:
            com.sun.mail.util.MailLogger r4 = r8.logger
            java.lang.String r5 = "IdleManager exiting"
            r4.fine(r5)
            throw r3
        L_0x011a:
            r1 = move-exception
            com.sun.mail.util.MailLogger r4 = r8.logger
            java.util.logging.Level r5 = java.util.logging.Level.FINEST
            java.lang.String r6 = "IdleManager unwatch exception"
            r4.log((java.util.logging.Level) r5, (java.lang.String) r6, (java.lang.Throwable) r1)
            goto L_0x0112
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IdleManager.select():void");
    }

    private void watchAll() {
        while (true) {
            IMAPFolder folder = this.toWatch.poll();
            if (folder != null) {
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.log(Level.FINEST, "IdleManager adding {0} to selector", (Object) folderName(folder));
                }
                try {
                    SocketChannel sc = folder.getChannel();
                    if (sc != null) {
                        sc.configureBlocking(false);
                        sc.register(this.selector, 1, folder);
                    }
                } catch (IOException ex) {
                    this.logger.log(Level.FINEST, "IdleManager can't register folder", (Throwable) ex);
                } catch (CancelledKeyException ex2) {
                    this.logger.log(Level.FINEST, "IdleManager can't register folder", (Throwable) ex2);
                }
            } else {
                return;
            }
        }
    }

    private void processKeys() throws IOException {
        Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
        while (it.hasNext()) {
            SelectionKey sk = it.next();
            it.remove();
            sk.cancel();
            IMAPFolder folder = (IMAPFolder) sk.attachment();
            if (this.logger.isLoggable(Level.FINEST)) {
                this.logger.log(Level.FINEST, "IdleManager selected folder: {0}", (Object) folderName(folder));
            }
            sk.channel().configureBlocking(true);
            try {
                if (folder.handleIdle(false)) {
                    if (this.logger.isLoggable(Level.FINEST)) {
                        this.logger.log(Level.FINEST, "IdleManager continue watching folder {0}", (Object) folderName(folder));
                    }
                    this.toWatch.add(folder);
                } else if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.log(Level.FINEST, "IdleManager done watching folder {0}", (Object) folderName(folder));
                }
            } catch (MessagingException ex) {
                this.logger.log(Level.FINEST, "IdleManager got exception for folder: " + folderName(folder), (Throwable) ex);
            }
        }
        while (true) {
            IMAPFolder folder2 = this.toAbort.poll();
            if (folder2 != null) {
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.log(Level.FINEST, "IdleManager aborting IDLE for folder: {0}", (Object) folderName(folder2));
                }
                SocketChannel sc = folder2.getChannel();
                if (sc != null) {
                    SelectionKey sk2 = sc.keyFor(this.selector);
                    if (sk2 != null) {
                        sk2.cancel();
                    }
                    sc.configureBlocking(true);
                    Socket sock = sc.socket();
                    if (sock == null || sock.getSoTimeout() <= 0) {
                        folder2.idleAbort();
                        this.toWatch.add(folder2);
                    } else {
                        this.logger.finest("IdleManager requesting DONE with timeout");
                        this.toWatch.remove(folder2);
                        final IMAPFolder folder0 = folder2;
                        this.f273es.execute(new Runnable() {
                            public void run() {
                                folder0.idleAbortWait();
                            }
                        });
                    }
                }
            } else {
                return;
            }
        }
    }

    private void unwatchAll() {
        for (SelectionKey sk : this.selector.keys()) {
            sk.cancel();
            IMAPFolder folder = (IMAPFolder) sk.attachment();
            if (this.logger.isLoggable(Level.FINEST)) {
                this.logger.log(Level.FINEST, "IdleManager no longer watching folder: {0}", (Object) folderName(folder));
            }
            try {
                sk.channel().configureBlocking(true);
                folder.idleAbortWait();
            } catch (IOException ex) {
                this.logger.log(Level.FINEST, "IdleManager exception while aborting idle for folder: " + folderName(folder), (Throwable) ex);
            }
        }
        while (true) {
            IMAPFolder folder2 = this.toWatch.poll();
            if (folder2 != null) {
                if (this.logger.isLoggable(Level.FINEST)) {
                    this.logger.log(Level.FINEST, "IdleManager aborting IDLE for unwatched folder: {0}", (Object) folderName(folder2));
                }
                SocketChannel sc = folder2.getChannel();
                if (sc != null) {
                    try {
                        sc.configureBlocking(true);
                        folder2.idleAbortWait();
                    } catch (IOException ex2) {
                        this.logger.log(Level.FINEST, "IdleManager exception while aborting idle for folder: " + folderName(folder2), (Throwable) ex2);
                    }
                }
            } else {
                return;
            }
        }
    }

    public synchronized void stop() {
        this.die = true;
        this.logger.fine("IdleManager stopping");
        this.selector.wakeup();
    }

    private static String folderName(Folder folder) {
        try {
            return folder.getURLName().toString();
        } catch (MessagingException e) {
            return folder.getStore().toString() + "/" + folder.toString();
        }
    }
}
