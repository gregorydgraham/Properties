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
public class NumberProperty extends AdaptableType<Double> {

	public void setValue(Integer object) {
		setLiteralValue(object.doubleValue());
	}

	public void setValue(Long object) {
		setLiteralValue(object.doubleValue());
	}

	public void setValue(String object) {
		setLiteralValue(Double.parseDouble(object));
	}
}
