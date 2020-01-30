package edu.cnm.deepdive.nasaapod.controller;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import edu.cnm.deepdive.android.DateTimePickerFragment;
import edu.cnm.deepdive.android.DateTimePickerFragment.Mode;
import edu.cnm.deepdive.android.DateTimePickerFragment.OnChangeListener;
import edu.cnm.deepdive.nasaapod.R;
import edu.cnm.deepdive.nasaapod.model.Apod;
import edu.cnm.deepdive.nasaapod.viewmodel.MainViewModel;
import java.util.Calendar;

public class ImageFragment extends Fragment {

  private WebView contentView;
  private MainViewModel viewModel;
  private ProgressBar loading;
  private FloatingActionButton calendar;
  private Apod apod;

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
          this.apod = apod;
        });
    viewModel.getThrowable().observe(getViewLifecycleOwner(), (throwable -> {
      loading.setVisibility(View.GONE);
      Toast toast = Toast.makeText(getActivity(),
          getString(R.string.error_message, throwable.getMessage()), Toast.LENGTH_LONG);
      toast.setGravity(Gravity.BOTTOM, 0,
          (int) getResources().getDimension(R.dimen.toast__vertical_margin));
      toast.show();
    }));
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
        Toast toast = Toast.makeText(getActivity(), apod.getTitle(), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0,
            (int) getContext().getResources().getDimension(R.dimen.toast__vertical_margin));
        toast.show();
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
    this.calendar.setOnClickListener((v) -> { // when the button gets clicked ... *
      DateTimePickerFragment fragment = new DateTimePickerFragment(); // get ready to pick a date ...
      fragment.setCalendar(calendar);
      fragment.setMode(Mode.DATE);
      // when a date is picked ...
      fragment.setOnChangeListener((cal) -> {
        loading.setVisibility(View.VISIBLE); // show the loading icon ...
        viewModel.setApodDate(cal.getTime()); // figure out the date (and start the Retriever) ...
      });
      fragment.show(getChildFragmentManager(),
          fragment.getClass().getName()); // * show the calendar
    });
  }

}