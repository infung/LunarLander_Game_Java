import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
// the edit toolbar
public class ToolBarView extends JPanel implements Observer {
    private GameModel model;

    JButton undo = new JButton("Undo");
    JButton redo = new JButton("Redo");

    public ToolBarView(GameModel _model) {
        model = _model;
        model.addObserver(this);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        // prevent buttons from stealing focus
        undo.setFocusable(false);
        redo.setFocusable(false);
        add(undo);
        add(redo);
        undo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.undo();
            }
        });
        redo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.redo();
            }
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        undo.setEnabled(model.canUndo());
        redo.setEnabled(model.canRedo());
    }
}
