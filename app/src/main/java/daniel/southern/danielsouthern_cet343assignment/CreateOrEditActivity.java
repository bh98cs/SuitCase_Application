package daniel.southern.danielsouthern_cet343assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateOrEditActivity extends AppCompatActivity implements View.OnClickListener {

    EditText productTitle;
    EditText productDesc;
    EditText productLink;
    Button saveItem;
    Button cancelAction;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

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

        //instantiate views
        productTitle = findViewById(R.id.editText_productTitle);
        productDesc = findViewById(R.id.editText_productDesc);
        productLink = findViewById(R.id.editText_productLink);

        saveItem = findViewById(R.id.button_save);
        saveItem.setOnClickListener(this);

        cancelAction = findViewById(R.id.button_cancelAction);
        cancelAction.setOnClickListener(this);
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
        //set item bought to false when created
        itemUpload.put("itemBought", false);

        //add a new document with generated ID
        database.collection("itemUploads")
                .add(itemUpload)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FSLog", "DocumentSnapshot added with ID: " + documentReference.getId());
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
}