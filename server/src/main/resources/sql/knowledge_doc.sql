CREATE TABLE `knowledge_doc` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '文档标题',
  `type` varchar(20) NOT NULL DEFAULT 'text' COMMENT '文档类型：text-纯文本，pdf-PDF文档',
  `content` text COMMENT '文档内容（纯文本存储）',
  `chunk_count` int NOT NULL DEFAULT 0 COMMENT '分块数量',
  `vector_status` tinyint NOT NULL DEFAULT 0 COMMENT '向量化状态：0-未向量化，1-已向量化',
  `file_path` varchar(500) DEFAULT NULL COMMENT '文件存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `creator` varchar(50) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识库文档表';