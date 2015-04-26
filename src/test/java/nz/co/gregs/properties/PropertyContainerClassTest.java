package nz.co.gregs.properties;

import nz.co.gregs.properties.examples.DBColumn;
import nz.co.gregs.properties.examples.IntegerProperty;
import nz.co.gregs.properties.examples.DBForeignKey;
import nz.co.gregs.properties.examples.DBPrimaryKey;
import nz.co.gregs.properties.examples.StringProperty;
import java.sql.SQLException;
import java.util.List;
import nz.co.gregs.properties.examples.DBPropertyTypeHandler;
import nz.co.gregs.properties.examples.PropertyContainerImpl;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("warnings")
public class PropertyContainerClassTest {

//    private static DBDatabase database;
	@BeforeClass
	public static void setup() throws SQLException {
//        database = new H2MemoryDB("dbvolutionTest", "", "", false);
	}

	@Test
	public void getsProperties() {
		PropertyContainerClass classAdaptor = new PropertyContainerClass(MyTable1.class, new DBPropertyTypeHandler());
		List<PropertyDefinition> propertyDefinitions = classAdaptor.getPropertyDefinitions();
		for (PropertyDefinition propertyDefinition : propertyDefinitions) {
			System.out.println(propertyDefinition.qualifiedJavaName());
		}
		assertThat(classAdaptor.getPropertyDefinitions().size(), is(3));
	}

	@SuppressWarnings("serial")
	public static class MyTable1 extends PropertyContainerImpl {

		@DBPrimaryKey
		@DBColumn
		public IntegerProperty uid = new IntegerProperty();
		@DBColumn("table_text")
		public StringProperty text = new StringProperty();
		@DBColumn
		@DBForeignKey(value = MyTable2.class)
		public IntegerProperty fkTable2 = new IntegerProperty();
	}

	@SuppressWarnings("serial")
	public static class MyTable2 extends PropertyContainerImpl {

		@DBPrimaryKey
		@DBColumn("uid_2")
		public IntegerProperty uid = new IntegerProperty();
	}
}
