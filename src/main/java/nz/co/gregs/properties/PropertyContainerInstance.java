package nz.co.gregs.properties;

import java.util.ArrayList;
import java.util.List;

import nz.co.gregs.properties.exceptions.PropertyException;

/**
 * Wraps a specific target object according to its type's
 * {@link PropertyContainerClass}.
 *
 * <p>
 * To create instances of this type, call
 * {@link RowDefinitionWrapperFactory#instanceWrapperFor(nz.co.gregs.dbvolution.query.RowDefinition)}
 * on the appropriate {@link RowDefinition}.
 *
 * <p>
 * Instances of this class are lightweight and efficient to create, and they are
 * intended to be short lived. Instances of this class must not be shared
 * between different DBDatabase instances, however they can be safely associated
 * within a single DBDatabase instance.
 *
 * <p>
 * Instances of this class are <i>thread-safe</i>.
 *
 * @author Malcolm Lett
 */
public class PropertyContainerInstance {

    private final PropertyContainerClass containingClassWrapper;
    private final PropertyContainer containingInstance;
    private final List<Property> allProperties;

    /**
     * Called by
     * {@link DBRowClassWrapper#instanceAdaptorFor(DBDefinition, Object)}.
     *
     
     * @param rowDefinition the target object of the same type as analyzed by {@code classWrapper}
     */
    PropertyContainerInstance(PropertyContainerClass classWrapper, PropertyContainer rowDefinition) {
        if (rowDefinition == null) {
            throw new PropertyException("Target object is null");
        }
        if (!classWrapper.adapteeClass().isInstance(rowDefinition)) {
            throw new PropertyException("Target object's type (" + rowDefinition.getClass().getName()
                    + ") is not compatible with given class adaptor for type " + classWrapper.adapteeClass().getName()
                    + " (this is probably a bug in DBvolution)");
        }

        this.containingInstance = rowDefinition;
        this.containingClassWrapper = classWrapper;

        // pre-cache commonly used things
        // (note: if you change this to use lazy-initialisation, you'll have to
        // add explicit synchronisation, or it won't be thread-safe anymore)
        this.allProperties = new ArrayList<Property>();
        for (PropertyDefinition propertyDefinition : classWrapper.getPropertyDefinitions()) {
            this.allProperties.add(new Property(this, propertyDefinition, rowDefinition));
        }
    }

    /**
     * Gets a string representation suitable for debugging.
     *
     * @return a String representing this object sufficient for debugging
     * purposes
     */
    @Override
    public String toString() {
            return getClass().getSimpleName() + "<wrapping:" + containingClassWrapper.adapteeClass().getName() + ">";
    }
    
	/**
	 * Two {@code RowDefinitionInstanceWrappers} are equal if they wrap two {@code RowDefinition} instances
	 * that are themselves equal, and are instances of the same class.
	 * @param obj the other object to compare to.
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
		if (!(obj instanceof PropertyContainerInstance)) {
			return false;
		}
		PropertyContainerInstance other = (PropertyContainerInstance) obj;
		if (containingClassWrapper == null) {
			if (other.containingClassWrapper != null) {
				return false;
			}
		} else if (!containingClassWrapper.equals(other.containingClassWrapper)) {
			return false;
		}
		if (containingInstance == null) {
			if (other.containingInstance != null) {
				return false;
			}
		} else if (!containingInstance.equals(other.containingInstance)) {
			return false;
		}
		return true;
	}

	/**
	 * Calculates the hash-code based on the hash-code of the wrapped @{code RowDefinition}
	 * instance and its class.
	 * @return the hash-code
	 */
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containingClassWrapper == null) ? 0 : containingClassWrapper.hashCode());
		result = prime * result + ((containingInstance == null) ? 0 : containingInstance.hashCode());
		return result;
	}
	
    /**
     * Gets the class-wrapper for the class of wrapped {@code RowDefinition}
     * @return the class-wrapper
     */
	public PropertyContainerClass getContainingClassWrapper() {
        return containingClassWrapper;
    }

    /**
     * Gets the wrapped object type supported by this {@code ObjectAdaptor}.
     * Note: this should be the same as the wrapped object's actual type.
     *
     * @return the class of the wrapped instance
     */
    public Class<? extends PropertyContainer> adapteeContainingClass() {
        return containingClassWrapper.adapteeClass();
    }

    /**
     * Gets the {@link RowDefinition} instance wrapped by this
     * {@code ObjectAdaptor}.
     *
     * @return the {@link RowDefinition} (usually a {@link DBRow} or
     * {@link DBReport}) for this instance.
     */
    public PropertyContainer adapteeContainingInstance() {
        return containingInstance;
    }

    /**
     * Gets the simple name of the class being wrapped by this adaptor.
     * <p>
     * Use {@link #tableName()} for the name of the table mapped to this class.
     *
     * @return the simple class name of the wrapped RowDefinition
     */
    public String javaName() {
        return containingClassWrapper.javaName();
    }

    /**
     * Gets the fully qualified name of the class being wrapped by this adaptor.
     * <p>
     * Use {@link #tableName()} for the name of the table mapped to this class.
     *
     * @return the full class name of the wrapped RowDefinition
     */
    public String qualifiedJavaName() {
        return containingClassWrapper.qualifiedJavaName();
    }

    /**
     * Gets the property by its java field name.
     * <p>
     * Only provides access to properties annotated with {@code DBColumn}.
     *
	 * @param propertyName propertyName
     * @return property of the wrapped {@link RowDefinition} associated with the java field name supplied.
	 *         Null if no such property is found.
     */
    public Property getPropertyByName(String propertyName) {
        PropertyDefinition classProperty = containingClassWrapper.getPropertyDefinitionByName(propertyName);
        return (classProperty == null) ? null : new Property(this, classProperty, containingInstance);
    }

    /**
     * Gets all properties that are annotated with {@code DBColumn}. This method
     * is intended for where you need to get/set property values on all
     * properties in the class.
     *
     * <p>
     * Note: if you wish to iterate over the properties and only use their
     * definitions (ie: meta-information), this method is not efficient. Use
     * {@link #getPropertyDefinitions()} instead in that case.
     *
     * @return the non-null list of properties, empty if none
     */
    public List<Property> getPropertyWrappers() {
        return allProperties;
    }

    /**
     * Gets all property definitions that are annotated with {@code DBColumn}.
     * This method is intended for where you need to examine meta-information
     * about all properties in a class.
     *
     * @return a list of PropertyWrapperDefinitions for the PropertyWrappers of this RowDefinition
     */
    public List<PropertyDefinition> getPropertyDefinitions() {
        return containingClassWrapper.getPropertyDefinitions();
    }
}
