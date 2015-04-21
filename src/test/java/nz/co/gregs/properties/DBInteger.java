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

	private Long longValue = null;
	private PropertyWrapperDefinition wrapper;

	public DBInteger() {
	}

	public DBInteger(int val) {
		longValue = new Integer(val).longValue();
	}

	public DBInteger(Long val) {
		longValue = val;
	}

	public Long getLiteralValue() {
		return longValue;
	}

	public Object getOperator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void adaptTo(AdaptableType source) {
		this.setValue(source.getValue());
	}

	public Long getValue() {
		return longValue;
	}

	public Object intValue() {
		return longValue.intValue();
	}

	public void setValue(Integer object) {
		this.longValue = new Long(object);
	}

	public long longValue() {
		return this.longValue;
	}

	public void setValue(Long object) {
		this.longValue = object;
	}

	public void setValue(String object) {
		this.longValue = Long.parseLong(object);
	}

	public void setValue(Object object) {
		setValue(object.toString());
	}

	public void setPropertyWrapper(PropertyWrapperDefinition propertyWrapperDefn) {
		this.wrapper = propertyWrapperDefn;
	}

	public boolean isNull() {
		return longValue == null;
	}

	public void clear() {
		longValue = null;
	}

}
