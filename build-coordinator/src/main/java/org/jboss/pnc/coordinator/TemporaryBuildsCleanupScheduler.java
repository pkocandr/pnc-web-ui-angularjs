/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.coordinator;


import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.model.BuildRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.List;

/**
 * Executes regular cleanup of old temporary builds after expiration.
 *
 * @author Jakub Bartecek
 */
@Singleton
public class TemporaryBuildsCleanupScheduler {
    private final Logger log = LoggerFactory.getLogger(TemporaryBuildsCleanupScheduler.class);

    private final int DEFAULT_LIFESPAN = 14;

    private final int TEMPORARY_BUILD_LIFESPAN;

    @Deprecated
    public TemporaryBuildsCleanupScheduler() {
        log.warn("Deprecated constructor used to init TemporaryBuildsCleanupScheduler. Default values will be used.");
        TEMPORARY_BUILD_LIFESPAN = DEFAULT_LIFESPAN;
    }

    @Inject
    public TemporaryBuildsCleanupScheduler(Configuration configuration) {
        int _temporaryBuildLifeSpan;
        try {
            SystemConfig systemConfig = configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class));
            _temporaryBuildLifeSpan = systemConfig.getTemporaryBuildsLifeSpan();
        } catch (ConfigurationParseException e) {
            log.warn("TemporaryBuildsCleanupScheduler initialization from config file failed! Default values will be used.");
            _temporaryBuildLifeSpan = DEFAULT_LIFESPAN;
        }
        this.TEMPORARY_BUILD_LIFESPAN = _temporaryBuildLifeSpan;
    }


    /**
     * Cleanup old temporary builds every midnight
     */
    @Schedule
    public void cleanupExpiredTemporaryBuilds() {
        log.info("Regular cleanup of expired temporary builds started. Removing builds older than " + TEMPORARY_BUILD_LIFESPAN
                + " days.");

        List<BuildRecord> expiredBuilds= null;

        log.info("Regular cleanup of expired temporary builds finished.");
    }
}
