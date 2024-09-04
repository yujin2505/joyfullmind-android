package com.yh04.joyfulmindapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.yh04.joyfulmindapp.FirstFragment;
import com.yh04.joyfulmindapp.FourthFragment;
import com.yh04.joyfulmindapp.MainActivity;
import com.yh04.joyfulmindapp.SecondFragment;
import com.yh04.joyfulmindapp.thirdFragment;

public class ViewPager2Adapter extends FragmentStateAdapter {
    // 뷰페이저에 보여줄 페이지 개수 설정
    private static final int NUM_PAGES = 4;

    // 프래그먼트로 구성된 어댑터일 경우 RealMainActivity가 아닌 Fragment로 초기화하여야 함
    public ViewPager2Adapter(@NonNull MainActivity MainActivity) {
        super(MainActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 반환할 프래그먼트 인스턴스 생성 및 초기화
        Fragment fragment = new FirstFragment();

        // 해당 위치(position)에 따라 프래그먼트 반환
        if (position == 0) // 첫번째 위치일 경우 FirstFragment 화면 반환
            fragment = new FirstFragment();
        else if (position ==1) // 두번째 위치일 경우 SecondFragment 화면 반환
            fragment = new SecondFragment();
        else if (position==2)
            fragment = new thirdFragment();
        else if (position==3)
            fragment = new FourthFragment();

        // 위치에 따른 프래그먼트 반환
        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES; // 전체 아이템 수 반환
    }
}

