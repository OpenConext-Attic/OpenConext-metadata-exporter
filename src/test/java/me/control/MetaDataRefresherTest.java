package me.control;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import me.repository.ServiceRegistryRepository;

@RunWith(MockitoJUnitRunner.class)
public class MetaDataRefresherTest {

  @Mock
  private ServiceRegistryRepository serviceRegistryRepositoryMock;

  private MetaDataRefresher subject = new MetaDataRefresher(serviceRegistryRepositoryMock, 1);

  @Test
  public void afterStartTheHealthStatusShouldBeUp() {
    Health health = subject.health();

    assertThat(health.getStatus(), is(Status.UP));
  }
}
