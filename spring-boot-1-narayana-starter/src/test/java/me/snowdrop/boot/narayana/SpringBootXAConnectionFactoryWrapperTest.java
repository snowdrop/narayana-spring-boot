package me.snowdrop.boot.narayana;

import javax.jms.XAConnectionFactory;

import me.snowdrop.boot.narayana.core.jms.NarayanaXAConnectionFactoryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class SpringBootXAConnectionFactoryWrapperTest {

    @Mock
    private NarayanaXAConnectionFactoryWrapper mockDelegate;

    @Mock
    private XAConnectionFactory mockXaConnectionFactory;

    @Test
    public void shouldDelegateToNarayanaWrapper() {
        SpringBootXAConnectionFactoryWrapper wrapper = new SpringBootXAConnectionFactoryWrapper(this.mockDelegate);
        wrapper.wrapConnectionFactory(this.mockXaConnectionFactory);
        verify(this.mockDelegate).wrapConnectionFactory(this.mockXaConnectionFactory);
    }

}