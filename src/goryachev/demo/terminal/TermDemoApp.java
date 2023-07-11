// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.demo.terminal;
import goryachev.common.log.Log;
import goryachev.common.log.LogLevel;
import goryachev.common.util.CPlatform;
import goryachev.common.util.FileSettingsProvider;
import goryachev.common.util.GlobalSettings;
import java.io.File;
import javafx.application.Application;
import javafx.stage.Stage;


/**
 * Terminal Demo Application.
 */
public class TermDemoApp
	extends Application
{
	public static void main(String[] args)
	{
		Log.initConsole(LogLevel.DEBUG);
		
		Application.launch(TermDemoApp.class, args);
	}
	
	
	public void init() throws Exception
	{
		File settingsFile = new File(CPlatform.getSettingsFolder(), "TermDemo/ui.conf");
		FileSettingsProvider p = new FileSettingsProvider(settingsFile);
		GlobalSettings.setProvider(p);
		p.loadQuiet();
	}


	public void start(Stage s) throws Exception
	{
		new TermDemoWindow().open();
	}
}
