package com.easy.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.ai.entity.KnowledgeDoc;
import com.easy.ai.mapper.KnowledgeDocMapper;
import com.easy.ai.service.KnowledgeDocService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeDocServiceImpl extends ServiceImpl<KnowledgeDocMapper, KnowledgeDoc> implements KnowledgeDocService {
}