package daniel.southern.danielsouthern_cet343assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //TODO: Connect to firestore and load items into recycler view.
    public static final String DB_URL = "";
    public static final String TAG = "MainActivity";
    private myAdapter mAdapter;
    private List<ItemUpload> mItemUploads;
    private FirebaseFirestore database;
    private CollectionReference announcementRef = database.collection("savedItems");
    private Button logout;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //logout = findViewById(R.id.button_logout);
        //logout.setOnClickListener(this);

        setUpRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        //send user to login page if not already logged in
        if(currentUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else{
            //TODO:update UI with logged in user's items.
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
        /*if(v.getId() == R.id.button_logout){
            logout();
        }*/
    }


    private void setUpRecyclerView() {
    }

}
