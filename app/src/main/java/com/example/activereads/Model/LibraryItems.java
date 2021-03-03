package com.example.activereads.Model;

public class LibraryItems {
    String bookName;
    String link;
    String category;

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
    public String getBookName() {
        return bookName;
    }
    public String getLink() {
        return link;
    }
}
