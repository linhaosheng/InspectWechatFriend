package linhao.inspectwechatfriend;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import linhao.inspectwechatfriend.accessibility.InspectWechatFriendService;

/**
 * Created by linhao on 16/12/30.
 */

public class Preferences {

    public static void saveDeleteFriends(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("delete", Context.MODE_PRIVATE);
        sharedPreferences.edit().putStringSet("delete_friends", InspectWechatFriendService.deleteList).apply();
    }


    public static List<String> getDeleteFriends(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("delete", Context.MODE_PRIVATE);
        Set<String> hashSet = sharedPreferences.getStringSet("delete_friends",new HashSet<String>());
        List<String> stringList = new ArrayList<>();
        for(String s:hashSet){
            stringList.add(s);
        }
        return stringList;

    }
}
