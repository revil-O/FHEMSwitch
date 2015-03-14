package de.fehngarten.fhemswitch;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class SwitchesService extends RemoteViewsService
{
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent)
   {
      //Log.i("SwitchesService","started");
      return new SwitchesFactory(getApplicationContext(), intent);
   } 
}