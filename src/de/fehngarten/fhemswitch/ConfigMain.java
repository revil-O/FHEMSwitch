package de.fehngarten.fhemswitch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Ack;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;
import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene.Member;

public class ConfigMain extends Activity
{
   Button configOkButton;
   Button configOkButton2;
   int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
   private EditText urlpl, urljs;
   private ConfigData configData;
   private ConfigDataOnly configDataOnly;
   private MySocket mySocket;
   private Boolean isLast = false;

   public static ArrayList<ConfigSwitchRow> switchRows;
   public static ArrayList<ConfigLightsceneRow> lightsceneRows;
   public static ArrayList<ConfigValueRow> valueRows;

   public ConfigSwitchAdapter configSwitchAdapter;
   public ConfigLightsceneAdapter configLightsceneAdapter;
   public ConfigValueAdapter configValueAdapter;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      // TODO Auto-generated method stub
      super.onCreate(savedInstanceState);

      setResult(RESULT_CANCELED);

      setContentView(R.layout.config);

      urlpl = (EditText) findViewById(R.id.urlpl);
      urljs = (EditText) findViewById(R.id.urljs);

      try
      {
         FileInputStream f_in = openFileInput(WidgetService.CONFIGFILE);
         ObjectInputStream obj_in = new ObjectInputStream(f_in);

         Object obj = obj_in.readObject();
         obj_in.close();

         Log.i("config", "config.data found");
         if (obj instanceof ConfigDataOnly)
         {
            configDataOnly = (ConfigDataOnly) obj;
         }
      }
      catch (FileNotFoundException e)
      {
         Log.i("config", "config.data not found");
         configDataOnly = new ConfigDataOnly();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      // Read object using ObjectInputStream

      urljs.setText(configDataOnly.urljs, TextView.BufferType.EDITABLE);
      urlpl.setText(configDataOnly.urlpl, TextView.BufferType.EDITABLE);

      configOkButton = (Button) findViewById(R.id.okconfig);
      configOkButton.setOnClickListener(configOkButtonOnClickListener);
      configOkButton2 = (Button) findViewById(R.id.okconfig2);
      configOkButton2.setOnClickListener(configOkButtonOnClickListener);

      configData = new ConfigData();

      for (ConfigSwitchRow switchRow : configDataOnly.switchRows)
      {
         if (switchRow.enabled)
         {
            configData.switches.add(new MySwitch(switchRow.name, switchRow.unit, switchRow.cmd));
         }
         else
         {
            configData.switchesDisabled.add(new MySwitch(switchRow.name, switchRow.unit, switchRow.cmd));
         }
      }

      MyLightScene newLightScene = null;
      for (ConfigLightsceneRow lightsceneRow : configDataOnly.lightsceneRows)
      {
         //Log.i("lightscene row",lightsceneRow.isHeader.toString());
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
         else
         {
            configData.valuesDisabled.add(new MyValue(valueRow.name, valueRow.unit));
         }
      }

      Intent intent = getIntent();
      Bundle extras = intent.getExtras();
      if (extras != null)
      {
         mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
      }

      // If they gave us an intent without the widget id, just bail.
      if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
      {
         //finish();
      }
   }

   private Button.OnClickListener configOkButtonOnClickListener = new Button.OnClickListener()
   {
      @Override
      public void onClick(View arg0)
      {
         // TODO Auto-generated method stub
         Log.i("text button", configOkButton.getText().toString());
         if (configOkButton.getText().toString().equals(getText(R.string.getConfig)))
         {
            showFHEMunits();
         }
         else
         {
            saveConfig();
         }
      }
   };

   private void showFHEMunits()
   {
      try
      {
         URL url = new URL(urljs.getText().toString());
         url.toURI();
         mySocket = new MySocket(urljs.getText().toString());
         getAllSwitches(mySocket);
         getAllLightscenes(mySocket);
         getAllValues(mySocket);

         findViewById(R.id.switches_header1).setVisibility(View.VISIBLE);
         findViewById(R.id.switches_header2).setVisibility(View.VISIBLE);
         findViewById(R.id.lightscenes_header1).setVisibility(View.VISIBLE);
         findViewById(R.id.lightscenes_header2).setVisibility(View.VISIBLE);
         findViewById(R.id.values_header1).setVisibility(View.VISIBLE);
         findViewById(R.id.values_header2).setVisibility(View.VISIBLE);
         findViewById(R.id.okconfig2).setVisibility(View.VISIBLE);

         configOkButton.setText(R.string.save);
      }
      catch (MalformedURLException e)
      {
         sendAlertMessage("Die angegbene URL ist ungültig:\n " + e);
      }
      catch (URISyntaxException e)
      {
         sendAlertMessage("Die angegbene URL ist ungültig:\n " + e);
      }
   }

   private void sendAlertMessage(final String msg)
   {
      AlertDialog.Builder dialog = new AlertDialog.Builder(this);
      dialog.setTitle(getString(R.string.error_header));
      //dialog.setIcon(R.drawable.error_icon);
      dialog.setMessage(msg);
      dialog.setNeutralButton(getString(R.string.ok), null);

      dialog.create().show();

   }

   private void getAllSwitches(MySocket mySocket)
   {
      switchRows = new ArrayList<ConfigSwitchRow>();
      DragSortListView l = (DragSortListView) findViewById(R.id.switches);
      configSwitchAdapter = new ConfigSwitchAdapter(this, R.layout.config_switch_row);
      l.setAdapter(configSwitchAdapter);
      l.setDropListener(onDropSwitch);
      l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      l.setDragHandleId(R.id.config_switch_unit);

      mySocket.socket.emit("getAllSwitches", new Ack()
      {
         @Override
         public void call(Object... args)
         {
            //Log.i("get allSwitches", args[0].toString());

            JSONArray JSONswitches = (JSONArray) args[0];

            ArrayList<String> switchesFHEM = convertJSONarray(JSONswitches);
            ArrayList<String> switchesConfig = new ArrayList<String>();

            for (MySwitch mySwitch : configData.switches)
            {
               if (switchesFHEM.contains(mySwitch.unit))
               {
                  switchRows.add(new ConfigSwitchRow(mySwitch.unit, mySwitch.name, true, mySwitch.cmd));
                  switchesConfig.add(mySwitch.unit);
               }
            }
            for (MySwitch mySwitch : configData.switchesDisabled)
            {
               if (switchesFHEM.contains(mySwitch.unit))
               {
                  switchRows.add(new ConfigSwitchRow(mySwitch.unit, mySwitch.name, false, mySwitch.cmd));
                  switchesConfig.add(mySwitch.unit);
               }
            }
            for (String unit : switchesFHEM)
            {
               if (!switchesConfig.contains(unit))
               {
                  switchRows.add(new ConfigSwitchRow(unit, unit, false, "toggle"));
               }
            }

            runOnUiThread(new Runnable()
            {
               @Override
               public void run()
               {
                  configSwitchAdapter.notifyDataSetChanged();
                  setListViewHeightBasedOnChildren((ListView) findViewById(R.id.switches));
               }
            });
         }
      });
   }

   private void getAllLightscenes(final MySocket mySocket)
   {
      lightsceneRows = new ArrayList<ConfigLightsceneRow>();
      final ArrayList<ConfigLightsceneRow> lightsceneRowsTemp = new ArrayList<ConfigLightsceneRow>();
      DragSortListView l = (DragSortListView) findViewById(R.id.lightscenes);
      configLightsceneAdapter = new ConfigLightsceneAdapter(this, R.layout.config_lightscene_row);
      l.setAdapter(configLightsceneAdapter);
      l.setDropListener(onDropLightscene);

      // make and set controller on dslv
      LightscenesSectionController c = new LightscenesSectionController(l, configLightsceneAdapter);
      l.setFloatViewManager(c);
      l.setOnTouchListener(c);

      mySocket.socket.emit("getAllUnitsOf", "LightScene", new Ack()
      {
         @Override
         public void call(Object... args)
         {
            //JSONObject obj = (JSONObject) args[0];
            JSONArray JSONlightscenes = (JSONArray) args[0];
            try
            {
               for (int i = 0, size = JSONlightscenes.length(); i < size; i++)
               {
                  String unit = JSONlightscenes.getString(i);

                  if (i == size - 1)
                  {
                     isLast = true;
                  }
                  else
                  {
                     isLast = false;
                  }
                  final MyLightScene myLightScene = configData.isInLightScenes(unit);
                  String name;
                  if (myLightScene != null)
                  {
                     name = myLightScene.name;
                  }
                  else
                  {
                     name = unit;
                  }
                  final String SZname = name;
                  final String SZunit = unit;
                  mySocket.socket.emit("command", "get " + unit + " scenes", new Ack()
                  {
                     @Override
                     public void call(Object... args)
                     {
                        lightsceneRowsTemp.add(new ConfigLightsceneRow(SZunit, SZname, false, true));
                        //Log.i("get allLightscenes", args[0].toString());
                        JSONArray JSONresponse = (JSONArray) args[0];
                        for (int i = 0, size = JSONresponse.length(); i < size; i++)
                        {
                           try
                           {
                              String name;
                              Boolean enabled;
                              String unit = JSONresponse.getString(i);
                              if (unit.equals(""))
                                 continue;

                              if (myLightScene != null)
                              {
                                 Member member = myLightScene.isMember(unit);
                                 if (member == null)
                                 {
                                    name = unit;
                                    enabled = false;
                                 }
                                 else
                                 {
                                    name = member.name;
                                    enabled = member.enabled;
                                 }
                              }
                              else
                              {
                                 name = unit;
                                 enabled = false;
                              }
                              lightsceneRowsTemp.add(new ConfigLightsceneRow(unit, name, enabled, false));
                           }
                           catch (JSONException e)
                           {
                              e.printStackTrace();
                           }
                        }
                        if (isLast)
                        {
                           runOnUiThread(new Runnable()
                           {
                              @Override
                              public void run()
                              {
                                 lightsceneRows = lightsceneRowsTemp;
                                 configLightsceneAdapter.notifyDataSetChanged();
                                 setListViewHeightBasedOnChildren((ListView) findViewById(R.id.lightscenes));
                              }
                           });
                        }
                     }
                  });
               }
            }
            catch (JSONException e)
            {
               e.printStackTrace();
            }

         }
      });
   }

   private void getAllValues(final MySocket mySocket)
   {
      valueRows = new ArrayList<ConfigValueRow>();

      DragSortListView l = (DragSortListView) findViewById(R.id.values);
      configValueAdapter = new ConfigValueAdapter(this, R.layout.config_value_row);
      l.setAdapter(configValueAdapter);
      l.setDropListener(onDropValue);
      l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      l.setDragHandleId(R.id.config_value_unit);

      mySocket.socket.emit("getAllValues", new Ack()
      {
         @Override
         public void call(Object... args)
         {
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

                  String name;
                  Boolean enabled = false;
                  MyValue myValue = configData.isInValues(unit);
                  if (myValue != null)
                  {
                     name = myValue.name;
                     enabled = true;
                  }
                  else
                  {
                     myValue = configData.isInValuesDisabled(unit);
                     if (myValue != null)
                     {
                        name = myValue.name;
                        enabled = false;
                     }
                     else
                     {
                        name = unit;
                        enabled = false;
                     }
                  }
                  valueRows.add(new ConfigValueRow(unit, name, value, enabled));

               }
               catch (JSONException e)
               {
                  e.printStackTrace();
               }
            }
            runOnUiThread(new Runnable()
            {
               @Override
               public void run()
               {
                  configValueAdapter.notifyDataSetChanged();
                  setListViewHeightBasedOnChildren((ListView) findViewById(R.id.values));
               }
            });

         }
      });
   }

   private void saveConfig()
   {
      mySocket.socket.disconnect();
      mySocket.socket.close();

      Context context = ConfigMain.this;
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

      //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hellowidget_layout);
      //appWidgetManager.updateAppWidget(mAppWidgetId, views);
      //WidgetService.updateAppWidget(context, appWidgetManager, mAppWidgetId);

      configDataOnly = new ConfigDataOnly();
      configDataOnly.urljs = urljs.getText().toString();
      configDataOnly.urlpl = urlpl.getText().toString();
      configDataOnly.switchRows = switchRows;
      configDataOnly.lightsceneRows = lightsceneRows;
      configDataOnly.valueRows = valueRows;

      try
      {
         FileOutputStream f_out = openFileOutput(WidgetService.CONFIGFILE, Context.MODE_PRIVATE);
         ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
         obj_out.writeObject(configDataOnly);
         obj_out.close();
         Log.i("config", "config.data written");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      //WidgetService.startup();         

      Intent resultValue = new Intent();
      resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
      setResult(RESULT_OK, resultValue);
      Log.i("ersteZeile Name", switchRows.get(0).name.toString());
      Log.i("6. Zeile Name", switchRows.get(5).name.toString());
      ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
      Intent updateIntent = new Intent(context, WidgetProvider.class);
      int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
      updateIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
      updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
      context.sendBroadcast(updateIntent);
      finish();
   }

   public void setListViewHeightBasedOnChildren(ListView listView)
   {
      ListAdapter listAdapter = listView.getAdapter();
      if (listAdapter == null) { return; }

      int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
      for (int i = 0; i < listAdapter.getCount(); i++)
      {
         View listItem = listAdapter.getView(i, null, listView);
         if (listItem instanceof ViewGroup)
            listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
         listItem.measure(0, 0);
         totalHeight += listItem.getMeasuredHeight();
      }

      ViewGroup.LayoutParams params = listView.getLayoutParams();
      params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
      listView.setLayoutParams(params);
   }

   private ArrayList<String> convertJSONarray(JSONArray jsonArray)
   {
      ArrayList<String> arrayList = new ArrayList<String>();
      for (int i = 0, size = jsonArray.length(); i < size; i++)
      {
         try
         {
            arrayList.add(jsonArray.getString(i));
         }
         catch (JSONException e)
         {
            e.printStackTrace();
         }
      }
      return arrayList;
   }

   private DragSortListView.DropListener onDropSwitch = new DragSortListView.DropListener()
   {
      @Override
      public void drop(int from, int to)
      {
         configSwitchAdapter.changeItems(from, to);
      }
   };

   private DragSortListView.DropListener onDropLightscene = new DragSortListView.DropListener()
   {
      @Override
      public void drop(int from, int to)
      {
         configLightsceneAdapter.changeItems(from, to);
      }
   };

   private DragSortListView.DropListener onDropValue = new DragSortListView.DropListener()
   {
      @Override
      public void drop(int from, int to)
      {
         configValueAdapter.changeItems(from, to);
      }
   };



}