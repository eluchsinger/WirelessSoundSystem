package ch.wirelesssoundsystem.server.controllers.networking.discovery;

import java.net.InetAddress;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 * This class handles the WSS Discovery Protocol.
 */
public class DiscoveryService {
    private InetAddress localAddress;

    /**
     * Deprecated, Use ScheduledExecutorService instead.
     * http://stackoverflow.com/questions/22378422/how-to-use-timertask-with-lambdas
     */
    private ScheduledExecutorService discoveryScheduledService;

    // Waiting delay for the task to run again in milliseconds.
    private final long delay = 1000;

    public DiscoveryService(InetAddress localAddress){
        this.localAddress = localAddress;

    }

    public void start(){
        this.discoveryScheduledService = Executors.newScheduledThreadPool(1);
    }


    public void stop(){
    }

    private void discover(){

    }

    private void found(InetAddress inetAddress){

    }

    public String getDiscoveryText(){
        return "WSSServer:" + this.localAddress.getHostAddress();
    }

    public InetAddress getLocalAddress(){
        return this.localAddress;
    }

    public void setLocalAddress(InetAddress localAddress){
        this.localAddress = localAddress;
    }
}
