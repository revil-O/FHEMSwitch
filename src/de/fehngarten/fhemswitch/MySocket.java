package de.fehngarten.fhemswitch;

import java.net.URISyntaxException;
import java.util.ArrayList;

import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class MySocket
{
   private static final String CLASSNAME = "MySocket.";
   private IO.Options options;

   public Socket socket;

   public MySocket(String url)
   {
      Log.d(CLASSNAME, "started");

      try
      {
         socket = IO.socket(url, options);        
         socket.connect();
      }
      catch (URISyntaxException e1)
      {
         e1.printStackTrace();
      }
   }

   public void requestValuesOnce(ArrayList<String> unitsList)
   {
      for (String unit : unitsList)
      {
         socket.emit("getValueOnce", unit);
      }
   }

   public void requestValuesOnChange(ArrayList<String> unitsList)
   {
      for (String unit : unitsList)
      {
         socket.emit("getValueOnChange", unit);
      }
   }

   public void sendCommand(String cmd)
   {
      socket.emit("commandNoResp", cmd);
   }

 

}
