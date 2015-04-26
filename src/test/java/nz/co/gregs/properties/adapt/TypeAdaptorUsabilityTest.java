package nz.co.gregs.properties.adapt;


import java.util.Date;
import nz.co.gregs.properties.examples.DBColumn;
import nz.co.gregs.properties.examples.DateProperty;
import nz.co.gregs.properties.examples.IntegerProperty;
import nz.co.gregs.properties.examples.PropertyContainerImpl;
import nz.co.gregs.properties.examples.StringProperty;

import org.junit.Test;

/**
 * Focuses on ensuring that at a high level API interface these different
 * scenarios are supported by the library.
 */
@SuppressWarnings("serial")
public class TypeAdaptorUsabilityTest {

	@Test
	public void integerFieldAdaptedAsDBInteger_whenAdaptingOnSimpleTypes() {
		class MyTypeAdaptor implements TypeAdaptor<Integer, Integer> {

			@Override
			public Integer fromInternalValue(Integer dbvValue) {
				return dbvValue;
			}

			@Override
			public Integer fromExternalValue(Integer objectValue) {
				return objectValue;
			}
		}

		class MyTable extends PropertyContainerImpl {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public Integer year;
		}
	}

	@Test
	public void stringFieldAdaptedAsDBInteger_whenAdaptingOnSimpleTypes() {
		class MyTypeAdaptor implements TypeAdaptor<String, Long> {

			@Override
			public String fromInternalValue(Long dbvValue) {
				return (dbvValue == null) ? null : dbvValue.toString();
			}

			@Override
			public Long fromExternalValue(String objectValue) {
				return (objectValue == null) ? null : Long.parseLong(objectValue);
			}
		}

		class MyTable extends PropertyContainerImpl {

			@DBColumn
			@AdaptType(value = MyTypeAdaptor.class, type = IntegerProperty.class)
			public String year;
		}
	}

	@Test
	public void dbstringFieldAdaptedAsDBInteger_whenAdaptingOnSimpleTypes() {
		class MyTypeAdaptor implements TypeAdaptor<String, Long> {

			@Override
			public String fromInternalValue(Long dbvValue) {
				return (dbvValue == null) ? null : dbvValue.toString();
			}

			@Override
			public Long fromExternalValue(String objectValue) {
				return (objectValue == null) ? null : Long.parseLong(objectValue);
			}
		}

		class MyTable extends PropertyContainerImpl {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public StringProperty year;
		}
	}

	@Test
	public void integerFieldAdaptedAsDBDate_whenAdaptingOnSimpleTypes() {
		@SuppressWarnings("deprecation")
		class MyTypeAdaptor implements TypeAdaptor<Integer, Date> {

			@Override
			public Integer fromInternalValue(Date dbvValue) {
				return (dbvValue == null) ? null : dbvValue.getYear() + 1900;
			}

			@Override
			public Date fromExternalValue(Integer objectValue) {
				return (objectValue == null) ? null : new Date(objectValue - 1900, 0, 1);
			}
		}

		class MyTable extends PropertyContainerImpl {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public Integer year;
		}
	}

	@Test
	public void dbintegerFieldAdaptedAsDBDate_whenAdaptingOnSimpleTypes() {
		@SuppressWarnings("deprecation")
		class MyTypeAdaptor implements TypeAdaptor<Long, Date> {

			@Override
			public Long fromInternalValue(Date dbvValue) {
				return (dbvValue == null) ? null : (long) (dbvValue.getYear() + 1900);
			}

			@Override
			public Date fromExternalValue(Long objectValue) {
				return (objectValue == null) ? null : new Date(objectValue.intValue() - 1900, 0, 1);
			}
		}

		class MyTable extends PropertyContainerImpl {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public IntegerProperty year;
		}
	}

	@Test
	@SuppressWarnings("deprecation")
	public void dateFieldAdaptedAsDBInteger_whenAdaptingOnSimpleTypes() {
		class MyTypeAdaptor implements TypeAdaptor<Date, Integer> {

			@Override
			public Date fromInternalValue(Integer dbvValue) {
				return (dbvValue == null) ? null : new Date(dbvValue - 1900, 0, 1);
			}

			@Override
			public Integer fromExternalValue(Date objectValue) {
				return (objectValue == null) ? null : objectValue.getYear() + 1900;
			}
		}

		class MyTable extends PropertyContainerImpl {

			@DBColumn
			@AdaptType(value = MyTypeAdaptor.class, type = IntegerProperty.class)
			public Date year;
		}
	}

	@Test
	public void dbdateFieldAdaptedAsDBInteger_whenAdaptingOnSimpleTypes() {
		@SuppressWarnings("deprecation")
		class MyTypeAdaptor implements TypeAdaptor<Date, Integer> {

			@Override
			public Date fromInternalValue(Integer dbvValue) {
				return (dbvValue == null) ? null : new Date(dbvValue - 1900, 0, 1);
			}

			@Override
			public Integer fromExternalValue(Date objectValue) {
				return (objectValue == null) ? null : objectValue.getYear() + 1900;
			}
		}

		class MyTable extends PropertyContainerImpl {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public DateProperty year;
		}
	}

	@Test
	public void complexFieldAdaptedAsDBString_whenAdaptingOnComplexPOJOTypes() {
		class MyDataType {

			public MyDataType parse(String str) {
				return new MyDataType();
			}

			@Override
			public String toString() {
				return MyDataType.class.getSimpleName();
			}
		}

		class MyTypeAdaptor implements TypeAdaptor<MyDataType, String> {

			@Override
			public MyDataType fromInternalValue(String dbvValue) {
				return (dbvValue == null) ? null : new MyDataType().parse(dbvValue);
			}

			@Override
			public String fromExternalValue(MyDataType objectValue) {
				return (objectValue == null) ? null : objectValue.toString();
			}
		}

		class MyTable extends PropertyContainerImpl {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public MyDataType obj;
		}
	}

	// not sure if need to support this
	@Test
	public void stringFieldAdaptedAsCustomQDT_whenAdaptingOnSimpleTypes() {

		class MyQDT extends AdaptableType<Integer> {

			@SuppressWarnings("unused")
			MyQDT() {
				super(23);
			}
		}

		class MyTypeAdaptor implements TypeAdaptor<String, Integer> {

			@Override
			public String fromInternalValue(Integer dbvValue) {
				return (dbvValue == null) ? null : dbvValue.toString();
			}

			@Override
			public Integer fromExternalValue(String objectValue) {
				return (objectValue == null) ? null : Integer.parseInt(objectValue);
			}
		}

		class MyTable extends PropertyContainerImpl {

			@DBColumn
			@AdaptType(value = MyTypeAdaptor.class, type = MyQDT.class)
			public String year;
		}
	}
}
