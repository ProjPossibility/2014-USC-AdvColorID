package com.example.android.photobyintent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


public class PhotoIntentActivity extends Activity {

	private static final int ACTION_TAKE_PHOTO_B = 1;
	private static final int ACTION_TAKE_PHOTO_S = 2;
	private static final int ACTION_TAKE_VIDEO = 3;

	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;
	private Bitmap mImageBitmap;

	private static final String VIDEO_STORAGE_KEY = "viewvideo";
	private static final String VIDEOVIEW_VISIBILITY_STORAGE_KEY = "videoviewvisibility";
	private VideoView mVideoView;
	private Uri mVideoUri;
	
	private TextView textView;
	private TextView textView2;
	private TextView textView3;
	private TextView textView4;
	private TextView textView5;
	TextToSpeech tts ;

	private String mCurrentPhotoPath;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private int eventX;
	private int eventY;
	int scaleFactor;
	
	
	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	
	}

	
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}

	private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();
	
		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		//bmOptions.inJustDecodeBounds = true;
		bmOptions.inJustDecodeBounds = false;
		
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
			
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		//bmOptions.inSampleSize = 3;
		bmOptions.inPurgeable = true;
		textView4.setText(textView4.getText()+" sF="+scaleFactor);
	//	bmOptions.inScaled = true;
		

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		bitmap=procBitmapedBitmap(bitmap);
		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(bitmap);
		//mImageView.buildDrawingCache();
		// bitmap = mImageView.getDrawingCache();
		// mImageView.destroyDrawingCache();
		//
		textView4.setText(textView4.getText()+"viewW="+mImageView.getWidth()+" viewH="+mImageView.getHeight());
		
		mVideoUri = null;
		mImageView.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.INVISIBLE);
	}

	private Bitmap procBitmapedBitmap(final Bitmap src){
		 final Bitmap dest = Bitmap.createBitmap(
	                720, 540, src.getConfig());
		 
		 textView3.setText("srcW="+dest.getWidth()+" srcH="+ dest.getHeight());
		 mImageView.setOnTouchListener(new OnTouchListener() {
			 public boolean onTouch(View v, MotionEvent event) {
				 
				 eventX = (int) event.getX();
	            eventY = (int) event.getY();
	    
	            if (((eventX>=0)&&(eventY>=0)&&(eventX<dest.getWidth())&&(eventY<dest.getHeight()))){
	            	textView.setText("x="+eventX+" y="+eventY);
	             
	            	int pixelColor = dest.getPixel(eventX, eventY);
	            	//if (!(pixelColor.equals(null))){
	            		int pixelAlpha = Color.alpha(pixelColor);
	            		int pixelRed = Color.red(pixelColor);
	            		int pixelGreen = Color.green(pixelColor);
	            		int pixelBlue = Color.blue(pixelColor);
	            		
	            		textView2.setText("R="+pixelRed+" G="+pixelGreen+" B="+pixelBlue);
	            		String ss="";
	            		try {
							 ss = proceedComputation(pixelRed, pixelGreen, pixelBlue);
							  //tts.speak(ss, TextToSpeech.QUEUE_FLUSH, null);
						} catch (XmlPullParserException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            		textView5.setText("Color is "+ss);
	            	//}
	            }
	             
		        return true;
		        }
		});
		 for(int x = 0; x < 720; x++){
	            for(int y = 0; y < 540; y++){
		 
				    // получим каждый пиксель
	                int pixelColor = src.getPixel(x, y);
					// получим информацию о прозрачности
	                int pixelAlpha = Color.alpha(pixelColor);
					// получим цвет каждого пикселя
	                int pixelRed = Color.red(pixelColor);
	                int pixelGreen = Color.green(pixelColor);
	                int pixelBlue = Color.blue(pixelColor);
	                
					// перемешаем цвета
	               // int newPixel= Color.argb(
	               //         pixelAlpha, pixelBlue, pixelRed, pixelGreen);
	                int newPixel= Color.argb(
	                		pixelAlpha, pixelRed,pixelGreen, pixelBlue);
					// полученный результат вернём в Bitmap		
	                dest.setPixel(x, y, newPixel);
	            }
	        }
		 
	        return dest;
		//return null;
	}
	
	
	private String proceedComputation(int vR, int vG, int vB) throws XmlPullParserException, IOException{
		String curColor = "";
		
		try {
        int sum1 = 0, sum2 = 0;
        String starttag = "";
        int i = 0;
        int pixelRed = vR;
        int pixelGreen = vG;
        int pixelBlue = vB;
        String na = "";
        int r = 0;
        int g = 0;
        int b = 0;
        final String LOG_TAG = "myLogs";
        
        XmlPullParser parser = getResources().getXml(R.color.rgb);
        
        String str="";
        while (parser.getEventType()!= XmlPullParser.END_DOCUMENT) {
        	switch (parser.getEventType()) {
        	 case XmlPullParser.START_TAG:{
        		 starttag = parser.getName();
        		 break;}
        	case XmlPullParser.TEXT:{
        		 if (starttag.equals("name")){
        			 na = parser.getText();
        		 }
        		 if (starttag.equals("r")){
        			 r = Integer.parseInt(parser.getText());
        		 }
        		 if (starttag.equals("g")){
        			 g = Integer.parseInt(parser.getText());
        		 }
        		 if (starttag.equals("b")){
        			 b = Integer.parseInt(parser.getText());
        		 }
        		 break;}
        	
        	 case XmlPullParser.END_TAG:{
        		 if (parser.getName().equals("b")){
        			 sum2 = Math.abs(vR - r) + Math.abs(vG - g) + Math.abs(vB - b);
        			 int k = i;
        			 if (k !=0 ){
        				 k = i-1 ;
        			 }else{
        				 sum1 = sum2;
        			 }
        			 
        		 if (sum2 < sum1){
        			// curColor = curColor+" "+r+" "+g+" "+b+" ";
        			 curColor = /*curColor+" CCC= "+*/na;
        			 sum1 = sum2;
        		 }
        		 i=i+1;
        		 }
        		 break;}
        	 default:break;
        	}
        	parser.next();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			curColor="error";
			e.printStackTrace();
		}
		finally {
			return curColor;
		}
	}

	 @Override
	 public boolean onTouchEvent(MotionEvent event) {
	     switch ( event.getAction() ) {
	         case MotionEvent.ACTION_DOWN:
	             eventX = (int)event.getX();
	             eventY = (int)event.getY();
	     }
	    // int eventX = mImageView.getWidth();
			//int eventY = mImageView.getHeight();
	     
	    // textView.setText("W="+eventX+" H="+eventY);
		return false;
		}
	 
	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentPhotoPath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    this.sendBroadcast(mediaScanIntent);
	}

	private void dispatchTakePictureIntent(int actionCode) {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		switch(actionCode) {
		case ACTION_TAKE_PHOTO_B:
			File f = null;
			
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}
			break;

		default:
			break;			
		} // switch

		startActivityForResult(takePictureIntent, actionCode);
	}

	private void dispatchTakeVideoIntent() {
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
	}

	private void handleSmallCameraPhoto(Intent intent) {
		Bundle extras = intent.getExtras();
		mImageBitmap = (Bitmap) extras.get("data");
		mImageView.setImageBitmap(mImageBitmap);
		mVideoUri = null;
		mImageView.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.INVISIBLE);
	}

	private void handleBigCameraPhoto() {

		if (mCurrentPhotoPath != null) {
			setPic();
			galleryAddPic();
			mCurrentPhotoPath = null;
		}

	}

	private void handleCameraVideo(Intent intent) {
		mVideoUri = intent.getData();
		mVideoView.setVideoURI(mVideoUri);
		mImageBitmap = null;
		mVideoView.setVisibility(View.VISIBLE);
		mImageView.setVisibility(View.INVISIBLE);
	}

	Button.OnClickListener mTakePicOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
		}
	};

	Button.OnClickListener mTakePicSOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
		}
	};

	Button.OnClickListener mTakeVidOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakeVideoIntent();
		}
	};

	
	 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mImageView = (ImageView) findViewById(R.id.imageView1);
		mVideoView = (VideoView) findViewById(R.id.videoView1);
		textView = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		// tts = new TextToSpeech(this, (OnInitListener) this);
		mImageBitmap = null;
		mVideoUri = null;
  
		Button picBtn = (Button) findViewById(R.id.btnIntend);
		setBtnListenerOrDisable( 
				picBtn, 
				mTakePicOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
		);

		Button picSBtn = (Button) findViewById(R.id.btnIntendS);
		setBtnListenerOrDisable( 
				picSBtn, 
				mTakePicSOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
		);

		Button vidBtn = (Button) findViewById(R.id.btnIntendV);
		setBtnListenerOrDisable( 
				vidBtn, 
				mTakeVidOnClickListener,
				MediaStore.ACTION_VIDEO_CAPTURE
		);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_TAKE_PHOTO_B: {
			if (resultCode == RESULT_OK) {
				handleBigCameraPhoto();
			}
			break;
		} // ACTION_TAKE_PHOTO_B

		case ACTION_TAKE_PHOTO_S: {
			if (resultCode == RESULT_OK) {
				handleSmallCameraPhoto(data);
			}
			break;
		} // ACTION_TAKE_PHOTO_S

		case ACTION_TAKE_VIDEO: {
			if (resultCode == RESULT_OK) {
				handleCameraVideo(data);
			}
			break;
		} // ACTION_TAKE_VIDEO
		} // switch
	}

	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putParcelable(VIDEO_STORAGE_KEY, mVideoUri);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		outState.putBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY, (mVideoUri != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		mVideoUri = savedInstanceState.getParcelable(VIDEO_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
		mVideoView.setVideoURI(mVideoUri);
		mVideoView.setVisibility(
				savedInstanceState.getBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void setBtnListenerOrDisable( 
			Button btn, 
			Button.OnClickListener onClickListener,
			String intentName
	) {
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setText( 
				getText(R.string.cannot).toString() + " " + btn.getText());
			btn.setClickable(false);
		}
	}

}