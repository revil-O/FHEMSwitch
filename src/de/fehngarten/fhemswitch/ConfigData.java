package de.fehngarten.fhemswitch;

import java.util.ArrayList;
import java.util.List;

import de.fehngarten.fhemswitch.MyLightScenes.MyLightScene;

public class ConfigData 
{
   public List<MySwitch> switches = new ArrayList<MySwitch>();
   public List<MySwitch> switchesDisabled = new ArrayList<MySwitch>();
   public List<ArrayList<MySwitch>> switchesCols = new ArrayList<ArrayList<MySwitch>>();
   public MyLightScenes lightScenes = new MyLightScenes();
   public List<MyValue> values = new ArrayList<MyValue>();
   public List<ArrayList<MyValue>> valuesCols= new ArrayList<ArrayList<MyValue>>();
   public List<MyValue> valuesDisabled = new ArrayList<MyValue>();

   public MySwitch isInSwitches(String unit)
   {
      for (MySwitch mySwitch : switches)
      {
         if (mySwitch.unit.equals(unit)) { return mySwitch; }
      }
      return null; 
   }

   public MySwitch isInSwitchesDisabled(String unit)
   {
      for (MySwitch mySwitch : switchesDisabled)
      {
         if (mySwitch.unit.equals(unit)) { return mySwitch; }
      }
      return null;
   }

   public MyValue isInValues(String unit)
   {
      for (MyValue myValue : values)
      {
         if (myValue.unit.equals(unit)) { return myValue; }
      }
      return null; 
   }
   
   public MyValue isInValuesDisabled(String unit)
   {
      for (MyValue myValue : valuesDisabled)
      {
         if (myValue.unit.equals(unit)) { return myValue; }
      }
      return null;
   }

   public ArrayList<String> getSwitchesList()
   {
      ArrayList<String> switchesList = new ArrayList<String>();
      for (MySwitch mySwitch : switches)
      {
         switchesList.add(mySwitch.unit);
      }
      return switchesList;
   }
 
   public ArrayList<String> getValuesList()
   {
      ArrayList<String> valuesList = new ArrayList<String>();
      for (MyValue myValue : values)
      {
         valuesList.add(myValue.unit);
      }
      return valuesList;
   }

   public ArrayList<String> getLightScenesList()
   {
      ArrayList<String> lightScenesList = new ArrayList<String>();
      for (MyLightScene myLightScene : lightScenes.lightScenes)
      {
         if (myLightScene.enabled)
         {
            lightScenesList.add(myLightScene.unit);
         }
      }
      return lightScenesList;
   }

   public MyLightScene isInLightScenes(String unit)
   {
      for (MyLightScene myLightScene : lightScenes.lightScenes)
      {
         if (myLightScene.unit.equals(unit)) { return myLightScene; }
      }
      return null;
   }

}
