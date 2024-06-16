// Copyright © 2023-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import java.io.Closeable;


/**
 * Terminal Connection Base Class.
 */
public abstract class ATermConnection<T extends ITermEmulator> implements Closeable
{
	public interface Listener
	{
		public void onConnected(ATermConnection c);
		
		public void onDisconnected(ATermConnection c, Throwable err);
	}
	
	/** connects and starts sending events to the listener */
	public abstract void connect(ITermView view);
	
	//
	
	private final T emulator;
	private volatile Listener listener;
	

	public ATermConnection(T emulator)
	{
		this.emulator = emulator;
	}
	
	
	protected T emulator()
	{
		return emulator;
	}
	
	
	public void setListener(Listener li)
	{
		listener = li;
	}
	
	
	/** informs the terminal emulator about the view size change */
	public void setTerminalSize(int cols, int rows, double canvasWidth, double canvasHeight)
	{
		emulator.setTerminalSize(cols, rows, canvasWidth, canvasHeight);
	}
	
	
	protected void fireConnected()
	{
		Listener li = listener;
		if(li != null)
		{
			li.onConnected(this);
		}
	}
	
	
	protected void fireDisconnected(Throwable err)
	{
		Listener li = listener;
		if(li != null)
		{
			li.onDisconnected(this, err);
		}
	}
	
	
	public void handleKey(int ch) throws Exception
	{
		emulator.handleKey(ch);
	}


	public void handleKey(TermKey key) throws Exception
	{
		emulator.handleKey(key);
	}
}
