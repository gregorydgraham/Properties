/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.gregs.properties.exceptions;

/** Thrown internally when a Type is not supported by a method */
public class UnsupportedType extends Exception {
	private static final long serialVersionUID = 1L;

  /**
   * Thrown internally when a Type is not supported by a method.
   * 
   * @param message the message explaining the cause of the exception
   */
  public UnsupportedType(String message) {
		super(message);
	}
	
}
