package com.sun.mail.imap;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;
import gnu.kawa.lispexpr.LispReader;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;

public class DefaultFolder extends IMAPFolder {
    protected DefaultFolder(IMAPStore store) {
        super("", LispReader.TOKEN_ESCAPE_CHAR, store, (Boolean) null);
        this.exists = true;
        this.type = 2;
    }

    public synchronized String getName() {
        return this.fullName;
    }

    public Folder getParent() {
        return null;
    }

    public synchronized Folder[] list(final String pattern) throws MessagingException {
        Folder[] folderArr;
        ListInfo[] li = (ListInfo[]) doCommand(new IMAPFolder.ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.list("", pattern);
            }
        });
        if (li == null) {
            folderArr = new Folder[0];
        } else {
            folderArr = new IMAPFolder[li.length];
            for (int i = 0; i < folderArr.length; i++) {
                folderArr[i] = ((IMAPStore) this.store).newIMAPFolder(li[i]);
            }
        }
        return folderArr;
    }

    public synchronized Folder[] listSubscribed(final String pattern) throws MessagingException {
        Folder[] folderArr;
        ListInfo[] li = (ListInfo[]) doCommand(new IMAPFolder.ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.lsub("", pattern);
            }
        });
        if (li == null) {
            folderArr = new Folder[0];
        } else {
            folderArr = new IMAPFolder[li.length];
            for (int i = 0; i < folderArr.length; i++) {
                folderArr[i] = ((IMAPStore) this.store).newIMAPFolder(li[i]);
            }
        }
        return folderArr;
    }

    public boolean hasNewMessages() throws MessagingException {
        return false;
    }

    public Folder getFolder(String name) throws MessagingException {
        return ((IMAPStore) this.store).newIMAPFolder(name, LispReader.TOKEN_ESCAPE_CHAR);
    }

    public boolean delete(boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("Cannot delete Default Folder");
    }

    public boolean renameTo(Folder f) throws MessagingException {
        throw new MethodNotSupportedException("Cannot rename Default Folder");
    }

    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("Cannot append to Default Folder");
    }

    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("Cannot expunge Default Folder");
    }
}
