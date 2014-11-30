package org.jhu.metagenomics.alignmentportal.exceptions;

public class JobProcessingFailedException extends Exception {

	private static final long serialVersionUID = 4030455624221966967L;

	public JobProcessingFailedException(String msg) {
		super(msg);
	}
	
	public JobProcessingFailedException(String msg, Throwable t) {
		super(msg, t);
	}
}	
