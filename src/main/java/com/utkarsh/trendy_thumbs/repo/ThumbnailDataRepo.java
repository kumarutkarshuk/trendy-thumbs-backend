package com.utkarsh.trendy_thumbs.repo;

import com.utkarsh.trendy_thumbs.model.ThumbnailData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThumbnailDataRepo extends JpaRepository<ThumbnailData, String> {
}
