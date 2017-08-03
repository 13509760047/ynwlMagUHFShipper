package com.fxb.shipper;
import android.app.Application;
import android.hardware.uhf.magic.DevBeep;
import android.hardware.uhf.magic.reader;

public class App extends Application {
	static String C5U = "/dev/ttyMT1";
	static String C7DU = "/dev/ttyMT2";
	static String CM550 = "/dev/ttyMT2";
	static String CM398M = "/dev/ttyMSM0";

	@Override
	public void onCreate() {
		super.onCreate();
		InitUHF(CM398M);
	}

	/**
	 * Module initialization
	 * Scanning sound initialization
	 * @param type
	 */
	public void InitUHF(String type) {
		reader.init(type);
		reader.Open(type);
		reader.SetTransmissionPower(1950);
		DevBeep.init(App.this);
	}

}
