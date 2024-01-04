package daniel.southern.danielsouthern_cet343assignment;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class CreateOrEditActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_UPDATE_POSITION = "daniel.southern.danielsouthern_cet343assignment.UPDATE_POSITION";
    public static final String TAG = "CreateOrEditActivity";
    //changed this from 2 to 1 -- undo if doesnt work
    public static final int RESULT_PICK_IMAGE = 1;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    // Create a Cloud Storage reference from the app
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private StorageReference fileReference;
    private Uri productImageUri;
    private String itemImageDownloadUrl;
    //initialise views
    EditText productTitle;
    EditText productDesc;
    EditText productLink;
    Button saveItem;
    Button cancelAction;
    Button loadImage;
    ImageView productImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_edit);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //retrieve current user to check if they're already logged in
        currentUser = mAuth.getCurrentUser();
        //check user is logged in before proceeding
        updateUI(currentUser);

        //storage reference to itemImages file in database
        storageRef = FirebaseStorage.getInstance().getReference("itemImages");

        //instantiate views
        productTitle = findViewById(R.id.editText_productTitle);
        productDesc = findViewById(R.id.editText_productDesc);
        productLink = findViewById(R.id.editText_productLink);

        productImageView = findViewById(R.id.imageView_productImage);
        productImageView.setOnClickListener(this);

        saveItem = findViewById(R.id.button_save);
        saveItem.setOnClickListener(this);

        cancelAction = findViewById(R.id.button_cancelAction);
        cancelAction.setOnClickListener(this);

        loadImage = findViewById(R.id.button_loadImage);
        loadImage.setOnClickListener(this);

        //get intent that started activity
        Intent intent = getIntent();
        int position = intent.getIntExtra(MainActivity.EXTRA_UPDATE_POSITION, -2);

        //check if intent to this activity passed a position
        if(position != -2){
            //position is not default value therefore a position of item to edit has been given
            loadItemDetails();
        }
    }

    private void loadItemDetails() {
        //TODO: retrieve itemUpload details and load them into views on this activity.....
        // This could be done by either intenting all features to this page and then intenting
        // them back with position to mainactivity -> then
        // call update method to apply intented features at the intented position
        // OR....
        // figure out how to update the FireStore DB from this class by retrieving the the
        // id and then updating. May need to figure out how to apply adapter to this class
        // OR.....
        // handle the edit features on the MainActivity by enabling input on the textviews
    }

    private void updateUI(FirebaseUser currentUser) {
        //send user to login page if not already logged in
        if(currentUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        //check which button has been clicked and call relative method
        if(v.getId() == R.id.button_save){
            saveItemClicked();
        } else if (v.getId() == R.id.button_cancelAction) {
            cancelActionClicked();
        } else if (v.getId() == R.id.button_loadImage || v.getId() == R.id.imageView_productImage) {
            selectImageFromAlbum();
        }
    }

    private void selectImageFromAlbum() {
        try{
            Intent intent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, RESULT_PICK_IMAGE);
        } catch (Exception e) {
            Log.e(TAG, "Error selecting image.", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check if result is for the pick image request and check data retrieved is not null
        if(requestCode == RESULT_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            productImageUri = data.getData();
            Picasso.get().load(productImageUri).into(productImageView);
        }else{
            Log.e(TAG, "Error retrieving image from gallery.");
        }
    }

    private void cancelActionClicked() {
        //AlertDialog to request confirmation from user before discarding changes made on this activity
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Changes")
                .setMessage("Are you sure you wish to cancel? All changes will be discarded.")
                .setPositiveButton("Discard Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //return user to main activity
                        Intent intent = new Intent(CreateOrEditActivity.this, MainActivity.class);
                        startActivity(intent);
                   }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing as user wants to continue on this activity
                    }
                });

        AlertDialog ad = builder.create();
        ad.show();

    }

    private void saveItemClicked() {
        //save image first to retrieve download Url
        if(productImageUri != null){
            //create new path for image file using current time in milliseconds as a unique ID
            fileReference = storageRef.child(System.currentTimeMillis() + "."
                    + getFileExtension(productImageUri));
            //try save the file to FireBase
            fileReference.putFile(productImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Successfully saved image to FireBase
                            Log.d(TAG, "onSuccess: Image uploaded");
                            //retrieve download Url for image to save with item details in FireStore DB
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //store download Url in a string to save to FireStore
                                    itemImageDownloadUrl = uri.toString();
                                    Log.d(TAG, "onSuccess: Successfully retrieved download Url: " + itemImageDownloadUrl);
                                    //once successfully retrieved download Url system is ready to save item to FireBase
                                    saveItemToDataBase();
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "onFailure: Failed to retrieve download Url.", e);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: Unable to upload Image", e);
                        }
                    });
        }else{
            Toast.makeText(this, "Please provide an Image for the item.", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveItemToDataBase() {
        String title = productTitle.getText().toString().trim();
        String desc = productDesc.getText().toString().trim();
        String link = productLink.getText().toString().trim();

        //get reference to database
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        //create new ItemUpload
        Map<String, Object> itemUpload = new HashMap<>();
        itemUpload.put("itemTitle", title);
        itemUpload.put("itemDesc", desc);
        itemUpload.put("itemLink", link);
        itemUpload.put("email", currentUser.getEmail());
        itemUpload.put("imageDownloadUrl", itemImageDownloadUrl);
        //set item bought to false when created
        itemUpload.put("itemBought", false);

        //add a new document with generated ID
        database.collection("itemUploads")
                .add(itemUpload)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FSLog", "DocumentSnapshot added with ID: " + documentReference.getId());
                        Log.d(TAG, "Item added with Url: " + itemUpload.get("imageDownloadUrl"));
                        //user feedback to confirm new item has been added
                        Toast.makeText(CreateOrEditActivity.this, "New Item added!", Toast.LENGTH_LONG).show();
                        //return back to main activity to view all Items
                        Intent intent = new Intent(CreateOrEditActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FSLog", "Error adding document", e);
                        //user feedback to advise was unable to save new item
                        Toast.makeText(CreateOrEditActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                });

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}