package nz.co.gregs.properties;



import nz.co.gregs.properties.adapt.AdaptableType;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

@SuppressWarnings("serial")
public class PropertyWrapperDefinitionTest {

    @Test
    public void dotEqualsTrueWhenDifferentObjectAndSameClass() {
        class MyClass implements ContainingClass{

            public MyInteger intField1 = new MyInteger();
            public MyInteger intField2 = new MyInteger();
        }

        PropertyWrapperDefinition intField1_obj1 = propertyDefinitionOf(new MyClass(), "intField1");
        PropertyWrapperDefinition intField1_obj2 = propertyDefinitionOf(new MyClass(), "intField1");
        assertThat(intField1_obj1 == intField1_obj2, is(false));

        assertThat(intField1_obj1.equals(intField1_obj2), is(true));
    }

    @Test
    public void dotEqualsTrueWhenDifferentButIdenticalClass() {
        class MyClass1 implements ContainingClass {

            public MyInteger intField1 = new MyInteger();
            public MyInteger intField2 = new MyInteger();
        }

        class MyClass2 implements ContainingClass{

            public MyInteger intField1 = new MyInteger();
            public MyInteger intField2 = new MyInteger();
        }

        PropertyWrapperDefinition intField1_obj1 = propertyDefinitionOf(new MyClass1(), "intField1");
        PropertyWrapperDefinition intField1_obj2 = propertyDefinitionOf(new MyClass2(), "intField1");
        assertThat(intField1_obj1.equals(intField1_obj2), is(false));
    }

	@Test
	public void getsTableNameViaProperty() {
		class MyClass implements ContainingClass {
			public MyInteger intField1 = new MyInteger();
		}
		
		PropertyWrapperDefinition property = propertyDefinitionOf(new MyClass(), "intField1");
		assertThat(property.javaName(), is("intField1"));
	}
	
    private PropertyWrapperDefinition propertyDefinitionOf(ContainingClass target, String javaPropertyName) {
        return propertyDefinitionOf(target.getClass(), javaPropertyName);
    }

    // note: intentionally doesn't use a wrapper factory for tests on equals() methods
    private PropertyWrapperDefinition propertyDefinitionOf(Class<? extends ContainingClass> clazz, String javaPropertyName) {
        ContainingClassWrapper classWrapper = new ContainingClassWrapper(clazz);
        return classWrapper.getPropertyDefinitionByName(javaPropertyName);
    }
}
