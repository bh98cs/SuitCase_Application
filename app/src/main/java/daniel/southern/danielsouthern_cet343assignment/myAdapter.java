package daniel.southern.danielsouthern_cet343assignment;




import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class myAdapter extends FirestoreRecyclerAdapter<ItemUpload, myAdapter.ItemUploadHolder> {

    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    public static final String KEY_DESCRIPTION = "itemDesc";
    //key to change whether item is to be marked as bought
    public static final String KEY_ITEM_BOUGHT = "itemBought";


    public myAdapter(@NonNull FirestoreRecyclerOptions<ItemUpload> options){super(options);}

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
        //if item is marked as being bought update color
        if(model.getItemBought()){
            //TODO: change this to a color suiting the color scheme
            holder.itemBackground.setBackgroundColor(Color.GREEN);
        }
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

    //method to mark item as bought/not-bought on a double click
    public void changeIsBought(boolean isBought, int position){
        //set itemBought bool to opposite of current bool (allowing user to change between true and false)
        Map<String, Object> itemUpload = new HashMap<>();
        itemUpload.put(KEY_ITEM_BOUGHT, !isBought);
        getSnapshots().getSnapshot(position).getReference().set(itemUpload, SetOptions.merge());
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot,  int position);
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(DocumentSnapshot documentSnapshot,  int position);

    }
    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener){
        this.longClickListener = longClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


    class ItemUploadHolder extends RecyclerView.ViewHolder{
        TextView textViewTitle;
        TextView textViewDesc;
        TextView textViewLink;
        //item background to change colour to indicate whether it has been bought or not
        CardView itemBackground;

        public ItemUploadHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_itemTitle);
            textViewDesc = itemView.findViewById(R.id.text_view_itemDesc);
            textViewLink = itemView.findViewById(R.id.text_view_itemLink);
            itemBackground = itemView.findViewById(R.id.cardView);

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

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    //validation
                    if(position != RecyclerView.NO_POSITION && longClickListener != null){
                        longClickListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                    }
                    return true;
                }
            });

        }

    }

}
