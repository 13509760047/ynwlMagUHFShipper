//package android.hardware.rfid;
package android.hardware.uhf.magic;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class reader {
	static public Handler m_handler = null;
	static Boolean m_bASYC = false, m_bLoop = false, m_bOK = false;
	static byte[] m_buf = new byte[10240];
	static int m_nCount = 0, m_nReSend = 0, m_nread = 0;
	static int msound = 0;
	static int msound1 = 0;
	// The success prompt tone
	static SoundPool mSoundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
	// The fail prompt tone
	static SoundPool mfailSoundPool = new SoundPool(1,
			AudioManager.STREAM_RING, 0);

	static public String m_strPCEPC = "";
	// Message label
	static public int msgreadepc = 1;
	static public int msgreadwrireepc = 2;
	static public int msgreadwrite = 3;
	static public int readover = 4;// Tag Writer
	static public int editepcsmsg = 5;
	static public int locklable = 6;
	static public int killlable = 7;

	/**
	 * initgraph
	 * 
	 * @param strpath
	 */
	static public native int init(String strpath);

	/**
	 * Open the device
	 * 
	 * @param strpath
	 * 
	 * @return success 0，Otherwise, it returns an error code （-20 Device Type is
	 *         not correct， -1 The device cannot be opened， 1 The device has
	 *         been opened， -2 Device parameter cannot be set）
	 */
	static public native int Open(String strpath);

	/**
	 * 
	 * @param hd
	 */
	static public void reg_handler(Handler handler) {
		m_handler = handler;
	}

	/* *
	 * Reads data from a Device
	 * 
	 * @ param pout
	 * 
	 * @ param nStart to store data in a pout starting position
	 * 
	 * @ param nCount to read the data length
	 * 
	 * @ return
	 */

	static public native int Read(byte[] pout, int nStart, int nCount);

	/**
	 * Close the equipment
	 */
	static public native void Close();

	/**
	 * Clear equipment cache data
	 */
	static public native void Clean();

	/**
	 * 单次读取TID
	 * 
	 * @return 成功返回0x10,错误返回0x11
	 */
	static public native int Readtid(int tid_len);

	/* *
	 * a single polling
	 * 
	 * @ return successfully returns 0 x10, error returns 0 x11
	 */

	static public native int Inventory();

	/* *
	 * many polling
	 * 
	 * @ param ntimes number of polling, polling number of 0-65535 times
	 * 
	 * @ return successfully returns 0 x10, error returns 0 x11
	 */

	static public native int MultiInventory(int ntimes);

	/* *
	 * stop polling for many times
	 * 
	 * @ return successfully returns 0 x10, error returns 0 x11
	 */

	static public native int StopMultiInventory();

	/* *
	 * set the Select parameters, and set in a single polling or polling before
	 * the Inventory for many times, first send the Select command. In the case
	 * of multiple tags, can only to specific tags for polling the Inventory
	 * operation.
	 * 
	 * @ param selPa parameters (Target: 3 b, 000, the Action: 3 b, 000 MemBank:
	 * 2 b 01)
	 * 
	 * @ param nPTR (bit as unit, not a word) from the PC and EPC stored a start
	 * 
	 * @ param nMaskLen the length of the Mask
	 * 
	 * @ param turncate (0 x00 is Disable truncation, 0 x80 is Enable
	 * truncation)
	 * 
	 * @ param mask
	 * 
	 * @ return successfully returns 0 x00, error returns non-zero
	 */

	static public native int Select(byte selPa, int nPTR, byte nMaskLen,
			byte turncate, byte[] mask);

	/* *
	 * set send to send the Select statement
	 * 
	 * @ param data (0 x01 is to cancel the Select command, 0 x00 is send a
	 * Select statement)
	 * 
	 * @ return successfully returns 0 x00, error returns non-zero
	 */

	static public native int SetSelect(byte data);

	/**
	 * read the label data store
	 * 
	 * @ param password read password, 4 bytes @ param nUL PC + EPC length @
	 * param PCandEPC PC + EPC data @ param membank label data store @ param nSA
	 * read the label data area address offset @ param nDL read the label length
	 * data area address @ return
	 */

	static public native int ReadLable(byte[] password, int nUL,
			byte[] PCandEPC, byte membank, int nSA, int nDL);

	/* *
	 * write tag data store
	 * 
	 * @ param password password 4 bytes
	 * 
	 * @ param nUL PC + EPC length
	 * 
	 * @ param PCandEPC PC + EPC data
	 * 
	 * @ param membank label data store
	 * 
	 * @ param nSA write tag data area address offset
	 * 
	 * @ param nDL write tag data area data length
	 * 
	 * @ param data write data
	 * 
	 * @ return
	 */

	static public native int WriteLable(byte[] password, int nUL,
			byte[] PCandEPC, byte membank, int nSA, int nDL, byte[] data);

	/* *
	 * for a single tag, Lock the Lock or Unlock the Unlock the tag data store
	 * 
	 * @ param password lock password
	 * 
	 * @ param nUL PC + EPC length
	 * 
	 * @ param PCandEPC PC + EPC data
	 * 
	 * @ param nLD lock or unlock command
	 * 
	 * @ return
	 */

	static public native int Lock(byte[] password, int nUL, byte[] PCandEPC,
			int nLD);

	/* *
	 * inactivated Kill tags
	 * 
	 * @ param password password
	 * 
	 * @ param nUL PC + EPC length
	 * 
	 * @ param EPC PC + EPC content
	 * 
	 * @ return
	 */

	static public native int Kill(byte[] password, int nUL, byte[] EPC);

	/* *
	 * inactivated label (the results sent by Handle asynchronous)
	 * 
	 * @ param btReadId read/write device address
	 * 
	 * @ param pbtAryPassWord destroy password (4 bytes)
	 * 
	 * @ return
	 */

	static public int KillLables(byte[] password, int nUL, byte[] EPC) {
		Clean();
		int nret = Kill(password, nUL, EPC);
		if (!m_bASYC) {
			StartASYCKilllables();
		}
		return nret;
	}

	static public native int Query();

	/* *
	 * set the related parameters of a Query command
	 * 
	 * @ param nParam parameters for 2 bytes, have the following specific
	 * parameters according to a patchwork: DR (1 -) : DR = 8 (1 b0), DR = 64/3
	 * b1 (1). only support DR = 8 model M (2 -) : M = 1 b00 (2), M = 2 (2 b01),
	 * M = 4 (b10) 2, Bl1 * M = 8 (2). Only support M = 1 mode TRext (1 -) : No
	 * pilot tone (1 b0), Use pilot tone b1 (1). Support only Use pilot tone b1
	 * (1) mode Sel (2 -) : ALL (2 b00/2 b01), ~ SL (2 b10), SL (2 b11) Session
	 * (2 -) : S0 b00 (2), B01 * S1 (2), S2 (b10) 2, S3 bl1 (2) the Target (1 -)
	 * : A b0 (1), B (1 b1) Q (4) bit) : 4 b1111 b0000-4
	 * 
	 * @ return
	 */

	static public native int SetQuery(int nParam);

	static public native int SetFrequency(byte region);

	/* *
	 * set the working channel
	 * 
	 * @ param channel China 900 MHZ channel parameter calculation formula,
	 * Freq_CH for channel frequency: CH_Index = (Freq_CH to 920.125 M) / 0.25 M
	 * 
	 * China 800 MHZ channel parameter calculation formula, Freq_CH for channel
	 * frequency: CH_Index = (Freq_CH to 840.125 M) / 0.25 M
	 * 
	 * channel parameter calculation formula in the United States, Freq_CH for
	 * channel frequency: CH_Index = (Freq_CH to 902.25 M / 902.25 M
	 * 
	 * channel parameter calculation formula of Europe, Freq_CH for channel
	 * frequency: CH_Index = (Freq_CH to 865.1 M / 865.1 M
	 * 
	 * Korean channel parameter calculation formula, Freq_CH for channel
	 * frequency: CH_Index = (Freq_CH to 917.1 M / 917.1 M
	 * 
	 * @ return
	 */

	static public native int SetChannel(byte channel);

	/* *
	 * for working channel
	 * 
	 * @ return China 900 MHZ channel parameter calculation formula, Freq_CH for
	 * channel frequency: Freq_CH = CH_Index * 0.25 M + 920.125 M
	 * 
	 * China 800 MHZ channel parameter calculation formula, Freq_CH for channel
	 * frequency: Freq_CH = CH_Index * 0.25 M + 840.125 M
	 * 
	 * channel parameter calculation formula in the United States, Freq_CH for
	 * channel frequency: Freq_CH = CH_Index * 0.5 M + 902.25 M
	 * 
	 * channel parameter calculation formula of Europe, Freq_CH for channel
	 * frequency: Freq_CH = CH_Index * 0.2 M + 865.1 M
	 */

	static public native int GetChannel();

	/* *
	 * set to automatic frequency hopping pattern or cancel the automatic
	 * frequency hopping pattern
	 * 
	 * @ param auto 0 XFF to set up automatic frequency hopping, 0 x00 to cancel
	 * the automatic frequency hopping
	 * 
	 * @ return
	 */

	static public native int SetAutoFrequencyHopping(byte auto);

	/* *
	 * get transmitted power
	 * 
	 * @ return back to the actual transmission power
	 */

	static public native int GetTransmissionPower();

	/* *
	 * set the transmission power
	 * 
	 * @ param nPower transmitted power
	 * 
	 * @ return
	 */

	static public native int SetTransmissionPower(int nPower);

	/* *
	 * set the launch continuous carrier or closed continuous carrier
	 * 
	 * @ param bOn 0 XFF to open the continuous wave, 0 x00 to close the
	 * continuous wave
	 * 
	 * @ return
	 */

	static public native int SetContinuousCarrier(byte bOn);

	/* *
	 * get the current read/write device receives the modem parameters
	 * 
	 * @ param bufout two bytes, the first Mixer gain, the second intermediate
	 * frequency amplifier gain Mixer Mixer gain Type Mixer_G 0 0 x00 (dB) 0 x01
	 * 3 0 x02 6 0 x03 9 15 0 0 0 x05 x04 12 x06 IF AMP gain table 16
	 * intermediate frequency amplifier Type IF_G (dB) 0 x00 12 0 x01 18 0 x02
	 * 21 24 x04 27 0 0 0 x03 x05 30 0 0 x07 x06 36, 40
	 * 
	 * @ return
	 */

	static public native int GetParameter(byte[] bufout);

	/* *
	 * set the current read/write device receives the modem parameters
	 * 
	 * @ param bMixer mixer gain
	 * 
	 * @ param bIF if amplifier gain
	 * 
	 * @ param nThrd signal regulator value, can the smaller the demodulation of
	 * the signal demodulation threshold label return RSSI is lower, but the
	 * more volatile, lower than a year constant value completely unable to
	 * demodulation; Instead the greater the threshold to be back on the RSSI
	 * signal demodulation of the tag, the greater the distance between the
	 * closer, the more stable. The minimum value 0 x01b0 is recommended
	 * 
	 * @ return
	 */

	static public native int SetParameter(byte bMixer, byte bIF, int nThrd);

	/* *
	 * test rf signal input terminal block
	 * 
	 * @ param bufout
	 * 
	 * @ return
	 */

	static public native int ScanJammer(byte[] bufout);

	/* *
	 * test rf input RSSI signal size, used to detect the current environment
	 * for reading and writing at work
	 * 
	 * @ param bufout
	 * 
	 * @ return
	 */

	static public native int TestRssi(byte[] bufout);

	/* *
	 * set the direction of the IO port, read IO level and the IO level
	 * 
	 * @ param p1
	 * 
	 * @ param p2
	 * 
	 * @ param p3 number description length specification parameter 0 0 1 byte
	 * operation type choice: 0 x00: set IO direction; 0 x01: set IO level; 0
	 * x02: read IO level. To operate the pin in the parameters specified in the
	 * 1 1 1 byte for the parameter value range 0 x01 ~ 0 x04, corresponding to
	 * the operation of port IO1 ~ IO4 2 parameters 2 1 byte value is 0 x00 or 0
	 * x01. Description * Parameter0 Parameter2 0 x00 0 x00 IO configured for
	 * input mode 0 x00 0 x01 IO the configuration for the output mode 0 x01 0
	 * x00 set IO output for low level 0 x01 0 x01 set the I/o output to high
	 * level When the parameter 0 for 0 x02, this parameter is meaningless.
	 * 
	 * @ param bufout
	 * 
	 * @ return
	 */

	static public native int SetIOParameter(byte p1, byte p2, byte p3,
			byte[] bufout);

	/* *
	 * a single polling
	 * 
	 * @ return successfully returns 0 x10, error returns 0 x11
	 */

	static public int ReadtidLables(int tidlen) {
		int nret = Readtid(tidlen);
		if (!m_bASYC) {
			StartRead_TID(tidlen);
		}
		return nret;
	}

	static public int ReadtidLablesLoop(int tidlen) {
		int nret = Readtid(tidlen);
		m_bLoop = true;
		if (!m_bASYC) {
			StartRead_TID(tidlen);
		}
		return nret;
	}

	static public int InventoryLables() {
		int nret = Inventory();
		if (!m_bASYC) {
			StartASYClables();
		}
		return nret;
	}

	static public int InventoryLablesLoop() {
		int nret = Inventory();
		m_bLoop = true;
		if (!m_bASYC) {
			StartASYClables();
		}
		return nret;
	}

	static public void StopLoop() {
		m_bLoop = false;
	}

	static public int MultInventoryLables() {
		int nret = MultiInventory(65535);
		if (!m_bASYC) {
			StartASYClables();
		}
		return nret;
	}

	static public int ReadLablesepc(byte[] password, int nUL, byte[] PCandEPC,
			byte membank, int nSA, int nDL, String newepc) {
		int nret = 0;
		if (!m_bASYC) {
			Clean();
			nret = ReadLable(password, nUL, PCandEPC, membank, nSA, nDL);
			m_bOK = false;
			m_nReSend = 0;
			StartASYCReadlablesEPC(newepc);
		}
		return nret;
	}

	/* *
	 * read labels (sent via Handle asynchronous as a result, a card a message)
	 * 
	 * @ param password read password, 4 bytes
	 * 
	 * @ param nUL PC + EPC length
	 * 
	 * @ param PCandEPC PC + EPC data
	 * 
	 * @ param membank label data store
	 * 
	 * @ param nSA read the label data area address offset
	 * 
	 * @ param nDL read the label length data area address
	 * 
	 * @ return
	 */

	static public int ReadLables(byte[] password, int nUL, byte[] PCandEPC,
			byte membank, int nSA, int nDL) {
		int nret = 0;
		if (!m_bASYC) {
			Clean();
			nret = ReadLable(password, nUL, PCandEPC, membank, nSA, nDL);
			m_bOK = false;
			m_nReSend = 0;
			StartASYCReadlables();
		}
		return nret;
	}

	/* *
	 * for a single tag, Lock the Lock or Unlock the Unlock the tag data store
	 * 
	 * @ param password lock password
	 * 
	 * @ param nUL PC + EPC length
	 * 
	 * @ param PCandEPC PC + EPC data
	 * 
	 * @ param nLD lock or unlock command
	 * 
	 * @ return
	 */

	static public int LockLables(byte[] password, int nUL, byte[] PCandEPC,
			int nLD) {
		Clean();
		int nret = Lock(password, nUL, PCandEPC, nLD);
		if (!m_bASYC) {
			StartASYCLocklables();
		}
		return nret;
	}

	/* *
	 * write label (results through asynchronous send Handle, a card a message)
	 * 
	 * @ param password password 4 bytes
	 * 
	 * @ param nUL PC + EPC length
	 * 
	 * @ param PCandEPC PC + EPC data
	 * 
	 * @ param membank label data store
	 * 
	 * @ param nSA write tag data area address offset
	 * 
	 * @ param nDL write tag data area data length
	 * 
	 * @ param data write data
	 * 
	 * @ return
	 */

	static public int Writelables(byte[] password, int nUL, byte[] PCandEPC,
			byte membank, int nSA, int nDL, byte[] data) {
		Clean();
		int nret = WriteLable(password, nUL, PCandEPC, membank, nSA, nDL, data);
		if (!m_bASYC) {
			m_bOK = false;
			m_nReSend = 0;
			StartASYCWritelables();
		}
		return nret;
	}

	static public int GetLockPayLoad(byte membank, byte Mask) {
		int nret = 0;
		switch (Mask) {
		case 0:
			switch (membank) {
			case 0:
				nret = 0x80000;
				break;
			case 1:
				nret = 0x80200;
				break;
			case 2:
				nret = 0xc0100;
				break;
			case 3:
				nret = 0xc0300;
				break;
			}
			break;
		case 1:
			switch (membank) {
			case 0:
				nret = 0x20000;
				break;
			case 1:
				nret = 0x20080;
				break;
			case 2:
				nret = 0x30040;
				break;
			case 3:
				nret = 0x300c0;
				break;
			}
			break;
		case 2:
			switch (membank) {
			case 0:
				nret = 0x8000;
				break;
			case 1:
				nret = 0x8020;
				break;
			case 2:
				nret = 0xc010;
				break;
			case 3:
				nret = 0xc030;
				break;
			}
			break;
		case 3:
			switch (membank) {
			case 0:
				nret = 0x2000;
				break;
			case 1:
				nret = 0x2008;
				break;
			case 2:
				nret = 0x3004;
				break;
			case 3:
				nret = 0x300c;
				break;
			}
			break;
		case 4:
			switch (membank) {
			case 0:
				nret = 0x0800;
				break;
			case 1:
				nret = 0x0802;
				break;
			case 2:
				nret = 0x0c01;
				break;
			case 3:
				nret = 0x0c03;
				break;
			}
			break;
		}
		return nret;
	}

	static void StartASYCKilllables() {
		m_bASYC = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int nTemp = 0;
				m_nCount = 0;
				m_nread = 0;
				while (m_handler != null) {
					nTemp = Read(m_buf, m_nCount, 1024);
					m_nCount += nTemp;
					if (nTemp == 0) {
						m_nread++;
						if (m_nread > 5)
							break;

					}
					// Log.e("test",""+m_nCount+"="+m_nread);
					String str = reader.BytesToString(m_buf, 0, m_nCount);
					// Log.e("test",str);
					String[] substr = Pattern.compile("BB0165").split(str);
					// Log.e("test","sub="+substr.length);
					for (int i = 0; i < substr.length; i++) {
						if (substr[i].length() >= 10) {
							if (substr[i].substring(0, 10).equals("000100677E")) {
								Message msg = new Message();
								msg.what = killlable;
								msg.obj = "OK";
								m_handler.sendMessage(msg);
							} else {
								Message msg = new Message();
								msg.what = killlable;
								//BB  01  65  00  01  00  67  7E 
								//msg.obj = substr[i];
								String   Error=substr[i];
								if(Error.length()>=16)
								{
									String sigin=Error.substring(10, 12);
									if(sigin.equals("16"))
									{
										msg.obj="Access Password不正确";
									}
									if(sigin.equals("12"))
									{
										msg.obj="This tag does not have the EPC code specified in the site area or not ";
									}
									if(sigin.equals("D0"))
									{
										msg.obj="No Kill Password ";
									}
								}
								m_handler.sendMessage(msg);
							}
						}

					}
				}
				m_bASYC = false;
			}
		});
		thread.start();
	}

	static void StartASYCLocklables() {
		m_bASYC = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int nTemp = 0;
				m_nCount = 0;
				m_nread = 0;
				while (m_handler != null) {
					nTemp = Read(m_buf, m_nCount, 1024);
					m_nCount += nTemp;
					if (nTemp == 0) {
						m_nread++;
						if (m_nread > 5)
							break;
					}
					String str = reader.BytesToString(m_buf, 0, m_nCount);
					String[] substr = Pattern.compile("BB0182").split(str);
					for (int i = 0; i < substr.length; i++) {
						if (substr[i].length() >= 10) {
							if (substr[i].substring(0, 10).equals("000100847E")) {
								Message msg = new Message();
								msg.what = locklable;
								msg.obj = "OK";
								m_handler.sendMessage(msg);
							} else {
								Message msg = new Message();
								msg.what = locklable;

								String Error = substr[i];
								if (Error.length() >= 16) {
									String sigin = Error.substring(10, 12);
									if (sigin.equals("16")) {
										msg.obj = "The Access Password is not correct ";
									}
									if (sigin.equals("13")) {
										msg.obj = "This tag does not have the EPC code specified in the site area or not ";
									}
								}

								m_handler.sendMessage(msg);
							}
						}

					}
				}
				m_bASYC = false;
			}
		});
		thread.start();
	}

	static void StartASYCWritelables() {
		m_bASYC = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int nTemp = 0;
				m_nCount = 0;
				m_nread = 0;
				while (m_handler != null) {
					nTemp = Read(m_buf, m_nCount, 1024);
					m_nCount += nTemp;
					if (nTemp == 0) {
						m_nread++;
						if (m_nread > 10)
							break;

						String str = reader.BytesToString(m_buf, 0, m_nCount);
						Log.e("testtesttest", str);// BB0149
						if (str.startsWith("BB0149")) {
							int pcepclen = Integer.valueOf(
									str.substring(6, 10), 16);

							String sigin = str.substring(10, str.length() - 4);
							if ("00".equals(sigin)) {
								m_bOK = true;
								Message msg = new Message();
								msg.what = msgreadwrite;
								DevBeep.PlayOK();
								msg.obj = "OK";
								m_handler.sendMessage(msg);
								break;
							}
						} else if (str.startsWith("BB01FF")) {
							if (str.length() >= 12) {
								int nlen = Integer.valueOf(
										str.substring(6, 10), 16);
								if (str.substring(10, 12).equals("00")
										|| str.substring(10, 12).equals("B3")
										|| str.substring(10, 12).equals("B4")) {
									m_bOK = true;
									Message msg = new Message();
									msg.what = msgreadwrite;
									msg.obj = "OK";
									DevBeep.PlayOK();
									m_handler.sendMessage(msg);
									break;
								} else if (str.substring(10, 12).equals("10")) {
									// BB 01 FF 00 01 B3 B4 7E
									m_bOK = true;
									Message msg = new Message();
									msg.what = msgreadwrite;
									// msg.obj = "Error:" +
									// "The tag does not have the presence area or the specified EPC code is incorrect. ";
									msg.obj = "Error:This tag does not have the EPC code specified in the site area or not";
									DevBeep.PlayErr();
									m_handler.sendMessage(msg);
									break;
								} else if (str.substring(10, 12).equals("16")) {
									m_bOK = true;
									Message msg = new Message();
									msg.what = msgreadwrite;
									// msg.obj = "Error:"
									// +"Access Password Error";
									DevBeep.PlayErr();
									msg.obj = "Error:Access Password wrong";
									m_handler.sendMessage(msg);
									break;
								} else if (str.substring(10, 12).equals("BF")) {
									m_bOK = true;
									Message msg = new Message();
									msg.what = msgreadwrite;
									msg.obj = "Label does not support Error-code return";
									DevBeep.PlayErr();
									// msg.obj = "Error:"
									// +"Label does not support Error-code return";
									m_handler.sendMessage(msg);
									break;
								} else if (str.substring(10, 12).equals("B0")) {
									m_bOK = true;
									DevBeep.PlayErr();
									Message msg = new Message();
									msg.what = msgreadwrite;
									msg.obj = "Error:" + "An unknown error ";
									m_handler.sendMessage(msg);
									break;
								}

							}

						}

					}

					m_bASYC = false;
				}
			}
		});
		thread.start();
	}

	static void StartASYCReadlables() {
		m_bASYC = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				boolean tag_find = false;
				int nTemp = 0;
				m_nCount = 0;
				m_nread = 0;
				while (m_handler != null) {
					nTemp = Read(m_buf, m_nCount, 1024);
					m_nCount += nTemp;
					if (nTemp == 0) {
						m_nread++;
						if (m_nread > 5)
							break;

						String str = reader.BytesToString(m_buf, 0, m_nCount);
						Log.e("1111111", str);

						if (str.startsWith("BB0139")) {
							int pcepclen = Integer.valueOf(
									str.substring(6, 10), 16);
							String readdata = str.substring(10,
									str.length() - 4);
							m_bOK = true;
							Message msg = new Message();
							msg.what = reader.msgreadwrireepc;
							msg.obj = readdata;

							m_handler.sendMessage(msg);
							DevBeep.PlayOK();
							m_bASYC = false;
							tag_find = true;
							break;

						} else if (str.startsWith("BB01FF")) {
							Message msg = new Message();
							if (str.substring(10, 12).equals("09")) {
								msg.obj = "Error:The tag does not have the presence area or the specified EPC code is incorrect";
								
							} else if (str.substring(10, 12).equals("16")) {
								msg.obj = "Error:Access Password Error";
								
							} else if (str.substring(10, 12).equals("A4")) {
								// msg.obj =
								
								msg.obj = "Error:The specified tag data storage area is locked and / or permanently Lock, and lock status is not to be written or not readable. ";
							} else if (str.substring(10, 12).equals("A3")) {
								msg.obj = "Error:The specified tag data storage area does not exist; or the label does not Supports the specified length of EPC, ";
								// msg.obj =
								
							} else if (str.substring(10, 12).equals("A0")) {
								msg.obj = "unknown error ";
								
							}
							msg.what = reader.msgreadwrireepc;
							m_handler.sendMessage(msg);
							DevBeep.PlayErr();
							m_bASYC = false;
							tag_find = true;
						}

					}
					if (tag_find) {
						break;
					} else {

					}

				}

				m_bASYC = false;
			}
		});
		thread.start();
	}

	static void StartASYCReadlablesEPC(final String newepc) {
		m_bASYC = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				boolean tag_find = false;
				int nTemp = 0;
				m_nCount = 0;
				m_nread = 0;
				while (m_handler != null) {

					nTemp = Read(m_buf, m_nCount, 1024);
					m_nCount += nTemp;
					if (nTemp == 0) {
						m_nread++;
						if (m_nread > 5)
							break;
					}
					String str = reader.BytesToString(m_buf, 0, m_nCount);
					Log.e("1111111", str);
					String[] substr = Pattern.compile("BB0139").split(str);
					String tempstr = "";
					for (int i = 0; i < substr.length; i++) {
						if (substr[i].length() > 10) {
							// if (!substr[i].substring(0,2).equals("BB"))
							Log.e("wwwwwww", substr[i].substring(4,
									substr[i].length() - 4));
							Log.e("fffffff", newepc);
							if (substr[i].substring(4, substr[i].length() - 4)
									.equals(newepc)) {
								m_bOK = true;
								Message msg = new Message();
								msg.what = reader.msgreadwrireepc;
								msg.obj = substr[i].substring(4,
										substr[i].length() - 4);
								m_handler.sendMessage(msg);
								DevBeep.PlayOK();
								m_bASYC = false;
								tag_find = true;
								break;
							} else {
								Message msg = new Message();
								msg.what = reader.msgreadwrireepc;
								msg.obj = "Read failure ";
								m_handler.sendMessage(msg);
								
								DevBeep.PlayErr();
								m_bASYC = false;
								tag_find = true;

							}
						}
					}
					// }
					if (tag_find) {
						break;
					} else {

					}

				}

				m_bASYC = false;
			}
		});
		thread.start();
	}

	static void StartRead_TID(final int tidlen) {
		m_bASYC = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int nTemp = 0, nIndex = 0;
				boolean tag_find = false;
				m_nCount = 0;
				m_nReSend = 0;
				nIndex = 0;
				while (m_handler != null) {
					// nIndex = m_nCount;
					nTemp = Read(m_buf, m_nCount, 10240 - m_nCount);
					m_nCount += nTemp;
					// Log.e("777777777777777777", "count=" + m_nCount);
					if (nTemp == 0) {
						// String str = reader.BytesToString(m_buf, nIndex,
						// m_nCount - nIndex);
						String str = reader.BytesToString(m_buf, 0, m_nCount);
						String[] substr = Pattern.compile("BB023A").split(str);
						// Log.e("9999999", "len=" + substr.length);
						for (int i = 0; i < substr.length; i++) {
							Log.e("777777", substr[i]);
							if (substr[i].length() > 16) {
								if (!substr[i].substring(0, 2).equals("BB")) {
									int nlen = Integer.valueOf(
											substr[i].substring(0, 4), 16);
									int epclen = Integer.valueOf(
											substr[i].substring(4, 6), 16);
									Log.e("EPClen", String.valueOf(epclen));
									if ((nlen > 3)
											&& (nlen < (substr[i].length() - 6) / 2)) {
										Message msg = new Message();
										msg.what = reader.msgreadepc;
										msg.obj = substr[i].substring(4,
												(nlen * 2 + 2));

										Log.e("epc24", substr[i]);

										m_handler.sendMessage(msg);
										tag_find = true;
										m_bOK = true;
										DevBeep.PlayOK();
										break;
									}
								}
							}
						}
						if (tag_find && !m_bLoop) {
							break;
						}
						if (m_bLoop) {
							m_nCount = 0;
							ReadtidLablesLoop(tidlen);
							tag_find = false;
						} else {
							if ((m_nReSend < 20) && (!tag_find)) {
								Readtid(12);
								m_nReSend++;
							} else {
								Log.e("CLOSE", "close device");

								tag_find = false;
								break;

							}
						}

						if (m_nCount >= 1024)
							m_nCount = 0;
					}
				}
				// Log.e("end", "quit");
				m_bASYC = false;
			}
		});
		thread.start();
	}

	static void StartASYClables() {
		m_bASYC = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int nTemp = 0, nIndex = 0;
				boolean tag_find = false;
				m_nCount = 0;
				m_nReSend = 0;
				nIndex = 0;
				while (m_handler != null) {
					// nIndex = m_nCount;
					nTemp = Read(m_buf, m_nCount, 10240 - m_nCount);
					m_nCount += nTemp;
					if (nTemp == 0)

					{
						String str = reader.BytesToString(m_buf, nIndex,
								m_nCount - nIndex);
						Log.e("777777", str);
						String[] substr = Pattern.compile("BB0222").split(str);
						// Log.e("9999999", "len=" + substr.length);
						for (int i = 0; i < substr.length; i++) {
							Log.e("777777", substr[i]);
							if (substr[i].length() > 16) {
								if (!substr[i].substring(0, 2).equals("BB")) {
									Log.e("9999999888888", "len=" + substr[i]);
									int nlen = Integer.valueOf(
											substr[i].substring(0, 4), 16);
									if ((nlen > 3)
											&& (nlen < (substr[i].length() - 6) / 2)) {
										Message msg = new Message();
										msg.what = reader.msgreadepc;
										msg.obj = substr[i].substring(6,
												(nlen * 2));
										m_handler.sendMessage(msg);
										tag_find = true;
										m_bOK = true;
										DevBeep.PlayOK();
									}
								}
							}
						}
						if (m_bLoop) {
							m_nCount = 0;
							InventoryLablesLoop();
						} else {

							if ((m_nReSend < 20) && (!tag_find)) {
								Inventory();
								m_nReSend++;
							} else {
								m_nCount = 0;
								Message msg = new Message();
								msg.what = reader.readover;
								msg.obj = "";
								m_handler.sendMessage(msg);
								Log.e("dqw---test", "break");
								break;
								
							}
							//
						}
						if (m_nCount >= 1024)
							m_nCount = 0;
					}
				}
				// Log.e("end", "quit");
				m_bASYC = false;
			}
		});
		thread.start();
	}

	public static Runnable ReadLables_Run = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int nTemp = 0, nIndex = 0;
			boolean tag_find = false;
			m_nCount = 0;
			m_nReSend = 0;
			nIndex = 0;
			while (m_handler != null) {
				// nIndex = m_nCount;
				nTemp = Read(m_buf, m_nCount, 10240 - m_nCount);
				m_nCount += nTemp;
				if (nTemp == 0) {
					String str = reader.BytesToString(m_buf, nIndex, m_nCount
							- nIndex);
					String[] substr = Pattern.compile("BB0222").split(str);
					String datas = "";
					for (int i = 0; i < substr.length; i++) {
						Log.e("777777", substr[i]);
						if (substr[i].length() > 16) {
							if (!substr[i].substring(0, 2).equals("BB")) {
								Log.e("9999999888888", "len=" + substr[i]);
								int nlen = Integer.valueOf(
										substr[i].substring(0, 4), 16);
								if ((nlen > 3)
										&& (nlen < (substr[i].length() - 6) / 2)) {
									Message msg = new Message();
									msg.what = reader.msgreadepc;
									datas = substr[i].substring(6, (nlen * 2));
									msg.obj = datas;
									m_handler.sendMessage(msg);
									tag_find = true;
									m_bOK = true;
									DevBeep.PlayOK();
									break;
								}
							}
						}

						if (substr[i].contains("BB01FF000115167E")) {
							Log.e("9999999888888", "len=" + substr[i]);
							Message msg = new Message();
							msg.what = reader.readover;
							msg.obj = "";
							m_handler.sendMessage(msg);
							Log.e("dqw---test", "break");
							break;
						}
					}

					if (m_nCount >= 1024)
						m_nCount = 0;

					break;
				}
			}
		}
	};

	static {
		System.loadLibrary("uhf-tools");
		msound1 = mfailSoundPool.load(
				"/system/media/audio/notifications/Argon.ogg", 1);
		msound = mSoundPool.load(
				"/system/media/audio/notifications/Heaven.ogg", 1);
	}

	public static byte[] stringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String BytesToString(byte[] b, int nS, int ncount) {
		String ret = "";
		int nMax = ncount > (b.length - nS) ? b.length - nS : ncount;
		for (int i = 0; i < nMax; i++) {
			String hex = Integer.toHexString(b[i + nS] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	public static int byteToInt(byte[] b) // byteToInt
	{
		int t2 = 0, temp = 0;
		for (int i = 3; i >= 0; i--) {
			t2 = t2 << 8;
			temp = b[i];
			if (temp < 0) {
				temp += 256;
			}
			t2 = t2 + temp;

		}
		return t2;

	}

	public static int byteToInt(byte[] b, int nIndex, int ncount) // byteToInt
	{
		int t2 = 0, temp = 0;
		for (int i = 0; i < ncount; i++) {
			t2 = t2 << 8;
			temp = b[i + nIndex];
			if (temp < 0) {
				temp += 256;
			}
			t2 = t2 + temp;

		}
		return t2;

	}

	/**** int to byte ******/
	public static byte[] intToByte(int content, int offset) {

		byte result[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		for (int j = offset; j < result.length; j += 4) {
			result[j + 3] = (byte) (content & 0xff);
			result[j + 2] = (byte) ((content >> 8) & 0xff);
			result[j + 1] = (byte) ((content >> 16) & 0xff);
			result[j] = (byte) ((content >> 24) & 0xff);
		}
		return result;
	}

	public static String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();
		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {

			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);
			temp.append(decimal);
		}
		return sb.toString();
	}

	//
	public static String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}
	public static String toStringHex(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	
	/*
	 * 
	 */
	private static String hexString = "0123456789ABCDEF";

	public static String encode(String str) {

		byte[] bytes = str.getBytes();
		StringBuilder sb = new StringBuilder(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++) {
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
		}
		return sb.toString();
	}

	public static String decode(String bytes) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				bytes.length() / 2);

		for (int i = 0; i < bytes.length(); i += 2)
			baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
					.indexOf(bytes.charAt(i + 1))));
		return new String(baos.toByteArray());
	}

	/**
	 * 
	 * 
	 * @param strwepc
	 * 
	 * @author Administrator
	 * **/
	static void StartASYClablesbyoldepc(final String strwepc) {
		m_bASYC = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int nTemp = 0, nIndex = 0;
				boolean tag_find = false;
				m_nCount = 0;
				m_nReSend = 0;
				nIndex = 0;
				while (m_handler != null) {
					// nIndex = m_nCount;
					nTemp = Read(m_buf, m_nCount, 10240 - m_nCount);
					m_nCount += nTemp;
					if (tag_find)
						break;
					if (nTemp == 0) {
						String str = reader.BytesToString(m_buf, nIndex,
								m_nCount - nIndex);
						String[] substr = Pattern.compile("BB0222").split(str);
						for (int i = 0; i < substr.length; i++) {
							if (substr[i].length() > 16) {
								if (!substr[i].substring(0, 2).equals("BB")) {
									int nlen = Integer.valueOf(
											substr[i].substring(0, 4), 16);
									if ((nlen > 3)
											&& (nlen < (substr[i].length() - 6) / 2)) {
										Message msg = new Message();
										msg.what = editepcsmsg;
										String newepc = substr[i].substring(10,
												10 + strwepc.length())
												.toString();
										Log.e("newepc:", newepc);
										Log.e("wepc:", strwepc);
										if (newepc.equals(strwepc)) {
											// if
											// (substr[i].substring(10,strwepc.length()).toString().equals(strwepc))
											// {
											msg.obj = "ok";
											m_handler.sendMessage(msg);
											tag_find = true;
											m_bOK = true;
											break;
										}
									}
								}
							}
						}//
						if (tag_find) {
							mSoundPool.play(msound, 1.0f, 1.0f, 0, 0, 1.0f);
							break;
						}
						if ((m_nReSend < 20) && (!tag_find)) {
							Inventory();
							m_nReSend++;
							m_bASYC = false;
						} else if (!tag_find && m_nReSend >= 20) {
							Message msg1 = new Message();
							msg1.what = editepcsmsg;//
							msg1.obj = "error";
							m_handler.sendMessage(msg1);
							tag_find = false;
							break;
						}
						if (m_nCount >= 1024)
							m_nCount = 0;
					}
				}
				m_bASYC = false;
				// Log.e("end", "quit");
			}
		});
		thread.start();
	}

}
