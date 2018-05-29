package com.example.tamir.sharenotes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.w3c.dom.Text;

public class NotesActivity extends AppCompatActivity {

    Button btnDelete;
    Button btnUpdate;
    public static TextView txtName, txtDate, txtContent;
    public static int _Notes_Id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        btnDelete = (Button)findViewById(R.id.btnDelete);
        btnUpdate = (Button)findViewById(R.id.btnUpdate);
        txtName = (TextView)findViewById(R.id.txtModule);
        txtDate = (TextView)findViewById(R.id.txtDate);
        txtContent = (TextView)findViewById(R.id.txtNotes);

        //Gets the ID from the ListView that was clicked
        _Notes_Id = 0;
        Intent intent = getIntent();
        _Notes_Id = intent.getIntExtra("notes_Id", 0);
        final NotesRepo repo = new NotesRepo(this);
        Notes notes = new Notes();
        notes = repo.getNotesById(_Notes_Id);

        //Displays the title, date and content on the activity
        txtName.setText(notes.name);
        txtDate.setText(notes.date);
        txtContent.setText(notes.content);

        //Deletes the database entry
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotesRepo repo = new NotesRepo(NotesActivity.this);
                //Runs the delete method within the NotesRepo class
                repo.delete(_Notes_Id);
                Toast.makeText(NotesActivity.this, "Notes Deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        //Updates the notes content
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Creates an AlertDialog which will contain the text from the Database
                LayoutInflater inflater = LayoutInflater.from(NotesActivity.this);
                View view = inflater.inflate(R.layout.sql_edit_message,null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NotesActivity.this);
                alertDialogBuilder.setView(view);
                final EditText updatenotes = (EditText)view.findViewById(R.id.edit_notes);
                updatenotes.setText(txtContent.getText().toString());
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            //When the user presses 'OK' will update the database
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NotesRepo repo = new NotesRepo(NotesActivity.this);
                                Notes notes = new Notes();
                                notes.name = txtName.getText().toString();
                                notes.date = txtDate.getText().toString();
                                notes.content = updatenotes.getText().toString();
                                notes.notes_ID = _Notes_Id;
                                repo.update(notes);
                                Toast.makeText(NotesActivity.this, "Saved Changes", Toast.LENGTH_SHORT).show();
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
        });

    }
}
