package com.example.tamir.sharenotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.tamir.sharenotes.Common.Common;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class UserProfile extends AppCompatActivity {

    EditText editPassword,editOldPassword,editFullName,editEmail,editPhone;
    Button btnUpdate,btnCancel;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout:
                logOut();
                break;
            default:
                break;
        }
        return true;
    }

    //Method to log user out
    private void logOut() {
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        //If user is logged out, a toast will be displayed and taken back to Sign in
                        Toast.makeText(UserProfile.this, "Logged Out",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserProfile.this,MainActivity.class);
                        //Ensures all other activities are cleared
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Sets the toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.update_toolbar);
        toolbar.setTitle("Update Profile");
        setSupportActionBar(toolbar);
        
        initViews();
        loadUserProfile();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Retrieves all values from EditText and updates values on QuickBlox
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = editOldPassword.getText().toString();
                String newPassword = editPassword.getText().toString();
                String fullName = editFullName.getText().toString();
                String email = editEmail.getText().toString();
                String phone = editPhone.getText().toString();

                QBUser user = new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId());
                if(!Common.isNullOrEmptyString(oldPassword));
                    user.setOldPassword(oldPassword);
                if(!Common.isNullOrEmptyString(newPassword));
                    user.setPassword(newPassword);
                if(!Common.isNullOrEmptyString(fullName));
                    user.setFullName(fullName);
                if(!Common.isNullOrEmptyString(email));
                    user.setEmail(email);
                if(!Common.isNullOrEmptyString(phone));
                    user.setPhone(phone);

                //Creates a new progress dialog
                final ProgressDialog mDialog = new ProgressDialog(UserProfile.this);
                mDialog.setMessage("Please Wait...");
                mDialog.show();
                QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {

                        Toast.makeText(UserProfile.this,"User"+qbUser.getLogin()+"updated",Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfile.this,"ERROR: "+e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }

    private void loadUserProfile() {

        //Gets the following values from QuickBlox and displays to user
        QBUser currentUser = QBChatService.getInstance().getUser();
        String fullName = currentUser.getFullName();
        String email = currentUser.getEmail();
        String phone = currentUser.getPhone();

        editFullName.setText(fullName);
        editEmail.setText(email);
        editPhone.setText(phone);
    }

    private void initViews() {

        btnCancel = (Button)findViewById(R.id.u_btn_cancel);
        btnUpdate = (Button)findViewById(R.id.btn_update);
        editEmail = (EditText)findViewById(R.id.update_email);
        editFullName  = (EditText)findViewById(R.id.update_fullName);
        editOldPassword  = (EditText)findViewById(R.id.update_old_password);
        editPassword  = (EditText)findViewById(R.id.update_new_password);
        editPhone  = (EditText)findViewById(R.id.update_number);
    }
}
