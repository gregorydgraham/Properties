package nz.co.gregs.properties;

import nz.co.gregs.properties.adapt.PropertyTypeHandler;
import nz.co.gregs.properties.adapt.AdaptableType;
import nz.co.gregs.properties.exceptions.*;

/**
 * Abstracts a java field or bean-property as a DBvolution-centric property,
 * which contains values from a specific column in a database table.
 * Transparently handles all annotations associated with the property, including
 * type adaption.
 *
 * <p>
 * Provides access to the meta-data defined on a single java property of a
 * class, and provides methods for reading and writing the value of the property
 * on target objects. Instances of this class are not bound to specific target
 * objects, nor are they bound to specific database definitions.
 *
 * <p>
 * For binding to specific target objects and database definitions, use the
 * {@link PropertyContainer} class.
 *
 * <p>
 * DB properties can be seen to have the types and values in the table that
 * follows. This class provides a virtual view over the property whereby the
 * DBv-centric type and value are easily accessible via the
 * {@link #getAdaptableType(Object) value()} and
 * {@link #setAdaptableType(java.lang.Object, nz.co.gregs.properties.adapt.AdaptableType) setValue()} methods.
 * <ul>
 * <li> rawType/rawValue - the type and value actually stored on the declared
 * java property
 * <li> dbvType/dbvValue - the type and value used within DBv (a
 * QueryableDataType)
 * <li> databaseType/databaseValue - the type and value of the database column
 * itself (this class doesn't deal with these)
 * </ul>
 *
 * <p>
 * Note: instances of this class are expensive to create and should be cached.
 *
 * <p>
 * This class is <i>thread-safe</i>.
 *
 * <p>
 * This class is not serializable. References to it within serializable classes
 * should be marked as {@code transient}.
 */
public class PropertyDefinition {

	private final PropertyContainerClass classWrapper;
	private final JavaProperty javaProperty;

	private final PropertyTypeHandler typeHandler;

	public PropertyDefinition(PropertyContainerClass classWrapper, JavaProperty javaProperty, PropertyTypeHandler handler, boolean processIdentityOnly) {
		this.classWrapper = classWrapper;
		this.javaProperty = javaProperty;

		// handlers
		this.typeHandler = handler;
		typeHandler.initialiseHandler(javaProperty, processIdentityOnly);
	}

	public JavaProperty getRawJavaProperty() {
		return javaProperty;
	}

	/**
	 * Gets a string representation of the wrapped property, suitable for
	 * debugging and logging.
	 *
	 * @return a string representation of this object
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(type().getSimpleName());
		buf.append(" ");
		buf.append(qualifiedJavaName());

		if (isTypeAdapted()) {
			buf.append(" (");
			buf.append(getRawJavaType().getSimpleName());
			buf.append(")");
		}
		return buf.toString();
	}

	/**
	 * Generates a hash-code of this property wrapper definition, based entirely
	 * on the java property it wraps.
	 *
	 * @return a hash-code.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaProperty == null) ? 0 : javaProperty.hashCode());
		return result;
	}

	/**
	 * Equality of this property wrapper definition, based on the java property
	 * it wraps in a specific class. Two instances are identical if they wrap
	 * the same java property (field or bean-property) in the same class and the
	 * same class-loader.
	 *
	 * @param obj the other object to compare to.
	 * @return {@code true} if the two objects are equal, {@code false}
	 * otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PropertyDefinition)) {
			return false;
		}
		PropertyDefinition other = (PropertyDefinition) obj;
		if (javaProperty == null) {
			if (other.javaProperty != null) {
				return false;
			}
		} else if (!javaProperty.equals(other.javaProperty)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the name of the java property, without the containing class name.
	 * Mainly used within error messages. eg: {@code "uid"}
	 *
	 * <p>
	 * Use {@link #getColumnName()} to determine column name.
	 *
	 * @return a String of the Java field name for this property
	 */
	public String javaName() {
		return javaProperty.name();
	}

	/**
	 * Gets the partially qualified name of the underlying java property, using
	 * the short-name of the containing class. Mainly used within logging and
	 * error messages. eg: {@code "Customer.uid"}
	 *
	 * <p>
	 * Use {@link #getColumnName()} to determine column name.
	 *
	 * @return a String of the short name of the declared class of this property
	 */
	public String shortQualifiedJavaName() {
		return javaProperty.shortQualifiedName();
	}

	/**
	 * Gets the fully qualified name of the underlying java property, including
	 * the fully qualified name of the containing class. Mainly used within
	 * logging and error messages. eg:
	 * {@code "nz.co.mycompany.myproject.Customer.uid"}
	 *
	 * <p>
	 * Use {@link #getColumnName()} to determine column name.
	 *
	 * @return a String of the full name of the class of this property
	 */
	public String qualifiedJavaName() {
		return javaProperty.qualifiedName();
	}

	/**
	 * Gets the DBvolution-centric type of the property. If a type adaptor is
	 * present, then this is the type after conversion from the target object's
	 * actual property type.
	 *
	 * <p>
	 * Use {@link #getRawJavaType()} in the rare case that you need to know the
	 * underlying java property type.
	 *
	 * @return the Class of the internal QueryableDatatype used by this property
	 */
	public Class<? extends AdaptableType> type() {
		return typeHandler.getType();
	}

	/**
	 * Convenience method for testing the type. Equivalent to
	 * {@code refType.isAssignableFrom(this.type())}.
	 *
	 * @param refType	 refType	
	 * @return TRUE if the supplied type is assignable from the internal
	 * QueryableDatatype, FALSE otherwise.
	 */
	public boolean isInstanceOf(Class<? extends AdaptableType> refType) {
		return refType.isAssignableFrom(type());
	}

	/**
	 * Indicates whether the value of the property can be retrieved. Bean
	 * properties which are missing a 'getter' can not be read, but may be able
	 * to be set.
	 *
	 * @return TRUE if the property is readable, FALSE otherwise.
	 */
	public boolean isReadable() {
		return javaProperty.isReadable();
	}

	/**
	 * Indicates whether the value of the property can be modified. Bean
	 * properties which are missing a 'setter' can not be written to, but may be
	 * able to be read.
	 *
	 * @return TRUE if the property can be set, FALSE otherwise
	 */
	public boolean isWritable() {
		return javaProperty.isWritable();
	}

	/**
	 * Indicates whether the property's type is adapted by an explicit or
	 * implicit type adaptor. (Note: at present there is no support for implicit
	 * type adaptors)
	 *
	 * @return {@code true} if a type adaptor is being used
	 */
	public boolean isTypeAdapted() {
		return typeHandler.isTypeAdapted();
	}

	/**
	 * Gets the DBvolution-centric value of the property. The value returned may
	 * have undergone type conversion from the target object's actual property
	 * type, if a type adaptor is present.
	 *
	 * <p>
	 * Use {@link #isReadable()} beforehand to check whether the property can be
	 * read.
	 *
	 * @param target object instance containing this property
	 * @return the QueryableDatatype used internally.
	 * @throws IllegalStateException if not readable (you should have called
	 * isReadable() first)
	 * @throws DBThrownByEndUserCodeException if any user code throws an
	 * exception
	 */
	public AdaptableType getAdaptableType(Object target) {
		AdaptableType adaptable = typeHandler.getJavaPropertyAsAdaptableType(target);
		new InternalAdaptableTypeProxy(adaptable).setPropertyWrapper(this);
		return adaptable;
	}

	/**
	 * Sets the DBvolution-centric value of the property. The value set may have
	 * undergone type conversion to the target object's actual property type, if
	 * a type adaptor is present.
	 *
	 * <p>
	 * Use {@link #isWritable()} beforehand to check whether the property can be
	 * modified.
	 *
	 * @param target object instance containing this property
	 * @param value value
	 value
	
	 * @throws IllegalStateException if not writable (you should have called
	 * isWritable() first)
	 * @throws DBThrownByEndUserCodeException if any user code throws an
	 * exception
	 */
	public void setAdaptableType(Object target, AdaptableType value) {
		new InternalAdaptableTypeProxy(value).setPropertyWrapper(this);
		typeHandler.setJavaPropertyAsAdaptableType(target, value);
	}

	/**
	 * Gets the value of the declared property in the end-user's target object,
	 * prior to type conversion to the DBvolution-centric type.
	 *
	 * <p>
	 * In most cases you will not need to call this method, as type conversion
	 * is done transparently via the {@link #getAdaptableType(Object)} and
	 * {@link #setQueryableDatatype(Object, QueryableDatatype)} methods.
	 *
	 * <p>
	 * Use {@link #isReadable()} beforehand to check whether the property can be
	 * read.
	 *
	 * @param target object instance containing this property
	 * @return value
	 * @throws IllegalStateException if not readable (you should have called
	 * isReadable() first)
	 * @throws DBThrownByEndUserCodeException if any user code throws an
	 * exception
	 */
	public Object rawJavaValue(Object target) {
		return javaProperty.get(target);
	}

	/**
	 * Set the value of the declared property in the end-user's target object,
	 * without type conversion to/from the DBvolution-centric type.
	 *
	 * <p>
	 * In most cases you will not need to call this method, as type conversion
	 * is done transparently via the {@link #getAdaptableType(Object)} and
	 * {@link #setQueryableDatatype(Object, QueryableDatatype)} methods.
	 *
	 * <p>
	 * Use {@link #isWritable()} beforehand to check whether the property can be
	 * modified.
	 *
	 * @param target object instance containing this property
	 * @param value new value
	 * @throws IllegalStateException if not writable (you should have called
	 * isWritable() first)
	 * @throws DBThrownByEndUserCodeException if any user code throws an
	 * exception
	 */
	public void setRawJavaValue(Object target, Object value) {
		javaProperty.set(target, value);
	}

	/**
	 * Gets the declared type of the property in the end-user's target object,
	 * prior to type conversion to the DBvolution-centric type.
	 *
	 * <p>
	 * In most cases you will not need to call this method, as type conversion
	 * is done transparently via the {@link #getAdaptableType(Object) } and
	 * {@link #setQueryableDatatype(Object, QueryableDatatype)} methods. Use the
	 * {@link #type()} method to get the DBv-centric property type, after type
	 * conversion.
	 *
	 * @return the declared class of the property
	 */
	public Class<?> getRawJavaType() {
		return javaProperty.type();
	}

	/**
	 * Gets the wrapper for the RowDefinition (DBRow or DBReport) subclass
	 * containing this property.
	 *
	 * @return the PropertyContainerClass representing the enclosing object
 of this property
	 */
	public PropertyContainerClass getPropertyContainerClass() {
		return classWrapper;
	}

}
