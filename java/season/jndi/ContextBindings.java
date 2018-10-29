package season.jndi;

import javax.naming.Context;
import java.util.Hashtable;

/**
 * Created by Administrator on 2018/10/29.
 */
public class ContextBindings {

    private static final Hashtable<Object, Context> contextNameBinding
            = new Hashtable<>();

    private static final Hashtable<Thread, Context> threadBinding
            = new Hashtable<>();

    private static final Hashtable<ClassLoader, Context> clBinding
            = new Hashtable<>();

    public static void bindContext(Object name, Context context) {
        contextNameBinding.put(name, context);
    }

    public static void unbindContext(Object name, Context context) {
        contextNameBinding.remove(name);
    }

    public static Context getContext(Object name, Context context) {
        return contextNameBinding.get(name);
    }

    public static void bindThread(Object name) {
        Context context = contextNameBinding.get(name);
        threadBinding.put(Thread.currentThread(), context);
    }

    public static void unbindThread(Object name) {
        threadBinding.remove(Thread.currentThread());
    }

    public static Context getThread() {
        return threadBinding.get(Thread.currentThread());
    }

    public static boolean isThreadBound() {
        return threadBinding.contains(Thread.currentThread());
    }

    public static void bindClassLoader(Object name) {
        Context context = contextNameBinding.get(name);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        clBinding.put(classLoader, context);
    }

    public static void unbindClassLoader(Object name) {
        clBinding.remove(Thread.currentThread().getContextClassLoader());
    }

    public static Context getClassLoader() {
        ClassLoader c = Thread.currentThread().getContextClassLoader();
        do {
            Context context = clBinding.get(c);
            if (context != null) return context;
        } while ((c = c.getParent()) != null);
        return null;
    }

    public static boolean isClassLoaderBound() {
        ClassLoader c = Thread.currentThread().getContextClassLoader();
        do {
            if (clBinding.contains(c)) return true;
        } while ((c = c.getParent()) != null);
        return false;
    }

}
