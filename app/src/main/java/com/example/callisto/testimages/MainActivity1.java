
package com.example.callisto.testimages;




import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



public class MainActivity1 extends ActionBarActivity{ 
	Button BTcapture,BTnext,BTchangeimage,BTprevious;
	ImageView IVdisplay;
	TextView TVvalue,TVvaluelabel;
	Spinner SPimagetype;
	CheckBox CBimagefrom;


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
		setContentView(R.layout.activity_main);
		
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
		 

		BTcapture.setOnClickListener(new View.OnClickListener() {

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
						Toast.makeText(MainActivity1.this,"Gallery Not Supported",Toast.LENGTH_SHORT).show();
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
						Toast.makeText(MainActivity1.this,"Camera Not Supported",Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)  {
		// TODO Auto-generated method stub

		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == CAMERA_IMAGE)

			{

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


			//	filepathtodelete  = getRealPathFromURI(String.valueOf(source));
			/*	if(filepathtodelete.equalsIgnoreCase(""))
				{
					Toast.makeText(MainActivity1.this,"Captured Image Not Found", Toast.LENGTH_SHORT).show();
				}
				else
				{*/
					new ImageCompression(getApplicationContext()).execute(String.valueOf(source));
				//}

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
			//	filepathtodelete  = getRealPathFromURI(String.valueOf(source));

				/*if(filepathtodelete.equalsIgnoreCase(""))
				{
					Toast.makeText(MainActivity1.this, "Captured Image Not Found", Toast.LENGTH_SHORT).show();
				}
				else
				{*/
					new ImageCompression(getApplicationContext()).execute(String.valueOf(source));
				//}

				imagecliecked=0;
				invalidateOptionsMenu();
			}

			else if (requestCode == GALLERY_IMAGE) {

				System.gc();
				
				
//				Toast.makeText(MainActivity.this, String.valueOf(source), Toast.LENGTH_SHORT).show();
				source = data.getData();
			/*	filepathtodelete  = getRealPathFromURI(String.valueOf(source));

				if(filepathtodelete.equalsIgnoreCase(""))
				{
					Toast.makeText(MainActivity.this, getResources().getString(R.string.eng_CapturedImageNotFound), Toast.LENGTH_SHORT).show();
				}
				else
				{*/
					new ImageCompression(getApplicationContext()).execute(String.valueOf(source));
			//	}

				imagecliecked=1;
				invalidateOptionsMenu();
			}

			

		}
		else
		{
			imagecliecked=0;
			invalidateOptionsMenu();
		}

	}


	public class ImageCompression extends AsyncTask<String, Void, String> {

	    private Context context;
	    private static final float maxHeight = 1280.0f;
	    private static final float maxWidth = 1280.0f;


	    public ImageCompression(Context context){
	        this.context=context;
	    }

	    @Override
	    protected String doInBackground(String... strings) {
	        if(strings.length == 0 || strings[0] == null)
	            return null;

	        return compressImage(strings[0]);
	    }

	    protected void onPostExecute(String imagePath){
	        // imagePath is path of new compressed image.
	    }


	    public String compressImage(String imagePath) {
	        Bitmap scaledBitmap = null;

	        BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

	        int actualHeight = options.outHeight;
	        int actualWidth = options.outWidth;

	        float imgRatio = (float) actualWidth / (float) actualHeight;
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
	        options.inTempStorage = new byte[16 * 1024];

	        try {
	            bmp = BitmapFactory.decodeFile(imagePath, options);
	        } catch (OutOfMemoryError exception) {
	            exception.printStackTrace();

	        }
	        try {
	            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
	        } catch (OutOfMemoryError exception) {
	            exception.printStackTrace();
	        }

	        float ratioX = actualWidth / (float) options.outWidth;
	        float ratioY = actualHeight / (float) options.outHeight;
	        float middleX = actualWidth / 2.0f;
	        float middleY = actualHeight / 2.0f;

	        Matrix scaleMatrix = new Matrix();
	        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

	        Canvas canvas = new Canvas(scaledBitmap);
	        canvas.setMatrix(scaleMatrix);
	        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

	        if(bmp!=null)
	        {
	            bmp.recycle();
	        }

	        ExifInterface exif;
	        try {
	            exif = new ExifInterface(imagePath);
	            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
	            Matrix matrix = new Matrix();
	            if (orientation == 6) {
	                matrix.postRotate(90);
	            } else if (orientation == 3) {
	                matrix.postRotate(180);
	            } else if (orientation == 8) {
	                matrix.postRotate(270);
	            }
	            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        FileOutputStream out = null;
	        String filepath = getFilename();
	        try {
	            out = new FileOutputStream(filepath);

	           //write the compressed bitmap at the destination specified by filename.
	            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }

	        return filepath;
	    }

	    public  int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

	    public String getFilename() {
	        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
	                + "/Android/data/"
	                + context.getApplicationContext().getPackageName()
	                + "/Files/Compressed");

	        // Create the storage directory if it does not exist
	        if (! mediaStorageDir.exists()){
	            mediaStorageDir.mkdirs();
	        }

	        String mImageName="IMG_"+ String.valueOf(System.currentTimeMillis()) +".jpg";
	        String uriString = (mediaStorageDir.getAbsolutePath() + "/"+ mImageName);;
	        return uriString;

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

}
