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
public class MyString implements AdaptableType {
	private String string;
	private PropertyWrapperDefinition wrapper;

	public String getLiteralValue() {
		return string;
	}

	public Object getOperator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void adaptTo(AdaptableType source) {
		this.setValue(source.getValue());
	}

	public String getValue() {
		return string;
	}

	public void setValue(String object) {
		this.string = object;
	}

	public void setValue(Object object) {
		setValue(object.toString());
	}

	public void setPropertyWrapper(PropertyWrapperDefinition propertyWrapperDefn) {
		this.wrapper = propertyWrapperDefn;
	}
	
	
}
