package nz.co.gregs.properties.adapt;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import nz.co.gregs.properties.examples.DBColumn;
import nz.co.gregs.properties.examples.DateProperty;
import nz.co.gregs.properties.examples.IntegerProperty;
import nz.co.gregs.properties.examples.PropertyContainerImpl;
import nz.co.gregs.properties.examples.StringProperty;
import nz.co.gregs.properties.JavaProperty;
import nz.co.gregs.properties.JavaPropertyFinder;
import nz.co.gregs.properties.exceptions.*;
import static nz.co.gregs.properties.PropertyMatchers.*;
import nz.co.gregs.properties.examples.DBPropertyTypeHandler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Focuses on low-level functionality of type adaptors.
 * End-to-end confirmation that it works when querying an actual
 * database is in {@link TypeAdaptorTest}.
 */
@SuppressWarnings({"serial","unused"})
public class PropertyTypeHandlerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void errorsOnConstructionGivenValidTypeAdaptorWithWrongExplicitDBvType()  {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=StringLongAdaptor.class, type=StringProperty.class)
			public StringProperty field = new StringProperty();
		}
		
		thrown.expect(InvalidDeclaredTypeException.class);
		thrown.expectMessage("internal Long type is not compatible");
		new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
	}

	@Test
	public void errorsOnConstructionGivenTypeAdaptorWithWrongExternalType()  {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=LongStringAdaptor.class)
			public StringProperty field = new StringProperty();
		}
		
		thrown.expect(InvalidDeclaredTypeException.class);
		thrown.expectMessage("external Long type is not compatible");
		new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
	}
	
	@Test
	public void errorsOnConstructionGivenInvalidAdaptorWithNonSimpleFirstType() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=DBStringIntegerAdaptor.class, type=IntegerProperty.class)
			public StringProperty field = new StringProperty();
		}
		
		thrown.expect(InvalidDeclaredTypeException.class);
		thrown.expectMessage("external type must not");
		new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
	}
	
	@Test
	public void errorsOnConstructionGivenInvalidAdaptorWithNonSimpleSecondType() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=IntegerDBIntegerAdaptor.class, type=IntegerProperty.class)
			public StringProperty field = new StringProperty();
		}
		
		thrown.expect(InvalidDeclaredTypeException.class);
		thrown.expectMessage("internal type must not");
		new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
	}
	
	@Test(expected=InvalidDeclaredTypeException.class)
	public void errorsOnConstructionGivenInterfaceTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=AdaptorInterface.class, type=IntegerProperty.class)
			public IntegerProperty field;
		}
		
		new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
	}

	@Test
	public void acceptsOnConstructionGivenValidTypeAdaptorWithImplicitDBvType() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=IntegerLongAdaptor.class)
			public Integer field;
		}
		
		PropertyTypeHandler propertyHandler = new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
		assertThat(propertyHandler, is(not(nullValue())));
	}
	
	@Test
	public void acceptsOnConstructionGivenValidTypeAdaptorWithCorrectExplicitDBvType() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=IntegerLongAdaptor.class, type=IntegerProperty.class)
			public Integer field;
		}
		
		PropertyTypeHandler propertyHandler =new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
		assertThat(propertyHandler, is(not(nullValue())));
	}

	@Test
	public void acceptsOnConstructionGivenValidTypeAdaptorWithExternalSimpleTypeUpcast() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=IntegerStringAdaptor.class)
			public Long field;
		}
		
		PropertyTypeHandler propertyHandler = new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
		assertThat(propertyHandler, is(not(nullValue())));
	}

	@Test
	public void acceptsOnConstructionGivenValidTypeAdaptorWithExternalSimpleTypeDowncast() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=LongStringAdaptor.class)
			public Integer field;
		}
		
		PropertyTypeHandler propertyHandler = new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
		assertThat(propertyHandler, is(not(nullValue())));
	}
	
	@Test
	public void acceptsOnConstructionGivenValidTypeAdaptorWithInternalSimpleTypeUpcast() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=StringIntegerAdaptor.class)
			public String field;
		}
		
		PropertyTypeHandler propertyHandler = new DBPropertyTypeHandler(propertyOf(MyClass.class, "field"), false);
		assertThat(propertyHandler, is(not(nullValue())));
	}
	
	@Test
	public void infersDBIntegerGivenStringLongAdaptorOnDBStringField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=StringLongAdaptor.class)
			@DBColumn
			public StringProperty field = new StringProperty("23");
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) IntegerProperty.class));
		assertThat(qdt, is(instanceOf(IntegerProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(Long.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new IntegerProperty(42));
	}
	
	@Test
	public void infersDBIntegerGivenStringLongAdaptorOnStringField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=StringLongAdaptor.class)
			@DBColumn
			public String field = "23";
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) IntegerProperty.class));
		assertThat(qdt, is(instanceOf(IntegerProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(Long.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new IntegerProperty(42));
	}

	@Test
	public void infersDBIntegerGivenStringIntegerAdaptorOnDBStringField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=StringIntegerAdaptor.class)
			@DBColumn
			public StringProperty field = new StringProperty("23");
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) IntegerProperty.class));
		assertThat(qdt, is(instanceOf(IntegerProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(Long.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new IntegerProperty(42));
	}
	
	@Test
	public void infersDBIntegerGivenStringIntegerAdaptorOnStringField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=StringIntegerAdaptor.class)
			@DBColumn
			public String field = "23";
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) IntegerProperty.class));
		assertThat(qdt, is(instanceOf(IntegerProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(Long.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new IntegerProperty(42));
	}
	
	@Test
	public void infersDBStringGivenLongStringAdaptorOnDBIntegerField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=LongStringAdaptor.class)
			@DBColumn
			public IntegerProperty field = new IntegerProperty(23);
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) StringProperty.class));
		assertThat(qdt, is(instanceOf(StringProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(String.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new StringProperty("42"));
	}

	@Test
	public void infersDBStringGivenLongStringAdaptorOnLongField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=LongStringAdaptor.class)
			@DBColumn
			public Long field = 23L;
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) StringProperty.class));
		assertThat(qdt, is(instanceOf(StringProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(String.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new StringProperty("42"));
	}
	
	@Test
	public void infersDBStringGivenLongStringAdaptorOnIntegerField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=LongStringAdaptor.class)
			@DBColumn
			public Integer field = 23;
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) StringProperty.class));
		assertThat(qdt, is(instanceOf(StringProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(String.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new StringProperty("42"));
	}

	@Test
	public void infersDBStringGivenIntegerStringAdaptorOnLongField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=IntegerStringAdaptor.class)
			@DBColumn
			public Long field = 23L;
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) StringProperty.class));
		assertThat(qdt, is(instanceOf(StringProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(String.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new StringProperty("42"));
	}
	
	@Test
	public void infersLongGivenDateLongAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=DateLongAdaptor.class)
			@DBColumn
			public DateProperty field = new DateProperty(new Date());
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) IntegerProperty.class));
		assertThat(qdt, is(instanceOf(IntegerProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(Long.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new IntegerProperty(42));
	}

	@Test
	public void infersDBDateGivenIntegerStringAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=LongDateAdaptor.class)
			@DBColumn
			public Long field = 2013L;
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) DateProperty.class));
		assertThat(qdt, is(instanceOf(DateProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(Date.class)));
		propertyHandler.setJavaPropertyAsQueryableDatatype(new MyClass(), new DateProperty(new Date()));
	}
	
	@Test
	public void acceptsDBIntegerGivenIntegerAdaptorAndExplicitType() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=StringIntegerAdaptor.class, type=IntegerProperty.class)
			@DBColumn
			public String field = "23";
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(propertyHandler.getType(), is((Object) IntegerProperty.class));
		assertThat(qdt, is(instanceOf(IntegerProperty.class)));
		assertThat(qdt.getValue(), is(instanceOf(Long.class)));
	}
        
        
	// Obsolete since IntegerProperty is now a separate type
//	@Test
//	public void acceptsDBNumberGivenIntegerAdaptorAndExplicitType() {
//		class MyClass extends PropertyContainerImpl {
//			@AdaptType(value=StringIntegerAdaptor.class, type=DBNumber.class)
//			@DBColumn
//			public String field = "23";
//		}
//		
//		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
//		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
//		assertThat(propertyHandler.getType(), is((Object) DBNumber.class));
//		assertThat(qdt, is(instanceOf(DBNumber.class)));
//		assertThat(qdt.getValue(), is(instanceOf(Double.class)));
//	}
	
	@Test
	public void getsQDTValueGivenValidFieldAndNoTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			public IntegerProperty field = new IntegerProperty();
		}
		
		MyClass myObj = new MyClass();
		myObj.field.setValue(23);
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		IntegerProperty qdt = (IntegerProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		assertThat(qdt.getValue().intValue(), is(23));
	}

	@Test
	public void getsUnchangedQDTInstanceGivenValidFieldAndNoTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			public IntegerProperty field = new IntegerProperty();
		}
		
		MyClass myObj = new MyClass();
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		IntegerProperty qdt = (IntegerProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		assertThat(qdt == myObj.field, is(true));
	}
	
	@Test
	public void getsNullQDTValueGivenValidNullQDTFieldAndNoTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			public IntegerProperty field = null;
		}
		
		MyClass myObj = new MyClass();
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		IntegerProperty qdt = (IntegerProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		assertThat(qdt, is(nullValue()));
	}

	@Test
	public void getsNullAdaptedQDTValueGivenNullQDTFieldAndTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=LongStringAdaptor.class)
			public IntegerProperty field = null;
		}
		
		MyClass myObj = new MyClass();
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		assertThat(qdt, is(nullValue()));
	}

	@Test
	public void getsIsNullAdaptedQDTValueGivenNullSimpleFieldAndTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=LongStringAdaptor.class)
			public Long field = null;
		}
		
		MyClass myObj = new MyClass();
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		assertThat(qdt, is(not(nullValue())));
		assertThat(qdt.isNull(), is(true));
	}
	
	@Test
	public void getsCorrectInternalValueTypeGivenIntegerStringAdaptorOnDBIntegerField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=LongStringAdaptor.class)
			@DBColumn
			public IntegerProperty field = new IntegerProperty();
		}
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		AdaptableType qdt = propertyHandler.getJavaPropertyAsAdaptableType(new MyClass());
		assertThat(qdt, is(instanceOf(StringProperty.class)));
	}

	@Test
	public void getsCorrectInternalValueGivenLongStringAdaptorOnDBIntegerField() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=LongStringAdaptor.class)
			@DBColumn
			public IntegerProperty field = new IntegerProperty();
		}
		
		MyClass myObj = new MyClass();
		myObj.field.setValue(23);
		
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		StringProperty qdt = (StringProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		
		assertThat(qdt.stringValue(), is("23"));
	}
	
	@Test
	public void getsSameInstanceOnConsecutiveReadsGivenAdaptorWhenUsingSameHandler() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=LongStringAdaptor.class)
			@DBColumn
			public IntegerProperty field = new IntegerProperty();
		}
		
		MyClass myObj = new MyClass();
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		StringProperty qdt1 = (StringProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		StringProperty qdt2 = (StringProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		StringProperty qdt3 = (StringProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		
		assertThat(qdt2 == qdt1, is(true));
		assertThat(qdt3 == qdt1, is(true));
	}

	@Test
	public void getsDifferentInstanceOnConsecutiveReadsAndWritesGivenAdaptorWhenUsingSameHandler() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=LongStringAdaptor.class)
			@DBColumn
			public IntegerProperty field = new IntegerProperty();
		}
		
		MyClass myObj = new MyClass();
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		StringProperty qdt1 = (StringProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		
		propertyHandler.setJavaPropertyAsQueryableDatatype(myObj, new StringProperty());
		StringProperty qdt2 = (StringProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		
		propertyHandler.setJavaPropertyAsQueryableDatatype(myObj, new StringProperty());
		StringProperty qdt3 = (StringProperty)propertyHandler.getJavaPropertyAsAdaptableType(myObj);
		
		assertThat(qdt2 == qdt1, is(false));
		assertThat(qdt3 == qdt1, is(false));
		assertThat(qdt3 == qdt2, is(false));
	}
	
	@Test
	public void setsFieldValueGivenValidFieldAndNoTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			public IntegerProperty field = new IntegerProperty();
		}
		
		MyClass myObj = new MyClass();
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		
		IntegerProperty qdt = new IntegerProperty();
		qdt.setValue(23);
		propertyHandler.setJavaPropertyAsQueryableDatatype(myObj, qdt);
		
		assertThat(myObj.field.getValue().intValue(), is(23));
	}

	@Test
	public void setsFieldValueGivenValidFieldAndTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(StringLongAdaptor.class)
			public StringProperty field = new StringProperty();
		}
		
		MyClass myObj = new MyClass();
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		
		IntegerProperty qdt = new IntegerProperty();
		qdt.setValue(23);
		propertyHandler.setJavaPropertyAsQueryableDatatype(myObj, qdt);
		
		assertThat(myObj.field.stringValue(), is("23"));
	}
	
	@Test
	public void setsUnchangedFieldReferenceGivenValidObjectAndNoTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			public IntegerProperty field = new IntegerProperty();
		}
		
		MyClass myObj = new MyClass();
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");

		IntegerProperty qdt = new IntegerProperty();
		propertyHandler.setJavaPropertyAsQueryableDatatype(myObj, qdt);
		
		assertThat(myObj.field == qdt, is(true));
	}
	
	@Test
	public void setsQDTFieldNullGivenNullQDTAndNoTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			public IntegerProperty field = new IntegerProperty();
		}
		
		MyClass myObj = new MyClass();
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");

		propertyHandler.setJavaPropertyAsQueryableDatatype(myObj, null);
		assertThat(myObj.field, is(nullValue()));
	}

	@Test
	public void setsQDTFieldNullGivenNullQDTAndTypeAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@DBColumn
			@AdaptType(value=IntegerStringAdaptor.class)
			public IntegerProperty field = new IntegerProperty();
		}
		
		MyClass myObj = new MyClass();
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");

		propertyHandler.setJavaPropertyAsQueryableDatatype(myObj, null);
		assertThat(myObj.field, is(nullValue()));
	}
	
	@Test
	public void setsAsDBIntegerWithLongGivenLongAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=StringLongAdaptor.class)
			@DBColumn
			public StringProperty field = new StringProperty();
		}

		MyClass obj = new MyClass();
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		propertyHandler.setJavaPropertyAsQueryableDatatype(obj, new IntegerProperty(42L));
		assertThat(obj.field.getValue(), is((Object)"42"));
	}

	@Test
	public void setsAsDBIntegerWithIntegerGivenLongAdaptor() {
		class MyClass extends PropertyContainerImpl {
			@AdaptType(value=StringLongAdaptor.class)
			@DBColumn
			public StringProperty field = new StringProperty();
		}

		MyClass obj = new MyClass();
		PropertyTypeHandler propertyHandler = propertyHandlerOf(MyClass.class, "field");
		propertyHandler.setJavaPropertyAsQueryableDatatype(obj, new IntegerProperty(42));
		assertThat(obj.field.getValue(), is((Object)"42"));
	}
	
	private PropertyTypeHandler propertyHandlerOf(Class<?> clazz, String javaPropertyName) {
		DBPropertyTypeHandler dbPropertyTypeHandler = new DBPropertyTypeHandler( propertyOf(clazz, javaPropertyName), false);
		return dbPropertyTypeHandler;		
	}
	
	private JavaProperty propertyOf(Class<?> clazz, String javaPropertyName) {
		List<JavaProperty> properties = new JavaPropertyFinder().getPropertiesOf(clazz);
		JavaProperty property = itemOf(properties, that(hasJavaPropertyName(javaPropertyName)));
		if (property == null) {
			throw new IllegalArgumentException("No public property found with java name '"+javaPropertyName+"'");
		}
		return property;
	}

	public static class IntegerStringAdaptor implements TypeAdaptor<Integer,String> {
		@Override
		public Integer fromInternalValue(String dbvValue) {
			if (dbvValue != null) {
				return Integer.parseInt(dbvValue);
			}
			return null;
		}

		@Override
		public String fromExternalValue(Integer objectValue) {
			if (objectValue != null) {
				return objectValue.toString();
			}
			return null;
		}
	}

	public static class StringIntegerAdaptor implements TypeAdaptor<String,Integer> {
		@Override
		public String fromInternalValue(Integer dbvValue) {
			if (dbvValue != null) {
				return dbvValue.toString();
			}
			return null;
		}

		@Override
		public Integer fromExternalValue(String objectValue) {
			if (objectValue != null) {
				return Integer.parseInt(objectValue);
			}
			return null;
		}
	}
	
	public static class LongStringAdaptor implements TypeAdaptor<Long,String> {
		@Override
		public Long fromInternalValue(String internalValue) {
			if (internalValue != null) {
				return Long.parseLong(internalValue);
			}
			return null;
		}

		@Override
		public String fromExternalValue(Long internalValue) {
			if (internalValue != null) {
				return internalValue.toString();
			}
			return null;
		}
	}

	public static class StringLongAdaptor implements TypeAdaptor<String,Long> {
		@Override
		public String fromInternalValue(Long dbvValue) {
			if (dbvValue != null) {
				return dbvValue.toString();
			}
			return null;
		}

		@Override
		public Long fromExternalValue(String objectValue) {
			if (objectValue != null) {
				return Long.parseLong(objectValue);
			}
			return null;
		}
	}

	public static class DateLongAdaptor implements TypeAdaptor<Date, Long> {
                @Override
		public Date fromInternalValue(Long dbvValue) {
			if (dbvValue != null) {
				Calendar c = Calendar.getInstance();
				c.clear();
				c.set(Calendar.YEAR, dbvValue.intValue());
				return c.getTime();
			}
			return null;
		}

                @Override
		public Long fromExternalValue(Date objectValue) {
			Calendar c = Calendar.getInstance();
			c.setTime(objectValue);
			return (long)c.get(Calendar.YEAR);
		}
	}

	public static class LongDateAdaptor implements TypeAdaptor<Long, Date> {
                @Override
		public Long fromInternalValue(Date dbvValue) {
			Calendar c = Calendar.getInstance();
			c.setTime(dbvValue);
			return (long)c.get(Calendar.YEAR);
		}

                @Override
		public Date fromExternalValue(Long objectValue) {
			if (objectValue != null) {
				Calendar c = Calendar.getInstance();
				c.clear();
				c.set(Calendar.YEAR, objectValue.intValue());
				return c.getTime();
			}
			return null;
		}
	}
	
	public static class IntegerLongAdaptor implements TypeAdaptor<Integer, Long> {
                @Override
		public Integer fromInternalValue(Long dbvValue) {
			return null;
		}

                @Override
		public Long fromExternalValue(Integer objectValue) {
			return null;
		}
	}
	
	public static class IntegerDBIntegerAdaptor implements TypeAdaptor<Integer, IntegerProperty> {
                @Override
		public Integer fromInternalValue(IntegerProperty dbvValue) {
			return null;
		}

                @Override
		public IntegerProperty fromExternalValue(Integer objectValue) {
			return null;
		}
	}

	public static class DBStringIntegerAdaptor implements TypeAdaptor<StringProperty, Integer> {
                @Override
		public StringProperty fromInternalValue(Integer dbvValue) {
			return null;
		}

                @Override
		public Integer fromExternalValue(StringProperty objectValue) {
			return null;
		}
	}
	
	public static interface AdaptorInterface extends TypeAdaptor<Object, AdaptableType> {
		// empty
	}
}
