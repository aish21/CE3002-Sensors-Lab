package javax.activation;

import java.io.IOException;
import java.io.OutputStream;

/* compiled from: DataHandler */
class DataSourceDataContentHandler implements DataContentHandler {
    private DataContentHandler dch = null;

    /* renamed from: ds */
    private DataSource f292ds = null;
    private ActivationDataFlavor[] transferFlavors = null;

    public DataSourceDataContentHandler(DataContentHandler dch2, DataSource ds) {
        this.f292ds = ds;
        this.dch = dch2;
    }

    public ActivationDataFlavor[] getTransferDataFlavors() {
        if (this.transferFlavors == null) {
            if (this.dch != null) {
                this.transferFlavors = this.dch.getTransferDataFlavors();
            } else {
                this.transferFlavors = new ActivationDataFlavor[1];
                this.transferFlavors[0] = new ActivationDataFlavor(this.f292ds.getContentType(), this.f292ds.getContentType());
            }
        }
        return this.transferFlavors;
    }

    public Object getTransferData(ActivationDataFlavor df, DataSource ds) throws IOException {
        if (this.dch != null) {
            return this.dch.getTransferData(df, ds);
        }
        if (df.equals(getTransferDataFlavors()[0])) {
            return ds.getInputStream();
        }
        throw new IOException("Unsupported DataFlavor: " + df);
    }

    public Object getContent(DataSource ds) throws IOException {
        if (this.dch != null) {
            return this.dch.getContent(ds);
        }
        return ds.getInputStream();
    }

    public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
        if (this.dch != null) {
            this.dch.writeTo(obj, mimeType, os);
            return;
        }
        throw new UnsupportedDataTypeException("no DCH for content type " + this.f292ds.getContentType());
    }
}
