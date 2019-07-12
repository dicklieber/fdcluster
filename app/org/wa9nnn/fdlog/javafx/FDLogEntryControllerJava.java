package org.wa9nnn.fdlog.javafx;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

public class FDLogEntryControllerJava {
    public TextField qsoCallsign;
    public TextField qsoClass;
    public TextField qsoSection;
    public BorderPane borderPane;


    public void init() {

        forcesCaps(qsoCallsign);
        forcesCaps(qsoClass);
        forcesCaps(qsoSection);

        qsoCallsign.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                System.out.println(event);
                char character = event.getCharacter().charAt(0);
                TextField tf = (TextField) event.getSource();
                String current = tf.getText();
                System.out.println(current);
                if (Callsign$.MODULE$.isCallsign(current) && Character.isDigit(character)) {
                    event.consume();
                    qsoClass.requestFocus();
                    qsoClass.setText("" + character);
                    qsoClass.positionCaret(1);
                }
            }
        });
        qsoClass.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                System.out.println(event);
                char character = event.getCharacter().charAt(0);
                TextField tf = (TextField) event.getSource();
                String current = tf.getText();
                System.out.println(current);
                if (ContestClass$.MODULE$.isClass(current)) {
                    event.consume();
                    qsoSection.requestFocus();
                    qsoSection.setText("" + character);
                    qsoSection.positionCaret(1);
                }
            }
        });
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        String text = keyEvent.getText();
        Character character = keyEvent.getCharacter().charAt(0);
        System.out.println(keyEvent);
        String current = qsoCallsign.getText();
        boolean mayBeCallsign = Callsign.isCallsign(current);
        if (Character.isDigit(character)) {
            System.out.println("mayBeCallsign = " + mayBeCallsign);
        }

    }

    void forcesCaps(TextField textField) {

        textField.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
    }
}
