package partha.firebasechatdemo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import partha.firebasechatdemo.fragments.ChatListFragment;
import partha.firebasechatdemo.fragments.FriendListFragment;

/**
 * TODO: Created by Tanay Mondal on 06-04-2017
 */

public class FragmentPagerAdapter extends FragmentStatePagerAdapter {
    public FragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment = new ChatListFragment();
        } else if (position == 1) {
            fragment = new FriendListFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
