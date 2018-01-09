package partha.firebasechatdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import partha.firebasechatdemo.PaginatedChatActivity;
import partha.firebasechatdemo.R;
import partha.firebasechatdemo.SignInActivity;
import partha.firebasechatdemo.firebaseChat.User;
import partha.firebasechatdemo.utils.Constants;

/**
 * Created by DAT-Asset-110 on 02-08-2017.
 */

public class RecentMessageAdapter extends RecyclerView.Adapter<RecentMessageAdapter.ViewHolder> {
    Context mContext;
    View itemView;
    ArrayList<User> userList = new ArrayList<>();
    private int lastPosition = -1;
    User currentUser;

    public RecentMessageAdapter(Context con, ArrayList<User> userList, User currentUser){
        this.mContext = con;
        this.userList = userList;
        this.currentUser = currentUser;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_message, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.txt_name.setText(userList.get(position).getName());
        Glide.with(mContext)
                .load(userList.get(position).getPhotoUrl())
                .into(holder.img_profile);

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.txt_name)
        TextView txt_name;

        @Bind(R.id.img_profile)
        CircleImageView img_profile;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
//            listClickListener.inEscortServiceListClick(getAdapterPosition());
            if (currentUser!=null) {
//                Intent intentChat = new Intent(mContext, ChatActivity.class);
                Intent intentChat = new Intent(mContext, PaginatedChatActivity.class);

                intentChat.putExtra(Constants.FROM_USER_ID, currentUser.getUserId());
                intentChat.putExtra(Constants.FROM_USER_NAME, currentUser.getName());
                intentChat.putExtra(Constants.FROM_USER_MAIL, currentUser.getEmail());
                intentChat.putExtra(Constants.FROM_USER_IMG, currentUser.getPhotoUrl());

                intentChat.putExtra(Constants.TO_USER_ID, userList.get(getAdapterPosition()).getUserId());
                intentChat.putExtra(Constants.TO_USER_NAME, userList.get(getAdapterPosition()).getName());
                intentChat.putExtra(Constants.TO_USER_MAIL, userList.get(getAdapterPosition()).getEmail());
                intentChat.putExtra(Constants.TO_USER_IMG, userList.get(getAdapterPosition()).getPhotoUrl());

//                ((Activity)mContext).finish();
                mContext.startActivity(intentChat);
            } else {
                Intent in = new Intent(mContext, SignInActivity.class);
                mContext.startActivity(in);
            }
        }

        void clearAnimation() {
            itemView.clearAnimation();
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
