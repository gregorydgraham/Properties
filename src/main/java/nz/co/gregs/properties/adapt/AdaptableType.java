/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.adapt;

import nz.co.gregs.properties.PropertyWrapperDefinition;

/**
 *
 * @author gregorygraham
 */
public interface AdaptableType {

	public Object getLiteralValue();

	public Object getOperator();

	public void adaptTo(AdaptableType source);

	public Object getValue();

	public void setValue(Object object);

	public void setPropertyWrapper(PropertyWrapperDefinition propertyWrapperDefn);
	
	public boolean isNull();
	
	public void clear();
}
