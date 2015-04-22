package nz.co.gregs.properties;

import nz.co.gregs.properties.examples.DBColumn;
import nz.co.gregs.properties.examples.DBInteger;
import nz.co.gregs.properties.examples.DBTableName;
import nz.co.gregs.properties.examples.DBForeignKey;
import nz.co.gregs.properties.examples.DBPrimaryKey;
import nz.co.gregs.properties.examples.DBString;
import java.sql.SQLException;
import java.util.List;
import java.util.Observer;
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
		PropertyContainerClass classAdaptor = new PropertyContainerClass(MyTable1.class);
		List<PropertyDefinition> propertyDefinitions = classAdaptor.getPropertyDefinitions();
		for (PropertyDefinition propertyDefinition : propertyDefinitions) {
			System.out.println(propertyDefinition.qualifiedJavaName());
		}
		assertThat(classAdaptor.getPropertyDefinitions().size(), is(3));
	}

	@SuppressWarnings("serial")
	public static class MyTable1 extends PropertyContainer {

		@DBPrimaryKey
		@DBColumn
		public DBInteger uid = new DBInteger();
		@DBColumn("table_text")
		public DBString text = new DBString();
		@DBColumn
		@DBForeignKey(value = MyTable2.class)
		public DBInteger fkTable2 = new DBInteger();
	}

	@SuppressWarnings("serial")
	@DBTableName("table2")
	public static class MyTable2 extends PropertyContainer {

		@DBPrimaryKey
		@DBColumn("uid_2")
		public DBInteger uid = new DBInteger();
	}
}
