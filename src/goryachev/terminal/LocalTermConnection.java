// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import goryachev.common.log.Log;
import goryachev.common.util.CPlatform;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;


/**
 * Local Terminal Connection.
 */
public class LocalTermConnection
	implements ITermConnection
{
	protected static final Log log = Log.get("LocalTermConnection");
	private Process shell;


	public LocalTermConnection()
	{
	}


	public void close() throws IOException
	{
	}


	public Writer getOutputWriter() throws Exception
	{
		return shell().outputWriter();
	}


	public Reader getInputReader() throws Exception
	{
		return shell().inputReader();
	}
	
	
	protected Process shell() throws Exception
	{
		if(shell == null)
		{
			synchronized(this)
			{
				if(shell == null)
				{
					String[] cmd;
					String[] env;
					if(CPlatform.isWindows())
					{
						cmd = new String[]
						{
							"Powershell.exe"
						};
						env = new String[]
						{
						};
					}
					else
					{
						cmd = new String[]
						{
							"/bin/bash"
						};
						env = new String[]
						{
							"TERM=xterm-256color"
						};
					}
					shell = Runtime.getRuntime().exec(cmd, env);
					
					// FIX remove
					var when = shell.onExit();
					Thread t = new Thread("waiting")
					{
						public void run()
						{
							try
							{
								Process p = when.get();
								log.info("Finished %s", p);
							}
							catch(Throwable e)
							{
								log.error(e);
							}
						}
					};
					t.setDaemon(true);
					t.start();
				}
			}
		}
		return shell;
	}


	public void setTerminalSize(int cols, int rows, int width, int height)
	{
		// TODO
	}
}
