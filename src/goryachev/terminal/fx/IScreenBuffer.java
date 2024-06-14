// Copyright © 2018-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal.fx;


/**
 * Terminal Screen Buffer Interface.
 */
public interface IScreenBuffer
{
	public int size();
	
	public int getCapacity();
	
	public ScreenLine get(int ix);
	
	public void add(ScreenLine item);

	/** scroll the specified area up or down.  (bottom - top - 1) lines get shifted, and one empty line added */
	public void scroll(int top, int bottom, boolean up);

	/** update buffer to reflect new size */
	public void updateSize(int colCount, int rowCount);
}
