package com.example.tamir.sharenotes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.example.tamir.sharenotes.Adapter.ChatMessageAdapter;
import com.example.tamir.sharenotes.Common.Common;
import com.example.tamir.sharenotes.Holder.QBChatMessagesHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener{

    QBChatDialog qbChatDialog;
    ListView lstChatMessages;
    ImageButton submitButton,ocrButton;
    EditText editContent;
    ChatMessageAdapter adapter;
    int contextMenuIndexClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;
    Toolbar toolbar;
    String ocrText;
    public String TAG = "ChatMessageActivity";


    //Shows the menu when their is a groupchat
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP)
            getMenuInflater().inflate(R.menu.groupchat_menu,menu);
        return true;
    }

    //Assigns which method to run for each menu button pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id._group_edit_name:
                editGroupName();
                break;
            case R.id._group_add_user:
                addUser();
                break;
            case R.id._group_remove_user:
                removeUser();
                break;
        }
        return true;
    }

    //Opens the List of users activity and passes string for removing
    private void removeUser() {
        Intent intent = new Intent(this,ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }

    //Opens the List of users activity and passes string for adding
    private void addUser() {
        Intent intent = new Intent(this,ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA,qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE,Common.UPDATE_ADD_MODE);
        startActivity(intent);
    }

    //Method to edit the name of the group
    private void editGroupName() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_group,null);

        //Creates a new AlertDialog, puts the current name in EditText
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        final EditText newName = (EditText)view.findViewById(R.id.edit_new_group_name);
        alertDialogBuilder.setCancelable(false)
                //When ok is pressed updates the new name on the QuickBlox database
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        qbChatDialog.setName(newName.getText().toString());

                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        QBRestChatService.updateGroupChatDialog(qbChatDialog,requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        //Displays toast and renames toolbar
                                        Toast.makeText(ChatMessageActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                                        toolbar.setTitle(qbChatDialog.getName());
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //Inflates the context menu options from XML file
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.message_menu,menu);
    }


    //Assigns which methods to run when context menu item is pressed
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        contextMenuIndexClicked = info.position;

        switch (item.getItemId())
        {
            case R.id.update_message:
                updateMessage();
                break;
            case R.id.delete_message:
                deleteMessage();
                break;
            case R.id.share_message:
                shareMessage();
                break;
            case R.id.save_message:
                saveMessage();
                break;
        }
        return true;
    }

    //Saves the message to SQLite
    private void saveMessage() {
        NotesRepo repo = new NotesRepo(this);
        Notes notes = new Notes();
        //Retrieves the name, date and title from the chat message
        //Adds the values to notes class
        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);
        String msgContent = editMessage.getBody().toString();
        notes.name = qbChatDialog.getName();
        String date = new SimpleDateFormat("EEE, d MMMM yyyy", Locale.getDefault()).format(new Date());
        notes.date = date;
        notes.content = msgContent;
        Log.d("STATE", "Message to save:" +editContent.getText().toString());
        notes.notes_ID = NotesActivity._Notes_Id;
        //Runs the insert method and saves the values that had been added to notes class
        NotesActivity._Notes_Id = repo.insert(notes);
        Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
        Log.d("STATE", "SAVED MESSAGE");

    }

    //Method to share message
    private void shareMessage() {
        Intent shareintent = new Intent(android.content.Intent.ACTION_SEND);
        //Retrieves the message to share
        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);
        shareintent.setType("text/plain");
        shareintent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Notes for" +qbChatDialog.getName());
        shareintent.putExtra(android.content.Intent.EXTRA_TEXT,editMessage.getBody());
        startActivity(Intent.createChooser(shareintent, "Share with"));
    }

    //Method to delete message
    private void deleteMessage() {
        final ProgressDialog deleteDialog = new ProgressDialog (ChatMessageActivity.this);
        deleteDialog.setMessage("Please wait...");
        deleteDialog.show();

        //Retrieves message
        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);

        //Delete message from database
        QBRestChatService.deleteMessage(editMessage.getId(),false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                retrieveMessages();
                deleteDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }
    //update message
    private void updateMessage() {

        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);
        editContent.setText(editMessage.getBody());
        isEditMode = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        initViews();
        initChatDialogs();
        retrieveMessages();

        //button to OCRActivity
        ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatMessageActivity.this,OCRActivity.class);
                startActivity(intent);
            }
        });

        //Submit - sends the message
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //When the message is not being edited, retrieves text fromEdittext and sends to database
                if (!isEditMode) {
                    QBChatMessage chatMessage = new QBChatMessage();
                    if (editContent.getText().toString().equals(""))
                    {
                        editContent.setText(ocrText, TextView.BufferType.NORMAL);
                        Log.d("STATE", "Message: " +editContent.getText().toString());
                    }
                    //Sets the body of message as the text message, and the ID of the sender
                    chatMessage.setBody(editContent.getText().toString());
                    chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
                    chatMessage.setSaveToHistory(true);

                    try {
                        qbChatDialog.sendMessage(chatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                    if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                        QBChatMessagesHolder.getInstance().putMessage(qbChatDialog.getDialogId(), chatMessage);
                        ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId());
                        adapter = new ChatMessageAdapter(getBaseContext(), messages);
                        lstChatMessages.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    editContent.setText("");
                    editContent.setFocusable(true);
                }
                //If the user is editing the message this code runs
                else
                {
                    final ProgressDialog updateDialog = new ProgressDialog (ChatMessageActivity.this);
                    updateDialog.setMessage("Please wait...");
                    updateDialog.show();
                    QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                    messageUpdateBuilder.updateText(editContent.getText().toString()).markDelivered().markRead();
                    QBRestChatService.updateMessage(editMessage.getId(),qbChatDialog.getDialogId(),messageUpdateBuilder)
                            .performAsync(new QBEntityCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid, Bundle bundle) {
                                    retrieveMessages();
                                    isEditMode = false;
                                    updateDialog.dismiss();
                                    editContent.setText("");
                                    editContent.setFocusable(true);
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    Toast.makeText(getBaseContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

    }

    //Retrieves all the messages for the chat dialog
    private void retrieveMessages() {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500);

        if(qbChatDialog != null)
        {
            QBRestChatService.getDialogMessages(qbChatDialog,messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    QBChatMessagesHolder.getInstance().putMessages(qbChatDialog.getDialogId(),qbChatMessages);
                    adapter = new ChatMessageAdapter(getBaseContext(),qbChatMessages);
                    lstChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    //Method initalise the chat dialogs
    private void initChatDialogs() {

        qbChatDialog = (QBChatDialog)getIntent().getSerializableExtra(Common.DIALOG_EXTRA);
        qbChatDialog.initForChat(QBChatService.getInstance());

        //Listener for RealTime message updates
        QBIncomingMessagesManager incomingMessage = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessage.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        //Retrieves all messages

        if(qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP)
        {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d("ERROR", "" +e.getMessage());
                }
            });
        }

        //Sets the title of the Chat
        toolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(toolbar);

        qbChatDialog.addMessageListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocrText = getIntent().getStringExtra("OCR");
        Log.d("STATE","From OCR Retrieved: "+ocrText);
    }



    private void initViews() {
        lstChatMessages = (ListView)findViewById(R.id.lstMsgs);
        submitButton = (ImageButton)findViewById(R.id.sendBtn);
        editContent = (EditText)findViewById(R.id.edit_content);
        registerForContextMenu(lstChatMessages);
        toolbar = (Toolbar)findViewById(R.id.message_toolbar);
        ocrButton = (ImageButton)findViewById(R.id.ocrBtn);

    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        QBChatMessagesHolder.getInstance().putMessage(qbChatMessage.getDialogId(),qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatMessage.getDialogId());
        adapter = new ChatMessageAdapter(getBaseContext(),messages);
        lstChatMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e("ERROR",""+e.getMessage());
    }
}
