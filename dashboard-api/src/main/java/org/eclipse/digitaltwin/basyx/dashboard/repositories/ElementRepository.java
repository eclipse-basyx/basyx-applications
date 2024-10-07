package org.eclipse.digitaltwin.basyx.dashboard.repositories;

import org.eclipse.digitaltwin.basyx.dashboard.models.Element;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ElementRepository extends MongoRepository<Element, String> {
    List<Element> findByGroup_GroupId(String groupId);
}
