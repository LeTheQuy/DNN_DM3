#include "com_gotit_quyle_dnn_dm3_NativeClass.h"
#include <opencv2/dnn.hpp>
#include <opencv2/dnn/blob.hpp>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
using namespace cv;
using namespace cv::dnn;

#include <fstream>
#include <iostream>
#include <cstdlib>
#define APPNAME "QUYDZ"

using namespace std;


int REGION_SIZE = 16;


/* Find best class for the blob (i. e. class with maximal probability) */
void getMaxClass(dnn::Blob &probBlob, int *classId, double *classProb)
{
    Mat probMat = probBlob.matRefConst().reshape(1, 1); //reshape the blob to 1x1000 matrix
    Point classNumber;

    minMaxLoc(probMat, NULL, classProb, NULL, &classNumber);
    *classId = classNumber.x;
}

std::vector<String> readClassNames(const char *filename = "/sdcard/android-opencv/synset_words.txt")
{
    std::vector<String> classNames;

    std::ifstream fp(filename);
    if (!fp.is_open())
    {
        std::cerr << "File with classes labels not found: " << filename << std::endl;
        exit(-1);
    }

    std::string name;
    while (!fp.eof())
    {
        std::getline(fp, name);
        if (name.length())
            classNames.push_back( name.substr(name.find(' ')+1) );
    }

    fp.close();
    return classNames;
}

std::vector<String> classNames;
Net net;

JNIEXPORT jstring JNICALL Java_com_gotit_quyle_dnn_1dm3_NativeClass_readNet(JNIEnv * env, jclass obj){
      classNames = readClassNames();
        String modelTxt = "/sdcard/android-opencv/mathpix.prototxt";
        String modelBin = "/sdcard/android-opencv/mathpix.caffemodel";
        net = dnn::readNetFromCaffe(modelTxt, modelBin);
        if (net.empty())
            {
             return env->NewStringUTF("Eroor");
            }

         return env->NewStringUTF("Succesful");
}



Mat getOutputImage(dnn::Blob &probBlob, Mat img)
{
    Mat probMat = probBlob.getPlane(0, 1); //reshape the blob to 1x1000 matrix
   // imwrite("bitmap.png", probMat * 255);
    int imin = probMat.rows;
    int imax = -1;
    int jmin = probMat.cols;
    int jmax = -1;
    for (int i = 0; i < probMat.rows; i++) {
        for (int j = 0; j < probMat.cols; j++) {
            if (probMat.at<float>(i,j) > 0.5) {
                imin = min(imin, i-1);
                imax = max(imax, i+1);
                jmin = min(jmin, j-1);
                jmax = max(jmax, j+1);
            }
        }
    }

    Point topLeft(jmin, imin);
    Point bottomRight(jmax, imax);
    int row = img.size().height;
    int col = img.size().width;
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%d %d %d %d --- wight %d -- height %d ",jmin,imin,jmax,imax,col,row);
    rectangle(img, REGION_SIZE * topLeft, REGION_SIZE * bottomRight, Scalar(255,0,0));
    return img;
}



JNIEXPORT jboolean JNICALL Java_com_gotit_quyle_dnn_1dm3_NativeClass_imageProcessing
(JNIEnv * env, jclass obj, jint width, jint height, jbyteArray NV21FrameData, jintArray outPixels){

   jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
   jint * poutPixels = env->GetIntArrayElements(outPixels, 0);

   Mat mGray(height, width, CV_8UC1, (unsigned char *)pNV21FrameData);
   Mat mResult(height, width, CV_8UC4, (unsigned char *)poutPixels);


  // cv::transpose(mGray, mGray);
  // cv::flip(mGray, mGray, 1);

   cvtColor(mGray,mResult, CV_GRAY2BGRA);

  // resize(mGray, mGray, Size(240, 80));
   // adaptiveThreshold(mGray, mGray, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV, 15, 5);
   // if (net.empty()){
    //       return false;
   // }
  // dnn::Blob inputBlob = dnn::Blob::fromImages(mGray);   //Convert Mat to dnn::Blob batch of images
  // net.setBlob(".image", inputBlob);        //set the network input
  // net.forward();                          //compute output
  // dnn::Blob prob = net.getBlob("conv_overfeat_softmax");

   float ratio = 20 / 100.f;
   cv::resize(mResult, mResult, cv::Size((int)(mResult.cols * ratio), (int)(mResult.rows * ratio)));   // Resize image


  //  mResult = getOutputImage(prob, mGray);

   env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
   env->ReleaseIntArrayElements(outPixels, poutPixels, 0);
   return true;
  }

JNIEXPORT jstring JNICALL Java_com_gotit_quyle_dnn_1dm3_NativeClass_getStringFromNative__Ljava_lang_String_2
        (JNIEnv * env, jclass obj,jstring javaString){

        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "start find bouding");
        String imageFile = "/data/data/com.gotit.quyle.dnn_dm3/cache/image.jpg";
        const char* ch = env->GetStringUTFChars(javaString, 0);
        __android_log_print(ANDROID_LOG_VERBOSE, APPNAME,"file name %s ",ch );

        if (net.empty()){
           return env->NewStringUTF("not load net");
        }
        if (ch == NULL ||ch == ""){
               ch = "/sdcard/android-opencv/image.jpg";
        }
        Mat img = imread(ch, 0);
      //  resize(img, img, Size(240, 80));
        adaptiveThreshold(img, img, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV, 15, 5);
            __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "time start Blob image");
           dnn::Blob inputBlob = dnn::Blob::fromImages(img);   //Convert Mat to dnn::Blob batch of images
           net.setBlob(".image", inputBlob);        //set the network input
           net.forward();                          //compute output
           dnn::Blob prob = net.getBlob("conv_overfeat_softmax");
           __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "time end Blob image ");
           Mat outputImage = getOutputImage(prob, img);
        imwrite("/sdcard/android-opencv/debug_image.jpg", outputImage);
        return env->NewStringUTF("Finish math pix");
}


JNIEXPORT jstring JNICALL Java_com_gotit_quyle_dnn_1dm3_NativeClass_getStringFromNative__
        (JNIEnv * env, jclass obj){

    String imageFile = "/sdcard/android-opencv/image.jpg";
         if (net.empty())
    if (net.empty()){
        exit(-1);
    }
    Mat img = imread(imageFile);
    if (img.empty())
    {
        std::cerr << "Can't read image from the file: " << imageFile << std::endl;
        exit(-1);
    }

    resize(img, img, Size(224, 224));       //GoogLeNet accepts only 224x224 RGB-images
    dnn::Blob inputBlob = dnn::Blob::fromImages(img);   //Convert Mat to dnn::Blob image batch

    net.setBlob(".data", inputBlob);        //set the network input

    net.forward();                          //compute output

    dnn::Blob prob = net.getBlob("prob");   //gather output of "prob" layer

    int classId;
    double classProb;
    getMaxClass(prob, &classId, &classProb);//find the best class

    std::stringstream sstr;
    sstr << classId;
    std::string str1 = sstr.str();

    std::cout << "Best class: #" << classId << " '" << classNames.at(classId) << "'" << std::endl;
    std::cout << "Probability: " << classProb * 100 << "%" << std::endl;

    return env->NewStringUTF(classNames.at(classId).c_str());

    return env->NewStringUTF("Hello from ");
}
