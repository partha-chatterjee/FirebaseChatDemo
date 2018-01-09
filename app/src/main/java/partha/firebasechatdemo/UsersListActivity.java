package partha.firebasechatdemo;

import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import partha.firebasechatdemo.adapter.FragmentPagerAdapter;

public class UsersListActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_chat, tv_users;
    private ViewPager view_pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        initFields();
    }

    private void initFields() {
        tv_chat = (TextView) findViewById(R.id.tv_chat);
        tv_users = (TextView) findViewById(R.id.tv_users);
        view_pager = (ViewPager) findViewById(R.id.view_pager);

        setUpClickListener();
        setUpViewPager();
    }

    private void setUpClickListener() {
        tv_chat.setOnClickListener(this);
        tv_users.setOnClickListener(this);
    }

    private void setUpViewPager() {
        view_pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()));

        view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 0) {
                    tv_chat.setBackgroundResource(R.drawable.shape_left_circle_selected);
                    tv_chat.setTextColor(ActivityCompat.getColor(UsersListActivity.this, android.R.color.white));
                    tv_users.setTextColor(ActivityCompat.getColor(UsersListActivity.this, R.color.colorPrimaryDark));
                    tv_users.setBackgroundResource(R.drawable.shape_right_circle);

                } else {
                    tv_chat.setBackgroundResource(R.drawable.shape_left_circle);
                    tv_chat.setTextColor(ActivityCompat.getColor(UsersListActivity.this, R.color.colorPrimaryDark));
                    tv_users.setTextColor(ActivityCompat.getColor(UsersListActivity.this, android.R.color.white));
                    tv_users.setBackgroundResource(R.drawable.shape_right_circle_selected);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_chat:
                view_pager.setCurrentItem(0);
                break;
            case R.id.tv_users:
                view_pager.setCurrentItem(1);
                break;
        }
    }
}
