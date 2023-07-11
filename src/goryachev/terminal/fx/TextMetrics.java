// Copyright © 2018-2023 Andy Goryachev <andy@goryachev.com>
package goryachev.terminal.fx;
import javafx.scene.text.Font;


/**
 * Text Metrics.
 */
public class TextMetrics
{
	public final Font font;
	public final double lineHeight;
	public final double baseline;
	public final double cellWidth;
	
	
	public TextMetrics(Font f, double lineHeight, double baseline, double cellWidth)
	{
		this.font = f;
		this.lineHeight = lineHeight;
		this.baseline = baseline;
		this.cellWidth = cellWidth;
	}
}
