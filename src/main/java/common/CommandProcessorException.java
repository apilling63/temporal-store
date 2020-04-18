package common;

/**
 * Simple exception class to deal with passing exception message to client
 *
 */
public class CommandProcessorException extends Exception {

	public CommandProcessorException(String message) {
		super(message);
	}
}
