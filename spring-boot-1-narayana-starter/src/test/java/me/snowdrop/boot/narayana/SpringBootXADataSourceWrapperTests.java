package me.snowdrop.boot.narayana;

import javax.sql.XADataSource;

import me.snowdrop.boot.narayana.core.jdbc.NarayanaXADataSourceWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class SpringBootXADataSourceWrapperTests {

    @Mock
    private NarayanaXADataSourceWrapper mockDelegate;

    @Mock
    private XADataSource mockXaDataSource;

    @Test
    public void shouldDelegateToNarayanaWrapper() {
        SpringBootXADataSourceWrapper wrapper = new SpringBootXADataSourceWrapper(this.mockDelegate);
        wrapper.wrapDataSource(this.mockXaDataSource);
        verify(this.mockDelegate).wrapDataSource(this.mockXaDataSource);
    }

}