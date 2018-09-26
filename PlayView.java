import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.awt.geom.Rectangle2D;
import javax.vecmath.Point2d;
import java.awt.geom.*;
import javax.vecmath.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Polygon;
import java.awt.Graphics;

// the actual game view
public class PlayView extends JPanel implements Observer {
	private GameModel model;
	private JPanel gameworld;
	private Rectangle2D gw;
	private float scale = 3.0f;          
	private Polygon terrain;
	int[] xpoints, ypoints;
	private boolean peaksChanged = false;
	private Rectangle2D.Double landingPad = new Rectangle2D.Double(330, 100, 40, 10);
	private boolean paused = true;

    public PlayView(GameModel _model) {
    	model = _model;
    	model.addObserver(this);
        // needs to be focusable for keylistener
        setFocusable(true);
        // want the background to be black
        setBackground(Color.BLACK);
        this.setLayout(new BorderLayout());
        gameworld =new JPanel() {
        	@Override
        	public void paintComponent(Graphics g) {
    			super.paintComponent(g);
    			Graphics2D g2 = (Graphics2D)g;
   				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  // antialiasing look nicer
                            RenderingHints.VALUE_ANTIALIAS_ON);
   				if (peaksChanged) {
        			cachePointsArray();
       			}
       			AffineTransform save = g2.getTransform();
       			g2.translate(model.ship.startPosition.x, model.ship.startPosition.y - 50);
       			g2.scale(scale, scale);
       			g2.translate((-1)*model.ship.getPosition().x, (-1)*(model.ship.getPosition().y - 50));
       			g2.setPaint(Color.lightGray);
   				g2.fill(new Rectangle2D.Double(0, 0, 700, 200));
       		terrain = new Polygon(xpoints, ypoints, model.getPeaks().size() + 2);
   				g2.setColor(Color.darkGray);
   				g2.fillPolygon(terrain);
          if(model.getMessage().equals("LANDED!")) {
            g2.setPaint(Color.GREEN);
            g2.fill(landingPad);
            g2.setPaint(Color.BLUE);
            g2.fill(model.ship.getShape());
            g2.setColor(Color.YELLOW);
            g2.fill(new Rectangle2D.Double(landingPad.x + 20, landingPad.y  - 10, 2, 10));
            g2.setColor(Color.RED);
            g2.fillPolygon(new int[] {(int)landingPad.x + 22, (int)landingPad.x + 32, (int)landingPad.x + 22}, new int[] {(int)landingPad.y  - 10, (int)landingPad.y  - 5, (int)landingPad.y  - 5}, 3);
          } else {
            g2.setPaint(Color.RED);
            g2.fill(landingPad);
            g2.setPaint(Color.BLUE);
            g2.fill(model.ship.getShape());
          }
          g2.setTransform(save);
          if(model.getMessage().equals("CRASH")) {
              g2.setPaint(Color.WHITE);
              g2.setStroke(new BasicStroke(12.0f));
              g2.drawString("PRESS SPACE TO RESTART", 270, 220);
              g2.setPaint(Color.ORANGE);
              g2.drawPolygon(new int[] {(int)model.getWorldBounds().getWidth()/2, 50, (int)model.getWorldBounds().getWidth()-50}, new int[] {30, (int)model.getWorldBounds().getHeight()+80, (int)model.getWorldBounds().getHeight()+80}, 3);
              Font font = g2.getFont().deriveFont( 80.0f );
              g2.setFont( font );
              g2.drawString("CRASH", 210, 220);
          } else if(model.getMessage().equals("LANDED!")) {
              g2.setPaint(Color.WHITE);
              g2.setStroke(new BasicStroke(12.0f));
              g2.drawString("PRESS SPACE TO RESTART", 290, 210);
              g2.setPaint(Color.CYAN);
              g2.draw(new Rectangle2D.Double(230, 60, 275, 180));
              Font font = g2.getFont().deriveFont( 100.0f );
              g2.setFont( font );
              g2.drawString("WIN", 270, 170);
          }
    		}};
        this.add(gameworld);
        gameworld.setPreferredSize(new Dimension((int)model.getWorldBounds().getWidth(), (int)model.getWorldBounds().getHeight()));
        gameworld.setBackground(Color.BLACK);
        this.addKeyListener(new KeyListener() {
        	@Override
       		public void keyTyped(KeyEvent e) {
        	}

      		@Override
        	public void keyPressed(KeyEvent e) {
      			    switch (e.getKeyCode()) {
      					case KeyEvent.VK_SPACE:
      						if(model.getMessage().equals("CRASH") || model.getMessage().equals("LANDED!")) {
      							model.ship.reset(model.ship.startPosition);
      						} else {
      							paused = !paused;
      						}
      						if(paused) model.setMessage("(Paused)");
      						else model.setMessage(" ");
      						model.ship.setPaused(paused);
        					break;
     					case KeyEvent.VK_A:
       						if(!paused) model.ship.thrustLeft();
       						break;      
     			 		case KeyEvent.VK_D:
        					if(!paused) model.ship.thrustRight();
        					break;      
      					case KeyEvent.VK_W:
        					if(!paused) model.ship.thrustUp();
        					break;
      					case KeyEvent.VK_S:
        					if(!paused) model.ship.thrustDown();
        					break;
    				} 
       		}

      		@Override
      		public void keyReleased(KeyEvent e) {

      		}
      	});

        cachePointsArray();

        gw = new Rectangle2D.Double(this.getX(), this.getY(), model.getWorldBounds().getWidth(), model.getWorldBounds().getHeight());
        terrain = new Polygon(xpoints, ypoints, model.getPeaks().size() + 2);
        model.ship.setPaused(paused);
    }

	void cachePointsArray() {
    	ArrayList<Point2d> peaks = model.getPeaks();
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
    	if(!paused && (terrain.intersects(model.ship.getShape()) || !gw.contains(model.ship.getShape()))) {
    		model.setMessage("CRASH");
    		paused = true;
    		model.ship.setPaused(paused);
		return;
    	}
    	if(!paused && landingPad.intersects(model.ship.getShape())) {
    		if(model.ship.getSpeed() >= model.ship.getSafeLandingSpeed()) {
    			model.setMessage("CRASH");
    		} else {
    			model.setMessage("LANDED!");
    		}
    		paused = true;
    		model.ship.setPaused(paused);
		return;
    	}
    	model.shipboundsChecking();
    	peaksChanged = model.getPeaksChanged();
    	model.boundsChecking();
    	landingPad.setRect(model.getPad().getX(), model.getPad().getY(), 40 ,10);
    	repaint();
    }
}
