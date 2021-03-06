package com.honchipay.honchi_android.profile.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.honchipay.honchi_android.R;
import com.honchipay.honchi_android.databinding.FragmentEditProfileBinding;
import com.honchipay.honchi_android.profile.viewmodel.EditProfileViewModel;
import com.honchipay.honchi_android.util.SharedPreferencesManager;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EditProfileFragment extends Fragment {
    private final int REQUEST_IMAGE = 333;
    FragmentEditProfileBinding binding;
    EditProfileViewModel editProfileViewModel;
    public String image;
    File file = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        image = requireArguments().getString("profileBundle");
        if (image == null) {
            Glide.with(requireContext()).asBitmap().load(R.drawable.default_profile).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    try {
                        file = new File(requireContext().getCacheDir(), SharedPreferencesManager.getInstance().getUserName());
                        OutputStream outStream = new BufferedOutputStream(new FileOutputStream(file));
                        resource.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) { }
            });
        } else {
            Glide.with(requireContext()).asBitmap().load(image).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    try {
                        OutputStream outStream = new BufferedOutputStream(new FileOutputStream(file));
                        resource.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) { }
            });
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editProfileViewModel = new ViewModelProvider(requireActivity()).get(EditProfileViewModel.class);
        binding.setEditProfileViewModel(editProfileViewModel);
        binding.setEditProfileFragment(this);
        binding.setLifecycleOwner(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EditPrivateInfoActivity activity = (EditPrivateInfoActivity) requireActivity();

        binding.editProfileUserImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요"), REQUEST_IMAGE);
        });

        binding.editProfileDoChangeButton.setOnClickListener(v -> editProfileViewModel.uploadUserNewInfo(file));
        editProfileViewModel.changeSuccess.observe(getViewLifecycleOwner(), success -> {
            if (success) {
                activity.finish();
            } else {
                Toast.makeText(getContext(), "프로필을 수정하는데 실패하였습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE) {
            Uri uri = data.getData();
            @SuppressLint("Recycle")
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null );
            cursor.moveToNext();
            String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
            file = new File(path);
            cursor.close();

            Glide.with(this).load(uri).circleCrop().into(binding.editProfileUserImageView);
        }
    }
}