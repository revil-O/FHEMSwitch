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
import de.fehngarten.fhemswitch.R;

class ConfigValueAdapter extends BaseAdapter
{
   Context mContext;
   int layoutResourceId;
   ArrayList<ConfigValueRow> data = null;

   public ConfigValueAdapter(Context mContext, int layoutResourceId)
   {

      //super(mContext, layoutResourceId, data);
      this.layoutResourceId = layoutResourceId;
      this.mContext = mContext;
   }

   public int getCount()
   {
      return ConfigMain.valueRows.size();
   }

   public ConfigValueRow getItem(int position)
   {
      return ConfigMain.valueRows.get(position);
   }

   public long getItemId(int position)
   {
      return (long) position;
   }

   public View getView(int position, View convertView, ViewGroup parent)
   {
      View rowView = convertView;
      ConfigValueRow valueRow = getItem(position);
      final ValueHolder valueHolder;

      if (rowView == null)
      {
         valueHolder = new ValueHolder();
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         rowView = inflater.inflate(R.layout.config_value_row, parent, false);
         rowView.setTag(valueHolder);
         valueHolder.value_unit = (TextView) rowView.findViewById(R.id.config_value_unit);
         valueHolder.value_name = (EditText) rowView.findViewById(R.id.config_value_name);
         valueHolder.value_value = (TextView) rowView.findViewById(R.id.config_value_value);
         valueHolder.value_enabled = (CheckBox) rowView.findViewById(R.id.config_value_enabled);
      }
      else
      {
         valueHolder = (ValueHolder) rowView.getTag();
      }

      valueHolder.ref = position;
      valueHolder.value_unit.setText(valueRow.unit);
      valueHolder.value_name.setText(valueRow.name);
      valueHolder.value_value.setText(valueRow.value);
      valueHolder.value_enabled.setChecked(valueRow.enabled);

      //private method of your class     

      valueHolder.value_enabled.setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View arg0)
         {
            getItem(valueHolder.ref).enabled = valueHolder.value_enabled.isChecked();
         }
      });

      valueHolder.value_name.addTextChangedListener(new TextWatcher()
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
            getItem(valueHolder.ref).name = arg0.toString();
         }
      });

      return rowView;
   }

   private class ValueHolder
   {
      CheckBox value_enabled;
      TextView value_unit;
      EditText value_name;
      TextView value_value;
      int ref;
   }
}
