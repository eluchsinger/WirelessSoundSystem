package ch.wirelesssoundsystem.server.controllers.networking;

import java.net.*;
import java.util.Enumeration;

/**
 * Created by Esteban Luchsinger on 03.12.2015.
 */
public class Utility {

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
     * @return
     * @throws SocketException
     */
    public static InetAddress getBroadcastAddress4() {

        // Todo: Check other possibility
        // <a href=http://enigma2eureka.blogspot.ch/2009/08/finding-your-ip-v4-broadcast-address.html>
        System.setProperty("java.net.preferIPv4Stack" , "true");

        // Init
        InetAddress broadcastAddress = null;


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
            e.printStackTrace();
        }

        return broadcastAddress;

    }
}
