package de.fehngarten.fhemswitch;

import java.util.ArrayList;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class MySocket
{
   private static final String CLASSNAME = "MySocket.";
   private IO.Options options = new IO.Options();

   public Socket socket = null;

   public MySocket(String url)
   {
      Log.d(CLASSNAME, "started");

      try
      {
         socket = null;
         //options.secure = true;
         socket = IO.socket(url, options);        
         socket.connect();
      }
      catch (Exception e1)
      {
         Log.e("socket error", e1.toString() );
      }
      
      socket.on(Socket.EVENT_ERROR, new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
          Log.e("socket error", args[0].toString());
         }
      });
      socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            Log.e("connection error", args[0].toString());
            socket.close();
         }
      });
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
