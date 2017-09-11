package com.example.callisto.testimages;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

@SuppressLint({ "InlinedApi", "FloatMath", "SdCardPath" })
public class MainActivity2 extends ActionBarActivity  implements OnTouchListener {

	Button BTcapture,BTCrop,BTChange;
	ImageView IVdisplay;
	TextView TVvalue;
	Spinner SPimagetype;



	

	String imagetypeid="", description="",descriptiondetails="",imageString="",isedited="";
	String filepathtodelete="",filepathtodeletesmall="",tempfile="";

	Boolean checkclient = false,checkclientimage=false;

	public int GALLERY_IMAGE = 101;
	public int CAMERA_IMAGE = 202;
	public int CROP_PIC = 303;
	public int CAMSCANNER = 404;
	public int SIGNATURE_IMAGE = 505;

	int count_records=0;

	Bitmap bm ;
	File file;

	private static final String TAG = "Touch";

	 static final float MIN_ZOOM = 1.0f;
	 static final float MAX_ZOOM = 5.0f;

	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	LinearLayout layoutOfPopup;
	PopupWindow popupMessage;

	TextView popupText,popupheading;
	Button insidePopupButton;

	byte[] bytearray;
	
	int imagecliecked = 0;
	
	Uri source;
	
	DisplayMetrics metrics;
	private LruCache<String, Bitmap> memoryCache;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

	
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		setContentView(R.layout.activity_main);

		
		int cachesize = 60*1024*1024;

		memoryCache	 = new LruCache<String, Bitmap>(cachesize){

			@Override
			protected int sizeOf(String key, Bitmap value) {
				if(android.os.Build.VERSION.SDK_INT>=12){
					return value.getByteCount();
				}
				else{
					return value.getRowBytes()*value.getHeight();
				}
			}
		};
		
		
		loadscreenwidget();

		onclick();
		//IVdisplay.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.test, 612, 816));
	}

	
	private void onclick() {
		BTChange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image/*");
					startActivityForResult(Intent.createChooser(intent, "Select File"),GALLERY_IMAGE);
				} catch (Exception e) {
					Toast.makeText(MainActivity2.this,"Gallery Not Supported",Toast.LENGTH_SHORT).show();
				}
			}
		});
		// TODO Auto-generated method stub
		SPimagetype.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});  

		BTCrop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				crop();
			}
		});
		BTcapture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				try {
					String fileName = String.valueOf(System.currentTimeMillis());

					ContentValues values = new ContentValues();
					values.put(MediaStore.Images.Media.TITLE, fileName);
					values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
					source = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, source);
					startActivityForResult(intent, CAMERA_IMAGE);
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(MainActivity2.this,"Camera Not Supported",Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		
		
	
	}


	private void loadscreenwidget() {
		// TODO Auto-generated method stub
		BTcapture = (Button)findViewById(R.id.image_captureBT);
		IVdisplay = (ImageView)findViewById(R.id.image_displayIV);
		TVvalue = (TextView)findViewById(R.id.image_descriptionTV);
		SPimagetype = (Spinner)findViewById(R.id.image_imagetypeSP);
		BTCrop = (Button)findViewById(R.id.btncrop);
		BTChange=(Button)findViewById(R.id.image_changeBT);
		IVdisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
		IVdisplay.setOnTouchListener(this);  


	}




	
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		view.setScaleType(ImageView.ScaleType.MATRIX);
		float scale;

		// Dump touch event to log
		dumpEvent(event);

		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {

		case MotionEvent.ACTION_DOWN: //first finger down only
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			Log.d(TAG, "mode=DRAG" );
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP: //first finger lifted
		case MotionEvent.ACTION_POINTER_UP: //second finger lifted
			mode = NONE;
			Log.d(TAG, "mode=NONE" );
			break;
		case MotionEvent.ACTION_POINTER_DOWN: //second finger down
			oldDist = spacing(event);
			Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 5f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				Log.d(TAG, "mode=ZOOM" );
			}
			break;

		case MotionEvent.ACTION_MOVE: 

			if (mode == ZOOM) { //pinch zooming
				float newDist = spacing(event);
				Log.d(TAG, "newDist=" + newDist);
				if (newDist > 5f) {
					matrix.set(savedMatrix);
					scale = newDist / oldDist; //**thinking i need to play around with this value to limit it**
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			else if (mode == DRAG) { //movement of first finger
				matrix.set(savedMatrix);
				if (view.getLeft() >= -392){
					matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
				}
			}
			break;
		}

		// Perform the transformation
		view.setImageMatrix(matrix);

		return true; // indicate event was handled
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		//return FloatMath.sqrt(x * x + y * y);
		return (float)Math.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/** Show an event in the LogCat view, for debugging */
	@SuppressWarnings("deprecation")
	private void dumpEvent(MotionEvent event) {
		String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
				"POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_" ).append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid " ).append(
					action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")" );
		}
		sb.append("[" );
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#" ).append(i);
			sb.append("(pid " ).append(event.getPointerId(i));
			sb.append(")=" ).append((int) event.getX(i));
			sb.append("," ).append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";" );
		}
		sb.append("]" );
		Log.d(TAG, sb.toString());
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)  {
		// TODO Auto-generated method stub


		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == CAMERA_IMAGE)

			{

			/*	String uristringpic = "";
				final ContentResolver cr = getContentResolver();     
				final String[] p1 = new String[] {MediaStore.Images.ImageColumns._ID,MediaStore.Images.ImageColumns.DATE_TAKEN};
				
				Cursor c1 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p1, null, null, p1[1] + " DESC");      
				if ( c1.moveToFirst() ) {
					uristringpic = "content://media/external/images/media/" +c1.getInt(0);

					Uri newuri = Uri.parse(uristringpic);
					tempfile =  getRealPathFromURI(String.valueOf(newuri));
					Log.i("TAG", "newuri   "+newuri);

				}
				c1.close();

				File tempFile = new File(tempfile);
				if(tempFile.exists())
					tempFile.delete();


				filepathtodelete  = getRealPathFromURI(String.valueOf(source));
				if(filepathtodelete.equalsIgnoreCase(""))
				{
					Toast.makeText(MainActivity2.this,"Camera not supported", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(MainActivity2.this,filepathtodelete, Toast.LENGTH_SHORT).show();
					new ImageCompressionAsyncTask(false).execute(String.valueOf(source));
				}

				imagecliecked=1;
				invalidateOptionsMenu();*/
				try {
				File actualImage;
						File compressedImage;
				actualImage = FileUtil.from(this, source);
				if (actualImage == null) {
					Toast.makeText(MainActivity2.this,"\"Please choose an image!\"", Toast.LENGTH_SHORT).show();
				} else {
					// Compress image in main thread using custom Compressor

						compressedImage = new Compressor(MainActivity2.this)
								.setMaxWidth(640)
								.setMaxHeight(480)
								.setQuality(75)
								.setCompressFormat(Bitmap.CompressFormat.WEBP)
								.setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
										Environment.DIRECTORY_PICTURES).getAbsolutePath())
								.compressToFile(actualImage);

					IVdisplay.setImageBitmap(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
					BTcapture.setText(String.format("Size : %s", getReadableFileSize(compressedImage.length())));

				}
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity2.this,e.getMessage(),Toast.LENGTH_SHORT).show();
				}
			}
			else if (requestCode == CROP_PIC) {

				System.gc();
//				File largeFile = new File(filepathtodelete);
//				if(largeFile.exists())
//				{
//					largeFile.delete();
//				}
				//				source = data.getData();
				filepathtodelete  = getRealPathFromURI(String.valueOf(source));

				if(filepathtodelete.equalsIgnoreCase(""))
				{
					Toast.makeText(MainActivity2.this,"Camera Not Supported", Toast.LENGTH_SHORT).show();
				}
				else
				{
					new ImageCompressionAsyncTask(false).execute(String.valueOf(source));
				}

				imagecliecked=0;
				invalidateOptionsMenu();
			}

			else if (requestCode == GALLERY_IMAGE) {

				System.gc();
				
				
//				Toast.makeText(MainActivity2.this, String.valueOf(source), Toast.LENGTH_SHORT).show();
				source = data.getData();
			/*	filepathtodelete  = getRealPathFromURI(String.valueOf(source));

				if(filepathtodelete.equalsIgnoreCase(""))
				{
					Toast.makeText(MainActivity2.this,"Camera Not Supported", Toast.LENGTH_SHORT).show();
				}
				else
				{
					new ImageCompressionAsyncTask(false).execute(String.valueOf(source));
				}

				imagecliecked=1;
				invalidateOptionsMenu();*/
				try {
					File actualImage;
					File compressedImage;
					actualImage = FileUtil.from(this, source);
					if (actualImage == null) {
						Toast.makeText(MainActivity2.this,"\"Please choose an image!\"", Toast.LENGTH_SHORT).show();
					} else {
						// Compress image in main thread using custom Compressor

						compressedImage = new Compressor(MainActivity2.this)
								.setMaxWidth(640)
								.setMaxHeight(480)
								.setQuality(75)
								.setCompressFormat(Bitmap.CompressFormat.WEBP)
								.setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
										Environment.DIRECTORY_PICTURES).getAbsolutePath())
								.compressToFile(actualImage);

						IVdisplay.setImageBitmap(BitmapFactory.decodeFile(compressedImage.getAbsolutePath()));
						BTcapture.setText(String.format("Size : %s", getReadableFileSize(compressedImage.length())));

					}
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity2.this,e.getMessage(),Toast.LENGTH_SHORT).show();
				}
			}

			else if (requestCode == CAMSCANNER) {

				//				File largeFile = new File(filepathtodelete);
				//				if(largeFile.exists())
				//				{
				//					largeFile.delete();
				//				}


				filepathtodelete  = getRealPathFromURI(String.valueOf("file://mnt/sdcard/scanned.jpg"));

				if(filepathtodelete.equalsIgnoreCase(""))
				{
					Toast.makeText(MainActivity2.this,"Camera Not Supported", Toast.LENGTH_SHORT).show();
				}
				else
				{
					new ImageCompressionAsyncTask(false).execute(String.valueOf("file://mnt/sdcard/scanned.jpg"));
				}

				imagecliecked=0;
				invalidateOptionsMenu();
			}
			
			else if (requestCode == SIGNATURE_IMAGE)

			{/*
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm = rotateImage(Common.SignatureImage, 90);


				bm.compress(Bitmap.CompressFormat.PNG, 70, baos);
				
				byte[] b = baos.toByteArray();

				imageString = "";
				imageString = Arrays.toString(b);



				imageString = imageString.substring(1, imageString.length()-1);

				Log.d("LOGTAG", "bytearraystring.....Start"+imageString+"bytearraystring.....End");

				String[] strings = imageString.split(",");

				byte[] decodedString = new byte[strings.length];
				for(int i=0;i<strings.length;i++)
				{
					decodedString[i] = Byte.valueOf(strings[i].trim());
				}

				Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);



				IVdisplay.setImageBitmap(decodedByte);
				
				imagecliecked=0;
				invalidateOptionsMenu();

			*/}

		}
		else
		{
			imagecliecked=0;
			invalidateOptionsMenu();
		}

	

	}

	
	class ImageCompressionAsyncTask extends AsyncTask<String, Void, String>{
		boolean fromGallery;

		ProgressDialog Dialog = new ProgressDialog(MainActivity2.this);

		public ImageCompressionAsyncTask(boolean fromGallery){
			this.fromGallery = fromGallery;
		}

		protected void onPreExecute() {
			/****** NOTE: You can call UI Element here. *****/

			// Progress Dialog
			Dialog.setMessage("Loading");
			Dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String filePath = compressImage(params[0]);
			return filePath;
		}

		public String compressImage(String imageUri) {

			String filePath = getRealPathFromURI(imageUri);


			Bitmap scaledBitmap = null;

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;		
			options.inMutable=true;
			Bitmap bmp = BitmapFactory.decodeFile(filePath,options);


			int actualHeight = options.outHeight;
			int actualWidth = options.outWidth;
			float maxHeight = 816.0f;
			float maxWidth = 612.0f;
			float imgRatio = actualWidth / actualHeight;
			float maxRatio = maxWidth / maxHeight;

			if (actualHeight > maxHeight || actualWidth > maxWidth) {
				if (imgRatio < maxRatio) {
					imgRatio = maxHeight / actualHeight;
					actualWidth = (int) (imgRatio * actualWidth);
					actualHeight = (int) maxHeight;
				} else if (imgRatio > maxRatio) {
					imgRatio = maxWidth / actualWidth;
					actualHeight = (int) (imgRatio * actualHeight);
					actualWidth = (int) maxWidth;
				} else {
					actualHeight = (int) maxHeight;
					actualWidth = (int) maxWidth;

				}
			}

			options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPurgeable = true;
			options.inInputShareable = true;
			options.inTempStorage = new byte[16*1024];

			try{	
				bmp = BitmapFactory.decodeFile(filePath,options);
			}
			catch(OutOfMemoryError exception){
				exception.printStackTrace();

			}
			try{
				scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
			}
			catch(OutOfMemoryError exception){
				exception.printStackTrace();
			}

			float ratioX = actualWidth / (float) options.outWidth;
			float ratioY = actualHeight / (float)options.outHeight;
			float middleX = actualWidth / 2.0f;
			float middleY = actualHeight / 2.0f;

			Matrix scaleMatrix = new Matrix();
			scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

			Canvas canvas = new Canvas(scaledBitmap);
			canvas.setMatrix(scaleMatrix);
			canvas.drawBitmap(bmp, middleX - bmp.getWidth()/2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));


			ExifInterface exif;
			try {
				exif = new ExifInterface(filePath);

				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
				Log.d("EXIF", "Exif: " + orientation);
				Matrix matrix = new Matrix();
				if (orientation == 6) {
					matrix.postRotate(90);
					Log.d("EXIF", "Exif: " + orientation);
				} else if (orientation == 3) {
					matrix.postRotate(180);
					Log.d("EXIF", "Exif: " + orientation);
				} else if (orientation == 8) {
					matrix.postRotate(270);
					Log.d("EXIF", "Exif: " + orientation);
				}
				scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0,0,scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			FileOutputStream out = null;
			String filename = getFilename();
			filepathtodeletesmall = filename;
			try {
				out = new FileOutputStream(filename);
				scaledBitmap.compress(Bitmap.CompressFormat.JPEG,80, out);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			return filename;

		}

		@Override
		protected void onPostExecute(String result) {			 
			super.onPostExecute(result);


			Dialog.dismiss();

			loadBitmap(result, IVdisplay, MainActivity2.this);

		}
	}


	public String getRealPathFromURI(String contentURI) {

		Uri contentUri = Uri.parse(contentURI);
		Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
		if (cursor == null) {

			return contentUri.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			String string =  cursor.getString(idx);

			return string;

		}

	}
	public String getFilename() {
		File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
		if (!file.exists()) {
			file.mkdirs();
		}
		String uriSting = (file.getAbsolutePath() + "/"+ System.currentTimeMillis() + ".jpg");
		return uriSting;

	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			memoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return memoryCache.get(key);
	}
/*	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		final float totalPixels = width * height;
		final float totalReqPixelsCap = reqWidth * reqHeight * 2;

		while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
			inSampleSize++;
		}

		return inSampleSize;
	}*/
	public  int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    Log.e("Test out", String.valueOf(options.outHeight));
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) >= reqHeight
                && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2;
        }
    }
		Toast.makeText(getApplicationContext(), String.valueOf(inSampleSize), Toast.LENGTH_SHORT).show();
    return inSampleSize;
}
	public  Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    String root = Environment.getExternalStorageDirectory().toString();
	    File myDir = new File(root + "/req_images");
	    myDir.mkdirs();
	    Random generator = new Random();
	    int n = 10000;
	    n = generator.nextInt(n);
	    String fname = "Image-" + n + ".jpg";
	    File file = new File(myDir, fname);
	    Log.i(TAG, "" + file);
	    if (file.exists())
	        file.delete();
	    Bitmap bm=null;
	    try {
	    	 bm=BitmapFactory.decodeResource(res, resId, options);
	    	Log.e("test",String.valueOf(bm.getHeight()));
	    	 bm=mark(bm,"This is a text");
	        FileOutputStream out = new FileOutputStream(file);
	        bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
	        out.flush();
	        out.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return bm;
	}
	public void loadBitmap(String filePath, ImageView imageView, Context context) {
		if (cancelPotentialWork(filePath, imageView)) {
			final Bitmap bitmap = getBitmapFromMemCache(filePath);
			if(bitmap != null){
				//				imageView.setImageBitmap(bitmap);
				getimagebytearray(bitmap);
			}
			else{
				final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
				final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher), task);
				imageView.setImageDrawable(asyncDrawable);
				task.execute(filePath);
			}
		}
	}
	class AsyncDrawable extends BitmapDrawable {

		final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	public boolean cancelPotentialWork(String filePath, ImageView imageView) {

		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapFilePath = bitmapWorkerTask.filePath;
			if (bitmapFilePath != null && !bitmapFilePath.equalsIgnoreCase(filePath)) {
				bitmapWorkerTask.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}

	BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap>{

		final WeakReference<ImageView> imageViewReference;
		public String filePath;

		public BitmapWorkerTask(ImageView imageView){
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			filePath = params[0];
			Bitmap bitmap = decodeBitmapFromPath(filePath);
			addBitmapToMemoryCache(filePath, bitmap);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {


			if (isCancelled()) {
				bitmap = null;
			}
			if(imageViewReference != null && bitmap != null){
				final ImageView imageView = imageViewReference.get();
				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
				if (this == bitmapWorkerTask && imageView != null) {
					//					imageView.setImageBitmap(bitmap);
					getimagebytearray(bitmap);
				}
			}
		}
	}
	public Bitmap decodeBitmapFromPath(String filePath){
		Bitmap scaledBitmap = null;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;						
		scaledBitmap = BitmapFactory.decodeFile(filePath,options);

		options.inSampleSize = calculateInSampleSize(options, convertDipToPixels(150), convertDipToPixels(200));
		options.inDither = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inJustDecodeBounds = false;

		scaledBitmap = BitmapFactory.decodeFile(filePath, options);		
		return scaledBitmap;
	}

	public int convertDipToPixels(float dips){
		Resources r = getResources();
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, r.getDisplayMetrics());
	}

	public void  getimagebytearray(Bitmap bitmap)
	{

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);

		byte[] b = baos.toByteArray();

		imageString = "";
		imageString = Arrays.toString(b);



		imageString = imageString.substring(1, imageString.length()-1);

		Log.d("LOGTAG", "bytearraystring.....Start"+imageString+"bytearraystring.....End");

		String[] strings = imageString.split(",");

		byte[] decodedString = new byte[strings.length];
		for(int i=0;i<strings.length;i++)
		{
			decodedString[i] = Byte.valueOf(strings[i].trim());
		}

		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);//mark(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length),"This is a text,This is a Text");

	    String root = Environment.getExternalStorageDirectory().toString();
	    File myDir = new File(root + "/req_images");
	    myDir.mkdirs();
	    Random generator = new Random();
	    int n = 10000;
	    n = generator.nextInt(n);
	    String fname = "Image-" + n + ".jpg";
	    File file1 = new File(myDir, fname);
	    Log.i(TAG, "" + file);
	    if (file1.exists())
	        file1.delete();
	    Bitmap bm=null;
	    try {
	    	 bm=decodedByte;
	    	
	    	// bm=mark(bm,"This is a text");
	        FileOutputStream out = new FileOutputStream(file1);
	        bm.compress(Bitmap.CompressFormat.PNG, 100, out);
	        out.flush();
	        out.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

		IVdisplay.setImageBitmap(decodedByte);
		//Delete Files

		


//		File smallFile = new File(filepathtodeletesmall);
//		if(smallFile.exists())
//			smallFile.delete();

		File cacheDir = getCacheDir();

		File[] files = cacheDir.listFiles();

		if (files != null) {
			for (File file : files)
				file.delete();
		}


	}
	
	private Bitmap rotateImage(Bitmap source, float angle) {

		Bitmap bitmap = null;
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		try {
			bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),matrix, true);
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
		}
		return bitmap;
	}
	
	public static Bitmap mark(Bitmap src, String watermark) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(18);
        paint.setAntiAlias(true);
        paint.setUnderlineText(true);
        canvas.drawText(watermark, 20, 25, paint);

        return result;
    }

	void crop(){

        // TODO Auto-generated method stub
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");

            // indicate image type and Uri
            cropIntent.setDataAndType(source, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 0);
            cropIntent.putExtra("aspectY", 0);
            // indicate output X and Y


            cropIntent.putExtra("outputX", 800);
            cropIntent.putExtra("outputY",800);
            // retrieve data on return
            cropIntent.putExtra("scale", true);
            //				cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult

            String fileName = String.valueOf(System.currentTimeMillis());


            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            values.put(MediaStore.Images.Media.DESCRIPTION,"Image Cropped");

            source = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, source);

            startActivityForResult(cropIntent, CROP_PIC);

        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            Toast toast = Toast.makeText(MainActivity2.this, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
	public String getReadableFileSize(long size) {
		if (size <= 0) {
			return "0";
		}
		final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
