package partha.firebasechatdemo.paginateFirebase;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import partha.firebasechatdemo.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView messageImageView, messageImageViewRight;
        public ProgressBar progress, progress_right;
        public RelativeLayout chat_item_parent;
        public ImageView img_is_edited;
        public TextView messageTextView, messageTextViewRight;
        public TextView userNameTextView, userNameTextViewRight, timeTextView, dateTextView, dateTextViewRight;
        public LinearLayout ll_date, ll_time, lin_date_time;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.txt_msg);
            messageTextViewRight = (TextView) itemView.findViewById(R.id.txt_msg_right);
            userNameTextView = (TextView) itemView.findViewById(R.id.txt_name);
            userNameTextViewRight = (TextView) itemView.findViewById(R.id.txt_name_right);
            timeTextView = (TextView) itemView.findViewById(R.id.txt_time);
            dateTextView = (TextView) itemView.findViewById(R.id.txt_date);
            dateTextViewRight = (TextView) itemView.findViewById(R.id.txt_date_right);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            messageImageViewRight = (ImageView) itemView.findViewById(R.id.messageImageViewRight);
            progress_right = (ProgressBar) itemView.findViewById(R.id.progress_right);
            progress = (ProgressBar) itemView.findViewById(R.id.progress);
            chat_item_parent = (RelativeLayout) itemView.findViewById(R.id.chat_item_parent);
            img_is_edited = (ImageView) itemView.findViewById(R.id.img_is_edited);
            /*ll_date = (LinearLayout) itemView.findViewById(R.id.ll_date);
            ll_time = (LinearLayout) itemView.findViewById(R.id.ll_time);
            lin_date_time = (LinearLayout) itemView.findViewById(R.id.lin_date_time);*/
        }
    }