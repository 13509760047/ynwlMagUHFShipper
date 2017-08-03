package com.fxb.shipper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.uhf.magic.reader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * Created by dxl on 2017-06-24.
 */

public class ShipperWriteActivity extends Activity {
    private static final String TAG = "ShipperWriteActivity";

    private RequestQueue mRequestQueue;

    private TextView tv_shipperEpc;
    private TextView tv_shipperCarNum;
    private EditText et_maoZhong;
    private EditText et_piZhong;
    private TextView tv_jingZhong;
    private TextView tv_resultView;

    private Button btn_shipperReadEpc;
    private Button btn_shipperReading;
    private Button btn_shipperWritting;

    private Handler mHandler = new ShipperWriteActivity.MainHandler();
    private String m_strresult = "";
    private static String myResult;
    private StringBuilder cardNumData;
    private String maoWeight, piWeight, jingWeight;

    /*
     *设置EPC参数
     * */
    private byte btMemBank;
    private int nadd;
    private int ndatalen;
    private String mimaStr;
    private String[] spStr;
    private ImageView imageView;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shipper_write);
//        初始化界面
        initView();
//        注册 hander
        reader.reg_handler(mHandler);
//        事件监听
        onClick();
    }

    private void initView() {
        mRequestQueue = Volley.newRequestQueue(this);

        tv_shipperEpc = (TextView) findViewById(R.id.tv_shipper_epc);
        tv_shipperCarNum = (TextView) findViewById(R.id.tv_shipper_carnum);
        et_maoZhong = (EditText) findViewById(R.id.et_maozhong);
        et_piZhong = (EditText) findViewById(R.id.et_pizhong);
        tv_jingZhong = (TextView) findViewById(R.id.tv_jingzhong);
        tv_resultView = (TextView) findViewById(R.id.tv_resultView);

        imageView = (ImageView) findViewById(R.id.shipper_image);

        btn_shipperReadEpc = (Button) findViewById(R.id.btn_shipper_readepc);
        btn_shipperReading = (Button) findViewById(R.id.btn_shipper_reading);
        btn_shipperWritting = (Button) findViewById(R.id.btn_shipper_writting);

    }

    private void onClick() {
        btn_shipperReadEpc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readEpc();
            }
        });
        btn_shipperReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readTag();
            }
        });
        btn_shipperWritting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upShipperMeadData();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    private void contrastShipper() {
        if (spStr == null) {
            return;
        }
        String url = "http://39.108.0.144/YJYNLogisticsSystem/appPublishInformation?action=getRealordShipper&";
        StringBuilder stringBuilder = new StringBuilder(url);
        stringBuilder.append("&CARNUM=").append(spStr[1]);
        Log.v(TAG, "上传地址为：" + stringBuilder.toString());
        StringRequest getContactRequest = new StringRequest(stringBuilder.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (TextUtils.isEmpty(s)) {
                    ToastUtil.getShortToastByString(ShipperWriteActivity.this, "服务器数据异常");
                    return;
                }
                try {
                    JSONObject o = new JSONObject(s);
                    if (o.getString("status").equals("0")) {
                        String localname = Sp.getStrings(ShipperWriteActivity.this, "name");
                        String intentname = o.getString("name");
                        Log.i(TAG, "onResponse: " + localname + "--" + intentname);
                        if (!localname.equals(intentname)) {
                            ToastUtil.getLongToastByString(ShipperWriteActivity.this, "所在发货商和订单发货商不符！");
                            finish();
                        } else {
                            ToastUtil.getLongToastByString(ShipperWriteActivity.this, "通过！");
                        }
                    } else if (o.getString("status").equals("1")) {
                        ToastUtil.getLongToastByString(ShipperWriteActivity.this, "请先确认是否接单！");
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToastUtil.getShortToastByString(ShipperWriteActivity.this, "网络异常，请稍后再试");
            }
        });
        mRequestQueue.add(getContactRequest);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            imageBitmap = (Bitmap) bundle.get("data");
            assert imageBitmap != null;
            imageView.setImageBitmap(imageBitmap);
        }
    }

    private void upShipperMeadData() {
        maoWeight = et_maoZhong.getText().toString().trim();
        piWeight = et_piZhong.getText().toString().trim();
        if (TextUtils.isEmpty(piWeight)) {
            ToastUtil.getShortToastByString(ShipperWriteActivity.this, "皮重不能为空");
            return;
        } else if (TextUtils.isEmpty(maoWeight)) {
            ToastUtil.getShortToastByString(ShipperWriteActivity.this, "毛重不能为空");
            return;
        } else {
            Double d_rough_weight = Double.parseDouble(maoWeight);
            Double d_tare = Double.parseDouble(piWeight);
            double d_weight_empty = d_rough_weight - d_tare;
            Log.e(TAG, "d_weight_empty: " + d_weight_empty);
            DecimalFormat df = new DecimalFormat("#.00");
            maoWeight = df.format(d_rough_weight);
            piWeight = df.format(d_tare);
            jingWeight = df.format(d_weight_empty);
            Log.e(TAG, "result: " + jingWeight);
            tv_jingZhong.setText(jingWeight);
        }
        if ("".equals(reader.m_strPCEPC)) {
            Toast.makeText(ShipperWriteActivity.this, "Please select the EPC tags",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String pn = tv_shipperCarNum.getText().toString().trim();
        if (TextUtils.isEmpty(pn)) {
            ToastUtil.getShortToastByString(ShipperWriteActivity.this, "车牌号不能为空,请先读卡");
            return;
        } else {
            //上传计量数据
            /*
            * 参数说明
            * 139.224.0.153：IP
            * LYMistSystem：项目名
            * appPublishInformation：类名
            * upShipperMeasData：action
            * CARDNUM：卡编号
            * CARNUM：车牌号
            * SHIPPERMAO：发货端毛重
            * SHIPPERPI：发货端皮重
            * SHIPPERJING：发货端净重
            * */
            String Shipperurl = "http://39.108.0.144/YJYNLogisticsSystem/appPublishInformation?action=upShipperMeasData&";
            StringBuilder stringBuilder = new StringBuilder(Shipperurl);
            stringBuilder.append("CARDNUM=").append(spStr[0]);
            stringBuilder.append("&CARNUM=").append(spStr[1]);
            stringBuilder.append("&SHIPPERMAO=").append(maoWeight);
            stringBuilder.append("&SHIPPERPI=").append(piWeight);
            stringBuilder.append("&SHIPPERJING=").append(jingWeight);
            Log.v(TAG, "上传地址为：" + stringBuilder.toString());
            StringRequest getContactRequest = new StringRequest(stringBuilder.toString(), new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    if (TextUtils.isEmpty(s)) {
                        ToastUtil.getShortToastByString(ShipperWriteActivity.this, "服务器数据异常");
                        return;
                    }
                    try {
                        JSONObject o = new JSONObject(s);
                        if (o.getString("status").equals("0")) {
                            //上传成功后重新写卡
                            writeTag();
                            if (imageBitmap != null) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //上传发货方图片
                                        String uploadShipperurl = "";
                                        uploadShipperServer(uploadShipperurl, spStr[1], getBitmapPath(), imageBitmap);
                                    }
                                }).start();
                            }
                        }
                        ToastUtil.getShortToastByString(ShipperWriteActivity.this, o.getString("msg"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ToastUtil.getShortToastByString(ShipperWriteActivity.this, "网络异常，请稍后再试");
                }
            });
            mRequestQueue.add(getContactRequest);
        }
    }

    private String getBitmapPath() {
        return "shipper" + System.currentTimeMillis() + ".jpg";
    }

    private boolean uploadShipperServer(String targetUrl, String carnum, String fileName, Bitmap bm) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(targetUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"CARNUM\"" + end);
            dos.writeBytes(end);
            dos.writeBytes(carnum + end);

            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"imagePath\"; filename=\"" + fileName + "\"" + end);
            dos.writeBytes(end);

            dos.write(Util.Bitmap2Bytes(bm));
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            JSONObject resultJson = new JSONObject(result);
            int i  = resultJson.getInt("status");
            if (i == 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.getShortToastByString(ShipperWriteActivity.this, "上传成功");
                    }
                });
            }
            dos.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                httpURLConnection = null;
            }
        }
        return true;
    }

    /**
     * 读取EPC
     */
    private void readEpc() {
        android.hardware.uhf.magic.reader.InventoryLables();
    }

    /**
     * 读取tag
     */
    private void readTag() {
        if ("".equals(reader.m_strPCEPC)) {
            Toast.makeText(ShipperWriteActivity.this, "Please select the EPC tags",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //操作区域 EPC(代码为1)
        btMemBank = (byte) 1;
        //起始地址 2
        nadd = 2;
        //读取长度
        ndatalen = 20;
        //密码
        mimaStr = "00000000";

        if (mimaStr == null || mimaStr.equals("")) {
            m_strresult += "Please enter your 8 - digit password!!\n";
            tv_resultView.setText(m_strresult);
            return;
        }
        byte[] passw = reader.stringToBytes(mimaStr);
        byte[] epc = reader.stringToBytes(reader.m_strPCEPC);
        if (null != epc) {
            if (btMemBank == 1) {
                reader.ReadLables(passw, epc.length, epc,
                        (byte) btMemBank, nadd, ndatalen);
            } else {
                reader.ReadLables(passw, epc.length, epc,
                        (byte) btMemBank, nadd, ndatalen);
            }
        }
    }

    /*
    * 写tag
    * */
    private void writeTag() {
        if ("".equals(reader.m_strPCEPC)) {
            Toast.makeText(ShipperWriteActivity.this, "Please select the EPC tags",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //操作区域 EPC(代码为1)
        btMemBank = (byte) 1;
        //起始地址 2
        nadd = 2;
        //读取长度
        ndatalen = 20;
        //密码
        mimaStr = "00000000";

        if (mimaStr == null || mimaStr.equals("")) {
            m_strresult += "Please enter your 8 - digit password!!\n";
            tv_resultView.setText(m_strresult);
            return;
        }
        byte[] passw = reader.stringToBytes(mimaStr);
        byte[] pwrite = new byte[ndatalen * 2];

        String pn = tv_shipperCarNum.getText().toString().trim();
        if (TextUtils.isEmpty(pn)) {
            ToastUtil.getShortToastByString(ShipperWriteActivity.this, "车牌号不能为空,请先读卡");
            return;
        } else {
            StringBuilder data = new StringBuilder();
            data.append(myResult);
            data.append(",");
            data.append(maoWeight);
            data.append(",");
            data.append(piWeight);
            data.append(",");
            data.append(jingWeight);
            data.append(",");
            String dataE = data.toString();
            Log.v(TAG, "写入的数据=================" + dataE);

            byte[] myByte = reader.stringToBytes(StringUtils.toHexString(dataE));
            System.arraycopy(myByte, 0, pwrite, 0,
                    myByte.length > ndatalen * 2 ? ndatalen * 2
                            : myByte.length);
            byte[] epc = reader.stringToBytes(reader.m_strPCEPC);
            {
                reader.Writelables(passw, epc.length, epc, btMemBank,
                        (byte) nadd, (byte) ndatalen * 2, pwrite);
            }
        }
    }

    /*
    * 注册 hander
    * */
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == reader.editepcsmsg) {
                tv_resultView.setText((String) msg.obj);
            }
            //读取tag
            if (msg.what == reader.msgreadwrireepc) {
                if (msg.obj != null) {
                    m_strresult = (String) msg.obj;
                    if (!m_strresult.contains("Error")) {
                         /*
                        * StringUtils.toStringHex(m_strresult).split(",")截取字符串重组数组
                        * substring(startst,endstr)截取从startst到endstr之间的字符
                        * */
                        tv_resultView.setText(StringUtils.toStringHex(m_strresult));
                        spStr = StringUtils.toStringHex(m_strresult).split(",");
                        myResult = spStr[0] + "," + spStr[1];
                        String s = spStr[1];
                        String sub_result = s.substring(0, 1);
                        if (StringUtils.isNumeric(sub_result)) {
                            String sub_result1 = s.substring(1, 7);
                            int province = Integer.valueOf(sub_result);
                            String pv = "";
                            switch (province) {
                                case 1:
                                    pv = "晋";
                                    break;
                                case 2:
                                    pv = "陕";
                                    break;
                                case 3:
                                    pv = "豫";
                                    break;
                                case 4:
                                    pv = "冀";
                                    break;
                                case 5:
                                    pv = "鲁";
                                    break;

                                default:
                                    break;
                            }
                            cardNumData = new StringBuilder();
                            cardNumData.append(pv);
                            cardNumData.append(sub_result1);
                            tv_shipperCarNum.setText(cardNumData.toString());
                        } else {
                            ToastUtil.getShortToastByString(ShipperWriteActivity.this, "请保证首个字符为数字");
                        }
                    }
                } else {
                    ToastUtil.getShortToastByString(ShipperWriteActivity.this, m_strresult);
                }
            }
            if (msg.what == reader.msgreadwrite) {
                if (msg.obj != null) {
                    m_strresult = (String) msg.obj;
                    tv_resultView.setText(m_strresult);
                }
            }
            //读卡信息
            if (msg.what == reader.msgreadepc) {
                String readerdata = (String) msg.obj;
                tv_shipperEpc.setText(readerdata);
                reader.m_strPCEPC = readerdata;
            }
            contrastShipper();
        }
    }

}
