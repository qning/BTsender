/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    
    public static final int bufSize=1024*10;
    // Layout Views
    private TextView mTitle;
    private TextView mData;
    private Button btn_rec;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    //My Surfaceview
    private MySurfaceView sfview;
    // Camera or receiver.
    private boolean Reciver=true;
    //MediaRecorder
    private boolean isRecording=false;
    private boolean Stopped=false;
    private BluetoothChat blc=this;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText("Camera Share by Qian");
        mTitle = (TextView) findViewById(R.id.title_right_text);
        mData=(TextView) findViewById(R.id.textView1);
        sfview=(MySurfaceView) findViewById(R.id.mySurfaceView1);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Create s
        btn_rec=(Button)  findViewById(R.id.button1);
        
		//sfview.destroyDrawingCache(); 
		//bufSize=sfview.getBufSize();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //btn_rec.set
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
        if(sfview.camera_closed())
        {
        	sfview.setCamera();
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        //mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        //mConversationView = (ListView) findViewById(R.id.in);
        //mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        //mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        //mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
       // mSendButton = (Button) findViewById(R.id.button_send);
       // mSendButton.setOnClickListener(new OnClickListener() {
           /* public void onClick(View v) {
                // Send a message using content of the edit text widget
                //TextView view = (TextView) findViewById(R.id.edit_text_out);
                //String message = view.getText().toString();
                //sendMessage(message);
            }
        });*/

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
      //  mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
        sfview.releaseMediaRecorder();
        // release the camera immediately on pause event
        sfview.releaseCamera();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        sfview.releaseMediaRecorder();
        sfview.releaseCamera();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    public void onRecordClick(View view) {
        if (isRecording) {
 
            // stop recording and release camera
        	sfview.stop_recoder(); // stop the recording
            sfview.releaseMediaRecorder(); // release the MediaRecorder object	       // take camera access back from MediaRecorder
 
            // inform the user that recording has stopped
            //setCaptureButtonText("Capture");
            isRecording = false;
            sfview.resumePreivew();
            Stopped=true;
 
        } else {
 
        	Stopped=false;
            new MediaPrepareTask().execute(null, null, null);
 
 
        }
    }
    /**
     * Sends a message.
     * @param message  A string of text to send.
     * @param bytes 
     * @param offsets 
     */
    public void sendMessage(byte[] message, int offsets, int bytes) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        int msg_len=message.length; 
        if (msg_len> 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            //byte[] send = message.getBytes();
            mChatService.write(message,offsets,bytes);
            // Reset out string buffer to zero and clear the edit text field
            //mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    /*private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };*/
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    if(Reciver==false&&sfview.camera_closed()) sfview.setCamera();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                //byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                int bytes=msg.arg1;
               // sfview.showVideo(readBuf);
                mData.setText(bytes+"Bytes!");
                //mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            Reciver=false;
            return true;
        case R.id.insecure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            Reciver=false;
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }
    class Write_thread extends Thread{
		//private String mFileName;
		private ParcelFileDescriptor.AutoCloseInputStream mInstream;
		private FileOutputStream fos;
		private FileOutputStream fos1;
		private boolean first;
		private final int offsets=44;
		public Write_thread(ParcelFileDescriptor rd_fd) {
			first=true;
			mInstream=new ParcelFileDescriptor.AutoCloseInputStream(rd_fd);	
			File  myfile = new File(Environment.getExternalStorageDirectory()
		            .getAbsolutePath(),"nohead.h264");
			File  myfile1= new File(Environment.getExternalStorageDirectory()
		            .getAbsolutePath(),"withhead.h264");
			try {
				fos=new FileOutputStream(myfile);
				fos1=new FileOutputStream(myfile1);
			} catch (FileNotFoundException e) {  
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		public void run()
		{
			byte[] buffer=new byte[1024*64];
			int bytes;
			int count=-offsets;
			int temp=0;
			int start=0;
			int frame_rem=0;
			int bytes_rem=0;
		/*	byte[] h264sps={0x67,0x42,0x00,0x0c,(byte) 0x96,0x54,0x0a,0x0f,(byte) 0x88};  
            byte[] h264pps={0x68,(byte) 0xce,0x38,(byte) 0x80};  
            byte[] h264head={0,0,0,1};
            try {
            	 fos.write(h264head);  
                 fos.write(h264sps);  
                 fos.write(h264head);  
                 fos.write(h264pps);  
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			try {				
					while(true)
					{
						bytes = mInstream.read(buffer,temp,buffer.length-temp);
						 if(bytes>0) 
		                 {
							 temp+=bytes;
							 count+=bytes;
							 if(temp>1024*5)
							 //if(temp>1024*96||(first&&temp>1024*10))
							 {
								 	fos1.write(buffer, 0, temp);
								 	if(first) start=offsets;
								 	else start=0;
			                    	if(frame_rem>0)
			                    	{
			                    		start+=frame_rem;
			                    		//mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, start, frame_rem).sendToTarget();
			                    		if(start>=temp)
			                    		{
			                    			frame_rem=frame_rem-temp;
			                    		}
			                    	}
			                    	while(start<temp)
			                    	{
			                    		frame_rem=readInt(buffer,start,temp);                   		
			                    		if(frame_rem>0)
			                    		{
			                    			bytes_rem=0;
			                    			buffer[start++]=0;
			                        		buffer[start++]=0;
			                        		buffer[start++]=0;
			                        		buffer[start++]=1;                     			
			                    			if(start+frame_rem<temp)
			                    			{
			                    				start=start+frame_rem;                    				
			                    			}
			                    			else
			                    			{
			                    				frame_rem=frame_rem-(temp-start);
			                    				break;
			                    			}
			                    		}
			                    		else 
			                    		{
			                    			bytes_rem=-frame_rem;
			                    			frame_rem=0;
			                    			break;
			                    		}
			                    	}
								 mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, count, temp-bytes_rem).sendToTarget();
								 if(first)
								 {
									 first=false;
									 fos.write(buffer, offsets, temp-offsets-bytes_rem);
									 //blc.sendMessage(buffer,offsets,temp-offsets-bytes_rem);									 
								 }
								 else
								 {
									
									 fos.write(buffer, 0, temp-bytes_rem);
									 //blc.sendMessage(buffer,0,temp-bytes_rem);
								 }
								 for(int k=0;k<bytes_rem;k++)
		        				{
		        						buffer[k]=buffer[temp-bytes_rem+k];
		        				}
								 temp=bytes_rem;
							 }
		                 }
						 
					} 
					
			}catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
		}
		 private int readInt(byte[] buffer,int start,int bytes)
	        {
	        	byte [] temp=new byte[4];
	        	int i;
	        	for(i=0;i<4&&start+i<bytes;i++)
	        	{
	        		temp[i]=buffer[start+i];       		
	        	}
	        	if(i<4) return -(bytes-start);
	        	return java.nio.ByteBuffer.wrap(temp).getInt();
	        }
	}


    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {
    	 
        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (sfview.prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
            	Write_thread wt=new Write_thread(sfview.read_fd);
            	wt.start();
            	sfview.start_recorder();
                isRecording = true;
            } else {
                // prepare didn't work, release the camera
            	sfview.releaseMediaRecorder();
                return false;
            }
            return true;
        }
 
        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
            	BluetoothChat.this.finish();
            }
            // inform the user that recording has started
            //setCaptureButtonText("Stop");
 
        }
    }
}
