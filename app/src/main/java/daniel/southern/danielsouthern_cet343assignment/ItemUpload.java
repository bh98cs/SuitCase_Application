package daniel.southern.danielsouthern_cet343assignment;

public class ItemUpload {
    //TODO: Add code to handle uploading item image
    private String itemTitle;
    private String itemDesc;
    private String itemLink;
    private String email;
    private String itemPrice;
    private boolean itemBought;
    private String imageDownloadUrl;

    public ItemUpload(){
        //empty constructor needed
    }

    public ItemUpload(String itemTitle, String itemDesc, String itemLink, String email, String itemPrice, boolean itemBought, String imageDownloadUrl){
        this.itemTitle = itemTitle;
        this.itemDesc = itemDesc;
        this.itemLink = itemLink;
        this.email = email;
        this.itemPrice = itemPrice;
        //set item bought to false when a new item is created
        this.itemBought = itemBought;
        this.imageDownloadUrl = imageDownloadUrl;
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

    public String getImageDownloadUrl() {
        return imageDownloadUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getItemPrice() {
        return itemPrice;
    }
}
