package nz.co.gregs.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nz.co.gregs.properties.JavaPropertyFinder.PropertyType;
import nz.co.gregs.properties.JavaPropertyFinder.Visibility;
import nz.co.gregs.properties.adapt.PropertyTypeHandler;

import nz.co.gregs.properties.exceptions.*;

/**
 * Wraps the class-type of an end-user's data model object. Generally it's
 * expected that the class is annotated with DBvolution annotations to mark the
 * table name and the fields or bean properties that map to columns, however
 * this class will work against any class type.
 *
 * <p>
 * To wrap a target object instance, use the
 * {@link #instanceWrapperFor(nz.co.gregs.dbvolution.query.RowDefinition) }
 * method.
 *
 * <p>
 * Note: instances of this class are expensive to create, and are intended to be
 * cached and kept long-term. Instances can be safely shared between DBDatabase
 * instances for different database types.
 *
 * <p>
 * Instances of this class are <i>thread-safe</i>.
 *
 * @author Malcolm Lett
 */
public class PropertyContainerClass {

	private final Class<? extends PropertyContainer> adapteeClass;
	private final boolean identityOnly;
	/**
	 * All properties of which DBvolution is aware, ordered as first encountered.
	 * Properties are only included if they are columns.
	 */
	private final List<PropertyDefinition> properties;

	/**
	 * Indexed by java property name.
	 */
	private final Map<String, PropertyDefinition> propertiesByPropertyName;

	/**
	 * Fully constructs a wrapper for the given class, including performing all
	 * validations that can be performed up front.
	 *
	 * @param clazz the {@code DBRow} class to wrap
	 * @throws DBPebkacException on any validation errors
	 */
	public PropertyContainerClass(Class<? extends PropertyContainer> clazz, PropertyTypeHandler handler) {
		this(clazz, handler, false);
	}

	/**
	 * Internal constructor only. Pass {@code processIdentityOnly=true} when
	 * processing a referenced class.
	 *
	 * <p>
	 * When processing identity only, only the primary key properties are
	 * identified.
	 *
	 *
	 * @param processIdentityOnly pass {@code true} to only process the set of
	 * columns and primary keys, and to ensure that the primary key columns are
	 * valid, but to exclude all other validations on non-primary key columns and
	 * types etc.
	 */
	PropertyContainerClass(Class<? extends PropertyContainer> clazz, PropertyTypeHandler handler, boolean processIdentityOnly) {
		adapteeClass = clazz;
		identityOnly = processIdentityOnly;

		// pre-calculate properties list
		// (note: skip if processing identity only, in order to avoid
		//  all the per-property validation)
		properties = new ArrayList<PropertyDefinition>();
		propertiesByPropertyName = new HashMap<String, PropertyDefinition>();
		// identity-only: extract only primary key properties
		JavaPropertyFinder propertyFinder = getJavaPropertyFinder();
		for (JavaProperty javaProperty : propertyFinder.getPropertiesOf(clazz)) {
			PropertyDefinition property;
			try {
				property = new PropertyDefinition(this, javaProperty, handler, processIdentityOnly);
				properties.add(property);
				propertiesByPropertyName.put(property.javaName(), property);
			} catch (Exception ex) {
				Logger.getLogger(PropertyContainerClass.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Gets a new instance of the java property finder, configured as required
	 *
	 * @return A new JavePropertyFinder with the required settings
	 */
	private static JavaPropertyFinder getJavaPropertyFinder() {
		return new JavaPropertyFinder(
				Visibility.PRIVATE, Visibility.PUBLIC,
				JavaPropertyFilter.ANY_PROPERTY_FILTER,
				PropertyType.FIELD, PropertyType.BEAN_PROPERTY);
	}

	/**
	 * Checks for errors that can't be known in advance without knowing the
	 * database being accessed.
	 *
	 */
	@SuppressWarnings("empty-statement")
	protected void checkForRemainingErrorsOnAcccess() {
		;
	}

	/**
	 * Gets an object wrapper instance for the given target object
	 *
	 * @param target the {@code DBRow} instance
	 * @return A PropertyContainerInstance for the supplied target.
	 */
	public PropertyContainerInstance instanceWrapperFor(PropertyContainer target) {
		if (identityOnly) {
			throw new AssertionError("Attempt to access non-identity information of identity-only DBRow class wrapper");
		}
//		checkForRemainingErrorsOnAcccess(database);
		return new PropertyContainerInstance(this, target);
	}

	/**
	 * Gets a string representation suitable for debugging.
	 *
	 * @return a string representation of this object.
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "<adapting:" + adapteeClass.getName() + ">";
	}

	/**
	 * Two {@code RowDefinitionClassWrappers} are equal if they wrap the same
	 * classes.
	 *
	 * @param obj	obj
	 * @return {@code true} if the two objects are equal, {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PropertyContainerClass)) {
			return false;
		}
		PropertyContainerClass other = (PropertyContainerClass) obj;
		if (adapteeClass == null) {
			if (other.adapteeClass != null) {
				return false;
			}
		} else if (!adapteeClass.equals(other.adapteeClass)) {
			return false;
		}
		return true;
	}

	/**
	 * Calculates the hash-code based on the hash-code of the wrapped class.
	 *
	 * @return the hash-code
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adapteeClass == null) ? 0 : adapteeClass.hashCode());
		return result;
	}

	/**
	 * Gets the underlying wrapped class.
	 *
	 * @return the DBRow or Object wrapped by this instance.
	 */
	public Class<? extends PropertyContainer> adapteeClass() {
		return adapteeClass;
	}

	/**
	 * Gets the simple name of the class being wrapped by this adaptor.
	 * <p>
	 * Use {@link #tableName()} for the name of the table mapped to this class.
	 *
	 * <p>
	 * Equivalent to {@code this.adaptee().getSimpleName();}
	 *
	 * @return the SimpleName of the class being wrapped.
	 */
	public String javaName() {
		return adapteeClass.getSimpleName();
	}

	/**
	 * Gets the fully qualified name of the class being wrapped by this adaptor.
	 * <p>
	 * Use {@link #tableName()} for the name of the table mapped to this class.
	 *
	 * @return the fully qualified name of the class being wrapped.
	 */
	public String qualifiedJavaName() {
		return adapteeClass.getName();
	}

	/**
	 * Gets the property by its java property name.
	 * <p>
	 * Only provides access to properties annotated with {@code DBColumn}.
	 *
	 * <p>
	 * It's legal for a field and bean-property to have the same name, and to both
	 * be annotated, but for different columns. This method doesn't handle that
	 * well and returns only the first one it sees.
	 *
	 * @param propertyName	propertyName
	 * @return the PropertyDefinition for the named object property Null if no
	 * such property is found.
	 * @throws AssertionError if called when in {@code identityOnly} mode.
	 */
	public PropertyDefinition getPropertyDefinitionByName(String propertyName) {
		if (identityOnly) {
			throw new AssertionError("Attempt to access non-identity information of identity-only DBRow class wrapper");
		}
		return propertiesByPropertyName.get(propertyName);
	}

	/**
	 * Gets all properties annotated with {@code DBColumn}.
	 *
	 * @return a List of all PropertyWrapperDefinitions for the wrapped class.
	 */
	public List<PropertyDefinition> getPropertyDefinitions() {
		if (identityOnly) {
			throw new AssertionError("Attempt to access non-identity information of identity-only DBRow class wrapper");
		}
		return properties;
	}
}
