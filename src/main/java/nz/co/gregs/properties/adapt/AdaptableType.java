/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.adapt;

import nz.co.gregs.properties.PropertyContainer;
import nz.co.gregs.properties.PropertyDefinition;

/**
 *
 * @author gregorygraham
 */
public abstract class AdaptableType extends PropertyContainer{

	transient private PropertyDefinition propertyDefn; // no guarantees whether this gets set

	private Object literalValue = null;

	public AdaptableType() {
	}

	protected AdaptableType(Object val) {
		literalValue = val;
	}

	protected final void setLiteralValue(Object object) {
		this.literalValue = object;
	}

	protected final Object getLiteralValue() {
		return literalValue;
	}

	public Object getOperator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void adaptTo(AdaptableType source) {
		this.setValue(source.getValue());
	}

	public abstract void setValue(Object value);

	public abstract Object getValue();


	public boolean isNull() {
		return literalValue == null;
	}

	public void clear() {
		literalValue = null;
	}

	public void setPropertyDefinition(PropertyDefinition propertyWrapperDefn) {
		this.propertyDefn = propertyWrapperDefn;
	}

	/**
	 * @return the propertyDefn
	 */
	public PropertyDefinition getPropertyDefinition() {
		return propertyDefn;
	}
}
