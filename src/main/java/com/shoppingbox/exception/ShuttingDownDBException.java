package com.shoppingbox.exception;

public class ShuttingDownDBException extends RuntimeException{
	
	public ShuttingDownDBException(){
		super("DB is shutting down...");
	}

}
