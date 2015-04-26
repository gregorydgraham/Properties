/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.examples;

import nz.co.gregs.properties.adapt.AdaptableType;

/**
 *
 * @author gregory.graham
 */
public class DBInteger extends AdaptableType<Long> {

	public DBInteger() {
	}

	public DBInteger(int val) {
		super(new Integer(val).longValue());
	}

	public DBInteger(Long val) {
		super(val);
	}

	public Integer intValue() {
		return getValue().intValue();
	}

	public long longValue() {
		return getValue();
	}

	public void setValue(Integer object) {
		setLiteralValue(new Long(object));
	}

	public void setValue(String object) {
		setLiteralValue(Long.parseLong(object));
	}
}
