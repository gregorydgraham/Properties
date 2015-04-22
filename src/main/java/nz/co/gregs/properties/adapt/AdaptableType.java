/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.adapt;

import nz.co.gregs.properties.PropertyDefinition;

/**
 *
 * @author gregorygraham
 */
public abstract class AdaptableType {


	private Object literalValue = null;
	private PropertyDefinition wrapper;

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

	protected final void setPropertyWrapper(PropertyDefinition propertyWrapperDefn) {
		this.wrapper = propertyWrapperDefn;
	}

	public final PropertyDefinition getPropertyWrapper() {
		return this.wrapper;
	}

	public boolean isNull() {
		return literalValue == null;
	}

	public void clear() {
		literalValue = null;
	}
}
