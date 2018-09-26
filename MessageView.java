import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import java.text.NumberFormat;

public class MessageView extends JPanel implements Observer {
    private GameModel model;
    // status messages for game
    JLabel fuel = new JLabel("fuel: 50");
    JLabel speed = new JLabel("speed: 0.00");
    JLabel message = new JLabel("(Paused)");
    private static final NumberFormat formatter = NumberFormat.getNumberInstance();

    public MessageView(GameModel _model) {
        model = _model;
        model.addObserver(this);
        // want the background to be black
        setBackground(Color.BLACK);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(fuel);
        add(speed);
        add(message);

        for (Component c: this.getComponents()) {
            c.setForeground(Color.WHITE);
            c.setPreferredSize(new Dimension(100, 20));
        }
    }


    @Override
    public void update(Observable o, Object arg) {
        if(model.ship.getFuel() < 10) {
            fuel.setForeground(Color.RED);
        }
        fuel.setText("fuel: " + formatter.format((int)model.ship.getFuel()));
        if(model.ship.getSpeed() < model.ship.getSafeLandingSpeed()) {
            speed.setForeground(Color.GREEN);
        } else {
            speed.setForeground(Color.WHITE);
        }
        speed.setText("speed: " + formatter.format(model.ship.getSpeed()));
        message.setText(model.getMessage());
    }
}