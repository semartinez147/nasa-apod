package edu.cnm.deepdive.nasaapod.controller;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.cnm.deepdive.android.DateTimePickerFragment;
import edu.cnm.deepdive.android.DateTimePickerFragment.Mode;
import edu.cnm.deepdive.android.DateTimePickerFragment.OnChangeListener;
import edu.cnm.deepdive.nasaapod.R;
import edu.cnm.deepdive.nasaapod.viewmodel.MainViewModel;
import java.util.Calendar;

public class ImageFragment extends Fragment {

  private static final String IMAGE_URL = "https://apod.nasa.gov/apod/image/2001/ic410_WISEantonucci_1824.jpg";

  private WebView contentView;
  private MainViewModel viewModel;
  private ProgressBar loading;
  private FloatingActionButton calendar;

  @Override
  @SuppressLint("SetJavaScriptEnabled")
  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_image, container, false);
    loading = root.findViewById(R.id.loading);
    calendar = root.findViewById(R.id.calendar);
    setupWebView(root);
    setupCalendarPicker(Calendar.getInstance());
    return root;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    viewModel.getApod().observe(getViewLifecycleOwner(),
        (apod) -> {
          contentView.loadUrl(apod.getUrl());
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(apod.getDate());
          setupCalendarPicker(calendar);
        });
  }

  @SuppressLint("SetJavaScriptEnabled")
  private void setupWebView(View root) {
    contentView = root.findViewById(R.id.content_view);
    contentView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        loading.setVisibility(View.GONE);
      }
    });
    WebSettings settings = contentView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setSupportZoom(true);
    settings.setBuiltInZoomControls(true);
    settings.setDisplayZoomControls(false);
    settings.setUseWideViewPort(true);
    settings.setLoadWithOverviewMode(true);
  }

  private void setupCalendarPicker(Calendar calendar) {
    this.calendar.setOnClickListener((v) -> { // when the button gets clicked ... (line 86)
      DateTimePickerFragment fragment = new DateTimePickerFragment(); // get ready to pick a date ...
      fragment.setCalendar(calendar);
      fragment.setMode(Mode.DATE);
      // when a date is picked ...
      fragment.setOnChangeListener((cal) -> {
        loading.setVisibility(View.VISIBLE); // show the loading icon ...
        viewModel.setApodDate(cal.getTime()); // figure out the date (and start the Retriever) ...
      });
      fragment.show(getChildFragmentManager(),
          fragment.getClass().getName()); // show the calendar (@ line 77) ...
    });
  }

}