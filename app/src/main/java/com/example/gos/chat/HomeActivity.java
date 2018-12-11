package com.example.gos.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class HomeActivity extends AppCompatActivity {

    private ViewPager mViewpager;
    private SectionsStatePagerAdapter mSectionStatePaperAdapter;
    int PAGE=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment_layout);

        mSectionStatePaperAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewpager = (ViewPager)findViewById(R.id.container);
        setupViewPager(mViewpager);
        setViewpager(1);

        mViewpager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mViewpager.getCurrentItem() == PAGE) {
                    mViewpager.setCurrentItem(PAGE-1, false);
                    mViewpager.setCurrentItem(PAGE, false);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupViewPager(ViewPager iViewPager){
        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.AddFragment(new LoginFragment(),"LoginFragment");
        adapter.AddFragment(new FragmentHome(),"homePageFragement");
        adapter.AddFragment(new RegisterFragment(),"RegisterFragment");
        iViewPager.setAdapter(adapter);
    }

    public void setViewpager(int fragmentNumber){
        PAGE = fragmentNumber;
        mViewpager.setCurrentItem(fragmentNumber);
    }

    @Override
    public void onBackPressed() {
        int fragmentNumber = mViewpager.getCurrentItem();

        if(fragmentNumber==1){
            finish();
        }
        else {
            setViewpager(1);
        }
    }
}
