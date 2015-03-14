package de.fehngarten.fhemswitch;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.util.Log;
import android.net.Uri;

public class WidgetProvider extends AppWidgetProvider
{

   //private static final String LOG = "WetterstationWidgetProvider";
   public static final String SEND_FHEM_COMMAND = "de.fehngarten.fhemswitch.SEND_FHEM_COMMAND";
   public static final String COMMAND = "de.fehngarten.fhemswitch.COMMAND";
   public static final String URL = "de.fehngarten.fhemswitch.URL";
   public static final String OPEN_URL = "de.fehngarten.fhemswitch.OPEN_URL";
   private static Boolean startService = true;

   public void onReceive(Context context, Intent intent)
   {
      //Log.i("trace", "onReiceive startet by " + intent.getAction());
      // super.onReceive(context, intent);
      // make sure the user has actually installed a widget
      // before starting the update service
      if (widgetsInstalledLength(context) != 0
      && (intent.getAction().equals(Intent.ACTION_USER_PRESENT) || intent.getAction().equals(
      "android.appwidget.action.APPWIDGET_UPDATE")))
      {
         if (startService)
         {
            Intent serviceIntent = new Intent(context.getApplicationContext(), WidgetService.class);
            context.startService(serviceIntent);
         }
         else 
         {
            startService = true;
         }
      }
      else if (intent.getAction().equals("android.appwidget.action.APPWIDGET_DELETED"))
      {
         //Log.i("trace", "stop service");
         Intent serviceIntent = new Intent(context.getApplicationContext(), WidgetService.class);
         context.stopService(serviceIntent);
      }
      else if (intent.getAction().equals(SEND_FHEM_COMMAND))
      {
         //Log.i("trace", "switch pressed");
         String cmd = intent.getExtras().getString(COMMAND);
         WidgetService.sendCommand(cmd);
      }
      else if (intent.getAction().equals(OPEN_URL))
      {
         //Log.i("trace", "switch pressed");
         String urlString = intent.getExtras().getString(URL);

         Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlString));
         webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         context.startActivity(webIntent);
      }
      super.onReceive(context, intent);
   }

   @Override
   public void onDisabled(Context context)
   {
      //Log.i("trace", "onDisabled startet");
      Intent intent = new Intent(context, WidgetService.class);
      context.stopService(intent);
      intent = new Intent(context, WidgetService.class);
      context.stopService(intent);
      super.onDisabled(context);
   }

   @Override
   public void onEnabled(Context context)
   {
      //Log.i("trace", "onEnabled startet");
      startService = false;
      super.onEnabled(context);
   }

   // convenience method to count the number of installed widgets
   private int widgetsInstalledLength(Context context)
   {
      ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
      AppWidgetManager mgr = AppWidgetManager.getInstance(context);
      return mgr.getAppWidgetIds(thisWidget).length;
   }

}
