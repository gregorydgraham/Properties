package nz.co.gregs.properties;

import nz.co.gregs.properties.adapt.AdaptableType;
import nz.co.gregs.properties.exceptions.*;

/**
 * Abstracts a java field or bean-property on a target object as a
 * storage-centric property, which contains values from a specific value from an 
 * external storage system, such as a database or file system. Transparently 
 * handles all annotations associated with the property, including type adaption.
 *
 * <p>
 * Provides access to the meta-data defined on a single java property of a
 * class, and provides methods for reading and writing the value of the property
 * on a single bound object.
 *
 * <p>
 * properties can be seen to have the types and values in the table that
 * follows. This class provides a virtual view over the property whereby the
 * Java-centric type and value are easily accessible via the
 * {@link #getAdaptableType()} and
 * {@link #setAdaptableType(nz.co.gregs.properties.adapt.AdaptableType)} methods.
 * <ul>
 * <li> rawType/rawValue - the type and value actually stored on the declared
 * java property
 * <li> referenceType/referenceValue - the type and value used within Properties (a
 * Property type)
 * <li> externalType/externalValue - the type and value of the external store
 * itself (this class doesn't deal with these)
 * </ul>
 *
 * <p>
 * Note: instances of this class are cheap to create and do not need to be
 * cached.
 *
 * <p>
 * This class is <i>thread-safe</i>.
 *
 * <p>
 * This class is not serializable. References to it within serializable classes
 * should be marked as {@code transient}.
 */
public class Property {

    private final PropertyContainerWrapper dbRowInstanceWrapper;
    private final PropertyDefinition propertyDefinition;
    private final Object target;

    /**
     * Property constructor
     * 
     * @param instanceWrapper instanceWrapper
     * @param classProperty the class-level wrapper
     * @param target the target object containing the given property
     */
    public Property(
            PropertyContainerWrapper instanceWrapper,            
            PropertyDefinition classProperty, 
            Object target) {
      this.dbRowInstanceWrapper = instanceWrapper;
      this.propertyDefinition = classProperty;
      this.target = target;
    }

    /**
     * Gets a string representation of the wrapped property, suitable for
     * debugging and logging.
     *
     * @return a String representing this Property
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(type().getSimpleName());
        buf.append(" ");
        buf.append(qualifiedJavaName());
        if (isReadable()) {
            buf.append(" = [");
            try {
                buf.append(getAdaptableType());
            } catch (Exception e) {
                buf.append("<exception occurred>");
            }
            buf.append("]");
        }

        if (isTypeAdapted()) {
            buf.append(" (");
            buf.append(getRawJavaType().getSimpleName());
            if (isReadable()) {
                buf.append(" = [");
                try {
                    buf.append(rawJavaValue());
                } catch (Exception e) {
                    buf.append("<exception occurred>");
                }
                buf.append("]");
            }
            buf.append(")");
        }
        return buf.toString();
    }

    /**
     * Generates a hash-code of this property wrapper definition, based on the
     * java property it wraps and the referenced target object.
     *
     * @return a hash code for this instance
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((propertyDefinition == null) ? 0 : propertyDefinition.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    /**
     * Equality of this property wrapper definition, based on the java property
     * it wraps in a specific class, plus the underlying object reference
     * containing the wrapped property.
     *
     * <p>
     * Two instances are identical if they wrap the same java property (field or
     * bean-property) in the same object instance (by object reference, rather
     * than {@code .equals()} equality).
     *
	 * @param obj obj
     * @return TRUE if this Property wraps the same property on the same
 RowDefinition as the object supplied, FALSE otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Property)) {
            return false;
        }
        Property other = (Property) obj;
        if (propertyDefinition == null) {
            if (other.propertyDefinition != null) {
                return false;
            }
        } else if (!propertyDefinition.equals(other.propertyDefinition)) {
            return false;
        }
        return target == other.target;
    }

    /**
     * Gets the name of the java property, without the containing class name.
     * Mainly used within error messages. e.g.: {@code "uid"}
     *
     * @return a String of the declared field name of this property
     */
    public String javaName() {
        return propertyDefinition.javaName();
    }

    /**
     * Gets the partially qualified name of the underlying java property, using
     * the short-name of the containing class. Mainly used within logging and
     * error messages. eg: {@code "Customer.uid"}
     *
     * @return a convenient String including the class name of the PropertyDefinition and the field name for this property
     */
    public String shortQualifiedJavaName() {
        return propertyDefinition.shortQualifiedJavaName();
    }

    /**
     * Gets the fully qualified name of the underlying java property, including
     * the fully qualified name of the containing class. Mainly used within
     * logging and error messages. e.g.:
     * {@code "nz.co.mycompany.myproject.Customer.uid"}
     *
     * @return the String of the full class name of the containing PropertyDefinition.
     */
    public String qualifiedJavaName() {
        return propertyDefinition.qualifiedJavaName();
    }

    /**
     * Gets the Java-centric type of the property. If a type adaptor is
     * present, then this is the type after conversion from the target object's
     * actual property type.
     *
     * <p>
     * Use {@link #getRawJavaType()} in the rare case that you need to know the
     * underlying java property type.
     *
     * @return the Class of the AdaptableType used internally to handle external values.
     */
    public Class<? extends AdaptableType> type() {
        return propertyDefinition.type();
    }

    /**
     * Convenience method for testing the type of the reference type.
     * Equivalent to {@code refType.isAssignableFrom(this.type())}.
     *
	 * @param refType refType
     * @return TRUE if this property's internal reference type is the similar to that of the supplied instance.
     */
    public boolean isInstanceOf(Class<? extends AdaptableType> refType) {
        return propertyDefinition.isInstanceOf(refType);
    }


    /**
     * Indicates whether the value of the property can be retrieved. Bean
     * properties which are missing a 'getter' can not be read, but may be able
     * to be set.
     *
     * @return TRUE if this property is readable, FALSE otherwise.
     */
    public boolean isReadable() {
        return propertyDefinition.isReadable();
    }

    /**
     * Indicates whether the value of the property can be modified. Bean
     * properties which are missing a 'setter' can not be written to, but may be
     * able to be read.
     *
     * @return TRUE if the property can set, FALSE otherwise.
     */
    public boolean isWritable() {
        return propertyDefinition.isWritable();
    }

    /**
     * Indicates whether the property's type is adapted by an explicit or
     * implicit type adaptor. (Note: at present there is no support for implicit
     * type adaptors)
     *
     * @return {@code true} if a type adaptor is being used
     */
    public boolean isTypeAdapted() {
        return propertyDefinition.isTypeAdapted();
    }

    /**
     * Gets the Java-centric value of the property. The value returned may
     * have undergone type conversion from the target object's actual property
     * type, if a type adaptor is present.
     *
     * <p>
     * Use {@link #isReadable()} beforehand to check whether the property can be
     * read.
     *
	 * @param <A> the AdaptableType type
     * @return The queryableDatatype instance representing this property
     * @throws IllegalStateException if not readable (you should have called
     * isReadable() first)
     * @throws DBThrownByEndUserCodeException if any user code throws an
     * exception
     */
    @SuppressWarnings("unchecked")
    public <A extends AdaptableType> A getAdaptableType() {
        return (A)propertyDefinition.getAdaptableType(target);
    }

    /**
     * Sets the Java-centric value of the property. The value set may have
     * undergone type conversion to the target object's actual property type, if
     * a type adaptor is present.
     *
     * <p>
     * Use {@link #isWritable()} beforehand to check whether the property can be
     * modified.
     *
	 * @param value value
     * @throws IllegalStateException if not writable (you should have called
     * isWritable() first)
     * @throws DBThrownByEndUserCodeException if any user code throws an
     * exception
     */
    public void setAdaptableType(AdaptableType value) {
        propertyDefinition.setAdaptableType(target, value);
    }

    /**
     * Gets the value of the declared property in the end-user's target object,
     * prior to type conversion to the Java-centric type.
     *
     * <p>
     * In most cases you will not need to call this method, as type conversion
     * is done transparently via the {@link #getAdaptableType()} and
     * {@link #setAdaptableType(nz.co.gregs.properties.adapt.AdaptableType)} methods.
     *
     * <p>
     * Use {@link #isReadable()} beforehand to check whether the property can be
     * read.
     *
     * @return value
     * @throws IllegalStateException if not readable (you should have called
     * isReadable() first)
     * @throws DBThrownByEndUserCodeException if any user code throws an
     * exception
     */
    public Object rawJavaValue() {
        return propertyDefinition.rawJavaValue(target);
    }

    /**
     * Set the value of the declared property in the end-user's target object,
     * without type conversion to/from the DBvolution-centric type.
     *
     * <p>
     * In most cases you will not need to call this method, as type conversion
     * is done transparently via the {@link #getAdaptableType()} and
     * {@link #setAdaptableType(nz.co.gregs.properties.adapt.AdaptableType) } methods.
     *
     * <p>
     * Use {@link #isWritable()} beforehand to check whether the property can be
     * modified.
     *
     * @param value new value
     * @throws IllegalStateException if not writable (you should have called
     * isWritable() first)
     * @throws DBThrownByEndUserCodeException if any user code throws an
     * exception
     */
    public void setRawJavaValue(Object value) {
        propertyDefinition.setRawJavaValue(target, value);
    }

    /**
     * Gets the declared type of the property in the end-user's target object,
     * prior to type conversion to the DBvolution-centric type.
     *
     * <p>
     * In most cases you will not need to call this method, as type conversion
     * is done transparently via the {@link #getAdaptableType()} and
     * {@link #setAdaptableType(nz.co.gregs.properties.adapt.AdaptableType) } methods. Use the
     * {@link #type()} method to get the DBv-centric property type, after type
     * conversion.
     *
     * @return the declared Java class of the property.
     */
    public Class<?> getRawJavaType() {
        return propertyDefinition.getRawJavaType();
    }

    /**
     * Gets the definition of the property, independent of any DBRow instance.
     *
     * @return the propertyDefinition
     */
    public PropertyDefinition getDefinition() {
        return propertyDefinition;
    }

    /**
     * Gets the wrapper for the DBRow instance containing this property.
     *
     * @return the PropertyContainerInstance for this property.
     */
    public PropertyContainerWrapper getContainingInstanceWrapper() {
        return dbRowInstanceWrapper;
    }
}
