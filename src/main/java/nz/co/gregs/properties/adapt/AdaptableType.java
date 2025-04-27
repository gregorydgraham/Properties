/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author gregorygraham
 * @param <A>
 */
public abstract class AdaptableType<A extends Object> {

  transient private PropertyDefinition propertyDefn; // no guarantees whether this gets set

  private A literalValue = null;

  public AdaptableType() {
  }

  protected AdaptableType(A val) {
    literalValue = val;
  }

  protected void setLiteralValue(A object) {
    this.literalValue = object;
  }

  protected A getLiteralValue() {
    return literalValue;
  }

  protected A getLiteralType() {
    return getLiteralValue();
  }

  @SuppressWarnings("unchecked")
  public void adaptTo(AdaptableType<A> source) {
    this.setValue(source.getValue());
  }

  public void setValue(A value) {
    setLiteralValue(value);
  }

  public A getValue() {
    return getLiteralValue();
  }

  public boolean isNull() {
    return literalValue == null;
  }

  public AdaptableType<A> clear() {
    literalValue = null;
    return this;
  }

  public void setPropertyDefinition(PropertyDefinition propertyWrapperDefn) {
    this.propertyDefn = propertyWrapperDefn;
  }

  /**
   * @return the propertyDefn
   */
  public PropertyDefinition getPropertyDefinition() {
    return propertyDefn;
  }
}
