package nz.co.gregs.properties.adapt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adapts a non-DBvolution field or property to a DBvolution type, or adapts a
 * DBvolution field or property to a different DBvolution type.
 *
 * <p>
 * AdaptType uses a {@link TypeAdaptor} and a {@link AdaptableType} to a convert
 * an AdaptableType into a different Java class
 *
 * <p>
 * In some databases values are stored in an unusual datatype. For instance a
 * date might be stored as an integer or a integer as a string. It is possible
 * to implement these as a custom Adaptable but it is much easier to
 * "adapt" the value to an existing AdaptableType. This also allows for a form of
 * pre-processing of values like the TrimmingStringAdaptor example below.
 *
 * <p>
 * Think of the &#64;DAdaptType annotation adding a bridge between the actual
 * AT and the perceived AT. This is similar to the bridge that AT provide
 * between the actual DB value and the perceived Java value.
 *
 * Examples:
 * <pre>
 * &#64;AdaptType(value=DaysSinceEpochDateAdaptor.class, type=DBDate.class)
 public DBInteger daysSinceEpoch;

 &#64;AdaptType(value=MyFreeTextNumberAdaptor.class, type=DBInteger.class)
 public String freeTextNumber;

 &#64;AdaptType(value=TrimmingStringAdaptor.class, type=DBString.class) public
 DBString trimmedValue;
 </pre>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdaptType {

	/**
	 * The custom type adaptor used to convert between the type of the annotated
	 * field/property and the value of {@link #type()}.
	 *
	 * <p>
	 * The indicated class must be able to be instantiated. It cannot be an
	 * interface or an abstract class and must have a default constructor.
	 *
	 * @return the adaptor used to mediate between the raw java object
	 * and the property's reference type.
	 */
	Class<? extends TypeAdaptor<?, ?>> value();

	/**
	 * The internal type that the adaptor converts to.
	 *
	 * @return the QueryableDatatype class used internally for DB communication
	 */
	Class<? extends AdaptableType> type() default AdaptableType.class;
}
