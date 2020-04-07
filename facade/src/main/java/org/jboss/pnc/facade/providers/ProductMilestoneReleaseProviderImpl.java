/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.MilestoneReleaseStatus;
import org.jboss.pnc.facade.providers.api.ProductMilestoneReleaseProvider;
import org.jboss.pnc.mapper.api.ProductMilestoneReleaseMapper;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.spi.datastore.predicates.ProductMilestoneReleasePredicates;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@PermitAll
@Stateless
public class ProductMilestoneReleaseProviderImpl extends
        AbstractIntIdProvider<org.jboss.pnc.model.ProductMilestoneRelease, org.jboss.pnc.dto.ProductMilestoneRelease, org.jboss.pnc.dto.ProductMilestoneReleaseRef>
        implements ProductMilestoneReleaseProvider {

    private static final Logger log = LoggerFactory.getLogger(ProductMilestoneReleaseProviderImpl.class);
    private ProductMilestoneReleaseRepository repository;

    @Inject
    public ProductMilestoneReleaseProviderImpl(
            ProductMilestoneReleaseRepository repository,
            ProductMilestoneReleaseMapper mapper) {
        super(repository, mapper, ProductMilestoneRelease.class);
        this.repository = repository;
    }

    @Override
    public org.jboss.pnc.dto.ProductMilestoneRelease getLatestProductMilestoneRelease(int milestoneId) {
        ProductMilestoneRelease entity = repository
                .findLatestByMilestone(ProductMilestone.Builder.newBuilder().id(milestoneId).build());
        return mapper.toDTO(entity);
    }

    @Override
    public Page<org.jboss.pnc.dto.ProductMilestoneRelease> getProductMilestoneReleases(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            int milestoneId,
            boolean latest,
            boolean runningOnly) {

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(ProductMilestoneReleasePredicates.withMilestoneId(milestoneId));
        if (runningOnly) {
            predicates.add(ProductMilestoneReleasePredicates.withStatus(MilestoneReleaseStatus.IN_PROGRESS));
        }
        if (StringUtils.isEmpty(sortingRsql)) {
            log.debug("No sort provided, using the default 'ASC by startingDate'.");
            sortingRsql = "sort=asc=startingDate";
        }

        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                predicates.toArray(new Predicate[predicates.size()]));
    }
}
