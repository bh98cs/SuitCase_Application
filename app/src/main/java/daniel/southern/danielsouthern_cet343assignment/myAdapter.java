package daniel.southern.danielsouthern_cet343assignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class myAdapter extends RecyclerView.Adapter<myAdapter.TextViewHolder> {
    private Context mContext;
    private List<ItemUpload> mItemUploads;

    //constructor for class
    public myAdapter(Context context, List<ItemUpload> itemUploads){
        mContext = context;
        mItemUploads = itemUploads;
    }

    @NonNull
    @Override
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.data_item, parent, false);
        return new TextViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TextViewHolder holder, int position) {
        //TODO: Add code to handle item image
        ItemUpload uploadCurrent = mItemUploads.get(position);
        holder.itemTitle.setText(uploadCurrent.getItemTitle());
        holder.itemDesc.setText(uploadCurrent.getItemDesc());
        holder.itemLink.setText(uploadCurrent.getItemLink());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class TextViewHolder extends RecyclerView.ViewHolder{

        //TODO: Add code to handle item image
        public TextView itemTitle;
        public TextView itemDesc;
        public TextView itemLink;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.text_view_itemTitle);
            itemDesc = itemView.findViewById(R.id.text_view_itemDesc);
            itemLink = itemView.findViewById(R.id.text_view_itemLink);
        }
    }
}
