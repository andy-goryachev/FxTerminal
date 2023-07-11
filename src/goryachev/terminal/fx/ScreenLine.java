// Copyright © 2018-2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal.fx;
import goryachev.common.util.SB;
import java.util.BitSet;
import javafx.scene.paint.Color;


/**
 * Terminal Screen Line.
 */
public class ScreenLine
{
	private int width;
	private boolean modified;
	private boolean forceUpdate;
	private boolean empty = true;
	private BitSet updated;
	private String[] cells;
	private Color[] foregrounds; // TODO or color providers?
	private Color[] backgrounds;
	private short[] flags;
	
	
	public ScreenLine(int width)
	{
		updated = new BitSet(width);
		cells = new String[width];
		foregrounds = new Color[width];
		backgrounds = new Color[width];
		flags = new short[width];
		forceUpdate = true;
	}
	
	
	public boolean isModified()
	{
		return forceUpdate || modified;
	}
	
	
	public boolean isForceUpdate()
	{
		return forceUpdate;
	}
	
	
	public void setForceUpdate()
	{
		forceUpdate = true;
	}
	
	
	public void clearModified()
	{
		modified = false;
		forceUpdate = false;
	}
	
	
	public boolean isEmpty()
	{
		return empty;
	}
	
	
	public void setWidth(int w)
	{
		// TODO
		if(w > capacity())
		{
			grow(w);
			forceUpdate = true;
		}
		
		this.width = w;
	}
	
	
	protected void grow(int w)
	{
		BitSet up = updated.get(0, width);
		
		String[] cs = new String[w];
		System.arraycopy(cells, 0, cs, 0, width);
		
		Color[] fg = new Color[w];
		System.arraycopy(foregrounds, 0, fg, 0, width);
		
		Color[] bg = new Color[w];
		System.arraycopy(backgrounds, 0, bg, 0, width);
		
		short[] ff = new short[w];
		System.arraycopy(flags, 0, ff, 0, width);
		
		updated = up;
		cells = cs;
		foregrounds = fg;
		backgrounds = bg;
		flags = ff;
	}
	
	
	protected int capacity()
	{
		return cells.length;
	}
	

	public void setCell(int x, String text, Color bg, Color fg, int flag)
	{
		cells[x] = text;
		backgrounds[x] = bg;
		foregrounds[x] = fg;
		flags[x] = (short)flag;
		
		updated.set(x);
		modified = true;
		empty = false;
	}
	

	public void clearFrom(int x)
	{
		for(int i=cells.length-1; i>=x; --i)
		{
			cells[i] = null;
			backgrounds[i] = null;
			foregrounds[i] = null;
			flags[i] = 0;
			updated.set(i);
		}
		
		empty = (x == 0);
	}
	
	
	@Deprecated
	protected boolean getFlag(int x, Flag flag)
	{
		return (flags[x] & (1 << flag.ordinal())) != 0;
	}


	@Deprecated
	public void dump()
	{
		SB sb = new SB();
		for(int i=0; i<cells.length; i++)
		{
			sb.append(cells[i]);
			
			for(Flag f: Flag.values())
			{
				if(getFlag(i, f))
				{
					sb.append("<").append(f).append(">");
				}
			}
		}
	}


	public void getCell(CellInfo inf, int x)
	{
		inf.text = cells[x];
		inf.bg = backgrounds[x];
		inf.fg = foregrounds[x];
		inf.flags = flags[x];
	}
	
	
	/** 
	 * returns a populated CellInfo if the cell is updated, or null otherwise.
	 * note: this method clears the updated flag.
	 */
	public CellInfo getUpdatedCell(CellInfo inf, int x)
	{
		if(forceUpdate || updated.get(x))
		{
			updated.clear(x);
			
			inf.text = cells[x];
			inf.bg = backgrounds[x];
			inf.fg = foregrounds[x];
			inf.flags = flags[x];
			return inf;
		}
		return null;
	}


	public void setCellUpdated(int x)
	{
		updated.set(x);
		modified = true;
	}
}
