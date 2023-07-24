// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.demo.terminal;
import goryachev.common.log.Log;
import goryachev.fx.FX;
import goryachev.fx.FxWindow;
import goryachev.terminal.LocalTermConnection;
import goryachev.terminal.fx.FxTermView;


/**
 * Terminal Demo Window.
 */
public class TermDemoWindow
	extends FxWindow
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
			LocalTermConnection conn = new LocalTermConnection();
			termView.setConnection(conn);
		});
	}
}
