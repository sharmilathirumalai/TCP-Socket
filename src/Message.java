import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Message
{
  private static final String currentVersion = "Order3901/1.0";
  private static final String bodyLengthHeader = "Content-Length";
  private static final String lineEnd = "\r\n";
  protected static final String login = "AUTH";
  protected static final String logout = "LOGOUT";
  protected static final String list_info = "LIST";
  protected static final String new_order = "NEW";
  protected static final String add_item = "ADD";
  protected static final String place_order = "ORDER";
  protected static final String abandon_order = "DROP";
  protected static final String cookieHeader = "Cookie";
  protected static final String setCookieHeader = "Set-Cookie";
  private Set<String> valid_operations = null;
  
  protected static final int successStart = 200;
  
  protected static final int successEnd = 300;
  private String operation = null;
  private String target = null;
  private String version = null;
  private int returnCode = 0;
  private String returnMessage = null;
  private Map<String, String> headers = null;
  private ArrayList<String> body = null;
  private boolean sendToServer = true;
 

  public Message()
  {
    operation = null;
    target = null;
    version = null;
    returnCode = 0;
    returnMessage = null;
    headers = new HashMap();
    body = new ArrayList();
    
    valid_operations = new HashSet();
    valid_operations.add("AUTH");
    valid_operations.add("LOGOUT");
    valid_operations.add("LIST");
    valid_operations.add("NEW");
    valid_operations.add("ADD");
    valid_operations.add("ORDER");
    valid_operations.add("DROP");
  }
  




  public void sendOutgoingMessage(PrintWriter paramPrintWriter, boolean paramBoolean)
    throws Exception
  {
    if (((operation == null) && (target != null)) || ((operation != null) && (target == null)))
      throw new Exception("No operation or target specified for outgoing request message");
    if (((returnCode == 0) && (returnMessage != null)) || ((returnCode != 0) && (returnMessage == null))) {
      throw new Exception("No return code and return message specified for outgoing response message");
    }
    String str1 = new String();
    String str2 = new String();
    String str3 = new String();
    Set localSet = headers.keySet();
    


    if ((operation != null) && (target != null)) {
      str1 = operation + " " + target + " " + "Order3901/1.0";
    } else {
      str1 = "Order3901/1.0 " + returnCode + " " + returnMessage;
    }
    for (Iterator localIterator = localSet.iterator(); localIterator.hasNext();) { String str4 = (String)localIterator.next();
      str2 = str2 + str4 + ": " + (String)headers.get(str4) + "\r\n";
    }
    String str4;
    for (Iterator localIterator = body.iterator(); localIterator.hasNext();) { str4 = (String)localIterator.next();
      str3 = str3 + str4;
    }
    


    if (str3.length() > 0) {
      str2 = str2 + "Content-Length: " + str3.length() + "\r\n";
    }
    


    if (paramBoolean) {
      System.out.println("outgoing----------");
      System.out.print(str1 + "\r\n" + str2 + "\r\n" + str3);
      System.out.println("------------------");
    }
    
    paramPrintWriter.write(str1 + "\r\n" + str2 + "\r\n" + str3);
    paramPrintWriter.flush();
  }
  
  public void receiveIncomingMessage(BufferedReader paramBufferedReader, boolean paramBoolean) throws IOException
  {
    Scanner localScanner = new Scanner(paramBufferedReader);
    


    if (paramBoolean) {
      System.out.println("incoming----------");
    }
    

    localScanner.useDelimiter("\r\n");
    


    String str1 = localScanner.nextLine();
    if (paramBoolean) {
      System.out.println(str1);
      for (int i = 0; i < str1.length(); i++) {int j = str1.charAt(i);System.out.print(j + " "); } System.out.println();
    }
    
    String[] arrayOfString = str1.split(" ", 3);
    
    if (arrayOfString[0].equals("Order3901/1.0"))
    {
      version = arrayOfString[0];
      returnCode = Integer.valueOf(arrayOfString[1]).intValue();
      returnMessage = arrayOfString[2];
    }
    else {
      operation = arrayOfString[0];
      target = arrayOfString[1];
      version = arrayOfString[2];
    }
    


    int i = 1;
    str1 = localScanner.nextLine();
    int k; if (paramBoolean) {
      System.out.println(str1);
      for (int j = 0; j < str1.length(); j++) { k = str1.charAt(j);System.out.print(k + " "); } System.out.println();
    }
    while ((i != 0) && (str1 != null)) {
      if (str1.length() == 0) {
        i = 0;
      }
      else {
        arrayOfString = str1.split(":", 2);
        if ((arrayOfString[1] != null) && (arrayOfString[1].charAt(0) == ' ')) {
          arrayOfString[1] = arrayOfString[1].substring(1, arrayOfString[1].length());
        }
        headers.put(arrayOfString[0], arrayOfString[1]);
      }
      if (i != 0) {
        str1 = localScanner.nextLine();
        if (paramBoolean) {
          System.out.println(str1);
          for (int j = 0; j < str1.length(); j++) { k = str1.charAt(j);System.out.print(k + " "); } System.out.println();
        }
      }
    }
    System.out.println("Headers done");
    



    int j = 0;
    
    System.out.println("Check for bytes");
    if (headers.containsKey("Content-Length")) {
      String str2 = (String)headers.get("Content-Length");
      System.out.println("key is Content-Length and value is --" + str2 + "--");
      j = Integer.valueOf(str2).intValue();
    }
    System.out.println("Byte count " + j);
    
    while (j > 0) {
      str1 = localScanner.nextLine();
      if ((paramBoolean) && (str1 != null)) {
        System.out.println(str1);
        for (int m = 0; m < str1.length(); m++) { int n = str1.charAt(m);System.out.print(n + " "); } System.out.println();
      }
      body.add(str1);
      j -= str1.length() + "\r\n".length();
    }
    

    if (paramBoolean) {
      System.out.println("------------------");
    }
  }
  




  public boolean isValidMessage()
  {
    boolean bool = false;
    
    if ((version != null) && (!version.equals("Order3901/1.0"))) {
      if ((operation != null) && (target != null)) {
        if (valid_operations.contains(operation)) {
          bool = true;
        }
      } else if ((returnCode > 0) && (returnMessage != null)) {
        bool = true;
      }
    }
    
    return bool;
  }
  

  public boolean setTask(String paramString1, String paramString2)
  {
    boolean bool = false;
    

    if (valid_operations.contains(paramString1)) {
      operation = paramString1;
      target = paramString2;
      bool = true;
    }
    
    return bool;
  }
  

  public boolean addHeader(String paramString1, String paramString2)
  {
    boolean bool = false;
    
    if ((paramString1 != null) && (paramString2 != null) && (headers != null) && (!headers.containsKey(paramString1))) {
      headers.put(paramString1, paramString2);
      bool = true;
    }
    return bool;
  }
  


  public boolean appendBody(String paramString)
  {
    boolean bool = false;
    
    if ((body != null) && (paramString != null)) {
      body.add(paramString + "\r\n");
      bool = true;
    }
    
    return bool;
  }
  

  public int getReturnCode()
  {
    return returnCode;
  }
  

  public String getReturnMessage()
  {
    return returnMessage;
  }
  

  public ArrayList<String> getBody()
  {
    return body;
  }
  

  public String getHeaderMatch(String paramString)
  {
    String str = null;
    if (headers.containsKey(paramString)) {
      str = (String)headers.get(paramString);
    }
    
    return str;
  }
  
  public String getOperation() {
    return operation;
  }
  
  public String getTarget() {
    return target;
  }
  
  public void setReturn(int paramInt, String paramString) {
    returnCode = paramInt;
    returnMessage = paramString;
  }
}