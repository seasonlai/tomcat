package season.jndi;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

/**
 * Created by Administrator on 2018/10/29.
 */
public class ResourceRef extends Reference {

    public static final String DEFAULT_FACTORY = "season.jndi.ResourceFactory";
    public static final String DESCRIPTION = "description";

    public ResourceRef(String className, String desc) {
        this(className, desc, null, null);
    }

    public ResourceRef(String className, String desc, String factory, String factoryLocation) {
        super(className, factory, factoryLocation);
        if (desc != null) {
            add(new StringRefAddr(DESCRIPTION, desc));
        }
    }

    @Override
    public String getFactoryClassName() {
        String fn = super.getFactoryClassName();
        if (fn != null) return fn;
        return DEFAULT_FACTORY;
    }
}
