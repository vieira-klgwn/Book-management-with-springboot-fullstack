package vector.spring_boot_sec_vieira.book;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public void save (BookRequest bookRequest) {
        var book = Book.builder()
                .id(bookRequest.getId())
                .author(bookRequest.getAuthor())
                .isbn(bookRequest.getIsbn())
                .build();
        bookRepository.save(book);

    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }
}
