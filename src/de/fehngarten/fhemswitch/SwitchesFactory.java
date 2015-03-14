package de.fehngarten.fhemswitch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log; 
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

class SwitchesFactory implements RemoteViewsFactory
{
   private static final String CLASSNAME = "SwitchesFactory.";
   private Context mContext = null;
   //private List<MySwitch> switches = new ArrayList<MySwitch>();

   public SwitchesFactory(Context context, Intent intent)
   {
      //Log.d(CLASSNAME, "started");
      mContext = context;
   }

   public void initData()
   {
      //String methodname = "initData";
      //Log.d(CLASSNAME + methodname, "started");
   }

   @Override
   public void onCreate()
   {
      //String methodname = "onCreate";
      //Log.d(CLASSNAME + methodname, "started");    
      //initData(); 
   }

   @Override
   public void onDataSetChanged()
   {
      String methodname = "onDataSetChanged";
      Log.d(CLASSNAME + methodname, "started");
      //initData();
   }

   @Override
   public void onDestroy()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public int getCount()
   {
      //String methodname = "getCount";
      //Log.d(CLASSNAME + methodname, "switches size: " + Integer.toString(switches.size()));
      return WidgetService.configData.switches.size();
   }

   @Override
   public RemoteViews getViewAt(int position)
   {
      Log.i("switches Position: " + position + " of " + WidgetService.configData.switches.size(),WidgetService.configData.switches.get(position).name);
      RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.switch_row);
      mView.setTextViewText(R.id.switch_name, WidgetService.configData.switches.get(position).name);
      mView.setImageViewResource(R.id.switch_icon, WidgetService.icons.get(WidgetService.configData.switches.get(position).icon));

      final Intent fillInIntent = new Intent();
      fillInIntent.setAction(WidgetProvider.SEND_FHEM_COMMAND);
      final Bundle bundle = new Bundle();
      bundle.putString(WidgetProvider.COMMAND, WidgetService.configData.switches.get(position).activateCmd());
      fillInIntent.putExtras(bundle);
      mView.setOnClickFillInIntent(R.id.switch_row, fillInIntent);
 
      return mView;
   }

   @Override
   public RemoteViews getLoadingView()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public int getViewTypeCount()
   {
      // TODO Auto-generated method stub
      return 1;
   }

   @Override
   public long getItemId(int position)
   {
      // TODO Auto-generated method stub
      return position;
   }

   @Override
   public boolean hasStableIds()
   {
      // TODO Auto-generated method stub
      return false;
   }
}
