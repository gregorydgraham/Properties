/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.examples;

import nz.co.gregs.properties.PropertyContainerWrapperFactory;

/**
 *
 * @author gregorygraham
 */
class PropertyContainerWrapperFactoryImpl extends PropertyContainerWrapperFactory {

	public PropertyContainerWrapperFactoryImpl() {
		super(new DBPropertyTypeHandler());
	}

}
