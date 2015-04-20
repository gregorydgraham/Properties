package nz.co.gregs.properties.exceptions;

/**
 * Thrown when unexpected errors occur.
 *
 * <p>
 * DBvolution can only cope with so much, and when it reaches breaking point it
 * throws a DBRuntime exception.
 *
 * <p>
 PropertyException should not be thrown directly, please sub-class it and add
 information to the exception thrown to help developers improve their code.
 *
 * @author Malcolm Lett
 */
public class PropertyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Thrown when unexpected errors occur.
	 *
	 */
	public PropertyException() {
	}

	/**
	 * Thrown when unexpected errors occur.
	 *
	 * @param message	 message	
	 */
	public PropertyException(String message) {
		super(message);
	}

	/**
	 * Thrown when unexpected errors occur.
	 *
	 * @param cause	 cause	
	 */
	public PropertyException(Throwable cause) {
		super(cause);
	}

	/**
	 * Thrown when unexpected errors occur.
	 *
	 * @param message message
	 * @param cause cause
	 */
	public PropertyException(String message, Throwable cause) {
		super(message, cause);
	}

//	public PropertyException(String message, Throwable cause,
//			boolean enableSuppression, boolean writableStackTrace) {
//		super(message, cause, enableSuppression, writableStackTrace);
//	}
}
