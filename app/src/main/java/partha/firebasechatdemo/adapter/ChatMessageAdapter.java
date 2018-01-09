package partha.firebasechatdemo.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.PersonBuilder;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import partha.firebasechatdemo.R;
import partha.firebasechatdemo.firebaseChat.FriendlyMessage;
import partha.firebasechatdemo.interfaces.OnLoadNextPageListener;
import partha.firebasechatdemo.paginateFirebase.MessageViewHolder;
import partha.firebasechatdemo.utils.Constants;

/**
 * Created by phanvanlinh on 12/04/2017.
 * phanvanlinh.94vn@gmail.com
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private List<FriendlyMessage> mQuestionList;
    private Context context;
    private String TAG = "PARTHA";
    private String myId;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final String ANONYMOUS = "anonymous";
    private String mUsername = ANONYMOUS;
    private OnLoadNextPageListener onLoadNextPageListener;

    public ChatMessageAdapter(List<FriendlyMessage> mQuestionList, Context context, String myId) {
        this.mQuestionList = mQuestionList;
        this.context = context;
        this.myId = myId;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_message, parent, false);

        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        loadMessageItemData(holder, mQuestionList.get(position));
        System.out.println("PARTHA : recyclerview position : "+position);
        if (position==mQuestionList.size()-1){
            onLoadNextPageListener.loadNextPageNow(mQuestionList.size());
        }
    }

    /*@Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Question question = mQuestionList.get(position);
        holder.title.setText(question.getContent());
        holder.mark.setText("" + question.getMark());
    }*/

    @Override
    public int getItemCount() {
        return mQuestionList.size();
    }
    private void loadMessageItemData(MessageViewHolder viewHolder, FriendlyMessage friendlyMessage) {

        String datetime = convertDateFromMillis(friendlyMessage.getsenddate().toString());
        Boolean isEdited = friendlyMessage.getEdit();
        if (isEdited){
            viewHolder.img_is_edited.setVisibility(View.VISIBLE);
        } else {
            viewHolder.img_is_edited.setVisibility(View.GONE);
        }
        // It's me. Right Alignment
        if (friendlyMessage.getsender_id().equalsIgnoreCase(myId) /*|| friendlyMessage.getreceiver_id().equalsIgnoreCase(from_user_id)*/) {
            viewHolder.userNameTextView.setVisibility(View.GONE);
            viewHolder.messageTextView.setVisibility(View.GONE);
            viewHolder.dateTextView.setVisibility(View.GONE);
            viewHolder.messageImageView.setVisibility(View.GONE);
            viewHolder.progress.setVisibility(View.GONE);

            viewHolder.userNameTextViewRight.setVisibility(View.VISIBLE);
            viewHolder.dateTextViewRight.setVisibility(View.VISIBLE);
            viewHolder.userNameTextViewRight.setText(friendlyMessage.getName());
            viewHolder.dateTextViewRight.setText(datetime);

            // No Blank Message. So check img url
            if (friendlyMessage.getmessage().equalsIgnoreCase("")) {
                viewHolder.messageTextViewRight.setVisibility(View.GONE);
                viewHolder.messageImageViewRight.setVisibility(View.VISIBLE);
                viewHolder.progress_right.setVisibility(View.VISIBLE);
                try {
                    loadImage(viewHolder.messageImageViewRight, viewHolder.progress_right, friendlyMessage.getimage_url());
                } catch (FirebaseException e) {
                    e.printStackTrace();
                }
            } else {
                viewHolder.messageTextViewRight.setVisibility(View.VISIBLE);
                viewHolder.messageImageView.setVisibility(View.GONE);
                viewHolder.progress_right.setVisibility(View.GONE);
                viewHolder.messageTextViewRight.setText(friendlyMessage.getmessage());
            }

        } else {
            viewHolder.userNameTextViewRight.setVisibility(View.GONE);
            viewHolder.messageTextViewRight.setVisibility(View.GONE);
            viewHolder.dateTextViewRight.setVisibility(View.GONE);
            viewHolder.messageImageViewRight.setVisibility(View.GONE);
            viewHolder.progress_right.setVisibility(View.GONE);

            viewHolder.userNameTextView.setVisibility(View.VISIBLE);
            viewHolder.dateTextView.setVisibility(View.VISIBLE);
            viewHolder.userNameTextView.setText(friendlyMessage.getName());
            viewHolder.dateTextView.setText(datetime);

            // No Blank Message. So check img url
            if (friendlyMessage.getmessage().equalsIgnoreCase("")) {
                viewHolder.messageTextView.setVisibility(View.GONE);
                viewHolder.messageImageView.setVisibility(View.VISIBLE);
                viewHolder.progress.setVisibility(View.VISIBLE);
                try {
                    loadImage(viewHolder.messageImageView, viewHolder.progress, friendlyMessage.getimage_url());
                } catch (FirebaseException e) {
                    e.printStackTrace();
                }
            } else {
                viewHolder.messageTextView.setVisibility(View.VISIBLE);
                viewHolder.messageImageView.setVisibility(View.GONE);
                viewHolder.progress.setVisibility(View.GONE);
                viewHolder.messageTextView.setText(friendlyMessage.getmessage());
            }
        }
        if (friendlyMessage.getmessage() != null) {
            // write this message to the on-device index
            FirebaseAppIndex.getInstance().update(getMessageIndexable(friendlyMessage));
        }
        // log a view action on it
        FirebaseUserActions.getInstance().end(getMessageViewAction(friendlyMessage));
    }

    private void loadImage(final ImageView img_msg, final ProgressBar progressBar, String imageUrl) throws FirebaseException {

        if (imageUrl.startsWith("gs://")) {
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl);
            storageReference.getDownloadUrl().addOnCompleteListener(
                    new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String downloadUrl = task.getResult().toString();
                                Glide.with(context)
                                        .load(downloadUrl)
                                        .diskCacheStrategy( DiskCacheStrategy.ALL )
                                        .listener(new RequestListener<String, GlideDrawable>() {
                                            @Override
                                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                progressBar.setVisibility(View.GONE);
                                                return false;
                                            }
                                        })
                                        .into(img_msg);
                            } else {
                                Log.w(TAG, "Getting download url was not successful.",
                                        task.getException());
                            }
                        }
                    });
        } else {
            Glide.with(context)
                    .load(imageUrl)
                    .diskCacheStrategy( DiskCacheStrategy.ALL )
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(img_msg);
        }

    }

    private Indexable getMessageIndexable(FriendlyMessage friendlyMessage) {
        PersonBuilder sender = Indexables.personBuilder()
                .setIsSelf(mUsername.equals(friendlyMessage.getName()))
                .setName(friendlyMessage.getName())
                .setUrl(MESSAGE_URL.concat(friendlyMessage.getsender_id() + "/sender"));

        PersonBuilder recipient = Indexables.personBuilder()
                .setName(mUsername)
                .setUrl(MESSAGE_URL.concat(friendlyMessage.getreceiver_id() + "/recipient"));

        Indexable messageToIndex = Indexables.messageBuilder()
                .setName(friendlyMessage.getmessage())
                .setUrl(MESSAGE_URL.concat(friendlyMessage.getsender_id()))
                .setSender(sender)
                .setRecipient(recipient)
                .build();

        return messageToIndex;
    }

    private Action getMessageViewAction(FriendlyMessage friendlyMessage) {
        return new Action.Builder(Action.Builder.VIEW_ACTION)
                .setObject(friendlyMessage.getName(), MESSAGE_URL.concat(friendlyMessage.getsender_id()))
                .setMetadata(new Action.Metadata.Builder().setUpload(false))
                .build();
    }

    public static String convertDateFromMillis(String millis) {

        //long val = 1346524199000l;
        long val = Long.parseLong(millis);

        Date date = new Date(val);
        SimpleDateFormat df2 = new SimpleDateFormat(Constants.app_display_date_format + ", " + Constants.app_display_time_format);
        String dateText = df2.format(date);
        System.out.println(dateText);
        return dateText;
    }

    public void setLoadNextPageListener(OnLoadNextPageListener onLoadNextPageListener) {
        this.onLoadNextPageListener = onLoadNextPageListener;
    }
}