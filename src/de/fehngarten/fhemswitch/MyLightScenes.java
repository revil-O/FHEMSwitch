package de.fehngarten.fhemswitch;

import java.util.ArrayList;

public class MyLightScenes 
{
   public ArrayList<MyLightScene> lightScenes = null;
   public ArrayList<Item> items = new ArrayList<Item>();
   public int itemsCount = 0;

   public MyLightScenes()
   {
      lightScenes = new ArrayList<MyLightScene>();
      items = new ArrayList<Item>();
      itemsCount = 0;
   }

   public MyLightScene newLightScene(String name, String unit)
   {
      MyLightScene myLightScene = new MyLightScene(name, unit, false);
      lightScenes.add(myLightScene);
      return myLightScene;
   }

   public void aggregate()
   {
      items = new ArrayList<Item>();
      itemsCount = 0;
      for (MyLightScene lightScene : lightScenes)
      {
         if (lightScene.enabled)
         {
            String lightSceneUnit = lightScene.unit;
            items.add(new Item(lightSceneUnit, lightSceneUnit, lightScene.name, true, false));
            itemsCount++;
            for (MyLightScene.Member member : lightScene.members)
            {
               if (member.enabled)
               {
                  lightScene.enabled = true;
                  items.add(new Item(lightSceneUnit, member.name, member.unit, false, false));
                  itemsCount++;
               }
            }
         }
      }
   }

   public ArrayList<String> unitsList()
   {
      ArrayList<String> unitsList = new ArrayList<String>();
      for (MyLightScene myLightScene : lightScenes)
      {
         unitsList.add(myLightScene.unit);
      }
      return unitsList;   
   }
   
   class Item
   {
      String lightSceneName;
      String name;
      String unit;
      Boolean header;
      Boolean activ;

      public Item(String lightSceneName, String name, String unit, Boolean header, Boolean activ)
      {
         this.lightSceneName = lightSceneName;
         this.name = name;
         this.unit = unit;
         this.header = header;
         this.activ = activ;
      }
   }

   public String activateCmd(int pos)
   {
      String cmd = "set " + items.get(pos).lightSceneName + " scene " + items.get(pos).unit;
      return cmd;
   }

   class MyLightScene
   {
      public String name;
      public String unit;
      public Boolean enabled;

      private ArrayList<Member> members = new ArrayList<Member>();

      public MyLightScene(String name, String unit, Boolean enabled)
      {
         this.name = name;
         this.unit = unit;
         this.enabled = enabled;
      }

      public void addMember(String name, String unit, Boolean enabled)
      {
         members.add(new Member(name, unit, enabled));
         if (enabled)
         {
            this.enabled = true;
         }
         aggregate();
      } 

      public Member isMember(String unit)
      {
         for (Member member : members)
         {
            if (member.unit.equals(unit))
            {
               return member;
            }
         }
         return null;
      }
      public void setActiv(String unit)
      {
         String lightSceneName = "";
         for (Item item : items)
         {
            if (item.unit.equals(unit))
            {
               lightSceneName = item.lightSceneName;
               break;
            }
         }
         for (Item item : items)
         {
            if (item.lightSceneName.equals(lightSceneName))
            {
               if (item.unit.equals(unit))
               {
                  item.activ = true;
               }
               else
               {
                  item.activ = false;
               }
            }
         }
      }

      class Member
      {
         public String name;
         public String unit;
         public Boolean enabled;
         
         public Member(String name, String unit, Boolean enabled)
         {
            this.name = name;
            this.unit = unit;
            this.enabled = enabled;
         }
      }
   }
}