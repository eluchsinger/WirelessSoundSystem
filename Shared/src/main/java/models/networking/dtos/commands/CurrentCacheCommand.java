package models.networking.dtos.commands;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Esteban Luchsinger on 30.04.2016.
 * This command contains the hashCodes of the current cache.
 */
public class CurrentCacheCommand implements Serializable {
    private static final long serialVersionUID = 5015576093771140120L;

    public final List<Integer> currentCache;

    public CurrentCacheCommand(List<Integer> currentCache){
        this.currentCache = currentCache;
    }
}
