package com.example.callisto.testimages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.Arrays;

@SuppressLint({ "ClickableViewAccessibility", "SdCardPath" })
public class MainActivity extends ActionBarActivity implements OnTouchListener {

	Button BTcapture,BTnext,BTchangeimage,BTprevious;
	ImageView IVdisplay;
	TextView TVvalue,TVvaluelabel;
	Spinner SPimagetype;
	CheckBox CBimagefrom;

	ImageResizer mImageFetcher;
		


	String imagetypeid="", description="",imageString="",refid="";
	String currentmemberid="",baserailno="";
	String filepathtodelete="",filepathtodeletesmall="",tempfile="";

	Boolean checkclient = false,checkclientimage=false,checkclientimageslwise=false;

	public int GALLERY_IMAGE = 101;
	public int CAMERA_IMAGE = 202;
	public int CROP_PIC = 303;
	public int CAMSCANNER = 404;

	@SuppressWarnings("unused")
	private Uri picUri;

	int count_records=0;

	Bitmap bm ;
	File file;

	static final String TAG = "Touch";

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

	int count=0;

	Uri source;
	private LruCache<String, Bitmap> memoryCache;

	int imagecliecked = 0;
	DisplayMetrics metrics;
	Boolean isNetworkAvailable = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		
		
		
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
		
		
	}


	
	private void onclick() {
		// TODO Auto-generated method stub
	
				((Button)findViewById(R.id.btncrop)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
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
							Toast toast = Toast.makeText(MainActivity.this, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
							toast.show();
						}
					}
				});
	
		BTcapture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				
				
				// TODO Auto-generated method stub

				

		
				if(CBimagefrom.isChecked())
				{
					try {
						Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						intent.setType("image/*");
						startActivityForResult(Intent.createChooser(intent, "Select File"),GALLERY_IMAGE);
					} catch (Exception e) {
						Toast.makeText(MainActivity.this,"Gallery Not Supported",Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
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
						Toast.makeText(MainActivity.this,"Camera Not Supported",Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		


	}


	private void loadscreenwidget() {
		// TODO Auto-generated method stub
		BTcapture = (Button)findViewById(R.id.image_captureBT);
		BTnext = (Button)findViewById(R.id.image_nextBT);
		BTprevious = (Button)findViewById(R.id.image_previousBT);
		IVdisplay = (ImageView)findViewById(R.id.image_displayIV);
		TVvalue = (TextView)findViewById(R.id.image_descriptionTV);
		TVvaluelabel = (TextView)findViewById(R.id.image_descriptionlabelTV);
		SPimagetype = (Spinner)findViewById(R.id.image_imagetypeSP);
		BTchangeimage = (Button)findViewById(R.id.image_changeBT);
		CBimagefrom = (CheckBox)findViewById(R.id.image_imagetypeCB);

			BTnext.setVisibility(View.GONE);
			BTprevious.setVisibility(View.GONE);
			BTchangeimage.setVisibility(View.GONE);
			SPimagetype.setVisibility(View.GONE);
		IVdisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
		IVdisplay.setOnTouchListener(this);  

	

					imageString = "";//Assign imagestring of bytearray here

			if(imageString.equalsIgnoreCase(""))
			{
				IVdisplay.setImageResource(R.mipmap.ic_launcher);
				
			}
			else
			{
				
				//Common.LogSynchronzationDetails(imageString, "Image_Test", "glowLoad_image", "", ""); 

				String[] strings = imageString.split(",");

				byte[] decodedString = new byte[strings.length];
				for(int i=0;i<strings.length;i++)
				{
					decodedString[i] = Byte.valueOf(strings[i].trim());
				}

				Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
				IVdisplay.setImageBitmap(decodedByte);

			}

		
	
		
	}



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


	@SuppressLint("FloatMath")
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
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

		String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,"POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };

		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_" ).append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid " ).append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
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

	public void onActivityResult(int requestCode, int resultCode,final Intent data)  {
		// TODO Auto-generated method stub

		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			
			
			
						if (requestCode == CAMERA_IMAGE)
							{
							/*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
							builder.setTitle(getResources().getString(R.string.app_name));
							builder.setMessage("Do you want to crop this image?");
							builder.setCancelable(false);
							builder.setIcon(R.drawable.ic_launcher);

							builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {


									
								}
							});
							
							builder.setNegativeButton("No",new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
													int which) {

									String uristringpic = "";
									final ContentResolver cr = getContentResolver();     
									final String[] p1 = new String[] {
											MediaStore.Images.ImageColumns._ID,
											MediaStore.Images.ImageColumns.DATE_TAKEN
									};                   Cursor c1 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p1, null, null, p1[1] + " DESC");      
									if ( c1.moveToFirst() ) {
										uristringpic = "content://media/external/images/media/" +c1.getInt(0);

										Uri newuri = Uri.parse(uristringpic);
										tempfile =  getRealPathFromURI(String.valueOf(newuri));
										
										 mImageFetcher = new ImageResizer(MainActivity.this, 1024, 1024);
								            mImageFetcher.loadImage(tempfile, IVdisplay, new ImageWorker.OnImageLoadedListener() {
												
												@Override
												public void onImageLoaded(boolean success) {
													// TODO Auto-generated method stub
													Toast.makeText(getApplicationContext(), "Image Loaded", Toast.LENGTH_SHORT).show();
												}
											});
										Log.i("TAG", "newuri   "+newuri);

									}
									c1.close();

									File tempFile = new File(tempfile);
									if(tempFile.exists())
										tempFile.delete();


									filepathtodelete  = getRealPathFromURI(String.valueOf(source));
									if(filepathtodelete.equalsIgnoreCase(""))
									{
										Toast.makeText(MainActivity.this,"Captured Image Not Found", Toast.LENGTH_SHORT).show();
									}
									else
									{
										new ImageCompressionAsyncTask(false).execute(String.valueOf(source));
									}

									imagecliecked=1;
									invalidateOptionsMenu();
									
								}
							});


							builder.show();*/
						//	crop();
							String uristringpic = "";
							final ContentResolver cr = getContentResolver();     
							final String[] p1 = new String[] {
									MediaStore.Images.ImageColumns._ID,
									MediaStore.Images.ImageColumns.DATE_TAKEN
							};                   Cursor c1 = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, p1, null, null, p1[1] + " DESC");      
							if ( c1.moveToFirst() ) {
								uristringpic = "content://media/external/images/media/" +c1.getInt(0);

								Uri newuri = Uri.parse(uristringpic);
								tempfile =  getRealPathFromURI(String.valueOf(newuri));
								
							/*	 mImageFetcher = new ImageResizer(MainActivity.this, 1024, 1024);
						            mImageFetcher.loadImage(tempfile, IVdisplay, new ImageWorker.OnImageLoadedListener() {
										
										@Override
										public void onImageLoaded(boolean success) {
											// TODO Auto-generated method stub
											Toast.makeText(getApplicationContext(), "Image Loaded", Toast.LENGTH_SHORT).show();
										}
									});*/
								Log.i("TAG", "newuri   "+newuri);

							}
							c1.close();

							File tempFile = new File(tempfile);
							if(tempFile.exists())
								tempFile.delete();


							filepathtodelete  = getRealPathFromURI(String.valueOf(source));
							if(filepathtodelete.equalsIgnoreCase(""))
							{
								Toast.makeText(MainActivity.this,"Captured Image Not Found", Toast.LENGTH_SHORT).show();
							}
							else
							{
								new ImageCompressionAsyncTask(false).execute(String.valueOf(source));
							}

							imagecliecked=1;
							invalidateOptionsMenu();

				
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
					Toast.makeText(MainActivity.this, "Captured Image Not Found", Toast.LENGTH_SHORT).show();
				}
				else
				{
					new ImageCompressionAsyncTask(false).execute(String.valueOf(source));
				}

				imagecliecked=0;
				invalidateOptionsMenu();
			}

			else if (requestCode == GALLERY_IMAGE) {

				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(getResources().getString(R.string.app_name));
				builder.setMessage("Do you want to crop this image?");
				builder.setCancelable(false);
				builder.setIcon(R.mipmap.ic_launcher);

				builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
										int which) {


						crop();
					}
				});
				
				builder.setNegativeButton("No",new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
										int which) {
						
						System.gc();
						
						
//						Toast.makeText(MainActivity.this, String.valueOf(source), Toast.LENGTH_SHORT).show();
						source = data.getData();
					/*	filepathtodelete  = getRealPathFromURI(String.valueOf(source));

						if(filepathtodelete.equalsIgnoreCase(""))
						{
							Toast.makeText(MainActivity.this, getResources().getString(R.string.eng_CapturedImageNotFound), Toast.LENGTH_SHORT).show();
						}
						else
						{*/
							new ImageCompressionAsyncTask(false).execute(String.valueOf(source));
					//	}

						imagecliecked=1;
						invalidateOptionsMenu();
						
						
					}
				});


				builder.show();

				
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
					Toast.makeText(MainActivity.this,"Captured Image Not Found", Toast.LENGTH_SHORT).show();
				}
				else
				{
					new ImageCompressionAsyncTask(false).execute(String.valueOf("file://mnt/sdcard/scanned.jpg"));
				}

				imagecliecked=0;
				invalidateOptionsMenu();
			}

		}
		else
		{
			imagecliecked=0;
			invalidateOptionsMenu();
		}

	}

	public void displaydialogbox(String message)
	{
		AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
		builder1.setTitle(message);
		builder1.setIcon(R.mipmap.ic_launcher);

		builder1.setPositiveButton("ok",new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				// TODO Auto-generated method stub

			}
		});


		builder1.show();
	}
	class ImageCompressionAsyncTask extends AsyncTask<String, Void, String>{
		boolean fromGallery;

		ProgressDialog Dialog = new ProgressDialog(MainActivity.this);

		public ImageCompressionAsyncTask(boolean fromGallery){
			this.fromGallery = fromGallery;
		}

		protected void onPreExecute() {
			/****** NOTE: You can call UI Element here. *****/

			// Progress Dialog
			Dialog.setMessage(" Loading image Please Wait..");
			Dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			   String filePath="";
	            try{
	                 filePath = compressImage(params[0]);
	            }catch (Exception e){

	                return "";
	            }
	            return filePath;
		}

		public String compressImage(String imageUri) throws Exception{

			String filePath = getRealPathFromURI(imageUri);


			Bitmap scaledBitmap = null;

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;		
			options.inMutable=true;
			Bitmap bmp = BitmapFactory.decodeFile(filePath,options);


			int actualHeight = options.outHeight;
			int actualWidth = options.outWidth;
			float maxHeight = 512.0f;
			float maxWidth = 512.0f;
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
				scaledBitmap.compress(Bitmap.CompressFormat.JPEG,100, out);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			return filename;

		}

		@Override
		protected void onPostExecute(String result) {			 
			super.onPostExecute(result);


			Dialog.dismiss();
			
			 if(result.equalsIgnoreCase(""))
	            {
	                Toast.makeText(getApplicationContext(),"Error Occured...Try again",Toast.LENGTH_SHORT).show();
	            }
	            else
	            {
	            	loadBitmap(result, IVdisplay, MainActivity.this);
	            	
	            }

			//loadBitmap(result, IVdisplay, MainActivity.this);

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
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);



		IVdisplay.setImageBitmap(decodedByte);
		//Delete Files

		


		File smallFile = new File(filepathtodeletesmall);
		if(smallFile.exists())
			smallFile.delete();

		File cacheDir = getCacheDir();

		File[] files = cacheDir.listFiles();

		if (files != null) {
			for (File file : files)
				file.delete();
		}


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
			Toast toast = Toast.makeText(MainActivity.this, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
			toast.show();
		}
	
	}

}
