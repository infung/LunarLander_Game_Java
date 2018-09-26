import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.undo.*;
import javax.vecmath.*;
import java.util.concurrent.ThreadLocalRandom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.undo.*;

public class GameModel extends Observable {
    Rectangle2D.Double worldBounds;
    Point2d pad;
    Point2d lastpad;
    Point2d lastpeak;
    ArrayList<Point2d> Peaks;
    boolean peakschanged;
    private UndoManager undoManager;
    public Ship ship;
    private String message = "(Paused)";

    public GameModel(int fps, int width, int height, int peaks) {
        undoManager = new UndoManager();
        ship = new Ship(60, width/2, 50);   
        worldBounds = new Rectangle2D.Double(0, 0, width, height);
        pad = new Point2d(330, 100);
        lastpad = new Point2d(330, 100);
        Peaks = new ArrayList<Point2d>();
        for(int i = 0; i < peaks; i++) {
            double _y = ThreadLocalRandom.current().nextInt(0, 100 + 1);
            double _x = i * (700/(peaks - 1));
            if(i == (peaks - 1)) _x = 700;
            Point2d p = new Point2d(_x, _y);
            Peaks.add(p);
        }
        peakschanged = false;
        lastpeak = new Point2d(0, 0);
        // anonymous class to monitor ship updates
        ship.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                setChangedAndNotify();
            }
        });
    }
    
    // Ship
    // - - - - - - - - - - -
    public void setMessage(String m) {
        message = m;
        //setChangedAndNotify();
    }

    public String getMessage() {
        return message;
    }

    public void shipboundsChecking() {
        if(ship.position.x < 0 || (ship.position.x + 10) > (double)worldBounds.getWidth() || ship.position.y < 0 || ship.position.y > (double)(worldBounds.getHeight() - 10)) {
            double _x = ship.position.x;
            double _y = ship.position.y;
            if ((ship.position.x + 10) > worldBounds.getWidth()) {
                _x = worldBounds.getWidth() - 10;
            }
            if (ship.position.x < 0) {
                _x = 0;
            }
            if ((ship.position.y + 10) > worldBounds.getHeight()) {
                _y = worldBounds.getHeight() - 10;
            }
            if (ship.position.y < 0) {
                _y = 0;
            }
            ship.position.x = _x;
            ship.position.y = _y;
        }
    }

    // World
    // - - - - - - - - - - -
    public final Rectangle2D getWorldBounds() {
        return worldBounds;
    }  

    public void setPad(double x, double y) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
            final Point2d oldp = new Point2d(lastpad.x, lastpad.y);
            final Point2d newp = new Point2d(x, y);

            public void redo() throws CannotRedoException {
                super.redo();
                pad = newp;
                setChangedAndNotify();
            }

            public void undo() throws CannotUndoException {
                super.undo();
                pad = oldp;
                setChangedAndNotify();
            }
        };
        undoManager.addEdit(undoableEdit);

        pad.x = x;
        pad.y = y;
        peakschanged = false;
        setChangedAndNotify();
    }

    public void setLastpad(double x, double y){
        lastpad.x = x;
        lastpad.y = y;
    }

    public void setPad2(double x, double y) {
        pad.x = x;
        pad.y = y;
        peakschanged = false;
        setChangedAndNotify();
    }

    public Point2d getPad() {
            return pad;
    }

    public void boundsChecking() {
        if(pad.x < 0 || pad.x > (double)worldBounds.getWidth() || pad.y < 0 || pad.y > (double)(0 + worldBounds.getHeight())) {
            double _x = pad.x;
            double _y = pad.y;
            if ((pad.x + 40) > worldBounds.getWidth()) {
                _x = worldBounds.getWidth() - 40;
            }
            if (pad.x < 0) {
                _x = 0;
            }
            if ((pad.y + 10) > worldBounds.getHeight()) {
                _y = worldBounds.getHeight() - 10;
            }
            if (pad.y < 0) {
                _y = 0;
            }
            pad.x = _x;
            pad.y = _y;
        }
    }

    public ArrayList<Point2d> getPeaks() {
        return Peaks;
    }

    public void setPeaks(double _x, double _y, int index) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
            final Point2d oldp = new Point2d(lastpeak.x, lastpeak.y);
            final Point2d newp = new Point2d(_x, _y);

            public void redo() throws CannotRedoException {
                super.redo();
                Peaks.set(index, newp);
                peakschanged = true;
                setChangedAndNotify();
            }

            public void undo() throws CannotUndoException {
                super.undo();
                Peaks.set(index, oldp);
                peakschanged = true;
                setChangedAndNotify();
            }
        };
        undoManager.addEdit(undoableEdit);

        Point2d newpp = new Point2d(_x, _y);
        Peaks.set(index, newpp);
        peakschanged = true;
        setChangedAndNotify();
    }

    public void setLastpeak(double x, double y) {
        lastpeak.x = x;
        lastpeak.y = y;
    }

    public void setPeaks2(double _x, double _y, int index) {
        if(_y < -100) _y = -100;
        if(_y > 100) _y  = 100;
        Point2d newpp = new Point2d(_x, _y);
        Peaks.set(index, newpp);
        peakschanged = true;
        setChangedAndNotify();
    }

    public boolean getPeaksChanged() {
        return peakschanged;
    }

    public void undo() {
        if(canUndo()) {
            undoManager.undo();
        }
    }

    public void redo() {
        if(canRedo()) {
            undoManager.redo();
        }
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public  boolean canRedo()  {
        return undoManager.canRedo();
    }

    // helper function to do both
    void setChangedAndNotify() {
        setChanged();
        notifyObservers();
    }

}



