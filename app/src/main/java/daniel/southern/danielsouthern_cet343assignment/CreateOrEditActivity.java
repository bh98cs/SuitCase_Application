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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class CreateOrEditActivity extends AppCompatActivity implements View.OnClickListener {

    //tag for logs on this activity
    public static final String TAG = "CreateOrEditActivity";
    //declare instance of FireBase auth to retrieve user details
    private FirebaseAuth mAuth;
    //declare instance to store current user details
    private FirebaseUser currentUser;
    //declare instances of FireBase database references for uploading data to
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private StorageReference storageRef;
    private StorageReference fileReference;
    //reference for the item selected for editing
    private DocumentReference editItemRef;

    //variables for viewing and saving item image
    private Uri productImageUri;
    private String itemImageDownloadUrl;
    public static final int RESULT_PICK_IMAGE = 1;

    //variables to store Item details for item being edited
    private String editItemTitle;
    private String editItemDesc;
    private String editItemLink;
    private String editItemPrice;
    private String editItemImageUrl;

    //boolean to track whether user is editing an existing item
    private boolean editItem;
    //initialise views
    EditText productTitle;
    EditText productDesc;
    EditText productLink;
    EditText productPrice;
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
        productPrice = findViewById(R.id.editText_productPrice);

        //set on click listeners for activity
        productImageView = findViewById(R.id.imageView_productImage);
        productImageView.setOnClickListener(this);

        saveItem = findViewById(R.id.button_save);
        saveItem.setOnClickListener(this);

        cancelAction = findViewById(R.id.button_cancelAction);
        cancelAction.setOnClickListener(this);

        loadImage = findViewById(R.id.button_loadImage);
        loadImage.setOnClickListener(this);

        //set edit item bool to false
        editItem = false;

        //get intent that started activity
        Intent intent = getIntent();
        //store document reference for item being edited
        String firebaseDocId = intent.getStringExtra(MainActivity.EXTRA_ITEM_FIREBASE_ID);

        //check if intent to this activity passed a document ID
        if(firebaseDocId != null){
            //set edit item bool to true as intent sent Document ID to edit
            editItem = true;
            //retrieve item from firebase using ID intented from MainActivity
            editItemRef = database.collection("itemUploads").document(firebaseDocId);
            //position is not default value therefore a position of item to edit has been given
            loadItemDetails();
        }
    }

    //method to load item details if user has selected to edit an item
    private void loadItemDetails() {
        editItemRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //check if item has been successfully retrieved
                if(task.isSuccessful()){
                    //get result of task
                    DocumentSnapshot document = task.getResult();
                    //check if the item being edited exists within the database
                    if(document.exists()){
                        //document retrieved successfully
                        Log.i(TAG, "FireBase Document retrieved!");
                        //retrieve data stored in Firebase
                        editItemTitle = document.getString("itemTitle");
                        editItemDesc = document.getString("itemDesc");
                        editItemLink = document.getString("itemLink");
                        editItemPrice = document.getString("itemPrice");
                        editItemImageUrl = document.getString("imageDownloadUrl");

                        //load data into views
                        productTitle.setText(editItemTitle);
                        productDesc.setText(editItemDesc);
                        productLink.setText(editItemLink);
                        productPrice.setText(editItemPrice);

                        //load image into image view using Picasso library
                        Picasso.get()
                                .load(editItemImageUrl)
                                .fit()
                                .centerCrop()
                                .into(productImageView);
                    }
                    else{
                        //document is not in FireStore database (this should not occur)
                        Log.w(TAG, "Document does not exist.");
                    }
                }else {
                    //unable to retrieve data from Firebase Database
                    Log.e(TAG, "Failed to retrieve FireBase Document.", task.getException());
                    Toast.makeText(CreateOrEditActivity.this, "Unable to retrieve data.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        //save button has been clicked
        if(v.getId() == R.id.button_save){

            //ensure user has provided a title and price as a minimum
            if(!productTitle.getText().toString().trim().equals("") && !productPrice.getText().toString().trim().equals("")
            && !productTitle.getText().toString().trim().equals(null) && !productPrice.getText().toString().trim().equals(null)){
                //call method to save item details to database
                saveItem();
            }
            else{
                //user feedback to advise of required fields
                Toast.makeText(this, "Please provide a title and price for the item.", Toast.LENGTH_SHORT).show();
            }

        }
        //cancel button clicked
        else if (v.getId() == R.id.button_cancelAction) {
            //call method to handle cancel button being clicked
            cancelActionClicked();
        }
        //load image button or imageview clicked
        else if (v.getId() == R.id.button_loadImage || v.getId() == R.id.imageView_productImage) {
            //method to allow user to select new image for item
            selectImageFromAlbum();
        }
    }

    private void updateItem() {

        //create new hash map
        Map<String, Object> editedItem = new HashMap<>();

        //retrieve user input from views
        String title = productTitle.getText().toString().trim();
        String desc = productDesc.getText().toString().trim();
        String link = productLink.getText().toString().trim();
        String price = productPrice.getText().toString().trim();

        //add user input to hash map
        editedItem.put("itemTitle", title);
        editedItem.put("itemDesc", desc);
        editedItem.put("itemLink", link);
        editedItem.put("itemPrice", price);
        //check if the image has been changed
        if(itemImageDownloadUrl != null){
            //save new image url
            editedItem.put("imageDownloadUrl", itemImageDownloadUrl);
        }

        //update document with details in hash map
        editItemRef.update(editedItem)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //user feedback to confirm item successfully updated
                        Toast.makeText(CreateOrEditActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        //return user to main screen
                        Intent intent = new Intent(CreateOrEditActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //user feedback to advise update was unsuccessful
                        Toast.makeText(CreateOrEditActivity.this, "Unable to Update details", Toast.LENGTH_SHORT).show();
                        //log the error causing update to fail
                        Log.e(TAG, "onFailure: Unable to update FireBase Document", e);
                    }
                });

    }

    private void selectImageFromAlbum() {
        try{
            //redirect user to their photo album to select and image
            Intent intent = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
            //get result from users photo album
            startActivityForResult(intent, RESULT_PICK_IMAGE);
        } catch (Exception e) {
            //print error in log if user cannot be directed to their saved photos
            Log.e(TAG, "Error selecting image.", e);
            Toast.makeText(this, "Unable to access device's photos.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check if result is for the pick image request and check data retrieved is not null
        if(requestCode == RESULT_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            //get Uri of image selected from device's files
            productImageUri = data.getData();
            //load image into imageView using Picasso library
            Picasso.get().load(productImageUri).into(productImageView);
        }else{
            //log error if unable to load image into ImageView
            Log.e(TAG, "Error retrieving image from gallery.");
            Toast.makeText(this, "Unable to retrieve image from Device.", Toast.LENGTH_SHORT).show();
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

    //TODO: clean up this code as can separate saving image into it's own method
    private void saveItem() {
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
                            //TODO: add spinner for user feedback when saving image
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //store download Url in a string to save to FireStore
                                    itemImageDownloadUrl = uri.toString();
                                    Log.d(TAG, "onSuccess: Successfully retrieved download Url: " + itemImageDownloadUrl);
                                    //once successfully retrieved download Url system is ready to save item to FireBase
                                    if(editItem){
                                        //update existing item
                                        updateItem();
                                    }
                                    else{
                                        //create new item
                                        saveItemToDataBase();
                                    }
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //log error if unable to retrieve download Url
                                            Log.e(TAG, "onFailure: Failed to retrieve download Url.", e);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //log error if unable to upload image to FireStore
                            Log.e(TAG, "onFailure: Unable to upload Image", e);
                        }
                    });
        }
        //user is editing item but productImageUri therefore have not changed the image
        else if (editItem) {
            //update the item details
            updateItem();
        }
        //no image has been given
        else{
            Toast.makeText(this, "Please provide an Image for the item.", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveItemToDataBase() {
        //store user input in string variables
        String title = productTitle.getText().toString().trim();
        String desc = productDesc.getText().toString().trim();
        String link = productLink.getText().toString().trim();
        String price = productPrice.getText().toString().trim();

        //create new ItemUpload with user's input
        Map<String, Object> itemUpload = new HashMap<>();
        itemUpload.put("itemTitle", title);
        itemUpload.put("itemDesc", desc);
        itemUpload.put("itemLink", link);
        itemUpload.put("itemPrice", price);
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
                        //successfully added new item to database
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
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
                        Log.w(TAG, "Error adding document", e);
                        //user feedback to advise was unable to save new item
                        Toast.makeText(CreateOrEditActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                });

    }

    //method to retrieve file extension for an image
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}