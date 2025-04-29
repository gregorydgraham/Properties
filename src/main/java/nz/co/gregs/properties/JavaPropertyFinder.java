package nz.co.gregs.properties;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.gregs.properties.exceptions.PropertyException;

/**
 * Low-level internal utility for finding properties within classes.
 *
 * @author Malcolm Lett
 */
// Note: java.beans.Introspector
public class JavaPropertyFinder {

  /**
   * Properties can be either field or beans
   */
  public static enum PropertyType {

    /**
     * Field properties are accessed directly
     */
    FIELD,
    /**
     * Beans are accessed using getter and setter methods
     */
    BEAN_PROPERTY
  };

  /**
   * This enum collects all the security options for the properties.
   * 
   * <p>Seems a bit field biased [shrug]</p>
   */
  public static enum Visibility {

    /**
     * Properties with the PUBLIC keyword
     */
    PUBLIC,
    /**
     * Properties with the PROTECTED keyword
     */
    PROTECTED,
    /**
     * Default properties have no security keyword association
     */
    DEFAULT,
    /**
     * Properties with the PRIVATE keyword
     */
    PRIVATE
  };

  private Set<PropertyType> propertyTypes = EnumSet.allOf(PropertyType.class);
  private Visibility fieldVisibility = Visibility.PUBLIC;
  private Visibility methodVisibility = Visibility.PUBLIC;
  private JavaPropertyFilter filter = JavaPropertyFilter.ANY_PROPERTY_FILTER;

  /**
   * New default instance with default search characteristics.
   */
  public JavaPropertyFinder() {
  }

  /**
   * Standard constructor.
   *
   * <p>
   * Low-level internal utility for finding properties within classes</p>
   *
   * @param fieldVisibility the most private level of field that should be retrieved
   * @param methodVisibility the most private level of method that should be retrieved
   * @param filter supply null for no filter
   * @param propertyTypes supply null for default
   */
  public JavaPropertyFinder(Visibility fieldVisibility, Visibility methodVisibility,
          JavaPropertyFilter filter, PropertyType... propertyTypes) {
    // check for errors
    if (methodVisibility.ordinal() > Visibility.PUBLIC.ordinal()) {
      throw new UnsupportedOperationException("Scanning for non-public property accessors is not supported");
    }

    this.fieldVisibility = fieldVisibility;
    this.methodVisibility = methodVisibility;
    this.filter = (filter == null) ? JavaPropertyFilter.ANY_PROPERTY_FILTER : filter;

    if (propertyTypes == null || propertyTypes.length == 0) {
      this.propertyTypes = EnumSet.allOf(PropertyType.class);
    } else {
      this.propertyTypes = EnumSet.noneOf(PropertyType.class);
      for (PropertyType propertyType : propertyTypes) {
        this.propertyTypes.add(propertyType);
      }
    }
  }

  /**
   * Gets all properties according to configured criteria.
   *
   * <p>
   * Note: this class makes no attempt to avoid returning a property as both its field and it's accessor methods. The caller may thus investigate both for
   * expected annotations. However this does mean that the caller must do extra effort to avoid using both.
   *
   * @param clazz the type to inspect
   * @return the non-null list of properties found on the given class
   */
  public List<JavaProperty> getPropertiesOf(Class<?> clazz) {
    List<JavaProperty> properties = new ArrayList<>();

    // retrieve fields
    if (propertyTypes.contains(PropertyType.FIELD)) {
      properties.addAll(getFields(clazz));
    }

    // retrieve bean-properties
    if (propertyTypes.contains(PropertyType.BEAN_PROPERTY)) {
      properties.addAll(getBeanProperties(clazz));
    }

    return properties;
  }

  /**
   * Gets the field-based properties.
   *
   * @return
   */
  // TODO: this may not be able to handle inheritance of protected/default fields
  private List<JavaProperty> getFields(Class<?> clazz) {
    List<JavaProperty> properties = new ArrayList<>();

    Set<String> observedFieldNames = new HashSet<>();

    // get all public fields
    // (these are inherited, so need to use the proper inheritance-aware method)
    for (Field field : clazz.getFields()) {
      field.setAccessible(true);
      if (filter.acceptField(field)) {
        properties.add(new JavaField(field));
      }
      observedFieldNames.add(field.getName());
    }

    // get all non-public fields
    // (getDeclaredFields() isn't inheritance aware,
    //  so we're probably not going to be inherited protected/default fields this way)
    if (fieldVisibility.ordinal() > Visibility.PUBLIC.ordinal()) {
      for (Field field : clazz.getDeclaredFields()) {
        field.setAccessible(true);
        if (!observedFieldNames.contains(field.getName())) {
          if (visibilityOf(field).ordinal() <= fieldVisibility.ordinal()) {
            // skip standard java fields
            if (field.getName().equals("serialVersionUID")) {
              continue;
            }

            // add field if accepted
            // (plus set accessible)
            if (filter.acceptField(field)) {
              // make accessible
              // TODO: pretty sure there's exception types that need to be caught on this call
              field.setAccessible(true);

              properties.add(new JavaField(field));
            }
          }
        }
      }
    }

    return properties;
  }

  /**
   * Gets the bean-property-based properties.
   *
   * @return
   */
  private List<JavaProperty> getBeanProperties(Class<?> clazz) {
    List<JavaProperty> properties = new ArrayList<>();

    // get all public bean-properties
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
      for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
        Method getter = descriptor.getReadMethod();
        Method setter = descriptor.getWriteMethod();

        // skip standard java fields
        if (descriptor.getName().equals("class")) {
          continue;
        }

        // add field if accepted
        if (filter.acceptBeanProperty(getter, setter)) {
          properties.add(new JavaBeanProperty(descriptor));
        }
      }
    } catch (IntrospectionException e) {
      // TODO: handle this properly
      throw new PropertyException("Error inspecting " + clazz.getName() + ": " + e.getMessage(), e);
    }

    // get all non-public bean-properties
    if (methodVisibility.ordinal() > Visibility.PUBLIC.ordinal()) {
      throw new UnsupportedOperationException("Using non-public property accessors is not supported");
    }

    return properties;
  }

  private static Visibility visibilityOf(Field field) {
    return visibilityOf(field.getModifiers());
  }

  private static Visibility visibilityOf(Method method) {
    return visibilityOf(method.getModifiers());
  }

  private static Visibility visibilityOf(int modifiers) {
    if (Modifier.isPublic(modifiers)) {
      return Visibility.PUBLIC;
    } else if (Modifier.isProtected(modifiers)) {
      return Visibility.PROTECTED;
    } else if (Modifier.isPrivate(modifiers)) {
      return Visibility.PRIVATE;
    } else {
      return Visibility.DEFAULT;
    }
  }

}
