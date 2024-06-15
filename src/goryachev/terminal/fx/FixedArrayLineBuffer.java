// Copyright © 2018-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal.fx;


/**
 * Fixed Array Line Buffer.
 */
public class FixedArrayLineBuffer
	implements IScreenBuffer
{
	protected final int columnCount;
	protected ScreenLine[] lines;
	
	
	public FixedArrayLineBuffer(int rowCount, int columnCount)
	{
		this.lines = new ScreenLine[rowCount];
		this.columnCount = columnCount;
		
		for(int i=0; i<rowCount; i++)
		{
			lines[i] = new ScreenLine(columnCount);
		}
	}
	
	
	@Override
	public int size()
	{
		return lines.length;
	}


	@Override
	public int getCapacity()
	{
		return lines.length;
	}


	@Override
	public ScreenLine get(int ix)
	{
		return lines[ix];
	}


	@Override
	public void add(ScreenLine item)
	{
		throw new Error();
	}


	@Override
	public void scroll(int top, int bottom, boolean up)
	{
		// TODO check
		
		if(up)
		{
			for(int i=top+1; i<bottom; i++)
			{
				lines[i - 1] = lines[i];
			}
			lines[bottom - 1] = new ScreenLine(columnCount); 
		}
		else
		{
			for(int i=bottom-1; i>=top; --i)
			{
				lines[i] = lines[i - 1];
			}
			lines[top] = new ScreenLine(columnCount);
		}
	}
	
	
	@Override
	public void updateSize(int colCount, int rowCount)
	{
		ScreenLine[] ls = new ScreenLine[rowCount];

		int delta = rowCount - size();
		
		if(delta != 0)
		{
			if(delta < 0)
			{
				// TODO move to scroll buffer
				
				int src = -delta;
				for(int i=0; i<rowCount; i++)
				{
					ls[i] = lines[src];
					src++;
				}
			}
			else if(delta > 0)
			{
				for(int i=0; i<size(); i++)
				{
					ls[i] = lines[i];
				}
				
				for(int i=size(); i<rowCount; i++)
				{
					ls[i] = new ScreenLine(colCount);
				}
			}
			
			lines = ls;
		}
	}
}
