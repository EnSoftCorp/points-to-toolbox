package com.ensoftcorp.open.pointsto.utilities;

/**
 * A factory for generating unique "addresses" for each factory instance. An
 * address is an abstract notion and will depending on the implementation of the
 * points-to analysis. Typically an address will be used to differentiate
 * allocation sites for some given context.
 * 
 * @author Ben Holland
 */
public class AddressFactory {

	private long address = 0;
	
	public long getNewAddress(){
		return address++;
	}
	
}
