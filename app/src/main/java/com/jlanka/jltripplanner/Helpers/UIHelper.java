package com.jlanka.jltripplanner.Helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Workstation on 2/24/2018.
 */

public class UIHelper {
    private Context c;
    private static UIHelper cil;

    private UIHelper(Context c){
        this.c=c;
    }

    public static UIHelper getInstance(Context c){
        if (cil==null)
            cil=new UIHelper(c);

        return cil;
    }

    public Bitmap getMarkerIcon(String type, String status){
        Bitmap image=null;
        if (type.equals("AC") ){
            switch (status){
                case "Available":
                    image=(resizeMapIcons("l2a",100,120));
                    break;
                case "Busy":
                    image=(resizeMapIcons("l2b",100,120));
                    break;
                case "NA":
                    image=(resizeMapIcons("l2u",100,120));
                    break;
                case "Pending...":
                    image=(resizeMapIcons("l2u",100,120));
                    break;
            }
        }
        else {
            switch (status){
                case "Available":
                    image=(resizeMapIcons("dcfa",100,120));
                    break;
                case "Busy":
                    image=(resizeMapIcons("dcfb",100,120));
                    break;
                case "NA":
                    image=(resizeMapIcons("dcfu",100,120));
                    break;
                case "Pending...":
                    image=(resizeMapIcons("dcfu",100,120));
                    break;
            }
        }
        return image;
    }

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(c.getResources(),c.getResources().getIdentifier(iconName, "drawable", c.getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public int getMarginInDp(int sizeInDP) {
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDP, c.getResources()
                        .getDisplayMetrics());

        return marginInDp;
    }
}
