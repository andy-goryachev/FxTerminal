// Copyright © 2018-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;


/**
 * Terminal View Interface.
 */
public interface ITermView
{
	public int getRowCount();

	
	public int getColumnCount();
	
	
	/** plays audio or provides visual indication, or both, or none */
	public void bell();
	
	
	/** 
	 * draws a code point on the terminal screen and returns:
	 * <ul>
	 * <li>
	 * 0 .. 2 the number of cells the code point occupies, or
	 * <li>
	 * -1 when the glyph did not fit on the line and should be drawn on the next line
	 * (no glyph is drawn on the current line) 
	 * </ul>
	 */
	public int draw(int x, int y, int codePoint);


	public void scroll(int top, int bottom, boolean up);


	/** clear line from position x to the right edge of the screen */
	public void clearLine(int x, int y);
	
	
	/** hides cursor before an update */
	public void hideCursor();
	
	
	/** sets the new cursor screen position, showing it if it was hidden before */
	public void showCursor(int x, int y);
	
	
	public void clearAttributes();
	
	
	public void setBlink();
	
	
	public void setBold();
	
	
	public void setConcealed();
	
	
	public void setReversed();
	
	
	public void setUnderscore();
	
	
	public void setBackground(TermColor c);
	
	
	public void setForeground(TermColor c);
}
