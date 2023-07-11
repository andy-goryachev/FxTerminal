// Copyright © 2018-2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import goryachev.common.util.SB;
import java.io.InputStream;


/**
 * Terminal Tools.
 */
public class TermTools
{
	public static String codePointToString(int c)
	{
		return new String(Character.toChars(c));
	}
	
	
	public static boolean contains(String pattern, int c)
	{
		return pattern.indexOf(c) >= 0;
	}
	
	
	public static boolean isFinalSymbol(int c)
	{
		return contains("@ABCDEFGHIJKLMNOPQRSTUVXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~", c);
	}
	
	
	public static String readLine(InputStream in)
	{
		SB sb = new SB();
		try
		{
			for(;;)
			{
				int c = in.read();
				if(c < 0)
				{
					break;
				}
				else if(c == '\r')
				{
					continue;
				}
				else if(c == '\n')
				{
					break;
				}
				else
				{
					sb.append((char)c);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}
}
