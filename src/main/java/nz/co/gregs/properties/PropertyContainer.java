/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties;

/**
 *
 * @author gregorygraham
 */
public class PropertyContainer {
	protected PropertyDefinition wrapper;

	protected final PropertyDefinition getPropertyWrapper() {
		return this.wrapper;
	}

	protected final void setPropertyWrapper(PropertyDefinition propertyWrapperDefn) {
		this.wrapper = propertyWrapperDefn;
	}
	
}
