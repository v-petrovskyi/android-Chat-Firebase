package training.android.chatfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private static final int RC_PROFILE_PHOTO_PICKER = 567;
    private static final String TAG = "UserListActivity";
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;

    private FirebaseAuth auth;

    private List<User> userList;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager userLayoutManager;

    private FirebaseStorage storage;
    private StorageReference profilePhotosStorageReference;
    private String childOfCurrentUserInDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        auth = FirebaseAuth.getInstance();

        userList = new ArrayList<>();

        storage = FirebaseStorage.getInstance();
        profilePhotosStorageReference = storage.getReference().child("avatars");


        attachUserDatabaseReferenceListener();
        buildRecyclerView();
    }

    private void attachUserDatabaseReferenceListener() {
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if (usersChildEventListener == null) {
            usersChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if (!user.getId().equals(auth.getCurrentUser().getUid())) {
                        userList.add(user);
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "else in onChildAdded");
                        childOfCurrentUserInDatabase = snapshot.getKey();
                        Log.d(TAG, "onChildAdded: " + childOfCurrentUserInDatabase);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            usersDatabaseReference.addChildEventListener(usersChildEventListener);
        }
    }

    private void buildRecyclerView() {
        userRecyclerView = findViewById(R.id.userListRecyclerView);
        userRecyclerView.setHasFixedSize(true);
        userLayoutManager = new LinearLayoutManager(this);
        userAdapter = new UserAdapter(userList);

        userRecyclerView.setLayoutManager(userLayoutManager);
        userRecyclerView.setAdapter(userAdapter);
        userRecyclerView.addItemDecoration(new DividerItemDecoration(userRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));
        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                goToChat(position);
            }
        });
    }

    private void goToChat(int position) {
        Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
        intent.putExtra("recipientUserId", userList.get(position).getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.sign_out) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UserListActivity.this, SignInActivity.class));
            return true;
        } else if (item.getItemId() == R.id.addProfilePhoto) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Chose an image"), RC_PROFILE_PHOTO_PICKER);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PROFILE_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            StorageReference imageReference = profilePhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            UploadTask uploadTask = imageReference.putFile(selectedImageUri);

            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    Log.d(TAG, imageReference.getDownloadUrl().toString());
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("avatarURL", downloadUri.toString());
                        Log.d(TAG, childOfCurrentUserInDatabase);
                        usersDatabaseReference.child(childOfCurrentUserInDatabase).updateChildren(updates);
                    } else {
                        Log.d(TAG, "addOnCompleteListener: task is not successful");
                    }
                }
            });
        }
    }
}