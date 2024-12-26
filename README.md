# Documentation : Distributed-Averaging-System
### Overview
<p>
  The DAS ((Distributed Average System) project is a master-slave communication
system implemented using UDP. <br>Program is operating in two modes Master and Slave mode.
Master mode collects the data that has been sent from Slave mode, calculates their average and broadcast the result
lastly listens for the signal to terminate program.</b>
</p>

### UDP Protocol Workflow
1. **Slave to Master Communication:**
<br>Slave send integer value or control signal to Master via UDP packets</br>
2. **Master Communication:**
   <br>Master listens on a specified port for incoming UDP packets</br>
   Based on the message does either : append the value to the list, computes the average or terminates the program.


### Implementation
#### Execution : 
Program requires two arguments
1. `port` : The port number for communication
2. `number` : Integer to be sent with **Slave** mode or initial value of **Master** mode

* Command to run : `java DAS <port> <number>`

## Code Explanation :
### `main` Method :

Handles command line arguments and mode selection.

Handles exceptions for the arguments.

If a `SocketExpection` occurs program starts to run in **Slave** mode
otherwise runs in **Master**.

---

### `runMaster` Method : 

Handles the master mode functionality with managing received data,handles  signals
and broadcasting messages.

Creates a list starts with appending the initial data.
<br>The line `byte[] buffer = new byte[1024]` allocate a fixed sized array for store UDP packet data.

Then starts listening the data and extracts the data from datagram packet using:

`String receivedData = new String(packet.getData(), 0, packet.getLength());`

`packet.getData()` : retrieves the array contains raw byte data.

Data is converted to String for easier interpretation.

Parsing Integer number ` receivedValue = Integer.parseInt(receivedData)` and if it's not valid sends `NumberFormatException`.

if `recivedValue == 0` : compute the average for given integers using `computeAverage(List<Integer> numbers)` method
<br>if `recivedValue == -1` : terminate the program
<br>else : display the receivedValue and add it to the list

---

### `runSlave` Method :
Creates a datagramSocket without binding a specific port for UDP communication.
<br>`try (DatagramSocket socket = new DatagramSocket())`
<br>The method `gerBroadcastAddress()` dynamically determines the broadcast address of local network.
Ensures Slave can send packets master in same network.

Then prepares the message to send converts Slave's integer number to String.
<br>Encapsulates to the packet data , length of the data , broadcast address and port number :
<br>`DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, masterPort);`
<br>Sends the packet using `socket.send()` method.
<br>Displays the Data that has sent and Port number of the Master.

---

### `computeAverage(List<Integer> numbers)`:

**Purpose:** Computes the average of integers received by the MASTER.

**Details:**

- Iterates over the list of integers, excluding any `0` values.
- Calculates the sum and the count of valid numbers.
- Returns the average or `0` if no valid numbers are present.

---

### `broadcastMessage(DatagramSocket socket, int value)`:

**Purpose:** Sends a message to all SLAVE nodes via UDP broadcast.

**Details:**

- Converts the integer value into a byte array.
- Determines the broadcast address dynamically using `getBroadcastAddress`.
- Sends the message to the broadcast address on the specified port.
- Logs the broadcast operation.

---

### `getBroadcastAddress()`:

**Purpose:** Dynamically identifies the network's broadcast address.

**Details:**

- Iterates through available network interfaces.
- Excludes loopback and inactive interfaces.
- Returns the first valid broadcast address or throws an exception if none are found

---

### Difficulties And Known Errors :
Difficulty that I encounter with is setting the `broadcastMessage` method and indemnifying networks address.

I find a solution with creating `getBroadcastAddress` method and dynamically identifying networks address instead of
using `'localhost' `.

#### Know Errors : 
There is no known error that I encounter.


