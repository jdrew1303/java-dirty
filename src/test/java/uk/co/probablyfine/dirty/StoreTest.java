package uk.co.probablyfine.dirty;

import org.junit.Before;
import org.junit.Test;
import uk.co.probablyfine.dirty.testobjects.SmallObject;
import uk.co.probablyfine.dirty.testobjects.HasEveryPrimitiveField;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class StoreTest {

  private File storeFile;

  @Before
  public void setUp() throws Exception {
    storeFile = createTempFile();
  }

  @Test
  public void shouldPersistObjectWithDifferentTypedFields() throws Exception {
    Store<HasEveryPrimitiveField> store = Store.of(HasEveryPrimitiveField.class).from(storeFile.getPath());

    store.put(HasEveryPrimitiveField.EXAMPLE);

    List<HasEveryPrimitiveField> collect = store.all().collect(toList());

    assertThat(collect, hasItems(HasEveryPrimitiveField.EXAMPLE));
  }

  @Test
  public void shouldNotSyncToDiskOnEachWrite() throws Exception {
    Store<SmallObject> store = Store.of(SmallObject.class).from(storeFile.getPath());

    long elapsedTime = timeElapsed(() ->
        range(0, 100)
        .mapToObj(SmallObject::new)
        .forEach(store::put)
    );

    assertTrue("Should have taken < 200ms, took " + elapsedTime, elapsedTime < 200);
  }

  @Test
  public void shouldBeAbleToOpenAndReadExistingStoreFile() throws Exception {
    Store<HasEveryPrimitiveField> store = Store.of(HasEveryPrimitiveField.class).from(storeFile.getPath());
    store.put(HasEveryPrimitiveField.EXAMPLE);

    // Load up a new instance from the same file

    store = Store.of(HasEveryPrimitiveField.class).from(storeFile.getPath());
    List<HasEveryPrimitiveField> collect = store.all().collect(toList());

    assertThat(collect.get(0), is(HasEveryPrimitiveField.EXAMPLE));
  }

  private File createTempFile() throws IOException {
    File tempFile = File.createTempFile(randomUUID().toString(), ".dirty");
    tempFile.deleteOnExit();
    return tempFile;
  }

  private long timeElapsed(Runnable r) {
    long time = System.currentTimeMillis();
      r.run();
    return System.currentTimeMillis() - time;
  }

}