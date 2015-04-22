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
public class DBString extends AdaptableType {

	public DBString(String string) {
		super(string);
	}

	public DBString() {
		super();
	}

	public String getValue() {
		return (String) getLiteralValue();
	}

	public String stringValue() {
		return getValue();
	}

	public void setValue(String object) {
		setLiteralValue(object);
	}

	public void setValue(Object object) {
		setValue(object.toString());
	}
}
