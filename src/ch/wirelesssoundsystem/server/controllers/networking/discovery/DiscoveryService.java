package ch.wirelesssoundsystem.server.controllers.networking.discovery;

import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Esteban Luchsinger on 04.12.2015.
 * Singleton handler for the DiscoveryService.
 */
public class DiscoveryService {
    private static DiscoveryService ourInstance = new DiscoveryService();
    private InetAddress localAddress;

    /**
     * Deprecated, Use ScheduledExecutorService instead.
     * http://stackoverflow.com/questions/22378422/how-to-use-timertask-with-lambdas
     */
    private ScheduledExecutorService discoveryScheduledService;

    // Waiting delay for the task to run again in milliseconds.
    private final long delay = 1000;

    public static DiscoveryService getInstance() {
        return ourInstance;
    }

    private DiscoveryService() {
    }

    public void start()
    {
        if(this.discoveryScheduledService == null || this.discoveryScheduledService.isShutdown() || this.discoveryScheduledService.isTerminated()) {
            this.discoveryScheduledService = Executors.newScheduledThreadPool(1);

            this.discoveryScheduledService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    discover();
                }
            }, 0, 2, TimeUnit.SECONDS);
        }
    }

    public void stop(){
        this.discoveryScheduledService.shutdown();

        this.discoveryScheduledService = null;
    }

    /**
     * This is the discover method.
     * This method contains the logic of the discovery protocol.
     */
    private void discover(){
        System.out.println("BEEP");

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
