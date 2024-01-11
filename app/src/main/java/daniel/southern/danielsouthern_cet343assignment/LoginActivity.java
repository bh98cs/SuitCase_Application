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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private Button loginBtn;
    private Button createAccBtn;
    private EditText userEmail;
    private EditText userPassword;
    private Toolbar toolbar;
    private ImageView homepageIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //create toolbar
        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //hide title as custom one created in layout file
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //initialize homepage icon
        homepageIcon = findViewById(R.id.imageView_homepageIcon);
        homepageIcon.setOnClickListener(this);

        //Initialize widgets
        loginBtn = findViewById(R.id.button_Login);
        createAccBtn = findViewById(R.id.button_CreateAccount);
        userEmail = findViewById(R.id.editText_UserEmail);
        userPassword = findViewById(R.id.editText_Password);

        loginBtn.setOnClickListener(this);
        createAccBtn.setOnClickListener(this);
        //get intent that started activity
        Intent intent = getIntent();
        String email = intent.getStringExtra(CreateAccountActivity.EXTRA_EMAIL_ADDRESS);
        //prepopulate email address for user if filled out in CreateAccountActivity
        userEmail.setText(email);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_Login){
            //check credentials and login user
            Log.i(TAG, "button_Login clicked.");
            String email = userEmail.getText().toString().trim();
            String password = userPassword.getText().toString().trim();
            signIn(email, password);
        } else if (v.getId() == R.id.button_CreateAccount) {
            //send user to create account page
            Intent intent = new Intent(this, CreateAccountActivity.class);
            //TODO: send any text from editText_UserEmail to CreateAccount page and prepopulate
            startActivity(intent);
        } else if (v.getId() == R.id.imageView_homepageIcon) {
            //send user to homepage
            Intent intent = new Intent(this, HomePageActivity.class);
            startActivity(intent);
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //sign in successful
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }else{
                            //sign in unsuccessful
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Email or Password is incorrect.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}