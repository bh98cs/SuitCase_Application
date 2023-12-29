package daniel.southern.danielsouthern_cet343assignment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //TODO: Connect to firestore and load items into recycler view.
    public static final String TAG = "MainActivity";
    private myAdapter mAdapter;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference announcementRef = database.collection("itemUploads");
    private FirebaseAuth mAuth;
    private FloatingActionButton createItemUpload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize FAB and set OnClickListener
        createItemUpload = findViewById(R.id.floatingActionButton_createItemUpload);
        createItemUpload.setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //retrieve current user to check if they're already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //update UI depending on whether user is logged in
        updateUI(currentUser);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();

    }

    private void updateUI(FirebaseUser currentUser) {
        //send user to login page if not already logged in
        if(currentUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else{
            setUpRecyclerView();
        }
    }

    private void logout(){
        //TODO: Add requirement for user to confirm sign out
        mAuth.signOut();
        Log.i(TAG, "User Signed out");
        //send user back to login page
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        //check which button has been clicked and call the relevant method
        if(v.getId() == R.id.floatingActionButton_createItemUpload){
            createItemUploadClicked();
        }
    }

    private void createItemUploadClicked() {
        //redirect to activity to create new item
        Intent intent = new Intent(this, CreateOrEditActivity.class);
        startActivity(intent);
    }


    private void setUpRecyclerView() {
        //retrieve data with the same associated email as current user
        Query query = announcementRef.whereEqualTo("email", mAuth.getCurrentUser().getEmail());

        FirestoreRecyclerOptions<ItemUpload> options = new FirestoreRecyclerOptions.Builder<ItemUpload>().setQuery(query,ItemUpload.class).build();

        mAdapter = new myAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction == 4){
                    mAdapter.deleteItem(viewHolder.getAdapterPosition());
                } else if (direction == 8) {
                    mAdapter.updateItem(viewHolder.getAdapterPosition());
                }


            }
        }).attachToRecyclerView(recyclerView);

        mAdapter.setOnItemClickListener(new myAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                ItemUpload itemUpload = documentSnapshot.toObject(ItemUpload.class);
                String id = documentSnapshot.getId();
                Toast.makeText(MainActivity.this,
                        "Position: " + position + " ID: " + id, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
