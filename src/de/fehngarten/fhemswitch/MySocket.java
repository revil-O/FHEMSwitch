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
   public String url;
   public MySocket(String url)
   {
      Log.d(CLASSNAME, "started");

      try
      {
         socket = null;
         options.reconnection = false;
         socket = IO.socket(url, options);        
         socket.connect();
         this.url = url;
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
            Log.w("socket.io", "lost connection to server");
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

      socket.on(Socket.EVENT_CONNECT, new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            Log.e("connection established", "");
         }
      });
   }

   public void requestValues(ArrayList<String> unitsList, String type)
   {
      for (String unit : unitsList)
      {
         if (type.equals("once"))
         { 
            socket.emit("getValueOnce", unit);
         }
         else
         {
            socket.emit("getValueOnChange", unit);
         }
      }
   }

   public void sendCommand(String cmd)
   {
      //Log.i("mySocket command",cmd);
      socket.emit("commandNoResp", cmd);
   }
}
