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
public class DBInteger implements AdaptableType {
	private Integer integer;
	private PropertyWrapperDefinition wrapper;

	public Integer getLiteralValue() {
		return integer;
	}

	public Object getOperator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void adaptTo(AdaptableType source) {
		this.setValue(source.getValue());
	}

	public Object getValue() {
		return integer;
	}

	public void setValue(Integer object) {
		this.integer = object;
	}

	public void setValue(Long object) {
		this.integer = object.intValue();
	}

	public void setValue(String object) {
		this.integer = Integer.parseInt(object);
	}

	public void setValue(Object object) {
		setValue(object.toString());
	}

	public void setPropertyWrapper(PropertyWrapperDefinition propertyWrapperDefn) {
		this.wrapper = propertyWrapperDefn;
	}
	
}
