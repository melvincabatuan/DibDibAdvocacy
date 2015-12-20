#ifndef CMT_H
#define CMT_H

#include <opencv2/opencv.hpp>
#include <opencv2/features2d/features2d.hpp>



class CMT
{

public:

	/** Keypoint detector, i.e. FAST or AKAZE */
    std::string detectorType;
    cv::Ptr<cv::FeatureDetector> detector;
    
    /** Keypoint descriptor, i.e. ORB, BRISK, FREAK, AKAZE, etc. */
    std::string descriptorType;
    cv::Ptr<cv::DescriptorExtractor> descriptorExtractor;
    
    /// Note: Most descriptors are also detectors, ex. AKAZE, while ORB and BRISK uses FAST detector
    
    /** Keypoint matcher, i.e. Bruteforce, Bruteforce-Hamming, etc. */
    std::string matcherType;
    cv::Ptr<cv::DescriptorMatcher> descriptorMatcher;
    
    int descriptorLength; // Ex. 512 for BRISK
    
    /** Outlier threshold, delta, controls the degree of allowed flexibility on the model 
    *   The larger the value. In hierarchical agglomerative clustering, data is organised 
    *   into hierarchical structures according to a proximity matrix, resulting in a dendrogram 
    *   that is then cut off at a certain threshold delta.
    *
    *	CMT authors experimented with different values of the cut-off threshold and found 
    *   delta = 20 to give good results.
    */
    
    int thrOutlier;    // Ex.  thrOutlier = 20
    
    float thrConf;     // Ex.  thrConf = 0.75 why ???    
    
    /**
    *	For matching candidate keypoints to the model, CMT authors follow D. G. Lowe and 
    *	set the ratio threshold.    
    */
    float thrRatio;

	/** Estimate scale parameter, s */
    bool estimateScale;
    
    /** Estimate rotation parameter, R */
    bool estimateRotation;
  
    
    /** Computed descriptors for the keypoints within the tracked bounding box */
    cv::Mat selectedFeatures;
    
    /** Tracked object descriptor classes, i.e. 1, 2, 3, .., selected_keypoints.size() */
    std::vector<int> selectedClasses;
    // Note: background class is 0.
    
    /** Contains the computed descriptors for both background and selected keypoints */
    cv::Mat featuresDatabase;
    
    /** Vector of integer labels for the background, i.e. 0 and 
    *   selected features , i.e. 1, 2, 3, .., selected_keypoints.size()
    */
    std::vector<int> classesDatabase;

	/** Pairwise Euclidean distance between selected keypoints 
	*   to be used for estimating the scale parameter
	*/
    std::vector<std::vector<float> > squareForm;
    
    /** Corresponding pairwise angles between selected keypoints
    * 	to be used for estimating the angle parameter
    */
    std::vector<std::vector<float> > angles;

	/** ROI rectangle corners and boundingbox */
    cv::Point2f topLeft;
    cv::Point2f topRight;
    cv::Point2f bottomRight;
    cv::Point2f bottomLeft;

    cv::Rect_<float> boundingbox;
    
    /** Is the result available? */
    bool hasResult;

	/** Relative distances from center to four corners of the bounding box */
    cv::Point2f centerToTopLeft;
    cv::Point2f centerToTopRight;
    cv::Point2f centerToBottomRight;
    cv::Point2f centerToBottomLeft;

	/** dx, dy distances from each selected keypoints relative to the center */
    std::vector<cv::Point2f> springs;  

    cv::Mat im_prev;
    std::vector<std::pair<cv::KeyPoint,int> > activeKeypoints;
    std::vector<std::pair<cv::KeyPoint,int> > trackedKeypoints;

	/** Size or number of initial keypoints
	*   equals selected_keypoints.size()
	*/
    unsigned int nbInitialKeypoints;

	/** Center votes */
    std::vector<cv::Point2f> votes;
    
    /** Centers trajectory - mkc */
    std::vector<cv::Point2f> trajectory;
    
    /** Scale estimates o2 pseudoDepth- mkc */
    std::vector<float> pseudoDepth;

	/** Outlier keypoints with their index */
    std::vector<std::pair<cv::KeyPoint, int> > outliers;

	/** Default constructor */
    CMT();
    
    /** Initialise CMT with the ROI to be tracked */
    bool initialise(cv::Mat im_gray0, cv::Point2f topleft, cv::Point2f bottomright);
    
    /** Estimates the scale, rotation, translation, and the new center of the tracked object
    * It iterates through the keypoints and computes their votes for the new center
    * Then, hierarchical agglomerative clustering is performed to come up with a consensus 
    * center cluster containing the most votes.
    * Finally, using the keypoints that belong to the consensus, the new center is computed.
    */
    void estimate(const std::vector<std::pair<cv::KeyPoint, int> >& keypointsIN, cv::Point2f& center, float& scaleEstimate, float& medRot, std::vector<std::pair<cv::KeyPoint, int> >& keypoints);
    
    
    /** Process the frame */
    void processFrame(cv::Mat im_gray);


    bool Save(const char *path);
    
    bool Load(const char *path);
};



/** VOTE cluster*/
class Cluster
{
public:
    int first, second;//cluster id
    float dist;
    int num;
};


/** Segregate keypoints into inside roi points (in) or foreground; 
*	and outside roi points (out) or background 
*/
void inout_rect(const std::vector<cv::KeyPoint>& keypoints, cv::Point2f topleft, cv::Point2f bottomright, std::vector<cv::KeyPoint>& in, std::vector<cv::KeyPoint>& out);


/** Track keypoint from previous frame (im_prev) to current (im_gray) 
* 	using forward-backward optical flow
*/
void track(cv::Mat im_prev, cv::Mat im_gray, const std::vector<std::pair<cv::KeyPoint, int> >& keypointsIN, std::vector<std::pair<cv::KeyPoint, int> >& keypointsTracked, std::vector<unsigned char>& status, int THR_FB = 20); // THR_FB - delta parameter


/** Rotates a point by angle rad by applying the rotation matrix, R,
*	cos(alpha)   -sin(alpha)
*	sin(alpha)	  cos(alpha)
*
*/
cv::Point2f rotate(cv::Point2f p, float rad);

#endif // CMT_H
