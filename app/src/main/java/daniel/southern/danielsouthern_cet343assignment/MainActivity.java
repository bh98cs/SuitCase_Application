package daniel.southern.danielsouthern_cet343assignment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //TODO: Add red background to cardview when swiping to delete and add different colour when editing
    //TODO: Potentially use coding in flow's tutorial to add search function to recyclerview
    public static final String TAG = "MainActivity";
    public static final String EXTRA_ITEM_FIREBASE_ID = "daniel.southern.danielsouthern_cet343assignment.ITEM_FIREBASE_ID ";
    private myAdapter mAdapter;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference announcementRef = database.collection("itemUploads");
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    // Create a Cloud Storage reference from the app
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    //initialise FAB
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
        currentUser = mAuth.getCurrentUser();
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
        //request confirmation to sign out
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Confirm Logout")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAuth.signOut();
                                Log.i(TAG, "User Signed out");
                                //send user back to login page
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.show();

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
                //user swipes left to delete
                if(direction == 4){
                    RelativeLayout relativeLayout = findViewById(R.id.activity_main_layout);
                    //store the position of the item in a local variable to use for deleting item and
                    //potentially undoing deletion
                    int position = viewHolder.getAdapterPosition();
                    //store the deleted item in a local variable incase user wants to undo delete
                    ItemUpload deletedItem = mAdapter.getItem(position);
                    //delete item from recyclerview and FireStore
                    mAdapter.deleteItem(position);
                    //create Snackbar to provide user feedback and give option to undo deletion
                    Snackbar snackbar = Snackbar.make(relativeLayout, "Item Deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //undo the deletion
                                    undoDelete(deletedItem);
                                }
                            });
                    snackbar.show();
                } else if (direction == 8) {
                    //mAdapter.updateItem(viewHolder.getAdapterPosition());
                    //user swipes right to edit
                    Intent intent = new Intent(MainActivity.this, CreateOrEditActivity.class);
                    int position = viewHolder.getAdapterPosition();
                    String itemFirebaseId = mAdapter.getItemFirebaseId(position);
                    intent.putExtra(EXTRA_ITEM_FIREBASE_ID , itemFirebaseId);
                    startActivity(intent);

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

        mAdapter.setOnItemLongClickListener(new myAdapter.OnItemLongClickListener(){
            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot, int position) {
                ItemUpload itemUpload = documentSnapshot.toObject(ItemUpload.class);
                String id = documentSnapshot.getId();
                Toast.makeText(MainActivity.this,
                        "Long click", Toast.LENGTH_SHORT).show();
                if(itemUpload != null){
                    mAdapter.changeIsBought(itemUpload.getItemBought(), position);
                }
                else{
                    //user feedback incase itemUpload is null which means no item was clicked
                    Toast.makeText(MainActivity.this, "Please click an item to mark as bought.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //method to undo a deletion of an item
    private void undoDelete(ItemUpload deletedItem) {
        //re-upload the item to FireStore
        Map<String, Object> itemUpload = new HashMap<>();
        itemUpload.put("itemTitle", deletedItem.getItemTitle());
        itemUpload.put("itemDesc", deletedItem.getItemDesc());
        itemUpload.put("itemLink", deletedItem.getItemLink());
        itemUpload.put("email", currentUser.getEmail());
        itemUpload.put("itemBought", deletedItem.getItemBought());
        itemUpload.put("imageDownloadUrl", deletedItem.getImageDownloadUrl());

        database.collection("itemUploads")
                .add(itemUpload)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(MainActivity.this, "Undo Successful", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error Undoing Delete", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
