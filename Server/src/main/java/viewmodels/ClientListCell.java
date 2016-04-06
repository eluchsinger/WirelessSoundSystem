package viewmodels;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import models.networking.clients.NetworkClient;

/**
 * Created by Esteban Luchsinger on 06.04.2016.
 */
public class ClientListCell extends ListCell<NetworkClient> {

    private final TextField textField = new TextField();

    public ClientListCell() {
        textField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if(e.getCode() == KeyCode.ESCAPE){
                cancelEdit();
            }
        });

        textField.setOnAction(e -> {
            getItem().setName(textField.getText());
            setText(textField.getText());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        });
    }

    @Override
    protected void updateItem(NetworkClient client, boolean empty) {
        super.updateItem(client, empty);

        if(isEditing()) {
            textField.setText(client.getName());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            setContentDisplay(ContentDisplay.TEXT_ONLY);
            if(empty) {
                setText(null);
            } else {
                setText(client.getName());
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        textField.setText(getItem().getName());
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().getName());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}
