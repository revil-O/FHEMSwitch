package de.fehngarten.fhemswitch;

import java.util.ArrayList;

public class ConfigDataOnly implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   public String urljs = "https://your-domain.de:8086";
   public String urlpl = "https://your-domain.de:8082/fhem";
   public ArrayList<ConfigSwitchRow> switchRows = new ArrayList<ConfigSwitchRow>();
   public ArrayList<ConfigLightsceneRow> lightsceneRows = new ArrayList<ConfigLightsceneRow>();
   public ArrayList<ConfigValueRow> valueRows = new ArrayList<ConfigValueRow>();
   public String connectionPW = "";
   public int switchCols = 0;
   public int valueCols = 1;
}
