// Copyright © 2018-2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal;
import goryachev.common.log.Log;
import goryachev.common.util.ASCII;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.Hex;
import goryachev.common.util.SB;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;


/**
 * GTermVT100.
 * 
 * Escape sequence info:
 * https://en.wikipedia.org/wiki/ANSI_escape_code
 * 
 * https://stackoverflow.com/questions/4842424/list-of-ansi-color-escape-sequences
 * 
 * The following links are incomplete:
 * http://ascii-table.com/ansi-escape-sequences.php
 * http://ascii-table.com/ansi-escape-sequences-vt-100.php
 * http://ascii-table.com/documents/vt100/chapter3.php
 * 
 * http://invisible-island.net/xterm/ctlseqs/ctlseqs.html
 */
public class GTermVT100
{
	protected static final Log log = Log.get("GTermVT100");
	protected static final Log logRx = Log.get("GTermVT100.rx");
	
	protected ITermView view;
	protected final CList<Integer> pushback = new CList();
	protected final SB escapeSequence = new SB();
	protected final CList<String> args = new CList();
	protected final SB arg = new SB();
	protected ITermConnection connection;
	protected boolean running;
	protected Reader rd;
	protected Writer wr;
	private char highSurrogate;
	private Thread thread;
	/** visible window x coordinate 0 ... colCount - 1 */
	private int curx;
	/** visible window y coordinate 0 ... rowCount - 1 */
	private int cury;
	private int colCount;
	private int rowCount;
	private int scrollTop;
	private int scrollBottom;
	private int tabSize = 8;
	
	
	public GTermVT100()
	{
	}
	
	
	public void connect(ITermConnection conn) throws Exception
	{
		Objects.nonNull(conn);
		this.connection = conn;
		
		rd = connection.getInputReader();
		
		wr = connection.getOutputWriter();
		
		thread = new Thread(() ->
		{
			try
			{
				readProcess();
			}
			catch(Throwable e)
			{
				log.error(e);
			}
		});
		thread.setName("terminal");
		thread.setDaemon(true);
		thread.start();
	}
	
	
	public void setView(ITermView v)
	{
		this.view = v;
		reset();
	}
	
	
	public void readProcess()
	{
		try
		{
			running = true;
			while(running)
			{
				int c = readCodePoint();
				logRx.trace("%04x (%c)", c, (char)c);
									
				switch(c)
				{
				case ASCII.BEL:
					view.bell();
					break;
				case ASCII.BS:
					backspace();
					break;
				case ASCII.ENQ:
					// Return Terminal Status (ENQ  is Ctrl-E).  Default response is
			        // an empty string, but may be overridden by a resource answer-backString (xterm).
					enq();
					break;
				case ASCII.FF:
					// Form Feed or New Page (NP ).  (FF  is Ctrl-L).  FF  is treated the same as LF .
					linefeed();
					break;
				case ASCII.HT:
					tab();
					break;
				case ASCII.LF:
					linefeed();
					break;
				case ASCII.CR:
					carriageReturn();
					break;
				case ASCII.SI:
					// TODO
					// Switch to Standard Character Set (Ctrl-O is Shift In or LS0).
			        // This invokes the G0 character set (the default) as GL.
			        // VT200 and up implement LS0.
					log.error("SI");
					break;
				case ASCII.SO:
					// TODO
					// Switch to Alternate Character Set (Ctrl-N is Shift Out or
				    // LS1).  This invokes the G1 character set as GL.
				    // VT200 and up implement LS1.
					log.error("SO");
					break;
				case ASCII.ESC:
					processEscapeSequence();
					break;
				case ASCII.VT:
					// Vertical Tab (VT  is Ctrl-K).  This is treated the same as LF.
					linefeed();
					break;
				default:
					if(c < 0)
					{
						running = false;
					}
					else if(c < ASCII.SPACE)
					{
						log.info("unknown input 0x%02X", c);
					}
					else
					{
						hideCursor();
						
						if((curx >= colCount) || (cury == rowCount))
						{
							carriageReturn();
							linefeed();
						}
						
						int dx = view.draw(curx, cury, c);
						if(dx < 0)
						{
							carriageReturn();
							linefeed();
							
							dx = view.draw(curx, cury, c);
						}
						curx += dx;
						
						if(curx >= colCount)
						{
							carriageReturn();
							linefeed();
						}
						
						showCursor();
					}
				}
			}
		}
		catch(Throwable e)
		{
			log.error(e);
		}
		finally
		{
			shutdown();
		}
	}
	
	
	public void shutdown()
	{
		CKit.close(rd);
		CKit.close(wr);

		if(connection != null)
		{
			try
			{
				connection.close();
			}
			catch(Throwable e)
			{
				log.error(e);
			}
			finally
			{
				connection = null;
			}
		}
	}
	
	
	public void handleKey(TermKey k) throws Exception
	{
		String s = getKeySequence(k);
		if(s == null)
		{
			throw new Error("?" + k);
		}
		out(s);
	}
	
	
	protected void out(String s) throws Exception
	{
		wr.write(s);
		wr.flush();
	}
	
	
	protected String getKeySequence(TermKey k)
	{
		// ftp://ftp.invisible-island.net/shuford/terminal/dec_keyboards_news.txt
		switch(k)
		{
		case DOWN:
			return "\u001bOB";
		case ENTER:
			return "\r";
		case F1:
			return "\u001bOP";
		case F2:
			return "\u001bOQ";
		case F3:
			return "\u001bOR";
		case F4:
			return "\u001bOS";
		case F5:
			return "\u001bOt";
		case F6:
			return "\u001bOu";
		case F7:
			return "\u001bOv";
		case F8:
			return "\u001bOI";
		case F9:
			return "\u001bOw";
		case F10:
			return "\u001bOx";
		case LEFT:
			return "\u001bOD";
		case RIGHT:
			return "\u001bOC";
		case TAB:
			return "\t";
		case UP:
			return "\u001bOA";			
		}
		return null;
	}
	
	
	public void handleKey(int c) throws Exception
	{
		String s = TermTools.codePointToString(c);
		out(s);
	}
	
	
	protected void reset()
	{
		colCount = view.getColumnCount();
		rowCount = view.getRowCount();
		scrollTop = 0;
		scrollBottom = rowCount;
		
		// TODO clear view?
	}
	
	
	protected void hideCursor()
	{
		view.hideCursor();
	}
	
	
	protected void showCursor()
	{
		log.debug("curx=%d, cury=%d", curx, cury);
		
		view.showCursor(curx, cury);
	}
	
	
	protected void backspace()
	{
		log.error("BS"); // TODO
	}
	
	
	protected void enq()
	{
		log.error("ENQ"); // TODO
	}
	
	
	protected void carriageReturn()
	{
		hideCursor();
		curx = 0;
		showCursor();
	}
	
	
	protected void linefeed()
	{
		hideCursor();
		if(cury < rowCount - 1)
		{
			cury++;
		}
		else
		{
			view.scroll(scrollTop, scrollBottom, true);
		}
		showCursor();
	}
	
	
	protected void tab()
	{
		hideCursor();
		
		curx = ((curx / tabSize) + 1) * tabSize;
		if(curx >= colCount)
		{
			curx = 0;
			cury++;
		}
		
		showCursor();
	}
	
	
	protected int readCodePoint() throws Exception
	{
		if(pushback.size() > 0)
		{
			return pushback.remove(0);
		}
		
		int c = rd.read();

		// FIX
		//D.print("[0x" + Hex.toHexString(c, 4) + "] ", c < 0x20 ? Hex.toHexString(c, 2) : (char)c);

		if(highSurrogate > 0)
		{
			if(c < 0)
			{
				c = highSurrogate;
				highSurrogate = 0;
				return c;
			}
			else
			{
				return -1;
			}
		}
		else
		{
			if(Character.isHighSurrogate((char)c))
			{
				highSurrogate = (char)c;
				
				c = rd.read();
				
				if(Character.isLowSurrogate((char)c))
				{
					c = Character.toCodePoint(highSurrogate, (char)c);
					highSurrogate = 0;
					return c;
				}
				else
				{
					pushback.add(0, c);
					c = highSurrogate;
					highSurrogate = 0;
					return c;
				}
			}
			else
			{
				return c;
			}
		}
	}
	

	public void requestSize(int cols, int rows, int width, int height)
	{
		reset();
		
		if(connection != null)
		{
			connection.setTerminalSize(cols, rows, width, height);
		}
		
		// TODO
//		view.refreshAll();
	}
	
	
	protected int getIntArg(int ix)
	{
		if(ix < args.size())
		{
			String v = args.get(ix);
			if(CKit.isNotBlank(v))
			{
				try
				{
					return Integer.parseInt(v);
				}
				catch(Exception e)
				{
					log.error(e);
				}
			}
		}
		return 0;
	}
	
	
	protected int argCount()
	{
		return args.size();
	}
	
	
	protected void clearScreen(int fromRow, int toRow)
	{
		for(int i=fromRow; i<toRow; i++)
		{
			view.clearLine(0, i);
		}
	}
	
	
	protected void cursorHome()
	{
		curx = 0;
		cury = 0;
		showCursor();
	}
	
	
	/** Esc[{val};{val}m */
	protected boolean setGraphicsMode()
	{
//		D.print("setGraphicsMode", args); // FIX
		
		for(int i=0; i<argCount(); i++)
		{
			int arg = getIntArg(i);
			switch(arg)
			{
			case 0:
				// all attributes off
				view.clearAttributes();
				break;
			case 1:
				// bold on
				view.setBold();
				break;
			case 4:
				// underscore
				view.setUnderscore();
				break;
			case 5:
				// blink on
				view.setBlink();
				break;
			case 7:
				// reverse video on
				view.setReversed();
				break;
			case 8:
				// concealed on
				view.setConcealed();
				break;
			case 30:
				// black fg
				view.setForeground(TermColor.BLACK);
				break;
			case 31:
				// red fg
				view.setForeground(TermColor.RED);
				break;
			case 32:
				// green fg
				view.setForeground(TermColor.GREEN);
				break;
			case 33:
				// yellow fg
				view.setForeground(TermColor.YELLOW);
				break;
			case 34:
				// blue fg
				view.setForeground(TermColor.BLUE);
				break;
			case 35:
				// magenta fg
				view.setForeground(TermColor.MAGENTA);
				break;
			case 36:
				// cyan fg
				view.setForeground(TermColor.CYAN);
				break;
			case 37:
				// white fg
				view.setForeground(TermColor.WHITE);
				break;
			case 40:
				// black bg
				break;
			case 41:
				// read bg
				break;
			case 42:
				// green bg
				break;
			case 43:
				// yellow bg
				break;
			case 44:
				// blue bg
				break;
			case 45:
				// magenta bg
				break;
			case 46:
				// cyan bg
				break;
			case 47:
				// white bg
				break;
			default:
				return false;
			}
		}
		
		return true;
	}
	
	
	protected void processEscapeSequence() throws Exception
	{
		escapeSequence.clear();
		args.clear();
		arg.clear();
		
		boolean inQuotes = false;
		
		int c = readCodePoint();
		escapeSequence.appendCodePoint(c);
		
		switch(c)
		{
		case 'H':
			// EscH - Move cursor to upper left corner (cursorhome)
			cursorHome();
			return;
		case 'J':
			// EscJ - Erase to end of screen (cleareos)
			clearScreen(cury, rowCount);
			return;
		case 'K':
			// EscK - Erase to end of current line (cleareol)
			// TODO
			log.error("cleareol");
			return;
		case 'N':
			// SS2 – Single Shift Two, select G2 character set
			// TODO
			log.error("SS2");
			return;
		case 'O':
			// SS3 – Single Shift Three, select G3 character set
			// TODO
			log.error("SS3");
			return;
		case 'P':
			// DCS – Device Control String
			// Terminated by ST. Xterm's uses of this sequence include defining User-Defined Keys, and requesting or setting Termcap/Terminfo data.
			// TODO
			log.error("DCS");
			return;
		case '\\':
			// ST – String Terminator. Terminates strings in other controls.
			// TODO
			log.error("ST");
			return;
		case ']':
			// OSC – Operating System Command. Starts a control string for the operating system to use, terminated by ST
			// TODO
			log.error("OSC");
			return;
		case 'X':
			// SOS – Start of String.  Takes an argument of a string of text, terminated by ST.
			// TODO
			log.error("SOS");
			return;
		case '^':
			// PM – Privacy Message.  Takes an argument of a string of text, terminated by ST.
			// TODO
			log.error("PM");
			return;
		case '_':
			// APC – Application Program Command.  Takes an argument of a string of text, terminated by ST.
			// TODO
			log.error("APC");
			return;
		case 'c':
			// RIS – Reset to Initial State
			reset();
			return;
		case '=':
			// ESC =     Application Keypad (DECKPAM).
			log.error("DECKPAM");
			return;
		case '>':
			// ESC >     Normal Keypad (DECKPNM), VT100.
			log.error("DECKPNM");
			return;
		case '[':
			// CSI - Control Sequence Introducer
			for(;;)
			{
				c = readCodePoint();
				escapeSequence.appendCodePoint(c);
				
				if(TermTools.isFinalSymbol(c))
				{
					// end of sequence
					args.add(arg.toString());
					arg.clear();
					
					boolean handled = handleControlSequence(c);
					if(!handled)
					{
						log.error("unknown control sequence: %s (%s)", escapeSequence, Hex.toHexString(escapeSequence.getBytes(CKit.CHARSET_ASCII)));						
					}
					return;
				}
				else
				{
					// TODO check for buffer size, prevent overflow.  log and display accumulated symbols?
					
					switch(c)
					{
					case '"':
						if(inQuotes)
						{
							// end of quoted argument
							arg.appendCodePoint(c);
							inQuotes = false;
						}
						else
						{
							inQuotes = true;
							arg.appendCodePoint(c);
						}
						break;
					case ';':
						if(!inQuotes)
						{
							// argument separator
							args.add(arg.toString());
							arg.clear();
						}
						break;
					default:
						arg.appendCodePoint(c);
					}
				}
			}
		default:
			log.error("unhandled escape sequence %c %04X", (char)c, c);
			return;
		}
	}

	
	/** process Esc-[ (CSI) control sequence */
	protected boolean handleControlSequence(int ch)
	{
		switch(ch)
		{
		case 'H':
			// Esc[H - Move cursor to upper left corner - cursorhome 
			cursorHome();
			break;
		case 'J':
			int n = getIntArg(0);
			switch(n)
			{
			case 0:
				// EscJ, Esc[0J - Clear screen from cursor down - ED0
				clearScreen(cury, rowCount);
				break;
			case 1:
				// Esc[1J - Clear screen from cursor up - ED1
				clearScreen(0, cury - 1);
				break;
			case 2:
				// Esc[2J - Clear entire screen - ED2
				clearScreen(0, rowCount);
				break;
			}
			break;
		case 'K':
			// Erase Line: Clears all characters from the cursor position to the end of the line
			// (including the character at the cursor position).
			view.clearLine(curx, cury);
			break;
		case 'm':
			return setGraphicsMode();
		default:
			return false;
		}
		
		return true;
	}
}
