package com.example.android.BluetoothChat;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PipedReader;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.Log;
import android.view.SurfaceHolder.Callback;

	public class MySurfaceView extends SurfaceView implements Callback {

		private static final String TAG = "MySurfaceView";


		private MediaRecorder recorder;
		private SurfaceHolder mHolder;

		private Camera mCamera;

		private boolean isPreviewRunning = false;
		private ParcelFileDescriptor buf_fd;		
		public ParcelFileDescriptor read_fd;
		
		//private ParcelFileDescriptor[] pipe;
		//private int mMultiplyColor;
		
		public MySurfaceView(Context context, AttributeSet attrs) {
		    super(context, attrs);
		    mHolder = getHolder();
		    mHolder.addCallback(this);
		    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		    getPipeFD();
		   // mMultiplyColor = getResources().getColor(R.color.multiply_color);
		}

		// @Override
		// protected void onDraw(Canvas canvas) {
		// Log.w(this.getClass().getName(), "On Draw Called");
		// }
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				
		}
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			synchronized (this) {       
		        this.setWillNotDraw(false); // This allows us to make our own draw calls to this canvas
		    }
		    
		}
		private void getPipeFD()
		{
		    final String FUNCTION = "getPipeFD";
		    try
		    {
		    	ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
		        read_fd=pipe[0];
		        buf_fd = pipe[1];
		    }
		    catch(Exception e)
		    {
		        Log.e(TAG, FUNCTION + " : " + e.getMessage());
		    }
		}
		
	    public boolean setCamera()
	    {
	    	synchronized (this) {
		        if (isPreviewRunning)
		            return false;
		        mCamera = Camera.open(); 
		        try {
					mCamera.setPreviewDisplay(mHolder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mCamera.release();
					mCamera=null;
					return false;	
					
				}
		       
		        Camera.Parameters p = mCamera.getParameters();
		    //    List<Size> picSize=p.getSupportedPictureSizes();
		        mCamera.setDisplayOrientation(90);
		        mCamera.setParameters(p);	        
		        mCamera.startPreview();
		        isPreviewRunning=true;
		        return true;

		    }
	    }
	    public boolean prepareVideoRecorder()
	    {
	    	synchronized (this) {
	    		recorder = new MediaRecorder();
	    		mCamera.stopPreview();
	    		isPreviewRunning=false;
	    		mCamera.unlock();
	    		recorder.setCamera(mCamera);

	    		//recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
	    		
	    		//this is for android addc
	    		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
	    		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	    		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
	    		//recorder.setVideoSize(720, 480);
	    		//recorder.setVideoSize(640, 480);
	    		recorder.setVideoSize(1280, 720);
	    		//recorder.s
	    		//recorder.setVideoFrameRate(25);

	    	//	recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);	
	    		
	    		//recorder.setVideoSize(680, 480); 

	    		String mFileName = Environment.getExternalStorageDirectory()
		    	            .getAbsolutePath();
		    		mFileName += "/original.mp4";
	    		recorder.setPreviewDisplay(mHolder.getSurface());
	    		//recorder.set
	    		recorder.setOutputFile(buf_fd.getFileDescriptor());    		
	    		//recorder.setOutputFile(mFileName);       		
	    		try {
	    			recorder.prepare();
	    		} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
	    		return true;
				//recorder.start();	    
	    	}
	    }


		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
		    synchronized (this) {
		        try {
		            if (mCamera != null) {
		                //mHolder.removeCallback(this);
		            	releaseMediaRecorder();
		            	releaseCamera();
		            }
		        } catch (Exception e) {
		            Log.e("Camera", e.getMessage());
		        }
		    }
		}
	public void stop_recoder()
	{
		recorder.stop();
	}
	public void start_recorder(){
			 try {
				 recorder.start();
	    		} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
	    		}
			
	   }
	public void releaseMediaRecorder(){
        if (recorder != null) {
            // clear recorder configuration
        	recorder.reset();
            // release the recorder object
        	recorder.release();
        	recorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }
	public boolean camera_closed()
	{
		if(mCamera==null) return true;
		else return false;
	}
    public void resumePreivew(){
        if (mCamera != null){
            // release the camera for other applications
        	mCamera.startPreview();
        	isPreviewRunning=true;
        }
    }
    public void releaseCamera()
    {
    	if (mCamera != null){
            // release the camera for other applications
        	mCamera.release();
        	mCamera=null;
        	isPreviewRunning=false;
        }
    }
   }
