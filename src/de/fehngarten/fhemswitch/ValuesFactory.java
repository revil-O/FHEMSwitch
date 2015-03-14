package de.fehngarten.fhemswitch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
 
class ValuesFactory implements RemoteViewsFactory
{
   //private static final String CLASSNAME = "ValuesFactory.";
   private Context mContext = null;

   public ValuesFactory(Context context, Intent intent)
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
      //String methodname = "onDataSetChanged";
      //Log.d(CLASSNAME + methodname, "started");
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
      //Log.d(CLASSNAME + methodname, "values size: " + Integer.toString(values.size()));
      return WidgetService.configData.values.size();
   }

   @Override
   public RemoteViews getViewAt(int position)
   {
      //Log.i("values Position: " + position + " of " + values.size(),values.get(position).name);
      RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.value_row);
      mView.setTextViewText(R.id.value_name, WidgetService.configData.values.get(position).name);
      mView.setTextViewText(R.id.value_value, WidgetService.configData.values.get(position).value);
      
      if (position == 0)
      {
         mView.setInt(R.id.value_row, "setBackgroundResource", R.drawable.valuefirst);
      }
      else if (position == WidgetService.configData.values.size() - 1)
      {
         mView.setInt(R.id.value_row, "setBackgroundResource", R.drawable.valuelast);
      }
      else
      {
         mView.setInt(R.id.value_row, "setBackgroundResource", R.drawable.value);
      }

      final Intent fillInIntent = new Intent();
      fillInIntent.setAction(WidgetProvider.OPEN_URL);
      final Bundle bundle = new Bundle();
      bundle.putString(WidgetProvider.URL, WidgetService.fhemUrl + "?detail=" + WidgetService.configData.values.get(position).unit);
      fillInIntent.putExtras(bundle);
      mView.setOnClickFillInIntent(R.id.value_name, fillInIntent);
      
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
