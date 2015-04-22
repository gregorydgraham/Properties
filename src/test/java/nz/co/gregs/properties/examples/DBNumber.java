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
public class DBNumber extends AdaptableType {

	public Number getValue() {
		return (Number) getLiteralValue();
	}

	public void setValue(Integer object) {
		setLiteralValue(object.longValue());
	}

	public void setValue(Long object) {
		setLiteralValue(object);
	}

	public void setValue(Double object) {
		setLiteralValue(object);
	}

	public void setValue(String object) {
		setLiteralValue(Double.parseDouble(object));
	}

	public void setValue(Object object) {
		setValue(object.toString());
	}

}
