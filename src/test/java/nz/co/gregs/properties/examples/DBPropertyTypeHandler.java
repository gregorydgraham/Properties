/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.examples;

import java.util.Date;
import nz.co.gregs.properties.JavaProperty;
import nz.co.gregs.properties.adapt.AdaptableType;
import nz.co.gregs.properties.adapt.PropertyTypeHandler;

/**
 *
 * @author gregorygraham
 */
public class DBPropertyTypeHandler extends PropertyTypeHandler{

	public DBPropertyTypeHandler() {
	}

	public DBPropertyTypeHandler(JavaProperty propertyOf, boolean b) {
		super(propertyOf, b);
	}

	@Override
	public Class<? extends AdaptableType> inferredAdaptableTypeForSimpleType(Class<?> simpleType) {
		
        if (simpleType.equals(String.class)) {
            return DBString.class;
        } else if (Number.class.isAssignableFrom(simpleType)) {
            if (Integer.class.isAssignableFrom(simpleType) || Long.class.isAssignableFrom(simpleType)) {
                return DBInteger.class;
            }
            if (Float.class.isAssignableFrom(simpleType) || Double.class.isAssignableFrom(simpleType)) {
                return DBNumber.class;
            } else {
                return DBNumber.class;
            }
        } else if (Date.class.isAssignableFrom(simpleType)) {
            return DBDate.class;
        } else if (Boolean.class.isAssignableFrom(simpleType)) {
            return DBBoolean.class;
        }

        // all remaining types require explicit declaration
        return null;	}

	@Override
	public Class<?> literalTypeOf(Class<? extends AdaptableType> type) {
        if (type.equals(DBString.class)) {
            return String.class;
        } else if (type.equals(DBNumber.class)) {
            return Double.class;
        } else if (type.equals(DBInteger.class)) {
            return Long.class;
        } else if (type.equals(DBDate.class)) {
            return Date.class;
        } else if (type.equals(DBBoolean.class)) {
            return Boolean.class;
        } else {
            throw new RuntimeException("Unrecognised AdptableType-type " + type.getSimpleName());
        }
	}
	
}
