package nz.co.gregs.properties.adapt;

import nz.co.gregs.properties.PropertyDefinition;
import nz.co.gregs.properties.adapt.AdaptableType;

/**
 * Internal class. Do not use.

 Used internally to bridge between packages. Makes it possible to hide
 internal methods on the QueryableDatatype so that they don't pollute the API
 or JavaDocs, while still providing access to the internal methods from other
 packages within DBvolution.

 For example QueryableDatatype.setPropertyDefinition() is set to package-private,
 so the only way of calling it from other packages is via this class. If
 QueryableDatatype.setPropertyDefinition() was public, then this class wouldn't
 be needed, but it would pollute the public API.
 */
public class InternalAdaptableTypeProxy {

	private final AdaptableType adaptableType;

	/**
	 * Internal class, do not use.
	 *
	 * @param adaptableType	 qdt	
	 */
	public InternalAdaptableTypeProxy(AdaptableType adaptableType) {
		this.adaptableType = adaptableType;
	}

	/**
	 * Internal class, do not use.
	 * <p>
	 * Injects the PropertyWrapper into the QDT.
	 * <p>
	 * For use with QDT types that need meta-data only available via property
	 * wrappers.
	 *
	 * @param propertyWrapperDefn	 propertyWrapperDefn	
	 */
	public void setPropertyWrapper(PropertyDefinition propertyWrapperDefn) {
		adaptableType.setPropertyDefinition(propertyWrapperDefn);
	}

	/**
	 * Internal class, do not use.
	 * <p>
	 * Hides the generic setValue(Object) method within AdaptableType while
	 * allowing it to be used.
	 *
	 * @param obj	 obj	
	 */
	@SuppressWarnings("unchecked")
	public void setValue(Object obj) {
		adaptableType.setValue(obj);
	}
}
