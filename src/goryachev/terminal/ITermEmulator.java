// Copyright © 2023-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;


/**
 * Terminal Emulator Interface.
 */
public interface ITermEmulator
{
	public void setView(ITermView view);

	
	public void handleKey(int ch) throws Exception;


	public void handleKey(TermKey key) throws Exception;

	
	/** informs the underlying process about the view size change */
	public void setTerminalSize(int cols, int rows, double canvasWidth, double canvasHeight);
}
