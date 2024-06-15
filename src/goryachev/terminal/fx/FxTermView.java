// Copyright © 2018-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal.fx;
import goryachev.common.log.Log;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.fx.CPane;
import goryachev.fx.CssStyle;
import goryachev.fx.FX;
import goryachev.fx.FxObject;
import goryachev.terminal.ITermView;
import goryachev.terminal.TermColor;
import goryachev.terminal.ATermConnection;
import goryachev.terminal.TermKey;
import goryachev.terminal.TermTools;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;


/**
 * FX Terminal View.
 * 
 * TODO split into FX view and TermScreenBuffer
 */
public class FxTermView
	extends CPane
	implements ITermView
{
	public enum Status
	{
		CONNECTED,
		CONNECTING,
		DISCONNECTED,
		ERROR
	}
	
	protected static final Log log = Log.get("FxTermView");

	public static final CssStyle PANE = new CssStyle("FxTermView_PANE");

	public final TermScreenBuffer buffer;
	protected final Object lock = new Object();
	protected final TermPalette palette = new TermPalette();
	protected final Text proto = new Text();
	protected final CellInfo cellInfo = new CellInfo();
	protected final ScrollBar scrollBar;
	protected final AtomicBoolean repaintRequested = new AtomicBoolean();
	private Font font;
	private TextMetrics metrics;
	private Canvas canvas;
	private int topLine;
	protected int rowCount;
	protected int colCount;
	private int curx = -1;
	private int cury = -1;
	private GraphicsContext gx;
	private Timeline cursorAnimation;
	private boolean cursorEnabled = true;
	private boolean cursorOn = true;
	private boolean blink;
	private boolean bold;
	private boolean concealed;
	private boolean italic;
	private boolean reversed;
	private boolean underscore;
	private final FxObject<ATermConnection> connection = new FxObject<>();
	private final FxObject<Status> status = new FxObject<>();
	private ATermConnection.Listener listener;
			
	
	public FxTermView()
	{
		FX.style(this, PANE);
		
		setMinWidth(0);
		setMinHeight(0);
		
		buffer = new TermScreenBuffer();
		
		scrollBar = new ScrollBar();
		scrollBar.setOrientation(Orientation.VERTICAL);
		setRight(scrollBar);
		
		cursorAnimation = createCursorAnimation();
		
		setFont(new Font("Courier New", 11.0));

		setFocusTraversable(true);
		addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
		addEventHandler(KeyEvent.KEY_TYPED, this::handleKeyTyped);
		addEventHandler(MouseEvent.MOUSE_PRESSED, (ev) -> requestFocus());
		
		connection.addListener((s,p,c) -> handleConnectionChange(p,c));
		FX.onChange(this::handleSizeChange, true, widthProperty(), heightProperty());
	}
	
	
	public void shutdown()
	{
		cursorAnimation.stop();
	}
	

	public void setFont(Font f)
	{
		// TODO property
		if(f == null)
		{
			throw new NullPointerException("font");
		}
		
		this.font = f;
		metrics = null;
	}
	
	
	protected void handleSizeChange()
	{
		TextMetrics tm = textMetrics();
		Insets m = getInsets();
		
		double w = getWidth() - m.getLeft() - m.getRight() - scrollBar.getWidth();
		double h = getHeight() - m.getTop() - m.getBottom();
		
		if((w < 1) || (h < 1))
		{
			colCount = 80;
			rowCount = 25;
		}
		else
		{
			colCount = CKit.floor(w / tm.cellWidth);
			rowCount = CKit.floor(h / tm.lineHeight);
		}
		
		Canvas cv = new Canvas(w, h);
		if(canvas != null)
		{
			getChildren().remove(canvas);
		}
		canvas = cv;
		setCenter(cv);

		gx = canvas.getGraphicsContext2D();
		gx.setFill(getBackgroundColor());
		gx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		int dy = buffer.resize(colCount, rowCount);
		log.debug("dy=%d cury=%d", dy, cury); // FIX
		cury -= dy;
		
		ATermConnection conn = getConnection();
		if(conn != null)
		{
			conn.setTerminalSize(colCount, rowCount, (int)canvas.getWidth(), (int)canvas.getHeight());
		}
		
		repaint();
	}
	
	
	protected void error(Throwable e)
	{
		// TODO visual feedback?
		log.error(e);
	}
	
	
	protected void handleKeyPressed(KeyEvent ev)
	{
		ATermConnection conn = getConnection();
		if(conn != null)
		{
			KeyCode c = ev.getCode();
			TermKey k = translateKeyCode(c);
			if(k != null)
			{
				try
				{
					conn.handleKey(k);
					ev.consume();
				}
				catch(Exception e)
				{
					error(e);
				}
			}
			
			resetCursorPhase();
		}
	}
	
	
	// TODO or may be on cursor set
	protected void resetCursorPhase()
	{
		synchronized(lock)
		{
			cursorOn = true;
		}
		
		cursorAnimation.stop();
		cursorAnimation.play();
	}


	protected TermKey translateKeyCode(KeyCode c)
	{
		switch(c)
		{
		case BACK_SPACE:
			return TermKey.BACKSPACE;
		case DOWN:
			return TermKey.DOWN;
		case ENTER:
			return TermKey.ENTER;
		case F1:
			return TermKey.F1;
		case F2:
			return TermKey.F2;
		case F3:
			return TermKey.F3;
		case F4:
			return TermKey.F4;
		case F5:
			return TermKey.F5;
		case F6:
			return TermKey.F6;
		case F7:
			return TermKey.F7;
		case F8:
			return TermKey.F8;
		case F9:
			return TermKey.F9;
		case F10:
			return TermKey.F10;
		case LEFT:
			return TermKey.LEFT;
		case RIGHT:
			return TermKey.RIGHT;
		case TAB:
			return TermKey.TAB;
		case UP:
			return TermKey.UP;
		default:
			return null;
		}
	}


	protected void handleKeyTyped(KeyEvent ev)
	{
		ATermConnection conn = getConnection();
		if(conn != null)
		{
			try
			{
				String s = ev.getCharacter();
				if(s.length() == 1)
				{
					int c = s.codePointAt(0);
					if(c >= 0x20)
					{
						conn.handleKey(c);
						ev.consume();
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace(); // FIX
			}
	
			resetCursorPhase();
		}
	}
	

	@Override
	public void bell()
	{
		// TODO
		D.print("<BELL>");
	}
	
	
	@Override
	public void scroll(int top, int bottom, boolean up)
	{
		log.debug("scroll top=%d, bottom=%d, up=%s", top, bottom, up);
		
		synchronized(lock)
		{
			buffer.scroll(top, bottom, up);
		}
		repaint();
	}
	
	
	@Override
	public int getRowCount()
	{
		return rowCount;
	}
	
	
	@Override
	public int getColumnCount()
	{
		return colCount;
	}
	
	
	public Color getTermBackground()
	{
		// FIX
		return Color.WHITE;
	}
	
	
	protected GraphicsContext getGraphicsContext()
	{
		return gx;
	}
	
	
//	protected TextMetrics textMetricsLargest()
//	{
//		if(metrics == null)
//		{
//			proto.setText(" ");
//			proto.setFont(font);
//			Bounds b = proto.getBoundsInLocal();
//			
//			double w = 0.0;
//			for(char c=0x20; c<0x80; c++)
//			{
//				proto.setText(new String(new char[] { c }));
//				b = proto.getBoundsInLocal();
//				
//				if(w < b.getWidth())
//				{
//					w = b.getWidth();
//				}
//			}
//			
//			w = Math.ceil(w);
//			
//			metrics = new TextMetrics(font, b.getHeight(), b.getMinY(), w);
//		}
//		return metrics;
//	}
	
	
//	protected TextMetrics textMetricsAverage()
//	{
//		if(metrics == null)
//		{
//			proto.setText(" ");
//			proto.setFont(font);
//			Bounds b = proto.getBoundsInLocal();
//			
//			int ct = 0;
//			double w = 0.0;
//			for(char c=0x20; c<0x80; c++)
//			{
//				proto.setText(new String(new char[] { c }));
//				b = proto.getBoundsInLocal();
//				
//				w += b.getWidth();
//				ct++;
//			}
//			
//			w = Math.ceil(w / ct);
//			
//			metrics = new TextMetrics(font, b.getHeight(), b.getMinY(), w);
//		}
//		return metrics;
//	}
	
	
	protected TextMetrics textMetrics()
	{
		if(metrics == null)
		{
			proto.setText("8");
			proto.setFont(font);
			Bounds b = proto.getBoundsInLocal();
			double w = Math.ceil(b.getWidth());
			double h = Math.ceil(b.getHeight());
			
			metrics = new TextMetrics(font, h, b.getMinY(), w);
		}
		return metrics;
	}
	
	
	@Override
	public int draw(int x, int y, int codePoint)
	{
		synchronized(lock)
		{
			// TODO unicode processing here
			// TODO combining characters, double width, etc.
			// for now, one code point per cell
			if(x >= colCount)
			{
				return -1;
			}
			
			String text = TermTools.codePointToString(codePoint);
			Color bg = getBackgroundColor();
			Color fg = getForegroundColor();

			int flags = 0;
			if(blink)
			{
				flags |= Flag.BLINK.bit(); // TODO blink must be handled in software!  cursor animation, opposite phase
			}
			if(bold)
			{
				flags |= Flag.BOLD.bit();
			}
			if(reversed)
			{
				flags |= Flag.REVERSE.bit();
			}
			if(italic)
			{
				flags |= Flag.BOLD.bit();
			}
			if(underscore)
			{
				flags |= Flag.UNDERSCORE.bit();
			}
			
			ScreenLine t = buffer.getScreenLine(y);
			t.setCell(x, text, bg, fg, flags);
		}

		repaint();
		return 1;
	}


	@Override
	public void clearLine(int x, int y)
	{
		synchronized(lock)
		{
			buffer.clearLineFrom(x, y);
		}
		repaint();
	}
	
	
	protected Timeline createCursorAnimation()
	{
		Timeline t = new Timeline(new KeyFrame(Duration.millis(500), (ev) -> blinkCursor()));
		t.setCycleCount(Timeline.INDEFINITE);
		t.play();
		return t;
	}
	
	
	protected void blinkCursor()
	{
		synchronized(lock)
		{
			cursorOn = !cursorOn;
			buffer.updateCursor(curx, cury, cursorOn && cursorEnabled);
		}
		
		repaint();
	}
	
	
	@Override
	public void hideCursor()
	{
		synchronized(lock)
		{
			cursorEnabled = false;
			buffer.updateCursor(curx, cury, cursorOn && cursorEnabled);
		}
		repaint();
	}
	
	
	@Override
	public void showCursor(int x, int y)
	{
		synchronized(lock)
		{			
			if((curx != x) || (cury != y) || (!cursorEnabled))
			{
				buffer.updateCursor(curx, cury, false);
				
				curx = x;
				cury = y;
				cursorEnabled = true;
				
				buffer.updateCursor(curx, cury, cursorOn);
			}
			else
			{
				return;
			}
		}
		
		repaint();
	}
	
	
	@Override
	public void setForeground(TermColor c)
	{
		synchronized(lock)
		{
			palette.setForeground(c);
		}
	}
	
	
	@Override
	public void setBackground(TermColor c)
	{
		synchronized(lock)
		{
			palette.setBackground(c);
		}
	}
	
	
	protected Color getBackgroundColor()
	{
		return palette.getBackground();
	}
	
	
	protected Color getForegroundColor()
	{
		return palette.getForeground();
	}
	
	
	protected Color getCursorBackground()
	{
		return Color.BLACK;
	}
	
	
	protected Color getCursorForeground()
	{
		return Color.WHITE;
	}
	
	
	public void dumpBufferAtCursor()
	{
//		ScreenLine t = textLine(cury);
//		t.dump();
	}
	
	
	@Override
	public void clearAttributes()
	{
		synchronized(lock)
		{
			blink = false;
			bold = false;
			concealed = false;
			reversed = false;
			underscore = false;
			palette.reset();
			
			buffer.forceUpdateAll();
		}

		repaint();
	}
	
	
	@Override
	public void setBlink()
	{
		synchronized(lock)
		{
			blink = true;
		}
	}
	
	
	@Override
	public void setBold()
	{
		synchronized(lock)
		{
			bold = true;
		}
	}
	
	
	@Override
	public void setConcealed()
	{
		synchronized(lock)
		{
			concealed = true;
		}
	}
	
	
	public boolean isConcealed()
	{
		return concealed;
	}
	
	
	public void setItalic()
	{
		synchronized(lock)
		{
			italic = true;
		}
	}
	
	
	@Override
	public void setReversed()
	{
		synchronized(lock)
		{
			reversed = true;
		}
	}
	
	
	@Override
	public void setUnderscore()
	{
		synchronized(lock)
		{
			underscore = true;
		}
	}
	
	
	protected void repaint()
	{
		if(repaintRequested.compareAndSet(false, true))
		{
			repaintRequested.set(true);
			
			FX.later(this::redraw);
			Thread.yield();
		}
	}
	
	
	protected void redraw()
	{
		if(repaintRequested.getAndSet(false) == false)
		{
			log.warn("repaint not requested");
			return;
		}
	
		// FIX remove
		int count = 0;
		long start = System.nanoTime();

		synchronized(lock)
		{
			TextMetrics tm = textMetrics();
			Insets m = getInsets();
			double cw = tm.cellWidth;
			double lineHeight = tm.lineHeight;
			
			for(int y=0; y<rowCount; y++)
			{
				ScreenLine t = buffer.getScreenLine(y);
				if(t.isModified())
				{
					double posy = m.getTop() + y * tm.lineHeight;
					
//					if(t.isForceUpdate())
//					{
//						// attempt to limit the canvas queue
//						// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8092801
//						// https://github.com/kasemir/org.csstudio.display.builder/issues/174
//						// https://stackoverflow.com/questions/18097404/how-can-i-free-canvas-memory
//						// https://bugs.openjdk.java.net/browse/JDK-8103438
//						gx.clearRect(m.getLeft(), posy, cw * colCount, lineHeight);
//					}

					for(int x=0; x<colCount; x++)
					{
						// FIX next updated cell?
						CellInfo inf = t.getUpdatedCell(cellInfo, x);
						if(inf != null)
						{
							// paint modified cell
							
							double posx = m.getLeft() + x * tm.cellWidth;
							boolean isCursor = cursorEnabled && cursorOn && (curx == x) && (cury == y);
							
//							if(!t.isForceUpdate())
//							{
//								// attempt to limit the canvas queue
//								// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8092801
//								// https://github.com/kasemir/org.csstudio.display.builder/issues/174
//								// https://stackoverflow.com/questions/18097404/how-can-i-free-canvas-memory
//								// https://bugs.openjdk.java.net/browse/JDK-8103438
//								gx.clearRect(posx, posy, cw, lineHeight);
//							}
							Color bg;
							if(isCursor)
							{
								bg = getCursorBackground();
							}
							else
							{
								bg = inf.bg;
								if(bg == null)
								{
									bg = getBackgroundColor();
								}
							}
							
							gx.setFill(bg);
							gx.fillRect(posx, posy, cw, lineHeight);
							
							String text = inf.text;
							if(text != null)
							{
								Color fg;
								if(isCursor)
								{
									fg = getCursorForeground();
								}
								else
								{
									fg = inf.fg;
									if(fg == null)
									{
										fg = getForegroundColor();
									}
								}
								
								gx.setFill(fg);
								gx.setFont(tm.font);
								// TODO cell width from flags
								gx.fillText(text, posx, posy - tm.baseline, cw);
							}
							
							count++;
						}
					}
					
					t.clearModified();
				}
			}
		}
		
		if(count > 1)
		{
			log.trace("redraw cells=%d in %d ms", count, (System.nanoTime() - start)/1_000_000L);
		}
	}
	
	
	public final void setConnection(ATermConnection c)
	{
		connection.set(c);
	}
	
	
	public final ATermConnection getConnection()
	{
		return connection.get();
	}
	
	
	public final ObjectProperty<ATermConnection> connectionProperty()
	{
		return connection;
	}
	
	
	protected void handleConnectionChange(ATermConnection old, ATermConnection conn)
	{
		if(old != null)
		{
			old.setListener(null);
			try
			{
				old.close();
			}
			catch(Throwable e)
			{
				log.error(e);
			}
		}

		if(conn == null)
		{
			setStatus(Status.DISCONNECTED);
		}
		else
		{
			setStatus(Status.CONNECTING);
			
			if(listener == null)
			{
				listener = new ATermConnection.Listener()
				{
					@Override
					public void onConnected(ATermConnection c)
					{
						FX.later(() -> handleConnected(c));
					}
	
	
					@Override
					public void onDisconnected(ATermConnection c, Throwable err)
					{
						FX.later(() -> handleDisconnected(c, err));
					}
				};
			}
			
			conn.setListener(listener);
			conn.connect(this);
		}
	}
	
	
	protected void handleConnected(ATermConnection c)
	{
		if(c == getConnection())
		{
			setStatus(Status.CONNECTED);
		}
	}
	
	
	protected void handleDisconnected(ATermConnection c, Throwable err)
	{
		if(c == getConnection())
		{
			if(err == null)
			{
				setStatus(Status.DISCONNECTED);
			}
			else
			{
				setStatus(Status.ERROR);
			}
		}
	}
	
	
	protected void setStatus(Status s)
	{
		status.set(s);
	}
	
	
	public final ReadOnlyProperty<Status> statusProperty()
	{
		return status.getReadOnlyProperty();
	}
}
