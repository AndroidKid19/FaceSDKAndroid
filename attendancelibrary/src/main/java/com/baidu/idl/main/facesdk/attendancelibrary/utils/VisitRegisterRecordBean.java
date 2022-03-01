package com.baidu.idl.main.facesdk.attendancelibrary.utils;

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
public class VisitRegisterRecordBean {
    private List<VisitRegisterRecordBean> list;
    private String name;
    private String personalPhotos;
    private String certificateNumber;

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
}
