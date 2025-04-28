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

	public IntegerProperty() {
	}

	public IntegerProperty(int val) {
		super(Integer.valueOf(val).longValue());
	}

	public IntegerProperty(Long val) {
		super(val);
	}

	public Integer intValue() {
		return getValue().intValue();
	}

	public long longValue() {
		return getValue();
	}

	public void setValue(Integer object) {
		setLiteralValue(object.longValue());
	}

	public void setValue(String object) {
		setLiteralValue(Long.valueOf(object));
	}
}
