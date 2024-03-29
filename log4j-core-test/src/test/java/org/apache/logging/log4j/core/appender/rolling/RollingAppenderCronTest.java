/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.logging.log4j.core.appender.rolling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.test.junit.LoggerContextSource;
import org.apache.logging.log4j.core.util.CronExpression;
import org.apache.logging.log4j.plugins.Named;
import org.apache.logging.log4j.test.junit.CleanUpDirectories;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DisabledUntil;

/**
 *
 */
@DisabledUntil(date = "2024-04-01", reason = "Temporarily disabled due to deadlocks.")
public class RollingAppenderCronTest extends AbstractRollingListenerTest {

    private static final String CONFIG = "log4j-rolling-cron.xml";
    private static final String DIR = "target/rolling-cron";
    private static final String FILE = "target/rolling-cron/rollingtest.log";
    private final CountDownLatch rollover = new CountDownLatch(2);
    private final CountDownLatch reconfigured = new CountDownLatch(1);

    @Test
    @CleanUpDirectories(DIR)
    @LoggerContextSource(value = CONFIG, timeout = 10)
    public void testAppender(final LoggerContext context, @Named("RollingFile") final RollingFileManager manager)
            throws Exception {
        manager.addRolloverListener(this);
        final Logger logger = context.getLogger(getClass());
        final File file = new File(FILE);
        assertThat(file).exists();
        logger.debug("This is test message number 1");
        rollover.await();

        final File dir = new File(DIR);
        assertThat(dir).isNotEmptyDirectory();
        assertThat(dir).isDirectoryContaining("glob:**.gz");

        final Path src = Path.of("target", "test-classes", "log4j-rolling-cron2.xml");
        context.addConfigurationStartedListener(ignored -> reconfigured.countDown());
        try (final OutputStream os =
                Files.newOutputStream(Path.of("target", "test-classes", "log4j-rolling-cron.xml"))) {
            Files.copy(src, os);
        }
        // force a reconfiguration
        for (int i = 0; i < 20; ++i) {
            logger.debug("Adding new event {}", i);
        }
        reconfigured.await();
        final RollingFileAppender appender = context.getConfiguration().getAppender("RollingFile");
        final TriggeringPolicy policy = appender.getManager().getTriggeringPolicy();
        assertThat(policy).isNotNull();
        assertThat(policy).isInstanceOf(CronTriggeringPolicy.class);
        final CronExpression expression = ((CronTriggeringPolicy) policy).getCronExpression();
        assertEquals("* * * ? * *", expression.getCronExpression(), "Incorrect triggering policy");
    }

    @Override
    public void rolloverComplete(final String fileName) {
        rollover.countDown();
    }
}
