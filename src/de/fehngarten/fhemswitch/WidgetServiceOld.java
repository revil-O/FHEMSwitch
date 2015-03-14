package de.fehngarten.fhemswitch;

import java.io.FileInputStream;
import android.view.View;
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
import android.util.Log;
import android.widget.RemoteViews;

import com.github.nkzawa.emitter.Emitter;

import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;

public class WidgetServiceOld extends Service
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

   private AppWidgetManager appWidgetManager;
   private int[] allWidgetIds;
   private Handler handler = new Handler();
   private Context context;
   public RemoteViews mView;

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
      
      ComponentName thisWidget = new ComponentName(getApplicationContext(), WidgetProvider.class);
      allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
      
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

      MyLightScene newLightScene = null;
      for (ConfigLightsceneRow lightsceneRow : configDataOnly.lightsceneRows)
      {
         if (lightsceneRow.isHeader)
         {
            newLightScene = configData.lightScenes.newLightScene(lightsceneRow.name, lightsceneRow.unit);
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

      icons.put("on", R.drawable.on);
      icons.put("off", R.drawable.off);
      icons.put("undefined", R.drawable.undefined);
      icons.put("toggle", R.drawable.undefined);

      switchesList = configData.getSwitchesList();
      lightScenesList = configData.getLightScenesList();
      valuesList = configData.getValuesList();

      if (mySocket != null)
      {
         //Log.i("trace", "reset socket");
         mySocket.socket.disconnect();
         mySocket.socket.close();
         mySocket = null;
      }
      mView = new RemoteViews(context.getPackageName(), R.layout.main_layout);
      initWidget();
      handler.postDelayed(checkSocketTimer, 100000);
   }

   public static void sendCommand(String cmd)
   {
      mySocket.sendCommand(cmd);
   }

   @SuppressWarnings("deprecation")
   @SuppressLint("NewApi")
   private void initSwitches(int widgetId)
   {
      //String methodname = "initSwitches";
      //Log.d(CLASSNAME + methodname, "started");
      //RemoteViews mView = new RemoteViews(context.getPackageName(), R.layout.main_layout);
      Context context = getApplicationContext();
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      RemoteViews mView = new RemoteViews(context.getPackageName(), R.layout.main_layout);
      Log.i("switchesList.size",Integer.toString(switchesList.size()));
      if (switchesList.size() == 0)
      {
         //findViewById(R.id.values);
         mView.setViewVisibility(R.id.switches, View.GONE);
      }
      else
      {
         Intent switchIntent = new Intent(context, SwitchesService.class);
         switchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
         switchIntent.setData(Uri.parse(switchIntent.toUri(Intent.URI_INTENT_SCHEME)));
         mView.setRemoteAdapter(widgetId, R.id.switches, switchIntent);

         final Intent onItemClick = new Intent(context, WidgetProvider.class);
         onItemClick.setData(Uri.parse(onItemClick.toUri(Intent.URI_INTENT_SCHEME)));
         final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onItemClick,
         PendingIntent.FLAG_UPDATE_CURRENT);
         mView.setPendingIntentTemplate(R.id.switches, onClickPendingIntent);
      }
      appWidgetManager.updateAppWidget(widgetId, mView);
   }

   @SuppressWarnings("deprecation")
   @SuppressLint("NewApi")
   private void initLightScenes(int widgetId)
   {
      //String methodname = "initLightScenes";
      //Log.d(CLASSNAME + methodname, "started");

      if (lightScenesList.size() == 0)
      {
         //findViewById(R.id.values);
         mView.setViewVisibility(R.id.lightscenes, View.GONE);
      }
      else
      {
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

      //RemoteViews mView = new RemoteViews(context.getPackageName(), R.layout.main_layout);

      if (valuesList.size() == 0)
      {
         //findViewById(R.id.values);
         mView.setViewVisibility(R.id.values, View.GONE);
      }
      else
      {
         Intent valueIntent = new Intent(context, ValuesService.class);
         valueIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
         valueIntent.setData(Uri.parse(valueIntent.toUri(Intent.URI_INTENT_SCHEME)));
         mView.setRemoteAdapter(widgetId, R.id.values, valueIntent);

         final Intent onItemClick = new Intent(context, WidgetProvider.class);
         onItemClick.setData(Uri.parse(onItemClick.toUri(Intent.URI_INTENT_SCHEME)));
         final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onItemClick,
         PendingIntent.FLAG_UPDATE_CURRENT);
         mView.setPendingIntentTemplate(R.id.values, onClickPendingIntent);
      }

      appWidgetManager.updateAppWidget(widgetId, mView);
   }

   private void initListviews()
   {
      //Log.i("widget_ids",allWidgetIds.toString());
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

      initListviews();
      setVisibility("connected");
      initSocket();
   }

   public Runnable checkSocketTimer = new Runnable()
   {
      @Override
      public void run()
      {
         //String methodname = "checkSocketTimer";
         //Log.d(CLASSNAME + methodname, "started");
         //Context context = getApplicationContext();

         //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         //int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

         checkSocket();
         initListviews();

         handler.removeCallbacks(this);
         handler.postDelayed(this, 30000);
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
         mySocket.socket.off();
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
      //findViewById(R.id.switches_header1).setVisibility(View.VISIBLE);
      Log.i("type of setVisibility",type); 
      Context context = getApplicationContext();
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      RemoteViews mView = new RemoteViews(context.getPackageName(), R.layout.main_layout);
      if (type.equals("connected"))
      {
         mView.setViewVisibility(R.id.noconn, View.GONE);
         mView.setViewVisibility(R.id.switches, View.VISIBLE);
         mView.setViewVisibility(R.id.lightscenes, View.VISIBLE);
         mView.setViewVisibility(R.id.values, View.VISIBLE);
      }
      else
      {
         mView.setViewVisibility(R.id.noconn, View.VISIBLE);
         mView.setViewVisibility(R.id.switches, View.GONE);
         mView.setViewVisibility(R.id.lightscenes, View.GONE);
         mView.setViewVisibility(R.id.values, View.GONE);
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

      mySocket.socket.on("connect", new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            Log.i("socket", "connected");

            setVisibility("connected");
            try
            {
               mySocket.requestValuesOnce(switchesList);
               mySocket.requestValuesOnce(lightScenesList);
               mySocket.requestValuesOnce(valuesList);
               mySocket.requestValuesOnChange(switchesList);
               mySocket.requestValuesOnChange(lightScenesList);
               mySocket.requestValuesOnChange(valuesList);
            }
            catch (NullPointerException e)
            {
               //ignore this exception
            }
         }
      });

      mySocket.socket.on("disconnect", new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            //Log.i("socket", "connected");

            setVisibility("disconnected");
            
         }
      });
      
      mySocket.socket.on("value", new Emitter.Listener()
      {
         @Override
         public void call(Object... args)
         {
            Log.i("get value", args[0].toString());
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
                           appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.switches);
                        }
                        break;
                     }
                  }
               }
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
                     }
                  }
               }

               if (valuesList.contains(unit))
               {
                  for (MyValue myValue : configData.values)
                  {
                     if (myValue.unit.equals(unit))
                     {
                        myValue.value = value;
                        for (int id : allWidgetIds)
                        {
                           appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.values);
                        }
                        break;
                     }
                  }
               }
            }
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
