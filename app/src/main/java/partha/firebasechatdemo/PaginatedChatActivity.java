package partha.firebasechatdemo;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import partha.firebasechatdemo.adapter.ChatMessageAdapter;
import partha.firebasechatdemo.firebaseChat.FriendlyMessage;
import partha.firebasechatdemo.interfaces.OnLoadNextPageListener;
import partha.firebasechatdemo.paginateFirebase.InfiniteFirebaseRecyclerAdapter;
import partha.firebasechatdemo.paginateFirebase.Logger;
import partha.firebasechatdemo.paginateFirebase.MessageViewHolder;
import partha.firebasechatdemo.utils.CameraManagerActivity;
import partha.firebasechatdemo.utils.Constants;

public class PaginatedChatActivity extends CameraManagerActivity implements View.OnClickListener, ValueEventListener, OnLoadNextPageListener {

    String from_user_id = "";
    String from_user_name = "";
    String from_user_img = "";
    String from_user_mail = "";

    String to_user_id = "";
    String to_user_name = "";
    String to_user_img = "";
    String to_user_mail = "";

    private RelativeLayout rl_extra;
    private ImageView img_cancel_extra, img_reply_extra, img_delete_extra, img_copy_extra, img_edit_extra;
    private Button mSendButton;
    private ImageView img_send, img_share;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private SharedPreferences mSharedPreferences;
    private String mUsername;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    public static String MESSAGES_CHILD_unique_users;
    private String uniqueChatId = "test";
    private DatabaseReference mFirebaseDatabaseReference, userMessageDbReference;
    private EmojiconEditText mMessageEditText;
    private View rootView;
    private ImageView emojiImageView;
    private EmojIconActions emojIcon;
    private static final String TAG = "PaginatedChatActivity";
    public static final String ANONYMOUS = "anonymous";
    public static final String MESSAGES_CHILD = "messages/";

    private int mPageLimit = 10;
    private int currentPage = 0;

    private InfiniteFirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mAdapter;

    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

    List<FriendlyMessage> questionList = new ArrayList<>();
    ChatMessageAdapter chatMessageAdapter;

    String lastKey = "";
    Boolean allDataLoaded = false;

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

    @Override
    protected void onBitmapReceivedFromCamera(Bitmap bitmap, String path, Uri imageUri) {
        sendImageMessage(imageUri);
    }

    @Override
    protected void onBitmapReceivedFromGallery(Bitmap bitmap, String path, Uri imageUri) {
        sendImageMessage(imageUri);
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
        mMessageRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

//        initQuery();
        initQuery1();

//        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

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
//                        onCancelExtraClick();
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

    private void initQuery1() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        chatMessageAdapter = new ChatMessageAdapter(questionList, this, from_user_id);
        chatMessageAdapter.setLoadNextPageListener(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setAdapter(chatMessageAdapter);

        loadData();

    }

    private void loadData() {
        mFirebaseDatabaseReference.child(MESSAGES_CHILD_unique_users)
                .limitToLast(mPageLimit)
                .orderByKey()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            Toast.makeText(PaginatedChatActivity.this, "No more questions", Toast.LENGTH_SHORT).show();
                            currentPage--;
                        }
                        List<FriendlyMessage> tempList = new ArrayList<FriendlyMessage>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            FriendlyMessage question = data.getValue(FriendlyMessage.class);
                            question.setMsgID(data.getKey());
                            tempList.add(question);
//                            questionList.add(question);
                        }
                        if (tempList.size() > 0) {
                            lastKey = tempList.get(0).getMsgID();
                            Log.d("PARTHA_TAG:", " " + tempList.size() + " : " + lastKey);
                        }
                        Collections.reverse(tempList);
                        questionList.addAll(tempList);
                        chatMessageAdapter.notifyDataSetChanged();
                        scrollToBottom();
                        mProgressBar.setVisibility(RecyclerView.GONE);
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mProgressBar.setVisibility(RecyclerView.GONE);
                    }
                });
    }

    private void loadMoreData(int totalCurrentSize) {
        currentPage++;
        Log.d("PARTHA_TAG:", " " + totalCurrentSize + " : " + questionList.get(0).getMsgID() + " : " + questionList.get(totalCurrentSize - 1).getMsgID());
//        loadData();
        mProgressBar.setVisibility(RecyclerView.VISIBLE);
        mFirebaseDatabaseReference.child(MESSAGES_CHILD_unique_users)
                .limitToLast(mPageLimit)
                .endAt(questionList.get(totalCurrentSize - 1).getMsgID())
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            Toast.makeText(PaginatedChatActivity.this, "No more questions", Toast.LENGTH_SHORT).show();
                            currentPage--;
                        } else {
                            List<FriendlyMessage> tempList = new ArrayList<FriendlyMessage>();
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                FriendlyMessage question = data.getValue(FriendlyMessage.class);
                                question.setMsgID(data.getKey());
                                Log.d("PARTHA_TAG:", " " + question.getMsgID());

                                // To restrict duplicate data, as end at returns <= which includes the last data
                                if (!lastKey.equalsIgnoreCase(data.getKey())) {
                                    tempList.add(question);
                                }
//                            questionList.add(question);
                            }
                            if (!lastKey.equalsIgnoreCase(tempList.get(0).getMsgID())) {
                                lastKey = tempList.get(0).getMsgID();
                                Collections.reverse(tempList);
//                        questionList.addAll(0, tempList);
                                questionList.addAll(tempList);
                            } else {
                                allDataLoaded = true;
                            }
                            Log.d("PARTHA_TAG:", " " + tempList.size() + " : " + lastKey);
                            chatMessageAdapter.notifyDataSetChanged();
                            mProgressBar.setVisibility(RecyclerView.GONE);
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mProgressBar.setVisibility(RecyclerView.GONE);
                    }
                });
    }

    /*private boolean checkDuplicateKey(String nextChildKey) {
        if (questionList.size() > 0) {
            DataSnapshot previousSnapshot = questionList.get(questionList.size() - 1);
            String previousChildkey = previousSnapshot == null ? "" : previousSnapshot.getKey();
            return (!TextUtils.isEmpty(previousChildkey) && previousChildkey.equals(nextChildKey));
        }
        return false;
    }*/

    private void scrollToBottom() {
        int messageCount = chatMessageAdapter.getItemCount();
//        int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
        // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
        // to the bottom of the list to show the newly added message.
        /*if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
            mMessageRecyclerView.scrollToPosition(positionStart);
        }*/
        mMessageRecyclerView.scrollToPosition(messageCount - 1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_send:
                sendMessage();
                break;
            case R.id.img_share:
                onCallCameraButton();
                break;
            /*case R.id.img_cancel_extra:
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
                break;*/
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Logger.enter();
        /*prbLoading.setVisibility(View.GONE);
        mMessageRecyclerView.setVisibility(View.VISIBLE);*/
        Logger.exit();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Logger.enter();
        Logger.exit();
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
        storageReference.putFile(uri).addOnCompleteListener(PaginatedChatActivity.this,
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
                            Toast.makeText(PaginatedChatActivity.this, "Image upload task was not successful." + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendMessage() {
        FriendlyMessage friendlyMessage;
//        if (selectedMessageKey.equalsIgnoreCase("")) {
        friendlyMessage = new FriendlyMessage(from_user_id, to_user_id, mMessageEditText.getText().toString(),
                from_user_name,
                from_user_img, "", String.valueOf(System.currentTimeMillis()), false);
        mFirebaseDatabaseReference.child(MESSAGES_CHILD_unique_users).push().setValue(friendlyMessage);
        /*} else {
            friendlyMessage = new FriendlyMessage(from_user_id, to_user_id, mMessageEditText.getText().toString(),
                    from_user_name,
                    from_user_img, "", String.valueOf(System.currentTimeMillis()), true);
            userMessageDbReference.setValue(friendlyMessage);
        }*/
//        lastMessage = mMessageEditText.getText().toString().trim();
        mMessageEditText.setText("");
//        mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    @Override
    public void loadNextPageNow(int totalCurrentSize) {
        if (!allDataLoaded) {
            loadMoreData(totalCurrentSize);
        }
    }
}
