package de.fehngarten.fhemswitch;

import java.io.File;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.pm.ActivityInfo;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.Socket;
import com.mobeta.android.dslv.DragSortListView;

import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;
import android.util.Log;

public class ConfigMain extends Activity
{
   Button configOkButton;
   Button configOkButton2;
   int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
   private EditText urlpl, urljs, connectionPW;
   public static ConfigData configData;
   private ConfigDataOnly configDataOnly;
   public static MySocket mySocket;

   public ConfigSwitchAdapter configSwitchAdapter;
   public ConfigLightsceneAdapter configLightsceneAdapter;
   public ConfigValueAdapter configValueAdapter;

   public int lsCounter = 0;
   public int lsSize = 0;
   public static Context mContext;
   public Handler waitAuth = new Handler();
   public Spinner spinnerSwitchCols;
   public Spinner spinnerValueCols;
   public RadioGroup radioLayoutLandscape ;
   public RadioGroup radioLayoutPortrait ;
   
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      
      mContext = this;
      setResult(RESULT_CANCELED);
      
      //int height = size.y;
      int screenWidth = getResources().getDisplayMetrics().widthPixels; 
      float density  = getResources().getDisplayMetrics().density;
      float dpWidth  = screenWidth / density;    
      
      if (dpWidth < 596)
      {
         this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      }
      setContentView(R.layout.config);

      urlpl = (EditText) findViewById(R.id.urlpl);
      urljs = (EditText) findViewById(R.id.urljs);
      connectionPW = (EditText) findViewById(R.id.connection_pw);

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
            Log.i("layouttype1",Integer.toString(configDataOnly.layoutPortrait) + " - " + configDataOnly.layoutLandscape);
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
      connectionPW.setText(configDataOnly.connectionPW, TextView.BufferType.EDITABLE);

      configOkButton = (Button) findViewById(R.id.okconfig);
      configOkButton.setOnClickListener(configOkButtonOnClickListener);
      configOkButton2 = (Button) findViewById(R.id.okconfig2);
      configOkButton2.setOnClickListener(configOkButtonOnClickListener);

      spinnerSwitchCols = (Spinner) this.findViewById(R.id.config_switch_cols);   
      ArrayAdapter<CharSequence> adapterSwitchCols = ArrayAdapter.createFromResource(this, R.array.colnum, R.layout.spinner_item);
      adapterSwitchCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
      spinnerSwitchCols.setAdapter(adapterSwitchCols);
      spinnerSwitchCols.setSelection(configDataOnly.switchCols);

      spinnerValueCols = (Spinner) this.findViewById(R.id.config_value_cols);   
      ArrayAdapter<CharSequence> adapterValueCols = ArrayAdapter.createFromResource(this, R.array.colnum, R.layout.spinner_item);
      adapterValueCols.setDropDownViewResource(R.layout.spinner_dropdown_item);
      spinnerValueCols.setAdapter(adapterValueCols);
      spinnerValueCols.setSelection(configDataOnly.valueCols);
      
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
            newLightScene = configData.lightScenes.newLightScene(lightsceneRow.name, lightsceneRow.unit, lightsceneRow.showHeader);
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
      radioLayoutLandscape = (RadioGroup) findViewById(R.id.layout_landscape);
      radioLayoutPortrait = (RadioGroup) findViewById(R.id.layout_portrait); 
      Log.i("layouttype2",configDataOnly.layoutPortrait + " - " + configDataOnly.layoutLandscape);
      
      ((RadioButton)radioLayoutLandscape.getChildAt(configDataOnly.layoutLandscape)).setChecked(true);
      ((RadioButton)radioLayoutPortrait.getChildAt(configDataOnly.layoutPortrait)).setChecked(true);
   }

   private Button.OnClickListener configOkButtonOnClickListener = new Button.OnClickListener()
   {
      @Override
      public void onClick(View arg0)
      {
         //Log.i("text button", configOkButton.getText().toString());
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

   private Runnable runnableWaitAuth = new Runnable()
   {
      @Override
      public void run()
      {
         ConfigMain.sendAlertMessage(getString(R.string.checkpw));
         mySocket.socket.off("authenticated");
         mySocket.socket.close();
         mySocket = null;
         
      }
   };

   private Emitter.Listener authListener = new Emitter.Listener() 
   {
      @Override
      public void call(Object... args)
      {
         runOnUiThread(new Runnable()
         {
            @Override
            public void run()
            {
               waitAuth.removeCallbacks(runnableWaitAuth);
               getAllSwitches(mySocket);
               getAllLightscenes(mySocket);
               getAllValues(mySocket);
               
               findViewById(R.id.layout_block).setVisibility(View.VISIBLE);
               findViewById(R.id.switches_header1).setVisibility(View.VISIBLE);
               findViewById(R.id.switches_header2).setVisibility(View.VISIBLE);
               findViewById(R.id.lightscenes_header1).setVisibility(View.VISIBLE);
               findViewById(R.id.lightscenes_header2).setVisibility(View.VISIBLE);
               findViewById(R.id.values_header1).setVisibility(View.VISIBLE);
               findViewById(R.id.values_header2).setVisibility(View.VISIBLE);
               findViewById(R.id.okconfig2).setVisibility(View.VISIBLE);

               configOkButton.setText(R.string.save);   
            }
         });
      }
   };     
     
   private void showFHEMunits()
   {
      try
      {
         URL url = new URL(urljs.getText().toString());
         url.toURI();
         try
         {           
            if (WidgetService.mySocket != null && WidgetService.mySocket.socket.connected())
            {
               WidgetService.mySocket.socket.close();
            }
            
            mySocket = new MySocket(urljs.getText().toString());
            
            mySocket.socket.on("authenticated", authListener);
            String pw = connectionPW.getText().toString();

            if (!pw.equals(""))
            {
               //Log.i("trace","send pw");
               mySocket.socket.emit("authentication", pw);
               waitAuth.postDelayed(runnableWaitAuth, 2000);
            }
         }
         catch (Exception e)
         {
            waitAuth.removeCallbacks(runnableWaitAuth);
            sendAlertMessage(getString(R.string.noconn) + ":\n- " + getString(R.string.urlcheck) + "!\n- " + getString(R.string.onlinecheck) + "?\n" + e);
         }
         
         mySocket.socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener()
         {
            @Override
            public void call(Object... args)
            {
               runOnUiThread(new Runnable()
               {
                  @Override
                  public void run()
                  {
                     waitAuth.removeCallbacks(runnableWaitAuth);
                     ConfigMain.sendAlertMessage(getString(R.string.noconn) + ":\n- " + getString(R.string.urlcheck) + ".\n- " + getString(R.string.onlinecheck) + "?");
                  }
               });
            }
         });
      }
      catch (MalformedURLException e)
      {
         sendAlertMessage(getString(R.string.urlerr) + ":\n " + e);
      }
      catch (URISyntaxException e)
      {
         sendAlertMessage(getString(R.string.urlerr) + ":\n " + e);
      }
   }
  
   public static void sendAlertMessage(final String msg)
   {
      AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
      dialog.setTitle(mContext.getString(R.string.error_header));
      //dialog.setIcon(R.drawable.error_icon);
      dialog.setMessage(msg);
      dialog.setNeutralButton(mContext.getString(R.string.ok), null);
      dialog.create().show();
   }

   private void getAllSwitches(MySocket mySocket)
   {
      DragSortListView l = (DragSortListView) findViewById(R.id.switches);
      configSwitchAdapter = new ConfigSwitchAdapter(this, R.layout.config_switch_row);
      l.setAdapter(configSwitchAdapter);
      l.setDropListener(onDropSwitch);
      l.setFloatViewManager(switchFloatViewManager);
      l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      l.setDragHandleId(R.id.config_switch_unit);

      // read switches from FHEM server
      mySocket.socket.emit("getAllSwitches", new Ack()
      {
         @Override
         public void call(Object... args)
         {
            //Log.i("get allSwitches", args[0].toString());
            configSwitchAdapter.initData((JSONArray) args[0], configData.switches, configData.switchesDisabled);
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
      final ArrayList<ConfigLightsceneRow> lightsceneRowsTemp = new ArrayList<ConfigLightsceneRow>();

      DragSortListView l = (DragSortListView) findViewById(R.id.lightscenes);
      configLightsceneAdapter = new ConfigLightsceneAdapter(this, R.layout.config_lightscene_row);
      l.setAdapter(configLightsceneAdapter);
      l.setDropListener(onDropLightscene);
      LightscenesSectionController c = new LightscenesSectionController(l, configLightsceneAdapter, this);
      l.setFloatViewManager(c);
      //l.setOnTouchListener(c);   

      mySocket.socket.emit("getAllUnitsOf", "LightScene", new Ack()
      {
         @Override
         public void call(Object... args)
         {
            ArrayList<String> lightscenesFHEM = convertJSONarray((JSONArray) args[0]);

            lsSize = lightscenesFHEM.size();
            for (int i = 0; i < lsSize; i++)
            {
               final String unit = lightscenesFHEM.get(i);

               mySocket.socket.emit("command", "get " + unit + " scenes", new Ack()
               {
                  @Override
                  public void call(Object... args)
                  {
                     //Log.i("get allLightscenes", args[0].toString());
                     lightsceneRowsTemp.add(new ConfigLightsceneRow(unit, unit, false, true, true));
                     ArrayList<String> lightscenesMember = convertJSONarray((JSONArray) args[0]);
                     for (String unit : lightscenesMember)
                     {
                        if (!unit.equals(""))
                        {   
                           lightsceneRowsTemp.add(new ConfigLightsceneRow(unit, unit, false, false, false));
                        }
                     }
                     lsCounter++;
                     if (lsCounter == lsSize)
                     {
                        configLightsceneAdapter.initData(configData, lightsceneRowsTemp);
                        runOnUiThread(new Runnable()
                        {
                           @Override
                           public void run()
                           {
                              configLightsceneAdapter.notifyDataSetChanged();
                              setListViewHeightBasedOnChildren((ListView) findViewById(R.id.lightscenes));
                           }
                        });
                     }
                  }
               });
            }
         }
      });
   }

   private void getAllValues(final MySocket mySocket)
   {
      DragSortListView l = (DragSortListView) findViewById(R.id.values);
      configValueAdapter = new ConfigValueAdapter(this, R.layout.config_value_row);
      l.setAdapter(configValueAdapter);
      l.setDropListener(onDropValue);
      l.setFloatViewManager(valueFloatViewManager);
      l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      l.setDragHandleId(R.id.config_value_unit);

      // read values from FHEM server
      mySocket.socket.emit("getAllValues", new Ack()
      {
         @Override
         public void call(Object... args)
         {
            configValueAdapter.initData((JSONObject) args[0], configData.values, configData.valuesDisabled);

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

      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

      //RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hellowidget_layout);
      //appWidgetManager.updateAppWidget(mAppWidgetId, views);
      //WidgetService.updateAppWidget(context, appWidgetManager, mAppWidgetId);

      configDataOnly = new ConfigDataOnly();
      configDataOnly.urljs = urljs.getText().toString();
      configDataOnly.urlpl = urlpl.getText().toString();
      configDataOnly.connectionPW = connectionPW.getText().toString();
      configDataOnly.switchRows = configSwitchAdapter.getData();
      configDataOnly.lightsceneRows = configLightsceneAdapter.getData();
      configDataOnly.valueRows = configValueAdapter.getData();
      configDataOnly.switchCols = spinnerSwitchCols.getSelectedItemPosition();
      configDataOnly.valueCols = spinnerValueCols.getSelectedItemPosition();
      
      RadioButton radioLayoutPortraitButton = (RadioButton) findViewById(radioLayoutPortrait.getCheckedRadioButtonId());
      RadioButton radioLayoutLandscapeButton = (RadioButton) findViewById(radioLayoutLandscape.getCheckedRadioButtonId());
      configDataOnly.layoutPortrait = Integer.valueOf(radioLayoutPortraitButton.getTag().toString());
      configDataOnly.layoutLandscape = Integer.valueOf(radioLayoutLandscapeButton.getTag().toString());

      try
      {
         String dir = getFilesDir().getAbsolutePath();
         File f0 = new File(dir, WidgetService.CONFIGFILE);
         f0.delete(); 
         FileOutputStream f_out = openFileOutput(WidgetService.CONFIGFILE, Context.MODE_PRIVATE);
         ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
         obj_out.writeObject(configDataOnly);
         obj_out.close();
         Log.i("config", "config.data written");
      }
      catch (Exception e)
      {
         sendAlertMessage(getString(R.string.fileerr) + ":\n " + e);
      }

      //WidgetService.startup();         

      Intent resultValue = new Intent();
      resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
      setResult(RESULT_OK, resultValue);
      ComponentName thisAppWidget = new ComponentName(mContext.getPackageName(), WidgetProvider.class.getName());
      Intent updateIntent = new Intent(mContext, WidgetProvider.class);
      int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
      updateIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
      updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
      mContext.sendBroadcast(updateIntent);
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

   private DragSortListView.FloatViewManager switchFloatViewManager = new DragSortListView.FloatViewManager()
   {
      @Override
      public View onCreateFloatView(int position)
      {
         DragSortListView l = (DragSortListView) findViewById(R.id.switches);
         View v = configSwitchAdapter.getView(position, null, l);
         v.setBackgroundColor(mContext.getResources().getColor(R.color.conf_bg_handle_pressed));
         return v;
      }

      @Override
      public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint)
      {

      }

      @Override
      public void onDestroyFloatView(View floatView)
      {
         //do nothing; block super from crashing
      }
   };
   private DragSortListView.FloatViewManager valueFloatViewManager = new DragSortListView.FloatViewManager()
   {
      @Override
      public View onCreateFloatView(int position)
      {
         DragSortListView l = (DragSortListView) findViewById(R.id.values);
         View v = configValueAdapter.getView(position, null, l);
         v.setBackgroundColor(mContext.getResources().getColor(R.color.conf_bg_handle_pressed));
         return v;
      }

      @Override
      public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint)
      {

      }

      @Override
      public void onDestroyFloatView(View floatView)
      {
         //do nothing; block super from crashing
      }
   };

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

   public static ArrayList<String> convertJSONarray(JSONArray jsonArray)
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
}
