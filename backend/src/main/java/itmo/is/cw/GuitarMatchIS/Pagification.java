package itmo.is.cw.GuitarMatchIS;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.validation.ValidationException;

public class Pagification {

    public static Pageable createPageTemplate(Integer from, Integer size) {
        Pageable page = null;
        if (from != null && size != null) {
            if (from < 0 || size <= 0)
                throw new ValidationException(
                        "The pagination parameters should be as follows: from >= 0 and size > 0.");
            page = PageRequest.of(from / size, size);
        }
        return page;
    }

}