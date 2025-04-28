package nz.co.gregs.properties.adapt;

import nz.co.gregs.properties.PropertyDefinition;

/**
 * A datatype capable of storing value of the specified external type.
 *
 * <p>
 * This datatype allows the value to be automatically converted from the normal Java-centric value to the external facing type supported by the
 * AdaptableType</p>
 *
 * <p>
 * Implement {@link #setValue(java.lang.Object) } to complete the AdaptableType. Check
 * {@link nz.co.gregs.properties.examples.BooleanProperty}, {@link nz.co.gregs.properties.examples.DateProperty}, {@link nz.co.gregs.properties.examples.IntegerProperty}, {@link nz.co.gregs.properties.examples.NumberProperty},
 * or {@link nz.co.gregs.properties.examples.StringProperty} for an example implementation.</p>
 *
 * @author Gregory Graham
 * @param <A> the external type
 */
public abstract class AdaptableType<A extends Object> {

  transient private PropertyDefinition propertyDefn; // no guarantees whether this gets set

  private A literalValue = null;

  /**
   * Create a new blank AdaptableType
   *
   */
  public AdaptableType() {
  }

  /**
   * Create a new AdaptableType with the supplied literal value.
   *
   * @param val the literal value to use
   */
  protected AdaptableType(A val) {
    literalValue = val;
  }

  /**
   * Change the value stored to the new supplied value.
   *
   * @param object the new value to use
   */
  protected void setLiteralValue(A object) {
    this.literalValue = object;
  }

  /**
   * get the stored value.
   *
   * @return the stored value
   */
  protected A getLiteralValue() {
    return literalValue;
  }

  /**
   * Set the value of this object to the value of the source.
   *
   * <p>
   * Equivalent to <code>this.setValue(source.getValue())</code></p>
   *
   * @param source the source of the value to copy
   */
  @SuppressWarnings("unchecked")
  public void adaptTo(AdaptableType<A> source) {
    this.setValue(source.getValue());
  }

  /**
   * change the value stored to the value supplied.
   *
   * @param value the new literal value
   */
  public void setValue(A value) {
    setLiteralValue(value);
  }

  /**
   * Retrieve the value stored.
   *
   * @return the stored value
   */
  public A getValue() {
    return getLiteralValue();
  }

  /**
   * Checks to see if the value of the stored value is null.
   *
   * <p>
   * Properties was made to handle ORM interactions in particular and thus has 2 versions of Null: the standard Java version where the property object itself
   * does not exist, plus the database version where there is a value and it is NULL.</p>
   *
   * <p>
   * This method covers the second version of NULL</p>
   *
   * @return true if the stored value is NULL
   */
  public boolean isNull() {
    return literalValue == null;
  }

  /**
   * Empty the literal value
   *
   * @return a reference to this object.
   */
  public AdaptableType<A> clear() {
    literalValue = null;
    return this;
  }

  /**
   * Sets the Property definition.
   *
   * <p>
   * This is done automatically, and should never be done manually. So the method is NOT public.</p>
   *
   * @param propertyDefn the new property definition to use
   */
  void setPropertyDefinition(PropertyDefinition propertyDefn) {
    this.propertyDefn = propertyDefn;
  }

  /**
   * Return the PropertyDefinition
   *
   * @return the propertyDefn
   */
  public PropertyDefinition getPropertyDefinition() {
    return propertyDefn;
  }
}
