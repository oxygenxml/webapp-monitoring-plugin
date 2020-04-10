package com.oxygenxml.webapp.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.codahale.metrics.MetricRegistry;
import com.oxygenxml.privilegedlogs.PrivilegedPropertyConfigurator;

import ro.sync.basic.util.StringUtil;

public class PlainTextReporterTest {

  private static PrintStream originalOut;
  private static ByteArrayOutputStream out;

  @BeforeClass
  public static void recordOut() {
    originalOut = System.out;

    out = new ByteArrayOutputStream();
    PrintStream mockPrintStream = new PrintStream(out) {
      @Override
      public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        originalOut.write(buf, off, len);
      }
    };
    System.setOut(mockPrintStream);
  }
  
  @AfterClass
  public static void tearDown() {
    System.setOut(originalOut);
  }
  @Before
  public void before() {
    out.reset();
  }
  /**
   * <p><b>Description:</b> Test that instantiation works.</p>
   * <p><b>Bug ID:</b> WA-3775</p>
   *
   * @author cristi_talau
   *
   * @throws Exception
   */
  @Test
  public void testInstantiation() throws Exception {
    MetricRegistry registry = Mockito.mock(MetricRegistry.class);
    PlainTextReporter reporter = new PlainTextReporter(registry, "Reporter", TimeUnit.SECONDS, TimeUnit.SECONDS);
    reporter.report();
    reporter.close();
    String log = new String(out.toByteArray(), StandardCharsets.US_ASCII);
    assertTrue(log, log.contains("timestamp"));
  }
  

  /**
   * <p><b>Description:</b> Asserts that the custom logger persists after
   * {@code PrivilegedPropertyConfigurator.configure()} is called. 
   * .</p>
   * <p><b>Bug ID:</b> WA-3775</p>
   *
   * @author bogdan_dumitru
   */
  @Test
  public void testInstantiationBeforeConfigLog4j() {
    MetricRegistry registry = Mockito.mock(MetricRegistry.class);
    PlainTextReporter reporter = new PlainTextReporter(registry, "Reporter", TimeUnit.SECONDS, TimeUnit.SECONDS);
    reporter.report();
    String log = new String(out.toByteArray(), StandardCharsets.US_ASCII);
    assertTrue(log, log.contains("timestamp"));

    // Configure log4j2.
    ClassLoader cl = this.getClass().getClassLoader();
    String logFile = new File("target/ignored.log.file.log").getAbsolutePath();
    File optLog4jConfigurationFile = new File("src/test/resources/log4j2.xml");
    PrivilegedPropertyConfigurator.configure(cl, logFile , optLog4jConfigurationFile);

    reporter.report();
    log = new String(out.toByteArray(), StandardCharsets.US_ASCII);
    assertEquals(2, StringUtil.countMatches(log, "timestamp"));
    reporter.close();
  }
}
