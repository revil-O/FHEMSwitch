package de.fehngarten.fhemswitch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
//import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

class LightScenesFactory implements RemoteViewsService.RemoteViewsFactory
{
   //private static final String CLASSNAME = "LightScenesFactory.";
   private Context mContext = null;

   public LightScenesFactory(Context context, Intent intent)
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
      //Log.d(CLASSNAME + methodname, "lightscenes size: " + Integer.toString(WidgetService.configData.lightScenes.itemsCount));
      return WidgetService.configData.lightScenes.itemsCount;
   }

   @Override
   public RemoteViews getViewAt(int position)
   {
      RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.lightscene_row);
      //Log.i("LightScene Position: " + position + " of " + lightScenes.items.size(),lightScenes.items.get(position).name);

      if (WidgetService.configData.lightScenes.items.get(position).header)
      {
         SpannableString s = new SpannableString(WidgetService.configData.lightScenes.items.get(position).name);
         s.setSpan(new StyleSpan(Typeface.BOLD), 0,s.length(),0); 
         mView.setTextViewText(R.id.lightscene_name, s);
         mView.setTextColor(R.id.lightscene_name, 0xFFCCCCCC);
         mView.setFloat(R.id.lightscene_name,"setTextSize",20);
         if (position == 0)
         {
            mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.header);
         }
         else
         {
            mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.header2);
         }

         final Intent fillInIntent = new Intent();
         fillInIntent.setAction(WidgetProvider.OPEN_URL);
         final Bundle bundle = new Bundle();
         bundle.putString(WidgetProvider.URL, "https://home.wernerschaeffer.de:8082/fhem?detail=" + WidgetService.configData.lightScenes.items.get(position).unit);
         fillInIntent.putExtras(bundle);
         mView.setOnClickFillInIntent(R.id.lightscene_name, fillInIntent);
      }
      else
      {
         mView.setTextViewText(R.id.lightscene_name, WidgetService.configData.lightScenes.items.get(position).name);
         mView.setTextColor(R.id.lightscene_name, 0xFF000088);
         mView.setFloat(R.id.lightscene_name,"setTextSize",16);
         if (WidgetService.configData.lightScenes.items.get(position).activ)
         {
            if (position == WidgetService.configData.lightScenes.itemsCount - 1)
            {
               mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.activelast);
            }
            else
            {
               mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.active);
            }
         }
         else
         {
            if (position == WidgetService.configData.lightScenes.itemsCount - 1)
            {
               mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactivelast);
            }
            else
            {
               mView.setInt(R.id.lightscene_name, "setBackgroundResource", R.drawable.inactive);
            }
            final Intent fillInIntent = new Intent();
            fillInIntent.setAction(WidgetProvider.SEND_FHEM_COMMAND);
            final Bundle bundle = new Bundle();
            bundle.putString(WidgetProvider.COMMAND, WidgetService.configData.lightScenes.activateCmd(position));
            fillInIntent.putExtras(bundle);
            mView.setOnClickFillInIntent(R.id.lightscene_name, fillInIntent);
         }
      }
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
