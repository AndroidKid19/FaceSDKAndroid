/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.example.datalibrary.model;

import com.baidu.idl.main.facesdk.model.Feature;

/**
 * 用户实体类
 */
public class User extends Feature {

    private String userInfo = "";
    private int userIndex = -1;

    public int getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(int userIndex) {
        this.userIndex = userIndex;
    }


    public User() {
    }


    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }


}
