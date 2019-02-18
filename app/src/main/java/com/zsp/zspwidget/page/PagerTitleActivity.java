package com.zsp.zspwidget.page;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.zsp.zspwidget.R;
import com.zsp.zspwidget.base.BaseActivity;
import java.util.ArrayList;
import java.util.List;

public class PagerTitleActivity extends BaseActivity {
    ViewPager mPager;

    private MyPagerAdapter adapter1;
    private MyPagerAdapter adapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_title);
        setTitle("PagerTitleView");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        mPager = findViewById(R.id.pager_title_view_pager);
//        mTitleView = findViewById(R.id.pager_title_pager_title);
        adapter1=new MyPagerAdapter(getTitle(10));
        adapter2=new MyPagerAdapter(getTitle(2));
//        mPager.setAdapter(adapter1);


    }


    private  List<String> getTitle(int size) {
        List<String> titles = new ArrayList<>();
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<size;i++){
            builder.append(i);
            if(i%2==0){
                titles.add("TITLE"+i);
            }else {
                titles.add(i+"");
            }
        }
        return titles;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.pager_titler_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_change_adapter:
                if(mPager.getAdapter()==adapter1){
                    mPager.setAdapter(adapter2);
                }else {
                    mPager.setAdapter(adapter1);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyPagerAdapter extends PagerAdapter{
        List<String> titles;
        List<View> views;

        MyPagerAdapter(List<String> titles) {
            this.titles = titles;
            views=new ArrayList<>();
            for(int i=0;i<titles.size();i++){
                TextView textView=new TextView(PagerTitleActivity.this);
                textView.setText("这个是个测试页面");
                views.add(textView);
            }
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            container.removeView(container.getChildAt(position));
            container.removeView(views.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
