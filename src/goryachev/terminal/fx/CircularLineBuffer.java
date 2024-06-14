// Copyright © 2018-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal.fx;
import goryachev.common.util.CircularBuffer;


/**
 * Circular Line Buffer.
 */
public class CircularLineBuffer
	implements IScreenBuffer
{
	protected final CircularBuffer<ScreenLine> buffer;

	
	public CircularLineBuffer(int capacity)
	{
		buffer = new CircularBuffer<ScreenLine>(capacity);
	}


	public int size()
	{
		return buffer.size();
	}
	
	
	public int getCapacity()
	{
		return buffer.getCapacity();
	}


	public ScreenLine get(int ix)
	{
		if(ix < 0)
		{
			return null;
		}
		return buffer.get(ix);
	}


	public void add(ScreenLine item)
	{
		buffer.add(item);
	}


	public void scroll(int top, int bottom, boolean up)
	{
		throw new Error("implement scroll!");
	}
	
	
	public void updateSize(int colCount, int rowCount)
	{
		throw new Error("implement updateSize!");
	}
}
