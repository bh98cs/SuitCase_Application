package daniel.southern.danielsouthern_cet343assignment;





import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class myAdapter extends FirestoreRecyclerAdapter<ItemUpload, myAdapter.ItemUploadHolder> {

    //declare listener for clicking on an item
    private OnItemClickListener listener;
    //declare listener for long click on an item
    private OnItemLongClickListener longClickListener;
    //key to change whether item is to be marked as bought
    public static final String KEY_ITEM_BOUGHT = "itemBought";

    //constructor for adapter
    public myAdapter(@NonNull FirestoreRecyclerOptions<ItemUpload> options){
        super(options);
    }

    @NonNull
    @Override
    public ItemUploadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_item, parent, false);
        return new ItemUploadHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull ItemUploadHolder holder, int position, @NonNull ItemUpload model) {
        //set text for items title using ItemUpload title getter
        holder.textViewTitle.setText(model.getItemTitle());
        //set text for items description using ItemUpload description getter
        holder.textViewDesc.setText(model.getItemDesc());
        //set text for items link using ItemUpload link getter
        holder.textViewLink.setText(model.getItemLink());
        //set text for items price using ItemUpload price getter
        holder.textViewPrice.setText("£"+model.getItemPrice());
        //if item is marked as being bought update color
        if(model.getItemBought()){
            //change background color of item's card to indicate it has been bought
            holder.itemBackground.setBackgroundColor(MaterialColors.getColor(holder.itemBackground, com.google.android.material.R.attr.colorSurfaceVariant));
            //update text color on the card to provide sufficient contrast with new background color
            holder.textViewTitle.setTextColor(MaterialColors.getColor(holder.textViewTitle, com.google.android.material.R.attr.colorOnSurfaceVariant));
            holder.textViewDesc.setTextColor(MaterialColors.getColor(holder.textViewTitle, com.google.android.material.R.attr.colorOnSurfaceVariant));
            holder.textViewLink.setTextColor(MaterialColors.getColor(holder.textViewTitle, com.google.android.material.R.attr.colorOnSurfaceVariant));
            holder.textViewPrice.setTextColor(MaterialColors.getColor(holder.textViewTitle, com.google.android.material.R.attr.colorOnSurfaceVariant));
            holder.delegateItem.setColorFilter(MaterialColors.getColor(holder.textViewTitle, com.google.android.material.R.attr.colorOnSurfaceVariant));
        }
        //load the saved image for the item using picasso library
        Picasso.get()
                .load(model.getImageDownloadUrl())
                .fit()
                .centerCrop()
                .into(holder.imageViewImage);
    }

    public void deleteItem(int position){
        //delete item from Firebase
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    //method to return item's FireBase doc ID
    public String getItemFirebaseId(int position){
        //return the id of the item at the specified position in the recyclerview
        return getSnapshots().getSnapshot(position).getReference().getId();
    }

    //method to mark item as bought/not-bought on a long click
    public void changeIsBought(boolean isBought, int position){
        //set itemBought bool to opposite of current bool (allowing user to change between true and false)
        Map<String, Object> itemUpload = new HashMap<>();
        itemUpload.put(KEY_ITEM_BOUGHT, !isBought);
        getSnapshots().getSnapshot(position).getReference().set(itemUpload, SetOptions.merge());
    }


    //Interface for clicking on an item
    public interface OnItemClickListener{
        void onItemDelegateClick(DocumentSnapshot documentSnapshot,  int position);
    }
    //interface for a long click on an item
    public interface OnItemLongClickListener{
        void onItemLongClick(DocumentSnapshot documentSnapshot,  int position);

    }
    //constructor for long click listener
    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener){
        this.longClickListener = longClickListener;
    }

    //constructor for clicking to delegate an item
    public void setOnItemDelegateClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


    class ItemUploadHolder extends RecyclerView.ViewHolder{
        //declare views containing item details
        TextView textViewTitle;
        TextView textViewDesc;
        TextView textViewLink;
        TextView textViewPrice;
        ImageView imageViewImage;
        ImageView delegateItem;
        //item background to change colour to indicate whether it has been bought or not
        RelativeLayout itemBackground;

        public ItemUploadHolder(@NonNull View itemView) {
            super(itemView);
            //initialise views
            textViewTitle = itemView.findViewById(R.id.text_view_itemTitle);
            textViewDesc = itemView.findViewById(R.id.text_view_itemDesc);
            textViewLink = itemView.findViewById(R.id.text_view_itemLink);
            textViewPrice = itemView.findViewById(R.id.textView_itemPrice);
            imageViewImage = itemView.findViewById(R.id.imageView_itemImage);
            itemBackground = itemView.findViewById(R.id.itemCard);
            delegateItem = itemView.findViewById(R.id.imageView_delegateIcon);
            //set on click listener for clicking the delegate icon
            delegateItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get position of the item selected to delegate
                    int position = getAdapterPosition();
                    //check listener and item position are valid
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        //call method to handle delegate icon being clicked
                        listener.onItemDelegateClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            //set long click listener
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //get position of item in recycler view
                    int position = getAdapterPosition();
                    //check there is a valid position and long click listener
                    if(position != RecyclerView.NO_POSITION && longClickListener != null){
                        //call method to handle long click action
                        longClickListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                    }
                    return true;
                }
            });

        }

    }

}
