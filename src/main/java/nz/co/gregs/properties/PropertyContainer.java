/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties;

import java.util.List;

/**
 * A container of Properties.
 *
 * @author Gregory Graham
 */
public class PropertyContainer {
	private transient PropertyContainerWrapper wrapper;
	private final PropertyContainerWrapperFactory wrapperFactory;

  /**
   * Standard Constructor.
   *
   * @param factory the wrapper factory to generate a wrapper for this PropertyContainer
   */
  public PropertyContainer(PropertyContainerWrapperFactory factory) {
		wrapperFactory=factory;
	}

	/**
	 * Gets a wrapper for the underlying property (field or method) given the
	 * property's object reference.
	 *
	 * <p>
	 * For example the following code snippet will get a property wrapper for the
	 * {@literal name} field:
	 * <pre>
	 * Customer customer = ...;
	 * getPropertyWrapperOf(customer.name);
	 * </pre>
	 *
	 * @param obj	 a object in a field or bean of the PropertyContainer	
	 * @return the PropertyWrapper associated with the Object supplied or NULL.
	 */
	public Property getPropertyOf(Object obj) {
		List<Property> props = getWrapper().getPropertyWrappers();

		Object maybeTheObj;
		for (Property prop : props) {
			maybeTheObj = prop.rawJavaValue();
			if (maybeTheObj == obj) {
				return prop;
			}
		}
		return null;
	}

  /**
   * Returns the proper PropertyContainerWrapper for this object.
   * 
   * <p>If no wrapper exists, a new one is created.</p>
   *
   * @return the wrapper
   */
  protected PropertyContainerWrapper getWrapper() {
		if (wrapper == null) {
			wrapper = wrapperFactory.instanceWrapperFor(this);
		}
		return wrapper;
	}

	/**
	 * Returns the PropertyWrappers used internally to maintain the relationship
	 * between fields and columns
	 *
	 * @return non-null list of property wrappers, empty if none
	 */
	public List<PropertyDefinition> getPropertyWrappers() {
		return getWrapper().getPropertyDefinitions();
	}
	
}
