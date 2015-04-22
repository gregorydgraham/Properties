package nz.co.gregs.properties;



import nz.co.gregs.properties.examples.DBColumn;
import nz.co.gregs.properties.examples.DBRow;
import nz.co.gregs.properties.examples.DBInteger;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

@SuppressWarnings("serial")
public class PropertyWrapperTest {
	@Test
	public void dotEqualsFalseWhenSameFieldOnSameObjectButRetrievedSeparately() {
		class MyClass extends DBRow {
			@DBColumn
			public DBInteger intField1 = new DBInteger();

			@DBColumn
			public DBInteger intField2 = new DBInteger();
		}
		
		MyClass obj = new MyClass();
		Property intField1_obj1 = propertyOf(obj, "intField1");
		Property intField1_obj2 = propertyOf(obj, "intField1");
		
		assertThat(intField1_obj1 == intField1_obj2, is(false));
		assertThat(intField1_obj1.equals(intField1_obj2), is(true));
	}
	
	@Test
	public void dotEqualsFalseWhenSameFieldOnDifferentObject() {
		class MyClass extends DBRow {
			@DBColumn
			public DBInteger intField1 = new DBInteger();

			@DBColumn
			public DBInteger intField2 = new DBInteger();
		}
		
		Property intField1_obj1 = propertyOf(new MyClass(), "intField1");
		Property intField1_obj2 = propertyOf(new MyClass(), "intField1");
		assertThat(intField1_obj1 == intField1_obj2, is(false));
		
		assertThat(intField1_obj1.equals(intField1_obj2), is(false));
	}

	@Test
	public void dotEqualsTrueWhenDifferentButIdenticalClass() {
		class MyClass1 extends DBRow {
			@DBColumn
			public DBInteger intField1 = new DBInteger();

			@DBColumn
			public DBInteger intField2 = new DBInteger();
		}

		class MyClass2 extends DBRow {
			@DBColumn
			public DBInteger intField1 = new DBInteger();

			@DBColumn
			public DBInteger intField2 = new DBInteger();
		}
		
		Property intField1_obj1 = propertyOf(new MyClass1(), "intField1");
		Property intField1_obj2 = propertyOf(new MyClass2(), "intField1");
		assertThat(intField1_obj1.equals(intField1_obj2), is(false));
	}

	// note: intentionally doesn't use a wrapper factory for tests on equals() methods
	private Property propertyOf(PropertyContainer target, String javaPropertyName) {
		PropertyContainerClass classWrapper = new PropertyContainerClass(target.getClass());
		return classWrapper.instanceWrapperFor(target).getPropertyByName(javaPropertyName);
	}
}
