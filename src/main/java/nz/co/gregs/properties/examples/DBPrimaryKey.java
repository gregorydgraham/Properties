/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.examples;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An example implementation representing a Primary Key relationship in an ORM layer.
 * 
 * <p>Note that the PK is a String in this example, an integer is more common. Properties with work with either, and others as well.</p>
 *
 * @author Gregory Graham
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DBPrimaryKey {

  /**
   * The name of the primary key column
   *
   * @return the name of the primary key column
   */
  String value() default "";
	
}
