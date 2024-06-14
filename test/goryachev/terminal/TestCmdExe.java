// Copyright © 2023-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import goryachev.common.util.CPlatform;
import goryachev.common.util.D;
import java.io.Reader;
import java.io.Writer;


/**
 * TestCmdExe.
 */
public class TestCmdExe
{
	public static void main(String[] args) throws Exception
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
				//"SHELL=/bin/bash",
				"TERM=xterm-256color"
				//"TERM_PROGRAM=Apple_Terminal"
			};
		}
		Process p = Runtime.getRuntime().exec(cmd, env);
		
		new Thread(() ->
		{
			readProcess(p.inputReader());
		}).start();
		
		Writer wr = p.outputWriter();
		
		for(;;)
		{
			int c = System.in.read();
			if(c < 0)
			{
				D.print("End of input.");
				return;
			}
			
			wr.write(c);
			wr.flush();
		}
	}
	
	
	protected static void readProcess(Reader rd)
	{
		try
		{
			for(;;)
			{
				int c = rd.read();
				if(c < 0)
				{
					D.print("EOF");
					return;
				}
				
				System.out.print((char)c);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
}
