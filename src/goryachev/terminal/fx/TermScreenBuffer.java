// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal.fx;
import goryachev.common.log.Log;


/**
 * Terminal Screen Buffer.
 * 
 * Access to screen buffer must be exterally synchronized.
 */
public class TermScreenBuffer
{
	protected static final Log log = Log.get("TermScreenBuffer");
	protected int columnCount;
	protected ScreenLine[] lines;
	
	
	public TermScreenBuffer()
	{
	}
	
	
	// FIX flicker on resize?
	public int resize(int colCount, int rowCount)
	{
		log.debug("cols=%d, rows=%d", colCount, rowCount);
		
		int ct = nonEmptyRowCount();
		
		int start;
		if(ct > rowCount)
		{
			start = ct - rowCount;
		}
		else
		{
			start = 0;
		}
		
		int lineCount = lines == null ? 0 : lines.length;
		
		ScreenLine[] ls = new ScreenLine[rowCount];
		for(int i=0; i<rowCount; i++)
		{
			int ix = i + start;
			ScreenLine s;
			if(ix < lineCount)
			{
				s = lines[ix];
				s.setWidth(colCount);
			}
			else
			{
				s = new ScreenLine(colCount);
			}
			s.setForceUpdate();
			ls[i] = s;
		}
		
		this.lines = ls;
		this.columnCount = colCount;
		
		// FIX when height is reduced, discard top rows and update cursor
		return start;
	}
	
	
	protected int nonEmptyRowCount()
	{
		if(lines != null)
		{
			for(int i=lines.length-1; i>=0; i--)
			{
				if(!lines[i].isEmpty())
				{
					return (i+1); 
				}
			}
		}
		return 0;
	}
	
	
	public void getCell(CellInfo inf, int x, int y)
	{
		ScreenLine t = lines[y];
		t.getCell(inf, x);
	}
	
	
	public void clearLineFrom(int x, int y)
	{
		if(y < lines.length)
		{
			ScreenLine t = lines[y];
			t.clearFrom(x);
		}
		else
		{
			log.error("line=%d, total=%d", y, lines.length);
		}
	}
	

	public void scroll(int top, int bottom, boolean up)
	{
		// TODO check
		
		if(up)
		{
			for(int i=top+1; i<bottom; i++)
			{
				ScreenLine s = lines[i];
				s.setForceUpdate();
				lines[i - 1] = s;
			}
			lines[bottom - 1] = new ScreenLine(columnCount); 
		}
		else
		{
			for(int i=bottom-1; i>=top; --i)
			{
				ScreenLine s = lines[i - 1];
				s.setForceUpdate();
				lines[i] = s;
			}
			lines[top] = new ScreenLine(columnCount);
		}
	}


	public ScreenLine getScreenLine(int y)
	{
		return lines[y];
	}


	public void updateCursor(int x, int y, boolean on)
	{
		if(x < 0)
		{
			return;
		}
		else if(y < 0)
		{
			return;
		}
		
		if(y < lines.length)
		{
			ScreenLine s = lines[y];
			s.setCellUpdated(x);
		}
		else
		{
			log.error("line buffer len=%d, y=%d", lines.length, y);
		}
	}


	public void forceUpdateAll()
	{
		for(ScreenLine s: lines)
		{
			s.setForceUpdate();
		}
	}
}
