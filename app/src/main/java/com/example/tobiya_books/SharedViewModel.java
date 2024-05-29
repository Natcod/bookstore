//package com.example.tobiya_books;
//
//import androidx.lifecycle.LiveData;
//
//import java.util.List;
//
//public class SharedViewModel extends ViewModel {
//    private final MutableLiveData<List<Book>> cart = new MutableLiveData<>(new ArrayList<>());
//
//    public void addBookToCart(Book book) {
//        List<Book> currentBooks = cart.getValue();
//        if (currentBooks != null) {
//            currentBooks.add(book);
//            cart.setValue(currentBooks);
//        }
//    }
//
//    public LiveData<List<Book>> getCart() {
//        return cart;
//    }
//}
//
