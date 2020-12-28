package com.example.adapterbreaker;

import android.bluetooth.BluetoothGatt;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class MainActivity extends AppCompatActivity {

	TextView device_data;
	EditText editText;
	EditText begin_year,begin_month,begin_day;
	EditText end_year,end_month,end_day;
	String name;
	String Month;

	LinearLayout linearLayout_oximeter;
	LinearLayout linearLayout_thermometer;
	LinearLayout linearLayout_public;

	LineChartView lineChartView1;
	LineChartView lineChartView2;

	String uuid_service = "";
	String uuid_notify = "";


	int bmp;
	int spo2;
	double pi;
	float temperature;

	boolean flag_bmp = false;
	boolean flag_spo2 = false;
	boolean flag_pi = false;

	String str, readline, result = "";
	URL url;
	HttpURLConnection urlConnection;
	InputStream inputStream;
	BufferedReader bufferedReader;

	String str_acquire, readline_acquire, result_acquire = "";
	URL url_acquire;
	HttpURLConnection urlConnection_acquire;
	InputStream inputStream_acquire;
	BufferedReader bufferedReader_acquire;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		linearLayout_public = findViewById(R.id.LinearLayout_public);
		linearLayout_public.setVisibility(View.GONE);
		linearLayout_oximeter = findViewById(R.id.LinearLayout_oximeter);
		linearLayout_oximeter.setVisibility(View.GONE);
		linearLayout_thermometer = findViewById(R.id.LinearLayout_thermometer);
		linearLayout_thermometer.setVisibility(View.GONE);


		device_data = findViewById(R.id.data);

		editText = findViewById(R.id.editText);

		begin_year = findViewById(R.id.begin_year);
		begin_month = findViewById(R.id.begin_month);
		begin_day = findViewById(R.id.begin_day);

		end_year = findViewById(R.id.end_year);
		end_month = findViewById(R.id.end_month);
		end_day = findViewById(R.id.end_day);

		lineChartView1 = findViewById(R.id.lineChartView1);
		lineChartView2 = findViewById(R.id.lineChartView2);

		Calendar cd = Calendar.getInstance();

		if (cd.get(Calendar.MONTH)+1 < 10){
			Month = "0"+(cd.get(Calendar.MONTH)+1);
		}else {
			Month = ""+cd.get(Calendar.MONTH)+1;
		}

		begin_year.setText(cd.get(Calendar.YEAR)+"");
		begin_month.setText(Month);
		begin_day.setText(cd.get(Calendar.DATE)+"");
		end_year.setText(cd.get(Calendar.YEAR)+"");
		end_month.setText(Month);
		end_day.setText((cd.get(Calendar.DATE)+1)+"");


		SCAN();
	}

	public void SCAN(){
		BleManager.getInstance().init(getApplication());
		BleManager.getInstance().enableLog(true).setReConnectCount(1, 5000).setConnectOverTime(20000).setOperateTimeout(5000);
		BleScanRuleConfig bleScanRuleConfig = new BleScanRuleConfig.Builder().setScanTimeOut(-1).build();
		BleManager.getInstance().initScanRule(bleScanRuleConfig);

		BleManager.getInstance().scan(new BleScanCallback() {
			@Override
			public void onScanFinished(List<BleDevice> scanResultList) { }

			@Override
			public void onScanStarted(boolean success) { }

			@Override
			public void onScanning(BleDevice bleDevice) {
				if (bleDevice.getName() != null){
					if (bleDevice.getName().equals("My Oximeter")){
						BleManager.getInstance().cancelScan();

						linearLayout_public.setVisibility(View.VISIBLE);
						linearLayout_thermometer.setVisibility(View.GONE);
						linearLayout_oximeter.setVisibility(View.VISIBLE);

						name = bleDevice.getName();
						editText.setText(name);

						CONNECT(bleDevice);

					}else if (bleDevice.getName().equals("e-Thermometer")){
						BleManager.getInstance().cancelScan();

						linearLayout_public.setVisibility(View.VISIBLE);
						linearLayout_oximeter.setVisibility(View.GONE);
						linearLayout_thermometer.setVisibility(View.VISIBLE);

						name = bleDevice.getName();
						editText.setText(name);

						CONNECT(bleDevice);
					}
				}
			}
		});
	}

	public void CONNECT(BleDevice bleDevice){
		BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
			@Override
			public void onStartConnect() { }

			@Override
			public void onConnectFail(BleDevice bleDevice, BleException exception) { }

			@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
			@Override
			public void onConnectSuccess(final BleDevice bleDevice, BluetoothGatt gatt, int status) {
				if (bleDevice.getName().equals("My Oximeter")){
					uuid_service = "cdeacb80-5235-4c07-8846-93a37ee6b86d";
					uuid_notify = "cdeacb81-5235-4c07-8846-93a37ee6b86d";
				}else if (bleDevice.getName().equals("e-Thermometer")){
					uuid_service = "0000fff0-0000-1000-8000-00805f9b34fb";
					uuid_notify = "0000fff4-0000-1000-8000-00805f9b34fb";
				}

				BleManager.getInstance().notify(bleDevice, uuid_service, uuid_notify, new BleNotifyCallback() {
					@Override
					public void onNotifySuccess() { }

					@Override
					public void onNotifyFailure(final BleException exception) { }

					@Override
					public void onCharacteristicChanged(final byte[] data) {
						if (bleDevice.getName().equals("My Oximeter")){
							oximeter_data_processing(data);
						}else if (bleDevice.getName().equals("e-Thermometer")){
							thermometer_data_processing(data);
						}
					}
				});
			}

			@Override
			public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
				linearLayout_public.setVisibility(View.GONE);
				linearLayout_oximeter.setVisibility(View.GONE);
				linearLayout_thermometer.setVisibility(View.GONE);

				device_data.setText("");
				editText.setText("");

				SCAN();
			}
		});
	}





	public void oximeter_data_processing(byte[] data){
		String s = HexUtil.formatHexString(data,true);
		if (s.startsWith("81")){
			String[] d = s.split(" ");
			bmp = Integer.parseInt(d[1],16);
			spo2 = Integer.parseInt(d[2],16);
			pi = Integer.parseInt(d[3],16)/10.0;

			if (bmp == 255 || spo2 == 127 || pi == 0.0){
				return;
			}
			device_data.setText("脉搏：" + bmp + "  血氧含量：" + spo2 + "  血氧率：" + pi + " %");
			new Thread(network_send_oximeter).start();
		}
	}

	Runnable network_send_oximeter = new Runnable() {
		@Override
		public void run() {
			try {
				str = "http://192.168.137.1:8080/oximeter/?a=" + name + "&b=" + bmp + "&c=" + spo2 + "&d=" + pi;
				url = new URL(str);
				urlConnection = (HttpURLConnection)url.openConnection();
				urlConnection.setDoOutput(true);
				inputStream = urlConnection.getInputStream();
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
				while ((readline = bufferedReader.readLine()) != null){
					result += readline;
				}
				inputStream.close();
				bufferedReader.close();
				urlConnection.disconnect();
				result = "";
				Thread.sleep(1000);
			}catch (Exception e){
				//Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
			}
		}
	};





	public void thermometer_data_processing(byte[] data){
		try {
			final StringBuilder stringBuilder = new StringBuilder(data.length);
			for (byte byteChar : data) {
				stringBuilder.append(String.format("%02X", byteChar));
			}
			if ("F0FFFFFF000000000000".equals(stringBuilder.toString())) {
				device_data.setText("close");
			} else {
				if ("FF".equals(stringBuilder.toString().substring(0, 2))) {
					temperature = ((float) Integer.parseInt(stringBuilder.toString().substring(4, 8), 16)) / 10;
					device_data.setText("红外耳温枪温度：" + Float.toString(temperature));
					new Thread(network_send_thermometer).start();
				}
			}
		} catch (Exception e) {
			device_data.setText(e.toString());
		}
	}

	Runnable network_send_thermometer = new Runnable() {
		@Override
		public void run() {
			try {
				str = "http://192.168.137.1:8080/thermometer/?a=" + name + "&b=" + temperature;
				url = new URL(str);
				urlConnection = (HttpURLConnection)url.openConnection();
				urlConnection.setDoOutput(true);
				inputStream = urlConnection.getInputStream();
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
				while ((readline = bufferedReader.readLine()) != null){
					result += readline;
				}
				inputStream.close();
				bufferedReader.close();
				urlConnection.disconnect();
				result = "";
				Thread.sleep(1000);
			}catch (Exception e){
				//Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
			}
		}
	};





	public void acquire_oximeter(View view){
		try {
			if (view.getId() == R.id.bmp_bt){
				flag_bmp = true;
				flag_spo2 = false;
				flag_pi = false;
			}else if (view.getId() == R.id.spo2_bt){
				flag_bmp = false;
				flag_spo2 = true;
				flag_pi = false;
			}else if (view.getId() == R.id.pi_bt){
				flag_bmp = false;
				flag_spo2 = false;
				flag_pi = true;
			}
			new Thread(network_acquire_oximeter).start();
			Toast.makeText(this,"图片开始",Toast.LENGTH_LONG).show();
		}catch (Exception e){
			Log.e("CLM_9",e.toString());
			Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
		}
	}

	Runnable network_acquire_oximeter = new Runnable() {
		@Override
		public void run() {
			try {
				name = editText.getText().toString();
				str_acquire = "http://192.168.137.1:8080/acquire_oximeter/?name=" + name + "&begin_year=" + begin_year.getText().toString() + "&begin_month=" + begin_month.getText().toString() + "&begin_day=" + begin_day.getText().toString() + "&end_year=" + end_year.getText().toString() + "&end_month=" + end_month.getText().toString() + "&end_day=" + end_day.getText().toString();
				url_acquire = new URL(str_acquire);
				urlConnection_acquire = (HttpURLConnection)url_acquire.openConnection();
				urlConnection_acquire.setDoOutput(true);
				inputStream_acquire = urlConnection_acquire.getInputStream();
				bufferedReader_acquire = new BufferedReader(new InputStreamReader(inputStream_acquire,"UTF-8"));
				while ((readline_acquire = bufferedReader_acquire.readLine()) != null){
					result_acquire += readline_acquire;
				}
				inputStream_acquire.close();
				bufferedReader_acquire.close();
				urlConnection_acquire.disconnect();


				ArrayList<PointValue> values_bmp = new ArrayList<PointValue>();
				ArrayList<PointValue> values_spo2 = new ArrayList<PointValue>();
				ArrayList<PointValue> values_pi = new ArrayList<PointValue>();

				String[] results = result_acquire.split("#");


				for (String r : results){
					String[] sensors = r.split("@");
					values_bmp.add(new PointValue(Float.valueOf(sensors[0]),Float.valueOf(sensors[1])));
				}
				for (String r : results){
					String[] sensors = r.split("@");
					values_spo2.add(new PointValue(Float.valueOf(sensors[0]),Float.valueOf(sensors[2])));
				}
				for (String r : results){
					String[] sensors = r.split("@");
					values_pi.add(new PointValue(Float.valueOf(sensors[0]),Float.valueOf(sensors[3])));
				}

				Line line_bmp = new Line(values_bmp).setColor(Color.RED);
				Line line_spo2 = new Line(values_spo2).setColor(Color.YELLOW);
				Line line_pi = new Line(values_pi).setColor(Color.BLUE);

				line_bmp.setCubic(false);
				line_spo2.setCubic(false);
				line_pi.setCubic(false);

				ArrayList<Line> lines1 = new ArrayList<Line>();

				if (flag_bmp){
					lines1.clear();
					lines1.add(line_bmp);
				}else if (flag_spo2){
					lines1.clear();
					lines1.add(line_spo2);
				}else if(flag_pi){
					lines1.clear();
					lines1.add(line_pi);
				}

				LineChartData data1 = new LineChartData();
				Axis axisX1 = new Axis();
				Axis axisY1 = new Axis();
				data1.setAxisXBottom(axisX1);
				data1.setAxisYLeft(axisY1);
				data1.setLines(lines1);

				lineChartView1.setInteractive(true);
				lineChartView1.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
				lineChartView1.setLineChartData(data1);

				result_acquire = "";
			}catch (Exception e){
				Log.e("CLM_8",e.toString());
			}
		}
	};





	public void acquire_thermometer(View view){
		new Thread(network_acquire_thermometer).start();
	}

	Runnable network_acquire_thermometer = new Runnable() {
		@Override
		public void run() {
			try {
				name = editText.getText().toString();
				str_acquire = "http://192.168.137.1:8080/acquire_thermometer/?name=" + name + "&begin_year=" + begin_year.getText().toString() + "&begin_month=" + begin_month.getText().toString() + "&begin_day=" + begin_day.getText().toString() + "&end_year=" + end_year.getText().toString() + "&end_month=" + end_month.getText().toString() + "&end_day=" + end_day.getText().toString();
				url_acquire = new URL(str_acquire);
				urlConnection_acquire = (HttpURLConnection)url_acquire.openConnection();
				urlConnection_acquire.setDoOutput(true);
				inputStream_acquire = urlConnection_acquire.getInputStream();
				bufferedReader_acquire = new BufferedReader(new InputStreamReader(inputStream_acquire,"UTF-8"));
				while ((readline_acquire = bufferedReader_acquire.readLine()) != null){
					result_acquire += readline_acquire;
				}
				inputStream_acquire.close();
				bufferedReader_acquire.close();
				urlConnection_acquire.disconnect();


				ArrayList<PointValue> values_temperature = new ArrayList<PointValue>();

				String[] results = result_acquire.split("#");

				for (String r : results){
					String[] sensors = r.split("@");
					values_temperature.add(new PointValue(Float.valueOf(sensors[0]),Float.valueOf(sensors[1])));
				}

				Line line_temperature = new Line(values_temperature).setColor(Color.RED);

				line_temperature.setCubic(false);

				ArrayList<Line> lines1 = new ArrayList<Line>();

				lines1.clear();
				lines1.add(line_temperature);

				LineChartData data1 = new LineChartData();
				Axis axisX1 = new Axis();
				Axis axisY1 = new Axis();
				data1.setAxisXBottom(axisX1);
				data1.setAxisYLeft(axisY1);
				data1.setLines(lines1);

				lineChartView2.setInteractive(true);
				lineChartView2.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
				lineChartView2.setLineChartData(data1);

				result_acquire = "";
			}catch (Exception e){
				Log.e("CLM_8",e.toString());
			}
		}
	};





	@Override
	protected void onDestroy(){
		super.onDestroy();
		BleManager.getInstance().disconnectAllDevice();
		BleManager.getInstance().destroy();
	}
}
