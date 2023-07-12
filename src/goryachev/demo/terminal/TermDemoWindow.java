// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.demo.terminal;
import goryachev.common.log.Log;
import goryachev.fx.FX;
import goryachev.fx.FxWindow;
import goryachev.terminal.GTermVT100;
import goryachev.terminal.LocalTermConnection;
import goryachev.terminal.fx.FxTermView;


/**
 * Terminal Demo Window.
 */
public class TermDemoWindow extends FxWindow
{
	protected static final Log log = Log.get("TermDemoWindow");
	protected final FxTermView termView;
	
	public TermDemoWindow()
	{
		super("TermDemoWindow");
		setTitle("Terminal Demo");
		setSize(800, 400);
		
		termView = new FxTermView();
		
		setCenter(termView);
		
		FX.later(() ->
		{
			try
			{
				LocalTermConnection conn = new LocalTermConnection();
				GTermVT100 term = new GTermVT100();
				termView.setTerm(term);
				term.connect(conn);
			}
			catch(Exception e)
			{
				log.error(e);
			}
		});
	}
}
