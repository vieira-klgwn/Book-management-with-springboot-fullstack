package vector.spring_boot_sec_vieira.book;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookRequest {
    private Integer id;
    private String author;
    private String isbn;

}
