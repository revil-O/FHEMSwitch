package de.fehngarten.fhemswitch;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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
      final int first = mDslv.getFirstVisiblePosition();
      final int lvDivHeight = mDslv.getDividerHeight();

      if (origHeight == -1)
      {
         origHeight = floatView.getHeight();
      }

      if (touchPoint.x > mDslv.getWidth() / 2)
      {
         float scale = touchPoint.x - mDslv.getWidth() / 2;
         scale /= (float) (mDslv.getWidth() / 5);
         ViewGroup.LayoutParams lp = floatView.getLayoutParams();
         lp.height = Math.max(origHeight, (int) (scale * origHeight));
         Log.d("mobeta", "setting height " + lp.height);
         floatView.setLayoutParams(lp);
      }

      int[] bounds = configLightsceneAdapter.getBounds(mPos);
      Log.i("bounds", Integer.toString(bounds[0]) + " " + Integer.toString(bounds[1]));

      /*          View div = mDslv.getChildAt(mDivPos - first);

                if (div != null) {
                    if (mPos > mDivPos) {
                        // don't allow floating View to go above
                        // section divider
                        final int limit = div.getBottom() + lvDivHeight;
                        if (floatPoint.y < limit) {
                            floatPoint.y = limit;
                        }
                    } else {
                        // don't allow floating View to go below
                        // section divider
                        final int limit = div.getTop() - lvDivHeight - floatView.getHeight();
                        if (floatPoint.y > limit) {
                            floatPoint.y = limit;
                        }
                    }
                }*/
   }

   @Override
   public void onDestroyFloatView(View floatView)
   {
      //do nothing; block super from crashing
   }
}
