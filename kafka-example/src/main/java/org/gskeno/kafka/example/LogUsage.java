package org.gskeno.kafka.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUsage {
    private static final Logger LOG = LoggerFactory.getLogger(LogUsage.class);
    public static void main(String[] args) {
        LOG.debug("debug");
        LOG.info("info");
    }
}
