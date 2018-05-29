package com.example.tamir.sharenotes.Common;

import com.example.tamir.sharenotes.Holder.QBUsersHolder;
import com.example.tamir.sharenotes.Model.MyPlaces;
import com.example.tamir.sharenotes.Model.Results;
import com.example.tamir.sharenotes.Remote.IGoogleAPIService;
import com.example.tamir.sharenotes.Remote.RetrofitClient;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class Common {

    //This class is used to hold static variables

    public static final String DIALOG_EXTRA = "Dialogs";
    public static final String UPDATE_DIALOG_EXTRA = "ChatDialogs";
    public static final String UPDATE_MODE = "Mode";
    public static final String UPDATE_ADD_MODE = "add";
    public static final String UPDATE_REMOVE_MODE = "remove";

    public static final String GoogleAPI_URL = "https://maps.googleapis.com/";
    public static Results currentResult;

    public static IGoogleAPIService getGoogleAPIService()
    {
        return RetrofitClient.getClient(GoogleAPI_URL).create(IGoogleAPIService.class);
    }

    public static String createChatDialogName(List<Integer> qbUsers)
    {
        List<QBUser> qbUsers1 = QBUsersHolder.getInstance().getUserByIds(qbUsers);
        StringBuilder name = new StringBuilder();
        for(QBUser user:qbUsers1)
            name.append(user.getFullName()).append(" ");
        if(name.length() > 30)
            name = name.replace(30,name.length()-1,"...");
        return name.toString();
    }

    public static boolean isNullOrEmptyString(String content)
    {
        return (content != null &&content.trim().isEmpty()?false:true);
    }
}
