package com.example.tamir.sharenotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity {

    static final String APP_ID = "69573";
    static final String AUTH_KEY = "uMJZFwFS7ajuFuB";
    static final String AUTH_SECRET = "m89NnbBXhEzfUTZ";
    static final String ACCOUNT_KEY = "FwBqYxxwu9eegWjBSu2j";
    private CheckBox rememberMe;
    private SharedPreferences.Editor prefsEditor;
    private String username,password;
    Button loginbtn, signupbtn;
    EditText userinput, passwordinput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFramework();

        //Initialize Views
        loginbtn = (Button)findViewById(R.id.m_btn_login);
        signupbtn = (Button)findViewById(R.id.m_btn_signup);
        userinput = (EditText)findViewById(R.id.m_Login);
        passwordinput = (EditText)findViewById(R.id.m_Password);
        rememberMe = (CheckBox)findViewById(R.id.saveUserPass);

        //Retrieves the content from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        //Creates an Editor for the SharedPreferences
        prefsEditor = sharedPreferences.edit();
        //Boolean Value - If it is true, the Username and Password will be retrieved from SharedPreferences
        //Originally set as false
        Boolean saveLoginDetails = sharedPreferences.getBoolean("saveLogin", false);
        if (saveLoginDetails == true) {
            //Gets the text from SharedPreferences and puts to EditText Field
            userinput.setText(sharedPreferences.getString("username", ""));
            passwordinput.setText(sharedPreferences.getString("password", ""));
            //Keeps the Checkbox tioked
            rememberMe.setChecked(true);
        }

        //When the user presses Sign Up, takes them to Sign Up Activity
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
            }
        });


        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checks if the Checkbox is ticked
                if (rememberMe.isChecked())
                {
                    //Retrieves username and password from EditText
                    username = userinput.getText().toString();
                    password = passwordinput.getText().toString();
                    //Puts the values into SharedPreferences
                    prefsEditor.putBoolean("saveLogin", true);
                    prefsEditor.putString("username", username);
                    prefsEditor.putString("password", password);
                    prefsEditor.apply();
                }
                else {
                    //If they untick the Checkbox, the values will be cleared
                    prefsEditor.clear();
                    prefsEditor.commit();
                }

                //Retrieves username and password from EditText
                final String user = userinput.getText().toString();
                final String password = passwordinput.getText().toString();
                //Creates new object of the users information
                QBUser qbuser = new QBUser(user,password);
                //Signs in user
                QBUsers.signIn(qbuser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        //Displays a toast to the user
                        Toast.makeText(getBaseContext(),"Login Successfull", Toast.LENGTH_SHORT).show();
                        //Create a new Intent to new activity
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        //Passes the username and password
                        intent.putExtra("user",user);
                        intent.putExtra("password",password);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        //When there is an error signing up, it will be dislayed to user
                        Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }
    private void initializeFramework() {
        //Initialises the QuickBlox SDK using these values declared at the top of class taken from QuickBlox website
        QBSettings.getInstance().init(getApplicationContext(),APP_ID,AUTH_KEY,AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
