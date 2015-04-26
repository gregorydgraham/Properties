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
 * @param <A>
 */
public abstract class AdaptableType<A extends Object> {

	transient private PropertyDefinition propertyDefn; // no guarantees whether this gets set

	private A literalValue = null;

	public AdaptableType() {
	}

	protected AdaptableType(A val) {
		literalValue = val;
	}

	protected final void setLiteralValue(A object) {
		this.literalValue = object;
	}

	protected final A getLiteralValue() {
		return literalValue;
	}

	protected A getLiteralType() {
		return getLiteralValue();
	}

	@SuppressWarnings("unchecked")
	public void adaptTo(AdaptableType<A> source) {
		this.setValue(source.getValue());
	}

	public void setValue(A value) {
		setLiteralValue(value);
	}

	public A getValue() {
		return getLiteralValue();
	}

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
