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
 * Handles annotation processing, business logic, validation rules, defaulting, and error handling associated with the type of a property. This includes
 * processing of the {@link AdaptType} annotation on a property, and type conversion of the property's underlying type.
 *
 * <p>
 * This class handles the majority of the type support logic that is exposed by the {@link nz.co.gregs.properties.PropertyDefinition} class, which just
 * delegates to this class.
 *
 * <p>
 * This class behaves correctly when no {@link AdaptType} property is present.
 *
 * @author Malcolm Lett
 * @author Gregory Graham
 */
public abstract class PropertyTypeHandler {

  private JavaProperty javaProperty;
  private Class<? extends AdaptableType> adaptableTypeClass;
  private TypeAdaptor<Object, Object> typeAdaptor;
  private AdaptableTypeSyncer internalAdaptableTypeSyncer;
  private boolean identityOnly;
  private AdaptType annotation;

  /**
   * Create a new PropertyTypeHandler.
   *
   * @param javaProperty the annotated property
   * @param processIdentityOnly indicates that Property is not to track the value and only process the identity.
   */
  public PropertyTypeHandler(JavaProperty javaProperty, boolean processIdentityOnly) {
    initialiseHandler(javaProperty, processIdentityOnly);
  }

  /**
   * Default constructor
   *
   */
  public PropertyTypeHandler() {
  }

  /**
   * Used to correctly prepare a handler.
   * 
   * @param javaProperty1 the property to track
   * @param processIdentityOnly whether the property is identity or identity+value
   * @throws NullPointerException thrown if Properties can't obtain the annotation
   * @throws InvalidDeclaredTypeException javaProperty is not an AdaptableType or correctly annotated
   */
  public final void initialiseHandler(JavaProperty javaProperty1, boolean processIdentityOnly) throws NullPointerException, InvalidDeclaredTypeException {
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
        throw new InvalidDeclaredTypeException(javaProperty1.type().getName() + " is not a supported type on " + javaProperty1 + ". " + "Use an AdaptableType extension, or use the @" + AdaptType.class.getSimpleName() + " annotation " + "to adapt from a non-standard type.");
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
      this.internalAdaptableTypeSyncer = null;
    } else if (isIdentityOnly()) {
      // populate identity-only information when type adaptor declared
      Class<? extends AdaptableType> type = explicitTypeOrNullOf(getAnnotation());
      if (type == null && typeAdaptorInternalType != null) {
        type = inferredAdaptableTypeForSimpleType(typeAdaptorInternalType);
      }
      if (type == null) {
        throw new NullPointerException("null PropertyType, this is an internal bug");
      }
      this.adaptableTypeClass = type;

      this.typeAdaptor = null;
      this.internalAdaptableTypeSyncer = null;
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
        this.internalAdaptableTypeSyncer = new AdaptableTypeSyncer(javaProperty1.qualifiedName(), this.getAdaptableTypeClass(), internalLiteralType, externalLiteralType, this.getTypeAdaptor());
      } else {
        this.internalAdaptableTypeSyncer = new SimpleValueAdaptableTypeSyncer(javaProperty1.qualifiedName(), this.getAdaptableTypeClass(), internalLiteralType, externalLiteralType, this.getTypeAdaptor());
      }
    }
  }

  /**
   * Infers the AdaptableType-type that corresponds to the given simple type. Used to infer the AdaptableType-type that should be used internally, based on the
   * type supplied by the type adaptor.
   *
   * <p>
   * Given a value A of class B, this method will return an AdaptableType class C that can store value A</p>
   *
   * <p>
   * Make sure to keep this in sync with {@link #literalTypeOf}.</p>
   * <p>
   * for an AdaptableType class A&lt;B&gt; inferredAdaptableTypeForSimpleType(B.class) should return A.class</p>
   *
   * <p>
   * Importantly {@link #inferredAdaptableTypeForSimpleType(java.lang.Class) } and {@link #literalTypeOf(java.lang.Class) } work together to maintain the
   * typesafety of Properties. so it should always be true that literalTypeOf(inferredAdaptableTypeForSimpleType(B.class)).equals(B.class) and
   * inferredAdaptableTypeForSimpleType(literalTypeOf(A.class)).equals(A.class)</p>
   *
   *
   * @param simpleType a class that needs to be stored in an AdaptableType
   * @return an AdaptableType that can store simpleType classes
   */
  public abstract Class<? extends AdaptableType> inferredAdaptableTypeForSimpleType(Class<?> simpleType);

  /**
   * Returns the literal type of the AdaptableType Class supplied.
   *
   * <p>
   * Make sure to keep this in sync with {@link #inferredAdaptableTypeForSimpleType}.</p>
   *
   * <p>
   * for an AdaptableType class A&lt;B&gt; literalTypeOf(A.class) should return B.class</p>
   *
   * <p>
   * Importantly {@link #inferredAdaptableTypeForSimpleType(java.lang.Class) } and {@link #literalTypeOf(java.lang.Class) } work together to maintain the
   * typesafety of Properties. so it should always be true that literalTypeOf(inferredAdaptableTypeForSimpleType(B.class)).equals(B.class) and
   * inferredAdaptableTypeForSimpleType(literalTypeOf(A.class)).equals(A.class)</p>
   *
   * @param type an AdaptableType for which the literal type is required
   * @return the class of the literal type
   */
  public abstract Class<?> literalTypeOf(Class<? extends AdaptableType> type);

  /**
   * Tests whether the simpleType is supported by the given adaptableType. A simple type is supported by the adaptableType iff the simple type implies an
   * AdaptableType, and:
   * <ul>
   * <li> the implied AdaptableType is exactly the same as the given adaptableType, or
   * <li> the implied AdaptableType (e.g. IntegerProperty) is instance-of assignable to the given adaptableType (e.g. NumberProperty), or
   * <li> the implied AdaptableType (e.g. DateTimeProperty) is a super-class of the given adaptableType (e.g. DateProperty).
   * </ul>
   *
   *
   *
   * @param simpleType the class of the value to store
   * @param adaptableType the class of the AdaptableType to store the value in
   * @return true if the AdaptableType will store this simpleType
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
   * @return annotation.type() or null
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
   * Gets the AdaptableType-centric type of the property, possibly after type adaption.
   *
   * @return the Class used internally by AdaptableType to hold the value
   */
  public Class<? extends AdaptableType> getType() {
    return getAdaptableTypeClass();
  }

  /**
   * Indicates whether the property's type is adapted by an explicit or implicit type adaptor. (Note: at present there is no support for implicit type adaptors)
   *
   * @return return true if this Property is adapted, false otherwise
   */
  public boolean isTypeAdapted() {
    return (getAnnotation() != null);
  }

  /**
   * Gets the AdaptableType-centric value from the underlying java property, converting if needed. This method behaves correctly regardless of whether an
   * {@link AdaptType} annotation is present.
   *
   * @param target object containing the property
   * @return the AdaptableType-centric property value
   * @throws DBThrownByEndUserCodeException if any user code throws an exception
   * @throws IllegalStateException if the underlying java property is not readable
   */
  public AdaptableType getJavaPropertyAsAdaptableType(Object target) {
    if (isIdentityOnly()) {
      throw new AssertionError("Attempt to read value from identity-only property");
    }

    // get via type adaptor and simple-type java property
    if (getTypeAdaptor() != null && getInternalAdaptableTypeSyncer() instanceof SimpleValueAdaptableTypeSyncer) {
      SimpleValueAdaptableTypeSyncer syncer = (SimpleValueAdaptableTypeSyncer) getInternalAdaptableTypeSyncer();
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
      return getInternalAdaptableTypeSyncer().setInternalFromExternal(externalQdt);
    } // get directly without type adaptor
    // (note: type checking was performed at creation time)
    else {
      return (AdaptableType) getJavaProperty().get(target);
    }
  }

  /**
   * Sets the underlying java property according to the given AdaptableType-centric value. This method behaves correctly regardless of whether an
   * {@link AdaptType} annotation is present.
   *
   * @param target object containing the property
   * @param source source value as an AdaptableType
   *
   * @throws DBThrownByEndUserCodeException if any user code throws an exception
   * @throws IllegalStateException if the underlying java property is not writable
   */
  public void setJavaPropertyAsAdaptableType(Object target, AdaptableType source) {
    if (isIdentityOnly()) {
      throw new AssertionError("Attempt to write value to identity-only property");
    }

    // set via type adaptor and simple-type java property
    if (getTypeAdaptor() != null && getInternalAdaptableTypeSyncer() instanceof SimpleValueAdaptableTypeSyncer) {
      SimpleValueAdaptableTypeSyncer syncer = (SimpleValueAdaptableTypeSyncer) getInternalAdaptableTypeSyncer();
      syncer.setInternalAdaptableType(source);
      Object externalValue = syncer.getExternalSimpleValueFromInternalAdaptableType();

      // TODO think this still needs some last-minute type checks
      getJavaProperty().set(target, externalValue);
    } // set via type adaptor and QDT java property
    else if (getTypeAdaptor() != null) {
      Object externalValue = getJavaProperty().get(target);

      // this should be completely safe by now
      AdaptableType externalAdaptableType = (AdaptableType) externalValue;

      // convert
      getInternalAdaptableTypeSyncer().setInternalAdaptableType(source);
      externalAdaptableType = getInternalAdaptableTypeSyncer().setExternalFromInternal(externalAdaptableType);
      if (externalAdaptableType == null && externalValue != null) {
        getJavaProperty().set(target, null);
      }
    } // set directly without type adaptor
    // (note: type checking was performed at creation time)
    else {
      getJavaProperty().set(target, source);
    }
  }

  /**
   * Constructs a new instance of the type adaptor referenced by the given annotation instance. Handles all exceptions and throws them as the appropriate runtime
   * exceptions
   *
   *
   *
   * @return a new instance of the type adaptor
   * @throws PropertyException on unexpected internal errors, and
   * @throws InvalidDeclaredTypeException on errors with the end-user supplied code
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
   * Indicates if the Property is an IdentityOnly Property or not.
   * 
   * <p>IndentityOnly Properties do not track the value of the property.</p>
   * 
   * @return returns true if the Property's value is not tracked
   */
  public boolean isIdentityOnly() {
    return identityOnly;
  }

  /**
   * Returns the Java property.
   * 
   * @return the javaProperty
   */
  public JavaProperty getJavaProperty() {
    return javaProperty;
  }

  /**
   * Returns class of the AdaptableType.
   * 
   * @return the adaptableType Class
   */
  public Class<? extends AdaptableType> getAdaptableTypeClass() {
    return adaptableTypeClass;
  }

  /**
   * Returns the TypeAdaptor
   * 
   * @return the typeAdaptor
   */
  public TypeAdaptor<Object, Object> getTypeAdaptor() {
    return typeAdaptor;
  }

  /**
   * Returns the internal AdaptableTypeSyncer
   * 
   * @return the internalAdaptableTypeSyncer
   */
  public AdaptableTypeSyncer getInternalAdaptableTypeSyncer() {
    return internalAdaptableTypeSyncer;
  }

  /**
   * Returns the annotation.
   * 
   * @return the annotation
   */
  public AdaptType getAnnotation() {
    return annotation;
  }
}
