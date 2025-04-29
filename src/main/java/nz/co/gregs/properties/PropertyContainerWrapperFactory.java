package nz.co.gregs.properties;

import java.util.HashMap;
import java.util.Map;
import nz.co.gregs.properties.adapt.PropertyTypeHandler;

/**
 * Constructs class adaptors for property containers classes and maintains an in-memory cache for re-use. Creating class adaptors is expensive and this class is
 * provided as a convenience for anything that needs to access class adaptors for multiple types and would benefit from the performance improvement of caching
 * their values.
 *
 * <p>
 * Note that class adaptors are immutable, so this is safe to do.
 *
 * <p>
 * This class is <i>thread-safe</i>.
 *
 * @author Malcolm Lett
 */
public class PropertyContainerWrapperFactory {

  /**
   * Thread-safety: access to this object must be synchronized on it
   */
  private final Map<Class<?>, PropertyContainerClass> classWrappersByClass = new HashMap<Class<?>, PropertyContainerClass>();
  private final PropertyTypeHandler handler;

  public PropertyContainerWrapperFactory(PropertyTypeHandler handler) {
    this.handler = handler;
  }

  /**
   * Gets the class adaptor for the given class. If an adaptor for the given class has not yet been created, one will be created and added to the internal
   * cache.
   *
   * @param clazz clazz
   * @return the class adaptor
   */
  public PropertyContainerClass classWrapperFor(Class<? extends PropertyContainer> clazz) {
    synchronized (classWrappersByClass) {
      PropertyContainerClass wrapper = classWrappersByClass.get(clazz);
      if (wrapper == null) {
        wrapper = new PropertyContainerClass(clazz, handler);
        classWrappersByClass.put(clazz, wrapper);
      }
      return wrapper;
    }
  }

  /**
   * Gets the object adaptor for the given object.If an adaptor for the object's class has not yet been created, one will be created and added to the internal
   * cache.
   *
   * @param object the DBRow instance to wrap
   * @return the object adaptor for the given object
   */
  public PropertyContainerWrapper instanceWrapperFor(PropertyContainer object) {
    return classWrapperFor(object.getClass()).instanceWrapperFor(object);
  }
}
