/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.examples;

import nz.co.gregs.properties.adapt.AdaptableType;

/**
 * A Property for handling both Integers and Longs
 *
 * @author Gregory Graham
 */
public class IntegerProperty extends AdaptableType<Long> {

  /**
   * default constructor
   *
   */
  public IntegerProperty() {
  }

  /**
   * int constructor
   *
   * @param val initial value
   */
  public IntegerProperty(int val) {
    super(Integer.valueOf(val).longValue());
  }

  /**
   * Long constructor
   *
   * @param val initial value
   */
  public IntegerProperty(Long val) {
    super(val);
  }

  /**
   * Returns the integer value
   *
   * @return an integer of the internal value
   */
  public Integer intValue() {
    return getValue().intValue();
  }

  /**
   * Returns the long value
   *
   * @return a long of the internal value
   */
  public long longValue() {
    return getValue();
  }

  /**
   * Set the internal value to the new value
   *
   * @param integer the new value
   */
  public void setValue(Integer integer) {
    setLiteralValue(integer.longValue());
  }

  /**
   * Set the internal value to the new value
   *
   * @param stringValue the new value
   * @throws NumberFormatException If the string cannot be parsed as a {@code long}.
   */
  public void setValue(String stringValue) throws NumberFormatException {
    setLiteralValue(Long.valueOf(stringValue));
  }
}
