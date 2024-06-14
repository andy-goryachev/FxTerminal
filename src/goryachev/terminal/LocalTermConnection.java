// Copyright © 2023-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import goryachev.common.log.Log;
import goryachev.common.util.CPlatform;
import goryachev.common.util.SystemTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


/**
 * Local Terminal Connection: starts an OS-specific shell process.
 */
public class LocalTermConnection
	extends ATermConnection<LocalCommandTerminalEmulator>
{
	protected static final Log log = Log.get("LocalTermConnection");
	private volatile Process process;


	public LocalTermConnection()
	{
		super(new LocalCommandTerminalEmulator());
	}


	public void close() throws IOException
	{
		Process p = process;
		if(p != null)
		{
			p.destroy();
			process = null;
			
			SystemTask.schedule(750, () ->
			{
				p.destroyForcibly();	
			});
		}
	}


	public void setTerminalSize(int cols, int rows, int width, int height)
	{
		// no-op
	}


	public void connect(ITermView view)
	{
		emulator().setView(view);
		emulator().setConnection(this);

		try
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
			
			process = Runtime.getRuntime().exec(cmd, env);
			
			CompletableFuture<Process> onExit = process.onExit();
			Thread t = new Thread("process monitor")
			{
				public void run()
				{
					try
					{
						Process p = onExit.get();
						log.info("Finished %s", p);
						fireDisconnected(null);
					}
					catch(Throwable e)
					{
						log.error(e);
						fireDisconnected(e);
					}
				}
			};
			t.setDaemon(true);
			t.start();
			
			BufferedReader stdout = process.inputReader();
			Thread stdoutThread = new Thread("stdout reader")
			{
				public void run()
				{
					try
					{
						for(;;)
						{
							int c = stdout.read();
							if(c < 0)
							{
								return;
							}
							
							emulator().writeStdout(c);
						}
					}
					catch(Throwable ignore)
					{ }
				}
			};
			stdoutThread.setDaemon(true);
			stdoutThread.start();
			
			BufferedReader stderr = process.errorReader();
			Thread stderrThread = new Thread("stdout reader")
			{
				public void run()
				{
					try
					{
						for(;;)
						{
							int c = stderr.read();
							if(c < 0)
							{
								return;
							}
							
							emulator().writeStderr(c);
						}
					}
					catch(Throwable ignore)
					{ }
				}
			};
			stderrThread.setDaemon(true);
			stderrThread.start();
		}
		catch(Throwable e)
		{
			log.error(e);
			fireDisconnected(e);
		}
	}


	public void userInput(String ch) throws IOException
	{
		Process p = process;
		if(p != null)
		{
			p.outputWriter().write(ch);
			p.outputWriter().flush();
		}
	}
}
