package javax.activation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class CommandInfo {
    private String className;
    private String verb;

    public CommandInfo(String verb2, String className2) {
        this.verb = verb2;
        this.className = className2;
    }

    public String getCommandName() {
        return this.verb;
    }

    public String getCommandClass() {
        return this.className;
    }

    public Object getCommandObject(DataHandler dh, ClassLoader loader) throws IOException, ClassNotFoundException {
        InputStream is;
        Object new_bean = Beans.instantiate(loader, this.className);
        if (new_bean != null) {
            if (new_bean instanceof CommandObject) {
                ((CommandObject) new_bean).setCommandContext(this.verb, dh);
            } else if (!(!(new_bean instanceof Externalizable) || dh == null || (is = dh.getInputStream()) == null)) {
                ((Externalizable) new_bean).readExternal(new ObjectInputStream(is));
            }
        }
        return new_bean;
    }

    private static final class Beans {
        static final Method instantiateMethod;

        private Beans() {
        }

        static {
            Method m;
            try {
                m = Class.forName("java.beans.Beans").getDeclaredMethod("instantiate", new Class[]{ClassLoader.class, String.class});
            } catch (ClassNotFoundException e) {
                m = null;
            } catch (NoSuchMethodException e2) {
                m = null;
            }
            instantiateMethod = m;
        }

        static Object instantiate(ClassLoader loader, String cn) throws IOException, ClassNotFoundException {
            int b;
            if (instantiateMethod != null) {
                try {
                    return instantiateMethod.invoke((Object) null, new Object[]{loader, cn});
                } catch (InvocationTargetException e) {
                    InvocationTargetException invocationTargetException = e;
                    return null;
                } catch (IllegalAccessException e2) {
                    IllegalAccessException illegalAccessException = e2;
                    return null;
                }
            } else {
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    String cname = cn.replace('/', '.');
                    if (cname.startsWith("[") && (b = cname.lastIndexOf(91) + 2) > 1 && b < cname.length()) {
                        cname = cname.substring(b);
                    }
                    int i = cname.lastIndexOf(46);
                    if (i != -1) {
                        security.checkPackageAccess(cname.substring(0, i));
                    }
                }
                if (loader == null) {
                    loader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            try {
                                return ClassLoader.getSystemClassLoader();
                            } catch (SecurityException e) {
                                return null;
                            }
                        }
                    });
                }
                Class<?> beanClass = Class.forName(cn, true, loader);
                try {
                    return beanClass.newInstance();
                } catch (Exception ex) {
                    throw new ClassNotFoundException(beanClass + ": " + ex, ex);
                }
            }
        }
    }
}
