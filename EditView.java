import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.Rectangle2D;
import javax.vecmath.Point2d;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.AbstractUndoableEdit;

// the editable view of the terrain and landing pad
public class EditView extends JPanel implements Observer {
	private GameModel model;
	private JPanel world;
	private Rectangle2D.Double landingPad;
	private boolean notpress;
	private double oldX;
	private double oldY;
	private ArrayList<Point2d> peaks = new ArrayList<Point2d>();
	private boolean peaksChanged;
	int[] xpoints, ypoints;
	private boolean notpressPeaks;
	private int selected;
	private double oldPeakY;
	private boolean dragging;
	private boolean draggingp;

    public EditView(GameModel _model) {
    	model = _model;
    	model.addObserver(this);
    	notpress = false;
    	notpressPeaks =false;
    	peaksChanged = false;
    	dragging = false;
    	draggingp = false;
        // want the background to be black
        this.setBackground(Color.BLACK);
        this.setLayout(new  BorderLayout());
        world =new JPanel() {
        	@Override
        	public void paintComponent(Graphics g) {
    			super.paintComponent(g);
    			Graphics2D g2 = (Graphics2D)g;
   				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  // antialiasing look nicer
                            RenderingHints.VALUE_ANTIALIAS_ON);
   				if (peaksChanged) {
        			cachePointsArray();
       			}
   				g2.setColor(Color.darkGray);
   				g2.fillPolygon(xpoints, ypoints, peaks.size() + 2);
   				for(int i = 0; i < peaks.size(); i++) {
   					 g2.setColor(Color.GRAY);
   					 g2.drawOval(xpoints[i+1] - 15, ypoints[i+1] - 15, 30, 30);
   				}
   				if(draggingp) {
   					g2.setColor(Color.WHITE);
   					g2.drawOval(xpoints[selected+1] - 15, ypoints[selected+1] - 15, 30, 30);
   				}
   				g2.setPaint(Color.RED);
   				g2.fill(landingPad);
   				if(dragging) {
   					g2.setColor(Color.WHITE);
   					g2.drawRect((int)landingPad.x, (int)landingPad.y, (int)landingPad.getWidth(), (int)landingPad.getHeight());
   				}
    		}};
        this.add(world, BorderLayout.WEST);
        world.setPreferredSize(new Dimension((int)model.getWorldBounds().getWidth(), (int)model.getWorldBounds().getHeight()));
        world.setBackground(Color.lightGray);
        
        landingPad =new Rectangle2D.Double(330, 100, 40, 10);
        
        this.model.addObserver(this);
        world.addMouseMotionListener(new MouseAdapter() {
        	@Override
        	public void mouseDragged(MouseEvent e) {
        		if(!notpress) {
        			dragging = true;
        			model.setPad2(e.getX() + oldX, e.getY() + oldY);
        		} else if(!notpressPeaks) {
        			draggingp = true;
        			model.setPeaks2(xpoints[selected+1], e.getY() + oldPeakY, selected);
        		}
        	}
        });
        world.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mousePressed(MouseEvent e) {
        		oldX = landingPad.x - e.getX();
        		oldY = landingPad.y - e.getY();
        		if(landingPad.contains(e.getX(), e.getY())) {
        			model.setLastpad(e.getX() + oldX, e.getY() + oldY);
        			model.setPad2(e.getX() + oldX, e.getY() + oldY);
        			return;
        		} else {
        			notpress = true;
        		}
        		for(int i = 0; i < peaks.size(); i++) {
        			if(hitttest(e.getX(), e.getY(), xpoints[i+1], ypoints[i+1])) {
        				selected = i;
        				oldPeakY = ypoints[i+1] - e.getY() - (model.getWorldBounds().getHeight() / 2);
        				model.setLastpeak(xpoints[i+1], e.getY() + oldPeakY);
        				model.setPeaks2(xpoints[i+1], e.getY() + oldPeakY, i);
        				return;
        			}
        		}
        		notpressPeaks = true;

        	}
        	@Override
        	public void mouseReleased(MouseEvent e) {
        		if(landingPad.contains(e.getX(), e.getY())) {
        			model.setPad(e.getX() + oldX, e.getY() + oldY);
        			dragging = false;
        			return;
        		} else {
        			notpress = false;
        		}
        		for(int i = 0; i < peaks.size(); i++) {
        			if(hitttest(e.getX(), e.getY(), xpoints[i+1], ypoints[i+1])) {
        				model.setPeaks(xpoints[i+1], e.getY() + oldPeakY, i);
        				draggingp = false;
        				return;
        			}
        		}
        		notpressPeaks = false;

        	}
        	public void mouseClicked(MouseEvent e) {
        		if(e.getClickCount() == 2) {
        			model.setLastpad(model.getPad().getX(), model.getPad().getY() - 5);
        			model.setPad(e.getX() - 20, e.getY() - 5);
        		}
        	}
        });


        cachePointsArray();
    }

    boolean hitttest(double _x, double _y, double px, double py) {
    	double distance = Math.sqrt((_x-px)*(_x-px) + (_y-py)*(_y-py));
    	return (distance <= 15);
    }

    void cachePointsArray() {
    	peaks = model.getPeaks();
        xpoints = new int[peaks.size() + 2];
        ypoints = new int[peaks.size() + 2];
     	int i = 1;
     	xpoints[0] = 0;
     	ypoints[0] = (int)(model.getWorldBounds().getHeight());
        for(Point2d p: peaks) {
            xpoints[i] = (int)p.x;
            if(p.y < -100) p.y = -100;
        	if(p.y > 100) p.y  = 100;
            ypoints[i] = (int)(p.y + (model.getWorldBounds().getHeight() / 2));
            i++;
        }
        xpoints[i] = (int)model.getWorldBounds().getWidth();
        ypoints[i] = (int)model.getWorldBounds().getHeight();
        peaksChanged = false;
    }


    @Override
    public void update(Observable o, Object arg) {
    	peaksChanged = (model.getPeaksChanged() && (!notpressPeaks));
    	model.boundsChecking();
    	landingPad.setRect(model.getPad().getX(), model.getPad().getY(), 40 ,10);
    	repaint();
    }

}
