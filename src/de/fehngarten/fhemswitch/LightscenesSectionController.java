package de.fehngarten.fhemswitch;

import android.graphics.Point;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class LightscenesSectionController extends DragSortController
{
   private int mPos;
   private ConfigLightsceneAdapter configLightsceneAdapter;
   DragSortListView mDslv;

   public LightscenesSectionController(DragSortListView dslv, ConfigLightsceneAdapter adapter)
   {
      super(dslv, R.id.config_lightscene_unit, DragSortController.ON_DOWN, 0);
      setRemoveEnabled(false);
      mDslv = dslv;
      configLightsceneAdapter = adapter;
      //setDragHandleId(dslv);
   }

   @Override
   public int startDragPosition(MotionEvent ev)
   {
      int res = super.startDragPosition(ev);

      if (res < 0) { return DragSortController.MISS; }

      if (configLightsceneAdapter.isDragable(res))
      {
         return res;
      }
      else
      {
         return DragSortController.MISS;
      }
   }

   @Override
   public View onCreateFloatView(int position)
   {
      mPos = position;

      View v = configLightsceneAdapter.getView(position, null, mDslv);
      /*          if (position < mDivPos) {
                    v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_handle_section1));
                } else {
                    v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_handle_section2));
                }
                v.getBackground().setLevel(10000);*/
      return v;
   }

   private int origHeight = -1;

   @Override
   public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint)
   {
      //final int first = mDslv.getFirstVisiblePosition();
      //final int lvDivHeight = mDslv.getDividerHeight();
      if (origHeight == -1)
      {
         origHeight = floatView.getHeight();
      }
/*
      if (touchPoint.x > mDslv.getWidth() / 2)
      {
         float scale = touchPoint.x - mDslv.getWidth() / 2;
         scale /= (float) (mDslv.getWidth() / 5);
         ViewGroup.LayoutParams lp = floatView.getLayoutParams();
         lp.height = Math.max(origHeight, (int) (scale * origHeight));
         Log.d("mobeta", "setting height " + lp.height);
         floatView.setLayoutParams(lp);
      }
*/
      int[] bounds = configLightsceneAdapter.getBounds(mPos);
      
      View top = mDslv.getChildAt(bounds[0]);
      View bottom = mDslv.getChildAt(bounds[1]);
      final int limitTop = top.getTop();
      final int limitBottom = bottom.getBottom() - floatView.getHeight();
      
      if (floatPoint.y < limitTop )
      {
         floatPoint.y = limitTop;
      }
      else if (floatPoint.y > limitBottom )
      {
         floatPoint.y = limitBottom;
      }
   }

   @Override
   public void onDestroyFloatView(View floatView)
   {
      //do nothing; block super from crashing
   }
}
