package com.claropr.service.impl;

import com.claropr.dao.SirDao;
import com.claropr.model.LovItem;
import com.claropr.service.SirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SirServiceImpl implements SirService {

    @Autowired
    private SirDao sirDao;

    @Override
    public List<LovItem> getOficinasComerciales() {
        return sirDao.getOficinasComerciales();
    }
}


