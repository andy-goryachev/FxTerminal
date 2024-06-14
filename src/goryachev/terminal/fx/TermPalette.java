// Copyright © 2018-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal.fx;
import goryachev.terminal.TermColor;
import javafx.scene.paint.Color;


/**
 * Terminal Palette Manager.
 */
public class TermPalette
{
	private Color fg;
	private Color bg;
	
	
	public TermPalette()
	{
		reset();
	}
	
	
	public void reset()
	{
		fg = Color.BLACK;
		bg = Color.WHITE;
	}
	
	
	protected Color translateColor(TermColor c)
	{
		switch(c)
		{
		case BLACK:
			return Color.BLACK;
		case BLUE:
			return Color.BLUE;
		case CYAN:
			return Color.CYAN;
		case GREEN:
			return Color.GREEN;
		case MAGENTA:
			return Color.MAGENTA;
		case RED:
			return Color.RED;
		case WHITE:
			return Color.WHITE;
		case YELLOW:
			return Color.YELLOW;
		default:
			throw new Error("?" + c);
		}
	}
	
	
	public void setBackground(TermColor c)
	{
		bg = translateColor(c);
	}
	
	
	public void setForeground(TermColor c)
	{
		fg = translateColor(c);
	}
	
	
	public Color getBackground()
	{
		return bg;
	}
	
	
	public Color getForeground()
	{
		return fg;
	}
}