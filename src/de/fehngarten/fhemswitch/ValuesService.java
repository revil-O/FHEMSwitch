package de.fehngarten.fhemswitch;

import android.content.Intent;
//import android.util.Log;
import android.widget.RemoteViewsService;

public class ValuesService extends RemoteViewsService
{
   @Override
   public RemoteViewsFactory onGetViewFactory(Intent intent)
   {
      //Log.i("ValuesService","started");
      return new ValuesFactory(getApplicationContext(), intent);
   } 
}