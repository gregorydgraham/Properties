package nz.co.gregs.properties.examples;

import nz.co.gregs.properties.PropertyContainer;
import nz.co.gregs.properties.PropertyContainerWrapperFactory;

/**
 * Minimalist PropertyContainer Implementation.
 * 
 * @author gregorygraham
 */
public class PropertyContainerImpl extends PropertyContainer{
	
	private static final PropertyContainerWrapperFactory dbFactory = new PropertyContainerWrapperFactoryImpl();

  /**
   * Default Constructor
   */
  public PropertyContainerImpl() {
		super(dbFactory);
	}
	
}
