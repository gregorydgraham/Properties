package nz.co.gregs.properties.examples;

import nz.co.gregs.properties.PropertyContainer;
import nz.co.gregs.properties.PropertyContainerWrapperFactory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gregorygraham
 */
public class PropertyContainerImpl extends PropertyContainer{
	
	private static final PropertyContainerWrapperFactory dbFactory = new PropertyContainerWrapperFactoryImpl();

	public PropertyContainerImpl() {
		super(dbFactory);
	}
	
}
