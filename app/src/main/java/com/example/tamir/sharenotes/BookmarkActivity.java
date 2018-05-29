package com.example.tamir.sharenotes;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class BookmarkActivity extends ListActivity {

        TextView notes_Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        NotesRepo repo = new NotesRepo(this);

        //Creates a ListView and retrieves all saved notes
        //When the notes are clicked, the ID is passed so that the rest of the content can be viewed
        ArrayList<HashMap<String, String>> notesList = repo.getNotesList();
        if (notesList.size() !=0) {
            ListView listView = getListView();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    notes_Id =(TextView) view.findViewById(R.id.notes_Id);
                    String notesId = notes_Id.getText().toString();
                    Intent objIndent = new Intent(getApplicationContext(),NotesActivity.class);
                    objIndent.putExtra("notes_Id", Integer.parseInt(notesId));
                    startActivity(objIndent);
                }
            });
            ListAdapter adapter = new SimpleAdapter(BookmarkActivity.this, notesList, R.layout.view_notes, new String[] { "id", "name"}, new int[] {R.id.notes_Id, R.id.module_name});
            setListAdapter(adapter);
        }
    }
}
