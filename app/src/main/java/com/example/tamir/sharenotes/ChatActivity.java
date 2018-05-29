package com.example.tamir.sharenotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;

import com.example.tamir.sharenotes.Adapter.ChatDialogsAdapters;
import com.example.tamir.sharenotes.Common.Common;
import com.example.tamir.sharenotes.Holder.QBChatDialogHolder;
import com.example.tamir.sharenotes.Holder.QBUnreadMessageHolder;
import com.example.tamir.sharenotes.Holder.QBUsersHolder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.chat.Chat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatActivity extends AppCompatActivity implements QBSystemMessageListener,QBChatDialogMessageListener {

    FloatingActionButton addbtn, findlibbtn;
    ListView chatlist;

    //Inflates the menu for the chat messages from the XML file
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_dialog_menu,menu);
    }

    //Sets the action for when button pressed on menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //Retrieves the information for the Menu item that is selected
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId())
        {
            //If they press the delete button, it will run the delete dialog method and pass the position in the adapter for the one selected
            case R.id.delete_dialog:
                deleteDialog(info.position);
                break;
        }
        return true;
    }

    //Method to delete dialogs
    private void deleteDialog(int index) {
        //Creates a new object of a chat dialog from the position in the adapter
        final QBChatDialog chatDialog = (QBChatDialog)chatlist.getAdapter().getItem(index);
        //Deletes the dialog by retrieving the ID from the chatDialog object
        QBRestChatService.deleteDialog(chatDialog.getDialogId(),false)
                .performAsync(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        //Removes the dialog from the Holder
                        QBChatDialogHolder.getInstance().removeDialog(chatDialog.getDialogId());
                        //Creates a new ChatDialogsAdapter object by retrieving the all Dialogs
                        ChatDialogsAdapters adapter = new ChatDialogsAdapters(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());
                        //Adds the adapter to the ListView
                        chatlist.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
    }

    //Inflates the menu XML file
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return true;
    }

    //Assigning what to do when menu buttons are clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            //Runs the method which opens user profile
            case R.id.chat_menu_profile:
                showUserProfile();
                break;
            //Opens Bookmarks
            case R.id.chat_menu_bookmarks:
                Intent intent = new Intent(ChatActivity.this, BookmarkActivity.class);
                startActivity(intent);
            default:
                break;
        }
        return true;
    }

    private void showUserProfile() {
        Intent intent = new Intent(ChatActivity.this,UserProfile.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialogs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Sets the Toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.chat_toolbar);
        toolbar.setTitle("Messages");
        setSupportActionBar(toolbar);

        createSessionForChat();

        //Gets the position of the item clicked in the ListView and passes the Chat through an Intent
        chatlist = (ListView)findViewById(R.id.chatlist);
        registerForContextMenu(chatlist);
        chatlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog = (QBChatDialog)chatlist.getAdapter().getItem(position);
                Intent intent = new Intent(ChatActivity.this, ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA,qbChatDialog);
                startActivity(intent);
            }
        });

        loadChatDialogs();

        //Opens the activity to create a chat
        addbtn = (FloatingActionButton)findViewById(R.id.adduser);
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,ListUsersActivity.class);
                startActivity(intent);
            }
        });

        //Opens the activity to find libraries
        findlibbtn = (FloatingActionButton)findViewById(R.id.libraries);
        findlibbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    //Loads the Chat Dialogs from the QuickBlox database
    private void loadChatDialogs() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);

        QBRestChatService.getChatDialogs(null,requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {

                QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);
                Set<String> setIds = new HashSet<>();
                for(QBChatDialog chatDialog:qbChatDialogs)
                    setIds.add(chatDialog.getDialogId());

                //Gets all unread messages
                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle())
                        .performAsync(new QBEntityCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer, Bundle bundle) {
                                //Creates an adapter and adds to ListView
                                QBUnreadMessageHolder.getInstance().setBundle(bundle);
                                ChatDialogsAdapters adapter = new ChatDialogsAdapters(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());
                                chatlist.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR",e.getMessage());
            }
        });
    }

    //Creates a chat session
    private void createSessionForChat() {
        /*final ProgressDialog mDialog = new ProgressDialog(ChatActivity.this);
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();*/

        //Gets the username and password from whe user signs in
        String user,password;
        user = getIntent().getStringExtra("user");
        password = getIntent().getStringExtra("password");

        //Puts users in Holder class
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

        //Creates a new QuickBlox session for the user
        final QBUser qbUser = new QBUser(user,password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                //Adds message listener for realtime message updates
                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        // mDialog.dismiss();
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR", ""+e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    //Process message method for Listener
    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                //Adds the dialog to the Holder class, then displays all
                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource = QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogsAdapters adapters = new ChatDialogsAdapters(getBaseContext(),adapterSource);
                chatlist.setAdapter(adapters);
                adapters.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e("ERROR",""+e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadChatDialogs();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}
