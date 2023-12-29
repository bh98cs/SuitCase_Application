package daniel.southern.danielsouthern_cet343assignment;

public class ItemUpload {
    //TODO: Add code to handle uploading item image
    private String itemTitle;
    private String itemDesc;
    private String itemLink;
    private boolean itemBought;

    public ItemUpload(){
        //empty constructor needed
    }

    public ItemUpload(String itemTitle, String itemDesc, String itemLink, boolean itemBought){
        this.itemTitle = itemTitle;
        this.itemDesc = itemDesc;
        this.itemLink = itemLink;
        //set item bought to false when a new item is created
        this.itemBought = itemBought;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public String getItemLink() {
        return itemLink;
    }

    public Boolean getItemBought() {
        return itemBought;
    }
}
