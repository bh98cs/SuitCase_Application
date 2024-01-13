package daniel.southern.danielsouthern_cet343assignment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.color.MaterialColors;
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

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    public static final String TAG = "MainActivity";
    public static final String EXTRA_ITEM_FIREBASE_ID = "daniel.southern.danielsouthern_cet343assignment.ITEM_FIREBASE_ID";

    //for detecting device shaking
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    //bool to track whether accelerometer is available
    private boolean isAccelerometerAvailable;
    private boolean notFirstTime = false;
    //variables for shake detection
    private float currentX, currentY, currentZ, lastX, lastY, lastZ;
    private float xDifference, yDifference, zDifference;
    //variable to determine whether device has been shaken
    private float shakeThreshold = 3f;
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

    //initialise toolbar
    private Toolbar toolbar;
    //initialise logout button on toolbar
    private ImageView logoutIcon;
    //to store most recently deleted item incase user wishes to undo delete
    private ItemUpload deletedItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //check if sensor is available
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerAvailable = true;
            Log.d(TAG, "Accelerometer is available.");

        }
        else{
            Log.w(TAG, "Accelerometer is unavailable.");
            isAccelerometerAvailable = false;
        }

        // Initialize FAB and set OnClickListener
        createItemUpload = findViewById(R.id.floatingActionButton_createItemUpload);
        createItemUpload.setOnClickListener(this);

        // set tool bar as the action bar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //set logout icon on click listener
        logoutIcon = findViewById(R.id.imageView_logoutIcon);
        logoutIcon.setOnClickListener(this);

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
        //send user to homepage if not already logged in
        if(currentUser == null){
            Intent intent = new Intent(this, HomePageActivity.class);
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
        } else if (v.getId() == R.id.imageView_logoutIcon) {
            //call logout method
            logout();
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
                    //store the deleted item incase user wants to undo delete
                    deletedItem = mAdapter.getItem(position);
                    //delete item from recyclerview and FireStore
                    mAdapter.deleteItem(position);

                    optionToUndoDelete();


                } else if (direction == 8) {
                    //user swipes right to edit
                    Intent intent = new Intent(MainActivity.this, CreateOrEditActivity.class);
                    int position = viewHolder.getAdapterPosition();
                    String itemFirebaseId = mAdapter.getItemFirebaseId(position);
                    intent.putExtra(EXTRA_ITEM_FIREBASE_ID , itemFirebaseId);
                    startActivity(intent);

                }


            }
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                //create background colors and icons to display when swiping items using RecyclerViewSwipeDecorator library
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(MaterialColors.getColor(recyclerView,com.google.android.material.R.attr.colorError))
                        .addSwipeLeftActionIcon(R.drawable.baseline_delete).setSwipeLeftActionIconTint(MaterialColors.getColor(recyclerView, com.google.android.material.R.attr.colorOnError))
                        .addSwipeRightBackgroundColor(MaterialColors.getColor(recyclerView, com.google.android.material.R.attr.colorTertiary))
                        .addSwipeRightActionIcon(R.drawable.baseline_edit).setSwipeRightActionIconTint(MaterialColors.getColor(recyclerView, com.google.android.material.R.attr.colorOnTertiary))
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        mAdapter.setOnItemDelegateClickListener(new myAdapter.OnItemClickListener() {
            @Override
            public void onItemDelegateClick(DocumentSnapshot documentSnapshot, int position) {
                sendSMS(documentSnapshot);
            }
        });

        mAdapter.setOnItemLongClickListener(new myAdapter.OnItemLongClickListener(){
            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot, int position) {
                //change item's isBought boolean
                changeIsBought(documentSnapshot, position);
            }
        });
    }

    private void optionToUndoDelete() {
        RelativeLayout layout = findViewById(R.id.activity_main_layout);
        //create Snackbar to provide user feedback and give option to undo deletion
        Snackbar snackbar = Snackbar.make(layout, "Undo Delete", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //undo the deletion
                        undoDelete();
                    }
                });
        snackbar.show();
    }

    private String createSMSMessage(String itemTitle, String itemDesc, String itemPrice, String itemLink) {
        //variable to store the template message in
        String message;
        //check if link saved on Firebase document is empty
        if(itemLink != ""){
            //create SMS template message containing link
            message = "Hi!\n\nHere's the link to purchase the " + itemTitle + " needed for our " +
                    "holiday.\n\n" + itemLink + "\n\nProduct Description: " + itemDesc + "\n\nProduct Price: £" + itemPrice
                    + "\n\n(This message was sent through Suitcase)";
        }
        else{
            //template message without a link
            message = "Hi!\n\nPlease can you purchase the " + itemTitle + " needed for our " +
                    "holiday.\n\nProduct Description: " + itemDesc + "\n\nProduct Price: £" + itemPrice
                    + "\n\n(This message was sent through Suitcase)";
        }
        //return SMS template message
        return message;
    }

    //method to delegate item through SMS
    private void sendSMS(DocumentSnapshot documentSnapshot){
        //get title and link of item selected
        String itemTitle = documentSnapshot.getString("itemTitle");
        String itemLink = documentSnapshot.getString("itemLink");
        String itemDesc = documentSnapshot.getString("itemDesc");
        String itemPrice = documentSnapshot.getString("itemPrice");

        //check item title and link are not null
        if(itemTitle != null && itemLink != null){
            String smsMessage = createSMSMessage(itemTitle, itemDesc, itemPrice, itemLink);
            //TODO: Fix bug as app crashes when returned to from sending SMS
            //set up intent to send item as SMS
            Intent sendSMSIntent = new Intent();
            sendSMSIntent.setAction(Intent.ACTION_SEND);
            sendSMSIntent.putExtra(Intent.EXTRA_TEXT, smsMessage);
            sendSMSIntent.setType("text/plain");

            startActivity(sendSMSIntent);
        }
        else{
            //user feedback to advise item must have a valid title and link
            Toast.makeText(MainActivity.this, "Please select an item with a valid title and link.", Toast.LENGTH_LONG).show();
        }

    }

    private void changeIsBought(DocumentSnapshot  documentSnapshot, int position){

        ItemUpload itemUpload = documentSnapshot.toObject(ItemUpload.class);
        if(itemUpload != null){
            //get original bool value
            boolean isItemBought = itemUpload.getItemBought();
            mAdapter.changeIsBought(isItemBought, position);
            //original bool value was false therefore is being changed to true
            if(!isItemBought){
                Toast.makeText(this, itemUpload.getItemTitle() + " Marked as Bought", Toast.LENGTH_SHORT).show();
            }
            //original bool value was true therefore being changed to false
            else{
                Toast.makeText(this, itemUpload.getItemTitle() + " Changed to not Bought", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            //user feedback incase itemUpload is null which means no item was clicked
            Toast.makeText(MainActivity.this, "Please click an item to mark as bought.", Toast.LENGTH_SHORT).show();
        }
    }

    //method to undo a deletion of an item
    private void undoDelete() {
        //check if an item has been deleted in this session
        if(deletedItem == null){
            Toast.makeText(this, "Unable to Undo. No items deleted in this session.", Toast.LENGTH_SHORT).show();
            return;
        }

        //re-upload the item to FireStore
        Map<String, Object> itemUpload = new HashMap<>();
        itemUpload.put("itemTitle", deletedItem.getItemTitle());
        itemUpload.put("itemDesc", deletedItem.getItemDesc());
        itemUpload.put("itemLink", deletedItem.getItemLink());
        itemUpload.put("itemPrice", deletedItem.getItemPrice());
        itemUpload.put("email", currentUser.getEmail());
        itemUpload.put("itemBought", deletedItem.getItemBought());
        itemUpload.put("imageDownloadUrl", deletedItem.getImageDownloadUrl());

        database.collection("itemUploads")
                .add(itemUpload)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(MainActivity.this, "Undo Successful", Toast.LENGTH_SHORT).show();
                        //set deleted item back to null so that it can not be add back again
                        deletedItem = null;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error Undoing Delete", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //set sensor event listener if accelerometer is available
        if(isAccelerometerAvailable){
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //check if sensor was available
        if(isAccelerometerAvailable){
            //unregister the listener
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //
        currentX = event.values[0];
        currentY = event.values[1];
        currentZ = event.values[2];

        if(notFirstTime){
            //get difference between last and current X, Y, Z values
            xDifference = Math.abs(lastX - currentX);
            yDifference = Math.abs(lastY - currentY);
            zDifference = Math.abs(lastZ = currentZ);

            //check that device has been shaken sufficiently
            if((xDifference > shakeThreshold && yDifference > shakeThreshold) ||
            (xDifference > shakeThreshold && zDifference > shakeThreshold) ||
                    (yDifference > shakeThreshold && zDifference > shakeThreshold)){
                optionToUndoDelete();
            }
        }

        lastX = currentX;
        lastY = currentY;
        lastZ = currentZ;

        notFirstTime = true;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
