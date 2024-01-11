package daniel.southern.danielsouthern_cet343assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

    Button loginBtn;
    Button createAccBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        loginBtn = findViewById(R.id.button_login);
        createAccBtn = findViewById(R.id.button_createAccount);

        loginBtn.setOnClickListener(this);
        createAccBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_login){
            goToLoginPage();
        } else if (v.getId() == R.id.button_createAccount) {
            goToCreateAccountPage();
        }
    }

    private void goToCreateAccountPage() {
        Intent intent = new Intent(HomePageActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    private void goToLoginPage() {
        Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}