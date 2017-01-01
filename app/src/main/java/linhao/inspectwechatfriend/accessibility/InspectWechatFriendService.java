package linhao.inspectwechatfriend.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import linhao.inspectwechatfriend.Preferences;
import linhao.inspectwechatfriend.activity.DeleteFriendListActivity;
import linhao.inspectwechatfriend.utils.PerformClickUtils;
import linhao.inspectwechatfriend.utils.Utils;

import static android.content.ContentValues.TAG;
/**
 * Created by linhao on 16/12/30.
 */

public class InspectWechatFriendService extends AccessibilityService {

    public static final int GROUP_COUNT = 39;//群组成员个数

    public static final String WECHAT_VERSION_32 = "6.3.32";

    public static List<String> nickNameList = new ArrayList<>();
    public static HashSet<String> deleteList = new HashSet<>();
    public static HashSet<String> sortItems = new HashSet<>();

    public static boolean hasComplete = false;
    public static boolean complete = false;
    public static boolean canChecked;

    public static String selectUI_listview_id = "com.tencent.mm:id/en";
    public static String selectUI_checkbox_id = "com.tencent.mm:id/n3";
    public static String selectUI_sortitem_id = "com.tencent.mm:id/a90";
    public static String selectUI_nickname_id = "com.tencent.mm:id/lm";
    public static String selectUI_create_button_id = "com.tencent.mm:id/g9";
    public static String chattingUI_message_id = "com.tencent.mm:id/bfx";
    public static String groupinfoUI_listview_id = "android:id/list";
    private Intent intent = null;

    @Override
    protected void onServiceConnected() {//辅助服务被打开后 执行此方法
        super.onServiceConnected();
        Toast.makeText(this, "_已开启检测好友服务_", Toast.LENGTH_LONG).show();
        intent = new Intent(this, DeleteFriendListActivity.class);
        String wechatVersion = Utils.getVersion(this);
       if (WECHAT_VERSION_32.equals(wechatVersion)) {
            selectUI_listview_id = "com.tencent.mm:id/en";
            selectUI_checkbox_id = "com.tencent.mm:id/n3";
            selectUI_sortitem_id = "com.tencent.mm:id/a90";
            selectUI_nickname_id = "com.tencent.mm:id/lm";
            selectUI_create_button_id = "com.tencent.mm:id/g9";
            chattingUI_message_id = "com.tencent.mm:id/bfx";
            groupinfoUI_listview_id = "android:id/list";
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {//监听手机当前窗口状态改变 比如 Activity 跳转,内容变化,按钮点击等事件

        //如果手机当前界面的窗口发送变化
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            //获取当前activity的类名:
            String currentWindowActivity = accessibilityEvent.getClassName().toString();
            if (!hasComplete) {
                if ("com.tencent.mm.ui.contact.SelectContactUI".equals(currentWindowActivity)) {
                    canChecked = true;
                    createGroup();
                } else if ("com.tencent.mm.ui.chatting.ChattingUI".equals(currentWindowActivity)) {
                    getDeleteFriend();
                } else if ("com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI".equals(currentWindowActivity)) {
                    deleteGroup();
                } else if ("com.tencent.mm.ui.LauncherUI".equals(currentWindowActivity)) {
                    PerformClickUtils.findTextAndClick(this, "更多功能按钮");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PerformClickUtils.findTextAndClick(this, "发起群聊");
                }
            } else {
                nickNameList.clear();
                deleteList.clear();
                sortItems.clear();
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    stopSelf();
                }
                intent = null;
            }
        }

    }

    /**
     * 模拟创建群组步骤
     */
    private void createGroup() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> listview = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(selectUI_listview_id);
        int count = 0;
        int allCount = listview.get(0).getCollectionInfo().getRowCount() - 4;
        int sorltCount = sortItems.size();
        Log.e("allCount---", String.valueOf(allCount));
        Log.e("sorltCount---", String.valueOf(sorltCount));
        if (!listview.isEmpty()) {
            while (canChecked) {
                List<AccessibilityNodeInfo> checkboxList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(selectUI_checkbox_id);
                List<AccessibilityNodeInfo> sortList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(selectUI_sortitem_id);

                for (AccessibilityNodeInfo nodeInfo : sortList) {
                    if (nodeInfo != null && nodeInfo.getText() != null) {
                        sortItems.add(nodeInfo.getText().toString());
                    }
                }
                for (AccessibilityNodeInfo nodeInfo : checkboxList) {

                    String nickname = nodeInfo.getParent().findAccessibilityNodeInfosByViewId(selectUI_nickname_id).get(0).getText().toString();
                    if (!nickNameList.contains(nickname)) {
                        nickNameList.add(nickname);
                        PerformClickUtils.performClick(nodeInfo);
                        count++;
                        Log.e(TAG, "nickname = " + nickname);
                        Log.e("count---", String.valueOf(count));
                        int lastCount = nickNameList.size() - listview.get(0).getCollectionInfo().getRowCount() + sortItems.size() + 4;
                        Log.e("lastCount---", String.valueOf(lastCount));
                        if (count >= GROUP_COUNT || nickNameList.size() >= listview.get(0).getCollectionInfo().getRowCount() - sortItems.size() - 4) {
                            List<AccessibilityNodeInfo> createButtons = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(selectUI_create_button_id);
                            if (!createButtons.isEmpty()) {
                                canChecked = false;
                                PerformClickUtils.performClick(createButtons.get(0));
                            }
                            if (nickNameList.size() >= listview.get(0).getCollectionInfo().getRowCount() - sortItems.size() - 4) {
                                complete = true;
                                System.out.println("end--------" + String.valueOf(complete));
                            }
                            return;
                        }
                    }
                }
                listview.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 模拟获取被删好友列表步骤
     */
    private void getDeleteFriend() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(chattingUI_message_id);

        for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
            if (nodeInfo != null && nodeInfo.getText() != null && nodeInfo.getText().toString().contains("你无法邀请未添加你为好友的用户进去群聊，请先向")) {
                String str = nodeInfo.getText().toString();
                str = str.replace("你无法邀请未添加你为好友的用户进去群聊，请先向", "");
                str = str.replace("发送朋友验证申请。对方通过验证后，才能加入群聊。", "");
                String[] arr = str.split("、");
                System.out.println("arr------" + arr);
                deleteList.addAll(Arrays.asList(arr));
                Preferences.saveDeleteFriends(this);
                Log.e(TAG, "deleteList.size():" + deleteList.size());
                Toast.makeText(this, "僵尸粉数量:" + deleteList.size(), Toast.LENGTH_SHORT).show();
                break;
            }
        }
        PerformClickUtils.findTextAndClick(this, "聊天信息");
    }

    /**
     * 退出群组步骤
     */
    private void deleteGroup() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(groupinfoUI_listview_id);
        if (!nodeInfoList.isEmpty()) {
            boolean isDelete = false;
            while (true) {
                List<AccessibilityNodeInfo> deletes = nodeInfoList.get(0).findAccessibilityNodeInfosByText("删除并退出");
                if (deletes != null && deletes.size() != 0) {
                    String deleteButton = deletes.get(0).getText().toString();
                    if (deleteButton != null && deleteButton.equals("删除并退出")) {
                        isDelete = true;
                        break;
                    }
                }
                if (!isDelete) {
                    nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            PerformClickUtils.findTextAndClick(this, "删除并退出");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            PerformClickUtils.findTextAndClick(this, "离开群聊");
            try {
                Thread.sleep(1000);
                PerformClickUtils.findTextAndClick(this, "离开群聊");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (complete) {
                hasComplete = true;
            }
        }
    }

    @Override
    public void onInterrupt() {//辅助服务被关闭 执行此方法
        canChecked = false;
        Toast.makeText(this, "_检测好友服务被中断啦_", Toast.LENGTH_LONG).show();
    }
}
