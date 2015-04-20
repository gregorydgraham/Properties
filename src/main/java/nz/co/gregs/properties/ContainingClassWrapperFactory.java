package nz.co.gregs.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * Constructs class adaptors for DB table classes and maintains an in-memory
 * cache for re-use. Creating class adaptors is expensive and this class is
 * provided as a convenience for anything that needs to access class adaptors
 * for multiple types and would benefit from the performance improvement of
 * caching their values.
 *
 * <p>
 * Note that class adaptors are immutable, so this is safe to do.
 *
 * <p>
 * This class is <i>thread-safe</i>.
 *
 * @author Malcolm Lett
 */
public class ContainingClassWrapperFactory {

    /**
     * Thread-safety: access to this object must be synchronized on it
     */
    private final Map<Class<?>, ContainingClassWrapper> classWrappersByClass = new HashMap<Class<?>, ContainingClassWrapper>();

    /**
     * Gets the class adaptor for the given class. If an adaptor for the given
     * class has not yet been created, one will be created and added to the
     * internal cache.
     *
	 * @param clazz clazz
     * @return the class adaptor
     */
    public ContainingClassWrapper classWrapperFor(Class<? extends ContainingClass> clazz) throws InstantiationException, IllegalAccessException {
        synchronized (classWrappersByClass) {
            ContainingClassWrapper wrapper = classWrappersByClass.get(clazz);
            if (wrapper == null) {
                wrapper =(ContainingClassWrapper) clazz.newInstance();
                classWrappersByClass.put(clazz, wrapper);
            }
            return wrapper;
        }
    }

    /**
     * Gets the object adaptor for the given object. If an adaptor for the
     * object's class has not yet been created, one will be created and added to
     * the internal cache.
     *
     * @param object the DBRow instance to wrap
     * @return the object adaptor for the given object
     */
    public ContainingInstanceWrapper instanceWrapperFor(ContainingClass object) throws InstantiationException, IllegalAccessException {
        return classWrapperFor(object.getClass()).instanceWrapperFor(object);
    }
}
