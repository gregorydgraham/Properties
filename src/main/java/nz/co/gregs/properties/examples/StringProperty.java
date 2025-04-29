/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.examples;

import nz.co.gregs.properties.adapt.AdaptableType;

/**
 * a Property for values stored as Strings.
 *
 * @author Gregory Graham
 */
public class StringProperty extends AdaptableType<String> {

  /**
   * Create a StringProperty with the given value.
   * 
   * @param string the initial value
   */
  public StringProperty(String string) {
		super(string);
	}

  /**
   * Default constructor.
   */
  public StringProperty() {
		super();
	}

  /**
   * The value as a String.
   *
   * @return the value as a String
   */
  public String stringValue() {
		return getValue();
	}
}
