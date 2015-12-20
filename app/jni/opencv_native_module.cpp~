#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

#include "CMT.h"




extern "C" {


/** CMT Initialize and process frame */
JNIEXPORT void JNICALL Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_initCMT(JNIEnv*, jobject, jlong addrGray, jlong addrRgba,jlong x, jlong y, jlong width, jlong height);
JNIEXPORT void JNICALL Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_ProcessCMT(JNIEnv *env, jobject instance, jlong addrGray, jlong addrRgba);

/** Display Contours; Note: Java version is painfully slow but Native is awesome */ 
JNIEXPORT void JNICALL Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_Contour(JNIEnv *env, jobject instance, jlong addrGray, jlong addrRgba, jlong addrIntermediateMat);

/** Texture - LBP (Local Binary Pattern) */
JNIEXPORT void JNICALL
Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_Texture(JNIEnv *env, jobject instance, jlong addrGray, jlong addrRgba, jlong addrIntermediateMat);

/** Threshold with Otsu */
JNIEXPORT void JNICALL
Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_Threshold(JNIEnv *env, jobject instance, jlong addrGray, jlong addrRgba);


// Global variables
bool CMTinitiated = false;
CMT  *cmt = new CMT();
 


JNIEXPORT void JNICALL
Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_initCMT(JNIEnv*, jobject, jlong addrGray, jlong addrRgba,jlong x, jlong y, jlong width, jlong height) {

    cv::Mat& im_gray  = *(cv::Mat*)addrGray;
    cv::Point tl(x,y);                    // TopLeft
    cv::Point br(x+width,y+height);       // BottomRight

    CMTinitiated = false;
    
    if (cmt->initialise(im_gray, tl, br))
        CMTinitiated = true;

}



JNIEXPORT void JNICALL
Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_ProcessCMT(JNIEnv *env, jobject instance, jlong addrGray, jlong addrRgba) {

    if (!CMTinitiated)
        return;

    cv::Mat &im_rgba = *(cv::Mat *) addrRgba;
    cv::Mat &im_gray = *(cv::Mat *) addrGray;

    cmt->processFrame(im_gray);

    /// Compute the x and y scale factor
    float px = (float) im_rgba.cols / (float) im_gray.cols;
    float py = (float) im_rgba.rows / (float) im_gray.rows;


    for(size_t i = 0; i < (int)cmt->trackedKeypoints.size(); i++){
        const cv::KeyPoint &kp = cmt->trackedKeypoints[i].first;
        cv::circle(im_rgba, cv::Point(kp.pt.x * px, kp.pt.y * py), 10, cv::Scalar(0, 255, 0));
    }

    cv::Point _topLeft = cv::Point(cmt->topLeft.x * px, cmt->topLeft.y * py);
    cv::Point _topRight = cv::Point(cmt->topRight.x * px, cmt->topRight.y * py);
    cv::Point _bottomLeft = cv::Point(cmt->bottomLeft.x * px, cmt->bottomLeft.y * py);
    cv::Point _bottomRight = cv::Point(cmt->bottomRight.x * px, cmt->bottomRight.y * py);

    cv::line(im_rgba, _topLeft, _topRight, cv::Scalar(255,0,0));
    cv::line(im_rgba, _topRight, _bottomRight, cv::Scalar(255,0,0));
    cv::line(im_rgba, _bottomRight, _bottomLeft, cv::Scalar(255,0,0));
    cv::line(im_rgba, _bottomLeft, _topLeft, cv::Scalar(255,0,0));


    // Plot the scale graph
    int alpha = 50;
    int baseline = im_rgba.rows - 48;
    cv::line(im_rgba, cv::Point(0, baseline), cv::Point(im_rgba.cols, baseline), cv::Scalar(255,255,255));
    for(size_t i = cmt->pseudoDepth.size() - 1; i > 1 && ((cmt->pseudoDepth.size() - i) < im_rgba.cols); --i){
        cv::line(im_rgba, cv::Point(i % im_rgba.cols, baseline - alpha * (cmt->pseudoDepth[i] - 1)), cv::Point(i % im_rgba.cols, baseline - alpha * (cmt->pseudoDepth[i] - 1)), cv::Scalar(255,0,0), 4);
     }

     
    // Display trajectory ; limit trajectory to at most 20 recent pts

    for(size_t i = cmt->trajectory.size() - 1; i > 1 && ((cmt->trajectory.size() - i) < 20); --i){
        const cv::Point2f &head = cmt->trajectory[i];
        const cv::Point2f &tail = cmt->trajectory[i-1];
        cv::line(im_rgba, cv::Point(tail.x * px, tail.y * py), cv::Point(head.x * px, head.y * py), cv::Scalar(255,105,180), 5);
    }
      
}


// Completely unrelated to CMT functions

/** Contours */

JNIEXPORT void JNICALL
Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_Contour(JNIEnv *env, jobject instance, jlong addrGray, jlong addrRgba, jlong addrIntermediateMat) {

    cv::Mat &im_rgba = *(cv::Mat *) addrRgba;
    cv::Mat &im_gray = *(cv::Mat *) addrGray;
    cv::Mat &im_intermediate = *(cv::Mat *) addrIntermediateMat;
    
    // Initialize variables
    std::vector<std::vector<cv::Point> > contours;
    std::vector<cv::Vec4i> hierarchy;

	cv::RNG rng(12345); // for random color
    cv::Scalar color;

  	// Detect the edges
    cv::Canny(im_gray, im_intermediate, 50, 100);  // im_intermediate - edges

    // Find contours
    cv::findContours(im_intermediate, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_NONE);

    // Draw contours
    for( size_t i = 0; i < contours.size(); i++ )
    {
        if(contours[i].size() > 50){
        	// Randomize color
            color = cv::Scalar( rng.uniform(0, 255), rng.uniform(0,255), rng.uniform(0,255), 255);
            cv::drawContours( im_rgba, contours, i, color, 2, 8, hierarchy, 0, cv::Point() );
        }
    }

}


/** Texture - LBP (Local Binary Pattern) */

JNIEXPORT void JNICALL
Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_Texture(JNIEnv *env, jobject instance, jlong addrGray, jlong addrRgba, jlong addrIntermediateMat) {

    cv::Mat &im_rgba = *(cv::Mat *) addrRgba;
    cv::Mat &im_gray = *(cv::Mat *) addrGray;
    cv::Mat &im_intermediate = *(cv::Mat *) addrIntermediateMat;
    
    im_intermediate = cv::Mat::zeros(im_gray.size(), im_gray.type());
    
     for(size_t j=1; j<im_gray.rows-1; j++) {
    
        const uchar* previous = im_gray.ptr<const uchar>(j-1); // previous row
        const uchar* current  = im_gray.ptr<const uchar>(j);   // current row 
        const uchar* next     = im_gray.ptr<const uchar>(j+1); // next row
        
        uchar* output          = im_intermediate.ptr<uchar>(j); // output row       
        
        for(size_t i = 1;i < im_gray.cols-1; i++) {
            
            const uchar center = im_gray.ptr<const uchar>(j)[i];
            
            uchar code = 0;

            code |= ((previous[i-1]) > center) << 7;
            code |= ((previous[i])   > center) << 6;
            code |= ((previous[i+1]) > center) << 5;
            code |= ((current[i+1])  > center) << 4;
            code |= ((next[i+1])     > center) << 3;
            code |= ((next[i])       > center) << 2;
            code |= ((next[i-1])     > center) << 1;
            code |= ((current[i-1])  > center) << 0;
            
            *output++ = code;
        }
    }
    
    cv::cvtColor(im_intermediate, im_rgba, CV_GRAY2BGRA);
}


/** Threshold with Otsu */

JNIEXPORT void JNICALL
Java_com_cabatuan_breastfriend_CameraBasedCheckActivity_Threshold(JNIEnv *env, jobject instance, jlong addrGray, jlong addrRgba) {

    cv::Mat &im_rgba = *(cv::Mat *) addrRgba;
    cv::Mat &im_gray = *(cv::Mat *) addrGray;
    
    cv::threshold( im_gray, im_gray, 0, 255, cv::THRESH_BINARY | cv::THRESH_OTSU );
	cv::cvtColor(im_gray, im_rgba, CV_GRAY2BGRA);

}


}
