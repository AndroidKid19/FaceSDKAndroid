package com.baidu.idl.main.facesdk.registerlibrary.user.utils;

/**
 * @ProjectName: FaceSDKAndroid
 * @Package: com.baidu.idl.main.facesdk.registerlibrary.user.utils
 * @ClassName: JsonRootBean
 * @Description:
 * @Author: Yuan
 * @CreateDate: 2022/3/1 9:57
 * @UpdateUser: 更新者
 * @UpdateDate: 2022/3/1 9:57
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class JsonRootBean {

    private String respDesc;
    private UploadFileBean data;
    private String respCode;

    public void setRespDesc(String respDesc) {
        this.respDesc = respDesc;
    }

    public String getRespDesc() {
        return respDesc;
    }

    public void setData(UploadFileBean data) {
        this.data = data;
    }

    public UploadFileBean getData() {
        return data;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespCode() {
        return respCode;
    }

}
