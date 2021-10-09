package javax.activation;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class FileTypeMap {
    private static FileTypeMap defaultMap = null;
    private static Map<ClassLoader, FileTypeMap> map = new WeakHashMap();

    public abstract String getContentType(File file);

    public abstract String getContentType(String str);

    public static synchronized void setDefaultFileTypeMap(FileTypeMap fileTypeMap) {
        synchronized (FileTypeMap.class) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                try {
                    security.checkSetFactory();
                } catch (SecurityException ex) {
                    ClassLoader cl = FileTypeMap.class.getClassLoader();
                    if (cl == null || cl.getParent() == null || cl != fileTypeMap.getClass().getClassLoader()) {
                        throw ex;
                    }
                }
            }
            map.remove(SecuritySupport.getContextClassLoader());
            defaultMap = fileTypeMap;
        }
    }

    public static synchronized FileTypeMap getDefaultFileTypeMap() {
        FileTypeMap def;
        synchronized (FileTypeMap.class) {
            if (defaultMap != null) {
                def = defaultMap;
            } else {
                ClassLoader tccl = SecuritySupport.getContextClassLoader();
                def = map.get(tccl);
                if (def == null) {
                    def = new MimetypesFileTypeMap();
                    map.put(tccl, def);
                }
            }
        }
        return def;
    }
}
