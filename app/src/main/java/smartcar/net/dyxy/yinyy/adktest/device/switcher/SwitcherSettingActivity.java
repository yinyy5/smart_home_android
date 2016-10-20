package smartcar.net.dyxy.yinyy.adktest.device.switcher;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import smartcar.net.dyxy.yinyy.adktest.AccessoryContact;
import smartcar.net.dyxy.yinyy.adktest.R;

public class SwitcherSettingActivity extends AppCompatActivity {

    interface OnClickListener{
        void onClick(int channel, boolean checked);
    }

    private RecyclerView switcherStatusView;
    private SwitcherDevice switcherDevice;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switcher_setting);

        Intent intent = getIntent();
        switcherDevice = (SwitcherDevice) intent.getSerializableExtra("data");

        switcherStatusView = (RecyclerView) findViewById(R.id.switcher_status_view);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        switcherStatusView.setLayoutManager(layoutManager);

        MyAdapter adapter = new MyAdapter(switcherDevice.getChannels());
        adapter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(int channel, boolean checked) {
                if(checked){
                    switcherDevice.set(channel);
                }else{
                    switcherDevice.reset(channel);
                }

                Intent intent  = new Intent(AccessoryContact.CommandText.Update);
                intent.putExtra("device", switcherDevice);
                sendBroadcast(intent);
            }
        });
        switcherStatusView.setAdapter(adapter);

        closeButton = (Button) findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private int channels;
        private OnClickListener onClickListener;

        MyAdapter(int channels) {
            this.channels = channels;
        }

        public void setOnClickListener(OnClickListener onClickListener){
            this.onClickListener = onClickListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final ToggleButton button = new ToggleButton(parent.getContext());
            button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
            button.setChecked(false);
            button.setTextOn(getString(R.string.title_switch_status_off));
            button.setTextOff(getString(R.string.title_switch_status_on));

            return new MyViewHolder(button);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final MyViewHolder h = (MyViewHolder) holder;

            if (position >= channels) {
                h.button.setChecked(false);
                h.button.setEnabled(false);
            } else {
                h.button.setChecked(switcherDevice.getStatus(position));
                h.button.setEnabled(true);
            }

            if(onClickListener!=null){
                h.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickListener.onClick(position, h.button.isChecked());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return 8;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ToggleButton button;

            public MyViewHolder(View itemView) {
                super(itemView);

                button = (ToggleButton) itemView;
            }
        }
    }
}
