

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class DAS {
    private static int port;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java DAS <port> <number>");
            System.exit(1);
        }

        int number;
        try {
            port = Integer.parseInt(args[0]);
            number = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Both <port> and <number> must be integers.");
            System.exit(1);
            return;
        }

        try {
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("Running in MASTER mode.");
            runMaster(socket, number);
        } catch (SocketException e) {
            System.out.println("Port is already in use. Running in SLAVE mode.");
            runSlave(port, number);
        }
    }

    private static void runMaster(DatagramSocket socket, int initialValue) {
        List<Integer> receivedNumbers = new ArrayList<>();
        receivedNumbers.add(initialValue);
        byte[] buffer = new byte[1024];

        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String receivedData = new String(packet.getData(), 0, packet.getLength());
                int receivedValue;
                try {
                    receivedValue = Integer.parseInt(receivedData);
                } catch (NumberFormatException e) {
                    System.err.println("Received invalid data: " + receivedData);
                    continue;
                }

                if (receivedValue == 0) {
                    int average = computeAverage(receivedNumbers);
                    System.out.println("Computed average: " + average);
                    broadcastMessage(socket, average);
                } else if (receivedValue == -1) {
                    System.out.println("Received '-1', terminating...");
                    broadcastMessage(socket, -1);
                    break;
                } else {
                    System.out.println("Received: " + receivedValue);
                    receivedNumbers.add(receivedValue);
                }
            }
        } catch (IOException e) {
            System.err.println("Error in master mode: " + e.getMessage());
        } finally {
            socket.close();
        }
    }

    private static void runSlave(int masterPort, int number) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress broadcastAddress = getBroadcastAddress();
            String message = String.valueOf(number);
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, masterPort);
            socket.send(packet);
            System.out.println("Sent value: " + number + " to master on port: " + masterPort);

        } catch (IOException e) {
            System.err.println("Error in slave mode: " + e.getMessage());
        }
    }

    private static int computeAverage(List<Integer> numbers) {
        int sum = 0;
        int count = 0;
        for (int num : numbers) {
            if (num != 0) {
                sum += num;
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    private static void broadcastMessage(DatagramSocket socket, int value) {
        try {
            String message = String.valueOf(value);
            byte[] data = message.getBytes();

            InetAddress broadcastAddress = getBroadcastAddress();
            DatagramPacket broadcastPacket = new DatagramPacket(data, data.length, broadcastAddress, port);

            socket.setBroadcast(true);
            socket.send(broadcastPacket);

            System.out.println("Broadcasting: " + message + " to port " + port);
        } catch (Exception e) {
            System.err.println("Error broadcasting message: " + e.getMessage());
        }
    }

    private static InetAddress getBroadcastAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) continue;

            for (InterfaceAddress address : iface.getInterfaceAddresses()) {
                InetAddress broadcast = address.getBroadcast();
                if (broadcast != null) {
                    return broadcast;
                }
            }
        }
        throw new SocketException("No broadcast address found.");
    }
}

