package com.example.pagertest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initUi();
	}
	
	private void initUi() {
		ScrollPanel panel = (ScrollPanel) findViewById(R.id.panel);
		panel.addPage(R.layout.page_item);
		Pager pager = panel.addPage(R.layout.page_item);
		pager.setBackgroundColor(Color.DKGRAY);
		panel.addPage(R.layout.page_item);
		
		findViewById(R.id.test).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Toast.makeText(MainActivity.this, "test", 3000).show();
					}
					
				}
				);
		
		findViewById(R.id.test).setOnLongClickListener(
				new OnLongClickListener() {

					@Override
					public boolean onLongClick(View arg0) {
						// TODO Auto-generated method stub
						Toast.makeText(MainActivity.this, "long", 3000).show();
						return false;
					}});
		
	}
}
