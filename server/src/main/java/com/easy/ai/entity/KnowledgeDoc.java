package com.easy.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_doc")
public class KnowledgeDoc {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    /** 文档类型：text-纯文本，pdf-PDF文档 */
    private String type;

    /** 文档内容（纯文本存储） */
    private String content;

    /** 分块数量 */
    private Integer chunkCount;

    /** 向量化状态：0-未向量化，1-已向量化 */
    private Integer vectorStatus;

    /** 文件存储路径 */
    private String filePath;

    /** 文件大小(字节) */
    private Long fileSize;

    private String creator;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}