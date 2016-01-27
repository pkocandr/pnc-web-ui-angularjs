/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordSetRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

import static org.jboss.pnc.spi.datastore.predicates.ProductMilestonePredicates.withProductVersionId;

@Stateless
public class ProductMilestoneProvider extends AbstractProvider<ProductMilestone, ProductMilestoneRest> {

    private BuildRecordSetRepository buildRecordSetRepository;

    private ProductVersionRepository productVersionRepository;

    @Inject
    public ProductMilestoneProvider(BuildRecordSetRepository buildRecordSetRepository, 
            ProductMilestoneRepository productMilestoneRepository,
            ProductVersionRepository productVersionRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        super(productMilestoneRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildRecordSetRepository = buildRecordSetRepository;
        this.productVersionRepository = productVersionRepository;
    }

    // needed for EJB/CDI
    public ProductMilestoneProvider() {
    }

    public CollectionInfo<ProductMilestoneRest> getAllForProductVersion(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer versionId) {
        return super.queryForCollection(pageIndex, pageSize,sortingRsql, query, withProductVersionId(versionId));
    }

    @Override
    protected Function<? super ProductMilestone, ? extends ProductMilestoneRest> toRESTModel() {
        return productMilestone -> new ProductMilestoneRest(productMilestone);
    }

    @Override
    protected Function<? super ProductMilestoneRest, ? extends ProductMilestone> toDBModel() {
        return productMilestoneRest -> {
            ProductMilestone.Builder builder = productMilestoneRest.toDBEntityBuilder();
            if(productMilestoneRest.getId() == null) {
                // When creating a new milestone, we need to also create a new performed and distributed build record set
                BuildRecordSet distributedBuildRecordSet = BuildRecordSet.Builder.newBuilder().build();
                builder.distributedBuildRecordSet(buildRecordSetRepository.save(distributedBuildRecordSet));
                BuildRecordSet performedBuildRecordSet = BuildRecordSet.Builder.newBuilder().build();
                builder.performedBuildRecordSet(buildRecordSetRepository.save(performedBuildRecordSet));
            } else {
                // When updating a milestone, the record sets and the product version may not change
                ProductMilestone milestone = repository.queryById(productMilestoneRest.getId());
                builder.distributedBuildRecordSet(milestone.getDistributedBuildRecordSet());
                builder.performedBuildRecordSet(milestone.getPerformedBuildRecordSet());
                builder.productVersion(milestone.getProductVersion());
            }
            return builder.build();
        };
    }

}
