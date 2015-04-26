/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties;

import java.util.List;

/**
 *
 * @author gregorygraham
 */
public class PropertyContainer {
	private transient PropertyContainerInstance wrapper;
	private final PropertyContainerWrapperFactory wrapperFactory;

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
	 * @param qdt	 qdt	
	 * @return the PropertyWrapper associated with the Object suppled or NULL.
	 */
	public Property getPropertyOf(Object qdt) throws InstantiationException, IllegalAccessException {
		List<Property> props = getWrapper().getPropertyWrappers();

		Object qdtOfProp;
		for (Property prop : props) {
			qdtOfProp = prop.rawJavaValue();
			if (qdtOfProp == qdt) {
				return prop;
			}
		}
		return null;
	}

	protected PropertyContainerInstance getWrapper() throws InstantiationException, IllegalAccessException {
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
	public List<PropertyDefinition> getPropertyWrappers() throws InstantiationException, IllegalAccessException {
		return getWrapper().getPropertyDefinitions();
	}
	
}
