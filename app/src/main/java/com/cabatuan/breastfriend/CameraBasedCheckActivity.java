package com.cabatuan.breastfriend;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by cobalt on 11/4/15.
 */
public class CameraBasedCheckActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static final String TAG = "CameraBasedCheck";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_HIST = 1;
    public static final int VIEW_MODE_CANNY = 2;
    public static final int VIEW_MODE_SEPIA = 3;
    public static final int VIEW_MODE_SOBEL = 4;
    public static final int VIEW_MODE_ZOOM = 5;
    public static final int VIEW_MODE_PIXELIZE = 6;
    public static final int VIEW_MODE_POSTERIZE = 7;
    public static final int VIEW_MODE_DETECT = 8;
    public static final int VIEW_MODE_CONTOUR = 9;
    public static final int VIEW_MODE_TRACK = 10;
    public static final int VIEW_MODE_TEXTURE = 11;
    public static final int VIEW_MODE_THRESHOLD = 12;

    public static int viewMode = VIEW_MODE_RGBA;

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private DetectionBasedTracker mNativeDetector;

    /// For Image Reduction
    private static int scale = 2;
    private static final int WIDTH = 1280 / scale;
    private static final int HEIGHT = 720 / scale;

    private static final float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private OpenCvCameraView mOpenCvCameraView;

    private Size mSize0;

    private Mat mIntermediateMat;
    private Mat mMat0;
    private MatOfInt mChannels[];
    private MatOfInt mHistSize;
    private int mHistSizeNum = 25;
    private MatOfFloat mRanges;
    private Scalar mColorsRGB[];
    private Scalar mColorsHue[];
    private Scalar mWhilte;
    private Point mP1;
    private Point mP2;
    private float mBuff[];
    private Mat mSepiaKernel;

    // ROI Selection
    SurfaceHolder _holder;
    private int _canvasImgYOffset;
    private int _canvasImgXOffset;
    private AtomicReference<Point> trackedBox1stCorner;
    private Paint rectPaint;

    private Rect _trackedBox = null;

    private boolean cmtInitialised = false;
    private boolean isShowTrackToast = true;

    private Point globalCenter;
    private int targetRadius;

    // Camera
    private static final String STATE_CAMERA_INDEX = "cameraIndex";
    private boolean mIsCameraFrontFacing;
    private int mNumCameras;
    private int mCameraIndex = 0;  // default to front camera

    // Take picture view
    private Bitmap photo;
    private Boolean isTakePicture = false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    //Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");
                    System.loadLibrary("opencv_native_module");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalbreast);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "haarcascade_frontalbreast.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                       // Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();

                    mOpenCvCameraView.setOnTouchListener(CameraBasedCheckActivity.this);

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public CameraBasedCheckActivity() {
       // Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        final Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null) {
            mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
        }

        setContentView(R.layout.activity_camera_based_check);

        mOpenCvCameraView = (OpenCvCameraView) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCameraIndex(mCameraIndex);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mNumCameras = mOpenCvCameraView.getNumberOfCameras();
        mIsCameraFrontFacing = mOpenCvCameraView.isCameraFrontFacing();
        // mOpenCvCameraView.setMaxFrameSize(WIDTH, HEIGHT);
        mOpenCvCameraView.setCvCameraViewListener(this);

        _holder = mOpenCvCameraView.getHolder();

        // ROI selection initialization
        trackedBox1stCorner = new AtomicReference<Point>();
        rectPaint = new Paint();
        rectPaint.setColor(Color.rgb(0, 255, 0));
        rectPaint.setStrokeWidth(5);
        rectPaint.setStyle(Style.STROKE);
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current camera index.
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        cmtInitialised = false;
        _trackedBox = null;
        isShowTrackToast = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        cmtInitialised = false;
        _trackedBox = null;
        isShowTrackToast = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            // Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else
        {
            // Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        /// Frame processing initialization
        mGray = new Mat(height, width, CvType.CV_8UC4);
        mRgba = new Mat(height, width, CvType.CV_8UC1);
        mIntermediateMat = new Mat();

        /// Imag processing routine initialization
        mSize0 = new Size();
        mChannels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
        mBuff = new float[mHistSizeNum];
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        mMat0 = new Mat();
        mColorsRGB = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
        mColorsHue = new Scalar[]{
                new Scalar(255, 0, 0, 255), new Scalar(255, 60, 0, 255), new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255), new Scalar(20, 255, 0, 255), new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255), new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255), new Scalar(0, 0, 255, 255), new Scalar(64, 0, 255, 255), new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255), new Scalar(255, 0, 0, 255)
        };
        mWhilte = Scalar.all(255);
        mP1 = new Point();
        mP2 = new Point();

        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);

        globalCenter = new Point(width / 2, height / 2);
        targetRadius = height / 2; // Note: Orientation is Landscape

        _trackedBox = null;
        isTakePicture = false;

        if (isShowTrackToast && viewMode == CameraBasedCheckActivity.VIEW_MODE_TRACK)
            showToast("Choose your 3-finger pad ROI...");
        else
            showToast("Click the screen to take a picture.");
    }

    public void onCameraViewStopped() {

        mGray.release();
        mRgba.release();

        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mIsCameraFrontFacing) {
        // Mirror (horizontally flip) the preview.
            Core.flip(mRgba, mRgba, 1);
            Core.flip(mGray, mGray, 1);
        }

        Size sizeRgba = mRgba.size();

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;


        switch (CameraBasedCheckActivity.viewMode) {

            case CameraBasedCheckActivity.VIEW_MODE_RGBA:
                break;

            case CameraBasedCheckActivity.VIEW_MODE_DETECT:

                if (mAbsoluteFaceSize == 0) {
                    if (Math.round(rows * mRelativeFaceSize) > 0) {
                        mAbsoluteFaceSize = Math.round(rows * mRelativeFaceSize);
                    }
                    mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
                }

                MatOfRect breasts = new MatOfRect();

                if (mNativeDetector != null)
                    mNativeDetector.detect(mGray, breasts);

                Rect[] facesArray = breasts.toArray();
                for (int i = 0; i < facesArray.length; i++)
                    Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

                mGray.release();
                break;


            case CameraBasedCheckActivity.VIEW_MODE_ZOOM:

                Mat tlCorner = mRgba.submat(3 * rows / 5, rows, 0, cols / 2 - cols / 10);
                Mat trCorner = mRgba.submat(3 * rows / 5, rows, cols - (cols / 2 - cols / 10), cols);

                Mat mZoomWindow = mRgba.submat(rows / 2 - 9 * rows / 100, rows / 2 + 9 * rows / 100, cols / 2 - 9 * cols / 100, cols / 2 + 9 * cols / 100);
                Mat mGrayZoomWindow = mGray.submat(rows / 2 - 9 * rows / 100, rows / 2 + 9 * rows / 100, cols / 2 - 9 * cols / 100, cols / 2 + 9 * cols / 100);

                Imgproc.resize(mZoomWindow, tlCorner, tlCorner.size());

                //apply Sobel
                Imgproc.Sobel(mGrayZoomWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);

                Mat temp = new Mat();
                Imgproc.cvtColor(mIntermediateMat, temp, Imgproc.COLOR_GRAY2BGRA, 4);
                Imgproc.resize(temp, trCorner, trCorner.size());
                temp.release();

                Size wsize = mZoomWindow.size();
                Imgproc.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(0, 255, 0, 255), 2);


                tlCorner.release();
                trCorner.release();
                mZoomWindow.release();
                mGrayZoomWindow.release();

                break;

            case CameraBasedCheckActivity.VIEW_MODE_HIST:
                Mat hist = new Mat();
                int thikness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
                if (thikness > 5) thikness = 5;
                int offset = (int) ((sizeRgba.width - (5 * mHistSizeNum + 4 * 10) * thikness) / 2);
                // RGB
                for (int c = 0; c < 3; c++) {
                    Imgproc.calcHist(Arrays.asList(mRgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
                    Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
                    hist.get(0, 0, mBuff);
                    for (int h = 0; h < mHistSizeNum; h++) {
                        mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
                        mP1.y = sizeRgba.height - 1;
                        mP2.y = mP1.y - 2 - (int) mBuff[h];
                        Imgproc.line(mRgba, mP1, mP2, mColorsRGB[c], thikness);
                    }
                }
                // Value and Hue
                Imgproc.cvtColor(mRgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);

                // Value
                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for (int h = 0; h < mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = sizeRgba.height - 1;
                    mP2.y = mP1.y - 2 - (int) mBuff[h];
                    Imgproc.line(mRgba, mP1, mP2, mWhilte, thikness);
                }
                // Hue
                Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for (int h = 0; h < mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) + h) * thikness;
                    mP1.y = sizeRgba.height - 1;
                    mP2.y = mP1.y - 2 - (int) mBuff[h];
                    Imgproc.line(mRgba, mP1, mP2, mColorsHue[h], thikness);
                }
                break;

            case CameraBasedCheckActivity.VIEW_MODE_TRACK:

                mIntermediateMat = Reduce(inputFrame.gray());

                /// Initialize the frame width and height
                int w = mIntermediateMat.width();
                int h = mIntermediateMat.height();

                /// If trackedBox is already initialized, that is, the user had chosen her hand
                if (_trackedBox != null) {

                    // Log.i("TAG","!cmtInitialised : " + (!cmtInitialised) );

                    if (!cmtInitialised) { // if CMT is not yet initialised.

                       /* Log.i(TAG, "START DEFINED: " + _trackedBox.x / 2 + " "
                                + _trackedBox.y / 2 + " " + _trackedBox.width / 2 + " "
                                + _trackedBox.height / 2); */

                        /// x and y scaling
                        double px = (w) / (double) (mOpenCvCameraView.getWidth());
                        double py = (h) / (double) (mOpenCvCameraView.getHeight());

                        /// initialize CMT tracker
                        initCMT(mIntermediateMat.getNativeObjAddr(), mRgba.getNativeObjAddr(),
                                (long) (_trackedBox.x * px),
                                (long) (_trackedBox.y * py),
                                (long) (_trackedBox.width * px),
                                (long) (_trackedBox.height * py));

                        cmtInitialised = true; // Has been initialized

                    } else { // If CMT tracker has been initialised

                        ProcessCMT(mIntermediateMat.getNativeObjAddr(), mRgba.getNativeObjAddr());

                    }
                }
                /// Display target circle with center point and outline
                Imgproc.circle(mRgba, globalCenter, 5, new Scalar(255, 255, 255), -1, 8, 0);
                Imgproc.circle(mRgba, globalCenter, targetRadius, new Scalar(255, 255, 255), 5, 8, 0);
                break;

            case CameraBasedCheckActivity.VIEW_MODE_CANNY:
                Imgproc.Canny(mRgba, mIntermediateMat, 80, 90);
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
                break;

            case CameraBasedCheckActivity.VIEW_MODE_SOBEL:
                Imgproc.Sobel(mGray, mIntermediateMat, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
                mGray.release();
                break;

            case CameraBasedCheckActivity.VIEW_MODE_SEPIA:
                Core.transform(mRgba, mRgba, mSepiaKernel);
                break;

            case CameraBasedCheckActivity.VIEW_MODE_PIXELIZE:
                Imgproc.resize(mRgba, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
                Imgproc.resize(mIntermediateMat, mRgba, mRgba.size(), 0., 0., Imgproc.INTER_NEAREST);
                break;

            case CameraBasedCheckActivity.VIEW_MODE_POSTERIZE:
                Imgproc.Canny(mRgba, mIntermediateMat, 80, 90);
                mRgba.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
                Core.convertScaleAbs(mRgba, mIntermediateMat, 1. / 16, 0);
                Core.convertScaleAbs(mIntermediateMat, mRgba, 16, 0);
                break;

            case CameraBasedCheckActivity.VIEW_MODE_CONTOUR:
                Contour(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(), mIntermediateMat.getNativeObjAddr());
                break;

            case CameraBasedCheckActivity.VIEW_MODE_TEXTURE:
                Texture(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(), mIntermediateMat.getNativeObjAddr());
                break;

            case CameraBasedCheckActivity.VIEW_MODE_THRESHOLD:
                Threshold(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
                break;

        } // END SWITCH

        if(isTakePicture)
            takePhoto(mRgba);

        return mRgba;
    }



    private void takePhoto(Mat rgba) {

        /// reset take photo boolean
        isTakePicture = false;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        String currentDateandTime = sdf.format(new Date());

        String fileName = currentDateandTime + PhotoActivity.PHOTO_FILE_EXTENSION;

        photo = null;

        try {
            photo = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgba, photo);
        } catch (CvException e) {
            //Log.d(TAG, e.getMessage());
        }


        FileOutputStream out = null;

        final String appName = getString(R.string.app_name);

        final String albumPath = Environment.getExternalStorageDirectory() + File.separator + appName;

        final String photoPath = albumPath + File.separator + fileName;


        File sd = new File(albumPath);

        boolean success = true;

        if (!sd.exists()) {
            success = sd.mkdir();
        }

        if (success) {
            File dest = new File(sd, fileName);

            try {
                out = new FileOutputStream(dest);
                photo.compress(Bitmap.CompressFormat.PNG, 1, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored (changed to 0)

            } catch (Exception e) {
                // e.printStackTrace();
                //Log.d(TAG, e.getMessage());
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        //Log.d(TAG, "OK!!");
                    }
                } catch (IOException e) {
                    //Log.d(TAG, e.getMessage() + "Error");
                    // e.printStackTrace();
                }
            }

            // Recycle bitmap
            if (photo != null)
                photo.recycle();

            photo = null;


            // Save photo information
            final ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, photoPath);
            values.put(MediaStore.Images.Media.MIME_TYPE, PhotoActivity.PHOTO_MIME_TYPE);
            values.put(MediaStore.Images.Media.TITLE, appName);
            values.put(MediaStore.Images.Media.DESCRIPTION, appName);
            values.put(MediaStore.Images.Media.DATE_TAKEN, currentDateandTime);


            // Try to insert the photo into the MediaStore.
            Uri uri = null;

            try {
                uri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (final Exception e) {
                //Log.e(TAG, "Failed to insert photo into MediaStore");
                e.printStackTrace();
            // Since the insertion failed, delete the photo.
                File fphoto = new File(photoPath);
                if (!fphoto.delete()) {
                    //Log.e(TAG, "Failed to delete non-inserted photo");
                }
            }


            // Open the photo in LabActivity.
            final Intent intent = new Intent(this, PhotoActivity.class);
            intent.putExtra(PhotoActivity.EXTRA_PHOTO_URI, uri);
            intent.putExtra(PhotoActivity.EXTRA_PHOTO_DATA_PATH, photoPath);
            startActivity(intent);
        }
    }

    /**
     * Helper function to reduce the size of the image
     */
    private Mat Reduce(Mat m) {
        Mat dst =  new Mat();
        Imgproc.resize(m, dst, new org.opencv.core.Size(WIDTH, HEIGHT));
        return dst;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.i(TAG, "called onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_camera, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        long action = item.getItemId();

        if (action == R.id.action_rgba) {
            viewMode = VIEW_MODE_RGBA;
        }
        else if (action == R.id.action_detect) {
            viewMode = VIEW_MODE_DETECT;
        }
        else if (action == R.id.action_histogram) {
            viewMode = VIEW_MODE_HIST;
        }
        else if (action == R.id.action_canny) {
            viewMode = VIEW_MODE_CANNY;
        }
        else if (action == R.id.action_sepia) {
            viewMode = VIEW_MODE_SEPIA;
         }
        else if (action == R.id.action_sobel) {
            viewMode = VIEW_MODE_SOBEL;
           }
        else if (action == R.id.action_zoom) {
            viewMode = VIEW_MODE_ZOOM;
            }
        else if (action == R.id.action_track) {
            viewMode = VIEW_MODE_TRACK;
             }
        else if (action == R.id.action_contour) {
            viewMode = VIEW_MODE_CONTOUR;
        }
        else if (action == R.id.action_texture) {
            viewMode = VIEW_MODE_TEXTURE;
        }
        else if (action == R.id.action_threshold) {
            viewMode = VIEW_MODE_THRESHOLD;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (CameraBasedCheckActivity.viewMode == CameraBasedCheckActivity.VIEW_MODE_TRACK ) {

            final Point corner = new Point(
                    event.getX() - _canvasImgXOffset, event.getY()
                    - _canvasImgYOffset);
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    trackedBox1stCorner.set(corner);
                    //Log.i("TAG", "1st corner: " + corner);
                    break;

                case MotionEvent.ACTION_UP:
                    _trackedBox = new Rect(trackedBox1stCorner.get(), corner);
                    if (_trackedBox.area() > 100) {
                       // Log.i("TAG", "Tracked box DEFINED: " + _trackedBox);
                        cmtInitialised = false;
                        isShowTrackToast = false;
                    } else {
                        _trackedBox = null;
                        isShowTrackToast  = true;
                    }
                    break;


                case MotionEvent.ACTION_MOVE:
                    final android.graphics.Rect rect = new android.graphics.Rect(
                            (int) trackedBox1stCorner.get().x
                                    + _canvasImgXOffset,
                            (int) trackedBox1stCorner.get().y
                                    + _canvasImgYOffset, (int) corner.x
                            + _canvasImgXOffset, (int) corner.y
                            + _canvasImgYOffset);
                    final Canvas canvas = _holder.lockCanvas(rect);
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    canvas.drawRect(rect, rectPaint);
                    _holder.unlockCanvasAndPost(canvas);

                    break;
            }

            return true; //  all follow up calls of this touch event like
                         // ACTION_MOVE or ACTION_UP will be delivered
        }


        else { // Take a picture

            isTakePicture = true;
            return false; // return false to get the initial events and not the follow up events
        }
    }



    private void showToast(String message) {

        // Inflate the Layout
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.mytoast,
                (ViewGroup) findViewById(R.id.custom_toast_layout));

        // Retrieve the ImageView and TextView
        ImageView iv = (ImageView) layout.findViewById(R.id.toastImageView);
        TextView text = (TextView) layout.findViewById(R.id.textToShow);

        // Set the image
        iv.setImageResource(R.mipmap.ic_video);

        // Set the Text to show in TextView
        text.setText(message);
        text.setBackgroundColor(Color.BLACK);

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public void onClickReverseCamera(View v){
        mCameraIndex++;
        if (mCameraIndex == mNumCameras) {
            mCameraIndex = 0;
        }
        recreate();
    }

    public native void initCMT(long matAddrGr, long matAddrRgba, long x,
                               long y, long w, long h);

    public native void ProcessCMT(long matAddrGr, long matAddrRgba);

    // Detect the Image Contours
    public native void Contour(long matAddrGr, long matAddrRgba, long matAddrIntermediate);

    // Detect the Image Texture using LBP (Local Binary Pattern)
    public native void Texture(long matAddrGr, long matAddrRgba, long matAddrIntermediate);

    // Threshold image with Otsu Thresholding
    public native void Threshold(long matAddrGr, long matAddrRgba);

}
