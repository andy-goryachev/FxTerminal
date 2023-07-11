// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;


/**
 * Terminal Connection.
 */
public interface ITermConnection extends Closeable
{
	public OutputStream getOutputStream();
	
	public InputStream getInputStream();
	
	public void setTerminalSize(int cols, int rows, int width, int height);

	public Charset getCharset();	
}
