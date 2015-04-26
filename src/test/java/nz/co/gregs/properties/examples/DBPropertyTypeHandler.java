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
            return StringProperty.class;
        } else if (Number.class.isAssignableFrom(simpleType)) {
            if (Integer.class.isAssignableFrom(simpleType) || Long.class.isAssignableFrom(simpleType)) {
                return IntegerProperty.class;
            }
            if (Float.class.isAssignableFrom(simpleType) || Double.class.isAssignableFrom(simpleType)) {
                return NumberProperty.class;
            } else {
                return NumberProperty.class;
            }
        } else if (Date.class.isAssignableFrom(simpleType)) {
            return DateProperty.class;
        } else if (Boolean.class.isAssignableFrom(simpleType)) {
            return BooleanProperty.class;
        }

        // all remaining types require explicit declaration
        return null;	}

	@Override
	public Class<?> literalTypeOf(Class<? extends AdaptableType> type) {
        if (type.equals(StringProperty.class)) {
            return String.class;
        } else if (type.equals(NumberProperty.class)) {
            return Double.class;
        } else if (type.equals(IntegerProperty.class)) {
            return Long.class;
        } else if (type.equals(DateProperty.class)) {
            return Date.class;
        } else if (type.equals(BooleanProperty.class)) {
            return Boolean.class;
        } else {
            throw new RuntimeException("Unrecognised AdptableType-type " + type.getSimpleName());
        }
	}
	
}
