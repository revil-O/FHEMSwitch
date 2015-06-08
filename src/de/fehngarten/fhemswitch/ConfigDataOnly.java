package de.fehngarten.fhemswitch;

import java.util.ArrayList;

public class ConfigDataOnly implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   public String urljs = "https://home.wernerschaeffer.de:8086";
   public String urlpl = "https://your-domain.de:8082/fhem";
   public ArrayList<ConfigSwitchRow> switchRows = new ArrayList<ConfigSwitchRow>();
   public ArrayList<ConfigLightsceneRow> lightsceneRows = new ArrayList<ConfigLightsceneRow>();
   public ArrayList<ConfigValueRow> valueRows = new ArrayList<ConfigValueRow>();
   public String connectionPW = "";
   public int layoutLandscape;
   public int layoutPortrait;
   public int switchCols;
   public int valueCols;
   
   public ConfigDataOnly()
   {
      this.layoutPortrait = 0; 
      this.layoutLandscape = 0;
      this.switchCols = 0;
      this.valueCols = 0;
   }
}
