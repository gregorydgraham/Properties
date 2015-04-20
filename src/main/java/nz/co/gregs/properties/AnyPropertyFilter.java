/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Implementation that accepts everything.
 */
class AnyPropertyFilter implements JavaPropertyFilter {

	@Override
	public boolean acceptField(Field field) {
		return true;
	}

	@Override
	public boolean acceptBeanProperty(Method getter, Method setter) {
		return true;
	}
	
}
