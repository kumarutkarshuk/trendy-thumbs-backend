package com.utkarsh.trendy_thumbs.repo;

import com.utkarsh.trendy_thumbs.model.ThumbnailData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThumbnailDataRepo extends MongoRepository<ThumbnailData, String> {
}
