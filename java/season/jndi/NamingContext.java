package season.jndi;

import javax.naming.*;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;

/**
 * Created by Administrator on 2018/10/29.
 */
public class NamingContext implements Context{

    protected Hashtable<String,Object> env;
    protected Hashtable bindings;
    protected String name;

    public NamingContext(Hashtable<String, Object> env, String name) {
        this.bindings = new Hashtable();
        this.env = env!=null? (Hashtable<String, Object>) env.clone() :null;
        this.name = name;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        while ((!name.isEmpty())&&name.get(0).length()==0){
            name=name.getSuffix(1);
            Object entry = bindings.get(name.get(0));
            if(name.size()>1){
                if(entry instanceof Context){
                    return ((Context)entry).lookup(name.getSuffix(1));
                }
            }else {
                if(entry instanceof Reference){
                    try {
                        return NamingManager.getObjectInstance(entry, name, this, env);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    return entry;
                }
            }
        }
        return null;
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return null;
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        while ((!name.isEmpty())&&name.get(0).length()==0){
            name=name.getSuffix(1);
            Object entry = bindings.get(name.get(0));
            if(name.size()>0){
                if(entry instanceof Context){
                    ((Context)entry).bind(name.getSuffix(1),obj);
                }
            }else {
                Object toBind = NamingManager.getStateToBind(obj, name, this, env);
                bindings.put(name.get(0),toBind);
            }

        }
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {

    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {

    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {

    }

    @Override
    public void unbind(Name name) throws NamingException {

    }

    @Override
    public void unbind(String name) throws NamingException {

    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {

    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {

    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return null;
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {

    }

    @Override
    public void destroySubcontext(String name) throws NamingException {

    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        NamingContext context = new NamingContext(env,this.name);
        bind(name,context);
        return context;
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return null;
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        return null;
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        return null;
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return null;
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return null;
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        return null;
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        return null;
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return null;
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        return null;
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return null;
    }

    @Override
    public void close() throws NamingException {

    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return null;
    }
}
