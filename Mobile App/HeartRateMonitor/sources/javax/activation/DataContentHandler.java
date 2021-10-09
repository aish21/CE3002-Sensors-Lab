package javax.activation;

import java.io.IOException;
import java.io.OutputStream;

public interface DataContentHandler {
    Object getContent(DataSource dataSource) throws IOException;

    Object getTransferData(ActivationDataFlavor activationDataFlavor, DataSource dataSource) throws IOException;

    ActivationDataFlavor[] getTransferDataFlavors();

    void writeTo(Object obj, String str, OutputStream outputStream) throws IOException;
}
