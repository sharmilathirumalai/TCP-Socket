## Northwind Client Server Implementation - using Socket
 The task is to build server for the defined protocol "Order3901/1.0" to interact with northwind client to place order by using socket programming.

## Restrictions
- Only one client is allowed to interact with server at an instance.
- Server needs to be restarted when client shutdowns.

## Class
#### Server
   The main server starting point.
   
  **Methods**
  1. Server
    Contructor defining all the required params (like port) to start a server.

  2. pullIncomingMessage
    Reads the incoming message from socket buffer

#### MessageHandler
  Processes the incoming message and writes the ouput in the socket buffer.

  **Methods**
  1. incomingMessage
    Process the incoming message and decodes the header values.
    Checks for valid incoming message throws exception if error exists.

  2. processBody
    Reads the body content based on the header's content-length.

  3. performOperation
    Checks for valid user before performing any operation.
    Performs the given operation and handles the state exceptions.
    Handles any operation specific exception cases.

  4. WriteOutgoingMessage
    Writes the ouput to the ouput socket buffer from which client reads.
  
  5. shutdownConnection
    Clears all the user and order details

#### MyIdentity
  Has username, password and database for identity purpose.

#### Northwind
  Has database operations and computations for northwind.

   **Methods**
   1. authenticate:
      Checks for valid user using lastname and dateofbirth
   2. placeOrder:
      Places the given order; Also checks for any invalid cases and throws exception.
      Inserts orderdetails on successfull order placement.
   3. listElements:
      Lists the customer, products and products in the order based on the target
   4. checkProduct:
      Checks whether the product added in current order is valid
      
#### Order
  POJO class for storing order details

#### Client
  POJO class for storing user  and session details


## References
1) Generate random String of given size in Java. (2018, December 11). Retrieved from https://www.geeksforgeeks.org/generate-random-string-of-given-size-in-java/
