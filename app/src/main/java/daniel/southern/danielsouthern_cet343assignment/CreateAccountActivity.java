package daniel.southern.danielsouthern_cet343assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener{
    //TODO: follow Coding in Flow's tutorial for validating email and password
    //TODO: Follow coding in Flow's TextWatcher tutorial for disabling buttons

    //constant variable for sending contents of email address field to LoginActivity
    public static final String EXTRA_EMAIL_ADDRESS = "daniel.southern.danielsouthern_cet343assignment.EXTRA_EMAIL_ADDRESS";
    //tag for logs
    public static final String TAG = "CreateAccountActivity";
    //declare instance of FireBase Auth
    private FirebaseAuth mAuth;
    //declare instances of views
    private EditText userEmail;
    private EditText userPassword;
    private Button createAccBtn;
    private Button loginBtn;
    private EditText confirmPassword;
    private Toolbar toolbar;
    private ImageView homepageIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //set toolbar as activities actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //set on click listener for HomePage icon
        homepageIcon = findViewById(R.id.imageView_homepageIcon);
        homepageIcon.setOnClickListener(this);

        //assign variables to EditText views and Buttons
        userEmail = findViewById(R.id.editText_UserEmail);
        userPassword = findViewById(R.id.editText_Password);
        confirmPassword = findViewById(R.id.editText_confirmPassword);
        createAccBtn = findViewById(R.id.button_CreateAccount);
        loginBtn = findViewById(R.id.button_Login);

        //initialise firebase auth
        mAuth = FirebaseAuth.getInstance();

        //set onClick listeners for buttons
        loginBtn.setOnClickListener(this);
        createAccBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //get user email outside of if statement as needed for both scenarios
        String email = userEmail.getText().toString().trim();
        //user clicks to create account
        if(v.getId() == R.id.button_CreateAccount){
            //store contents of the two password boxes in variables
            String password = userPassword.getText().toString();
            String password2 = confirmPassword.getText().toString();
            //check if the two passwords match (to make sure user hasn't miss typed)
            if(passwordsMatch(password, password2)){
                //create new user if passwords match
                createNewUser(email, password);
            }
            else {
                //user feedback to advise two password boxes do not match
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
            }
        }
        //login button has been clicked
        else if (v.getId() == R.id.button_Login) {
            //send user to login page
            Intent intent = new Intent(this, LoginActivity.class);
            //send contents of email address box to login page
            intent.putExtra(EXTRA_EMAIL_ADDRESS, email);
            startActivity(intent);
        }
        //homepage icon clicked
        else if (v.getId() == R.id.imageView_homepageIcon) {
            //send user back to homepage
            Intent intent = new Intent(this, HomePageActivity.class);
            startActivity(intent);
        }
    }

    //create new user and save credentials to Firebase
    private void createNewUser(String email, String password){
        //create new user using email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //check if task was successful
                        if(task.isSuccessful()){
                            //sign up success, go to main page
                            Log.d(TAG, "createUserWithEmail:success");
                            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                            startActivity(intent);
                        }else {
                            //sign up fails print error in the log
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(CreateAccountActivity.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //check if both password boxes are identical
    private boolean passwordsMatch(String pw1, String pw2){
        return pw1.equals(pw2);
    }
}