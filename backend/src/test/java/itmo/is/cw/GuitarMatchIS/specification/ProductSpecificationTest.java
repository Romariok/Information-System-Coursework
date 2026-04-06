package itmo.is.cw.GuitarMatchIS.specification;

import itmo.is.cw.GuitarMatchIS.models.*;
import itmo.is.cw.GuitarMatchIS.repository.specification.ProductSpecification;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductSpecificationTest {

    @Mock
    private Root<Product> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> brandPath;

    @Mock
    private Path<Object> brandIdPath;

    @Mock
    private Path<String> namePath;

    @Mock
    private Path<Object> ratePath;

    @Mock
    private Path<Object> ladsPath;

    @Mock
    private Path<Object> avgPricePath;

    @Mock
    private Path<Object> stringsPath;

    @Mock
    private Predicate predicate;

    // --- hasBrand ---

    @Test
    void hasBrand_null_returnsNullPredicate() {
        Specification<Product> spec = ProductSpecification.hasBrand(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    void hasBrand_valid_returnsEqualPredicate() {
        when(root.get("brand")).thenReturn(brandPath);
        when(brandPath.get("id")).thenReturn(brandIdPath);
        when(cb.equal(brandIdPath, 1L)).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.hasBrand(1L);
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb).equal(brandIdPath, 1L);
    }

    // --- hasName ---

    @Test
    void hasName_null_returnsNullPredicate() {
        Specification<Product> spec = ProductSpecification.hasName(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    void hasName_empty_returnsNullPredicate() {
        Specification<Product> spec = ProductSpecification.hasName("");
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    void hasName_valid_returnsLikePredicate() {
        when(root.<String>get("name")).thenReturn(namePath);
        when(cb.like(namePath, "%Gibson%")).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.hasName("Gibson");
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb).like(namePath, "%Gibson%");
    }

    // --- hasRateBetween ---

    @Test
    void hasRateBetween_oneParamNull_returnsNullPredicate() {
        Specification<Product> specMinNull = ProductSpecification.hasRateBetween(null, 5.0f);
        assertThat(specMinNull.toPredicate(root, query, cb)).isNull();

        Specification<Product> specMaxNull = ProductSpecification.hasRateBetween(3.0f, null);
        assertThat(specMaxNull.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void hasRateBetween_valid_returnsBetweenPredicate() {
        when(root.get("rate")).thenReturn(ratePath);
        when(cb.between(any(Expression.class), eq(3.0f), eq(5.0f))).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.hasRateBetween(3.0f, 5.0f);
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb).between(any(Expression.class), eq(3.0f), eq(5.0f));
    }

    // --- hasLads ---

    @Test
    void hasLads_zero_returnsNullPredicate() {
        Specification<Product> spec = ProductSpecification.hasLads(0);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    void hasLads_negative_returnsNullPredicate() {
        Specification<Product> spec = ProductSpecification.hasLads(-1);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    void hasLads_null_returnsNullPredicate() {
        Specification<Product> spec = ProductSpecification.hasLads(null);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    void hasLads_valid_returnsEqualPredicate() {
        when(root.get("lads")).thenReturn(ladsPath);
        when(cb.equal(ladsPath, 22)).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.hasLads(22);
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb).equal(ladsPath, 22);
    }

    // --- hasPriceBetween ---

    @Test
    void hasPriceBetween_oneParamNull_returnsNullPredicate() {
        Specification<Product> specMinNull = ProductSpecification.hasPriceBetween(null, 2000.0);
        assertThat(specMinNull.toPredicate(root, query, cb)).isNull();

        Specification<Product> specMaxNull = ProductSpecification.hasPriceBetween(500.0, null);
        assertThat(specMaxNull.toPredicate(root, query, cb)).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void hasPriceBetween_valid_returnsBetweenPredicate() {
        when(root.get("avgPrice")).thenReturn(avgPricePath);
        when(cb.between(any(Expression.class), eq(500.0), eq(2000.0))).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.hasPriceBetween(500.0, 2000.0);
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb).between(any(Expression.class), eq(500.0), eq(2000.0));
    }

    // --- hasStrings ---

    @Test
    void hasStrings_zero_returnsNullPredicate() {
        Specification<Product> spec = ProductSpecification.hasStrings(0);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    void hasStrings_negative_returnsNullPredicate() {
        Specification<Product> spec = ProductSpecification.hasStrings(-1);
        assertThat(spec.toPredicate(root, query, cb)).isNull();
    }

    @Test
    void hasStrings_valid_returnsEqualPredicate() {
        when(root.get("strings")).thenReturn(stringsPath);
        when(cb.equal(stringsPath, 6)).thenReturn(predicate);

        Specification<Product> spec = ProductSpecification.hasStrings(6);
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb).equal(stringsPath, 6);
    }
}
