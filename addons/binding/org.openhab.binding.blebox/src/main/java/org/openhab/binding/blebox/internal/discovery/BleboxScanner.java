package org.openhab.binding.blebox.internal.discovery;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.openhab.binding.blebox.devices.DeviceInfo;
import org.openhab.binding.blebox.devices.StatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class BleboxScanner {
    private final static Logger logger = LoggerFactory.getLogger(BleboxScanner.class);

    static final int ENVISALINK_BRIDGE_PORT = 4025;
    static final int CONNECTION_TIMEOUT = 10;
    static final int TIMEOUT = 2500;
    static final String ENVISALINK_DISCOVERY_RESPONSE = "505";

    private BleboxDiscovery bleboxDiscovery = null;

    /**
     * Constructor.
     */
    public BleboxScanner(BleboxDiscovery bleboxDiscovery) {
        this.bleboxDiscovery = bleboxDiscovery;
    }

    public void test() {

    }

    /**
     * Method for Bridge Discovery.
     */
    public synchronized void discoverBridge() {
        logger.debug("Starting Blebox Discovery.");

        SubnetUtils subnetUtils = null;
        SubnetInfo subnetInfo = null;
        long lowIP = 0;
        long highIP = 0;

        try {

            List<Inet4Address> inet4 = getInet4Addresses();
            logger.error("discoverBridge(): ip addresses - {}", inet4.size());
            logger.error("discoverBridge(): ip main ip - {}", inet4.get(0));

            NetworkInterface networkInterfacee = NetworkInterface.getByInetAddress(inet4.get(0));
            logger.error("discoverBridge(): networkInterface - {}", networkInterfacee.toString());
            String hostAddress = inet4.get(0).getHostAddress();
            subnetUtils = new SubnetUtils(hostAddress + "/" + "24");
            subnetInfo = subnetUtils.getInfo();
            lowIP = convertIPToNumber(subnetInfo.getLowAddress());
            highIP = convertIPToNumber(subnetInfo.getHighAddress());

        } catch (IllegalArgumentException e) {
            logger.error("discoverBridge(): Illegal Argument Exception - {}", e.toString());
            return;
        } catch (Exception e) {
            logger.error("discoverBridge(): Error - Unable to get Subnet Information! {}", e.toString());
            return;
        }

        logger.debug("   Local IP Address: {} - {}", subnetInfo.getAddress(),
                convertIPToNumber(subnetInfo.getAddress()));
        logger.debug("   Subnet:           {} - {}", subnetInfo.getNetworkAddress(),
                convertIPToNumber(subnetInfo.getNetworkAddress()));
        logger.debug("   Network Prefix:   {}", subnetInfo.getCidrSignature().split("/")[1]);
        logger.debug("   Network Mask:     {}", subnetInfo.getNetmask());
        logger.debug("   Low IP:           {}", convertNumberToIP(lowIP));
        logger.debug("   High IP:          {}", convertNumberToIP(highIP));

        ExecutorService threadpool = Executors.newFixedThreadPool(50);
        Async async = Async.newInstance().use(threadpool);

        for (long ip = lowIP; ip <= highIP; ip++) {

            try {
                final String ipAddress = convertNumberToIP(ip);

                Gson gson = new Gson();

                // RequestConfig.Builder requestBuilder = RequestConfig.custom();
                // requestBuilder = requestBuilder.setConnectTimeout(TIMEOUT);
                // requestBuilder = requestBuilder.setConnectionRequestTimeout(TIMEOUT);

                URIBuilder builder = new URIBuilder();
                builder.setScheme("http").setHost(ipAddress).setPath("/api/device/state");
                URI requestURL = null;
                try {
                    requestURL = builder.build();
                } catch (URISyntaxException use) {
                }

                final Request request = Request.Get(requestURL).connectTimeout(2000).socketTimeout(2000);

                Future<Content> future = async.execute(request, new FutureCallback<Content>() {
                    @Override
                    public void failed(final Exception e) {
                        System.out.println(e.getMessage() + ": " + request);
                    }

                    @Override
                    public void completed(final Content content) {
                        // content.
                        System.out.println("Request completed: " + request);

                        try {
                            // Standard response for every blebox device, except gateBox
                            StatusResponse statusResp = gson.fromJson(content.asString(), StatusResponse.class);

                            if (statusResp.device != null) {

                                logger.debug("Found blebox device: {}", statusResp.device.id);
                                bleboxDiscovery.addDevice(ipAddress, statusResp.device.type, statusResp.device.id,
                                        statusResp.device.deviceName);
                            } else {
                                DeviceInfo deviceInfo = gson.fromJson(content.asString(), DeviceInfo.class);

                                if (deviceInfo != null) {
                                    logger.debug("Found blebox device: {}", deviceInfo.id);
                                    bleboxDiscovery.addDevice(ipAddress, deviceInfo.type, deviceInfo.id,
                                            deviceInfo.deviceName);
                                }
                            }

                        } catch (Exception ex) {
                        }
                    }

                    @Override
                    public void cancelled() {
                    }
                });

            } catch (Exception ex) {
                logger.debug("Error" + ex.getMessage());
                // handle exception here
            } finally {
                // httpClient.close();
            }
        }
    }

    /**
     * Returns this host's non-loopback IPv4 addresses.
     *
     * @return
     * @throws SocketException
     */
    private static List<Inet4Address> getInet4Addresses() throws SocketException {
        List<Inet4Address> ret = new ArrayList<Inet4Address>();

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                    ret.add((Inet4Address) inetAddress);
                }
            }
        }

        return ret;
    }

    /**
     * Returns this host's first non-loopback IPv4 address string in textual
     * representation.
     *
     * @return
     * @throws SocketException
     */
    private static String getHost4Address() throws SocketException {
        List<Inet4Address> inet4 = getInet4Addresses();
        return !inet4.isEmpty() ? inet4.get(0).getHostAddress() : null;
    }

    /**
     * Convert an IP address to a number.
     *
     * @param ipAddress
     * @return
     */
    private long convertIPToNumber(String ipAddress) {

        String octets[] = ipAddress.split("\\.");

        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
        }

        long ip = 0;

        for (int i = 3; i >= 0; i--) {
            long octet = Long.parseLong(octets[3 - i]);

            if (octet != (octet & 0xff)) {
                throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
            }

            ip |= octet << (i * 8);
        }

        return ip;
    }

    /**
     * Convert a number to an IP address.
     *
     * @param ip
     * @return
     */
    private String convertNumberToIP(long ip) {
        StringBuilder ipAddress = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {

            ipAddress.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                ipAddress.insert(0, '.');
            }

            ip = ip >> 8;
        }

        return ipAddress.toString();
    }
}
