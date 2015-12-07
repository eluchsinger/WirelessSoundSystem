package ch.wirelesssoundsystem.server.models;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Esteban Luchsinger on 30.11.2015.
 */
public class Client {
    private SimpleStringProperty name;

    public Client(){
        this.name = new SimpleStringProperty();
    }

    public Client(String name){
        this.name = new SimpleStringProperty(name);
    }

    public String getName(){
        return this.name.get();
    }

    public void setName(String name){
        this.name.set(name);
    }

    @Override
    public String toString(){
        return this.getName();
    }
}
