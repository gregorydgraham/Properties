package nz.co.gregs.properties.adapt;

import nz.co.gregs.properties.exceptions.UnsupportedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import nz.co.gregs.properties.InterfaceInfo;
import nz.co.gregs.properties.JavaProperty;

import nz.co.gregs.properties.exceptions.PropertyException;
import nz.co.gregs.properties.exceptions.DBThrownByEndUserCodeException;
import nz.co.gregs.properties.exceptions.InvalidDeclaredTypeException;

/**
 * Handles annotation processing, business logic, validation rules, defaulting,
 * and error handling associated with the type of a property. This includes
 * processing of the {@link AdaptType} annotation on a property, and type
 * conversion of the property's underlying type.
 *
 * <p>
 * This class handles the majority of the type support logic that is exposed by
 * the {@link DBPropertyDefinition} class, which just delegates to this class.
 *
 * <p>
 * This class behaves correctly when no {@link AdaptType} property is present.
 *
 * @author Malcolm Lett
 */
public abstract class PropertyTypeHandler {

	private JavaProperty javaProperty;
	private Class<? extends AdaptableType> adaptableTypeClass;
	private TypeAdaptor<Object, Object> typeAdaptor;
	private AdaptableTypeSyncer internalQdtSyncer;
	private boolean identityOnly;
	private AdaptType annotation;

	/**
	 *
	 * @param javaProperty the annotated property
	 * @param processIdentityOnly indicates whether property's value needs to be
	 * tracked as well.
	 */
	public PropertyTypeHandler(JavaProperty javaProperty, boolean processIdentityOnly) {
		initialiseHandler(javaProperty, processIdentityOnly);
	}

	public PropertyTypeHandler() {
	}

	public void initialiseHandler(JavaProperty javaProperty1, boolean processIdentityOnly) throws NullPointerException, InvalidDeclaredTypeException {
		this.javaProperty = javaProperty1;
		this.identityOnly = processIdentityOnly;
		this.annotation = javaProperty1.getAnnotation(AdaptType.class);
		Class<?> typeAdaptorClass = null;
		if (getAnnotation() != null) {
			typeAdaptorClass = getAnnotation().value();
		}
		Class<?> typeAdaptorInternalType = null; // DBv-internal
		Class<?> typeAdaptorExternalType = null;
		// validation: must use type adaptor if java property not a QueryableDataType
		if (!AdaptableType.class.isAssignableFrom(javaProperty1.type())) {
			if (getAnnotation() == null) {
				throw new InvalidDeclaredTypeException(javaProperty1.type().getName() + " is not a supported type on " + javaProperty1 + ". " + "Use one of the standard DB types, or use the @" + AdaptType.class.getSimpleName() + " annotation " + "to adapt from a non-standard type.");
			}
		}
		// validation: type adaptor must implement TypeAdaptor interface if used
		if (typeAdaptorClass != null) {
			if (!TypeAdaptor.class.isAssignableFrom(typeAdaptorClass)) {
				throw new InvalidDeclaredTypeException("Type adaptor " + typeAdaptorClass.getName() + " must implement "
						+ TypeAdaptor.class.getSimpleName() + ", on " + javaProperty1);
			}
		}
		// validation: type adaptor must not be an interface or abstract
		if (typeAdaptorClass != null) {
			if (typeAdaptorClass.isInterface()) {
				throw new InvalidDeclaredTypeException("Type adaptor " + typeAdaptorClass.getName()
						+ " must not be an interface, on " + javaProperty1);
			}
			if (Modifier.isAbstract(typeAdaptorClass.getModifiers())) {
				throw new InvalidDeclaredTypeException("Type adaptor " + typeAdaptorClass.getName()
						+ " must not be abstract, on " + javaProperty1);
			}
		}
		// validation: type adaptor must use only acceptable styles of generics
		// (note: rule de-activates if InterfaceInfo can't handle the class,
		//   or if other assumptions are broken.
		//   This is intentional to future-proof and because generics of type
		//   hierarchies is tremendously complex and its process very prone to error.)
		if (typeAdaptorClass != null) {
			InterfaceInfo.ParameterBounds[] parameterBounds = null;
			try {
				InterfaceInfo interfaceInfo = new InterfaceInfo(TypeAdaptor.class, typeAdaptorClass);
				parameterBounds = interfaceInfo.getInterfaceParameterValueBounds();
			} catch (UnsupportedOperationException dropped) {
				// bumped into generics that can't be handled, so best to give the
				// end-user the benefit of doubt and just skip the validation
//                logger.debug("Cancelled validation on type adaptor " + typeAdaptorClass.getName()
//                        + " due to internal error: " + dropped.getMessage(), dropped);
			}
			if (parameterBounds != null && parameterBounds.length == 2) {
				if (parameterBounds[0].isUpperMulti()) {
					throw new InvalidDeclaredTypeException("Type adaptor " + typeAdaptorClass.getName() + " must not be"
							+ " declared with multiple super types for type variables"
							+ ", on " + javaProperty1);
				}
				if (parameterBounds[1].isUpperMulti()) {
					throw new InvalidDeclaredTypeException("Type adaptor " + typeAdaptorClass.getName() + " must not be"
							+ " declared with multiple super types for type variables"
							+ ", on " + javaProperty1);
				}
				try {
					typeAdaptorExternalType = parameterBounds[0].upperClass();
				} catch (UnsupportedType e) {
					// rules dependent on this attribute will be disabled
				}
				try {
					typeAdaptorInternalType = parameterBounds[1].upperClass();
				} catch (UnsupportedType e) {
					// rules dependent on this attribute will be disabled
				}
			}
		}
		// validation: Type adaptor's external type must not be a QDT.
		if (typeAdaptorExternalType != null) {
			if (AdaptableType.class.isAssignableFrom(typeAdaptorExternalType)) {
				throw new InvalidDeclaredTypeException("Type adaptor's external type must not be a " + AdaptableType.class.getSimpleName()
						+ ", on " + javaProperty1);
			}
		}
		// validation: Type adaptor's internal type must not be a QDT.
		if (typeAdaptorInternalType != null) {
			if (AdaptableType.class.isAssignableFrom(typeAdaptorInternalType)) {
				throw new InvalidDeclaredTypeException("Type adaptor's internal type must not be a " + AdaptableType.class.getSimpleName()
						+ ", on " + javaProperty1);
			}
		}
		// validation: explicit external type must be a QDT and must not be abstract or an interface
		if (getAnnotation() != null && explicitTypeOrNullOf(getAnnotation()) != null) {
			Class<?> explicitQDTType = explicitTypeOrNullOf(getAnnotation());
			if (!AdaptableType.class.isAssignableFrom(explicitQDTType)) {
				throw new InvalidDeclaredTypeException("@DB" + AdaptType.class.getSimpleName() + "(type) on " + javaProperty1 + " is not a supported type. " + "Use one of the standard DB types.");
			}
			if (Modifier.isAbstract(explicitQDTType.getModifiers()) || Modifier.isInterface(explicitQDTType.getModifiers())) {
				throw new InvalidDeclaredTypeException("@DB" + AdaptType.class.getSimpleName()
						+ "(type) must be a concrete type"
						+ ", on " + javaProperty1);
			}
		}
		// validation: Type adaptor's external type must be either:
		//   a) castable to the external property type (and not a QDT), or
		//   b) a simple type that is supported by the external property type,
		//      and the external property type must be a QDT
		// (note: in either case can't be a QDT itself due to rules above)
		if (typeAdaptorExternalType != null && !AdaptableType.class.isAssignableFrom(javaProperty1.type())) {
			if (!javaProperty1.type().equals(typeAdaptorExternalType) && SafeOneWaySimpleTypeAdaptor.getSimpleCastFor(javaProperty1.type(), typeAdaptorExternalType) == null) {
				throw new InvalidDeclaredTypeException("Type adaptor's external " + typeAdaptorExternalType.getSimpleName()
						+ " type is not compatible with the property type, on " + javaProperty1);
			}
		}
		if (typeAdaptorExternalType != null && AdaptableType.class.isAssignableFrom(javaProperty1.type())) {
			Class<? extends AdaptableType> explicitQDTType = (Class<? extends AdaptableType>) javaProperty1.type();
			Class<?> inferredQDTType = inferredAdaptableTypeForSimpleType(typeAdaptorExternalType);
			if (inferredQDTType == null) {
				throw new InvalidDeclaredTypeException("Type adaptor's external " + typeAdaptorExternalType.getSimpleName()
						+ " type is not a supported simple type, on " + javaProperty1);
			} else if (!isSimpleTypeSupportedByAdaptableType(typeAdaptorExternalType, explicitQDTType)) {
				throw new InvalidDeclaredTypeException("Type adaptor's external " + typeAdaptorExternalType.getSimpleName()
						+ " type is not compatible with a " + explicitQDTType.getSimpleName()
						+ " property, on " + javaProperty1);
			}
		}
		// validation: Type adaptor's internal type must be either:
		//   a) a simple type that implies an internal QDT type,
		//      and no explicit QDT type is specified, or
		//   b) a simple type that is supported by the explicit internal QDT type,
		//      and the explicit internal QDT type is specified
		// (note: in either case can't be a QDT itself due to rule above)
		if (typeAdaptorInternalType != null && explicitTypeOrNullOf(getAnnotation()) == null) {
			Class<?> inferredQDTType = inferredAdaptableTypeForSimpleType(typeAdaptorInternalType);
			if (inferredQDTType == null) {
				throw new InvalidDeclaredTypeException("Type adaptor's internal " + typeAdaptorInternalType.getSimpleName()
						+ " type is not a supported simple type, on " + javaProperty1);
			}
		}
		if (typeAdaptorInternalType != null && explicitTypeOrNullOf(getAnnotation()) != null) {
			Class<? extends AdaptableType> explicitQDTType = explicitTypeOrNullOf(getAnnotation());
			Class<?> inferredQDTType = inferredAdaptableTypeForSimpleType(typeAdaptorInternalType);
			if (inferredQDTType == null) {
				throw new InvalidDeclaredTypeException("Type adaptor's internal " + typeAdaptorInternalType.getSimpleName()
						+ " type is not a supported simple type, on " + javaProperty1);
			} else if (!isSimpleTypeSupportedByAdaptableType(typeAdaptorInternalType, explicitQDTType)) {
				throw new InvalidDeclaredTypeException("Type adaptor's internal " + typeAdaptorInternalType.getSimpleName()
						+ " type is not compatible with " + explicitQDTType.getSimpleName()
						+ ", on " + javaProperty1);
			}
		}
		// populate everything
		if (getAnnotation() == null) {
			// populate when no annotation
			this.typeAdaptor = null;
			this.adaptableTypeClass = (Class<? extends AdaptableType>) javaProperty1.type();
			this.internalQdtSyncer = null;
		} else if (isIdentityOnly()) {
			// populate identity-only information when type adaptor declared
			Class<? extends AdaptableType> type = explicitTypeOrNullOf(getAnnotation());
			if (type == null && typeAdaptorInternalType != null) {
				type = inferredAdaptableTypeForSimpleType(typeAdaptorInternalType);
			}
			if (type == null) {
				throw new NullPointerException("null dbvPropertyType, this is an internal bug");
			}
			this.adaptableTypeClass = type;

			this.typeAdaptor = null;
			this.internalQdtSyncer = null;
		} else {
			// initialise type adapting
			this.typeAdaptor = newTypeAdaptorInstanceGiven(javaProperty1, getAnnotation());
			Class<? extends AdaptableType> type = explicitTypeOrNullOf(getAnnotation());
			if (type == null && typeAdaptorInternalType != null) {
				type = inferredAdaptableTypeForSimpleType(typeAdaptorInternalType);
			}
			if (type == null) {
				throw new NullPointerException("null dbvPropertyType, this is an internal bug");
			}
			this.adaptableTypeClass = type;
			Class<?> internalLiteralType = literalTypeOf(type);
			Class<?> externalLiteralType;
			if (AdaptableType.class.isAssignableFrom(javaProperty1.type())) {
				externalLiteralType = literalTypeOf((Class<? extends AdaptableType>) javaProperty1.type());
			} else {
				externalLiteralType = javaProperty1.type();
			}
			if (AdaptableType.class.isAssignableFrom(javaProperty1.type())) {
				this.internalQdtSyncer = new AdaptableTypeSyncer(javaProperty1.qualifiedName(), this.getAdaptableTypeClass(), internalLiteralType, externalLiteralType, this.getTypeAdaptor());
			} else {
				this.internalQdtSyncer = new SimpleValueAdaptableTypeSyncer(javaProperty1.qualifiedName(), this.getAdaptableTypeClass(), internalLiteralType, externalLiteralType, this.getTypeAdaptor());
			}
		}
	}

	/**
	 * Infers the QDT-type that corresponds to the given simple type. Used to
	 * infer the QDT-type that should be used internally, based on the type
	 * supplied by the type adaptor.
	 *
	 * <p>
	 * Make sure to keep this in sync with {@link #literalTypeOf}.
	 *
	 * @param simpleType
	 * @return
	 */
	// FIXME: change to require exact matches, rather than 'instance of'
	public abstract Class<? extends AdaptableType> inferredAdaptableTypeForSimpleType(Class<?> simpleType);

	/**
	 *
	 * <p>
	 * Make sure to keep this in sync with
	 * {@link #inferredAdaptableTypeForSimpleType}.
	 *
	 * @param type
	 * @return
	 */
	public abstract Class<?> literalTypeOf(Class<? extends AdaptableType> type);

	/**
	 * Tests whether the simpleType is supported by the given QDT-type. A simple
	 * type is supported by the QDT type iff the simple type implies a QDT-type,
	 * and:
	 * <ul>
	 * <li> the implied QDT-type is exactly the same as the given QDT-type, or
	 * <li> the implied QDT-type (eg: DBInteger) is instance-of assignable to the
	 * given QDT-type (eg: DBNumber), or
	 * <li> the implied QDT-type (eg: DBDate) is a super-class of the given given
	 * QDT-type (eg: DBSpecialDate).
	 * </ul>
	 *
	 *
	 *
	 * @param simpleType
	 * @param adaptableType
	 * @return
	 */
	public boolean isSimpleTypeSupportedByAdaptableType(
			Class<?> simpleType,
			Class<? extends AdaptableType> adaptableType) {
		Class<?> inferredQDTType = this.inferredAdaptableTypeForSimpleType(simpleType);
		if (inferredQDTType != null) {
			if (adaptableType.isAssignableFrom(inferredQDTType) || inferredQDTType.isAssignableFrom(adaptableType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Internal helper to support the way annotation attribute defaulting works.
	 *
	 *
	 * @return
	 */
	private static Class<? extends AdaptableType> explicitTypeOrNullOf(AdaptType annotation) {
		if (annotation == null) {
			return null;
		}

		// detect default
		if (annotation.type().equals(AdaptableType.class)) {
			return null;
		}

		// return value
		return annotation.type();
	}

	/**
	 * Gets the DBv-centric type of the property, possibly after type adaption.
	 *
	 * @return
	 */
	public Class<? extends AdaptableType> getType() {
		return getAdaptableTypeClass();
	}

	/**
	 * Indicates whether the property's type is adapted by an explicit or implicit
	 * type adaptor. (Note: at present there is no support for implicit type
	 * adaptors)
	 *
	 * @return
	 */
	public boolean isTypeAdapted() {
		return (getAnnotation() != null);
	}

	/**
	 * Gets the DBv-centric value from the underlying java property, converting if
	 * needed. This method behaves correctly regardless of whether an
	 * {@link AdaptType} annotation is present.
	 *
	 * @param target object containing the property
	 * @return the DBv-centric property value
	 * @throws DBThrownByEndUserCodeException if any user code throws an exception
	 * @throws IllegalStateException if the underlying java property is not
	 * readable
	 */
	public AdaptableType getJavaPropertyAsAdaptableType(Object target) {
		if (isIdentityOnly()) {
			throw new AssertionError("Attempt to read value from identity-only property");
		}

		// get via type adaptor and simple-type java property
		if (getTypeAdaptor() != null && getInternalQdtSyncer() instanceof SimpleValueAdaptableTypeSyncer) {
			SimpleValueAdaptableTypeSyncer syncer = (SimpleValueAdaptableTypeSyncer) getInternalQdtSyncer();
			Object externalValue = getJavaProperty().get(target);

            // convert
			// TODO think this still needs some last-minute type checks
			return syncer.setInternalTypeFromExternalSimpleValue(externalValue);
		} // get via type adaptor and QDT java property
		else if (getTypeAdaptor() != null) {
			Object externalValue = getJavaProperty().get(target);

			// this should be completely safe by now
			AdaptableType externalQdt = (AdaptableType) externalValue;

			// convert
			return getInternalQdtSyncer().setInternalQDTFromExternalQDT(externalQdt);
		} // get directly without type adaptor
		// (note: type checking was performed at creation time)
		else {
			return (AdaptableType) getJavaProperty().get(target);
		}
	}

	/**
	 * Sets the underlying java property according to the given DBv-centric value.
	 * This method behaves correctly regardless of whether an {@link AdaptType}
	 * annotation is present.
	 *
	 * @param target object containing the property
	 * @param dbvValue
	 *
	 * @throws DBThrownByEndUserCodeException if any user code throws an exception
	 * @throws IllegalStateException if the underlying java property is not
	 * writable
	 */
	public void setJavaPropertyAsAdaptableType(Object target, AdaptableType dbvValue) {
		if (isIdentityOnly()) {
			throw new AssertionError("Attempt to write value to identity-only property");
		}

		// set via type adaptor and simple-type java property
		if (getTypeAdaptor() != null && getInternalQdtSyncer() instanceof SimpleValueAdaptableTypeSyncer) {
			SimpleValueAdaptableTypeSyncer syncer = (SimpleValueAdaptableTypeSyncer) getInternalQdtSyncer();
			syncer.setInternalQueryableDatatype(dbvValue);
			Object externalValue = syncer.getExternalSimpleValueFromInternalQDT();

			// TODO think this still needs some last-minute type checks
			getJavaProperty().set(target, externalValue);
		} // set via type adaptor and QDT java property
		else if (getTypeAdaptor() != null) {
			Object externalValue = getJavaProperty().get(target);

			// this should be completely safe by now
			AdaptableType externalQdt = (AdaptableType) externalValue;

			// convert
			getInternalQdtSyncer().setInternalQueryableDatatype(dbvValue);
			externalQdt = getInternalQdtSyncer().setExternalFromInternalQDT(externalQdt);
			if (externalQdt == null && externalValue != null) {
				getJavaProperty().set(target, null);
			}
		} // set directly without type adaptor
		// (note: type checking was performed at creation time)
		else {
			getJavaProperty().set(target, dbvValue);
		}
	}

	/**
	 * Constructs a new instanceof the type adaptor referenced by the given
	 * annotation instance. Handles all exceptions and throws them as the
	 * appropriate runtime exceptions
	 *
	 *
	 *
	 * @return
	 * @throws PropertyException on unexpected internal errors, and
	 * @throws InvalidDeclaredTypeException on errors with the end-user supplied
	 * code
	 */
	private static TypeAdaptor<Object, Object> newTypeAdaptorInstanceGiven(JavaProperty property, AdaptType annotation) {
		Class<? extends TypeAdaptor<?, ?>> adaptorClass = annotation.value();
		if (adaptorClass == null) {
			// shouldn't be possible
			throw new PropertyException("Encountered unexpected null " + AdaptType.class.getSimpleName()
					+ ".adptor() (probably a bug in DBvolution)");
		}

		if (adaptorClass.isInterface()) {
			throw new InvalidDeclaredTypeException("TypeAdaptor cannot be an interface (" + adaptorClass.getSimpleName()
					+ "), on property " + property.qualifiedName());
		}
		if (Modifier.isAbstract(adaptorClass.getModifiers())) {
			throw new InvalidDeclaredTypeException("TypeAdaptor cannot be an abstract class (" + adaptorClass.getSimpleName()
					+ "), on property " + property.qualifiedName());
		}

		try {
			adaptorClass.newInstance();
		} catch (InstantiationException e) {
			throw new InvalidDeclaredTypeException("Type adaptor " + adaptorClass.getName()
					+ " could not be constructed, on property "
					+ property.qualifiedName() + ": " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new InvalidDeclaredTypeException("Type adaptor " + adaptorClass.getName()
					+ " could not be constructed, on property "
					+ property.qualifiedName() + ": " + e.getMessage(), e);
		}

		// get default constructor
		Constructor<? extends TypeAdaptor<?, ?>> constructor;
		try {
			constructor = adaptorClass.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new InvalidDeclaredTypeException("Type adaptor " + adaptorClass.getName()
					+ " has no default constructor, on property "
					+ property.qualifiedName(), e);
		} catch (SecurityException e) {
            // caused by a Java security manager or an attempt to access a non-visible field
			// without first making it visible
			throw new PropertyException("Java security error retrieving constructor for " + adaptorClass.getName()
					+ ", referenced by property " + property.qualifiedName() + ": " + e.getLocalizedMessage(), e);
		}

		// construct adaptor instance
		TypeAdaptor<?, ?> instance;
		try {
			instance = constructor.newInstance();
		} catch (InstantiationException e) {
			throw new InvalidDeclaredTypeException(adaptorClass.getName() + " cannot be constructed (it is probably abstract), referenced by property "
					+ property.qualifiedName(), e);
		} catch (IllegalAccessException e) {
            // caused by a Java security manager or an attempt to access a non-visible field
			// without first making it visible
			throw new PropertyException("Java security error instantiating " + adaptorClass.getName()
					+ ", referenced by property " + property.qualifiedName() + ": " + e.getLocalizedMessage(), e);
		} catch (IllegalArgumentException e) {
			// expected, so probably represents a bug
			throw new IllegalArgumentException("Internal error instantiating "
					+ adaptorClass.getName() + ", referenced by property " + property.qualifiedName() + ": " + e.getLocalizedMessage(), e);
		} catch (InvocationTargetException e) {
			// any checked or runtime exception thrown by the setter method itself
			Throwable cause = (e.getCause() == null) ? e : e.getCause();
			String msg = (cause.getLocalizedMessage() == null) ? "" : ": " + cause.getLocalizedMessage();
			throw new DBThrownByEndUserCodeException("Constructor threw " + cause.getClass().getSimpleName() + " when instantiating "
					+ adaptorClass.getName() + ", referenced by property " + property.qualifiedName() + msg, cause);
		}

        // downcast
		// (technically the instance is for <?,? extends QueryableDataType> but
		//  that can't be used reflectively when all we know is Object and QueryableDataType)
		@SuppressWarnings("unchecked")
		TypeAdaptor<Object, Object> result = (TypeAdaptor<Object, Object>) instance;
		return result;
	}

	/**
	 * @return the identityOnly
	 */
	public boolean isIdentityOnly() {
		return identityOnly;
	}

	/**
	 * @return the javaProperty
	 */
	public JavaProperty getJavaProperty() {
		return javaProperty;
	}

	/**
	 * @return the adaptableTypeClass
	 */
	public Class<? extends AdaptableType> getAdaptableTypeClass() {
		return adaptableTypeClass;
	}

	/**
	 * @return the typeAdaptor
	 */
	public TypeAdaptor<Object, Object> getTypeAdaptor() {
		return typeAdaptor;
	}

	/**
	 * @return the internalQdtSyncer
	 */
	public AdaptableTypeSyncer getInternalQdtSyncer() {
		return internalQdtSyncer;
	}

	/**
	 * @return the annotation
	 */
	public AdaptType getAnnotation() {
		return annotation;
	}
}
