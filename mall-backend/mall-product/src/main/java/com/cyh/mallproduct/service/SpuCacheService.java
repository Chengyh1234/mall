package com.cyh.mallproduct.service;

import com.cyh.mallproduct.vo.SpuAdminDetailVo;
import com.cyh.mallproduct.vo.SpuDetailVo;
import com.cyh.mallproduct.vo.SpuSellerDetailVo;

public interface SpuCacheService {

    SpuDetailVo getSpuDetail(Long spuId);

    void setSpuDetail(Long spuId, SpuDetailVo vo);

    SpuSellerDetailVo getSpuSellerDetail(Long spuId);

    void setSpuSellerDetail(Long spuId, SpuSellerDetailVo vo);

    SpuAdminDetailVo getSpuAdminDetail(Long spuId);

    void setSpuAdminDetail(Long spuId, SpuAdminDetailVo vo);

    void clearSpuDetailCache(Long spuId);
}