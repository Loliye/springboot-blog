package blog.service;

import blog.model.Vo.RelationshipVoKey;

import java.util.List;

public interface RelationshipService
{
    void deleteById(Integer cid, Integer mid);

    Long countById(Integer cid, Integer mid);

    void insertVo(RelationshipVoKey relationshipVoKey);

    List<RelationshipVoKey> getRelationshipById(Integer cid, Integer mid);
}
