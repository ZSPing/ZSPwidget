package com.zsp.zspwidget.page;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zsp.zspwidget.R;
import com.zsp.zspwidget.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    RecyclerView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mList = findViewById(R.id.main_list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mList.setLayoutManager(manager);
        ItemAdapter adapter = new ItemAdapter(getItemData());
        mList.setAdapter(adapter);

    }

    private List<ItemData> getItemData() {
        List<ItemData> data = new ArrayList<>();
        data.add(new ItemData("PagerTitle", PagerTitleActivity.class));
        return data;
    }

    private class ItemData {
        private String describe;
        private Class<? extends Activity> aClass;

        ItemData(String describe, Class<? extends Activity> aClass) {
            this.describe = describe;
            this.aClass = aClass;
        }

        String getDescribe() {
            return describe;
        }

        Class<? extends Activity> getAClass() {
            return aClass;
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
        private List<ItemData> data;

        ItemAdapter(List<ItemData> data) {
            this.data = data;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_main, parent, false));
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            holder.bindData(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView mItemText;
            ItemData data;

            ItemViewHolder(View itemView) {
                super(itemView);
                mItemText = itemView.findViewById(R.id.item_main_text);
                mItemText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (data != null) {
                            startActivity(new Intent(MainActivity.this, data.getAClass()));
                        }
                    }
                });
            }

            void bindData(ItemData data) {
                this.data = data;
                mItemText.setText(data.getDescribe());
            }
        }
    }
}
