package de.fehngarten.fhemswitch;

import java.util.ArrayList;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import de.fehngarten.fhemswitch.R;

class ConfigSwitchAdapter extends BaseAdapter
{
   Context mContext;
   int layoutResourceId;
   ArrayList<ConfigSwitchRow> data = null;

   public ConfigSwitchAdapter(Context mContext, int layoutResourceId)
   {

      //super(mContext, layoutResourceId, data);
      this.layoutResourceId = layoutResourceId;
      this.mContext = mContext;
   }

   public int getCount()
   {
      return ConfigMain.switchRows.size();
   }

   public ConfigSwitchRow getItem(int position)
   {
      return ConfigMain.switchRows.get(position);
   }

   public long getItemId(int position)
   {
      return (long) position;
   }

   public View getView(int position, View convertView, ViewGroup parent)
   {
      View rowView = convertView;
      ConfigSwitchRow switchRow = getItem(position);
      final SwitchHolder switchHolder;

      if (rowView == null)
      {
         switchHolder = new SwitchHolder();
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         rowView = inflater.inflate(R.layout.config_switch_row, parent, false);
         rowView.setTag(switchHolder);
         switchHolder.switch_unit = (TextView) rowView.findViewById(R.id.config_switch_unit);
         switchHolder.switch_name = (EditText) rowView.findViewById(R.id.config_switch_name);
         switchHolder.switch_enabled = (CheckBox) rowView.findViewById(R.id.config_switch_enabled);
         switchHolder.switch_cmd = (Spinner) rowView.findViewById(R.id.config_switch_cmd);
      }
      else
      {
         switchHolder = (SwitchHolder) rowView.getTag();
      }

      switchHolder.ref = position;
      switchHolder.switch_unit.setText(switchRow.unit);
      switchHolder.switch_name.setText(switchRow.name);
      switchHolder.switch_enabled.setChecked(switchRow.enabled);

      switchHolder.switch_cmd.setSelection(getSpinnerIndex(switchHolder.switch_cmd, switchRow.cmd));

      //private method of your class     

      switchHolder.switch_enabled.setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View arg0)
         {
            getItem(switchHolder.ref).enabled = switchHolder.switch_enabled.isChecked();
         }
      });

      switchHolder.switch_name.addTextChangedListener(new TextWatcher()
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
            getItem(switchHolder.ref).name = arg0.toString();
         }
      });

      switchHolder.switch_cmd.setOnItemSelectedListener(new OnItemSelectedListener()
      {
         @Override
         public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
         {
            getItem(switchHolder.ref).cmd = parentView.getItemAtPosition(position).toString();
         }

         @Override
         public void onNothingSelected(AdapterView<?> parentView)
         {
            // your code here
         }

      });

      return rowView;
   }

   private int getSpinnerIndex(Spinner spinner, String myString)
   {
      int index = 0;

      for (int i = 0; i < spinner.getCount(); i++)
      {
         if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString))
         {
            index = i;
            break;
         }
      }
      return index;
   }

   private class SwitchHolder
   {
      CheckBox switch_enabled;
      TextView switch_unit;
      EditText switch_name;
      Spinner switch_cmd;
      int ref;
   }
}
