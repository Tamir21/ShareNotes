package com.example.tamir.sharenotes.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.tamir.sharenotes.Holder.QBUnreadMessageHolder;
import com.example.tamir.sharenotes.R;
import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;

/**
 * Created by Tamir on 21/03/2018.
 */

public class ChatDialogsAdapters extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;

    public ChatDialogsAdapters(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }

    @Override
    public int getCount() {
        return qbChatDialogs.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertview, ViewGroup parent) {
        View view = convertview;
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_chat_dialog, null);

            TextView title, msg;
            ImageView imageView, imageViewunread;

            msg = (TextView)view.findViewById(R.id.chat_message);
            title = (TextView)view.findViewById(R.id.chat_title);
            imageView = (ImageView)view.findViewById(R.id.chatImg);
            imageViewunread = (ImageView)view.findViewById(R.id.chatUnread);

            String message = qbChatDialogs.get(position).getLastMessage();
            msg.setText(message);
            Log.d("The Message: ","Message: "+message);

            String Title = qbChatDialogs.get(position).getName();
            String letter = Title.substring(0,1);
            title.setText(Title);
            Log.d("The Title: ","Title"+Title);

            TextDrawable.IBuilder  builder = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();

            Log.d("The Letter: ", letter);
            TextDrawable drawable = builder.build(letter,Color.parseColor("#11721a"));
            imageView.setImageDrawable(drawable);

            TextDrawable.IBuilder  unreadBuilder = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();

            int unread_count = QBUnreadMessageHolder.getInstance().getBundle().getInt(qbChatDialogs.get(position).getDialogId());
            if(unread_count > 0)
            {
                TextDrawable unread_drawable = unreadBuilder.build(""+unread_count, Color.RED);
                imageViewunread.setImageDrawable(unread_drawable);
            }

        }
        return view;
    }
}
