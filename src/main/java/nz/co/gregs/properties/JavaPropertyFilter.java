package nz.co.gregs.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Used internally to control filtering of properties when first retrieving them.
 * The same logic could be included directly within {@link JavaPropertyFilter},
 * but this way the logic is sitting in one place plus it gives the ability to 
 * re-use the {@link JavaPropertyFilter} if we ever extend the to process all fields/bean-properties.
 * @author Malcolm Lett
 */
interface JavaPropertyFilter {
	/** Accepts everything */
	public static final JavaPropertyFilter ANY_PROPERTY_FILTER = new AnyPropertyFilter();
	
	/**
	 * Indicates whether the specified field is accepted by the filter.
	 
	 * @return
	 */
	public boolean acceptField(Field field);
	
	/**
	 * Indicates whether the specified getter/setter pair are accepted
	 * by the filter.
	 
	 
	 * @return
	 */
	public boolean acceptBeanProperty(Method getter, Method setter);

}
