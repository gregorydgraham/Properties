package nz.co.gregs.properties;



import nz.co.gregs.properties.examples.IntegerProperty;
import nz.co.gregs.properties.examples.DBPropertyTypeHandler;
import nz.co.gregs.properties.examples.PropertyContainerImpl;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

@SuppressWarnings("serial")
public class PropertyWrapperDefinitionTest {

    @Test
    public void dotEqualsTrueWhenDifferentObjectAndSameClass() {
        class MyClass extends PropertyContainerImpl{

            public IntegerProperty intField1 = new IntegerProperty();
            public IntegerProperty intField2 = new IntegerProperty();

        }

        PropertyDefinition intField1_obj1 = propertyDefinitionOf(new MyClass(), "intField1");
        PropertyDefinition intField1_obj2 = propertyDefinitionOf(new MyClass(), "intField1");
        assertThat(intField1_obj1 == intField1_obj2, is(false));

        assertThat(intField1_obj1.equals(intField1_obj2), is(true));
    }

    @Test
    public void dotEqualsTrueWhenDifferentButIdenticalClass() {
        class MyClass1 extends PropertyContainerImpl {

            public IntegerProperty intField1 = new IntegerProperty();
            public IntegerProperty intField2 = new IntegerProperty();
        }

        class MyClass2 extends PropertyContainerImpl{

            public IntegerProperty intField1 = new IntegerProperty();
            public IntegerProperty intField2 = new IntegerProperty();
        }

        PropertyDefinition intField1_obj1 = propertyDefinitionOf(new MyClass1(), "intField1");
        PropertyDefinition intField1_obj2 = propertyDefinitionOf(new MyClass2(), "intField1");
        assertThat(intField1_obj1.equals(intField1_obj2), is(false));
    }

	@Test
	public void getsTableNameViaProperty() {
		class MyClass extends PropertyContainerImpl {
			public IntegerProperty intField1 = new IntegerProperty();
		}
		
		PropertyDefinition property = propertyDefinitionOf(new MyClass(), "intField1");
		assertThat(property.javaName(), is("intField1"));
	}
	
    private PropertyDefinition propertyDefinitionOf(PropertyContainer target, String javaPropertyName) {
        return propertyDefinitionOf(target.getClass(), javaPropertyName);
    }

    // note: intentionally doesn't use a wrapper factory for tests on equals() methods
    private PropertyDefinition propertyDefinitionOf(Class<? extends PropertyContainer> clazz, String javaPropertyName) {
        PropertyContainerClass classWrapper = new PropertyContainerClass(clazz,  new DBPropertyTypeHandler());
        return classWrapper.getPropertyDefinitionByName(javaPropertyName);
    }
}
