package com.example.goodreads.service;

import com.example.goodreads.model.BookRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;

import com.example.goodreads.model.Book;
import com.example.goodreads.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;

@Service
@Primary
public class BookH2Service implements BookRepository {

    @Autowired
    private JdbcTemplate db;

    // ---------------------- GET BY ID ----------------------
    @Override
    @Cacheable(value = "books", key = "#bookId")   // Cache single book
    public Book getBookById(int bookId) {
        try {
            Book book = db.queryForObject("select * from book where id = ?", new BookRowMapper(), bookId);
            return book;
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    // ---------------------- GET ALL BOOKS ----------------------
    @Override
    @Cacheable(value = "booksList")  // Cache all books list
    public ArrayList<Book> getBooks() {
        List<Book> bookList = db.query("select * from book", new BookRowMapper());
        ArrayList<Book> books = new ArrayList<>(bookList);
        return books;
    }

    // ---------------------- ADD BOOK ----------------------
    @Override
    @Caching(
            put = { @CachePut(value = "books", key = "#result.id") },              // Update cache with new book
            evict = { @CacheEvict(value = "booksList", allEntries = true) }       // Clear list cache
    )
    public Book addBook(Book book) {
        db.update("insert into book(name, imageUrl) values (?, ?)", book.getName(), book.getImageUrl());

        Book savedBook = db.queryForObject("select * from book where name = ? and imageUrl = ?",
                new BookRowMapper(), book.getName(), book.getImageUrl());

        return savedBook;
    }

    // ---------------------- UPDATE BOOK ----------------------
    @Override
    @Caching(
            put = { @CachePut(value = "books", key = "#bookId") },                // Update single book cache
            evict = { @CacheEvict(value = "booksList", allEntries = true) }       // Clear list cache
    )
    public Book updateBook(int bookId, Book book) {
        if (book.getName() != null) {
            db.update("update book set name = ? where id = ?", book.getName(), bookId);
        }
        if (book.getImageUrl() != null) {
            db.update("update book set imageUrl = ? where id = ?", book.getImageUrl(), bookId);
        }
        return getBookById(bookId);
    }

    // ---------------------- DELETE BOOK ----------------------
    @Override
    @Caching(evict = {
            @CacheEvict(value = "books", key = "#bookId"),                       // Remove single book cache
            @CacheEvict(value = "booksList", allEntries = true)                  // Clear list cache
    })
    public void deleteBook(int bookId) {
        db.update("delete from book where id = ?", bookId);
    }

}
