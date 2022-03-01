package com.baidu.idl.main.facesdk.attendancelibrary.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: FaceSDKAndroid
 * @Package: com.baidu.idl.main.facesdk.attendancelibrary.utils
 * @ClassName: VisitRegisterRecordBean
 * @Description:
 * @Author: Yuan
 * @CreateDate: 2022/2/23 10:34
 * @UpdateUser: 更新者
 * @UpdateDate: 2022/2/23 10:34
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class VisitRegisterRecordBean implements Parcelable {
    private List<VisitRegisterRecordBean> list;
    private String name;
    private String personalPhotos;
    private String certificateNumber;
    private String orgTitle;

    public String getOrgTitle() {
        return orgTitle;
    }

    public void setOrgTitle(String orgTitle) {
        this.orgTitle = orgTitle;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersonalPhotos() {
        return personalPhotos;
    }

    public void setPersonalPhotos(String personalPhotos) {
        this.personalPhotos = personalPhotos;
    }

    public List<VisitRegisterRecordBean> getList() {
        return list;
    }

    public void setList(List<VisitRegisterRecordBean> list) {
        this.list = list;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.list);
        dest.writeString(this.name);
        dest.writeString(this.personalPhotos);
        dest.writeString(this.certificateNumber);
        dest.writeString(this.orgTitle);
    }

    public void readFromParcel(Parcel source) {
        this.list = new ArrayList<VisitRegisterRecordBean>();
        source.readList(this.list, VisitRegisterRecordBean.class.getClassLoader());
        this.name = source.readString();
        this.personalPhotos = source.readString();
        this.certificateNumber = source.readString();
        this.orgTitle = source.readString();
    }

    public VisitRegisterRecordBean() {
    }

    protected VisitRegisterRecordBean(Parcel in) {
        this.list = new ArrayList<VisitRegisterRecordBean>();
        in.readList(this.list, VisitRegisterRecordBean.class.getClassLoader());
        this.name = in.readString();
        this.personalPhotos = in.readString();
        this.certificateNumber = in.readString();
        this.orgTitle = in.readString();
    }

    public static final Parcelable.Creator<VisitRegisterRecordBean> CREATOR = new Parcelable.Creator<VisitRegisterRecordBean>() {
        @Override
        public VisitRegisterRecordBean createFromParcel(Parcel source) {
            return new VisitRegisterRecordBean(source);
        }

        @Override
        public VisitRegisterRecordBean[] newArray(int size) {
            return new VisitRegisterRecordBean[size];
        }
    };
}
