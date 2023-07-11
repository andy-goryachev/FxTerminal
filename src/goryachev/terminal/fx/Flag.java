// Copyright © 2018-2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal.fx;


/**
 * Text Attribute Flag.
 */
public enum Flag
{
	BLINK,
	BOLD,
	ITALIC,
	REVERSE,
	UNDERSCORE;
	
	//
	
	private final int bit;
	
	
	private Flag()
	{
		bit = (1 << ordinal());
	}
	
	
	public int bit()
	{
		return bit;
	}
	
	
	public int mask()
	{
		return ~bit;	
	}
	
	
	public boolean is(short flags)
	{
		return (bit & flags) != 0;
	}
}