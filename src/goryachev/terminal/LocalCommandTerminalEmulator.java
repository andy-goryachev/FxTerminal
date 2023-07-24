// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import goryachev.common.log.Log;
import java.io.IOException;


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
	
	
	public void setView(ITermView view)
	{
		this.view = view;
	}
	
	
	public void setConnection(LocalTermConnection c)
	{
		this.conn = c;
	}


	public void setTerminalSize(int cols, int rows, int width, int height)
	{
		// no-op
	}


	public void handleKey(int ch) throws Exception
	{
		// TODO pass ch to connection
		log.info(ch);
		conn.userInput(String.valueOf((char)ch));
	}


	public void handleKey(TermKey key) throws Exception
	{
		log.info(key);
		switch(key)
		{
		case ENTER:
			// TODO pass CR
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
