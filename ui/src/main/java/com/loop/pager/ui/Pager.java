/**
 * 
 */
package com.loop.pager.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * @author szy
 *
 */
public class Pager extends LinearLayout {

	public Pager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

}
