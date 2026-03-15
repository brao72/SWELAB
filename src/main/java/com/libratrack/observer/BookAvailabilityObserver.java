package com.libratrack.observer;

public interface BookAvailabilityObserver {
    void onBookAvailable(int bookId, String bookTitle);
}
