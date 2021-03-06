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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.co.gregs.properties.exceptions.PropertyException;
import nz.co.gregs.properties.adapt.SafeOneWaySimpleTypeAdaptor.Direction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allows synchronizations to be done between two QueryableDatatypes, based on a
 * Type Adaptor.
 *
 * @author Malcolm Lett
 */
public class AdaptableTypeSyncer {

	private static final Log log = LogFactory.getLog(AdaptableTypeSyncer.class);

	private final String propertyName;
	private final TypeAdaptor<Object, Object> typeAdaptor;
	private final Class<? extends AdaptableType> internalQdtType;
	private AdaptableType internalQdt;
	private SafeOneWaySimpleTypeAdaptor toExternalSimpleTypeAdaptor;
	private SafeOneWaySimpleTypeAdaptor toInternalSimpleTypeAdaptor;

	/**
	 *
	 * @param propertyName used in error messages
	 * @param internalQdtType internalQdtType
	 * @param internalQdtLiteralType internalQdtLiteralType
	 * @param externalSimpleType externalSimpleType
	 * @param typeAdaptor typeAdaptor typeAdaptor
	 */
	public AdaptableTypeSyncer(
			String propertyName,
			Class<? extends AdaptableType> internalQdtType,
			Class<?> internalQdtLiteralType,
			Class<?> externalSimpleType,
			TypeAdaptor<Object, Object> typeAdaptor) {
		if (typeAdaptor == null) {
			throw new PropertyException("Null typeAdaptor was passed, this is an internal bug");
		}
		this.propertyName = propertyName;
		this.typeAdaptor = typeAdaptor;
		this.internalQdtType = internalQdtType;
		this.toExternalSimpleTypeAdaptor = new SafeOneWaySimpleTypeAdaptor(propertyName,
				typeAdaptor, Direction.TO_EXTERNAL, internalQdtLiteralType, externalSimpleType);

		this.toInternalSimpleTypeAdaptor = new SafeOneWaySimpleTypeAdaptor(propertyName,
				typeAdaptor, Direction.TO_INTERNAL, externalSimpleType, internalQdtLiteralType);

		try {
			this.internalQdt = internalQdtType.newInstance();
		} catch (InstantiationException e) {
			// TODO produce a better error message that is consistent with how this is handled elsewhere
			throw new PropertyException("Instantiation error creating internal "
					+ internalQdtType.getSimpleName() + " QDT: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			// TODO produce a better error message that is consistent with how this is handled elsewhere
			throw new PropertyException("Access error creating internal "
					+ internalQdtType.getSimpleName() + " QDT: " + e.getMessage(), e);
		}
	}

	/**
	 * supplies the QDT used internally, that is the QDT the represents the
	 * database's view of the data.
	 *
	 * @return the internal QDT.
	 */
	public AdaptableType getInternalInstance() {
		return internalQdt;
	}

	/**
	 * Replaces the internal QDT with the one provided. Validates that the
	 * provided QDT is of the correct type.
	 *
	 * @param internalQdt	internalQdt
	 */
	public void setInternalQueryableDatatype(AdaptableType internalQdt) {
		if (internalQdt != null && !internalQdt.getClass().equals(internalQdtType)) {
			//throw new RuntimeException("Don't know what to do here: targetQdtType:"+internalQdt.getClass().getSimpleName()+" != "+internalQdtType+":"+internalQdtType.getSimpleName());
			throw new ClassCastException("Cannot assign " + internalQdt.getClass().getSimpleName()
					+ " to " + internalQdtType.getSimpleName() + " property " + propertyName);
		}
		this.internalQdt = internalQdt;
	}

	/**
	 * Sets the cached internal QDT after adapting the value from the provided
	 * QDT.
	 *
	 * @param externalQdt may be null
	 * @return the updated internal QDT
	 */
	public AdaptableType setInternalQDTFromExternalQDT(AdaptableType externalQdt) {
		if (externalQdt == null) {
			internalQdt = null;
		} else {
			DBSafeInternalQDTAdaptor qdtAdaptor = new DBSafeInternalQDTAdaptor(internalQdtType, getToInternalSimpleTypeAdaptor());
			qdtAdaptor.setTargetFromSource(getInternalInstance(), externalQdt);
		}
		return getInternalInstance();
	}

	/**
	 * Sets the provided external QDT from the internal QDT and returns the
	 * updated external QDT.
	 *
	 * @param externalQdt	externalQdt
	 * @return the updated external QDT or null if the internal QDT is null
	 */
	public AdaptableType setExternalFromInternalQDT(AdaptableType externalQdt) {
		if (getInternalInstance() == null) {
			return null;
		} else {
			DBSafeInternalQDTAdaptor qdtAdaptor = new DBSafeInternalQDTAdaptor(externalQdt.getClass(), getToExternalSimpleTypeAdaptor());
			qdtAdaptor.setTargetFromSource(externalQdt, getInternalInstance());
		}
		return externalQdt;
	}

	// for DEBUG purposes only
	static String qdtToString(AdaptableType qdt) {
		String literalStr;
		if (qdt == null) {
			literalStr = null;
		} else if (qdt.getLiteralValue() == null) {
			literalStr = "null";
		} else {
			literalStr = qdt.getLiteralValue().getClass().getSimpleName() + "[" + qdt.getLiteralValue() + "]";
		}
		StringBuilder buf = new StringBuilder();
		if (qdt == null) {
			buf.append("null");
		} else {
			buf.append(qdt.getClass().getSimpleName());
			buf.append("[");
			buf.append(qdt);
			buf.append(", ");
			buf.append("literal=").append(literalStr);
			buf.append("]");
		}
		return buf.toString();
	}

	/**
	 * @return the toExternalSimpleTypeAdaptor
	 */
	protected SafeOneWaySimpleTypeAdaptor getToExternalSimpleTypeAdaptor() {
		return toExternalSimpleTypeAdaptor;
	}

	/**
	 * @return the toInternalSimpleTypeAdaptor
	 */
	protected SafeOneWaySimpleTypeAdaptor getToInternalSimpleTypeAdaptor() {
		return toInternalSimpleTypeAdaptor;
	}

	/**
	 * One-shot cycle-aware recursive QDT adaptor. Converts from existing QDT to
	 * brand new one, and copies from one QDT to another.
	 *
	 * <p>
	 * DBOperators can reference the same QDT that own the operator instance,
	 * such as:
	 * <code>AdaptableType.setLiteralValue{this.operator = new DBEqualsOperator(this)}</code>.
	 * Cycles are handled by tracking source QDTs observed and returning the
	 * previously mapped target QDT when re-observed.
	 *
	 * <p>
	 * Must be used only once for a given read or write of a field.
	 */
	public static class DBSafeInternalQDTAdaptor {

		private final Class<? extends AdaptableType> targetQdtType;
		private final SafeOneWaySimpleTypeAdaptor simpleTypeAdaptor;
		private final List<Map.Entry<AdaptableType, AdaptableType>> observedSourcesAndTargets
				= new ArrayList<Map.Entry<AdaptableType, AdaptableType>>();

		/**
		 * Constructor
		 *
		 * @param targetQdtType targetQdtType
		 * @param typeAdaptor typeAdaptor
		 */
		public DBSafeInternalQDTAdaptor(
				Class<? extends AdaptableType> targetQdtType,
				SafeOneWaySimpleTypeAdaptor typeAdaptor) {
			this.targetQdtType = targetQdtType;
			this.simpleTypeAdaptor = typeAdaptor;
		}

		/**
		 * Creates a brand new QDT of the configured target type, based on
		 * converted values from the given QDT. Recursively traverses the
		 * operators and inner QDT references within the given QDT.
		 *
		 * <p>
		 * If {@code source} is null, returns {@code null}.
		 *
		 * @param source the QDT to convert to the target type, may be null
		 * @return the newly created QDT of the target type, or null if
		 * {@code source} was null
		 */
		public AdaptableType convert(AdaptableType source) {
			if (!(source instanceof AdaptableType)) {
				return source;
			} else {
				AdaptableType sourceQDT = source;
				try {
					// cycle-detection
					// (note: important that it uses reference equality, not object equality)
					for (Map.Entry<AdaptableType, AdaptableType> sourceAndTarget : observedSourcesAndTargets) {
						if (sourceAndTarget.getKey() == sourceQDT) {
							// re-use existing value
							return sourceAndTarget.getValue();
						}
					}

					AdaptableType targetQdt = newTargetAdaptableType();
					setTargetFromSource(targetQdt, sourceQDT);

					log.debug(simpleTypeAdaptor + " converting " + qdtToString(sourceQDT) + " ==> " + qdtToString(targetQdt));
					return targetQdt;
				} catch (RuntimeException e) {
					log.debug(simpleTypeAdaptor + " converting " + qdtToString(sourceQDT) + " ==> " + e.getClass().getSimpleName());
					throw e;
				}
			}
		}

		/**
		 * Updates the target QDT with converted values from the source QDT.
		 * Recursively traverses the operations and inner QDT references within
		 * the given source QTD.
		 *
		 * @param targetAdaptableType the QDT to update (must not be null)
		 * @param sourceAdaptableType the QDT with values to convert and copy to the
		 * target (must not be null)
		 */
		protected void setTargetFromSource(AdaptableType targetAdaptableType, AdaptableType sourceAdaptableType) {
			// sanity checks
			if (!targetAdaptableType.getClass().equals(targetQdtType)) {
				throw new RuntimeException("Don't know what to do here: targetQdtType:"
						+ targetAdaptableType.getClass().getSimpleName() + " != " + targetQdtType + ":" + targetQdtType.getSimpleName());
			}

			// cycle-detection
			// (note: important that it uses reference equality, not object equality)
			for (Map.Entry<AdaptableType, AdaptableType> soFarEntry : observedSourcesAndTargets) {
				if (soFarEntry.getKey() == sourceAdaptableType) {
					// already observed, so already done.
					return;
				}
			}
			observedSourcesAndTargets.add(new SimpleEntry<AdaptableType, AdaptableType>(sourceAdaptableType, targetAdaptableType));

			targetAdaptableType.setValue(simpleTypeAdaptor.convert(sourceAdaptableType.getValue()));
		}

		// factory method
		private AdaptableType newTargetAdaptableType() {
			try {
				return targetQdtType.newInstance();
			} catch (InstantiationException e) {
				// TODO produce a better error message that is consistent with how this is handled elsewhere
				throw new PropertyException("Instantiation error creating internal "
						+ targetQdtType.getSimpleName() + " QDT: " + e.getMessage(), e);
			} catch (IllegalAccessException e) {
				// TODO produce a better error message that is consistent with how this is handled elsewhere
				throw new PropertyException("Access error creating internal "
						+ targetQdtType.getSimpleName() + " QDT: " + e.getMessage(), e);
			}
		}
	}
		}
