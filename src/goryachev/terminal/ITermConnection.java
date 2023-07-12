// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import java.io.Closeable;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;


/**
 * Terminal Connection.
 */
public interface ITermConnection extends Closeable
{
	public Writer getOutputWriter() throws Exception;
	
	public InputStream getInputStream() throws Exception;
	
	public void setTerminalSize(int cols, int rows, int width, int height);

	public Charset getCharset();	
}
