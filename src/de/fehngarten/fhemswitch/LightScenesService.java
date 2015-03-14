package de.fehngarten.fhemswitch;

import android.content.Intent;
//import android.util.Log;
import android.widget.RemoteViewsService;

public class LightScenesService extends RemoteViewsService
{
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent)
   {
      //Log.i("SwitchesService","started");
      return new LightScenesFactory(this.getApplicationContext(), intent);
   }
}