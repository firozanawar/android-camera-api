/*
 * Copyright (C) 2018 Oleg Shnaydman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.arbuz.cameraformats;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] activitiesTitle = getResources().getStringArray(R.array.activities_title);
        String[] activitiesDescription = getResources().getStringArray(R.array.activities_description);
        String[] activities = getResources().getStringArray(R.array.activities);

        RecyclerView recyclerView = findViewById(R.id.listActivity);

        ActivityAdapter adapter = new ActivityAdapter(activitiesTitle, activitiesDescription);
        adapter.setOnClickListener(position -> startActivity(
                new Intent()
                        .setClassName(getApplicationContext(), getPackageName() + activities[position])));

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        checkPermissions();
    }

    void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
            }
        }
    }

    private static class ActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        String[] mTitleArray;
        String[] mSubtitleArray;

        OnItemClickListener mOnClickListener;

        interface OnItemClickListener {
            void onItemClicked(int position);
        }

        ActivityAdapter(String[] titleArray, String[] subtitleArray) {
            mTitleArray = titleArray;
            mSubtitleArray = subtitleArray;

            mOnClickListener = pos -> {
            };
        }

        void setOnClickListener(OnItemClickListener onClickListener) {
            mOnClickListener = onClickListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_activity, parent, false);
            return new ViewHolder(rowView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            ViewHolder tmpHolder = (ViewHolder) holder;
            tmpHolder.txtTitle.setText(mTitleArray[position]);
            tmpHolder.txtSubtitle.setText(mSubtitleArray[position]);

            tmpHolder.itemView.setOnClickListener(v -> mOnClickListener.onItemClicked(position));
        }

        @Override
        public int getItemCount() {
            return mTitleArray == null ? 0 : mTitleArray.length;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View     layout;
            TextView txtTitle;
            TextView txtSubtitle;

            ViewHolder(View v) {
                super(v);
                layout = v.findViewById(R.id.lytRow);
                txtTitle = v.findViewById(R.id.txtTitle);
                txtSubtitle = v.findViewById(R.id.txtSubtitle);
            }
        }
    }
}
