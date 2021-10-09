package javax.activation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;

public class DataHandler {
    private static final ActivationDataFlavor[] emptyFlavors = new ActivationDataFlavor[0];
    private static DataContentHandlerFactory factory = null;
    private CommandMap currentCommandMap = null;
    private DataContentHandler dataContentHandler = null;
    private DataSource dataSource = null;
    private DataContentHandler factoryDCH = null;
    private DataSource objDataSource = null;
    /* access modifiers changed from: private */
    public Object object = null;
    /* access modifiers changed from: private */
    public String objectMimeType = null;
    private DataContentHandlerFactory oldFactory = null;
    private String shortType = null;
    private ActivationDataFlavor[] transferFlavors = emptyFlavors;

    public DataHandler(DataSource ds) {
        this.dataSource = ds;
        this.oldFactory = factory;
    }

    public DataHandler(Object obj, String mimeType) {
        this.object = obj;
        this.objectMimeType = mimeType;
        this.oldFactory = factory;
    }

    public DataHandler(URL url) {
        this.dataSource = new URLDataSource(url);
        this.oldFactory = factory;
    }

    private synchronized CommandMap getCommandMap() {
        CommandMap defaultCommandMap;
        if (this.currentCommandMap != null) {
            defaultCommandMap = this.currentCommandMap;
        } else {
            defaultCommandMap = CommandMap.getDefaultCommandMap();
        }
        return defaultCommandMap;
    }

    public DataSource getDataSource() {
        if (this.dataSource != null) {
            return this.dataSource;
        }
        if (this.objDataSource == null) {
            this.objDataSource = new DataHandlerDataSource(this);
        }
        return this.objDataSource;
    }

    public String getName() {
        if (this.dataSource != null) {
            return this.dataSource.getName();
        }
        return null;
    }

    public String getContentType() {
        if (this.dataSource != null) {
            return this.dataSource.getContentType();
        }
        return this.objectMimeType;
    }

    public InputStream getInputStream() throws IOException {
        if (this.dataSource != null) {
            return this.dataSource.getInputStream();
        }
        DataContentHandler dch = getDataContentHandler();
        if (dch == null) {
            throw new UnsupportedDataTypeException("no DCH for MIME type " + getBaseType());
        } else if (!(dch instanceof ObjectDataContentHandler) || ((ObjectDataContentHandler) dch).getDCH() != null) {
            final DataContentHandler fdch = dch;
            final PipedOutputStream pos = new PipedOutputStream();
            InputStream pin = new PipedInputStream(pos);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        fdch.writeTo(DataHandler.this.object, DataHandler.this.objectMimeType, pos);
                        try {
                            pos.close();
                        } catch (IOException e) {
                        }
                    } catch (IOException e2) {
                        try {
                            pos.close();
                        } catch (IOException e3) {
                        }
                    } catch (Throwable th) {
                        try {
                            pos.close();
                        } catch (IOException e4) {
                        }
                        throw th;
                    }
                }
            }, "DataHandler.getInputStream").start();
            return pin;
        } else {
            throw new UnsupportedDataTypeException("no object DCH for MIME type " + getBaseType());
        }
    }

    public void writeTo(OutputStream os) throws IOException {
        if (this.dataSource != null) {
            byte[] data = new byte[8192];
            InputStream is = this.dataSource.getInputStream();
            while (true) {
                try {
                    int bytes_read = is.read(data);
                    if (bytes_read > 0) {
                        os.write(data, 0, bytes_read);
                    } else {
                        return;
                    }
                } finally {
                    is.close();
                }
            }
        } else {
            getDataContentHandler().writeTo(this.object, this.objectMimeType, os);
        }
    }

    public OutputStream getOutputStream() throws IOException {
        if (this.dataSource != null) {
            return this.dataSource.getOutputStream();
        }
        return null;
    }

    public synchronized ActivationDataFlavor[] getTransferDataFlavors() {
        ActivationDataFlavor[] activationDataFlavorArr;
        if (factory != this.oldFactory) {
            this.transferFlavors = emptyFlavors;
        }
        if (this.transferFlavors == emptyFlavors) {
            this.transferFlavors = getDataContentHandler().getTransferDataFlavors();
        }
        if (this.transferFlavors == emptyFlavors) {
            activationDataFlavorArr = this.transferFlavors;
        } else {
            activationDataFlavorArr = (ActivationDataFlavor[]) this.transferFlavors.clone();
        }
        return activationDataFlavorArr;
    }

    public boolean isDataFlavorSupported(ActivationDataFlavor flavor) {
        ActivationDataFlavor[] lFlavors = getTransferDataFlavors();
        for (ActivationDataFlavor equals : lFlavors) {
            if (equals.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    public Object getTransferData(ActivationDataFlavor flavor) throws IOException {
        return getDataContentHandler().getTransferData(flavor, this.dataSource);
    }

    public synchronized void setCommandMap(CommandMap commandMap) {
        if (commandMap != this.currentCommandMap || commandMap == null) {
            this.transferFlavors = emptyFlavors;
            this.dataContentHandler = null;
            this.currentCommandMap = commandMap;
        }
    }

    public CommandInfo[] getPreferredCommands() {
        if (this.dataSource != null) {
            return getCommandMap().getPreferredCommands(getBaseType(), this.dataSource);
        }
        return getCommandMap().getPreferredCommands(getBaseType());
    }

    public CommandInfo[] getAllCommands() {
        if (this.dataSource != null) {
            return getCommandMap().getAllCommands(getBaseType(), this.dataSource);
        }
        return getCommandMap().getAllCommands(getBaseType());
    }

    public CommandInfo getCommand(String cmdName) {
        if (this.dataSource != null) {
            return getCommandMap().getCommand(getBaseType(), cmdName, this.dataSource);
        }
        return getCommandMap().getCommand(getBaseType(), cmdName);
    }

    public Object getContent() throws IOException {
        if (this.object != null) {
            return this.object;
        }
        return getDataContentHandler().getContent(getDataSource());
    }

    public Object getBean(CommandInfo cmdinfo) {
        try {
            ClassLoader cld = SecuritySupport.getContextClassLoader();
            if (cld == null) {
                cld = getClass().getClassLoader();
            }
            return cmdinfo.getCommandObject(this, cld);
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    private synchronized DataContentHandler getDataContentHandler() {
        DataContentHandler dataContentHandler2;
        if (factory != this.oldFactory) {
            this.oldFactory = factory;
            this.factoryDCH = null;
            this.dataContentHandler = null;
            this.transferFlavors = emptyFlavors;
        }
        if (this.dataContentHandler != null) {
            dataContentHandler2 = this.dataContentHandler;
        } else {
            String simpleMT = getBaseType();
            if (this.factoryDCH == null && factory != null) {
                this.factoryDCH = factory.createDataContentHandler(simpleMT);
            }
            if (this.factoryDCH != null) {
                this.dataContentHandler = this.factoryDCH;
            }
            if (this.dataContentHandler == null) {
                if (this.dataSource != null) {
                    this.dataContentHandler = getCommandMap().createDataContentHandler(simpleMT, this.dataSource);
                } else {
                    this.dataContentHandler = getCommandMap().createDataContentHandler(simpleMT);
                }
            }
            if (this.dataSource != null) {
                this.dataContentHandler = new DataSourceDataContentHandler(this.dataContentHandler, this.dataSource);
            } else {
                this.dataContentHandler = new ObjectDataContentHandler(this.dataContentHandler, this.object, this.objectMimeType);
            }
            dataContentHandler2 = this.dataContentHandler;
        }
        return dataContentHandler2;
    }

    private synchronized String getBaseType() {
        if (this.shortType == null) {
            String ct = getContentType();
            try {
                this.shortType = new MimeType(ct).getBaseType();
            } catch (MimeTypeParseException e) {
                this.shortType = ct;
            }
        }
        return this.shortType;
    }

    public static synchronized void setDataContentHandlerFactory(DataContentHandlerFactory newFactory) {
        synchronized (DataHandler.class) {
            if (factory != null) {
                throw new Error("DataContentHandlerFactory already defined");
            }
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                try {
                    security.checkSetFactory();
                } catch (SecurityException ex) {
                    if (DataHandler.class.getClassLoader() != newFactory.getClass().getClassLoader()) {
                        throw ex;
                    }
                }
            }
            factory = newFactory;
        }
    }
}
