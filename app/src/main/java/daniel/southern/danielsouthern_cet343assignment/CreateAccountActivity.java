package daniel.southern.danielsouthern_cet343assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener{
    //TODO: follow Coding in Flow's tutorial for validating email and password

    //tag for logs
    public static final String TAG = "CreateAccountActivity";
    private FirebaseAuth mAuth;
    //initialise views
    private EditText userEmail;
    private EditText userPassword;
    private Button createAccBtn;
    private Button loginBtn;
    private EditText confirmPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        userEmail = findViewById(R.id.editText_UserEmail);
        userPassword = findViewById(R.id.editText_Password);
        confirmPassword = findViewById(R.id.editText_confirmPassword);
        createAccBtn = findViewById(R.id.button_CreateAccount);
        loginBtn = findViewById(R.id.button_Login);

        //initialise firebase auth
        mAuth = FirebaseAuth.getInstance();

        //set onClick listeners
        loginBtn.setOnClickListener(this);
        createAccBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_CreateAccount){
            String email = userEmail.getText().toString().trim();
            String password = userPassword.getText().toString();
            String password2 = confirmPassword.getText().toString();
            //create account
            if(passwordsMatch(password, password2)){
                createNewUser(email, password);
            }
            else {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
            }
        } else if (v.getId() == R.id.button_Login) {
            //send user to login page
            Intent intent = new Intent(this, LoginActivity.class);
            //TODO: send text from editText_UserEmail to LoginActivity and prepopulate
            startActivity(intent);
        }
    }

    //create new user and save credentials to Firebase
    private void createNewUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //sign in success, go to main page
                            Log.d(TAG, "createUserWithEmail:success");
                            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                            startActivity(intent);
                        }else {
                            //sign in fails
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