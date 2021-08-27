package com.railprosfs.railsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import com.railprosfs.railsapp.databinding.AboutBinding;
import com.railprosfs.railsapp.data.observable.AboutModel;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AboutBinding binding = DataBindingUtil.setContentView(this, R.layout.about);
        binding.setAbout(new AboutModel(this));
    }
}
