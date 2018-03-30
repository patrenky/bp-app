package xmicha65.bp_app.view;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import xmicha65.bp_app.Main;
import xmicha65.bp_app.R;
import xmicha65.bp_app.controller.tmo.TMOReinhardGlobal;
import xmicha65.bp_app.model.ImageHDR;
import xmicha65.bp_app.model.ImageType;
import xmicha65.bp_app.model.TmoParams;

/**
 * Screen: control params of Reinhard global TMO
 */
public class EditReinhardFragment extends Fragment implements View.OnClickListener {
    public static String ARG_HDR = "ARG_HDR";
    private ImageHDR hdrImage;
    private ImageView imageView;
    private TMOReinhardGlobal tonemapper;
    private int rotation = 0;

    // seekBars
    private SeekBar barGama;
    private SeekBar barIntensity;
    private SeekBar barLightAdapt;
    private SeekBar barColorAdapr;

    // actual values
    private float gamma;
    private float intensity;
    private float lightAdapt;
    private float colorAdapt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // restore arguments from main activity
        if (savedInstanceState != null) {
            hdrImage = (ImageHDR) savedInstanceState.getSerializable(ARG_HDR);
        }
        return inflater.inflate(R.layout.fragment_edit_reinhard, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // check if there are arguments passed to the fragment
        Bundle args = getArguments();
        if (args != null) {
            // arguments passed in
            hdrImage = (ImageHDR) args.getSerializable(ARG_HDR);
            displayResult();
        } else if (hdrImage != null) {
            // saved instance state defined during onCreateView
            displayResult();
        }
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        imageView = (ImageView) view.findViewById(R.id.reinhard_image);
        // buttons
        view.findViewById(R.id.reinhard_back).setOnClickListener(this);
        view.findViewById(R.id.reinhard_reset).setOnClickListener(this);
        view.findViewById(R.id.reinhard_rotate).setOnClickListener(this);
        view.findViewById(R.id.reinhard_save_hdr).setOnClickListener(this);
        view.findViewById(R.id.reinhard_save_jpg).setOnClickListener(this);
        // seekBars
        barGama = (SeekBar) view.findViewById(R.id.reinhard_bar0);
        barIntensity = (SeekBar) view.findViewById(R.id.reinhard_bar1);
        barLightAdapt = (SeekBar) view.findViewById(R.id.reinhard_bar2);
        barColorAdapr = (SeekBar) view.findViewById(R.id.reinhard_bar3);

        resetTmoValues();
        setSeekBarsListeners();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reinhard_back: {
                ((Main) getActivity()).goHome();
                break;
            }
            case R.id.reinhard_reset: {
                resetTmoValues();
                displayResult();
                break;
            }
            case R.id.reinhard_rotate: {
                rotateView();
                break;
            }
            case R.id.reinhard_save_hdr: {
                DialogFragment saveDialog = SaveDialog.newInstance(hdrImage, ImageType.HDR);
                saveDialog.show(getActivity().getFragmentManager(), "saveHdrDialog");
                break;
            }
            case R.id.reinhard_save_jpg: {
                // save original size result
                tonemapper = new TMOReinhardGlobal(hdrImage.getMatHdrImg(), gamma, intensity, lightAdapt, colorAdapt);
                DialogFragment saveDialog = SaveDialog.newInstance(
                        new ImageHDR(tonemapper.getImageBmp()), ImageType.LDR);
                saveDialog.show(getActivity().getFragmentManager(), "saveLdrDialog");
                break;
            }
        }
    }

    /**
     * Progress bars listeners
     */
    public void setSeekBarsListeners() {
        barGama.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                gamma = TmoParams.getProgressValue(TmoParams.gama, progress);
                displayResult();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("### range check: " + TmoParams.getProgressValue(TmoParams.gama, seekBar.getProgress()));
            }
        });
        barIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                intensity = TmoParams.getProgressValue(TmoParams.rIntensity, progress);
                displayResult();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("### range check: " + TmoParams.getProgressValue(TmoParams.rIntensity, seekBar.getProgress()));
            }
        });
        barLightAdapt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                lightAdapt = TmoParams.getProgressValue(TmoParams.rLightAdapt, progress);
                displayResult();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("### range check: " + TmoParams.getProgressValue(TmoParams.rLightAdapt, seekBar.getProgress()));
            }
        });
        barColorAdapr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                colorAdapt = TmoParams.getProgressValue(TmoParams.rColorAdapt, progress);
                displayResult();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("### range check: " + TmoParams.getProgressValue(TmoParams.rColorAdapt, seekBar.getProgress()));
            }
        });
    }

    /**
     * Rotate image view
     */
    private void rotateView() {
        rotation += 90;
        imageView.setRotation(rotation);
    }

    /**
     * Tonemap and diplay result
     */
    private void displayResult() {
        tonemapper = new TMOReinhardGlobal(hdrImage.getMatHdrTemp(), gamma, intensity, lightAdapt, colorAdapt);
        imageView.setImageBitmap(tonemapper.getImageBmp());
    }

    /**
     * Reset to default tmo params
     */
    private void resetTmoValues() {
        gamma = TmoParams.getDefaultValue(TmoParams.gama);
        intensity = TmoParams.getDefaultValue(TmoParams.rIntensity);
        lightAdapt = TmoParams.getDefaultValue(TmoParams.rLightAdapt);
        colorAdapt = TmoParams.getDefaultValue(TmoParams.rColorAdapt);

        barGama.setProgress(TmoParams.getDefaultProgressValue(TmoParams.gama));
        barIntensity.setProgress(TmoParams.getDefaultProgressValue(TmoParams.rIntensity));
        barLightAdapt.setProgress(TmoParams.getDefaultProgressValue(TmoParams.rLightAdapt));
        barColorAdapr.setProgress(TmoParams.getDefaultProgressValue(TmoParams.rColorAdapt));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current state in case we need to recreate the fragment
        outState.putSerializable(ARG_HDR, hdrImage);

    }
}