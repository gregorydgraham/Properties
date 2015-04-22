package nz.co.gregs.properties.adapt;


import java.util.Date;
import nz.co.gregs.properties.examples.DBColumn;
import nz.co.gregs.properties.examples.DBDate;
import nz.co.gregs.properties.examples.DBInteger;
import nz.co.gregs.properties.examples.DBRow;
import nz.co.gregs.properties.examples.DBString;
import nz.co.gregs.properties.examples.DBTableName;
import nz.co.gregs.properties.adapt.*;

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
			public Integer fromDatabaseValue(Integer dbvValue) {
				return dbvValue;
			}

			@Override
			public Integer toDatabaseValue(Integer objectValue) {
				return objectValue;
			}
		}

		@DBTableName("Customer")
		class MyTable extends DBRow {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public Integer year;
		}
	}

	@Test
	public void stringFieldAdaptedAsDBInteger_whenAdaptingOnSimpleTypes() {
		class MyTypeAdaptor implements TypeAdaptor<String, Long> {

			@Override
			public String fromDatabaseValue(Long dbvValue) {
				return (dbvValue == null) ? null : dbvValue.toString();
			}

			@Override
			public Long toDatabaseValue(String objectValue) {
				return (objectValue == null) ? null : Long.parseLong(objectValue);
			}
		}

		@DBTableName("Customer")
		class MyTable extends DBRow {

			@DBColumn
			@AdaptType(value = MyTypeAdaptor.class, type = DBInteger.class)
			public String year;
		}
	}

	@Test
	public void dbstringFieldAdaptedAsDBInteger_whenAdaptingOnSimpleTypes() {
		class MyTypeAdaptor implements TypeAdaptor<String, Long> {

			@Override
			public String fromDatabaseValue(Long dbvValue) {
				return (dbvValue == null) ? null : dbvValue.toString();
			}

			@Override
			public Long toDatabaseValue(String objectValue) {
				return (objectValue == null) ? null : Long.parseLong(objectValue);
			}
		}

		@DBTableName("Customer")
		class MyTable extends DBRow {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public DBString year;
		}
	}

	@Test
	public void integerFieldAdaptedAsDBDate_whenAdaptingOnSimpleTypes() {
		@SuppressWarnings("deprecation")
		class MyTypeAdaptor implements TypeAdaptor<Integer, Date> {

			@Override
			public Integer fromDatabaseValue(Date dbvValue) {
				return (dbvValue == null) ? null : dbvValue.getYear() + 1900;
			}

			@Override
			public Date toDatabaseValue(Integer objectValue) {
				return (objectValue == null) ? null : new Date(objectValue - 1900, 0, 1);
			}
		}

		@DBTableName("Customer")
		class MyTable extends DBRow {

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
			public Long fromDatabaseValue(Date dbvValue) {
				return (dbvValue == null) ? null : (long) (dbvValue.getYear() + 1900);
			}

			@Override
			public Date toDatabaseValue(Long objectValue) {
				return (objectValue == null) ? null : new Date(objectValue.intValue() - 1900, 0, 1);
			}
		}

		@DBTableName("Customer")
		class MyTable extends DBRow {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public DBInteger year;
		}
	}

	@Test
	@SuppressWarnings("deprecation")
	public void dateFieldAdaptedAsDBInteger_whenAdaptingOnSimpleTypes() {
		class MyTypeAdaptor implements TypeAdaptor<Date, Integer> {

			@Override
			public Date fromDatabaseValue(Integer dbvValue) {
				return (dbvValue == null) ? null : new Date(dbvValue - 1900, 0, 1);
			}

			@Override
			public Integer toDatabaseValue(Date objectValue) {
				return (objectValue == null) ? null : objectValue.getYear() + 1900;
			}
		}

		@DBTableName("Customer")
		class MyTable extends DBRow {

			@DBColumn
			@AdaptType(value = MyTypeAdaptor.class, type = DBInteger.class)
			public Date year;
		}
	}

	@Test
	public void dbdateFieldAdaptedAsDBInteger_whenAdaptingOnSimpleTypes() {
		@SuppressWarnings("deprecation")
		class MyTypeAdaptor implements TypeAdaptor<Date, Integer> {

			@Override
			public Date fromDatabaseValue(Integer dbvValue) {
				return (dbvValue == null) ? null : new Date(dbvValue - 1900, 0, 1);
			}

			@Override
			public Integer toDatabaseValue(Date objectValue) {
				return (objectValue == null) ? null : objectValue.getYear() + 1900;
			}
		}

		@DBTableName("Customer")
		class MyTable extends DBRow {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public DBDate year;
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
			public MyDataType fromDatabaseValue(String dbvValue) {
				return (dbvValue == null) ? null : new MyDataType().parse(dbvValue);
			}

			@Override
			public String toDatabaseValue(MyDataType objectValue) {
				return (objectValue == null) ? null : objectValue.toString();
			}
		}

		@DBTableName("Customer")
		class MyTable extends DBRow {

			@DBColumn
			@AdaptType(MyTypeAdaptor.class)
			public MyDataType obj;
		}
	}

	// not sure if need to support this
	@Test
	public void stringFieldAdaptedAsCustomQDT_whenAdaptingOnSimpleTypes() {

		class MyQDT extends AdaptableType {

			@SuppressWarnings("unused")
			MyQDT() {
				super(23);
			}

			public Object getValue() {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}

			public void setValue(Object object) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
		}

		class MyTypeAdaptor implements TypeAdaptor<String, Integer> {

			@Override
			public String fromDatabaseValue(Integer dbvValue) {
				return (dbvValue == null) ? null : dbvValue.toString();
			}

			@Override
			public Integer toDatabaseValue(String objectValue) {
				return (objectValue == null) ? null : Integer.parseInt(objectValue);
			}
		}

		@DBTableName("Customer")
		class MyTable extends DBRow {

			@DBColumn
			@AdaptType(value = MyTypeAdaptor.class, type = MyQDT.class)
			public String year;
		}
	}
}