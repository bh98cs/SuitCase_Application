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
    //TAG for log on this Activity
    public static final String TAG = "MainActivity";
    //for intenting Firebase ID of an item
    public static final String EXTRA_ITEM_FIREBASE_ID = "daniel.southern.danielsouthern_cet343assignment.ITEM_FIREBASE_ID";

    //declare Sensor and SensorManager for detecting device shaking
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    //bool to track whether accelerometer is available
    private boolean isAccelerometerAvailable;
    //bool to track whether Accelerometer is detecting the first movements of the device
    private boolean notFirstTime = false;
    //variables for shake detection, tracking coordinates of the device
    private float currentX, currentY, currentZ, lastX, lastY, lastZ;
    private float xDifference, yDifference, zDifference;
    //variable to determine whether device has been shaken sufficiently
    private float shakeThreshold = 3f;
    //declare adapter for RecyclerView
    private myAdapter mAdapter;
    //get instance of FireStore to access saved images
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    //get collection reference for all itemUploads
    private CollectionReference announcementRef = database.collection("itemUploads");
    //declare instance of Firebase auth
    private FirebaseAuth mAuth;
    //instance of Firebase User to retrieve details of current user
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

        //set sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //check if accelerometer is available
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            //initialise sensor
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //set boolean to true to indicate sensor is available
            isAccelerometerAvailable = true;
            Log.d(TAG, "Accelerometer is available.");

        }
        else{
            Log.w(TAG, "Accelerometer is unavailable.");
            //set boolean to false to disable shake gesture
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
        //start adapter when activity starts
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //stop adapter when activity stops
        mAdapter.stopListening();
    }

    private void updateUI(FirebaseUser currentUser) {
        //send user to homepage if not already logged in
        if(currentUser == null){
            //current user is null therefore they are not logged in. Send them to homepage
            Intent intent = new Intent(this, HomePageActivity.class);
            startActivity(intent);
        }
        else{
            //user is logged in - call method to set up Recycler view
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
                                //sign user out
                                mAuth.signOut();
                                Log.i(TAG, "User Signed out");
                                //send user back to home page
                                Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing as user does not want to log out
                            }
                        });

        AlertDialog dialog = builder.create();
        //show alert dialog to request confirmation of user logging out
        dialog.show();

    }

    @Override
    public void onClick(View v) {
        //FAB  button to add a new item is clicked
        if(v.getId() == R.id.floatingActionButton_createItemUpload){
            //call method to handle user action
            createItemUploadClicked();
        }
        //logout is clicked
        else if (v.getId() == R.id.imageView_logoutIcon) {
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

        //set options for adapter
        FirestoreRecyclerOptions<ItemUpload> options = new FirestoreRecyclerOptions.Builder<ItemUpload>().setQuery(query,
                ItemUpload.class).build();

        //create adapter
        mAdapter = new myAdapter(options);

        //create recycler view
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        //ItemTouchHelper for gesture controls
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
                    //delete item from recyclerview and Firebase
                    mAdapter.deleteItem(position);
                    //call method to give user the option to undo the delete
                    optionToUndoDelete();


                }
                //user swipes right to edit
                else if (direction == 8) {
                    //send user to activity to edit item
                    Intent intent = new Intent(MainActivity.this, CreateOrEditActivity.class);
                    //get position of the item selected to edit
                    int position = viewHolder.getAdapterPosition();
                    //retrieve Firebase ID of item to edit using it's position
                    String itemFirebaseId = mAdapter.getItemFirebaseId(position);
                    //send Firebase ID of item to edit to new activity
                    intent.putExtra(EXTRA_ITEM_FIREBASE_ID , itemFirebaseId);
                    startActivity(intent);

                }


            }
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                //create background colors and icons to display when swiping items using RecyclerViewSwipeDecorator library
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        //add background color and icon for deleting
                        .addSwipeLeftBackgroundColor(MaterialColors.getColor(recyclerView,com.google.android.material.R.attr.colorError))
                        .addSwipeLeftActionIcon(R.drawable.baseline_delete).setSwipeLeftActionIconTint(MaterialColors.getColor(recyclerView,
                                com.google.android.material.R.attr.colorOnError))
                        //add background color and icon for editing
                        .addSwipeRightBackgroundColor(MaterialColors.getColor(recyclerView,
                                com.google.android.material.R.attr.colorPrimary))
                        .addSwipeRightActionIcon(R.drawable.baseline_edit).setSwipeRightActionIconTint(MaterialColors.getColor(recyclerView,
                                com.google.android.material.R.attr.colorOnPrimary))
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        //onclick listener for user selecting to delegate an item
        mAdapter.setOnItemDelegateClickListener(new myAdapter.OnItemClickListener() {
            @Override
            public void onItemDelegateClick(DocumentSnapshot documentSnapshot, int position) {
                //call method to delegate item via SMS
                sendSMS(documentSnapshot);
            }
        });

        //set onclick listener for long click on item
        mAdapter.setOnItemLongClickListener(new myAdapter.OnItemLongClickListener(){
            @Override
            public void onItemLongClick(DocumentSnapshot documentSnapshot, int position) {
                //call method to change bool indicating whether item has been bought
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
                        //call method to undo the deletion if user clicks to Undo
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
        //get details of item selected
        String itemTitle = documentSnapshot.getString("itemTitle");
        String itemLink = documentSnapshot.getString("itemLink");
        String itemDesc = documentSnapshot.getString("itemDesc");
        String itemPrice = documentSnapshot.getString("itemPrice");

        //check item details are not null
        if(itemTitle != null && itemLink != null && itemDesc != null && itemPrice != null){
            //create delegate message to send via SMS
            String smsMessage = createSMSMessage(itemTitle, itemDesc, itemPrice, itemLink);
            //TODO: Fix bug as app crashes when returned to from sending SMS
            //set up intent to send delegate message as SMS
            Intent sendSMSIntent = new Intent();
            sendSMSIntent.setAction(Intent.ACTION_SEND);
            sendSMSIntent.putExtra(Intent.EXTRA_TEXT, smsMessage);
            sendSMSIntent.setType("text/plain");

            startActivity(sendSMSIntent);
        }
        else{
            //user feedback to advise item must have a valid details
            Toast.makeText(MainActivity.this, "Item does not have sufficient details to send", Toast.LENGTH_LONG).show();
        }

    }

    private void changeIsBought(DocumentSnapshot  documentSnapshot, int position){
        //item from Firebase
        ItemUpload itemUpload = documentSnapshot.toObject(ItemUpload.class);
        if(itemUpload != null){
            //get original bool value
            boolean isItemBought = itemUpload.getItemBought();
            //call method in adapter to change item's bool value
            mAdapter.changeIsBought(isItemBought, position);
            //original bool value was false therefore is being changed to true
            if(!isItemBought){
                //user feedback
                Toast.makeText(this, itemUpload.getItemTitle() + " Marked as Bought", Toast.LENGTH_SHORT).show();
            }
            //original bool value was true therefore being changed to false
            else{
                //user feedback
                Toast.makeText(this, itemUpload.getItemTitle() + " Changed to not Bought", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            //user feedback incase itemUpload is null which means no item was clicked
            Toast.makeText(MainActivity.this, "Please click and hold an item to mark as bought.", Toast.LENGTH_SHORT).show();
        }
    }

    //method to undo a deletion of an item
    private void undoDelete() {
        //check if an item has been deleted in this session
        if(deletedItem == null){
            //user feedback to advise nothing to undo
            Toast.makeText(this, "Unable to Undo. No items deleted in this session.", Toast.LENGTH_SHORT).show();
            //return out of method
            return;
        }

        //add details from the stored item to a hashmap
        Map<String, Object> itemUpload = new HashMap<>();
        itemUpload.put("itemTitle", deletedItem.getItemTitle());
        itemUpload.put("itemDesc", deletedItem.getItemDesc());
        itemUpload.put("itemLink", deletedItem.getItemLink());
        itemUpload.put("itemPrice", deletedItem.getItemPrice());
        itemUpload.put("email", currentUser.getEmail());
        itemUpload.put("itemBought", deletedItem.getItemBought());
        itemUpload.put("imageDownloadUrl", deletedItem.getImageDownloadUrl());

        //re-upload deleted item to firebase
        database.collection("itemUploads")
                .add(itemUpload)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //user feedback to advise item deletion has been undone
                        Toast.makeText(MainActivity.this, "Undo Successful", Toast.LENGTH_SHORT).show();
                        //set deleted item back to null so that it can not be added back again
                        deletedItem = null;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //advise was unable to undo deletion
                        Toast.makeText(MainActivity.this, "Error Undoing Delete", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //adapter starts listening when activity resumes
        mAdapter.startListening();
        //set sensor event listener if accelerometer is available
        if(isAccelerometerAvailable){
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //adapter stops listening when activity is paused
        mAdapter.stopListening();
        //check if sensor was available
        if(isAccelerometerAvailable){
            //unregister the listener
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //store values of coordinates of device
        currentX = event.values[0];
        currentY = event.values[1];
        currentZ = event.values[2];

        //check this is not the first time device has detected movement
        if(notFirstTime){
            //get difference between last and current X, Y, Z values
            xDifference = Math.abs(lastX - currentX);
            yDifference = Math.abs(lastY - currentY);
            zDifference = Math.abs(lastZ = currentZ);

            //check that device has been shaken sufficiently
            if((xDifference > shakeThreshold && yDifference > shakeThreshold) ||
            (xDifference > shakeThreshold && zDifference > shakeThreshold) ||
                    (yDifference > shakeThreshold && zDifference > shakeThreshold)){
                //give option to undo deletion
                optionToUndoDelete();
            }
        }

        //store coordinates to reference again when another shake is detected
        lastX = currentX;
        lastY = currentY;
        lastZ = currentZ;

        //set bool to indicate this is not the first movements of the device
        notFirstTime = true;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //empty override as must implement this method
    }
}
