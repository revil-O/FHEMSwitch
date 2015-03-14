package de.fehngarten.fhemswitch;

import java.util.ArrayList;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
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

   private class LightsceneHolder
   {
      CheckBox lightscene_enabled;
      TextView lightscene_unit;
      EditText lightscene_name;
      int ref;
   }
}
