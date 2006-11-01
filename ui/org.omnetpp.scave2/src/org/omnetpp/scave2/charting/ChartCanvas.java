package org.omnetpp.scave2.charting;

import static org.omnetpp.scave2.model.ChartProperties.PROP_DISPLAY_LEGEND;
import static org.omnetpp.scave2.model.ChartProperties.PROP_GRAPH_TITLE;
import static org.omnetpp.scave2.model.ChartProperties.PROP_GRAPH_TITLE_FONT;
import static org.omnetpp.scave2.model.ChartProperties.PROP_LEGEND_ANCHORING;
import static org.omnetpp.scave2.model.ChartProperties.PROP_LEGEND_BORDER;
import static org.omnetpp.scave2.model.ChartProperties.PROP_LEGEND_FONT;
import static org.omnetpp.scave2.model.ChartProperties.PROP_LEGEND_POSITION;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.omnetpp.common.color.ColorFactory;
import org.omnetpp.common.util.Converter;
import org.omnetpp.scave2.model.ChartProperties.LegendAnchor;
import org.omnetpp.scave2.model.ChartProperties.LegendPosition;

public abstract class ChartCanvas extends ZoomableCachingCanvas {

	protected static final String DEFAULT_TITLE = "";
	protected static final Font DEFAULT_TITLE_FONT = new Font(null, "Arial", 10, SWT.NORMAL);
	protected static final boolean DEFAULT_DISPLAY_LEGEND = false;
	protected static final boolean DEFAULT_LEGEND_BORDER = false;
	protected static final LegendPosition DEFAULT_LEGEND_POSITION = LegendPosition.Above;
	protected static final LegendAnchor DEFAULT_LEGEND_ANCHOR = LegendAnchor.North;
	protected static final Font DEFAULT_LEGEND_FONT = new Font(null, "Arial", 8, SWT.NORMAL);
	
	protected Title title = new Title(DEFAULT_TITLE, DEFAULT_TITLE_FONT);
	protected Legend legend = new Legend(DEFAULT_DISPLAY_LEGEND, DEFAULT_LEGEND_BORDER, DEFAULT_LEGEND_FONT, DEFAULT_LEGEND_POSITION, DEFAULT_LEGEND_ANCHOR);
	
	private Runnable scheduledRedraw;
	
	public ChartCanvas(Composite parent, int style) {
		super(parent, style);
	}
	
	public void setProperty(String name, String value) {
		// Titles
		if (PROP_GRAPH_TITLE.equals(name))
			setTitle(value);
		else if (PROP_GRAPH_TITLE_FONT.equals(name))
			setTitleFont(Converter.stringToSwtfont(value));
		// Legend
		else if (PROP_DISPLAY_LEGEND.equals(name))
			setDisplayLegend(Converter.stringToBoolean(value));
		else if (PROP_LEGEND_BORDER.equals(name))
			setLegendBorder(Converter.stringToBoolean(value));
		else if (PROP_LEGEND_FONT.equals(name))
			setLegendFont(Converter.stringToSwtfont(value));
		else if (PROP_LEGEND_POSITION.equals(name))
			setLegendPosition(Converter.stringToEnum(value, LegendPosition.class));
		else if (PROP_LEGEND_ANCHORING.equals(name))
			setLegendAnchor(Converter.stringToEnum(value, LegendAnchor.class));
	}
	
	
	public Axis getHorizontalAxis() {
		return null;
	}
	
	public Axis getVerticalAxis() {
		return null;
	}
	
	public void setTitle(String value) {
		if (value == null)
			value = DEFAULT_TITLE;
		title.setText(value);
		scheduleRedraw();
	}

	public void setTitleFont(Font value) {
		if (value == null)
			value = DEFAULT_TITLE_FONT;
		title.setFont(value);
		scheduleRedraw();
	}
	
	public void setDisplayLegend(Boolean value) {
		if (value == null)
			value = DEFAULT_DISPLAY_LEGEND;
		legend.setVisible(value);
		scheduleRedraw();
	}
	
	public void setLegendBorder(Boolean value) {
		if (value == null)
			value = DEFAULT_LEGEND_BORDER;
		legend.setDrawBorder(value);
		scheduleRedraw();
	}
	
	public void setLegendFont(Font value) {
		if (value == null)
			value = DEFAULT_LEGEND_FONT;
		legend.setFont(value);
		scheduleRedraw();
	}
	
	public void setLegendPosition(LegendPosition value) {
		if (value == null)
			value = DEFAULT_LEGEND_POSITION;
		legend.setPosition(value);
		scheduleRedraw();
	}
	
	public void setLegendAnchor(LegendAnchor value) {
		if (value == null)
			value = DEFAULT_LEGEND_ANCHOR;
		legend.setAnchor(value);
		scheduleRedraw();
	}
	

	protected void scheduleRedraw() {
		if (scheduledRedraw == null) {
			scheduledRedraw = new Runnable() {
				public void run() {
					scheduledRedraw = null;
					clearCanvasCacheAndRedraw();
				}
			};
			getDisplay().asyncExec(scheduledRedraw);
		}
	}
	
	class Ticks implements Iterable<BigDecimal> {
		
		private BigDecimal start;
		private BigDecimal end;
		private BigDecimal delta;
		
		public Ticks(double start, double end, double delta) {
			int scale = (int)Math.ceil(Math.log10(delta));
			this.start = new BigDecimal(start).setScale(-scale, RoundingMode.FLOOR);
			this.end = new BigDecimal(end).setScale(-scale, RoundingMode.CEILING);
			BigDecimal spacing = BigDecimal.valueOf(delta);
			this.delta = new BigDecimal(1).scaleByPowerOfTen(scale);
			// use 2, 4, 6, 8, etc. if possible
			if (this.delta.divide(BigDecimal.valueOf(5)).compareTo(spacing) > 0)
				this.delta = this.delta.divide(BigDecimal.valueOf(5));
			// use 5, 10, 15, 20, etc. if possible
			else if (this.delta.divide(BigDecimal.valueOf(2)).compareTo(spacing) > 0)
				this.delta = this.delta.divide(BigDecimal.valueOf(2));
		}

		public Iterator<BigDecimal> iterator() {
			class TickIterator implements Iterator<BigDecimal> {
				BigDecimal current;
				
				public TickIterator() {
					current = start;
				}
				
				public boolean hasNext() {
					return current.compareTo(end) < 0;
				}

				public BigDecimal next() {
					if (!hasNext())
						throw new NoSuchElementException();
					BigDecimal result = current;
					current = current.add(delta);
					return result;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
			
			return new TickIterator();
		}
	}
	
	protected abstract class Axis {
		public abstract Ticks getTicks();
	}
	
	protected static final boolean DEFAULT_SHOW_GRID = false;
	
	protected class Grid {
		private Rectangle rect;
		protected boolean visible = DEFAULT_SHOW_GRID;
		
		public Rectangle layout(GC gc, Rectangle rect) {
			this.rect = rect;
			return rect;
		}
		
		public void draw(GC gc) {
			if (visible) {
				Graphics graphics = new SWTGraphics(gc);
				graphics.pushState();
				
				graphics.setForegroundColor(ColorFactory.asColor("red"));
				graphics.drawRectangle(rect);
				
				Axis axis;
				graphics.setForegroundColor(ColorFactory.asColor("black"));
				graphics.setLineStyle(SWT.LINE_DOT);
				if ((axis = getHorizontalAxis()) != null) {
					for (BigDecimal tick : axis.getTicks()) {
						int x = toCanvasX(tick.doubleValue());
						graphics.drawLine(x, rect.y, x, rect.y + rect.height);
					}
				}
				if ((axis = getVerticalAxis()) != null) {
					for (BigDecimal tick : axis.getTicks()) {
						int y = toCanvasY(tick.doubleValue());
						graphics.drawLine(rect.x, y, rect.x + rect.width, y);
					}
				}
				
				graphics.popState();
				graphics.dispose();
			}
		}
	}
	
	protected static class PlotArea {
		public double minX;
		public double maxX;
		public double minY;
		public double maxY;

		public PlotArea(double minX, double maxX, double minY, double maxY) {
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;
		}
		
	}
}
