package xmicha65.bp_app.controller.hdr;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.List;

import xmicha65.bp_app.model.CameraCRF;
import xmicha65.bp_app.model.ImageHDR;
import xmicha65.bp_app.model.ImageLDR;

/**
 * HDR algorithm main controller
 *
 * @author xmicha65
 */
public class HDRController {
    private double lambda = 50;     // smoothness scaling factor
    private double[] weights;       // weighting function
    private double[] lnT;           // log delta t for image j (B(j))

    private List<ImageLDR> capturedImages;
    private int numPixels;          // num of pixels (i)
    private int numExposures;       // num of exposures (j)

    private ImageHDR hdrImage;

    public HDRController(List<ImageLDR> captImages, boolean opencv) {
        this.capturedImages = captImages;

        if (opencv) opencvHDR();
        else buildHDR();
    }

    /**
     * Merge captured images with OpenCV
     */
    private void opencvHDR() {
        Mat matHdrImage = new Mat();
        Mat matCrf = new Mat();

        int numImages = capturedImages.size();

        // init float exposure times
        float[] expTimes = new float[numImages];
        for (int i = 0; i < numImages; i++) {
            expTimes[i] = (float) capturedImages.get(i).getExposureTime();
        }
        Mat mExpTimes = new MatOfFloat(expTimes);

        // init List of image Mat
        List<Mat> matImages = new ArrayList<>();
        for (int i = 0; i < numImages; i++) {
            matImages.add(capturedImages.get(i).getMatImg());
        }

        Photo.createCalibrateDebevec().process(matImages, matCrf, mExpTimes);
        Photo.createMergeDebevec().process(matImages, matHdrImage, mExpTimes, matCrf);

        this.hdrImage = new ImageHDR(matHdrImage);
    }

    /**
     * Main controller for manual merging images
     * according of P. Debevec article
     */
    private void buildHDR() {
        this.numPixels = capturedImages.get(0).getLength();
        this.numExposures = capturedImages.size();

        initWeights();
        initLnT();

        // select samples for algorithm (Zij)
        SamplesSelector samples = new SamplesSelector(capturedImages, numPixels, numExposures);

        // recover device's CRF
        CRFRecover crfRecover = new CRFRecover(lambda, weights, lnT, samples);
        CameraCRF cameraCRF = crfRecover.getCameraCRF();

        // merge exposures into HDR
        HDRMerge hdrMerge = new HDRMerge(cameraCRF, weights, lnT, numPixels, numExposures, capturedImages);

        this.hdrImage = hdrMerge.getHdrImage();
    }

    /**
     * Weighting function
     * source: P. Debevec
     */
    private double w(int z) {
        int zmin = 0;
        int zmax = 255;
        return z <= (zmin + zmax) / 2 ? (z - zmin) + 1 : (zmax - z) + 1;
    }

    /**
     * Create weight function as array[256]
     */
    private void initWeights() {
        this.weights = new double[256];
        for (int i = 0; i < this.weights.length; i++) {
            this.weights[i] = w(i);
        }
    }

    /**
     * Create array[numExposures] of log exposure time
     */
    private void initLnT() {
        this.lnT = new double[this.numExposures];
        for (int i = 0; i < this.numExposures; i++) {
            this.lnT[i] = Math.log(capturedImages.get(i).getExposureTime());
        }
    }

    /**
     * GETTERS
     */

    public ImageHDR getHdrImage() {
        return this.hdrImage;
    }
}
