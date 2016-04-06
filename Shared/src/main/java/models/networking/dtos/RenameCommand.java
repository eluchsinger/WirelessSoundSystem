package models.networking.dtos;

import java.io.Serializable;

/**
 * Created by Esteban on 01.04.2016.
 * This command tells the client to rename.
 */
public class RenameCommand implements Serializable {
    private static final long serialVersionUID = 5279400950017797772L;

    /**
     * Name field
     */
    public String name;

    /**
     * Constructor with parameter.
     * @param name The new name for the client.
     */
    public RenameCommand(String name) {
        this.name = name;
    }

    /**
     * @return Returns the name contained in this command.
     */
    public String getName() {
        return name;
    }
}
