/*
 * Copyright 2013 Gregory Graham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nz.co.gregs.properties.adapt;

/**
 * Syncs between a simple-type external value and an AdaptableType internal value.
 *
 * @author Malcolm Lett
 */
public class SimpleValueAdaptableTypeSyncer extends AdaptableTypeSyncer {

	/**
   * Creates a Syncer for simple-type values and an AdaptableType 
	 *
	 * @param propertyName used in error messages
	 * @param internalType internalAdaptableType
	 * @param internalLiteralType internalLiteralType
	 * @param typeAdaptor typeAdaptor
	 * @param externalSimpleType externalSimpleType
	 */
	public SimpleValueAdaptableTypeSyncer(String propertyName, Class<? extends AdaptableType> internalType,
			Class<?> internalLiteralType, Class<?> externalSimpleType, TypeAdaptor<Object, Object> typeAdaptor) {
		super(propertyName, internalType, internalLiteralType, externalSimpleType, typeAdaptor);
	}

	/**
	 * Sets the cached internal AdaptableType value from the provided non-QDT external
	 * value.
	 *
	 * @param externalValue may be null
	 * @return the updated internal AdaptableType
	 */
	public AdaptableType setInternalTypeFromExternalSimpleValue(Object externalValue) {
		Object internalValue = getToInternalSimpleTypeAdaptor().convert(externalValue);
		AdaptableType internal = getInternalInstance();
		if (internalValue == null) {
			// TODO complete this
			internal.setValue(null);
		} else {
			// TODO what type checking can/should be done here?
			internal.setValue(internalValue);
		}
		return internal;
	}

	/**
	 * Warning: this directly returns the value from the type adaptor, without
	 * casting to the specific type expected by the target java property.
	 *
	 * @return the internal value as a base Java object
	 */
	public Object getExternalSimpleValueFromInternalAdaptableType() {
		return getToExternalSimpleTypeAdaptor().convert(getInternalInstance().getValue());
	}
}
