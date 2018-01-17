package SIM.dependOnClass;

import java.util.Observable;
import java.util.Observer;

public class Characters extends Observable implements Observer {

    public Characters() {
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Characters: changes detected: " + arg);
        System.out.println("Characters: Update fields");
        setChanged();
        notifyObservers("characters are updated");
    }
}
