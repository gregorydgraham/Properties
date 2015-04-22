package nz.co.gregs.properties;



import java.sql.SQLException;
import nz.co.gregs.properties.PropertyContainer;
import nz.co.gregs.properties.PropertyContainerClass;
import nz.co.gregs.properties.DBForeignKey;
import nz.co.gregs.properties.DBColumn;
import nz.co.gregs.properties.DBInteger;
import nz.co.gregs.properties.DBPrimaryKey;
import nz.co.gregs.properties.DBRow;
import nz.co.gregs.properties.DBString;
import nz.co.gregs.properties.DBTableName;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("warnings")
public class DBRowClassWrapperTest {

//    private static DBDatabase database;

    @BeforeClass
    public static void setup() throws SQLException {
//        database = new H2MemoryDB("dbvolutionTest", "", "", false);
    }

    @SuppressWarnings("serial")
    @Test(expected = UnsupportedOperationException.class)
    public void errorsWhenConstructingGivenTwoPrimaryKeyColumns() {
        @DBTableName("table1")
        class TestClass extends DBRow {

            @DBPrimaryKey
            @DBColumn
            public DBInteger uid = new DBInteger();
            @DBPrimaryKey
            @DBColumn("table_text")
            public DBString text = new DBString();
            @DBColumn
            @DBForeignKey(value = MyTable2.class)
            public DBInteger fkTable2 = new DBInteger();
        }

        new PropertyContainerClass(TestClass.class);
    }

//	@Test
//	public void getsPrimaryKeyPropertiesGivenTwoPrimaryKeyColumns() {
//		@DBTableName("table2")
//		class TestClass extends DBRow {
//			@DBPrimaryKey
//			@DBColumn("uid_2")
//			public DBInteger uid = new DBInteger();
//
//			@DBPrimaryKey
//			@DBColumn
//			public DBInteger type = new DBInteger();
//		}
//		
//		PropertyContainerClass classWrapper = new PropertyContainerClass(TestClass.class);
//		assertThat(classWrapper.primaryKey(), is(not(nullValue())));
//		assertThat(classWrapper.primaryKey().size(), is(2));
//		assertThat(classWrapper.primaryKey().get(0).getColumnName(), is("uid_2"));
//		assertThat(classWrapper.primaryKey().get(1).getColumnName(), is("type"));
//	}
    @Test
    public void getsProperties() {
        PropertyContainerClass classAdaptor = new PropertyContainerClass(MyTable1.class);
        assertThat(classAdaptor.getPropertyDefinitions().size(), is(3));
    }

    @SuppressWarnings("serial")
    public static class MyTable1 implements PropertyContainer {

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
    public static class MyTable2 implements PropertyContainer {

        @DBPrimaryKey
        @DBColumn("uid_2")
        public DBInteger uid = new DBInteger();
    }
}
