import React, { useState } from 'react';
import BookForm from './BookForm';
import BookTable from './BookTable';

const BookManager = () => {
  const [books, setBooks] = useState([]);

  const addBook = (book) => {
    setBooks([...books, book]);
  };

  const deleteBook = (index) => {
    const updated = [...books];
    updated.splice(index, 1);
    setBooks(updated);
  };

  return (
    <div>
      <h1>도서 관리 시스템</h1>
      <BookForm onAdd={addBook} />
      <BookTable books={books} onDelete={deleteBook} />
    </div>
  );
};

export default BookManager;
