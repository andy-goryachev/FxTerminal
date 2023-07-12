// Copyright © 2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import java.io.Closeable;
import java.io.Reader;
import java.io.Writer;


/**
 * Terminal Connection.
 */
public interface ITermConnection extends Closeable
{
	public Writer getOutputWriter() throws Exception;
	
	public Reader getInputReader() throws Exception;
	
	public void setTerminalSize(int cols, int rows, int width, int height);
}
