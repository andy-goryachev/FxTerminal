// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import goryachev.common.log.Log;
import goryachev.common.util.CPlatform;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;


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


	public InputStream getInputStream() throws Exception
	{
		return shell().getInputStream();
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
					if(CPlatform.isWindows())
					{
						cmd = new String[]
						{
							"cmd.exe"
						};
					}
					else
					{
						cmd = new String[]
						{
							"/bin/bash"
						};
					}
					shell = Runtime.getRuntime().exec(cmd);
					
					// FIX remove
					var when = shell.onExit();
					new Thread("waiting")
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
					}.start();
				}
			}
		}
		return shell;
	}


	public void setTerminalSize(int cols, int rows, int width, int height)
	{
		// TODO
	}


	public Charset getCharset()
	{
		return Charset.defaultCharset();
	}
}
