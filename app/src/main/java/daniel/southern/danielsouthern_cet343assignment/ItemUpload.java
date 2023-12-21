package daniel.southern.danielsouthern_cet343assignment;

public class ItemUpload {
    //TODO: Add code to handle uploading item image
    private String mUser;
    private String mItemTitle;
    private String mItemDesc;
    private String mItemLink;

    public ItemUpload(){
        //empty constructor needed
    }

    public ItemUpload(String user, String itemTitle, String itemDesc, String itemLink){

        if(user.trim().equals("")){
            return;
        }

        if(itemTitle.trim().equals("")){
            itemTitle = "No Title entered.";
        }
        if(itemDesc.trim().equals("")){
            itemDesc = "No item description given.";
        }
        if(itemLink.trim().equals("")){
            itemLink = "Item link not provided.";
        }
    }

    public String getUser(){
        return mUser;
    }

    public void setUser(String user){
        mUser = user;
    }

    /** getter and setter for Item Title **/
    public String getItemTitle(){
        return mItemTitle;
    }
    public void setItemTitle(String itemTitle){
        mItemTitle = itemTitle;
    }

    /** getter and setter for Item Description **/
    public String getItemDesc(){
        return mItemDesc;
    }
    public void setItemDesc(String itemDesc){
        mItemDesc = itemDesc;
    }

    /** getter and setter for Item link **/
    public String getItemLink(){
        return mItemLink;
    }
    public void setItemLink(String itemLink){
        mItemLink = itemLink;
    }

}
