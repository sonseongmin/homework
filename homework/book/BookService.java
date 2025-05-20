package homework.book;

import homework.book.advice.BusinessException;
import com.basic.myspringboot.exception.advice.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final BookDetailRepository bookDetailRepository;

    public List<BookDTO.Response> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    public BookDTO.Response getBookById(Long id) {
        Book book = bookRepository.findByIdWithBookDetail(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));
        return BookDTO.Response.fromEntity(book);
    }

    public BookDTO.Response getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbnWithBookDetail(isbn)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "ISBN", isbn));
        return BookDTO.Response.fromEntity(book);
    }

    public List<BookDTO.Response> getBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author)
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    public List<BookDTO.Response> getBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    @Transactional
    public BookDTO.Response createBook(BookDTO.Request request) {
        // Validate ISBN is not already in use
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(ErrorCode.ISBN_DUPLICATE, request.getIsbn());
        }

        // Create book entity
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .publishDate(request.getPublishDate())
                .build();

        // Create book detail if provided
        if (request.getDetailRequest() != null) {
            BookDetail bookDetail = BookDetail.builder()
                    .description(request.getDetailRequest().getDescription())
                    .language(request.getDetailRequest().getLanguage())
                    .pageCount(request.getDetailRequest().getPageCount())
                    .publisher(request.getDetailRequest().getPublisher())
                    .coverImageUrl(request.getDetailRequest().getCoverImageUrl())
                    .edition(request.getDetailRequest().getEdition())
                    //연관관계 저장
                    .book(book)
                    .build();
            //연관관계 저장
            book.setBookDetail(bookDetail);
        }

        // Save and return the book
        Book savedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(savedBook);
    }

    @Transactional
    public BookDTO.Response updateBook(Long id, BookDTO.Request request) {
        // Find the book
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));

        // Check if another book already has the ISBN
        if (!book.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(ErrorCode.ISBN_DUPLICATE, request.getIsbn());
        }

        // Update book basic info
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPrice(request.getPrice());
        book.setPublishDate(request.getPublishDate());

        // Update book detail if provided
        if (request.getDetailRequest() != null) {
            BookDetail bookDetail = book.getBookDetail();

            // Create new detail if not exists
            if (bookDetail == null) {
                bookDetail = new BookDetail();
                bookDetail.setBook(book);
                book.setBookDetail(bookDetail);
            }

            // Update detail fields
            bookDetail.setDescription(request.getDetailRequest().getDescription());
            bookDetail.setLanguage(request.getDetailRequest().getLanguage());
            bookDetail.setPageCount(request.getDetailRequest().getPageCount());
            bookDetail.setPublisher(request.getDetailRequest().getPublisher());
            bookDetail.setCoverImageUrl(request.getDetailRequest().getCoverImageUrl());
            bookDetail.setEdition(request.getDetailRequest().getEdition());
        }

        // Save and return updated book
        Book updatedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(updatedBook);
    }

    // 부분 업데이트 메서드 (새로 추가)
    @Transactional
    public BookDTO.Response partialUpdateBook(Long id, BookDTO.PatchRequest request) {
        // Find the book
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));

        // Update only provided fields
        if (request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }

        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }

        if (request.getIsbn() != null) {
            // Check if another book already has the ISBN
            if (!book.getIsbn().equals(request.getIsbn()) &&
                    bookRepository.existsByIsbn(request.getIsbn())) {
                throw new BusinessException(ErrorCode.ISBN_DUPLICATE, request.getIsbn());
            }
            book.setIsbn(request.getIsbn());
        }

        if (request.getPrice() != null) {
            book.setPrice(request.getPrice());
        }

        if (request.getPublishDate() != null) {
            book.setPublishDate(request.getPublishDate());
        }

        // Update book detail if provided
        if (request.getDetailRequest() != null) {
            BookDetail bookDetail = book.getBookDetail();

            // Create new detail if not exists
            if (bookDetail == null) {
                bookDetail = new BookDetail();
                bookDetail.setBook(book);
                book.setBookDetail(bookDetail);
            }

            // Update only provided detail fields
            BookDTO.BookDetailPatchRequest detailRequest = request.getDetailRequest();

            if (detailRequest.getDescription() != null) {
                bookDetail.setDescription(detailRequest.getDescription());
            }
            if (detailRequest.getLanguage() != null) {
                bookDetail.setLanguage(detailRequest.getLanguage());
            }
            if (detailRequest.getPageCount() != null) {
                bookDetail.setPageCount(detailRequest.getPageCount());
            }
            if (detailRequest.getPublisher() != null) {
                bookDetail.setPublisher(detailRequest.getPublisher());
            }
            if (detailRequest.getCoverImageUrl() != null) {
                bookDetail.setCoverImageUrl(detailRequest.getCoverImageUrl());
            }
            if (detailRequest.getEdition() != null) {
                bookDetail.setEdition(detailRequest.getEdition());
            }
        }

        // Save and return updated book
        Book updatedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(updatedBook);
    }

    // BookDetail 만 업데이트 하는 메서드 (새로 추가)
    @Transactional
    public BookDTO.Response updateBookDetail(Long id, BookDTO.BookDetailPatchRequest request) {
        // Find the book
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));

        BookDetail bookDetail = book.getBookDetail();

        // Create new detail if not exists
        if (bookDetail == null) {
            bookDetail = new BookDetail();
            bookDetail.setBook(book);
            book.setBookDetail(bookDetail);
        }

        // Update only provided fields
        if (request.getDescription() != null) {
            bookDetail.setDescription(request.getDescription());
        }
        if (request.getLanguage() != null) {
            bookDetail.setLanguage(request.getLanguage());
        }
        if (request.getPageCount() != null) {
            bookDetail.setPageCount(request.getPageCount());
        }
        if (request.getPublisher() != null) {
            bookDetail.setPublisher(request.getPublisher());
        }
        if (request.getCoverImageUrl() != null) {
            bookDetail.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getEdition() != null) {
            bookDetail.setEdition(request.getEdition());
        }

        // Save and return updated book
        Book updatedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id);
        }
        bookRepository.deleteById(id);
    }
}