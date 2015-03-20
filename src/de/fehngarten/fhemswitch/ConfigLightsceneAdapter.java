package de.fehngarten.fhemswitch;

import java.lang.reflect.Array;
import java.util.ArrayList;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigLightsceneAdapter extends BaseAdapter
{
   Context mContext;
   int layoutResourceId;
   ArrayList<ConfigLightsceneRow> data = null;

   public ConfigLightsceneAdapter(Context mContext, int layoutResourceId)
   {

      //super(mContext, layoutResourceId, data);
      this.layoutResourceId = layoutResourceId;
      this.mContext = mContext;
   }

   public int getCount()
   {
      return ConfigMain.lightsceneRows.size();
   }

   public ConfigLightsceneRow getItem(int position)
   {
      return ConfigMain.lightsceneRows.get(position);
   }

   public long getItemId(int position)
   {
      return (long) position;
   }

   public Boolean isDragable(int res)
   {
      Log.d("isDragable res", Integer.toString(res));
      if (getItem(res).isHeader)
      {
         return false;
      }
      else
      {
         return true;
      }
   }
 
   public int[] getBounds(int pos)
   {
      int[] bounds = new int[2];
      
      int startPos = 0;
      int endPos = 9999;
      
      for (int i = 1; i < getCount();i++)
      {
         if (getItem(i).isHeader)
         {
            if (i < pos)
            {
               startPos = i;
            }
            else
            {
               endPos = i;
               break;
            }
         }
      }
      
      bounds[0] = startPos;
      bounds[1] = endPos;
      return bounds;
   }
   
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View rowView = convertView;
      ConfigLightsceneRow lightsceneRow = getItem(position);
      final LightsceneHolder lightsceneHolder;

      if (rowView == null)
      {
         lightsceneHolder = new LightsceneHolder();
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         rowView = inflater.inflate(R.layout.config_lightscene_row, parent, false);
         rowView.setTag(lightsceneHolder);
         lightsceneHolder.lightscene_unit = (TextView) rowView.findViewById(R.id.config_lightscene_unit);
         lightsceneHolder.lightscene_name = (EditText) rowView.findViewById(R.id.config_lightscene_name);
         lightsceneHolder.lightscene_enabled = (CheckBox) rowView.findViewById(R.id.config_lightscene_enabled);
      }
      else
      {
         lightsceneHolder = (LightsceneHolder) rowView.getTag();
      }

      lightsceneHolder.ref = position;
      lightsceneHolder.lightscene_unit.setText(lightsceneRow.unit);
      lightsceneHolder.lightscene_name.setText(lightsceneRow.name);
      if (lightsceneRow.isHeader)
      {
         lightsceneHolder.lightscene_enabled.setVisibility(View.INVISIBLE);
         rowView.setBackgroundColor(0xFFBBBBBB);
        
      }
      else
      {
         lightsceneHolder.lightscene_enabled.setChecked(lightsceneRow.enabled);
      }

      //private method of your class     

      lightsceneHolder.lightscene_enabled.setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View arg0)
         {
            getItem(lightsceneHolder.ref).enabled = lightsceneHolder.lightscene_enabled.isChecked();
         }
      });

      lightsceneHolder.lightscene_name.addTextChangedListener(new TextWatcher()
      {
         @Override
         public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
         {
            // TODO Auto-generated method stub

         }

         @Override
         public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
         {
            // TODO Auto-generated method stub

         }

         @Override
         public void afterTextChanged(Editable arg0)
         {
            // TODO Auto-generated method stub
            getItem(lightsceneHolder.ref).name = arg0.toString();
         }
      });

      return rowView;
   }

   public void changeItems(int from, int to)
   {
      Log.i("change switch", Integer.toString(from) + " " + Integer.toString(to));
      final ArrayList<ConfigLightsceneRow> lightsceneRowsTemp = new ArrayList<ConfigLightsceneRow>();
      if (from > to)
      {
         for (int i = 0; i < ConfigMain.lightsceneRows.size(); i++)
         {
            if (i < to)
            {
               lightsceneRowsTemp.add(ConfigMain.lightsceneRows.get(i));
            }
            else if (i == to)
            {
               lightsceneRowsTemp.add(ConfigMain.lightsceneRows.get(from));
            }
            else if (i <= from)
            {
               lightsceneRowsTemp.add(ConfigMain.lightsceneRows.get(i - 1));
            }
            else
            {
               lightsceneRowsTemp.add(ConfigMain.lightsceneRows.get(i));
            }
         }
      }
      else if (from < to)
      {
         for (int i = 0; i < ConfigMain.lightsceneRows.size(); i++)
         {
            if (i < from)
            {
               lightsceneRowsTemp.add(ConfigMain.lightsceneRows.get(i));
            }
            else if (i < to)
            {
               lightsceneRowsTemp.add(ConfigMain.lightsceneRows.get(i + 1));
            }
            else if (i == to)
            {
               lightsceneRowsTemp.add(ConfigMain.lightsceneRows.get(from));
            }
            else
            {
               lightsceneRowsTemp.add(ConfigMain.lightsceneRows.get(i));
            }
         }
      }
      if (from != to)
      {
         ConfigMain.lightsceneRows = lightsceneRowsTemp;
         notifyDataSetChanged();
      }
   }
   
   private class LightsceneHolder
   {
      CheckBox lightscene_enabled;
      TextView lightscene_unit;
      EditText lightscene_name;
      int ref;
   }
}
