package partha.firebasechatdemo.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import partha.firebasechatdemo.R;
import partha.firebasechatdemo.adapter.ChatListAdapter;
import partha.firebasechatdemo.firebaseChat.User;
import partha.firebasechatdemo.utils.SimpleDividerItemDecoration;

/**
 * Created by DAT-Asset-110 on 03-08-2017.
 */

public class ChatListFragment extends Fragment {
    private static final String TAG = "FriendList" ;
    private DatabaseReference chatListReference;
    private ValueEventListener mUserListListener;
    ArrayList<String> chatUserNamelist = new ArrayList<>();
    ArrayList<User> chatUserList = new ArrayList<>();
    ChatListAdapter chatListAdapter;

    private RecyclerView mRecyclerView;
    private FirebaseAuth mFirebaseAuth;
    User currentUser;
    private ProgressDialog pDialog;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.activity_recent_message, container, false);

        initFields(view);

        return view;
    }

    private void initFields(View view) {
        pDialog = new ProgressDialog(getActivity());

        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser()!=null){
            currentUser = new User(mFirebaseAuth.getCurrentUser().getDisplayName(),
                    mFirebaseAuth.getCurrentUser().getPhotoUrl().toString(),
                    mFirebaseAuth.getCurrentUser().getEmail(),
                    mFirebaseAuth.getCurrentUser().getUid(), mFirebaseAuth.getCurrentUser().getProviderId());
        }
        chatListReference = FirebaseDatabase.getInstance().getReference().child("users");
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        chatListAdapter = new ChatListAdapter(getActivity(), chatUserList, currentUser);
        mRecyclerView.setAdapter(chatListAdapter);
        chatListAdapter.notifyDataSetChanged();

        // Loader
        pDialog.setMessage(getString(R.string.dialog_msg));
        pDialog.setCancelable(false);
        pDialog.show();

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                chatUserNamelist.clear();
                chatUserList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    User user = snapshot.getValue(User.class);
                    if (!checkCurrentUser(user)) {
                        chatUserList.add(user);
                        chatUserNamelist.add(user.getName());
                    }
                    System.out.println(user.getName());
                }
                Log.i(TAG, "onDataChange2: "+ chatUserNamelist.toString());
                chatListAdapter.notifyDataSetChanged();

                if (pDialog!=null && pDialog.isShowing()){
                    pDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: ",databaseError.toException());
                Toast.makeText(getActivity(), "Failed to load list.",
                        Toast.LENGTH_SHORT).show();

                if (pDialog!=null && pDialog.isShowing()){
                    pDialog.dismiss();
                }
            }
        };
        chatListReference.addValueEventListener(userListener);
        // will be called only once. wont be called again for data changed
//        chatListReference.addListenerForSingleValueEvent(userListener);

        mUserListListener = userListener;
    }

    private boolean checkCurrentUser(User user) {
        if (mFirebaseAuth.getCurrentUser()!=null){
            if (user.getEmail().equalsIgnoreCase(mFirebaseAuth.getCurrentUser().getEmail())){
                return true;
            }
        }
        return false;
    }
}
