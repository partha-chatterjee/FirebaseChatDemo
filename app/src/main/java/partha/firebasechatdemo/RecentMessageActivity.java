package partha.firebasechatdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import partha.firebasechatdemo.adapter.RecentMessageAdapter;
import partha.firebasechatdemo.firebaseChat.User;
import partha.firebasechatdemo.utils.SimpleDividerItemDecoration;

public class RecentMessageActivity extends AppCompatActivity {

    private static final String TAG = "UserList" ;
    private DatabaseReference userlistReference;
    private ValueEventListener mUserListListener;
    ArrayList<String> usernamelist = new ArrayList<>();
    ArrayList<User> userList = new ArrayList<>();
    RecentMessageAdapter recentMessageAdapter;

    private RecyclerView mRecyclerView;
    private FirebaseAuth mFirebaseAuth;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_message);

        initFields();
    }

    private void initFields() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth.getCurrentUser()!=null){
            currentUser = new User(mFirebaseAuth.getCurrentUser().getDisplayName(),
                    mFirebaseAuth.getCurrentUser().getPhotoUrl().toString(),
                    mFirebaseAuth.getCurrentUser().getEmail(),
                    mFirebaseAuth.getCurrentUser().getUid(), mFirebaseAuth.getCurrentUser().getProviderId());
        }
        userlistReference = FirebaseDatabase.getInstance().getReference().child("users");
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        recentMessageAdapter = new RecentMessageAdapter(this, userList, currentUser);
        mRecyclerView.setAdapter(recentMessageAdapter);
        recentMessageAdapter.notifyDataSetChanged();

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                usernamelist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    User user = snapshot.getValue(User.class);
                    if (!checkCurrentUser(user)) {
                        userList.add(user);
                        usernamelist.add(user.getName());
                    }
                    System.out.println(user.getName());
                }
                Log.i(TAG, "onDataChange: "+usernamelist.toString());
                recentMessageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: ",databaseError.toException());
                Toast.makeText(RecentMessageActivity.this, "Failed to load User list.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        userlistReference.addValueEventListener(userListener);
//        userlistReference.removeEventListener(userListener);

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

    /*public String usernameOfCurrentUser()
    {
        String email = MainActivity.mAuth.getCurrentUser().getEmail();
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }*/

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mUserListListener != null) {
            userlistReference.removeEventListener(mUserListListener);
        }

    }
}
