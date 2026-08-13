package com.easy.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.ai.entity.BrowseHistory;
import com.easy.ai.mapper.BrowseHistoryMapper;
import com.easy.ai.service.BrowseHistoryService;
import org.springframework.stereotype.Service;

@Service
public class BrowseHistoryServiceImpl extends ServiceImpl<BrowseHistoryMapper, BrowseHistory> implements BrowseHistoryService {
}
