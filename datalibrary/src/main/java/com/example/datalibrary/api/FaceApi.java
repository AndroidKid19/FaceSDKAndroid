/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.example.datalibrary.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.datalibrary.db.DBManager;
import com.example.datalibrary.listener.DBLoadListener;
import com.example.datalibrary.model.Group;
import com.example.datalibrary.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceApi {
    private static FaceApi instance;

    public boolean isDelete;

    private List<User> users = new ArrayList<>();


    private FaceApi() {

    }

    public static synchronized FaceApi getInstance() {
        if (instance == null) {
            instance = new FaceApi();
        }
        return instance;
    }

    public void  init(DBLoadListener dbLoadListener , Context context){

        users = new ArrayList<>();
        DBManager.getInstance().init(context);
        DBManager.getInstance().queryAllUsers(dbLoadListener);
    }

    /**
     * 添加用户组
     */
    public boolean groupAdd(Group group) {
        if (group == null || TextUtils.isEmpty(group.getGroupId())) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
        Matcher matcher = pattern.matcher(group.getGroupId());
        if (!matcher.matches()) {
            return false;
        }
        boolean ret = DBManager.getInstance().addGroup(group);

        return ret;
    }

    /**
     * 查询用户组（默认最多取1000个组）
     */
    public List<Group> getGroupList(int start, int length) {
        if (start < 0 || length < 0) {
            return null;
        }
        if (length > 1000) {
            length = 1000;
        }
        List<Group> groupList = DBManager.getInstance().queryGroups(start, length);
        return groupList;
    }

    /**
     * 根据groupId查询用户组
     */
    public List<Group> getGroupListByGroupId(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }
        return DBManager.getInstance().queryGroupsByGroupId(groupId);
    }

    /**
     * 根据groupId删除用户组
     */
    public boolean groupDelete(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return false;
        }
        boolean ret = DBManager.getInstance().deleteGroup(groupId);
        return ret;
    }

    /**
     * 添加用户
     */
    public boolean userAdd(User user) {
        if (user == null || TextUtils.isEmpty(user.getGroupId())) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
        Matcher matcher = pattern.matcher(user.getUserId());
        if (!matcher.matches()) {
            return false;
        }
        boolean ret = DBManager.getInstance().addUser(user);
        User newUser = DBManager.getInstance().queryUserByUserNameItem(user.getUserName());
        users.add(0 , newUser);
        return ret;
    }

    /**
     * 查找所有用户
     */
    public synchronized List<User> getAllUserList() {
            if (users == null && users.size() == 0){
                users = DBManager.getInstance().queryAllUsers();
            }
            return users;
    }

    /**
     * 根据userName查找用户（精确查找）
     */
    public List<User> getUserListByUserName(String userName) {
        if (TextUtils.isEmpty(userName)) {
            return null;
        }
        return DBManager.getInstance().queryUserByUserNameAccu(userName);
    }

    /**
     * 根据userName查找用户（模糊查找）
     */
    public List<User> getUserListByUserNameVag(String userName) {
        if (TextUtils.isEmpty(userName)) {
            return null;
        }
        if (users != null && users.size() > 0){
            List list = new ArrayList();
            for (int i = 0 , k = users.size(); i < k; i++){
                User user = users.get(i);
                if (user.getUserName().indexOf(userName) != -1){
                    user.setUserIndex(i);
                    list.add(user);
                }
            }
            return list;
        }else {
            return DBManager.getInstance().queryUserByUserNameVag(userName);
        }
    }

    /**
     * 根据_id查找用户
     */
    public User getUserListById(int _id) {
        if (_id < 0) {
            return null;
        }
        List<User> userList = DBManager.getInstance().queryUserById(_id);
        if (userList != null && userList.size() > 0) {
            return userList.get(0);
        }
        return null;
    }

    /**
     * 更新用户
     */
    public boolean userUpdate(User user) {
        if (user == null) {
            return false;
        }

        boolean ret = DBManager.getInstance().updateUser(user);
        return ret;
    }

    /**
     * 更新用户
     */
    public boolean userUpdate(String userName, String imageName, byte[] feature) {
        if (userName == null || imageName == null || feature == null) {
            return false;
        }

        boolean ret = DBManager.getInstance().updateUser(userName, imageName, feature);
        return ret;
    }

    /**
     * 删除用户
     */
    public boolean userDelete(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return false;
        }
        boolean ret = DBManager.getInstance().deleteUser(userId);
        if (ret){
            for (int i = 0 , k = users.size(); i < k; i++){
                if (users.get(i).getUserId().equals(userId)){
                    users.remove(i);
                    break;
                }
            }
        }
        return ret;
    }
    /**
     * 删除用户
     */
    public boolean userDeletes(List<User> list , boolean isHave , DBLoadListener dbLoadListener) {
//        boolean isUsers = list.equals(users) && ;
        int count = list.size();
        boolean isAllCover = true;
        if (list.size() == users.size()){
            for (int i = 0 , k = count; i < k; i++){
                if (!list.get(i).isChecked()){
                    isAllCover = false;
                    break;
                }
            }
        } else {
            isAllCover = false;
        }
        if(isAllCover){
            dbLoadListener.onLoad(count / 2 ,
                    count , 0.5f);
            users = new ArrayList<>();
            DBManager.getInstance().clearTable();
            dbLoadListener.onComplete(null , count);
            return true;
        }
        boolean rets = true;
        int k = list.size();
        int i = 0;
        dbLoadListener.onStart(count);
            while (i < k && !Thread.currentThread().isInterrupted() && isDelete){
                User user = list.get(i);
                int userId = user.getId();
                if (list.get(i).isChecked()) {
                    long time = System.currentTimeMillis();
                    boolean ret = DBManager.getInstance().deleteUser(userId);
                    if (ret){
                        /*User user1 = */list.remove(user);
                        if (isHave){
                            users.remove(user);
                        }
                        k = list.size();
                        if (dbLoadListener != null){
                            dbLoadListener.onLoad(i + count - k ,
                                    count , ((float) (i + count - k) / (float) count));
                        }
                    }else {
                        rets = false;
                        i++;
                    }
                } else {
                    i++;
                }
            }
        dbLoadListener.onComplete(null , count);
            return rets;
    }

    public void userClean(){

        users = new ArrayList<>();
        DBManager.getInstance().clearTable();
    }


    /**
     * 远程删除用户
     */
    public boolean userDeleteByName(String userName) {
        if (TextUtils.isEmpty(userName)) {
            return false;
        }
        boolean ret = DBManager.getInstance().userDeleteByName(userName);
        if (ret){
            for (int i = 0 , k = users.size(); i < k; i++){
                if (users.get(i).getUserName().equals(userName)){
                    users.remove(i);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 是否是有效姓名
     *
     * @param username 用户名
     * @return 有效或无效信息
     */
    public String isValidName(String username) {
        if (username == null || "".equals(username.trim())) {
            return "姓名为空";
        }

        // 姓名过长
        if (!isSpotString(username)) {
            return "姓名过长";
        }

       /* String regex0 = "[ `~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）—"
                + "—+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p0 = Pattern.compile(regex0);
        Matcher m0 = p0.matcher(username);
        if (m0.find()) {
            return "姓名中含有特殊符号";
        }
*/
        // 含有特殊符号
        String regex = "^[0-9a-zA-Z_\\u3E00-\\u9FA5]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(username);
        if (!m.find()) {
            return "姓名中含有特殊符号";
        }
        return "0";
    }
    public static boolean isSpotString(String str){
        int n = 0;
        String newStr = "";
        for (int i = 0 , k = str.length(); i < k ; i++){
            char item = str.charAt(i);
            if (isChinese(item)){
                n += 2;
            }else {
                n += 1;
            }
            if (n > 10){
                return false;
            }
            newStr += item;
        }
        return true;
    }
    public static boolean isChinese(char c) {

        return c >= 0x4E00 && c <= 0x9FA5; // 根据字节码判断

    }


    public boolean registerUserIntoDBmanager(String groupName, String userName, String picName,
                                             String userInfo, byte[] faceFeature) {
        boolean isSuccess = false;

        User user = new User();
        user.setGroupId(DBManager.GROUP_ID);
        // 用户id（由数字、字母、下划线组成），长度限制128B
        // uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
        final String uid = UUID.randomUUID().toString();
        user.setUserId(uid);
        user.setUserName(userName);
        user.setFeature(faceFeature);
        user.setImageName(picName);
        if (userInfo != null) {
            user.setUserInfo(userInfo);
        }
        // 添加用户信息到数据库
        return FaceApi.getInstance().userAdd(user);
    }

    /**
     * 获取底库数量
     *
     * @return
     */
    public int getmUserNum() {
        return users.size();
    }

    // 删除识别记录
    public boolean deleteRecords(String userName) {
        boolean ret = false;
        if (TextUtils.isEmpty(userName)) {
            return ret;
        }
        ret = DBManager.getInstance().deleteRecords(userName);
        return ret;
    }

    // 删除识别记录
    public boolean deleteRecords(String startTime, String endTime) {
        boolean ret = false;
        if (TextUtils.isEmpty(startTime) && TextUtils.isEmpty(endTime)) {
            return ret;
        }
//        ret = DBManager.getInstance().deleteRecords(startTime, endTime);
        return ret;
    }

    // 清除识别记录
    public int cleanRecords() {
        boolean ret = false;
        int num = DBManager.getInstance().cleanRecords();
        return num;
    }

    public void clean(){
        users = null;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }


}
