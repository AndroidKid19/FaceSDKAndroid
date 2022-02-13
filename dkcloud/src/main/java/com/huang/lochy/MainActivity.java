package com.huang.lochy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.DKCloudID.DKCloudID;
import com.DKCloudID.IDCard;
import com.DKCloudID.IDCardData;
import com.DKCloudID.ImageUtil;
import com.Exception.CardNoResponseException;
import com.Exception.DKCloudIDException;
import com.Tool.MyTTS;
import com.Tool.UtilTool;
import com.Exception.DeviceNoResponseException;
import com.dk.uartnfc.R;
import com.dk.uartnfc.SamVIdCard;
import com.dk.uartnfc.SerialManager;
import com.Tool.StringTool;

import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static com.DKCloudID.ClientDispatcher.SAM_V_APDU_COM;
import static com.DKCloudID.ClientDispatcher.SAM_V_ERROR_COM;
import static com.DKCloudID.ClientDispatcher.SAM_V_FRAME_START_CODE;
import static com.DKCloudID.ClientDispatcher.SAM_V_INIT_COM;

public class MainActivity extends AppCompatActivity {
    final String[] botes = new String[]{"9600", "19200", "38400", "57600", "115200", "230400", "460800", "500000", "576000", "921600", "1000000", "1152000"};

    private static TextView msgTextView;
    private Spinner spSerial;
    private EditText edInput;
    private Button btSend;
    private Spinner spBote;
    private Button btOpen;
    private AlertDialog alertDialog;
    private MyTTS myTTS;

    SerialManager serialManager;
//    SerialManager serialManager1;
    String selectSerialName;
    String selectBaudRate;

    public static final int NUMBER_OF_REPARSING = 5;              /*解析失败时，重新解析的次数*/
    DKCloudID dkCloudID = null;
    static int err_cnt = 0;
    int schedule = 1;
    byte[] initData;

    private ProgressDialog readWriteDialog = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msgTextView = (TextView) findViewById(R.id.msgText);
        spSerial = (Spinner) findViewById(R.id.sp_serial);
        edInput = (EditText) findViewById(R.id.ed_input);
        btSend = (Button) findViewById(R.id.bt_send);
        spBote = (Spinner) findViewById(R.id.sp_bote);
        btOpen = (Button) findViewById(R.id.bt_open);

        readWriteDialog = new ProgressDialog(MainActivity.this);
        readWriteDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置ProgressDialog 标题
        readWriteDialog.setTitle("请稍等");
        // 设置ProgressDialog 提示信息
        readWriteDialog.setMessage("正在读写数据……");
        readWriteDialog.setMax(100);

        myTTS = new MyTTS(this);

        serialManager = new SerialManager();

        edInput.setText("aa020401");

        //如果是护照则跳出机读码输入框
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        final View textEntryView = factory.inflate(R.layout.layout, null);

        final EditText passportNoInputEditText = (EditText) textEntryView.findViewById(R.id.passportNoInputEditText);
        passportNoInputEditText.setText("E958722656");
        passportNoInputEditText.setHint(new SpannedString("请输入护照号(E123456789)"));
        passportNoInputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        final EditText birthDateInputEditText = (EditText) textEntryView.findViewById(R.id.birthDateInputEditText);
        birthDateInputEditText.setHint(new SpannedString("请输入出生日期(9012255)"));
        birthDateInputEditText.setText("9210125");
        birthDateInputEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        birthDateInputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});

        final EditText expiryDateInputEditText = (EditText) textEntryView.findViewById(R.id.expiryDateInputEditText);
        expiryDateInputEditText.setHint(new SpannedString("请输入有效期(2712257)"));
        expiryDateInputEditText.setText("2702124");
        expiryDateInputEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        expiryDateInputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("请输入护照上的信息：")
                .setMessage("请输入护照号码、出生日期、有效期")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (passportNoInputEditText.getText().length() != 0
                                && birthDateInputEditText.getText().length() != 0
                                && expiryDateInputEditText.getText().length() != 0) {

                            //验证并读取文件
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String MZR_string = "" + passportNoInputEditText.getText() + birthDateInputEditText.getText() + expiryDateInputEditText.getText();
                                        byte MZR_bytes[] = MZR_string.getBytes();
                                        byte MZR_cmd_bytes[] = new byte[MZR_bytes.length + 3];
                                        MZR_cmd_bytes[0] = (byte)0xAA;
                                        MZR_cmd_bytes[1] = (byte)0x19;
                                        MZR_cmd_bytes[2] = (byte)0x30;
                                        System.arraycopy(MZR_bytes, 0, MZR_cmd_bytes, 3, MZR_bytes.length);

                                        byte[] returnBytes = serialManager.sendWithReturn(MZR_cmd_bytes, 1000);
                                        if (StringTool.byteHexToSting(returnBytes).equals("AA01FE")) {
                                            //==============================读取文件1======================================
                                            returnBytes = serialManager.sendWithReturn(StringTool.hexStringToBytes("aa023101"), 2000);

                                            //和校验
                                            byte bcc_sum = 0;
                                            for ( int i=0; i<returnBytes.length - 1; i++ ) {
                                                bcc_sum ^= returnBytes[i];
                                            }
                                            if ( bcc_sum != returnBytes[returnBytes.length - 1] ) {
                                                System.out.println("和校验失败");
                                                logViewln("和校验失败！");
                                                return;
                                            }

                                            //提取数据
                                            byte dg1_byte[] = new byte[returnBytes.length - 5];
                                            System.arraycopy(returnBytes, 4, dg1_byte, 0, dg1_byte.length);

                                            final String dg1_String = new String(dg1_byte, "UTF-8");
                                            logViewln( "DG1File： " + dg1_String );

                                            //==============================读取文件11======================================
                                            returnBytes = serialManager.sendWithReturn(StringTool.hexStringToBytes("aa02310B"), 2000);

                                            //和校验
                                            bcc_sum = 0;
                                            for ( int i=0; i<returnBytes.length - 1; i++ ) {
                                                bcc_sum ^= returnBytes[i];
                                            }
                                            if ( bcc_sum != returnBytes[returnBytes.length - 1] ) {
                                                System.out.println("和校验失败");
                                                logViewln("和校验失败！");
                                                return;
                                            }

                                            //提取数据
                                            byte dg11_byte[] = new byte[returnBytes.length - 5];
                                            System.arraycopy(returnBytes, 4, dg11_byte, 0, dg11_byte.length);

                                            final String dg11_String = new String(dg11_byte, "UTF-8");
                                            logViewln( "DG11File： " + dg11_String );
                                            logViewln( "照片读取中..."  );

                                            //==============================读取文件2======================================
                                            returnBytes = serialManager.sendWithReturn(StringTool.hexStringToBytes("aa023102"), 10000);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    msgTextView.setText( msgTextView.getText().toString().replace("\r\n照片读取中...", "") );
                                                }
                                            });

                                            //和校验
                                            bcc_sum = 0;
                                            for ( int i=0; i<returnBytes.length - 1; i++ ) {
                                                bcc_sum ^= returnBytes[i];
                                            }
                                            if ( bcc_sum != returnBytes[returnBytes.length - 1] ) {
                                                System.out.println("和校验失败");
                                                logViewln("和校验失败！");
                                                return;
                                            }

                                            //提取数据
                                            final byte dg2_byte[] = new byte[returnBytes.length - 5];
                                            System.arraycopy(returnBytes, 4, dg2_byte, 0, dg2_byte.length);

                                            String dg2_String = StringTool.byteHexToSting(dg2_byte);

                                            //找到图片的位置
                                            int startIndex = dg2_String.indexOf("5F2E");
                                            if (startIndex < 0) {
                                                startIndex = dg2_String.indexOf("7F2E");
                                                if (startIndex < 0) {
                                                    logViewln("未找到图像数据！");
                                                    return;
                                                }
                                            }

                                            System.out.println("DG2 picture start index:" + startIndex);

                                            //提取照片长度
                                            dg2_String = dg2_String.substring(startIndex);
                                            int picture_data_len = Integer.parseInt(dg2_String.substring(6, 10), 16);
                                            System.out.println("DG2 picture data len:" + picture_data_len);
                                            dg2_String = dg2_String.substring(10);
                                            System.out.println("DG2 picture data:" + dg2_String);
                                            byte picture_bytes[] = StringTool.hexStringToBytes(dg2_String);

                                            //转换成照片
                                            try {
                                                FaceInfo faceInfo = new FaceInfo(new ByteArrayInputStream(picture_bytes));

                                                for (FaceImageInfo faceImageInfo : faceInfo.getFaceImageInfos() ) {
                                                    int imageLength = faceImageInfo.getImageLength();
                                                    DataInputStream dataInputStream = new DataInputStream(faceImageInfo.getImageInputStream());
                                                    byte[] buffer = new byte[imageLength];
                                                    dataInputStream.readFully(buffer, 0, imageLength);
                                                    InputStream inputStream = new ByteArrayInputStream(buffer, 0, imageLength);

                                                    Bitmap bitmap = ImageUtil.decodeImage(
                                                            MainActivity.this, faceImageInfo.getMimeType(), inputStream);

                                                    final SpannableString ss = new SpannableString(msgTextView.getText().toString() + "[smile]");
                                                    //得到要显示图片的资源
                                                    Drawable d = new BitmapDrawable(bitmap);//Drawable.createFromPath("mnt/sdcard/photo.bmp");
                                                    //设置高度
                                                    d.setBounds(0, 0, d.getIntrinsicWidth()/2, d.getIntrinsicHeight()/2);
                                                    //跨度底部应与周围文本的基线对齐
                                                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                                                    //附加图片
                                                    ss.setSpan(span, msgTextView.getText().length(),msgTextView.getText().length()+"[smile]".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            msgTextView.setText(ss);
                                                        }
                                                    });
                                                    inputStream.close();
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }finally {

                                            }

//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    MainActivity.logViewln( "DG2File： " + dg2_String.length() / 2 + "字节" );
//                                                }
//                                            });
                                        }
                                    } catch (DeviceNoResponseException e) {
                                        e.printStackTrace();
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();

        //设置串口数据接收监听
        serialManager.setOnReceiveDataListener(new SerialManager.onReceiveDataListener() {
            @Override
            public void OnReceiverData(String portNumberString, byte[] dataBytes) {
                final String portNumber = portNumberString;
                final byte[] data = dataBytes;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("[MainActivity]" + portNumber + "接收(" + data.length + ")：" + StringTool.byteHexToSting(data) + "\r\n");

                        if ((data.length >= 3) && (data[0] == (byte)0xAA) ) {
                            if (StringTool.byteHexToSting(data).equals("AA01EA")) {
                                refreshLogView("卡片已拿开！\r\n");
                                hidDialog();
                                if ( dkCloudID != null ) {
                                    dkCloudID.Close();
                                }

                                //卡片拿开退出输入框
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertDialog.hide();
                                    }
                                });
                            }
                            else if ( (data.length > 4)            //寻到cpu卡，则当作护照处理
                                    && (data[0] == (byte)0xAA)
                                    && (data[2] == (byte)0x01)
                                    && ((data[3] == (byte)0x04) || (data[3] == (byte)0x03)) ) {

                                //判断是不是护照
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            byte[] returnBytes = serialManager.sendWithReturn(StringTool.hexStringToBytes("aa0d1600a4040c07a0000002471001"), 500);
                                            if (StringTool.byteHexToSting(returnBytes).equals("AA03169000")) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        alertDialog.show();
                                                    }
                                                });
                                            }
                                        } catch (DeviceNoResponseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                            else if ( (data.length > 4)            //寻到M1卡
                                    && (data[0] == (byte)0xAA)
                                    && (data[2] == (byte)0x01)
                                    && ((data[3] == (byte)0x01)) ) {

                                //读M1卡块1
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            byte[] returnBytes = serialManager.sendWithReturn(StringTool.hexStringToBytes("AA020401"), 500);
                                            logViewln("返回:" + StringTool.byteHexToSting(returnBytes));
                                        } catch (DeviceNoResponseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        msgTextView.setText("");
                                        refreshLogView(portNumber + "接收(" + data.length + ")：" + StringTool.byteHexToSting(data) + "\r\n");
                                    }
                                });
                            }
                        }
                        else if ( (data.length >= 3) && (data[0] == SAM_V_FRAME_START_CODE) && (data[3] == SAM_V_INIT_COM) ) {
                            //校验数据
                            try {
                                SamVIdCard.verify(data);
                            } catch (CardNoResponseException e) {
                                e.printStackTrace();

                                logViewln( "正在重新解析.." );
                                serialManager.send(StringTool.hexStringToBytes("AA0118"));
                                return;
                            }

                            System.out.println("开始解析");
                            logViewln(null);
                            logViewln( "正在读卡，请勿移动身份证!" );
                            myTTS.speak("正在读卡，请勿移动身份证");

                            initData = Arrays.copyOfRange( data, 4, data.length - 1 );
                            SamVIdCard samVIdCard = new SamVIdCard(serialManager, initData);
                            IDCard idCard = new IDCard(samVIdCard);

                            int cnt = 0;
                            do {
                                try {
                                    /**
                                     * 获取身份证数据，带进度回调，如果不需要进度回调可以去掉进度回调参数或者传入null
                                     * 注意：此方法为同步阻塞方式，需要一定时间才能返回身份证数据，期间身份证不能离开读卡器！
                                     */
                                    IDCardData idCardData = idCard.getIDCardData(new IDCard.onReceiveScheduleListener() {
                                        @Override
                                        public void onReceiveSchedule(int rate) {  //读取进度回调
                                            showReadWriteDialog("正在读取身份证信息,请不要移动身份证", rate);
                                        }
                                    });

                                    /**
                                     * 读取成功，显示身份证数据，在此提示用户读取成功或者打开蜂鸣器提示
                                     */
                                    myTTS.speak("读取成功");

                                    showIDMsg(idCardData);
                                    //返回读取成功
                                    return;
                                } catch (DKCloudIDException e) {   //服务器返回异常，重复5次解析
                                    e.printStackTrace();

                                    //显示错误信息
                                    logViewln(e.getMessage());
                                }
                                catch (CardNoResponseException e) {    //卡片读取异常，直接退出，需要重新读卡
                                    e.printStackTrace();

                                    //显示错误信息
                                    logViewln(e.getMessage());

                                    //返回读取失败
                                    myTTS.speak("请不要移动身份证");
                                    logViewln( "正在重新解析.." );
                                    serialManager.send(StringTool.hexStringToBytes("AA0118"));
                                    return;
                                } finally {
                                    //读卡结束关闭进度条显示
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (readWriteDialog.isShowing()) {
                                                readWriteDialog.dismiss();
                                            }
                                            readWriteDialog.setProgress(0);
                                        }
                                    });
                                }
                            }while ( cnt++ < 5 );  //如果服务器返回异常则重复读5次直到成功

                        }
                        else if (StringTool.byteHexToSting(data).equals("aa01ea")) {
                            if ( dkCloudID != null ) {
                                dkCloudID.Close();
                            }
                            System.out.println("卡片已经拿开");
                            alertDialog.hide();
                            hidDialog();
                        }
                    }
                }).start();
            }
        });

        iniview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serialManager != null) {
            serialManager.close();
        }
    }

    private void iniview() {
        final List<String> ports = serialManager.getAvailablePorts();  //获取可用的串口
        System.out.println("可用的串口列表为：" + ports.toString());

        //配置选择串口号的选择器
        SpAdapter spAdapter = new SpAdapter(this);
        spAdapter.setDatas( ports.toArray(new String[ports.size()]) );
        spSerial.setAdapter(spAdapter);
        spSerial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectSerialName = ports.get(position);
                if ( serialManager.isOpen() ) {
                    serialManager.close();
                    serialManager.open(selectSerialName, selectBaudRate);
                    updataSendBt();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        //配置选择波特率的选择器
        SpAdapter spAdapter2 = new SpAdapter(this);
        spAdapter2.setDatas(botes);
        spBote.setAdapter(spAdapter2);
        spBote.setSelection(4);
        spBote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectBaudRate = botes[position];
                if ( serialManager.isOpen() ) {
                    serialManager.close();
                    serialManager.open(selectSerialName, selectBaudRate);
                    updataSendBt();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //打开串口按键监听
        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( serialManager.isOpen() ) {
                    serialManager.close();
                }
                else {
                    serialManager.open(selectSerialName, selectBaudRate);
                    System.err.println("可用的串口列表为：" + selectSerialName +" ---"+selectBaudRate);
                    //serialManager.open("/dev/ttyS3", "115200");
                }

                updataSendBt();
            }
        });

        //发送数据监听
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edInput.getText().toString().length() > 0) {
                    if (serialManager.isOpen()) {
                        serialManager.send( StringTool.hexStringToBytes( edInput.getText().toString()) );
                        msgTextView.setText("");
                        refreshLogView("发送：" +  StringTool.byteHexToSting(StringTool.hexStringToBytes( edInput.getText().toString())) + "\r\n");
                    } else {
                        Toast.makeText(getBaseContext(), "串口未打开！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "未输入指令！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        msgTextView.setText("正在搜索设备...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;

                for (String thePort:ports) {
                    try {

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (serialManager.open(thePort, "115200")) {
                            byte[] bytes = serialManager.sendWithReturn(new byte[]{(byte) 0xAA, 0x01, (byte)0xB0}, 200);
                            if (bytes[0] == (byte) 0xAA) {

                                final int selection = i;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        spSerial.setSelection(selection);
                                        msgTextView.setText("找到串口" + selectSerialName + "上的设备");
                                    }
                                });

                                try {
                                    serialManager.sendWithReturn(StringTool.hexStringToBytes("AA0495FF1476"));  //配置NFC模块
                                } catch (DeviceNoResponseException e1) {
                                    e1.printStackTrace();
                                }
                                break;
                            } else {
                                serialManager.close();
                            }
                        }
                    } catch (DeviceNoResponseException e) {
                        e.printStackTrace();
                        serialManager.close();
                    }
                    i++;
                }
            }
        }).start();
    }

    //更新按键状态
    private void updataSendBt() {
        if ( serialManager.isOpen() ) {
            btOpen.setText("关闭串口");
            Toast.makeText(getBaseContext(), "串口已打开！", Toast.LENGTH_SHORT).show();
        }
        else {
            btOpen.setText("打开串口");
            Toast.makeText(getBaseContext(), "串口已关闭！", Toast.LENGTH_SHORT).show();
        }
    }

    //更新显示框状态
    synchronized void refreshLogView(String msg){
        final String theMsg = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgTextView.append(theMsg);
            }
        });
    }

    private void logViewln(String string) {
        final String msg = string;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (msg == null) {
                    msgTextView.setText("");
                    return;
                }

                if (msgTextView.length() > 500) {
                    msgTextView.setText("");
                }
                msgTextView.append(msg + "\r\n");
                int offset = msgTextView.getLineCount() * msgTextView.getLineHeight();
                if(offset > msgTextView.getHeight()){
                    msgTextView.scrollTo(0,offset-msgTextView.getHeight());
                }
            }
        });
    }

    private void showIDMsg(IDCardData msg) {
        final IDCardData idCardData = msg ;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgTextView.setText(idCardData.toString() + "\r\n");


                SpannableString ss = new SpannableString(msgTextView.getText().toString()+"[smile]");
                //得到要显示图片的资源
                Drawable d = new BitmapDrawable(idCardData.PhotoBmp);//Drawable.createFromPath("mnt/sdcard/photo.bmp");
                if (d != null) {
                    //设置高度
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    //跨度底部应与周围文本的基线对齐
                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                    //附加图片
                    ss.setSpan(span, msgTextView.getText().length(),msgTextView.getText().length()+"[smile]".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    msgTextView.setText(ss);
                    //msgTextView.setText("\r\n");
                    System.out.println(idCardData.PhotoBmp);
                }
            }
        });
    }

    //进度条显示
    private void showReadWriteDialog(String msg, int rate) {
        final int theRate = rate;
        final String theMsg = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((theRate == 0) || (theRate == 100)) {
                    readWriteDialog.dismiss();
                    readWriteDialog.setProgress(0);
                } else {
                    readWriteDialog.setMessage(theMsg);
                    readWriteDialog.setProgress(theRate);
                    if (!readWriteDialog.isShowing()) {
                        readWriteDialog.show();
                    }
                }
            }
        });
    }

    //隐藏进度条
    private void hidDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                readWriteDialog.dismiss();
            }
        });
    }
}
