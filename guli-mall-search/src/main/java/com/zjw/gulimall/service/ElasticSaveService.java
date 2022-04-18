package com.zjw.gulimall.service;

import com.zjw.common.to.SkuEsModel;
import com.zjw.common.utils.R;
import org.bouncycastle.pqc.jcajce.provider.qtesla.SignatureSpi;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 17:27
 * @Modifier:
 */

public interface ElasticSaveService
{

    boolean uploadProductInfoToEs(List<SkuEsModel> modelList);
}
