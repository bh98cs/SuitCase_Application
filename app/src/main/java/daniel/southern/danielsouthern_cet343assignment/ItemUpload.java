package daniel.southern.danielsouthern_cet343assignment;

public class ItemUpload {
    //TODO: Add code to handle uploading item image
    private String itemTitle;
    private String itemDesc;
    private String itemLink;

    public ItemUpload(){
        //empty constructor needed
    }

    public ItemUpload(String itemTitle, String itemDesc, String itemLink){
        this.itemTitle = itemTitle;
        this.itemDesc = itemDesc;
        this.itemLink = itemLink;
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
}
