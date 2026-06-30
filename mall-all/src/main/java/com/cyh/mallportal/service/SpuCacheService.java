package com.cyh.mallportal.service;

import com.cyh.mallportal.vo.SpuAdminDetailVo;
import com.cyh.mallportal.vo.SpuDetailVo;
import com.cyh.mallportal.vo.SpuSellerDetailVo;

public interface SpuCacheService {

    SpuDetailVo getSpuDetail(Long spuId);

    void setSpuDetail(Long spuId, SpuDetailVo vo);

    SpuSellerDetailVo getSpuSellerDetail(Long spuId);

    void setSpuSellerDetail(Long spuId, SpuSellerDetailVo vo);

    SpuAdminDetailVo getSpuAdminDetail(Long spuId);

    void setSpuAdminDetail(Long spuId, SpuAdminDetailVo vo);

    void clearSpuDetailCache(Long spuId);
}
