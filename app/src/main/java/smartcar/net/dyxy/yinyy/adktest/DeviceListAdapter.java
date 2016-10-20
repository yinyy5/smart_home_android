package smartcar.net.dyxy.yinyy.adktest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import smartcar.net.dyxy.yinyy.adktest.db.DeviceDao;
import smartcar.net.dyxy.yinyy.adktest.device.Device;
import smartcar.net.dyxy.yinyy.adktest.device.UnknownDevice;
import smartcar.net.dyxy.yinyy.adktest.device.switcher.SwitcherDevice;
import smartcar.net.dyxy.yinyy.adktest.device.switcher.SwitcherSettingActivity;

/**
 * Created by yinyy on 2016/10/10.
 */

public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Device> devices;

    public DeviceListAdapter(Context context, List<Device> devices) {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder;

        switch (viewType) {
            case Device.Type.Switcher:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_switcher, parent, false);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, SwitcherSettingActivity.class);
                        intent.putExtra("data", (Device) v.getTag());
                        context.startActivity(intent);
                    }
                });
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //TODO:增加修改设备名称的代码
                        return true;
                    }
                });

                holder = new SwitcherViewHolder(view);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unknown_device, parent, false);

                holder = new UnknownViewHolder(view);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceDao dao = new DeviceDao(context);
        Device device = devices.get(position);
        String title = dao.getTitle(device.getAddress());

        if(device instanceof  SwitcherDevice){
            SwitcherViewHolder viewHolder = (SwitcherViewHolder) holder;

            viewHolder.deviceDescription.setText(device.getDescription());
            viewHolder.deviceImage.setImageResource(R.drawable.ic_switcher);
            if (title == null) {
                viewHolder.deviceNickname.setText(device.getAddress());
            } else {
                viewHolder.deviceNickname.setText(title);
            }

            //根据通道数量设置界面
            SwitcherDevice switcher = (SwitcherDevice) device;
            SwitcherViewHolder switcherViewHolder = (SwitcherViewHolder) holder;
            for (int i = 0; i < switcher.getChannels(); i++) {
                switcherViewHolder.set(i, switcher.getStatus(i));
            }
        }else if(device instanceof UnknownDevice){
            UnknownViewHolder viewHolder = (UnknownViewHolder) holder;

            viewHolder.deviceNickname.setText("未知设备");
            viewHolder.deviceImage.setImageResource(R.drawable.ic_default);
        }

        holder.itemView.setTag(device);
    }

    @Override
    public int getItemViewType(int position) {
        Device device = devices.get(position);
        if (device instanceof SwitcherDevice) {
            return Device.Type.Switcher;
        } else {
            return Device.Type.Unknown;
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class SwitcherViewHolder extends RecyclerView.ViewHolder {
        TextView deviceNickname;
        TextView deviceDescription;
        ImageView deviceImage;
        List<TextView> statusText;

        SwitcherViewHolder(View view) {
            super(view);

            deviceDescription = (TextView) view.findViewById(R.id.device_description_text);
            deviceImage = (ImageView) view.findViewById(R.id.device_image);
            deviceNickname = (TextView) view.findViewById(R.id.device_nickname_text);

            statusText = new LinkedList<>();
            statusText.add((TextView) view.findViewById(R.id.channel_status_1));
            statusText.add((TextView) view.findViewById(R.id.channel_status_2));
            statusText.add((TextView) view.findViewById(R.id.channel_status_3));
            statusText.add((TextView) view.findViewById(R.id.channel_status_4));
            statusText.add((TextView) view.findViewById(R.id.channel_status_5));
            statusText.add((TextView) view.findViewById(R.id.channel_status_6));
            statusText.add((TextView) view.findViewById(R.id.channel_status_7));
            statusText.add((TextView) view.findViewById(R.id.channel_status_8));
        }

        public void set(int channel, boolean status) {
            statusText.get(channel).setBackgroundResource(status ? R.drawable.ic_on : R.drawable.ic_off);
            statusText.get(channel).setVisibility(View.VISIBLE);
        }
    }

    class UnknownViewHolder extends RecyclerView.ViewHolder {
        TextView deviceNickname;
        TextView deviceDescription;
        ImageView deviceImage;

        UnknownViewHolder(View view) {
            super(view);

            deviceDescription = (TextView) view.findViewById(R.id.device_description_text);
            deviceImage = (ImageView) view.findViewById(R.id.device_image);
            deviceNickname = (TextView) view.findViewById(R.id.device_nickname_text);
        }
    }
}
