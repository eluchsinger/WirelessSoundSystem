package ch.wirelesssoundsystem.server.controllers.networking;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 */
public class Utility {
    private static InetAddress broadcastAddress;

    static {
        System.setProperty("java.net.preferIPv4Stack" , "true");

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while(interfaces != null && interfaces.hasMoreElements()){
                NetworkInterface currentInterface = interfaces.nextElement();

                if(!currentInterface.isLoopback()){
                    for(InterfaceAddress address : currentInterface.getInterfaceAddresses()){
                        InetAddress broadcast = address.getBroadcast();
                        if(broadcast != null){
                            broadcastAddress = broadcast;
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            // log the damn exception!
            e.printStackTrace();
        }
    }


    public static InetAddress getLocalAddress4(){
        InetAddress localAddress = null;

        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e != null && e.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    System.out.println(i.getHostAddress());
                    if (!i.isLinkLocalAddress() && !i.isMulticastAddress() && !i.isLoopbackAddress()){
                        localAddress = i;
                    }
                }
            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

        return localAddress;
    }

    /**
     * Gets the BroadcastAddress for IPv4.
     * This address is cached and only calculated on static class
     * initialization.
     *
     * Threadsafe.
     * @return
     * @throws SocketException
     */
    public static InetAddress getBroadcastAddress4() {
        return broadcastAddress;
    }
}
