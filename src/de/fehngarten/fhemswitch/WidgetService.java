package de.fehngarten.fhemswitch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.RemoteViews;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import de.fehngarten.fhemswitch.MyLightScenes.Item;
import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;
import android.util.Log;

public class WidgetService extends Service
{
   //private static final String CLASSNAME = "WidgetProvider.";

   public static final String SWITCH_NEW_VALUE = "de.fehngarten.fhemswitch.SWITCH_NEW_VALUE";
   public static final String VALUE_NEW_VALUE = "de.fehngarten.fhemswitch.VALUE_NEW_VALUE";
   public static final String LIGHTSCENE_NEW_VALUE = "de.fehngarten.fhemswitch.LIGHTSCENE_NEW_VALUE";
   public static final String UNITS = "de.fehngarten.fhemswitch.UNITS";
   public static final String UNIT = "de.fehngarten.fhemswitch.UNIT";
   public static final String VALUE = "de.fehngarten.fhemswitch.VALUE";
   public static final String CONFIGFILE = "config.data";

   public static MySocket mySocket;

   public static String websocketUrl;
   public static String fhemUrl;
   //public static List<MySwitch> switches = new ArrayList<MySwitch>();
   public static Map<String, Integer> icons = new HashMap<String, Integer>();
   //public static MyLightScenes lightScenes;
   //public static List<MyValue> values = new ArrayList<MyValue>();
   public int switchCols;
   public int valueCols;
   
   private static AppWidgetManager appWidgetManager;
   private static int[] allWidgetIds;
   private static Handler handler = new Handler();
   private static Context context;

   private static ArrayList<String> switchesList = new ArrayList<String>();
   private static ArrayList<String> lightScenesList = new ArrayList<String>();
   private static ArrayList<String> valuesList = new ArrayList<String>();
   
   public static ConfigData configData;
   private ConfigDataOnly configDataOnly;

   private BroadcastReceiver bReceiver;
   public static PowerManager pm;
 
   public void onCreate()
   {
      //Log.i("trace", "onCreate fired");
      super.onCreate();

      bReceiver = new BroadcastReceiver()
      {
         @Override
         public void onReceive(Context context, Intent intent)
         {
            //Log.i("trace", "config changed");
            handler.postDelayed(checkSocketTimer, 10);
         }
      };

      IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
      registerReceiver(bReceiver, intentFilter);
   }

   @Override
   public void onDestroy()
   {
      //Log.i("trace", "onDestroy fired");
      mySocket.socket.disconnect();
      mySocket.socket.close();
      mySocket = null;
      unregisterReceiver(bReceiver);
      handler.removeCallbacks(checkSocketTimer);
      super.onDestroy();
   }
 
   public void onStart(Intent intent, int startId)
   {
      //Log.i("trace", "onStart fired");
      context = getApplicationContext();
      appWidgetManager = AppWidgetManager.getInstance(context);
      pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
      try
      {
         FileInputStream f_in = openFileInput(WidgetService.CONFIGFILE);
         ObjectInputStream obj_in = new ObjectInputStream(f_in);

         Object obj = obj_in.readObject();
         obj_in.close();

         //Log.i("config", "config.data found");
         if (obj instanceof ConfigDataOnly)
         {
            configDataOnly = (ConfigDataOnly) obj;
            websocketUrl = configDataOnly.urljs;
            fhemUrl = configDataOnly.urlpl;
            switchCols = configDataOnly.switchCols;
            valueCols = configDataOnly.valueCols;
         }
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      configData = new ConfigData();

      for (ConfigSwitchRow switchRow : configDataOnly.switchRows)
      {
         if (switchRow.enabled)
         {
            configData.switches.add(new MySwitch(switchRow.name, switchRow.unit, switchRow.cmd));
         }
      }
      
      int rowscount = configData.switches.size();
      double rowspercol = Math.ceil((double)rowscount / (double)(configDataOnly.switchCols + 1));
 
      configData.switchesCols.add(new ArrayList<MySwitch>());
      configData.switchesCols.add(new ArrayList<MySwitch>());
      configData.switchesCols.add(new ArrayList<MySwitch>());
         
      int rownum = 0;
      int colnum = 0;
      for (MySwitch switchRow : configData.switches)
      {
         rownum = rownum + 1;
         configData.switchesCols.get(colnum).add(switchRow);
         if (rownum % rowspercol == 0)
         {
            colnum++;
         }
      }      
           
      MyLightScene newLightScene = null;
      for (ConfigLightsceneRow lightsceneRow : configDataOnly.lightsceneRows)
      {
         if (lightsceneRow.isHeader)
         {
            newLightScene = configData.lightScenes.newLightScene(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.showHeader);
         }
         else
         {
            newLightScene.addMember(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.enabled);
         }
      }

      for (ConfigValueRow valueRow : configDataOnly.valueRows)
      {
         if (valueRow.enabled)
         {
            configData.values.add(new MyValue(valueRow.name, valueRow.unit));
         }
      }

      configData.valuesCols.add(new ArrayList<MyValue>());
      configData.valuesCols.add(new ArrayList<MyValue>());
      configData.valuesCols.add(new ArrayList<MyValue>());
      
      rowscount = configData.values.size();
      rowspercol = Math.ceil((double)rowscount / (double)(configDataOnly.valueCols + 1));
      
      rownum = 0;
      colnum = 0;
      for (MyValue valueRow : configData.values)
      {
         rownum = rownum + 1;
         configData.valuesCols.get(colnum).add(valueRow);
         if (rownum % rowspercol == 0)
         {
            colnum++;
         }
      }      
      
      icons.put("on", R.drawable.on);
      icons.put("set_on", R.drawable.set_on);
      icons.put("off", R.drawable.off);
      icons.put("set_off", R.drawable.set_off);
      icons.put("set_toggle", R.drawable.set_toggle);
      icons.put("undefined", R.drawable.undefined);
      icons.put("toggle", R.drawable.undefined);

      switchesList = configData.getSwitchesList();
      lightScenesList = configData.getLightScenesList();
      valuesList = configData.getValuesList();
     
      if (ConfigMain.mySocket != null && ConfigMain.mySocket.socket.connected())
      {
         ConfigMain.mySocket.socket.close();
      }
      
      if (mySocket != null)
      {
         //Log.i("trace", "reset socket");
         mySocket.socket.disconnect();
         mySocket.socket.close();
         mySocket = null;
      }

      initWidget();
      handler.postDelayed(checkSocketTimer, 2000);

      for (int id : allWidgetIds)
      {
         appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches0);
         appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches1);
         appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches2);
         appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.lightscenes);
         appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.values0);
         appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.values1);
         appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.values2);
      }
   }

   public static void sendCommand(String cmd, String unit, int position)
   {
      mySocket.sendCommand(cmd);
      if (unit.length() > 0)
      {
         for (MySwitch mySwitch : configData.switches)
         {
            //Log.i("clicked unit",unit + " " + mySwitch.unit); 
            if (mySwitch.unit.equals(unit))
            {
               mySwitch.setIcon("set_toggle");
               //Log.i("trace","set toggle for " + unit);
               //AppWidgetManager mgr = AppWidgetManager.getInstance(context);

               for (int id : allWidgetIds)
               {
                  appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches0);
                  appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches1);
                  appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches2);
               }
               break;
            }
         }         
      }
      else if (position > -1)
      {
         configData.lightScenes.items.get(position).activ = true;

         for (int id : allWidgetIds)
         {
            appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.lightscenes);
         }
         handler.postDelayed(deactLightscene, 500);
      }
   }

   public static Runnable deactLightscene = new Runnable()
   {
      @Override
      public void run()
      {
         //String methodname = "checkSocketTimer";
         //Log.d(CLASSNAME + methodname, "started");
         for (Item item : configData.lightScenes.items)
         {
            item.activ = false;
         }

         for (int id : allWidgetIds)
         {
            appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.lightscenes);
         }
      }
   };
   
   @SuppressWarnings("deprecation")
   @SuppressLint("NewApi")
   private void initSwitches(int widgetId)
   {
      //String methodname = "initSwitches";
      //Log.d(CLASSNAME + methodname, "started");
      RemoteViews mView = new RemoteViews(context.getPackageName(), R.layout.main_layout);
      if (switchesList.size() == 0)
      {
         //findViewById(R.id.values);
         mView.setViewVisibility(R.id.switches0, View.GONE);
      }
      else
      {
         final Intent onItemClick = new Intent(context, WidgetProvider.class);
         onItemClick.setData(Uri.parse(onItemClick.toUri(Intent.URI_INTENT_SCHEME)));
         final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onItemClick,
         PendingIntent.FLAG_UPDATE_CURRENT);

         mView.setPendingIntentTemplate(R.id.switches0, onClickPendingIntent);
         Intent switchIntent0 = new Intent(context,SwitchesService0.class);
         switchIntent0.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
         switchIntent0.setData(Uri.parse(switchIntent0.toUri(Intent.URI_INTENT_SCHEME)));
         mView.setRemoteAdapter(widgetId, R.id.switches0, switchIntent0);
         mView.setViewVisibility(R.id.switches0, View.VISIBLE);

         if (switchCols > 0)
         {
            Intent switchIntent1 = new Intent(context,SwitchesService1.class);
            switchIntent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            switchIntent1.setData(Uri.parse(switchIntent1.toUri(Intent.URI_INTENT_SCHEME)));
            mView.setRemoteAdapter(widgetId, R.id.switches1, switchIntent1);            
            mView.setPendingIntentTemplate(R.id.switches1, onClickPendingIntent);
            mView.setViewVisibility(R.id.switches1, View.VISIBLE);
         }
         else
         {  
            mView.setViewVisibility(R.id.switches1, View.GONE);
         }
         
         if (switchCols > 1)
         {
            Intent switchIntent2 = new Intent(context,SwitchesService2.class);
            switchIntent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            switchIntent2.setData(Uri.parse(switchIntent2.toUri(Intent.URI_INTENT_SCHEME)));
            mView.setRemoteAdapter(widgetId, R.id.switches2, switchIntent2);            
            mView.setPendingIntentTemplate(R.id.switches2, onClickPendingIntent);
            mView.setViewVisibility(R.id.switches2, View.VISIBLE);
         }
         else
         {  
            mView.setViewVisibility(R.id.switches2, View.GONE);
         }
      }
      appWidgetManager.updateAppWidget(widgetId, mView);
   }

   @SuppressWarnings("deprecation")
   @SuppressLint("NewApi")
   private void initLightScenes(int widgetId)
   {
      //String methodname = "initLightScenes";
      //Log.d(CLASSNAME + methodname, "started");

      RemoteViews mView = new RemoteViews(context.getPackageName(), R.layout.main_layout);
      if (lightScenesList.size() == 0)
      {
         //findViewById(R.id.values);
         mView.setViewVisibility(R.id.lightscenes, View.GONE);
      }
      else
      {
         mView.setViewVisibility(R.id.lightscenes, View.VISIBLE);
         Intent lightSceneIntent = new Intent(context, LightScenesService.class);
         lightSceneIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
         lightSceneIntent.setData(Uri.parse(lightSceneIntent.toUri(Intent.URI_INTENT_SCHEME)));
         mView.setRemoteAdapter(widgetId, R.id.lightscenes, lightSceneIntent);
   
         final Intent onItemClick = new Intent(context, WidgetProvider.class);
         onItemClick.setData(Uri.parse(onItemClick.toUri(Intent.URI_INTENT_SCHEME)));
         final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onItemClick,
         PendingIntent.FLAG_UPDATE_CURRENT);
         mView.setPendingIntentTemplate(R.id.lightscenes, onClickPendingIntent);
      }
      appWidgetManager.updateAppWidget(widgetId, mView);
   }

   @SuppressWarnings("deprecation")
   @SuppressLint("NewApi")
   private void initValues(int widgetId)
   {
      //String methodname = "initValues";
      //Log.d(CLASSNAME + methodname, "started");

      RemoteViews mView = new RemoteViews(context.getPackageName(), R.layout.main_layout);

      if (valuesList.size() == 0)
      {
         //findViewById(R.id.values);
         mView.setViewVisibility(R.id.values0, View.GONE);
      }
      else
      {

         final Intent onItemClick = new Intent(context, WidgetProvider.class);
         onItemClick.setData(Uri.parse(onItemClick.toUri(Intent.URI_INTENT_SCHEME)));
         final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onItemClick,
         PendingIntent.FLAG_UPDATE_CURRENT);
         
         Intent valueIntent0 = new Intent(context, ValuesService0.class);
         valueIntent0.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
         valueIntent0.setData(Uri.parse(valueIntent0.toUri(Intent.URI_INTENT_SCHEME)));
         mView.setRemoteAdapter(widgetId, R.id.values0, valueIntent0);
         mView.setPendingIntentTemplate(R.id.values0, onClickPendingIntent);
         mView.setViewVisibility(R.id.values0, View.VISIBLE);

         if (valueCols > 0)
         {
            Intent valueIntent1 = new Intent(context, ValuesService1.class);
            valueIntent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            valueIntent1.setData(Uri.parse(valueIntent1.toUri(Intent.URI_INTENT_SCHEME)));
            mView.setRemoteAdapter(widgetId, R.id.values1, valueIntent1);
            mView.setPendingIntentTemplate(R.id.values1, onClickPendingIntent);
            mView.setViewVisibility(R.id.values1, View.VISIBLE);
         }
         else
         {
            mView.setViewVisibility(R.id.values1, View.GONE);
         }

         if (valueCols > 1)
         {
            Intent valueIntent2 = new Intent(context, ValuesService1.class);
            valueIntent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            valueIntent2.setData(Uri.parse(valueIntent2.toUri(Intent.URI_INTENT_SCHEME)));
            mView.setRemoteAdapter(widgetId, R.id.values2, valueIntent2);
            mView.setPendingIntentTemplate(R.id.values2, onClickPendingIntent); 
            mView.setViewVisibility(R.id.values2, View.VISIBLE);
         }
         else
         {
            mView.setViewVisibility(R.id.values2, View.GONE);
         }
      }
      appWidgetManager.updateAppWidget(widgetId, mView);
   }

   private void initListviews()
   {
      for (int widgetId : allWidgetIds)
      {
         
         initSwitches(widgetId);
         initLightScenes(widgetId);
         initValues(widgetId);
      }
   }

   private void initWidget()
   {

      //String methodname = "initWidget";
      //Log.d(CLASSNAME + methodname, "started");

      if (mySocket != null)
      {
         mySocket.socket.disconnect();
         mySocket.socket.close();
      }
      initSocket();
      initListviews();
   }

   public Runnable checkSocketTimer = new Runnable()
   {
      @Override
      public void run()
      {
         //String methodname = "checkSocketTimer";
         //Log.d(CLASSNAME + methodname, "started");
         checkSocket();
         initListviews();

         handler.removeCallbacks(this);
         handler.postDelayed(this, 100000);
      }
   };

   @SuppressWarnings("deprecation")
   private void checkSocket()
   {
      Boolean callInitSocket = false;
      if (mySocket == null)
      {
         //Log.i("trace", "socket is null");
         callInitSocket = true;
      }
      else if (!mySocket.socket.connected())
      {
         //Log.i("trace", "socket is disconnected");
         mySocket.socket.close();
         mySocket = null;
         callInitSocket = true;
      }
      if (callInitSocket)
      {
         if (pm.isScreenOn())
         {
            initSocket();
         }
      }
   }

   private void setVisibility(String type)
   {
      //Log.i("type of setVisibility",type); 
      Context context = getApplicationContext();
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      RemoteViews mView = new RemoteViews(context.getPackageName(), R.layout.main_layout);
      if (type.equals("connected"))
      {
         mView.setViewVisibility(R.id.noconn, View.GONE);
         mView.setViewVisibility(R.id.switches0, View.VISIBLE);
         mView.setViewVisibility(R.id.lightscenes, View.VISIBLE);
         mView.setViewVisibility(R.id.values, View.VISIBLE);
      }
      else
      {
         mView.setViewVisibility(R.id.noconn, View.VISIBLE);
         mView.setViewVisibility(R.id.switches0, View.GONE);
         mView.setViewVisibility(R.id.lightscenes, View.GONE);
         mView.setViewVisibility(R.id.values, View.GONE);
         mView.setTextViewText(R.id.noconn,getString(R.string.noconn));
      }

      for (int widgetId : allWidgetIds)
      {
         appWidgetManager.updateAppWidget(widgetId, mView);
      }
   }
   private void initSocket()
   {
      //String methodname = "initSocket";
      //Log.d(CLASSNAME + methodname, "started");

      mySocket = new MySocket(websocketUrl);

      mySocket.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            String pw = configDataOnly.connectionPW;
            if (!pw.equals(""))
            {
               mySocket.socket.emit("authentication", pw);
            }
            //Log.i("socket", "connected");
            try
            {
               mySocket.requestValuesOnce(switchesList);
               mySocket.requestValuesOnce(lightScenesList);
               mySocket.requestValuesOnce(valuesList);
               mySocket.requestValuesOnChange(switchesList);
               mySocket.requestValuesOnChange(lightScenesList);
               mySocket.requestValuesOnChange(valuesList);
               setVisibility("connected");
            }
            catch (NullPointerException e)
            {
               //ignore this exception
            }
         }
      });
      mySocket.socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            //Log.i("socket", "disconnected");
            try
            {
               setVisibility("disconnected");
            }
            catch (NullPointerException e)
            {
               //ignore this exception
            }
         }
      });

      mySocket.socket.off("value");
      mySocket.socket.on("value", new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            //Log.i("get value", args[0].toString());
            JSONObject obj = (JSONObject) args[0];
            Iterator<String> iterator = obj.keys();
            String unit = null;
            while (iterator.hasNext())
            {
               unit = iterator.next();
               String value = null;
               try
               {
                  value = obj.getString(unit);
               }
               catch (JSONException e)
               {
                  e.printStackTrace();
               }

               if (switchesList.contains(unit))
               {
                  for (MySwitch mySwitch : configData.switches)
                  {
                     if (mySwitch.unit.equals(unit))
                     {
                        mySwitch.setIcon(value);

                        for (int id : allWidgetIds)
                        {
                           appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches0);
                           appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches1);
                           appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches2);
                        }
                        break;
                     }
                  }
               }
               /*
               else if (lightScenesList.contains(unit))
               {
                  for (MyLightScenes.MyLightScene lightScene : configData.lightScenes.lightScenes)
                  {
                     if (lightScene.unit.equals(unit))
                     {
                        lightScene.setActiv(value);
                        for (int id : allWidgetIds)
                        {
                           appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.lightscenes);
                        }
                        break;
                     }
                  }
               }
               */
               if (valuesList.contains(unit))
               {
                  for (MyValue myValue : configData.values)
                  {
                     if (myValue.unit.equals(unit))
                     {
                        myValue.value = value;

                        for (int id : allWidgetIds)
                        {
                           appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.values0);
                           appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.values1);
                           appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.values2);
                        }
                        break;
                     }
                  }
               }
            }
         }
      });
 
      mySocket.socket.on("fhemError", new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            //Log.i("socket", "disconnected");
            setVisibility("fhemError");
         }
      });
      
      mySocket.socket.on("fhemConn", new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            //Log.i("socket", "disconnected");
            setVisibility("connected");
         }
      });
      
   }

   @Override
   public IBinder onBind(Intent intent)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
