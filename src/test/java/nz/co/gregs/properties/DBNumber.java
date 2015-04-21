/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties;

import nz.co.gregs.properties.adapt.AdaptableType;

/**
 *
 * @author gregory.graham
 */
public class DBNumber implements AdaptableType {
	private Number number;
	private PropertyWrapperDefinition wrapper;

	public Number getLiteralValue() {
		return number;
	}

	public Object getOperator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void adaptTo(AdaptableType source) {
		this.setValue(source.getValue());
	}

	public Object getValue() {
		return number;
	}

	public void setValue(Integer object) {
		this.number = object.longValue();
	}

	public void setValue(Long object) {
		this.number = object;
	}

	public void setValue(Double object) {
		this.number = object;
	}

	public void setValue(String object) {
		this.number = Long.parseLong(object);
	}

	public void setValue(Object object) {
		setValue(object.toString());
	}

	public void setPropertyWrapper(PropertyWrapperDefinition propertyWrapperDefn) {
		this.wrapper = propertyWrapperDefn;
	}
	
}
