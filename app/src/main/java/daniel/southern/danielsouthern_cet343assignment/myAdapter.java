package daniel.southern.danielsouthern_cet343assignment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class myAdapter extends FirestoreRecyclerAdapter<ItemUpload, myAdapter.ItemUploadHolder> {

    private OnItemClickListener listener;
    public static final String KEY_DESCRIPTION = "desc";

    public myAdapter(@NonNull FirestoreRecyclerOptions<ItemUpload> options){ super(options);}

    @NonNull
    @Override
    public ItemUploadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_item, parent, false);
        return new ItemUploadHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull ItemUploadHolder holder, int position, @NonNull ItemUpload model) {
        holder.textViewTitle.setText(model.getItemTitle());
        holder.textViewDesc.setText(model.getItemDesc());
        holder.textViewLink.setText(model.getItemLink());
    }

    public void deleteItem(int position){
        //reference to firestore document
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public void updateItem(int position){
        //TODO: Look into this method and allow for user to update all fields
        String desc = "changed description";
        Map<String, Object> itemUpload = new HashMap<>();
        itemUpload.put(KEY_DESCRIPTION, desc);
        getSnapshots().getSnapshot(position).getReference().set(itemUpload, SetOptions.merge());
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot,  int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


    class ItemUploadHolder extends RecyclerView.ViewHolder{
        TextView textViewTitle;
        TextView textViewDesc;
        TextView textViewLink;

        public ItemUploadHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_itemTitle);
            textViewDesc = itemView.findViewById(R.id.text_view_itemDesc);
            textViewLink = itemView.findViewById(R.id.text_view_itemLink);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    //validation
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

        }

    }

}
