package de.fehngarten.fhemswitch;

public class ConfigLightsceneRow implements java.io.Serializable
{
   private static final long serialVersionUID = 1L;
   public String unit;
   public String name;
   public Boolean enabled;
   public Boolean isHeader;

   public ConfigLightsceneRow(String unit, String name, Boolean enabled, Boolean isHeader)
   {
      this.unit = unit;
      this.name = name;
      this.enabled = enabled;
      this.isHeader = isHeader;
   }
}