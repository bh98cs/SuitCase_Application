package daniel.southern.danielsouthern_cet343assignment;

public class ItemUpload {
    //declare variables to hold details about an Item
    //name of the item
    private String itemTitle;
    //description of the item
    private String itemDesc;
    //link to buy the item
    private String itemLink;
    //email address of user who saved the item
    private String email;
    private String itemPrice;
    //indicate whether item has been bought
    private boolean itemBought;
    //download Url for the saved image for the item
    private String imageDownloadUrl;

    public ItemUpload(){
        //empty constructor needed for Firebase
    }

    //class constructor with parameters
    public ItemUpload(String itemTitle, String itemDesc, String itemLink, String email, String itemPrice, boolean itemBought, String imageDownloadUrl){
        this.itemTitle = itemTitle;
        this.itemDesc = itemDesc;
        this.itemLink = itemLink;
        this.email = email;
        this.itemPrice = itemPrice;
        this.itemBought = itemBought;
        this.imageDownloadUrl = imageDownloadUrl;
    }

    //getter methods for all variables
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

    //unused getter for Email is still required for Firebase
    public String getEmail() {
        return email;
    }

    public String getItemPrice() {
        return itemPrice;
    }
}
