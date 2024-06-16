// Copyright © 2023-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import goryachev.common.log.Log;


/**
 * Local Command TerminalEmulator.
 */
public class LocalCommandTerminalEmulator
	implements ITermEmulator
{
	protected static final Log log = Log.get("LocalCommandTerminalEmulator");
	private ITermView view;
	private LocalTermConnection conn;
	
	
	public LocalCommandTerminalEmulator()
	{
	}
	
	
	@Override
	public void setView(ITermView view)
	{
		this.view = view;
	}
	
	
	public void setConnection(LocalTermConnection c)
	{
		this.conn = c;
	}


	@Override
	public void setTerminalSize(int cols, int rows, double canvasWidth, double canvasHeight)
	{
		// no-op
	}


	@Override
	public void handleKey(int ch) throws Exception
	{
		conn.userInput(String.valueOf((char)ch));
	}


	@Override
	public void handleKey(TermKey key) throws Exception
	{
		switch(key)
		{
		case BACKSPACE:
			conn.userInput("\b");
			break;
		case ENTER:
			conn.userInput("\r");
			break;
		}
	}
	
	
	public void writeStdout(int c)
	{
		// TODO
		// output char in red color
		System.out.print((char)c);
	}
	
	
	public void writeStderr(int c)
	{
		// TODO
		// output char in black color
		System.err.print((char)c);
	}
}
