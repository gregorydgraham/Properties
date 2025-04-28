/*
 * Copyright 2013 Gregory Graham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.gregs.properties.adapt;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.co.gregs.properties.exceptions.PropertyException;
import nz.co.gregs.properties.adapt.SafeOneWaySimpleTypeAdaptor.Direction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allows synchronizations to be done between two QueryableDatatypes, based on a Type Adaptor.
 *
 * @author Malcolm Lett
 */
public class AdaptableTypeSyncer {

  private static final Log log = LogFactory.getLog(AdaptableTypeSyncer.class);

  private final String propertyName;
  private final TypeAdaptor<Object, Object> typeAdaptor;
  private final Class<? extends AdaptableType> internalAdaptableTypeClass;
  private AdaptableType internalAdaptableType;
  private SafeOneWaySimpleTypeAdaptor toExternalSimpleTypeAdaptor;
  private SafeOneWaySimpleTypeAdaptor toInternalSimpleTypeAdaptor;

  /**
   * Class for copying one AdaptableType to another
   *
   *
   * @param propertyName used in error messages
   * @param internalAdaptableTypeClass internalQdtType
   * @param internalAdaptableTypeLiteralClass internalQdtLiteralType
   * @param externalSimpleClass externalSimpleType
   * @param typeAdaptor typeAdaptor typeAdaptor
   */
  public AdaptableTypeSyncer(
          String propertyName,
          Class<? extends AdaptableType> internalAdaptableTypeClass,
          Class<?> internalAdaptableTypeLiteralClass,
          Class<?> externalSimpleClass,
          TypeAdaptor<Object, Object> typeAdaptor) {
    if (typeAdaptor == null) {
      throw new PropertyException("Null typeAdaptor was passed, this is an internal bug");
    }
    this.propertyName = propertyName;
    this.typeAdaptor = typeAdaptor;
    this.internalAdaptableTypeClass = internalAdaptableTypeClass;
    this.toExternalSimpleTypeAdaptor = new SafeOneWaySimpleTypeAdaptor(propertyName,
            typeAdaptor, Direction.TO_EXTERNAL, internalAdaptableTypeLiteralClass, externalSimpleClass);

    this.toInternalSimpleTypeAdaptor = new SafeOneWaySimpleTypeAdaptor(propertyName,
            typeAdaptor, Direction.TO_INTERNAL, externalSimpleClass, internalAdaptableTypeLiteralClass);

    try {
      this.internalAdaptableType = internalAdaptableTypeClass.newInstance();
    } catch (InstantiationException e) {
      // TODO produce a better error message that is consistent with how this is handled elsewhere
      throw new PropertyException("Instantiation error creating internal "
              + internalAdaptableTypeClass.getSimpleName() + " AdaptableTypeClass: " + e.getMessage(), e);
    } catch (IllegalAccessException e) {
      // TODO produce a better error message that is consistent with how this is handled elsewhere
      throw new PropertyException("Access error creating internal "
              + internalAdaptableTypeClass.getSimpleName() + " AdaptableTypeClass: " + e.getMessage(), e);
    }
  }

  /**
   * supplies the AdaptableType used internally, that is the AdaptableType the represents the database's view of the data.
   *
   * @return the internal QDT.
   */
  public AdaptableType getInternalInstance() {
    return internalAdaptableType;
  }

  /**
   * Replaces the internal AdaptableType with the one provided. Validates that the provided QDT is of the correct type.
   *
   * @param internalAdaptableType	the AdaptableType to use
   */
  public void setInternalAdaptableType(AdaptableType internalAdaptableType) {
    if (internalAdaptableType != null && !internalAdaptableType.getClass().equals(internalAdaptableTypeClass)) {
      //throw new RuntimeException("Don't know what to do here: targetQdtType:"+internalQdt.getClass().getSimpleName()+" != "+internalQdtType+":"+internalQdtType.getSimpleName());
      throw new ClassCastException("Cannot assign " + internalAdaptableType.getClass().getSimpleName()
              + " to " + internalAdaptableTypeClass.getSimpleName() + " property " + propertyName);
    }
    this.internalAdaptableType = internalAdaptableType;
  }

  /**
   * Sets the cached internal AdaptableType after adapting the value from the provided AdaptableType.
   *
   * @param externalAdaptableType may be null
   * @return the updated internal AdaptableType
   */
  public AdaptableType setInternalFromExternal(AdaptableType externalAdaptableType) {
    if (externalAdaptableType == null) {
      internalAdaptableType = null;
    } else {
      SafeInternalAdaptableTypeAdaptor qdtAdaptor = new SafeInternalAdaptableTypeAdaptor(internalAdaptableTypeClass, getToInternalSimpleTypeAdaptor());
      qdtAdaptor.setTargetFromSource(getInternalInstance(), externalAdaptableType);
    }
    return getInternalInstance();
  }

  /**
   * Sets the provided external AdaptableType from the internal AdaptableType and returns the updated external AdaptableType.
   *
   * @param externalAdaptableType 	external AdaptableType
   * @return the updated external or null if the internal is null
   */
  public AdaptableType setExternalFromInternal(AdaptableType externalAdaptableType) {
    if (getInternalInstance() == null) {
      return null;
    } else {
      SafeInternalAdaptableTypeAdaptor qdtAdaptor = new SafeInternalAdaptableTypeAdaptor(externalAdaptableType.getClass(), getToExternalSimpleTypeAdaptor());
      qdtAdaptor.setTargetFromSource(externalAdaptableType, getInternalInstance());
    }
    return externalAdaptableType;
  }

  // for DEBUG purposes only
  static String debugToString(AdaptableType qdt) {
    String literalStr;
    if (qdt == null) {
      literalStr = null;
    } else if (qdt.getLiteralValue() == null) {
      literalStr = "null";
    } else {
      literalStr = qdt.getLiteralValue().getClass().getSimpleName() + "[" + qdt.getLiteralValue() + "]";
    }
    StringBuilder buf = new StringBuilder();
    if (qdt == null) {
      buf.append("null");
    } else {
      buf.append(qdt.getClass().getSimpleName());
      buf.append("[");
      buf.append(qdt);
      buf.append(", ");
      buf.append("literal=").append(literalStr);
      buf.append("]");
    }
    return buf.toString();
  }

  /**
   * Return the adaptor used to convert the internal AdaptableType into the external type.
   * 
   * @return the toExternalSimpleTypeAdaptor
   */
  protected SafeOneWaySimpleTypeAdaptor getToExternalSimpleTypeAdaptor() {
    return toExternalSimpleTypeAdaptor;
  }

  /**
   * Return the adaptor used to convert the internal AdaptableType into the Developer facing Java type.
   * 
   * @return the toInternalSimpleTypeAdaptor
   */
  protected SafeOneWaySimpleTypeAdaptor getToInternalSimpleTypeAdaptor() {
    return toInternalSimpleTypeAdaptor;
  }

  /**
   * One-shot cycle-aware recursive AdaptableType adaptor. Converts from existing AdaptableType to brand new one, and copies from one AdaptableType to another.
   *
   * <p>
   * Operators can reference the same AdaptableType that own the operator instance, such as:
   * <code>AdaptableType.setLiteralValue{this.operator = new EqualsOperator(this)}</code>. Cycles are handled by tracking source AdaptableTypes observed and returning
   * the previously mapped target AdaptableType when re-observed.
   *
   * <p>
   * Must be used only once for a given read or write of a field.
   */
  public static class SafeInternalAdaptableTypeAdaptor {

    private final Class<? extends AdaptableType> targetAdaptableTypeClass;
    private final SafeOneWaySimpleTypeAdaptor simpleTypeAdaptor;
    private final List<Map.Entry<AdaptableType, AdaptableType>> observedSourcesAndTargets
            = new ArrayList<>();

    /**
     * Constructor
     *
     * @param targetQdtType targetQdtType
     * @param typeAdaptor typeAdaptor
     */
    public SafeInternalAdaptableTypeAdaptor(
            Class<? extends AdaptableType> targetQdtType,
            SafeOneWaySimpleTypeAdaptor typeAdaptor) {
      this.targetAdaptableTypeClass = targetQdtType;
      this.simpleTypeAdaptor = typeAdaptor;
    }

    /**
     * Creates a brand new AdaptableType of the configured target type, based on converted values from the given AdaptableType. Recursively traverses the operators and inner AdaptableType
     * references within the given AdaptableType.
     *
     * <p>
     * If {@code source} is null, returns {@code null}.
     *
     * @param source the AdaptableType to convert to the target type, may be null
     * @return the newly created AdaptableType of the target type, or null if {@code source} was null
     */
    public AdaptableType convert(AdaptableType source) {
      if (!(source instanceof AdaptableType)) {
        return source;
      } else {
        AdaptableType sourceAdaptableType = source;
        try {
          // cycle-detection
          // (note: important that it uses reference equality, not object equality)
          for (Map.Entry<AdaptableType, AdaptableType> sourceAndTarget : observedSourcesAndTargets) {
            if (sourceAndTarget.getKey() == sourceAdaptableType) {
              // re-use existing value
              return sourceAndTarget.getValue();
            }
          }

          AdaptableType targetAdaptableType = newTargetAdaptableType();
          setTargetFromSource(targetAdaptableType, sourceAdaptableType);

          log.debug(simpleTypeAdaptor + " converting " + debugToString(sourceAdaptableType) + " ==> " + debugToString(targetAdaptableType));
          return targetAdaptableType;
        } catch (RuntimeException e) {
          log.debug(simpleTypeAdaptor + " converting " + debugToString(sourceAdaptableType) + " ==> " + e.getClass().getSimpleName());
          throw e;
        }
      }
    }

    /**
     * Updates the target AdaptableType with converted values from the source AdaptableTypeClass. Recursively traverses the operations and inner AdaptableType references within the given source
     * AdaptableType.
     *
     * @param targetAdaptableType the AdaptableType to update (must not be null)
     * @param sourceAdaptableType the AdaptableType with values to convert and copy to the target (must not be null)
     */
    protected void setTargetFromSource(AdaptableType targetAdaptableType, AdaptableType sourceAdaptableType) {
      // sanity checks
      if (!targetAdaptableType.getClass().equals(targetAdaptableTypeClass)) {
        throw new RuntimeException("Don't know what to do here: targetAdaptableType:"
                + targetAdaptableType.getClass().getSimpleName() + " != " + targetAdaptableTypeClass + ":" + targetAdaptableTypeClass.getSimpleName());
      }

      // cycle-detection
      // (note: important that it uses reference equality, not object equality)
      for (Map.Entry<AdaptableType, AdaptableType> soFarEntry : observedSourcesAndTargets) {
        if (soFarEntry.getKey() == sourceAdaptableType) {
          // already observed, so already done.
          return;
        }
      }
      observedSourcesAndTargets.add(new SimpleEntry<AdaptableType, AdaptableType>(sourceAdaptableType, targetAdaptableType));

      targetAdaptableType.setValue(simpleTypeAdaptor.convert(sourceAdaptableType.getValue()));
    }

    // factory method
    private AdaptableType newTargetAdaptableType() {
      try {
        return targetAdaptableTypeClass.newInstance();
      } catch (InstantiationException e) {
        // TODO produce a better error message that is consistent with how this is handled elsewhere
        throw new PropertyException("Instantiation error creating internal "
                + targetAdaptableTypeClass.getSimpleName() + " targetAdaptableTypeClass: " + e.getMessage(), e);
      } catch (IllegalAccessException e) {
        // TODO produce a better error message that is consistent with how this is handled elsewhere
        throw new PropertyException("Access error creating internal "
                + targetAdaptableTypeClass.getSimpleName() + " targetAdaptableTypeClass: " + e.getMessage(), e);
      }
    }
  }
}
