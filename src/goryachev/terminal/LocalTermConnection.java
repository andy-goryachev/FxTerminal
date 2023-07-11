// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import goryachev.common.util.CPlatform;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;


/**
 * Local Terminal Connection.
 */
public class LocalTermConnection
	implements ITermConnection
{
	private Process shell;


	public LocalTermConnection()
	{
	}


	public void close() throws IOException
	{
	}


	public OutputStream getOutputStream() throws Exception
	{
		return shell().getOutputStream();
	}


	public InputStream getInputStream() throws Exception
	{
		return shell().getInputStream();
	}
	
	
	protected Process shell() throws Exception
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
