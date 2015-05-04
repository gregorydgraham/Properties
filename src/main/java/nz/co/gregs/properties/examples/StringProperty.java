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
public class StringProperty extends AdaptableType<String> {

	public StringProperty(String string) {
		super(string);
	}

	public StringProperty() {
		super();
	}

	public String stringValue() {
		return getValue();
	}
}
