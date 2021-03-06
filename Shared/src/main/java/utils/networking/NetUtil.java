package utils.networking;

import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * <pre>
 * Created by Esteban Luchsinger on 03.12.2015.
 * Provides some basic functionality.
 * </pre>
 */
public class NetUtil {
    private static InetAddress broadcastAddress;

    /**
     * Static initialization because this might be an expensive operation.
     */
    static {
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


    /**
     * Gets the local IPv4 address.
     * Might have to set the JVM Machine to use IPv4 by default.
     * @return Returns the IPv4 address of localhost.
     */
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
                    if (!i.isLinkLocalAddress() && !i.isMulticastAddress() && !i.isLoopbackAddress()){
                        localAddress = i;
                    }
                }
            }
        } catch (SocketException e1) {
            LoggerFactory.getLogger(NetUtil.class).error("Filed getting the local address",e1);
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
     */
    public static InetAddress getBroadcastAddress4() {
        return broadcastAddress;
    }
}
