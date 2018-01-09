package partha.firebasechatdemo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.PersonBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import partha.firebasechatdemo.firebaseChat.CodelabPreferences;
import partha.firebasechatdemo.firebaseChat.FriendlyMessage;
import partha.firebasechatdemo.utils.CameraManagerActivity;
import partha.firebasechatdemo.utils.Constants;

public class ChatActivity extends CameraManagerActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private RelativeLayout rl_extra;
    private ImageView img_cancel_extra, img_reply_extra, img_delete_extra, img_copy_extra, img_edit_extra;
    String selectedMessageKey = "";
    FriendlyMessage selectedFriendlyMessage;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_send:
                sendMessage();
                break;
            case R.id.img_share:
                onCallCameraButton();
                break;
            case R.id.img_cancel_extra:
                onCancelExtraClick();
                break;
            case R.id.img_copy_extra:
//                Constants.STRING_COPIED = selectedFriendlyMessage.getmessage();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(selectedFriendlyMessage.getMsgID(), selectedFriendlyMessage.getmessage());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "message copied", Toast.LENGTH_SHORT).show();
                break;
            case R.id.img_delete_extra:
                userMessageDbReference.setValue(null);
                onCancelExtraClick();
                break;
            case R.id.img_reply_extra:
                break;
            case R.id.img_edit_extra:
                onEditExtraClick();
                break;
        }
    }

    private void onEditExtraClick() {
        if (selectedFriendlyMessage.getmessage().equalsIgnoreCase("")){
            Toast.makeText(this, "Media Files Cann't be Edited.", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "Will be done Soon.", Toast.LENGTH_SHORT).show();
            mMessageEditText.setText(selectedFriendlyMessage.getmessage());
        }
    }

    private void onCancelExtraClick() {
        selectedMessageKey = "";
        selectedFriendlyMessage = null;
        userMessageDbReference.removeEventListener(userMessageListener);
        rl_extra.setVisibility(View.GONE);
    }

    private void sendMessage() {
        FriendlyMessage friendlyMessage;
        if (selectedMessageKey.equalsIgnoreCase("")) {
            friendlyMessage = new FriendlyMessage(from_user_id, to_user_id, mMessageEditText.getText().toString(),
                    from_user_name,
                    from_user_img, "", String.valueOf(System.currentTimeMillis()), false);
            mFirebaseDatabaseReference.child(MESSAGES_CHILD_unique_users).push().setValue(friendlyMessage);
        } else {
            friendlyMessage = new FriendlyMessage(from_user_id, to_user_id, mMessageEditText.getText().toString(),
                    from_user_name,
                    from_user_img, "", String.valueOf(System.currentTimeMillis()), true);
            userMessageDbReference.setValue(friendlyMessage);
        }
        lastMessage = mMessageEditText.getText().toString().trim();
        mMessageEditText.setText("");
        mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView messageImageView, messageImageViewRight;
        private final ProgressBar progress, progress_right;
        private final RelativeLayout chat_item_parent;
        private final ImageView img_is_edited;
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

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages/";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private Button mSendButton;
    private ImageView img_send, img_share;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;
    private ProgressBar mProgressBar;
    private DatabaseReference mFirebaseDatabaseReference, userMessageDbReference;
    ValueEventListener userMessageListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    //    private EditText mMessageEditText;
    private EmojiconEditText mMessageEditText;
    private ImageView mAddMessageImageView;
    private AdView mAdView;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private GoogleApiClient mGoogleApiClient;

    private View rootView;
    private ImageView emojiImageView;
    private EmojIconActions emojIcon;

    public static String MESSAGES_CHILD_unique_users;
    private String uniqueChatId = "test";

    String from_user_id = "";
    String from_user_name = "";
    String from_user_img = "";
    String from_user_mail = "";

    String to_user_id = "";
    String to_user_name = "";
    String to_user_img = "";
    String to_user_mail = "";
    private String lastMessage = "";

    String lastMsgKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle getData = getIntent().getExtras();
        if (getData != null) {
            from_user_id = getData.get(Constants.FROM_USER_ID).toString();
            from_user_img = getData.get(Constants.FROM_USER_IMG).toString();
            from_user_mail = getData.get(Constants.FROM_USER_MAIL).toString();
            from_user_name = getData.get(Constants.FROM_USER_NAME).toString();

            to_user_id = getData.get(Constants.TO_USER_ID).toString();
            to_user_img = getData.get(Constants.TO_USER_IMG).toString();
            to_user_mail = getData.get(Constants.TO_USER_MAIL).toString();
            to_user_name = getData.get(Constants.TO_USER_NAME).toString();
        }

        initFields();
    }

    private void initFields() {

        rl_extra = (RelativeLayout) findViewById(R.id.rl_extra);
        img_cancel_extra = (ImageView) findViewById(R.id.img_cancel_extra);
        img_reply_extra = (ImageView) findViewById(R.id.img_reply_extra);
        img_delete_extra = (ImageView) findViewById(R.id.img_delete_extra);
        img_copy_extra = (ImageView) findViewById(R.id.img_copy_extra);
        img_edit_extra = (ImageView) findViewById(R.id.img_edit_extra);
        img_cancel_extra.setOnClickListener(this);
        img_reply_extra.setOnClickListener(this);
        img_delete_extra.setOnClickListener(this);
        img_copy_extra.setOnClickListener(this);
        img_edit_extra.setOnClickListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        MESSAGES_CHILD_unique_users = MESSAGES_CHILD + uniqueChatId;

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);

        mLinearLayoutManager.setStackFromEnd(true);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

//        mFirebaseDatabaseReference.get

        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(
                FriendlyMessage.class,
//                R.layout.row_my_message,
                R.layout.row_message,
                MessageViewHolder.class,
                //mFirebaseDatabaseReference.child(MESSAGES_CHILD)) {
                mFirebaseDatabaseReference.child(MESSAGES_CHILD_unique_users)) {

            @Override
            protected FriendlyMessage parseSnapshot(DataSnapshot snapshot) {
                FriendlyMessage friendlyMessage = super.parseSnapshot(snapshot);
                if (friendlyMessage != null) {
//                    friendlyMessage.setsender_id(snapshot.getKey());
                    friendlyMessage.setMsgID(snapshot.getKey());
                }
                return friendlyMessage;
            }

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder, FriendlyMessage friendlyMessage, int position) {

                if (position==0){
                    lastMsgKey = friendlyMessage.getMsgID();
                }
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                loadMessageItemData(viewHolder, friendlyMessage);

                viewHolder.chat_item_parent.setTag(friendlyMessage.getMsgID());
//                viewHolder.chat_item_parent.setId(position);

                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (rl_extra.getVisibility()!=View.VISIBLE) {
                            String key = v.getTag().toString();
                            selectedMessageKey = key;
                            manageSelectedMessage(key);
//                            Toast.makeText(ChatActivity.this, key, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, "You Have Already Selected a message.", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
            }
        };

        mMessageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int pastVisibleItems = mLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
                if (pastVisibleItems  == 0) {
//                    Toast.makeText(getContext(),"Top most item",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
//        OverScrollDecoratorHelper.setUpOverScroll(mMessageRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);


        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize Firebase Remote Config.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Define Firebase Remote Config Settings.
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("friendly_msg_length", 1000L);

        // Apply config settings and default values.
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        // Fetch remote config.
        fetchConfig();

        mMessageEditText = (EmojiconEditText) findViewById(R.id.et_emojicon);

        rootView = (RelativeLayout) findViewById(R.id.root_view);

        emojiImageView = (ImageView) findViewById(R.id.emoji_btn);
        emojIcon = new EmojIconActions(this, rootView, mMessageEditText, emojiImageView);
        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e(TAG, "Keyboard opened!");
            }

            @Override
            public void onKeyboardClose() {
                Log.e(TAG, "Keyboard closed");
            }
        });

        //mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
//                    mSendButton.setEnabled(true);
                    img_share.setVisibility(View.GONE);
                    img_send.setVisibility(View.VISIBLE);
                } else {
//                    mSendButton.setEnabled(false);
                    img_send.setVisibility(View.GONE);
                    img_share.setVisibility(View.VISIBLE);
                    if (rl_extra.getVisibility() == View.VISIBLE) {
                        onCancelExtraClick();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (Button) findViewById(R.id.sendButton);
        img_send = (ImageView) findViewById(R.id.img_send);
        img_share = (ImageView) findViewById(R.id.img_share);
        img_send.setOnClickListener(this);
        img_share.setOnClickListener(this);
    }

    private void manageSelectedMessage(final String key) {
        userMessageDbReference = FirebaseDatabase.getInstance().getReference().child("messages").child("test").child(key);
        userMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                selectedFriendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                selectedFriendlyMessage.setMsgID(key);
                rl_extra.setVisibility(View.VISIBLE);
                // Edit Visibility GONE if not my message
                if (!selectedFriendlyMessage.getsender_id().equalsIgnoreCase(from_user_id)){
                    img_edit_extra.setVisibility(View.GONE);
                    img_delete_extra.setVisibility(View.GONE);
                } else {
                    img_edit_extra.setVisibility(View.VISIBLE);
                    img_delete_extra.setVisibility(View.VISIBLE);
                }
                Log.d("getValue"," test");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: ",databaseError.toException());
                Toast.makeText(ChatActivity.this, "Failed to load User list.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        userMessageDbReference.addValueEventListener(userMessageListener);

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
        if (friendlyMessage.getsender_id().equalsIgnoreCase(from_user_id) /*|| friendlyMessage.getreceiver_id().equalsIgnoreCase(from_user_id)*/) {
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
                loadImage(viewHolder.messageImageViewRight, viewHolder.progress_right, friendlyMessage.getimage_url());
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
                loadImage(viewHolder.messageImageView, viewHolder.progress, friendlyMessage.getimage_url());
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

    private void loadImage(final ImageView img_msg, final ProgressBar progressBar, String imageUrl) {

        if (imageUrl.startsWith("gs://")) {
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl);
            storageReference.getDownloadUrl().addOnCompleteListener(
                    new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String downloadUrl = task.getResult().toString();
                                Glide.with(ChatActivity.this)
                                        .load(downloadUrl)
                                        .listener(new RequestListener<String, GlideDrawable>() {
                                            @Override
                                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Glide.with(ChatActivity.this)
                    .load(imageUrl)
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

    private Action getMessageViewAction(FriendlyMessage friendlyMessage) {
        return new Action.Builder(Action.Builder.VIEW_ACTION)
                .setObject(friendlyMessage.getName(), MESSAGE_URL.concat(friendlyMessage.getsender_id()))
                .setMetadata(new Action.Metadata.Builder().setUpload(false))
                .build();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_menu:
                sendInvitation();
                return true;
            case R.id.crash_menu:
                FirebaseCrash.logcat(Log.ERROR, TAG, "crash caused");
                causeCrash();
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                        .addApi(Auth.GOOGLE_SIGN_IN_API)
                        .build();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
                mUsername = ANONYMOUS;
                mPhotoUrl = null;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.fresh_config_menu:
                fetchConfig();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void causeCrash() {
        throw new NullPointerException("Fake null pointer exception");
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available via FirebaseRemoteConfig get<type> calls.
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // There has been an error fetching the config
                        Log.w(TAG, "Error fetching config", e);
                        applyRetrievedLengthLimit();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Use Firebase Measurement to log that invitation was sent.
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");

                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Use Firebase Measurement to log that invitation was not sent
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);

                // Sending failed or it was canceled, show failure message to the user
                Log.d(TAG, "Failed to send invitation.");
            }
        }
    }

    private void sendImageMessage(final Uri uri) {
        FriendlyMessage friendlyImgMessage = new FriendlyMessage(from_user_id, to_user_id, mMessageEditText.getText().toString(),
                from_user_name,
                from_user_img, LOADING_IMAGE_URL, String.valueOf(System.currentTimeMillis()), false);

                    /*FriendlyMessage tempMessage = new FriendlyMessage(null, mUsername, mPhotoUrl,
                            LOADING_IMAGE_URL);*/
        mFirebaseDatabaseReference.child(MESSAGES_CHILD_unique_users).push()
                .setValue(friendlyImgMessage, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            String key = databaseReference.getKey();
                            StorageReference storageReference =
                                    FirebaseStorage.getInstance()
                                            .getReference(mFirebaseUser.getUid())
                                            .child(key)
                                            .child(uri.getLastPathSegment());

                            putImageInStorage(storageReference, uri, key);
                        } else {
                            Log.w(TAG, "Unable to write message to database.", databaseError.toException());
                        }
                    }
                });
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).addOnCompleteListener(ChatActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            FriendlyMessage friendlyImgMessage = new FriendlyMessage(from_user_id, to_user_id, mMessageEditText.getText().toString(),
                                    from_user_name,
                                    from_user_img, task.getResult().getDownloadUrl()
                                    .toString(), String.valueOf(System.currentTimeMillis()), false);
                            /*FriendlyMessage friendlyMessage =
                                    new FriendlyMessage(null, mUsername, mPhotoUrl,
                                            task.getResult().getDownloadUrl()
                                                    .toString());*/
                            mFirebaseDatabaseReference.child(MESSAGES_CHILD_unique_users).child(key)
                                    .setValue(friendlyImgMessage);
                        } else {
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());
                            Toast.makeText(ChatActivity.this, "Image upload task was not successful." + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent in = new Intent(ChatActivity.this, SignInActivity.class);
        startActivity(in);
        finish();
    }

    /**
     * Apply retrieved length limit to edit text field. This result may be fresh from the server or it may be from
     * cached values.
     */
    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length = mFirebaseRemoteConfig.getLong("friendly_msg_length");
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(TAG, "FML is: " + friendly_msg_length);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
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

    @Override
    protected void onBitmapReceivedFromCamera(Bitmap bitmap, String path, Uri imageUri) {
//        sendImageMessage(Uri.parse(path));
        sendImageMessage(imageUri);
    }

    @Override
    protected void onBitmapReceivedFromGallery(Bitmap bitmap, String path, Uri imageUri) {
//        sendImageMessage(Uri.parse(path));
        sendImageMessage(imageUri);
    }

    /*private void zoomImageFromThumb(final View thumbView, *//*int imageResId,*//* String name) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
//        expandedImageView.setImageResource(imageResId);
        Bitmap bitmap = getBitmapFromAssets(name);
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (width*bitmap.getHeight())/bitmap.getWidth();
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        expandedImageView.setImageBitmap(bitmap);
        view_thumb = thumbView;

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        startBounds = new Rect();
        finalBounds = new Rect();
        globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        rl_container.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    private void hideExpandedImage(final View thumb1View, int position) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(expandedImageView, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.Y,startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_X, startScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_Y, startScaleFinal));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());

//        Constants.setImage(expandedImageView, list_recentChat.get(position).getImage(), getActivity());
//        expandedImageView.setImageBitmap();


        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                thumb1View.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                mCurrentAnimator = null;


            }

            @Override
            public void onAnimationCancel(Animator animation) {
                thumb1View.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
    }*/
}
