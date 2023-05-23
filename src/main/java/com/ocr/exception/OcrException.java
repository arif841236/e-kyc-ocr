package com.ocr.exception;

/**
 * This class for exception and its extends to runtime exception
 */
public class OcrException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OcrException() {
	}

	public OcrException(String msg) {
		super(msg);
	}
}
