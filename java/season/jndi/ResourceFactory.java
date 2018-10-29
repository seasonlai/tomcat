package season.jndi;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * Created by Administrator on 2018/10/29.
 */
public class ResourceFactory implements ObjectFactory {
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        if(obj instanceof ResourceRef){
            Reference ref= (Reference) obj;
            String className = ref.getClassName();
            return Class.forName(className).newInstance();
        }
        return null;
    }
}
