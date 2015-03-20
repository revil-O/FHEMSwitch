package de.fehngarten.fhemswitch;

public class MySwitch
{
   public String name;
   public String unit;
   public String cmd;
   public String icon;

   public MySwitch(String name, String unit, String cmd)
   {
      this.name = name;
      this.unit = unit;
      this.cmd = cmd;
      icon = "off";
   }

   public void setIcon(String icon)
   {
      if (icon.equals("on") || icon.equals("off"))
      {
         this.icon = icon;
      }
      else
      {
         this.icon = "undefined";
      }

   }

   public String activateCmd()
   {
      String cmd = "set " + this.unit + " " + this.cmd;
      return cmd;
   }

}
